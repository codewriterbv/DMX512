package be.codewriter.dmx512.controller.serial;

import com.fazecast.jSerialComm.SerialPort;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Tool to detect USB-to-DMX controllers (as serial connection)
 */
public class DMXSerialDiscoverTool {

    private DMXSerialDiscoverTool() {
        // Hide constructor
    }

    /**
     * Get all the available serial connections
     *
     * @return list of {@link SerialConnection}
     */
    public static List<SerialConnection> getAvailablePorts() {
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
