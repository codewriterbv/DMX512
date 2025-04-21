package be.codewriter.dmx512.controller;

import be.codewriter.dmx512.client.DMXClient;
import be.codewriter.dmx512.helper.DMXMessage;
import be.codewriter.dmx512.serial.SerialConnection;
import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * DMX Serial Controller.
 * Controls DMX lights over USB-to-DMX interface using jSerialComm.
 */
public class DMXSerialController implements DMXController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DMXSerialController.class.getName());

    private static final int DMX_UNIVERSE_SIZE = 512;
    private final List<DMXMessage> messages;
    private SerialPort serialPort;
    private OutputStream outputStream;
    private boolean connected = false;

    public DMXSerialController() {
        this.messages = Collections.synchronizedList(new ArrayList<>());

        // Start message processing thread
        Thread.startVirtualThread(this::processMessages);
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
                250000,                  // Baud rate (DMX uses 250k)
                8,                       // Data bits
                SerialPort.TWO_STOP_BITS,
                SerialPort.NO_PARITY
        );

        // Set timeouts
        serialPort.setComPortTimeouts(
                SerialPort.TIMEOUT_WRITE_BLOCKING,
                0,
                2000
        );

        if (serialPort.openPort()) {
            outputStream = serialPort.getOutputStream();
            connected = true;
            return true;
        } else {
            LOGGER.error("Failed to open port: {}", portName);
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

        synchronized (messages) {
            messages.add(dmxMessage);
            messages.notify(); // Notify the processing thread that new data is available
        }
    }

    /**
     * Continuous processing of the message list.
     */
    private void processMessages() {
        while (!Thread.currentThread().isInterrupted()) {
            DMXMessage message = null;

            synchronized (messages) {
                while (messages.isEmpty()) {
                    try {
                        messages.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                message = messages.remove(0);
            }

            if (message != null) {
                sendMessage(message);
            }
        }
    }

    /**
     * Send the given DMX message to the DMX interface.
     */
    private void sendMessage(DMXMessage message) {
        // Send DMX break
        serialPort.setBreak();

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
        }

        // Send DMX data
        try {
            outputStream.write(message.getData());
            outputStream.flush();
        } catch (Exception e) {
            LOGGER.error("Failed to send DMX data: {}", e.getMessage());
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
