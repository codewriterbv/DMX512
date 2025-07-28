package be.codewriter.dmx512.controller.serial;

import be.codewriter.dmx512.controller.DMXController;
import be.codewriter.dmx512.controller.change.DMXStatusChangeMessage;
import be.codewriter.dmx512.controller.serial.builder.EnttecDMXUSBProBuilder;
import be.codewriter.dmx512.model.DMXUniverse;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HexFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    /**
     * Minimum break time in microseconds
     */
    private static final int DMX_BREAK_TIME_US = 88;
    /**
     * Mark After Break time in microseconds
     */
    private static final int DMX_MAB_TIME_US = 8;
    /**
     * Time per DMX slot in microseconds
     */
    private static final int DMX_PACKET_TIME_US = 44;
    /**
     * Interval between DMX message refresh on the serial connection
     */
    private static final int DMX_FRAME_INTERVAL_MS = 50;

    private final String portName;
    private final SerialProtocol protocol;
    private final AtomicBoolean shouldTransmit = new AtomicBoolean(false);
    private final ReadWriteLock dataLock = new ReentrantReadWriteLock();
    private final byte[] currentDmxData = new byte[MAX_DMX_CHANNELS];
    private SerialPort serialPort;
    private OutputStream outputStream;
    private boolean connected = false;
    // Continuous transmission support
    private Thread transmissionThread;
    private volatile boolean dataChanged = false;

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
                SerialPort.TIMEOUT_WRITE_BLOCKING,
                0,
                500
        );

        if (serialPort.openPort()) {
            outputStream = serialPort.getOutputStream();
            connected = true;

            setupDisconnectListener();
            startContinuousTransmission();
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

        // Update the current DMX data that's being continuously transmitted
        dataLock.writeLock().lock();
        try {
            // Copy new data to current buffer
            int copyLength = Math.min(data.length, MAX_DMX_CHANNELS);
            System.arraycopy(data, 0, currentDmxData, 0, copyLength);

            // Zero out any remaining channels if the new data is shorter
            if (copyLength < MAX_DMX_CHANNELS) {
                for (int i = copyLength; i < MAX_DMX_CHANNELS; i++) {
                    currentDmxData[i] = 0;
                }
            }

            dataChanged = true;
        } finally {
            dataLock.writeLock().unlock();
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
     * Start continuous DMX transmission in a background thread
     */
    private void startContinuousTransmission() {
        if (transmissionThread != null && transmissionThread.isAlive()) {
            stopContinuousTransmission();
        }

        shouldTransmit.set(true);
        transmissionThread = new Thread(this::continuousTransmissionLoop, "DMX-Transmission-" + portName);
        transmissionThread.setDaemon(true);
        transmissionThread.start();

        LOGGER.info("Started continuous DMX transmission thread for port {}", portName);
    }

    /**
     * Stop continuous DMX transmission
     */
    private void stopContinuousTransmission() {
        shouldTransmit.set(false);
        if (transmissionThread != null) {
            transmissionThread.interrupt();
            try {
                transmissionThread.join(1000); // Wait up to 1 second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        LOGGER.info("Stopped continuous DMX transmission thread for port {}", portName);
    }

    /**
     * Continuous transmission loop - runs in background thread
     */
    private void continuousTransmissionLoop() {
        byte[] localDataBuffer = new byte[MAX_DMX_CHANNELS];

        while (shouldTransmit.get() && connected) {
            try {
                // Copy current data if it has changed
                boolean hasNewData = false;
                dataLock.readLock().lock();
                try {
                    if (dataChanged) {
                        System.arraycopy(currentDmxData, 0, localDataBuffer, 0, MAX_DMX_CHANNELS);
                        dataChanged = false;
                        hasNewData = true;
                    }
                } finally {
                    dataLock.readLock().unlock();
                }

                // Send DMX packet
                sendDmxPacket(localDataBuffer);

                if (hasNewData && LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Transmitted updated DMX data");
                }

                // Wait for next frame
                Thread.sleep(DMX_FRAME_INTERVAL_MS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (IOException e) {
                LOGGER.error("Error during continuous transmission: {}", e.getMessage());
                // Short delay before retrying
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        LOGGER.debug("Continuous transmission loop ended for port {}", portName);
    }

    /**
     * Send a single DMX packet based on the configured protocol
     */
    private void sendDmxPacket(byte[] dmxData) throws IOException {
        if (!connected || outputStream == null) {
            return;
        }

        try {
            switch (protocol) {
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
                    throw new IllegalArgumentException("Unsupported protocol type: " + protocol);
            }
        } catch (IOException e) {
            // Re-throw to be handled by the calling method
            throw e;
        }
    }

    private void setupDisconnectListener() {
        if (serialPort != null) {
            serialPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
                }

                @Override
                public void serialEvent(SerialPortEvent event) {
                    if (event.getEventType() == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED) {
                        LOGGER.warn("Serial port {} disconnected", portName);
                        handleDisconnection("Port physically disconnected");
                    }
                }
            });
        }
    }

    private void handleDisconnection(String reason) {
        connected = false;
        LOGGER.error("DMX Serial Controller disconnected: {}", reason);

        // Clean up resources
        try {
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
        } catch (IOException e) {
            LOGGER.error("Error closing output stream during disconnect: {}", e.getMessage());
        }

        // Close the port
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
        }

        // Notify listeners
        notifyListeners(DMXStatusChangeMessage.DISCONNECTED);
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
