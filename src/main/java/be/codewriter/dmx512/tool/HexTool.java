package be.codewriter.dmx512.tool;

import java.util.HexFormat;

/**
 * Tool to help with hex logging
 */
public class HexTool {

    private HexTool() {
        // Hide constructore
    }

    /**
     * Create readable hex string
     *
     * @param bytes data
     * @return readable string
     */
    public static String toHexString(byte[] bytes) {
        return HexFormat.of().withDelimiter(" ").withUpperCase().formatHex(bytes);
    }
}
