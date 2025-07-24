package be.codewriter.dmx512.controller.serial;

import be.codewriter.dmx512.controller.DMXController;
import be.codewriter.dmx512.controller.change.DMXStatusChangeMessage;
import be.codewriter.dmx512.controller.serial.builder.EnttecDMXUSBProBuilder;
import be.codewriter.dmx512.model.DMXUniverse;
import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HexFormat;

/**
 * DMX Serial Controller.
 * Controls DMX lights over USB-to-DMX interface using jSerialComm.
 */
public class DMXSerialController implements DMXController {
    /**
     * DMX start command
     */
    public static final int DMX_START_CODE = 0x00;
    /**
     * Maximum number of DMX channels
     */
    public static final int MAX_DMX_CHANNELS = 512;
    private static final Logger LOGGER = LoggerFactory.getLogger(DMXSerialController.class.getName());
    private static final int DMX_BREAK_TIME_US = 88;    // Minimum break time in microseconds
    private static final int DMX_MAB_TIME_US = 8;       // Mark After Break time in microseconds
    private static final int DMX_PACKET_TIME_US = 44;   // Time per DMX slot in microseconds

    private final String portName;
    private final SerialProtocol protocol;
    private SerialPort serialPort;
    private OutputStream outputStream;
    private boolean connected = false;

    /**
     * Constructor for a serial (USB) controller on the given port name with the Enttec protocol.
     *
     * @param portName serial (USB) port name
     */
    public DMXSerialController(String portName) {
        this(portName, SerialProtocol.ENTTEC_OPEN_DMX);
    }

    /**
     * Constructor for a serial (USB) controller on the given port name.
     *
     * @param protocol {@link SerialProtocol}
     * @param portName serial (USB) port name
     */
    public DMXSerialController(String portName, SerialProtocol protocol) {
        this.portName = portName;
        this.protocol = protocol;

        connect();
    }

    @Override
    public DMXControllerType getType() {
        return DMXControllerType.SERIAL;
    }

    @Override
    public String getProtocolName() {
        return protocol.name();
    }

    @Override
    public String getAddress() {
        return portName;
    }

    /**
     * Connect to the USB-DMX interface
     *
     * @return true if connection successful
     */
    @Override
    public boolean connect() {
        // Find the port by name
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            if (port.getSystemPortName().equals(portName) ||
                    port.getDescriptivePortName().contains(portName)) {
                serialPort = port;
                break;
            }
        }

        if (serialPort == null) {
            LOGGER.error("Port not found: {}", portName);
            return false;
        }

        // Configure and open the port
        serialPort.setComPortParameters(
                250_000,     // Baud rate (DMX uses 250k)
                8,                      // Data bits
                SerialPort.TWO_STOP_BITS,
                SerialPort.NO_PARITY
        );

        // Set timeouts
        serialPort.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
                100,
                0
        );

        if (serialPort.openPort()) {
            outputStream = serialPort.getOutputStream();
            connected = true;
            notifyListeners(DMXStatusChangeMessage.CONNECTED);
            return true;
        } else {
            LOGGER.error("Failed to open port: {}", portName);
            notifyListeners(DMXStatusChangeMessage.DISCONNECTED);
            return false;
        }
    }

    @Override
    public synchronized void render(DMXUniverse universe) {
        render(universe.getId(), universe.getData());
    }

    @Override
    public synchronized void render(int universe, byte[] data) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("DMX message: {}", HexFormat.of().formatHex(data));
        }

        if (!connected || outputStream == null) {
            LOGGER.error("Not connected to DMX interface, can't render data to the devices");
            return;
        }

        try {
            switch (protocol) {
                case OPEN_DMX_USB:
                    sendOpenDMXUSB(data);
                    break;
                case FTDI_CHIP_DIRECT:
                    sendFTDIChipDirect(data);
                    break;
                case ENTTEC_OPEN_DMX:
                    sendEnttecOpenDMX(data);
                    break;
                case GENERIC_SERIAL:
                    sendGenericSerial(data);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported protocol type: " + protocol);
            }
        } catch (IOException e) {
            LOGGER.error("Could not send DMX message: {}", e.getMessage());
        }
    }

    /**
     * Close the connection to the DMX interface
     */
    public void close() {
        if (connected) {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                LOGGER.error("Failed to close output stream: {}", e.getMessage());
            } finally {
                if (serialPort != null) {
                    serialPort.closePort();
                }
                connected = false;
                notifyListeners(DMXStatusChangeMessage.DISCONNECTED);
            }
        }
    }

    /**
     * Check if the controller is connected
     *
     * @return true if connected
     */
    public boolean isConnected() {
        return connected && serialPort != null && serialPort.isOpen();
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
     * Enttec Open DMX USB protocol (FTDI-based)
     * Similar to Open DMX USB but with specific timing requirements
     */
    private void sendEnttecOpenDMX(byte[] dmxData) throws IOException {
        // Enttec Open DMX USB uses a simple serial protocol
        // Send break by writing to serial port with specific timing
        sendBreak();

        // Short delay for Mark After Break
        microDelay(DMX_MAB_TIME_US);

        // Send packet
        var enttecPacket = EnttecDMXUSBProBuilder.createEnttecDMXPacket(dmxData);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Enttec message: {}", HexFormat.of().formatHex(enttecPacket));
        }
        outputStream.write(enttecPacket);
        outputStream.flush();
    }

    /**
     * FTDI chip direct communication
     * Uses FTDI-specific commands for break generation
     */
    private void sendFTDIChipDirect(byte[] dmxData) throws IOException {
        byte FTDI_SIO_SET_BREAK_ON = (byte) 0x23;
        byte FTDI_SIO_SET_BREAK_OFF = (byte) 0x24;

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
        if (serialPort.setBreak()) {
            microDelay(DMX_BREAK_TIME_US);
            serialPort.clearBreak();
        } else {
            // Fallback: temporarily change baud rate for break timing
            int originalBaud = serialPort.getBaudRate();
            serialPort.setBaudRate(90000); // Lower baud rate
            outputStream.write(0x00);
            outputStream.flush();
            microDelay(DMX_BREAK_TIME_US);
            serialPort.setBaudRate(originalBaud);
        }
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
}
