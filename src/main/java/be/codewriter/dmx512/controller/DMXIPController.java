package be.codewriter.dmx512.controller;

import be.codewriter.dmx512.client.DMXClient;
import be.codewriter.dmx512.helper.DMXMessage;
import be.codewriter.dmx512.network.DMXIpDevice;
import be.codewriter.dmx512.network.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * DMX IP Controller.
 * Controls DMX lights over IP-to-DMX interface.
 */
public class DMXIPController extends DMXChangeNotifier implements DMXController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DMXIPController.class.getName());

    private static final int DEFAULT_ARTNET_PORT = 6454;
    private static final int DEFAULT_SACN_PORT = 5568;
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
        Set<InetAddress> localAddresses = new HashSet<>();

        try {
            // Get all local addresses
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                localAddresses.addAll(Collections.list(ni.getInetAddresses()));
            }

            // Send Art-Net poll packet
            byte[] pollPacket = createArtNetPollPacket();
            DatagramPacket packet = new DatagramPacket(
                    pollPacket,
                    pollPacket.length,
                    InetAddress.getByName("255.255.255.255"),
                    DEFAULT_ARTNET_PORT
            );

            // Create a temporary socket for discovery
            try (DatagramSocket discoverySocket = new DatagramSocket(DEFAULT_ARTNET_PORT)) {
                discoverySocket.setBroadcast(true);
                discoverySocket.send(packet);

                // Listen for responses
                byte[] receiveData = new byte[1024];
                discoverySocket.setSoTimeout(1000); // 1 second timeout

                while (true) {
                    try {
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        discoverySocket.receive(receivePacket);

                        // Skip if the packet is from our own machine
                        if (localAddresses.contains(receivePacket.getAddress())) {
                            continue;
                        }

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

    @Override
    public boolean connect(String ipAddress) {
        LOGGER.debug("Connecting to DMX network at {}", ipAddress);
        try {
            this.address = InetAddress.getByName(ipAddress);
            this.port = DEFAULT_ARTNET_PORT;
            this.socket = new DatagramSocket();
            this.connected = true;
            notifyListeners(DMXChangeMessage.CONNECTED);
            startListening();
            return true;
        } catch (IOException e) {
            this.connected = false;
            notifyListeners(DMXChangeMessage.DISCONNECTED);
            return false;
        }
    }

    @Override
    public synchronized void render(List<DMXClient> clients) {
        if (!connected || socket == null) {
            LOGGER.error("Not connected to DMX network, can't render data to the devices");
            return;
        }

        // Create and send packet based on protocol
        var dmxMessage = new DMXMessage(clients);
        byte[] packet;
        if (protocol == Protocol.ARTNET) {
            packet = createArtNetDataPacket(dmxMessage);
        } else {
            packet = createSACNPacket(dmxMessage);
        }

        sendData(packet);
    }

    @Override
    public void sendData(byte[] data) {
        try {
            DatagramPacket datagramPacket = new DatagramPacket(
                    data,
                    data.length,
                    address,
                    port
            );
            socket.send(datagramPacket);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Sent packet to {}, length {}: {}",
                        socket.getRemoteSocketAddress(), datagramPacket.getLength(),
                        HexFormat.of().withDelimiter(" ").withUpperCase().formatHex(datagramPacket.getData()));
            }
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
        notifyListeners(DMXChangeMessage.DISCONNECTED);
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    private void startListening() {
        listenerThread = new Thread(() -> {
            byte[] receiveBuffer = new byte[1024]; // Adjust buffer size as needed
            DatagramPacket receivedPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            while (listening) {
                try {
                    socket.receive(receivedPacket); // This blocks until a packet is received

                    // Log the received data
                    LOGGER.debug("Received packet from {}:{}, length: {}",
                            receivedPacket.getAddress(),
                            receivedPacket.getPort(),
                            receivedPacket.getLength());

                    // If you need to log the actual data:
                    byte[] data = Arrays.copyOf(receivedPacket.getData(), receivedPacket.getLength());
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

    private byte[] createArtNetPollPacket() {
        return createArtNetPacket((short) 0x2000, new byte[]{});
    }

    private byte[] createArtNetDataPacket(DMXMessage dmxMessage) {
        return createArtNetPacket((short) 0x5000, dmxMessage.getData());
    }

    private byte[] createArtNetPacket(Short opCode, byte[] dmxData) {
        // Art-Net packet structure
        int packageLength = dmxData.length;
        byte[] packet = new byte[18 + packageLength];

        // Art-Net header "Art-Net"
        packet[0] = 'A';
        packet[1] = 'r';
        packet[2] = 't';
        packet[3] = '-';
        packet[4] = 'N';
        packet[5] = 'e';
        packet[6] = 't';
        packet[7] = 0;

        // OpCode for ArtDMX
        packet[8] = (byte) (opCode & 0xFF);        // Low byte (0x00)
        packet[9] = (byte) ((opCode >> 8) & 0xFF);

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
        packet[16] = (byte) ((packageLength >> 8) & 0xFF);
        packet[17] = (byte) (packageLength & 0xFF);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Header: {}", HexFormat.of().withDelimiter(" ").withUpperCase().formatHex(packet));
        }

        // Copy DMX data
        if (dmxData.length > 0) {
            System.arraycopy(dmxData, 0, packet, 18, packageLength);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("DMX data: {}", HexFormat.of().withDelimiter(" ").withUpperCase().formatHex(dmxData));
                LOGGER.debug("Full packet: {}", HexFormat.of().withDelimiter(" ").withUpperCase().formatHex(packet));
            }
        }

        return packet;
    }
}