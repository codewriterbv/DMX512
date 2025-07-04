package be.codewriter.dmx512.controller.ip;

import be.codewriter.dmx512.client.DMXClient;
import be.codewriter.dmx512.controller.DMXChangeMessage;
import be.codewriter.dmx512.controller.DMXChangeNotifier;
import be.codewriter.dmx512.controller.DMXController;
import be.codewriter.dmx512.helper.DMXMessage;
import be.codewriter.dmx512.network.DMXIpDevice;
import be.codewriter.dmx512.network.Protocol;
import be.codewriter.dmx512.tool.HexTool;
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
    private int universe = 1;
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
            byte[] pollPacket = createDetectPaket();
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
        sendData(createDataPacket(dmxMessage));
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
                        HexTool.toHexString(datagramPacket.getData()));
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

    public byte[] createDetectPaket() {
        if (this.protocol == Protocol.ARTNET) {
            var builder = new ArtNetPacketBuilder();
            return builder.createArtPollPacket();
        } else {
            var builder = new SACNPacketBuilder("");
            return builder.createSACNPacket(new byte[]{}, universe);
        }
    }

    public byte[] createDataPacket(DMXMessage dmxMessage) {
        if (this.protocol == Protocol.ARTNET) {
            var builder = new ArtNetPacketBuilder();
            return builder.createArtDMXPacket(dmxMessage.getData(), universe);
        } else {
            var builder = new SACNPacketBuilder("");
            return builder.createSACNPacket(dmxMessage.getData(), universe);
        }
    }
}