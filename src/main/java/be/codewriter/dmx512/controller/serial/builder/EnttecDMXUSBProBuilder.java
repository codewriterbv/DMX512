package be.codewriter.dmx512.controller.serial.builder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static be.codewriter.dmx512.controller.serial.DMXSerialController.DMX_START_CODE;
import static be.codewriter.dmx512.controller.serial.DMXSerialController.MAX_DMX_CHANNELS;

/**
 * Java implementation for Enttec DMX USB Pro API to create DMX packets
 */
public class EnttecDMXUSBProBuilder {

    private static final byte START_OF_MESSAGE = (byte) 0x7E;
    private static final byte END_OF_MESSAGE = (byte) 0xE7;
    private static final byte SEND_DMX_PACKET = (byte) 0x06;

    private EnttecDMXUSBProBuilder() {
        // Hide constructor
    }

    /**
     * Sends DMX data to the Enttec DMX USB Pro device
     *
     * @param dmxData Array of DMX channel values (1-512 channels)
     * @return byte array with the full enttec packet
     * @throws IOException              if communication fails
     * @throws IllegalArgumentException if dmxData is invalid
     */
    public static byte[] createEnttecDMXPacket(byte[] dmxData) throws IOException {
        if (dmxData == null) {
            throw new IllegalArgumentException("DMX data cannot be null");
        }

        if (dmxData.length > MAX_DMX_CHANNELS) {
            throw new IllegalArgumentException("DMX data cannot exceed " + MAX_DMX_CHANNELS + " channels");
        }

        // Create the complete DMX packet (start code + channel data)
        byte[] fullDmxPacket = new byte[dmxData.length + 1];
        fullDmxPacket[0] = (byte) DMX_START_CODE;  // DMX start code
        System.arraycopy(dmxData, 0, fullDmxPacket, 1, dmxData.length);

        // Build the Enttec protocol packet
        return buildEnttecPacket(SEND_DMX_PACKET, fullDmxPacket);
    }

    /**
     * Builds an Enttec protocol packet
     *
     * @param label Command label
     * @param data  Data payload
     * @return Complete packet with headers and footers
     */
    private static byte[] buildEnttecPacket(byte label, byte[] data) {
        // Escape any special characters in the data
        byte[] escapedData = escapeData(data);

        // Calculate data length (low byte, high byte)
        int dataLength = escapedData.length;
        byte dataLengthLow = (byte) (dataLength & 0xFF);
        byte dataLengthHigh = (byte) ((dataLength >> 8) & 0xFF);

        // Build complete packet
        byte[] packet = new byte[5 + escapedData.length];
        packet[0] = START_OF_MESSAGE;
        packet[1] = label;
        packet[2] = dataLengthLow;
        packet[3] = dataLengthHigh;
        System.arraycopy(escapedData, 0, packet, 4, escapedData.length);
        packet[packet.length - 1] = END_OF_MESSAGE;

        return packet;
    }

    /**
     * Escapes special characters in data according to Enttec protocol
     *
     * @param data Original data
     * @return Escaped data
     */
    private static byte[] escapeData(byte[] data) {
        ByteArrayOutputStream escaped = new ByteArrayOutputStream();

        for (byte b : data) {
            if (b == START_OF_MESSAGE || b == END_OF_MESSAGE || b == (byte) 0x7D) {
                escaped.write(0x7D);  // Escape character
                escaped.write(b ^ 0x20);  // XOR with 0x20
            } else {
                escaped.write(b);
            }
        }

        return escaped.toByteArray();
    }
}
