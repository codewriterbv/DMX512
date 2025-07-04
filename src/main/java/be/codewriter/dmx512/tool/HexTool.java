package be.codewriter.dmx512.tool;

import java.util.HexFormat;

public class HexTool {

    private HexTool() {
        // Hide constructore
    }

    public static String toHexString(byte[] bytes) {
        return HexFormat.of().withDelimiter(" ").withUpperCase().formatHex(bytes);
    }
}
