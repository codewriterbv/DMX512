package be.codewriter.dmx512.controller.ip.builder;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Builder to create IP packages containing DMX512 data using the SACP (Streaming ACP) protocol.
 */
public class SACNPacketBuilder {

    public static final int SACN_PORT = 5568;
    // sACN Constants
    private static final byte[] ACN_PACKET_IDENTIFIER = {
            0x41, 0x53, 0x43, 0x2d, 0x45, 0x31, 0x2e, 0x31, 0x37, 0x00, 0x00, 0x00
    };
    private static final int ROOT_VECTOR = 0x00000004;
    private static final int FRAMING_VECTOR = 0x00000002;
    private static final byte DMP_VECTOR = 0x02;
    private static final byte ADDRESS_TYPE_DATA_TYPE = (byte) 0xa1;
    private byte sequenceNumber = 0;
    private byte[] cid;
    private String sourceName;

    /**
     * Constructor by name
     *
     * @param sourceName source name
     */
    public SACNPacketBuilder(String sourceName) {
        this.sourceName = sourceName;
        this.cid = generateCID();
    }

    /**
     * Creates a complete sACN packet with DMX data
     *
     * @param dmxData  Array of DMX channel values (1-512 bytes)
     * @param universe DMX universe number (1-63999)
     * @param priority Priority level (0-200, default 100)
     * @return Complete sACN packet ready for transmission
     */
    public byte[] createSACNPacket(byte[] dmxData, int universe, int priority) {
        if (dmxData == null || dmxData.length == 0 || dmxData.length > 512) {
            throw new IllegalArgumentException("DMX data must be 1-512 bytes");
        }
        if (universe < 1 || universe > 63999) {
            throw new IllegalArgumentException("Universe must be 1-63999");
        }
        if (priority < 0 || priority > 200) {
            throw new IllegalArgumentException("Priority must be 0-200");
        }

        // Calculate packet size more carefully
        // Standard sACN packet for 512 channels should be 638 bytes total
        int dmxPayloadSize = dmxData.length + 1; // +1 for start code

        // DMP layer: 10 bytes header + DMX payload
        int dmpLayerSize = 10 + dmxPayloadSize;

        // Framing layer: 77 bytes header + DMP layer
        int framingLayerSize = 77 + dmpLayerSize;

        // Root layer: 22 bytes header + Framing layer  
        int rootLayerSize = 22 + framingLayerSize;

        // Complete packet: 16 bytes preamble + Root layer
        int totalPacketSize = 16 + rootLayerSize;

        ByteBuffer buffer = ByteBuffer.allocate(totalPacketSize);

        // === PREAMBLE (16 bytes) ===
        buffer.put(new byte[16]); // 16 bytes of 0x00

        // === ROOT LAYER (22 bytes header) ===
        buffer.putShort((short) 0x0010); // Post-amble size (2 bytes)
        buffer.put(ACN_PACKET_IDENTIFIER); // 12 bytes
        buffer.putShort((short) (0x7000 | (rootLayerSize & 0x0FFF))); // Flags (0x7) + Length (2 bytes)
        buffer.putInt(ROOT_VECTOR); // Vector (4 bytes)
        buffer.put(cid); // 16-byte CID

        // === FRAMING LAYER (77 bytes header) ===
        buffer.putShort((short) (0x7000 | (framingLayerSize & 0x0FFF))); // Flags + Length (2 bytes)
        buffer.putInt(FRAMING_VECTOR); // Vector (4 bytes)

        // Source name (64 bytes, null-terminated)
        byte[] sourceNameBytes = new byte[64];
        byte[] nameBytes = sourceName.getBytes();
        System.arraycopy(nameBytes, 0, sourceNameBytes, 0,
                Math.min(nameBytes.length, 63)); // Leave room for null terminator
        buffer.put(sourceNameBytes);

        buffer.put((byte) priority); // Priority (1 byte)
        buffer.putShort((short) 0); // Synchronization Address (2 bytes, reserved)
        buffer.put(sequenceNumber++); // Sequence Number (1 byte)
        buffer.put((byte) 0); // Options (1 byte)
        buffer.putShort((short) universe); // Universe (2 bytes)

        // === DMP LAYER (10 bytes header) ===
        buffer.putShort((short) (0x7000 | (dmpLayerSize & 0x0FFF))); // Flags + Length (2 bytes)
        buffer.put(DMP_VECTOR); // Vector (1 byte)
        buffer.put(ADDRESS_TYPE_DATA_TYPE); // Address Type & Data Type (1 byte)
        buffer.putShort((short) 0); // First Property Address (2 bytes)
        buffer.putShort((short) 1); // Address Increment (2 bytes)
        buffer.putShort((short) dmxPayloadSize); // Property Value Count (2 bytes)

        // === DMX DATA ===
        buffer.put((byte) 0); // Start Code (1 byte)
        buffer.put(dmxData); // DMX channel data

        return buffer.array();
    }

    /**
     * Convenience method with default priority (100)
     *
     * @param dmxData  data
     * @param universe universe id
     */
    public byte[] createSACNPacket(byte[] dmxData, int universe) {
        return createSACNPacket(dmxData, universe, 100);
    }

    /**
     * Generate a random CID (Component Identifier)
     */
    private byte[] generateCID() {
        return uuidToBytes(UUID.randomUUID());
    }

    /**
     * Convert UUID to 16-byte array
     *
     * @param uuid uuid
     */
    private byte[] uuidToBytes(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    /**
     * Reset sequence number (useful for new connections)
     */
    public void resetSequence() {
        sequenceNumber = 0;
    }

    /**
     * Set a custom CID
     *
     * @param uuid uuid
     */
    public void setCID(UUID uuid) {
        this.cid = uuidToBytes(uuid);
    }

    /**
     * Set source name
     *
     * @param sourceName source name
     */
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
}

// Usage example:
/*
public class SACNExample {
    public static void main(String[] args) {
        SACNPacketBuilder builder = new SACNPacketBuilder("My Lighting Controller");
        
        // Create DMX data (example: all channels at 50%)
        byte[] dmxData = new byte[512];
        for (int i = 0; i < dmxData.length; i++) {
            dmxData[i] = (byte) 127; // 50% brightness
        }
        
        // Create packet for universe 1
        byte[] packet = builder.createSACNPacket(dmxData, 1);
        
        // Send packet via UDP multicast to 239.255.0.1:5568
        // (network code not shown)
    }
}
*/