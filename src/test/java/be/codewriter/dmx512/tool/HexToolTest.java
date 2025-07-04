package be.codewriter.dmx512.tool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HexToolTest {

    @Test
    void shouldConvertByteArrayToHexString() {
        assertAll(
                () -> assertEquals("00", HexTool.toHexString(new byte[]{(byte) 0x00}), "00"),
                () -> assertEquals("FF", HexTool.toHexString(new byte[]{(byte) 0xFF}), "FF"),
                () -> assertEquals("FF", HexTool.toHexString(new byte[]{(byte) 255}), "FF from 255"),
                () -> assertEquals("00 FF 05", HexTool.toHexString(new byte[]{(byte) 0, (byte) 255, (byte) 5}), "Three bytes")
        );
    }
}
