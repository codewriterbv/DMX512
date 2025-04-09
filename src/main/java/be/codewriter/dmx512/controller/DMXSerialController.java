package be.codewriter.dmx512.controller;

import be.codewriter.dmx512.serial.SerialConnection;
import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
 
/**
 * DMX Serial Controller.
 * Controls DMX lights over USB-to-DMX interface using jSerialComm.
 */
public class DMXSerialController implements DMXController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DMXSerialController.class.getName());

    private static final int DMX_UNIVERSE_SIZE = 512;
    private final byte[] dmxData;
    private SerialPort serialPort;
    private OutputStream outputStream;
    private boolean connected = false;

    public DMXSerialController() {
        dmxData = new byte[DMX_UNIVERSE_SIZE + 1]; // +1 for start code
        dmxData[0] = 0; // DMX start code
        // Initialize all channels to 0
        Arrays.fill(dmxData, 1, DMX_UNIVERSE_SIZE + 1, (byte) 0);
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
     * Set a DMX channel to a specific value
     *
     * @param channel DMX channel (1-512)
     * @param value   Value (0-255)
     */
    public void setChannel(int channel, int value) {
        if (channel < 1 || channel > DMX_UNIVERSE_SIZE) {
            throw new IllegalArgumentException("Channel must be between 1 and " + DMX_UNIVERSE_SIZE);
        }

        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("Value must be between 0 and 255");
        }

        dmxData[channel] = (byte) value;
    }

    /**
     * Set multiple DMX channels at once
     *
     * @param startChannel First channel to set
     * @param values       Array of values to set
     */
    public void setChannels(int startChannel, int[] values) {
        if (startChannel < 1 || startChannel > DMX_UNIVERSE_SIZE) {
            throw new IllegalArgumentException("Start channel must be between 1 and " + DMX_UNIVERSE_SIZE);
        }

        if (startChannel + values.length - 1 > DMX_UNIVERSE_SIZE) {
            throw new IllegalArgumentException("Channel range exceeds DMX universe size");
        }

        for (int i = 0; i < values.length; i++) {
            if (values[i] < 0 || values[i] > 255) {
                throw new IllegalArgumentException("Value must be between 0 and 255");
            }
            dmxData[startChannel + i] = (byte) values[i];
        }
    }

    /**
     * Send the current DMX universe data to the interface
     */
    public void render() {
        if (!connected || outputStream == null) {
            throw new IllegalStateException("Not connected to DMX interface");
        }

        // Send DMX break
        serialPort.setBreak();
        try {
            Thread.sleep(1); // Break duration (1ms)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        serialPort.clearBreak();

        try {
            Thread.sleep(1); // Mark After Break (MAB) duration
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Send DMX data
        try {
            outputStream.write(dmxData);
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
     * Get the value of a DMX channel
     *
     * @param channel DMX channel (1-512)
     * @return The current value (0-255)
     */
    public int getChannel(int channel) {
        if (channel < 1 || channel > DMX_UNIVERSE_SIZE) {
            throw new IllegalArgumentException("Channel must be between 1 and " + DMX_UNIVERSE_SIZE);
        }

        return dmxData[channel] & 0xFF; // Convert to unsigned byte
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
                        p.getSystemPortName(),
                        p.getSystemPortPath(),
                        p.getDescriptivePortName(),
                        p.getManufacturer(),
                        p.getSerialNumber(),
                        p.getPortDescription(),
                        p.getPortLocation()
                        ))
                .sorted(Comparator.comparing(SerialConnection::name))
                .toList();
    }
}
