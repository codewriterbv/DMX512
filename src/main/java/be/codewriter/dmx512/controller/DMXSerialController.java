package be.codewriter.dmx512.controller;

import be.codewriter.dmx512.client.DMXClient;
import be.codewriter.dmx512.helper.DMXMessage;
import be.codewriter.dmx512.serial.SerialConnection;
import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

/**
 * DMX Serial Controller.
 * Controls DMX lights over USB-to-DMX interface using jSerialComm.
 */
public class DMXSerialController extends DMXChangeNotifier implements DMXController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DMXSerialController.class.getName());

    private static final int DMX_UNIVERSE_SIZE = 512;
    private final byte[] universe = new byte[DMX_UNIVERSE_SIZE];
    private SerialPort serialPort;
    private OutputStream outputStream;
    private boolean connected = false;
    private volatile boolean isRunning = false;
    private Thread transmitThread;

    public DMXSerialController() {
    }

    /**
     * Connect to the USB-DMX interface
     *
     * @param portName Name of the serial port (e.g., "COM3" on Windows, "/dev/ttyUSB0" on Linux)
     * @return true if connection successful
     */
    public boolean connect(String portName) {
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
            notifyListeners(DMXChangeMessage.CONNECTED);
            sendData();
            return true;
        } else {
            LOGGER.error("Failed to open port: {}", portName);
            notifyListeners(DMXChangeMessage.DISCONNECTED);
            return false;
        }
    }

    /**
     * Add the current DMX universe data to list of messages to be sent to the DMX interface.
     * This list will be processed asap in a separate thread.
     */
    @Override
    public synchronized void render(List<DMXClient> clients) {
        var dmxMessage = new DMXMessage(clients);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("DMX message: {}", HexFormat.of().formatHex(dmxMessage.getData()));
        }

        if (!connected || outputStream == null) {
            LOGGER.error("Not connected to DMX interface, can't render data to the devices");
            return;
        }

        System.arraycopy(dmxMessage.getData(), 0, universe, 0, dmxMessage.getData().length);
    }

    /**
     * Send the given raw data to the DMX interface.
     */
    @Override
    public void sendData(byte[] data) {
        // Send DMX break
        /*serialPort.setBreak();

        try {
            Thread.sleep(1); // Break duration (1ms)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        // Clear DMX break
        serialPort.clearBreak();

        try {
            Thread.sleep(1); // Mark After Break (MAB) duration
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }*/


        // Send DMX data
        /*try {
            outputStream.write(new byte[]{(byte) 0});
            Thread.sleep(10);


            outputStream.write(new byte[]{(byte) 1});
            Thread.sleep(10);

            LOGGER.info("Sending DMX data: {}", HexFormat.of().formatHex(data));
            outputStream.write(data);
            outputStream.flush();
        } catch (Exception e) {
            LOGGER.error("Failed to send DMX data: {}", e.getMessage());
        }*/

        System.arraycopy(data, 0, universe, 0, data.length);
    }

    public void sendData() {
        // Start continuous DMX transmission in background
        if (transmitThread == null || !transmitThread.isAlive()) {
            isRunning = true;
            transmitThread = new Thread(this::transmitDMXLoop);
            transmitThread.setDaemon(true);
            transmitThread.start();
        }
    }

    private void transmitDMXLoop() {
        while (isRunning && serialPort.isOpen()) {
            try {
                // Send DMX break (0.0V) for at least 88 microseconds
                serialPort.setBreak();
                Thread.sleep(1); // 1ms is plenty for a break

                // Send DMX mark after break (1.0V) for at least 8 microseconds
                serialPort.clearBreak();
                Thread.sleep(1); // 1ms is plenty for mark after break

                // Start code (0) + DMX data
                serialPort.getOutputStream().write(0); // Start code
                serialPort.getOutputStream().write(universe);
                serialPort.getOutputStream().flush();

                LOGGER.info("Sending DMX data: {}", HexFormat.of().formatHex(universe));

                // DMX refresh rate (typically 40Hz)
                Thread.sleep(25); // 25ms = 40Hz
            } catch (Exception e) {
                isRunning = false;
                e.printStackTrace();
            }
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
                e.printStackTrace();
            } finally {
                if (serialPort != null) {
                    serialPort.closePort();
                }
                connected = false;
                notifyListeners(DMXChangeMessage.DISCONNECTED);
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
     * Get all the available serial connections
     */
    public List<SerialConnection> getAvailablePorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        return Arrays.stream(ports)
                .map(p -> new SerialConnection(
                        p.getSystemPortPath(),
                        p.getSystemPortName(),
                        p.getDescriptivePortName()))
                .sorted(Comparator.comparing(SerialConnection::name))
                .toList();
    }
}
