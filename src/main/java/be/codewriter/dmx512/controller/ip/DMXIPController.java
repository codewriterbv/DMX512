package be.codewriter.dmx512.controller.ip;

import be.codewriter.dmx512.controller.DMXController;
import be.codewriter.dmx512.controller.change.DMXStatusChangeMessage;
import be.codewriter.dmx512.controller.ip.builder.ArtNetPacketBuilder;
import be.codewriter.dmx512.controller.ip.builder.SACNPacketBuilder;
import be.codewriter.dmx512.model.DMXUniverse;
import be.codewriter.dmx512.tool.HexTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import static be.codewriter.dmx512.controller.ip.builder.ArtNetPacketBuilder.ART_NET_PORT;
import static be.codewriter.dmx512.controller.ip.builder.SACNPacketBuilder.SACN_PORT;

/**
 * DMX IP Controller.
 * Controls DMX lights over IP-to-DMX interface.
 */
public class DMXIPController implements DMXController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DMXIPController.class.getName());

    private final InetAddress address;
    private final IPProtocol protocol;
    private final int port;
    private final long reconnectDelayMs = 5000; // 5 seconds
    private final int maxReconnectAttempts = 10;
    private boolean listening = true;
    private DatagramSocket socket;
    private boolean connected = false;
    private volatile boolean autoReconnect = true;
    private int reconnectAttempts = 0;

    /**
     * Constructor for an IP controller with only the IP address, using the ArtNet protocol
     *
     * @param address IP address
     */
    public DMXIPController(InetAddress address) {
        this(address, IPProtocol.ARTNET, ART_NET_PORT);
    }

    /**
     * Constructor for an IP controller with an IP address and protocol, using the default port for the protocol
     *
     * @param address  IP address
     * @param protocol {@link IPProtocol}
     */
    public DMXIPController(InetAddress address, IPProtocol protocol) {
        this(address, protocol, (protocol == IPProtocol.ARTNET) ? ART_NET_PORT : SACN_PORT);
    }

    /**
     * Constructor for an IP controller with an IP address, protocol, and port
     *
     * @param address  IP address
     * @param protocol {@link IPProtocol}
     * @param port     port
     */
    public DMXIPController(InetAddress address, IPProtocol protocol, int port) {
        this.address = address;
        this.protocol = protocol;
        this.port = port;

        connect();
    }

    @Override
    public DMXControllerType getType() {
        return DMXControllerType.IP;
    }

    @Override
    public String getProtocolName() {
        return protocol.name();
    }

    @Override
    public String getAddress() {
        return address.getHostName();
    }

    @Override
    public boolean connect() {
        LOGGER.debug("Connecting to DMX network at {}", address);
        try {
            this.socket = new DatagramSocket();
            this.connected = true;
            notifyListeners(DMXStatusChangeMessage.CONNECTED);
            startListening();
            return true;
        } catch (IOException e) {
            this.connected = false;
            notifyListeners(DMXStatusChangeMessage.DISCONNECTED);
            return false;
        }
    }

    @Override
    public synchronized void render(DMXUniverse universe) {
        if (!connected || socket == null) {
            LOGGER.error("Not connected to DMX network, can't render data to the devices");
            return;
        }
        render(universe.getId(), universe.getData());
    }

    @Override
    public synchronized void render(int universe, byte[] data) {
        if (!connected || socket == null) {
            LOGGER.error("Not connected to DMX network, can't render data to the devices");
            return;
        }
        sendData(createDataPacket(universe, data));
    }

    @Override
    public void close() {
        autoReconnect = false;
        listening = false;
        if (socket != null) {
            socket.close();
        }
        connected = false;
        notifyListeners(DMXStatusChangeMessage.DISCONNECTED);
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    private void sendData(byte[] data) {
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
                        address, datagramPacket.getLength(),
                        HexTool.toHexString(datagramPacket.getData()));
            }
        } catch (IOException e) {
            LOGGER.error("Send failed: {}", e.getMessage());
            handleDisconnection();
        }
    }

    private void handleDisconnection() {
        if (connected) {
            connected = false;
            notifyListeners(DMXStatusChangeMessage.DISCONNECTED);

            if (autoReconnect && reconnectAttempts < maxReconnectAttempts) {
                LOGGER.info("Attempting to reconnect... (attempt {}/{})", reconnectAttempts + 1, maxReconnectAttempts);
                scheduleReconnect();
            } else if (reconnectAttempts >= maxReconnectAttempts) {
                LOGGER.error("Max reconnection attempts reached. Giving up.");
            }
        }
    }

    private void scheduleReconnect() {
        Thread reconnectThread = new Thread(() -> {
            try {
                Thread.sleep(reconnectDelayMs);
                if (attemptReconnect()) {
                    reconnectAttempts = 0;
                    LOGGER.info("Successfully reconnected to DMX network");
                } else {
                    reconnectAttempts++;
                    if (reconnectAttempts < maxReconnectAttempts) {
                        scheduleReconnect();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        reconnectThread.setName("DMX-Reconnect-Thread");
        reconnectThread.setDaemon(true);
        reconnectThread.start();
    }

    private boolean attemptReconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            this.socket = new DatagramSocket();
            this.connected = true;
            this.listening = true;

            notifyListeners(DMXStatusChangeMessage.CONNECTED);
            startListening();

            return true;
        } catch (IOException e) {
            LOGGER.warn("Reconnection attempt failed: {}", e.getMessage());
            return false;
        }
    }

    private void startListening() {
        var listenerThread = new Thread(() -> {
            byte[] receiveBuffer = new byte[1024]; // Adjust buffer size as needed
            DatagramPacket receivedPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            while (listening && connected) {
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
                    LOGGER.warn("Socket timeout: {}", e.getMessage());
                } catch (IOException e) {
                    if (listening && connected) {
                        LOGGER.error("Error receiving packet: {}", e.getMessage());
                        handleDisconnection();
                    }
                    break;
                } catch (Exception e) {
                    LOGGER.error("Unexpected error: {}", e.getMessage());
                }
            }
        });
        listenerThread.setName("DMX-UDP-Listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Create DMX data packet for the given universe
     *
     * @param universe {@link DMXUniverse}
     * @return byte array containing header and footer for the selected protocol
     */
    public byte[] createDataPacket(DMXUniverse universe) {
        return createDataPacket(universe.getId(), universe.getData());
    }

    /**
     * Create DMX data packet for the given universe ID with the given DMX data
     *
     * @param universe {@link DMXUniverse}
     * @param data     DMX data byte array
     * @return byte array containing header and footer for the selected protocol
     */
    public byte[] createDataPacket(int universe, byte[] data) {
        if (this.protocol == IPProtocol.ARTNET) {
            var builder = new ArtNetPacketBuilder();
            return builder.createArtNetDMXPacket(data, universe);
        } else {
            var builder = new SACNPacketBuilder("");
            return builder.createSACNPacket(data, universe);
        }
    }
}