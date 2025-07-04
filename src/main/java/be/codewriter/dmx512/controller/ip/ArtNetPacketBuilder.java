package be.codewriter.dmx512.controller.ip;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Logger;

public class ArtNetPacketBuilder {
    // Art-Net OpCodes
    public static final short OP_POLL = (short) 0x2000;
    public static final short OP_POLL_REPLY = (short) 0x2100;
    public static final short OP_DMX = (short) 0x5000;
    public static final short OP_NZS = (short) 0x5100;
    public static final short OP_SYNC = (short) 0x5200;
    public static final short OP_ADDRESS = 0x6000;
    public static final short OP_INPUT = 0x7000;
    public static final short OP_TOD_REQUEST = (short) 0x8000;
    public static final short OP_TOD_DATA = (short) 0x8100;
    public static final short OP_TOD_CONTROL = (short) 0x8200;
    public static final short OP_RDM = (short) 0x8300;
    public static final short OP_RDM_SUB = (short) 0x8400;
    private static final Logger LOGGER = Logger.getLogger(ArtNetPacketBuilder.class.getName());
    // Art-Net Constants
    private static final byte[] ART_NET_HEADER = {'A', 'r', 't', '-', 'N', 'e', 't', 0};
    private static final int PROTOCOL_VERSION = 14;
    private static final int ART_NET_PORT = 6454;
    private byte sequenceNumber = 0;
    private boolean enableSequencing = true;
    private int physicalPort = 0;

    public ArtNetPacketBuilder() {
        // Default constructor
    }

    public ArtNetPacketBuilder(int physicalPort) {
        setPhysicalPort(physicalPort);
    }

    /**
     * Calculate Art-Net universe address from Net, Subnet, and Universe
     *
     * @param net      Net number (0-127)
     * @param subnet   Subnet number (0-15)
     * @param universe Universe number (0-15)
     * @return Combined universe address
     */
    public static int calculateUniverseAddress(int net, int subnet, int universe) {
        return (net << 8) | (subnet << 4) | universe;
    }

    /**
     * Extract Net from universe address
     */
    public static int getNetFromAddress(int address) {
        return (address >> 8) & 0x7F;
    }

    /**
     * Extract Subnet from universe address
     */
    public static int getSubnetFromAddress(int address) {
        return (address >> 4) & 0x0F;
    }

    /**
     * Extract Universe from universe address
     */
    public static int getUniverseFromAddress(int address) {
        return address & 0x0F;
    }

    public static int getArtNetPort() {
        return ART_NET_PORT;
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
    public byte[] createArtDMXPacket(byte[] dmxData, int universe, int subnet, int net) {
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
        buffer.put(enableSequencing ? sequenceNumber++ : (byte) 0);

        // Physical port
        buffer.put((byte) physicalPort);

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
     */
    public byte[] createArtDMXPacket(byte[] dmxData, int universe) {
        return createArtDMXPacket(dmxData, universe, 0, 0);
    }

    /**
     * Creates an Art-Net Poll packet
     *
     * @param talkToMe Flags indicating what information is required
     * @param priority Priority of diagnostics messages (0-3)
     * @return Art-Net Poll packet
     */
    public byte[] createArtPollPacket(byte talkToMe, byte priority) {
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
     */
    public byte[] createArtPollPacket() {
        return createArtPollPacket((byte) 0x00, (byte) 0x00);
    }

    /**
     * Creates an Art-Net Sync packet
     *
     * @param aux Auxiliary data (usually 0)
     * @return Art-Net Sync packet
     */
    public byte[] createArtSyncPacket(short aux) {
        ByteBuffer buffer = ByteBuffer.allocate(14);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Art-Net header
        buffer.put(ART_NET_HEADER);

        // OpCode
        buffer.putShort(OP_SYNC);

        // Protocol version (big-endian)
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putShort((short) PROTOCOL_VERSION);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Aux
        buffer.putShort(aux);

        return buffer.array();
    }

    /**
     * Creates an Art-Net Sync packet with default aux value
     */
    public byte[] createArtSyncPacket() {
        return createArtSyncPacket((short) 0);
    }

    /**
     * Generic packet creation method (similar to your original)
     *
     * @param opCode  Art-Net operation code
     * @param dmxData DMX data payload
     * @return Art-Net packet
     */
    public byte[] createArtNetPacket(short opCode, byte[] dmxData) {
        if (opCode == OP_DMX) {
            // Use the proper ArtDMX method for DMX packets
            return createArtDMXPacket(dmxData, 0, 0, 0);
        }

        // Generic packet creation for other opcodes
        ByteBuffer buffer = ByteBuffer.allocate(18 + (dmxData != null ? dmxData.length : 0));
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Art-Net header
        buffer.put(ART_NET_HEADER);

        // OpCode
        buffer.putShort(opCode);

        // Protocol version (big-endian)
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putShort((short) PROTOCOL_VERSION);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Sequence number
        buffer.put(enableSequencing ? sequenceNumber++ : (byte) 0);

        // Physical port
        buffer.put((byte) physicalPort);

        // Universe (default to 0)
        buffer.putShort((short) 0);

        // Length of data
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putShort((short) (dmxData != null ? dmxData.length : 0));

        // Data
        if (dmxData != null && dmxData.length > 0) {
            buffer.put(dmxData);
        }

        return buffer.array();
    }

    public void resetSequence() {
        this.sequenceNumber = 0;
    }

    public int getPhysicalPort() {
        return physicalPort;
    }

    // Configuration methods
    public void setPhysicalPort(int port) {
        if (port < 0 || port > 3) {
            throw new IllegalArgumentException("Physical port must be 0-3");
        }
        this.physicalPort = port;
    }

    public boolean isSequencingEnabled() {
        return enableSequencing;
    }

    public void setSequencingEnabled(boolean enabled) {
        this.enableSequencing = enabled;
    }
}

// Usage example:
/*
public class ArtNetExample {
    public static void main(String[] args) {
        ArtNetPacketBuilder builder = new ArtNetPacketBuilder();

        // Create DMX data (example: first 4 channels)
        byte[] dmxData = {(byte) 255, (byte) 128, (byte) 64, (byte) 32};

        // Create ArtDMX packet for universe 0
        byte[] dmxPacket = builder.createArtDMXPacket(dmxData, 0);

        // Create ArtPoll packet
        byte[] pollPacket = builder.createArtPollPacket();

        // Create ArtSync packet
        byte[] syncPacket = builder.createArtSyncPacket();

        // Send packets via UDP broadcast to 255.255.255.255:6454
        // (network code not shown)
    }
}
*/
