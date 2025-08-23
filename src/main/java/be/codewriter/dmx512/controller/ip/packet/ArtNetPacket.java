package be.codewriter.dmx512.controller.ip.packet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Logger;

/**
 * Builder to create IP packages containing DMX512 data using the ArtNet protocol.
 */
public class ArtNetPacket {
    /**
     * Art-Net OpCode for a poll message
     */
    public static final short OP_POLL = (short) 0x2000;
    /**
     * Art-Net OpCode for a DMX message
     */
    public static final short OP_DMX = (short) 0x5000;
    /**
     * Default port for the ArtNet protocol
     */
    public static final int ART_NET_PORT = 6454;
    private static final Logger LOGGER = Logger.getLogger(ArtNetPacket.class.getName());
    // Art-Net Constants
    private static final byte[] ART_NET_HEADER = {'A', 'r', 't', '-', 'N', 'e', 't', 0};
    private static final int PROTOCOL_VERSION = 14;
    private static final boolean ENABLE_SEQUENCING = true;
    private static final int PHYSICAL_PORT = 0;

    private ArtNetPacket() {
        // Hide constructor
    }

    /**
     * Creates an Art-Net DMX packet (ArtDMX)
     *
     * @param dmxData  Array of DMX channel values (1-512 bytes)
     * @param universe Universe number (0-32767)
     * @param subnet   Subnet number (0-15)
     * @param net      Net number (0-127)
     * @return Complete Art-Net packet ready for transmission
     */
    public static byte[] createArtNetDMXPacket(byte[] dmxData, int universe, int subnet, int net) {
        if (dmxData == null || dmxData.length == 0 || dmxData.length > 512) {
            throw new IllegalArgumentException("DMX data must be 1-512 bytes");
        }
        if (universe < 0 || universe > 15) {
            throw new IllegalArgumentException("Universe must be 0-15");
        }
        if (subnet < 0 || subnet > 15) {
            throw new IllegalArgumentException("Subnet must be 0-15");
        }
        if (net < 0 || net > 127) {
            throw new IllegalArgumentException("Net must be 0-127");
        }

        // Art-Net DMX packet is always even-length for DMX data
        int dmxLength = dmxData.length;
        if (dmxLength % 2 != 0) {
            dmxLength++; // Pad to even length
        }

        ByteBuffer buffer = ByteBuffer.allocate(18 + dmxLength);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Art-Net header
        buffer.put(ART_NET_HEADER);

        // OpCode (little-endian)
        buffer.putShort(OP_DMX);

        // Protocol version (big-endian)
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putShort((short) PROTOCOL_VERSION);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Sequence number
        var sequenceNumber = 0; // TODO, should this increment?
        buffer.put(ENABLE_SEQUENCING ? (byte) sequenceNumber : (byte) 0);

        // Physical port
        buffer.put((byte) PHYSICAL_PORT);

        // Universe address (SubNet + Universe in low byte, Net in high byte)
        int universeAddress = (net << 8) | (subnet << 4) | universe;
        buffer.putShort((short) universeAddress);

        // Length of DMX data (big-endian, must be even)
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putShort((short) dmxLength);

        // DMX data
        buffer.put(dmxData);

        // Pad with zero if odd length
        if (dmxData.length % 2 != 0) {
            buffer.put((byte) 0);
        }

        byte[] packet = buffer.array();

        if (LOGGER.isLoggable(java.util.logging.Level.FINE)) {
            LOGGER.fine("Created ArtDMX packet: Universe=" + universe +
                    ", Subnet=" + subnet + ", Net=" + net +
                    ", Length=" + dmxData.length);
        }

        return packet;
    }

    /**
     * Convenience method for single universe (subnet=0, net=0)
     *
     * @param dmxData  DMX data byte array
     * @param universe universe id
     * @return byte array containing the full ArtNet package
     */
    public static byte[] createArtNetDMXPacket(byte[] dmxData, int universe) {
        return createArtNetDMXPacket(dmxData, universe, 0, 0);
    }

    /**
     * Creates an Art-Net Poll packet
     *
     * @param talkToMe Flags indicating what information is required
     * @param priority Priority of diagnostics messages (0-3)
     * @return Art-Net Poll packet
     */
    public static byte[] createArtPollPacket(byte talkToMe, byte priority) {
        ByteBuffer buffer = ByteBuffer.allocate(14);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Art-Net header
        buffer.put(ART_NET_HEADER);

        // OpCode
        buffer.putShort(OP_POLL);

        // Protocol version (big-endian)
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putShort((short) PROTOCOL_VERSION);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // TalkToMe
        buffer.put(talkToMe);

        // Priority
        buffer.put(priority);

        return buffer.array();
    }

    /**
     * Creates an Art-Net Poll packet with default settings
     *
     * @return byte array
     */
    public static byte[] createArtPollPacket() {
        return createArtPollPacket((byte) 0x00, (byte) 0x00);
    }

    public static byte[] extractDmxData(byte[] packet) {
        if (packet == null || packet.length < 18) {
            return null;
        }

        // Get DMX data length from bytes 16-17 (big-endian)
        int dmxLength = getDmxDataLength(packet);

        // Validate packet size
        if (packet.length < 18 + dmxLength) {
            return null;
        }

        // Extract only the original DMX data (without padding)
        // Need to determine actual data length vs padded length
        int actualDataLength = dmxLength;
        if (dmxLength > 0 && packet[17 + dmxLength] == 0) {
            // Check if last byte is padding (zero)
            actualDataLength = dmxLength - 1;
        }

        byte[] dmxData = new byte[actualDataLength];
        System.arraycopy(packet, 18, dmxData, 0, actualDataLength);

        return dmxData;

    }

    public static short getDmxDataLength(byte[] packet) {
        if (packet == null || packet.length < 18) {
            return 0;
        }

        // DMX data length is stored at bytes 16-17 in big-endian format
        return (short) (((packet[16] & 0xFF) << 8) | (packet[17] & 0xFF));
    }
}
