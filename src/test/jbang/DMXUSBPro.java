import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

import jssc.*;

/**
 * Java port of the ENTTEC DMX USB PRO C++ application
 * Uses JSSC (Java Simple Serial Connector) library for serial communication
 * <p>
 * Dependencies:
 * - jssc-2.8.0.jar (Java Simple Serial Connector)
 * <p>
 * Note: This assumes the FTDI device appears as a serial port in your system
 */
public class DMXUSBPro {

    // Constants from the original C code
    private static final int DMX_START_CODE = 0x7E;
    private static final int DMX_END_CODE = 0xE7;
    private static final int DMX_HEADER_LENGTH = 4;
    private static final int DMX_PACKET_SIZE = 513;
    private static final int ONE_BYTE = 1;
    private static final int BYTE_LENGTH = 8;
    private static final int OFFSET = 0xFF;
    private static final boolean NO_RESPONSE = false;

    // Command Labels
    private static final int GET_WIDGET_PARAMS = 3;
    private static final int GET_WIDGET_PARAMS_REPLY = 3;
    private static final int GET_WIDGET_SN = 10;
    private static final int SET_DMX_RX_MODE = 8;
    private static final int RECEIVE_DMX_ON_CHANGE = 8;

    // Serial port instance
    private SerialPort serialPort;
    private boolean deviceConnected = false;
    private DMXUSBPROParams proParams = new DMXUSBPROParams();

    /**
     * Main method - equivalent to the original C main function
     */
    public static void main(String[] args) {
        DMXUSBPro dmxPro = new DMXUSBPro();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enttec Pro - Java - JSSC Test");
        System.out.println("Looking for devices connected to PC...");

        String[] availablePorts = dmxPro.listDevices();

        if (availablePorts.length == 0) {
            System.out.println("Looking for Devices - 0 Found");
            return;
        }

        System.out.println("Press Enter to Initialize Device:");
        scanner.nextLine();

        // Try to connect to the first available port
        // In a real application, you might want to let the user choose
        boolean connected = false;
        for (String port : availablePorts) {
            System.out.println("Trying to connect to: " + port);
            if (dmxPro.openDevice(port)) {
                connected = true;
                break;
            }
        }

        if (connected) {
            // DMX receiving loop (equivalent to the C version)
            System.out.println("Press Enter to receive DMX data:");
            scanner.nextLine();

            System.out.println("Set the widget to send DMX only when signal changes...");
            byte[] changeFlag = {1};
            if (!dmxPro.sendData(RECEIVE_DMX_ON_CHANGE, changeFlag, 0)) {
                System.err.println("FAILED to set receive mode");
                dmxPro.closePort();
                return;
            }

            // Receive DMX data loop
            for (int i = 0; i < 1000; i++) {
                byte[] dmxIn = new byte[513];

                if (!dmxPro.receiveDMX(SET_DMX_RX_MODE, dmxIn, 513)) {
                    System.err.println("FAILED to receive DMX data");
                    dmxPro.closePort();
                    break;
                }

                System.out.printf("Iteration: %d%n", i);
                System.out.print("DMX Data from 0 to 8: ");
                for (int j = 0; j <= 8; j++) {
                    System.out.printf(" %d ", dmxIn[j] & 0xFF);
                }
                System.out.println();

                // Small delay to prevent overwhelming the console
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        System.out.println("Press Enter to Exit:");
        scanner.nextLine();

        dmxPro.closePort();
        scanner.close();
    }

    /**
     * Close the serial port connection
     */
    public void closePort() {
        if (serialPort != null && serialPort.isOpened()) {
            try {
                serialPort.closePort();
                System.out.println("Port closed successfully");
            } catch (SerialPortException e) {
                System.err.println("Error closing port: " + e.getMessage());
            }
        }
    }

    /**
     * List available serial ports
     */
    public String[] listDevices() {
        String[] portNames = SerialPortList.getPortNames();
        System.out.println("Available ports: " + Arrays.toString(portNames));
        return portNames;
    }

    /**
     * Send data to the DMX USB PRO device
     */
    public boolean sendData(int label, byte[] data, int length) {
        if (serialPort == null || !serialPort.isOpened()) {
            return false;
        }

        try {
            // Form packet header
            byte[] header = new byte[DMX_HEADER_LENGTH];
            header[0] = (byte) DMX_START_CODE;
            header[1] = (byte) label;
            header[2] = (byte) (length & OFFSET);
            header[3] = (byte) (length >> BYTE_LENGTH);

            // Write header
            if (!serialPort.writeBytes(header)) {
                return false;
            }

            // Write data
            if (length > 0 && data != null) {
                byte[] dataToSend = Arrays.copyOf(data, length);
                if (!serialPort.writeBytes(dataToSend)) {
                    return false;
                }
            }

            // Write end code
            byte[] endCode = {(byte) DMX_END_CODE};
            return serialPort.writeBytes(endCode);

        } catch (SerialPortException e) {
            System.err.println("Error sending data: " + e.getMessage());
            return false;
        }
    }

    /**
     * Receive data from the DMX USB PRO device
     */
    public boolean receiveData(int expectedLabel, byte[] buffer, int expectedLength) {
        if (serialPort == null || !serialPort.isOpened()) {
            return false;
        }

        try {
            // Look for start code and matching label
            byte currentByte = 0;

            // Wait for start code
            while (currentByte != (byte) DMX_START_CODE) {
                byte[] readByte = serialPort.readBytes(1, 5000); // 5 second timeout
                if (readByte == null || readByte.length == 0) {
                    return false;
                }
                currentByte = readByte[0];
            }

            // Read label
            byte[] labelByte = serialPort.readBytes(1, 1000);
            if (labelByte == null || labelByte.length == 0) {
                return false;
            }

            if ((labelByte[0] & 0xFF) != expectedLabel) {
                return false;
            }

            // Read length (2 bytes, little endian)
            byte[] lengthBytes = serialPort.readBytes(2, 1000);
            if (lengthBytes == null || lengthBytes.length != 2) {
                return false;
            }

            int length = (lengthBytes[0] & 0xFF) + ((lengthBytes[1] & 0xFF) << BYTE_LENGTH);

            if (length > DMX_PACKET_SIZE) {
                return false;
            }

            // Read the actual data
            byte[] receivedData = serialPort.readBytes(length, 2000);
            if (receivedData == null || receivedData.length != length) {
                return false;
            }

            // Read end code
            byte[] endCodeByte = serialPort.readBytes(1, 1000);
            if (endCodeByte == null || endCodeByte.length == 0) {
                return false;
            }

            if ((endCodeByte[0] & 0xFF) != DMX_END_CODE) {
                return false;
            }

            // Copy data to buffer
            int copyLength = Math.min(expectedLength, receivedData.length);
            System.arraycopy(receivedData, 0, buffer, 0, copyLength);

            return true;

        } catch (SerialPortException | SerialPortTimeoutException e) {
            System.err.println("Error receiving data: " + e.getMessage());
            return false;
        }
    }

    /**
     * Receive DMX data specifically
     */
    public boolean receiveDMX(int label, byte[] data, int expectedLength) {
        if (serialPort == null || !serialPort.isOpened()) {
            return false;
        }

        try {
            // Wait for start code
            byte currentByte = 0;
            while (currentByte != (byte) DMX_START_CODE) {
                byte[] readByte = serialPort.readBytes(1, 5000);
                if (readByte == null || readByte.length == 0) {
                    return false;
                }
                currentByte = readByte[0];
            }

            // Read header (label + length)
            byte[] header = serialPort.readBytes(3, 1000);
            if (header == null || header.length != 3) {
                return false;
            }

            if ((header[0] & 0xFF) != label) {
                return false;
            }

            int length = (header[1] & 0xFF) + ((header[2] & 0xFF) << BYTE_LENGTH) + 1;

            if (length > DMX_PACKET_SIZE + 2) {
                return false;
            }

            // Read data + end code
            byte[] buffer = serialPort.readBytes(length, 2000);
            if (buffer == null || buffer.length != length) {
                return false;
            }

            // Check end code
            if ((buffer[length - 1] & 0xFF) != DMX_END_CODE) {
                return false;
            }

            // Copy data
            int copyLength = Math.min(expectedLength, buffer.length - 1);
            System.arraycopy(buffer, 0, data, 0, copyLength);

            return true;

        } catch (SerialPortException | SerialPortTimeoutException e) {
            System.err.println("Error receiving DMX: " + e.getMessage());
            return false;
        }
    }

    /**
     * Purge the serial port buffers
     */
    public void purgeBuffer() {
        if (serialPort != null && serialPort.isOpened()) {
            try {
                serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
            } catch (SerialPortException e) {
                System.err.println("Error purging buffer: " + e.getMessage());
            }
        }
    }

    /**
     * Open and initialize the DMX USB PRO device
     */
    public boolean openDevice(String portName) {
        try {
            serialPort = new SerialPort(portName);

            System.out.println("Opening device on port: " + portName);

            // Open port with typical FTDI settings
            boolean opened = serialPort.openPort();
            if (!opened) {
                System.err.println("Failed to open port");
                return false;
            }

            // Set port parameters (typical for FTDI devices)
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            // Set timeouts
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

            System.out.println("Port opened successfully");

            // Purge buffers
            purgeBuffer();

            // Send GET_WIDGET_PARAMS to get device info
            System.out.println("Sending GET_WIDGET_PARAMS packet...");
            byte[] dummy = new byte[2];
            if (!sendData(GET_WIDGET_PARAMS, dummy, 2)) {
                System.err.println("Failed to send GET_WIDGET_PARAMS");
                closePort();
                return false;
            }

            System.out.println("PRO Connected Successfully");

            // Receive widget response
            System.out.println("Waiting for GET_WIDGET_PARAMS_REPLY packet...");
            byte[] paramsBuffer = new byte[5]; // Assuming 5-byte parameter structure
            if (!receiveData(GET_WIDGET_PARAMS_REPLY, paramsBuffer, 5)) {
                System.err.println("Failed to receive GET_WIDGET_PARAMS_REPLY");
                closePort();
                return false;
            }

            // Parse parameters
            proParams.firmwareLSB = paramsBuffer[0] & 0xFF;
            proParams.firmwareMSB = paramsBuffer[1] & 0xFF;
            proParams.breakTime = paramsBuffer[2] & 0xFF;
            proParams.mabTime = paramsBuffer[3] & 0xFF;
            proParams.refreshRate = paramsBuffer[4] & 0xFF;

            System.out.println("GET WIDGET REPLY Received...");

            // Display device information
            System.out.println("-----------::PRO Connected [Information Follows]::------------");
            System.out.printf("\t\t  FIRMWARE VERSION: %d.%d%n", proParams.firmwareMSB, proParams.firmwareLSB);

            int breakTime = (int) (proParams.breakTime * 10.67) + 100;
            System.out.printf("\t\t  BREAK TIME: %d micro sec%n", breakTime);

            int mabTime = (int) (proParams.mabTime * 10.67);
            System.out.printf("\t\t  MAB TIME: %d micro sec%n", mabTime);

            System.out.printf("\t\t  SEND REFRESH RATE: %d packets/sec%n", proParams.refreshRate);

            deviceConnected = true;
            return true;

        } catch (SerialPortException e) {
            System.err.println("Error opening device: " + e.getMessage());
            return false;
        }
    }

    // DMX USB PRO Parameters structure equivalent
    private static class DMXUSBPROParams {
        public int firmwareMSB;
        public int firmwareLSB;
        public int breakTime;
        public int mabTime;
        public int refreshRate;

        public DMXUSBPROParams() {
            this.firmwareMSB = 0;
            this.firmwareLSB = 0;
            this.breakTime = 0;
            this.mabTime = 0;
            this.refreshRate = 0;
        }
    }
}