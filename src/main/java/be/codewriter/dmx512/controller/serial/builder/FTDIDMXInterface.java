package be.codewriter.dmx512.controller.serial.builder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Java implementation for FTDI-based DMX interfaces
 * Supports common FTDI DMX protocols including Open DMX USB and similar devices
 */
public class FTDIDMXInterface {

    // DMX constants
    private static final int DMX_START_CODE = 0x00;
    private static final int MAX_DMX_CHANNELS = 512;
    private static final int DMX_BREAK_TIME_US = 88;    // Minimum break time in microseconds
    private static final int DMX_MAB_TIME_US = 8;       // Mark After Break time in microseconds
    private static final int DMX_PACKET_TIME_US = 44;   // Time per DMX slot in microseconds

    // FTDI-specific constants
    private static final byte FTDI_SIO_SET_BREAK_ON = (byte) 0x23;
    private static final byte FTDI_SIO_SET_BREAK_OFF = (byte) 0x24;

    private final InputStream inputStream;
    private final OutputStream outputStream;
    private boolean isConnected = false;
    private DMXProtocolType protocolType;

    /**
     * Constructor that accepts input and output streams
     *
     * @param inputStream  Serial port input stream
     * @param outputStream Serial port output stream
     * @param protocolType Type of FTDI DMX protocol to use
     */
    public FTDIDMXInterface(InputStream inputStream, OutputStream outputStream, DMXProtocolType protocolType) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.protocolType = protocolType;
        this.isConnected = true;
    }

    /**
     * Default constructor using OPEN_DMX_USB protocol
     *
     * @param inputStream
     * @param outputStream
     */
    public FTDIDMXInterface(InputStream inputStream, OutputStream outputStream) {
        this(inputStream, outputStream, DMXProtocolType.OPEN_DMX_USB);
    }

    /**
     * Creates a DMX universe filled with a specific value
     *
     * @param value Value to fill all channels with (0-255)
     * @return Array of 512 bytes with the specified value
     */
    public static byte[] createFullUniverse(int value) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("Value must be between 0-255");
        }

        byte[] universe = new byte[MAX_DMX_CHANNELS];
        Arrays.fill(universe, (byte) value);
        return universe;
    }

    /**
     * Creates a DMX universe with specific channel values
     *
     * @param channelValues Map of channel numbers (1-512) to values (0-255)
     * @return Array of 512 bytes with specified values
     */
    public static byte[] createUniverse(java.util.Map<Integer, Integer> channelValues) {
        byte[] universe = new byte[MAX_DMX_CHANNELS];

        for (java.util.Map.Entry<Integer, Integer> entry : channelValues.entrySet()) {
            int channel = entry.getKey();
            int value = entry.getValue();

            if (channel < 1 || channel > MAX_DMX_CHANNELS) {
                throw new IllegalArgumentException("Channel must be between 1-512");
            }
            if (value < 0 || value > 255) {
                throw new IllegalArgumentException("Value must be between 0-255");
            }

            universe[channel - 1] = (byte) value; // Convert to 0-based index
        }

        return universe;
    }

    /**
     * Example usage demonstrating how to use the class
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            // Example usage - you would replace these with actual serial port streams

            // Create some example DMX data
            byte[] dmxData = new byte[512];

            // Set some channels to various values
            dmxData[0] = (byte) 255;  // Channel 1 full brightness
            dmxData[1] = (byte) 127;  // Channel 2 half brightness
            dmxData[2] = (byte) 64;   // Channel 3 quarter brightness

            // Example of how you would use this with actual serial streams:
            /*
            SerialPort serialPort = new SerialPort("COM3"); // or /dev/ttyUSB0 on Linux
            serialPort.openPort();
            serialPort.setParams(250000, 8, 2, 0); // 250000 baud, 8N2 (note: 2 stop bits for DMX)

            // Try different protocol types
            FTDIDMXInterface dmxDevice = new FTDIDMXInterface(
                serialPort.getInputStream(),
                serialPort.getOutputStream(),
                FTDIDMXInterface.DMXProtocolType.OPEN_DMX_USB
            );

            // Send DMX data
            dmxDevice.sendDMXData(dmxData);

            // Try different protocol
            dmxDevice.setProtocolType(FTDIDMXInterface.DMXProtocolType.ENTTEC_OPEN_DMX);
            dmxDevice.sendDMXData(dmxData);

            // Clean up
            dmxDevice.close();
            serialPort.closePort();
            */

            // Demonstrate utility methods
            System.out.println("FTDI DMX Interface Example");
            System.out.println("Protocol types available:");
            for (DMXProtocolType type : DMXProtocolType.values()) {
                System.out.println("  - " + type);
            }

            // Create universe with specific channels
            java.util.Map<Integer, Integer> channels = new java.util.HashMap<>();
            channels.put(1, 255);  // Channel 1 = 255
            channels.put(2, 127);  // Channel 2 = 127
            channels.put(3, 64);   // Channel 3 = 64

            byte[] universe = createUniverse(channels);
            System.out.println("\nCreated universe with " + channels.size() + " active channels");
            System.out.println("Sample values: " + Arrays.toString(Arrays.copyOf(universe, 5)));

            // Create full universe
            byte[] fullUniverse = createFullUniverse(128);
            System.out.println("Created full universe with all channels at 128");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends DMX data using the specified protocol
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

        switch (protocolType) {
            case OPEN_DMX_USB:
                sendOpenDMXUSB(dmxData);
                break;
            case FTDI_CHIP_DIRECT:
                sendFTDIChipDirect(dmxData);
                break;
            case ENTTEC_OPEN_DMX:
                sendEnttecOpenDMX(dmxData);
                break;
            case GENERIC_SERIAL:
                sendGenericSerial(dmxData);
                break;
            default:
                throw new IllegalArgumentException("Unsupported protocol type: " + protocolType);
        }
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
     * Open DMX USB protocol implementation
     * Simple serial transmission with break and MAB timing
     */
    private void sendOpenDMXUSB(byte[] dmxData) throws IOException {
        // Send break (low signal)
        sendBreak();

        // Send Mark After Break (MAB) - high signal
        sendMAB();

        // Send start code
        outputStream.write(DMX_START_CODE);

        // Send DMX data
        outputStream.write(dmxData);

        // Pad to 512 channels if needed
        if (dmxData.length < MAX_DMX_CHANNELS) {
            byte[] padding = new byte[MAX_DMX_CHANNELS - dmxData.length];
            outputStream.write(padding);
        }

        outputStream.flush();
    }

    /**
     * FTDI chip direct communication
     * Uses FTDI-specific commands for break generation
     */
    private void sendFTDIChipDirect(byte[] dmxData) throws IOException {
        // Set break condition
        outputStream.write(FTDI_SIO_SET_BREAK_ON);
        microDelay(DMX_BREAK_TIME_US);

        // Clear break condition
        outputStream.write(FTDI_SIO_SET_BREAK_OFF);
        microDelay(DMX_MAB_TIME_US);

        // Send start code
        outputStream.write(DMX_START_CODE);

        // Send DMX data with inter-slot timing
        for (int i = 0; i < dmxData.length; i++) {
            outputStream.write(dmxData[i]);
            if (i < dmxData.length - 1) {
                microDelay(DMX_PACKET_TIME_US);
            }
        }

        // Pad to 512 channels if needed
        for (int i = dmxData.length; i < MAX_DMX_CHANNELS; i++) {
            outputStream.write(0);
            if (i < MAX_DMX_CHANNELS - 1) {
                microDelay(DMX_PACKET_TIME_US);
            }
        }

        outputStream.flush();
    }

    /**
     * Enttec Open DMX USB protocol (FTDI-based)
     * Similar to Open DMX USB but with specific timing requirements
     */
    private void sendEnttecOpenDMX(byte[] dmxData) throws IOException {
        // Enttec Open DMX USB uses a simple serial protocol
        // Send break by writing to serial port with specific timing
        sendBreak();

        // Short delay for Mark After Break
        microDelay(DMX_MAB_TIME_US);

        // Create complete DMX packet
        byte[] packet = new byte[dmxData.length + 1];
        packet[0] = (byte) DMX_START_CODE;
        System.arraycopy(dmxData, 0, packet, 1, dmxData.length);

        // Send packet
        outputStream.write(packet);
        outputStream.flush();
    }

    /**
     * Generic serial-based DMX transmission
     * Most basic implementation for simple FTDI-based devices
     */
    private void sendGenericSerial(byte[] dmxData) throws IOException {
        // Simple approach: just send the data with start code
        outputStream.write(DMX_START_CODE);
        outputStream.write(dmxData);
        outputStream.flush();
    }

    /**
     * Sends a DMX break signal
     * This is a simplified implementation - actual break generation
     * depends on the serial port configuration
     */
    private void sendBreak() throws IOException {
        // For most FTDI devices, sending a null byte at a lower baud rate
        // can simulate a break condition
        outputStream.write(0x00);
        microDelay(DMX_BREAK_TIME_US);
    }

    /**
     * Sends Mark After Break signal
     */
    private void sendMAB() throws IOException {
        // MAB is typically handled by the serial port returning to idle state
        microDelay(DMX_MAB_TIME_US);
    }

    /**
     * Microsecond delay implementation
     * Note: Java's timing resolution is limited, this is best-effort
     */
    private void microDelay(int microseconds) {
        if (microseconds <= 0) return;

        long nanos = microseconds * 1000L;
        long startTime = System.nanoTime();

        // Busy wait for very short delays
        if (nanos < 10000) { // Less than 10ms
            while (System.nanoTime() - startTime < nanos) {
                // Busy wait
            }
        } else {
            // Use Thread.sleep for longer delays
            try {
                long millis = nanos / 1000000;
                int remainingNanos = (int) (nanos % 1000000);
                Thread.sleep(millis, remainingNanos);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Gets the current protocol type
     *
     * @return Current protocol type
     */
    public DMXProtocolType getProtocolType() {
        return protocolType;
    }

    /**
     * Sets the protocol type for this interface
     *
     * @param protocolType New protocol type to use
     */
    public void setProtocolType(DMXProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    /**
     * Checks if the device is connected
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return isConnected;
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

    /**
     * Enumeration of supported FTDI DMX protocol types
     */
    public enum DMXProtocolType {
        /**
         * Simple serial transmission
         */
        OPEN_DMX_USB,
        /**
         * Direct FTDI chip communication
         */
        FTDI_CHIP_DIRECT,
        /**
         * Enttec Open DMX USB (FTDI-based)
         */
        ENTTEC_OPEN_DMX,
        /**
         * Generic serial-based DMX
         */
        GENERIC_SERIAL
    }
}