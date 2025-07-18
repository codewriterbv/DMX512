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
    private final Protocol protocol;
    private final int port;
    private boolean listening = true;
    private DatagramSocket socket;
    private boolean connected = false;
    private volatile boolean autoReconnect = true;
    private long reconnectDelayMs = 5000; // 5 seconds
    private int maxReconnectAttempts = 10;
    private int reconnectAttempts = 0;

    public DMXIPController(InetAddress address) {
        this(address, Protocol.ARTNET, ART_NET_PORT);
    }

    public DMXIPController(InetAddress address, Protocol protocol) {
        this(address, protocol, (protocol == Protocol.ARTNET) ? ART_NET_PORT : SACN_PORT);
    }

    public DMXIPController(InetAddress address, Protocol protocol, int port) {
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
                        socket.getRemoteSocketAddress(), datagramPacket.getLength(),
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

    public byte[] createDataPacket(DMXUniverse universe) {
        return createDataPacket(universe.getId(), universe.getData());
    }

    public byte[] createDataPacket(int universe, byte[] data) {
        if (this.protocol == Protocol.ARTNET) {
            var builder = new ArtNetPacketBuilder();
            return builder.createArtDMXPacket(data, universe);
        } else {
            var builder = new SACNPacketBuilder("");
            return builder.createSACNPacket(data, universe);
        }
    }

    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    public void setReconnectDelay(long delayMs) {
        this.reconnectDelayMs = delayMs;
    }

    public void setMaxReconnectAttempts(int maxAttempts) {
        this.maxReconnectAttempts = maxAttempts;
    }

}