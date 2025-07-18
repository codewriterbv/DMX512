package be.codewriter.dmx512.controller.serial;

import be.codewriter.dmx512.controller.DMXController;
import be.codewriter.dmx512.controller.change.DMXStatusChangeMessage;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(DMXSerialController.class.getName());

    private final String portName;
    private SerialPort serialPort;
    private OutputStream outputStream;
    private boolean connected = false;
    private volatile boolean isRunning = false;
    private Thread transmitThread;

    public DMXSerialController(String portName) {
        this.portName = portName;

        connect();
    }

    @Override
    public DMXType getType() {
        return DMXType.SERIAL;
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
            sendData();
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

        System.arraycopy(data, 0, universe, 0, data.length);
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
     * Send the given raw data to the DMX interface.
     */
    private void sendData(byte[] data) {
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

        // System.arraycopy(data, 0, universe, 0, data.length);
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
                // TODO serialPort.getOutputStream().write(universe);
                serialPort.getOutputStream().flush();

                // TODO LOGGER.info("Sending DMX data: {}", HexFormat.of().formatHex(universe));

                // DMX refresh rate (typically 40Hz)
                Thread.sleep(25); // 25ms = 40Hz
            } catch (Exception e) {
                isRunning = false;
                e.printStackTrace();
            }
        }
    }


}
