package be.codewriter.dmx512.controller;

import be.codewriter.dmx512.client.DMXClient;
import be.codewriter.dmx512.helper.DMXMessage;
import be.codewriter.dmx512.network.DMXIpDevice;
import be.codewriter.dmx512.network.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.*;

/**
 * DMX IP Controller.
 * Controls DMX lights over IP-to-DMX interface.
 */
public class DMXIPController implements DMXController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DMXIPController.class.getName());

    private static final int DEFAULT_ARTNET_PORT = 6454;
    private static final int DEFAULT_SACN_PORT = 5568;
    private static final long MIN_PACKET_INTERVAL = 25; // 40fps max
    private static final int RATE_LIMIT_WINDOW = 1000; // 1 second
    private static final int MAX_PACKETS_PER_SECOND = 44; // Art-Net recommended
    // Rate limiting
    private final Queue<Long> packetTimes = new LinkedList<>();
    private final boolean listening = true;
    private DatagramSocket socket;
    private InetAddress address;
    private int port;
    private boolean connected = false;
    private int universe = 0;
    private Protocol protocol = Protocol.ARTNET;
    // Network statistics
    private long packetsSent = 0;
    private long packetsDropped = 0;
    private long lastPacketTime = 0;
    private double bandwidth = 0;
    private String lastError = "";
    private Thread listenerThread;

    public void setUniverse(int universe) {
        if (universe < 0 || universe > 32767) {
            throw new IllegalArgumentException("Universe must be between 0 and 32767");
        }
        this.universe = universe;
    }

    public List<DMXIpDevice> discoverDevices() {
        List<DMXIpDevice> DMXIpDevices = new ArrayList<>();

        try {
            // Send Art-Net poll packet
            byte[] pollPacket = createArtNetPollPacket();
            DatagramPacket packet = new DatagramPacket(
                    pollPacket,
                    pollPacket.length,
                    InetAddress.getByName("255.255.255.255"),
                    DEFAULT_ARTNET_PORT
            );

            // Create temporary socket for discovery
            try (DatagramSocket discoverySocket = new DatagramSocket()) {
                discoverySocket.setBroadcast(true);
                discoverySocket.send(packet);

                // Listen for responses
                byte[] receiveData = new byte[1024];
                discoverySocket.setSoTimeout(1000); // 1 second timeout

                while (true) {
                    try {
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        discoverySocket.receive(receivePacket);

                        DMXIpDevice DMXIpDevice = parseArtNetPollReply(receivePacket);
                        if (DMXIpDevice != null) {
                            DMXIpDevices.add(DMXIpDevice);
                        }
                    } catch (SocketTimeoutException e) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            lastError = "Discovery failed: " + e.getMessage();
        }

        return DMXIpDevices;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
        this.port = (protocol == Protocol.ARTNET) ? DEFAULT_ARTNET_PORT : DEFAULT_SACN_PORT;
    }

    private byte[] createArtNetPollPacket() {
        byte[] packet = new byte[14];

        // Art-Net header
        packet[0] = 'A';
        packet[1] = 'r';
        packet[2] = 't';
        packet[3] = '-';
        packet[4] = 'N';
        packet[5] = 'e';
        packet[6] = 't';
        packet[7] = 0;

        // OpCode for ArtPoll (0x2000)
        packet[8] = 0x00;
        packet[9] = 0x20;

        // Protocol version
        packet[10] = 0x00;
        packet[11] = 0x0e;

        // Poll flags
        packet[12] = 0x02; // Enable diagnostic messages

        // Priority
        packet[13] = 0x00;

        return packet;
    }

    private byte[] createSACNPacket(DMXMessage dmxMessage) {
        // sACN packet header (simplified version)
        byte[] packet = new byte[126 + dmxMessage.getLength()];

        // Root layer
        packet[0] = 0x00; // Preamble Size
        packet[1] = 0x10; // Post-amble Size

        // ACN Packet Identifier
        packet[4] = 0x41;
        packet[5] = 0x53;
        packet[6] = 0x43;
        packet[7] = 0x2d;
        packet[8] = 0x45;
        packet[9] = 0x31;
        packet[10] = 0x2e;
        packet[11] = 0x17;

        // Flags and Length
        packet[16] = 0x70; // Vector

        // DMP layer
        packet[117] = (byte) 0x02; // Vector
        packet[118] = (byte) 0xa1; // Address Type & Data Type
        packet[119] = (byte) 0x00; // First Property Address
        packet[120] = (byte) 0x00; // Address Increment
        packet[121] = (byte) ((dmxMessage.getLength() + 1) >> 8);
        packet[122] = (byte) (dmxMessage.getLength() + 1);
        packet[123] = (byte) 0x00; // DMX Start Code

        // Copy DMX data
        System.arraycopy(dmxMessage.getData(), 0, packet, 124, dmxMessage.getLength());

        return packet;
    }

    private DMXIpDevice parseArtNetPollReply(DatagramPacket packet) {
        byte[] data = packet.getData();

        // Verify Art-Net header
        if (data.length < 14 ||
                data[0] != 'A' || data[1] != 'r' || data[2] != 't' || data[3] != '-' ||
                data[4] != 'N' || data[5] != 'e' || data[6] != 't' || data[7] != 0) {
            return null;
        }

        // Extract device information
        String ipAddress = packet.getAddress().getHostAddress();
        String name = new String(data, 26, 18).trim(); // Short name
        int universeCount = data[173] & 0xFF; // Number of ports

        return new DMXIpDevice(ipAddress, Protocol.ARTNET, name, universeCount);
    }

    private double calculateBandwidth(int packetSize) {
        long currentTime = System.currentTimeMillis();
        double timeDiff = (currentTime - lastPacketTime) / 1000.0; // Convert to seconds
        if (timeDiff > 0) {
            return (packetSize * 8) / timeDiff; // bits per second
        }
        return 0;
    }

    @Override
    public boolean connect(String ipAddress) {
        try {
            this.address = InetAddress.getByName(ipAddress);
            this.port = DEFAULT_ARTNET_PORT;
            this.socket = new DatagramSocket();
            this.connected = true;
            startListening();
            return true;
        } catch (IOException e) {
            this.connected = false;
            return false;
        }
    }

    @Override
    public synchronized void render(List<DMXClient> clients) {
        if (!connected || socket == null) {
            LOGGER.error("Not connected to DMX network, can't render data to the devices");
            return;
        }

        // Check rate limiting
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPacketTime < MIN_PACKET_INTERVAL) {
            packetsDropped++;
            return;
        }

        // Remove old packet times
        while (!packetTimes.isEmpty() &&
                packetTimes.peek() < currentTime - RATE_LIMIT_WINDOW) {
            packetTimes.poll();
        }

        // Check if we're within rate limit
        if (packetTimes.size() >= MAX_PACKETS_PER_SECOND) {
            packetsDropped++;
            return;
        }

        // Create and send packet based on protocol
        var dmxMessage = new DMXMessage(clients);
        byte[] packet;
        if (protocol == Protocol.ARTNET) {
            packet = createArtNetPacket(dmxMessage);
        } else {
            packet = createSACNPacket(dmxMessage);
        }

        try {
            DatagramPacket datagramPacket = new DatagramPacket(
                    packet,
                    packet.length,
                    address,
                    port
            );
            socket.send(datagramPacket);

            // Update statistics
            packetsSent++;
            lastPacketTime = currentTime;
            packetTimes.offer(currentTime);
            bandwidth = calculateBandwidth(packet.length);
        } catch (IOException e) {
            lastError = "Send failed: " + e.getMessage();
            LOGGER.error(lastError);
        }
    }

    @Override
    public void close() {
        if (socket != null) {
            socket.close();
        }
        connected = false;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    private void startListening() {
        listenerThread = new Thread(() -> {
            byte[] receiveBuffer = new byte[1024]; // Adjust buffer size as needed
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            while (listening) {
                try {
                    socket.receive(receivePacket); // This blocks until a packet is received

                    // Log the received data
                    LOGGER.debug("Received packet from {}:{}, length: {}",
                            receivePacket.getAddress(),
                            receivePacket.getPort(),
                            receivePacket.getLength());

                    // If you need to log the actual data:
                    byte[] data = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Received data: {}", Arrays.toString(data));
                    }
                } catch (SocketTimeoutException e) {
                    // Timeout is normal, continue listening
                    continue;
                } catch (IOException e) {
                    if (listening) { // Only log if we're still supposed to be listening
                        LOGGER.error("Error receiving packet: {}", e.getMessage());
                    }
                    break;
                }
            }
        });
        listenerThread.setName("DMX-UDP-Listener");
        listenerThread.start();
    }

    private byte[] createArtNetPacket(DMXMessage dmxMessage) {
        // Art-Net packet structure
        byte[] packet = new byte[18 + dmxMessage.getLength()];

        // Art-Net header "Art-Net"
        packet[0] = 'A';
        packet[1] = 'r';
        packet[2] = 't';
        packet[3] = '-';
        packet[4] = 'N';
        packet[5] = 'e';
        packet[6] = 't';
        packet[7] = 0;

        // OpCode for ArtDMX (0x5000)
        packet[8] = 0x00;
        packet[9] = 0x50;

        // Protocol version (14)
        packet[10] = 0x00;
        packet[11] = 0x0e;

        // Sequence number (0)
        packet[12] = 0x00;

        // Physical port (0)
        packet[13] = 0x00;

        // Universe (0)
        packet[14] = 0x00;
        packet[15] = 0x00;

        // Length of DMX data
        packet[16] = (byte) ((dmxMessage.getLength() >> 8) & 0xFF);
        packet[17] = (byte) (dmxMessage.getLength() & 0xFF);

        // Copy DMX data
        System.arraycopy(dmxMessage.getData(), 0, packet, 18, dmxMessage.getLength());

        return packet;
    }
}