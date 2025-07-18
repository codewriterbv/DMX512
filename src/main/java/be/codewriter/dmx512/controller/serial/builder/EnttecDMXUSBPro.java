package be.codewriter.dmx512.controller.serial.builder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Java implementation for Enttec DMX USB Pro API
 * Sends DMX data to Enttec DMX USB Pro devices over serial connection
 */
public class EnttecDMXUSBPro {

    // Protocol constants
    private static final byte START_OF_MESSAGE = (byte) 0x7E;
    private static final byte END_OF_MESSAGE = (byte) 0xE7;
    private static final byte SEND_DMX_PACKET = (byte) 0x06;
    private static final byte GET_WIDGET_PARAMS = (byte) 0x03;
    private static final byte SET_WIDGET_PARAMS = (byte) 0x04;

    // DMX constants
    private static final int DMX_START_CODE = 0x00;
    private static final int MAX_DMX_CHANNELS = 512;

    private final InputStream inputStream;
    private final OutputStream outputStream;
    private boolean isConnected = false;

    /**
     * Constructor that accepts input and output streams
     *
     * @param inputStream  Serial port input stream
     * @param outputStream Serial port output stream
     */
    public EnttecDMXUSBPro(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.isConnected = true;
    }

    /**
     * Example usage demonstrating how to use the class
     */
    public static void main(String[] args) {
        // Example usage - you would replace these with actual serial port streams
        try {
            // This is just a demonstration - in real usage you would use
            // a serial port library like JSSC, PureJavaComm, or similar

            // Create some example DMX data
            byte[] dmxData = new byte[512];

            // Set channel 1 to full brightness (255)
            dmxData[0] = (byte) 255;

            // Set channel 2 to half brightness (127)
            dmxData[1] = (byte) 127;

            // Set channels 3-5 to various values
            dmxData[2] = (byte) 64;
            dmxData[3] = (byte) 128;
            dmxData[4] = (byte) 192;

            // Example of how you would use this with actual serial streams:
            /*
            SerialPort serialPort = new SerialPort("COM3"); // or /dev/ttyUSB0 on Linux
            serialPort.openPort();
            serialPort.setParams(250000, 8, 1, 0); // 250000 baud, 8N1

            EnttecDMXUSBPro dmxDevice = new EnttecDMXUSBPro(
                serialPort.getInputStream(),
                serialPort.getOutputStream()
            );

            // Send DMX data
            dmxDevice.sendDMXData(dmxData);

            // Clean up
            dmxDevice.close();
            serialPort.closePort();
            */

            System.out.println("Example DMX data prepared. Connect to actual serial port to send.");
            System.out.println("DMX data length: " + dmxData.length + " channels");
            System.out.println("Sample values: " + Arrays.toString(Arrays.copyOf(dmxData, 5)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends DMX data to the Enttec DMX USB Pro device
     *
     * @param dmxData Array of DMX channel values (1-512 channels)
     * @throws IOException              if communication fails
     * @throws IllegalArgumentException if dmxData is invalid
     */
    public void sendDMXData(byte[] dmxData) throws IOException {
        if (!isConnected) {
            throw new IllegalStateException("Device not connected");
        }

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
        byte[] packet = buildEnttecPacket(SEND_DMX_PACKET, fullDmxPacket);

        // Send the packet
        outputStream.write(packet);
        outputStream.flush();
    }

    /**
     * Sends DMX data with unsigned integer values (0-255)
     *
     * @param dmxData Array of DMX channel values as integers (0-255)
     * @throws IOException              if communication fails
     * @throws IllegalArgumentException if dmxData is invalid
     */
    public void sendDMXData(int[] dmxData) throws IOException {
        if (dmxData == null) {
            throw new IllegalArgumentException("DMX data cannot be null");
        }

        // Convert int array to byte array
        byte[] byteData = new byte[dmxData.length];
        for (int i = 0; i < dmxData.length; i++) {
            if (dmxData[i] < 0 || dmxData[i] > 255) {
                throw new IllegalArgumentException("DMX values must be between 0-255. Invalid value: " + dmxData[i]);
            }
            byteData[i] = (byte) dmxData[i];
        }

        sendDMXData(byteData);
    }

    /**
     * Gets widget parameters from the device
     *
     * @return Widget parameters response or null if failed
     * @throws IOException if communication fails
     */
    public byte[] getWidgetParameters() throws IOException {
        if (!isConnected) {
            throw new IllegalStateException("Device not connected");
        }

        // Send get parameters command
        byte[] packet = buildEnttecPacket(GET_WIDGET_PARAMS, new byte[0]);
        outputStream.write(packet);
        outputStream.flush();

        // Read response
        return readResponse();
    }

    /**
     * Builds an Enttec protocol packet
     *
     * @param label Command label
     * @param data  Data payload
     * @return Complete packet with headers and footers
     */
    private byte[] buildEnttecPacket(byte label, byte[] data) {
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
    private byte[] escapeData(byte[] data) {
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

    /**
     * Reads response from the device
     *
     * @return Response data or null if no response
     * @throws IOException if communication fails
     */
    private byte[] readResponse() throws IOException {
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        // Simple timeout mechanism
        long startTime = System.currentTimeMillis();
        long timeout = 1000; // 1 second timeout

        while (System.currentTimeMillis() - startTime < timeout) {
            if (inputStream.available() > 0) {
                int bytesRead = inputStream.read(buffer);
                if (bytesRead > 0) {
                    response.write(buffer, 0, bytesRead);
                    // Check if we have a complete packet
                    byte[] data = response.toByteArray();
                    if (data.length > 0 && data[data.length - 1] == END_OF_MESSAGE) {
                        return parseResponse(data);
                    }
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return null; // Timeout
    }

    /**
     * Parses response packet and extracts data
     *
     * @param packet Raw response packet
     * @return Extracted data
     */
    private byte[] parseResponse(byte[] packet) {
        if (packet.length < 5) return null;

        int dataLength = (packet[2] & 0xFF) | ((packet[3] & 0xFF) << 8);
        if (packet.length < 5 + dataLength) return null;

        byte[] data = new byte[dataLength];
        System.arraycopy(packet, 4, data, 0, dataLength);

        return unescapeData(data);
    }

    /**
     * Unescapes data according to Enttec protocol
     *
     * @param data Escaped data
     * @return Unescaped data
     */
    private byte[] unescapeData(byte[] data) {
        ByteArrayOutputStream unescaped = new ByteArrayOutputStream();

        for (int i = 0; i < data.length; i++) {
            if (data[i] == (byte) 0x7D && i + 1 < data.length) {
                unescaped.write(data[i + 1] ^ 0x20);
                i++; // Skip next byte as it was part of escape sequence
            } else {
                unescaped.write(data[i]);
            }
        }

        return unescaped.toByteArray();
    }

    /**
     * Closes the connection
     */
    public void close() {
        isConnected = false;
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
        } catch (IOException e) {
            // Ignore close errors
        }
    }
}
