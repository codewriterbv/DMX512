package be.codewriter.dmx512.controller.ip;

import be.codewriter.dmx512.controller.ip.builder.ArtNetPacketBuilder;
import be.codewriter.dmx512.controller.ip.builder.SACNPacketBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.*;

import static be.codewriter.dmx512.controller.ip.builder.ArtNetPacketBuilder.ART_NET_PORT;
import static be.codewriter.dmx512.controller.ip.builder.SACNPacketBuilder.SACN_PORT;

/**
 * Tool to detect IP-to-DMX controllers
 */
public class DMXIPDiscoverTool {
    private static final Logger LOGGER = LoggerFactory.getLogger(DMXIPDiscoverTool.class.getName());
    private static final long DISCOVER_TIMEOUT_MILLIS = 2_000;

    private DMXIPDiscoverTool() {
        // Hide constructor
    }

    public static List<DMXIPDevice> discoverDevices() {
        return discoverDevices(Protocol.ARTNET, 1);
    }

    public static List<DMXIPDevice> discoverDevices(Protocol protocol, int universe) {
        List<DMXIPDevice> DMXIPDevices = new ArrayList<>();
        Set<InetAddress> localAddresses = new HashSet<>();

        try {
            // Get all local addresses
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                localAddresses.addAll(Collections.list(ni.getInetAddresses()));
            }

            // Send Art-Net poll packet
            byte[] pollPacket = createDetectPacket(protocol, universe);
            int port = protocol == Protocol.ARTNET ? ART_NET_PORT : SACN_PORT;
            DatagramPacket packet = new DatagramPacket(
                    pollPacket,
                    pollPacket.length,
                    InetAddress.getByName("255.255.255.255"),
                    port
            );

            // Create a temporary socket for discovery
            try (DatagramSocket discoverySocket = new DatagramSocket(port)) {
                discoverySocket.setBroadcast(true);
                discoverySocket.send(packet);

                // Listen for responses with time-based timeout
                byte[] receiveData = new byte[1024];
                discoverySocket.setSoTimeout(100); // Short timeout for individual receive calls

                long startTime = System.currentTimeMillis();
                long endTime = startTime + DISCOVER_TIMEOUT_MILLIS;

                while (System.currentTimeMillis() < endTime) {
                    try {
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        discoverySocket.receive(receivePacket);

                        // Skip if the packet is from our own machine
                        if (localAddresses.contains(receivePacket.getAddress())) {
                            continue;
                        }

                        DMXIPDevice dmxIpDevice = parseArtNetPollReply(receivePacket);
                        if (dmxIpDevice != null) {
                            DMXIPDevices.add(dmxIpDevice);
                        }
                    } catch (SocketTimeoutException e) {
                        // Continue until overall timeout is reached
                    }
                }

            }
        } catch (IOException e) {
            LOGGER.error("Discovery failed: {}", e.getMessage());
        }

        return DMXIPDevices;
    }

    private static byte[] createDetectPacket(Protocol protocol, int universe) {
        if (protocol == Protocol.ARTNET) {
            var builder = new ArtNetPacketBuilder();
            return builder.createArtPollPacket();
        } else {
            var builder = new SACNPacketBuilder("");
            return builder.createSACNPacket(new byte[]{}, universe);
        }
    }

    private static DMXIPDevice parseArtNetPollReply(DatagramPacket packet) {
        byte[] data = packet.getData();

        // Verify Art-Net header
        if (data.length < 14 ||
                data[0] != 'A' || data[1] != 'r' || data[2] != 't' || data[3] != '-' ||
                data[4] != 'N' || data[5] != 'e' || data[6] != 't' || data[7] != 0) {
            return null;
        }

        // Extract device information
        String name = new String(data, 26, 18).trim(); // Short name
        int universeCount = data[173] & 0xFF; // Number of ports

        return new DMXIPDevice(packet.getAddress(), name, Protocol.ARTNET, universeCount);
    }
}
