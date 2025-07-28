package be.codewriter.dmx512;

import be.codewriter.dmx512.controller.DMXController;
import be.codewriter.dmx512.controller.ip.DMXIPController;
import be.codewriter.dmx512.controller.ip.DMXIPDiscoverTool;
import be.codewriter.dmx512.controller.serial.DMXSerialController;
import be.codewriter.dmx512.controller.serial.DMXSerialDiscoverTool;
import be.codewriter.dmx512.controller.serial.SerialProtocol;
import be.codewriter.dmx512.demo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application to demo and test the DMX512 library
 */
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getName());

    private Main() {
        // Hide constructor
    }

    /**
     * Run the demo/test application
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Run all demos
        runIpDemo();
        //runSerialDemo();

        // Run demo with two universes
        (new IPTwoUniversesDemo()).run(new DMXIPController(DMXIPDiscoverTool.discoverDevices().getFirst().address()));
    }

    private static void runIpDemo() {
        var devices = DMXIPDiscoverTool.discoverDevices();

        if (devices.isEmpty()) {
            LOGGER.error("No IP DMX controllers found");
            return;
        }

        for (var device : devices) {
            LOGGER.info("Found IP DMX controller {} at address:{}",
                    device.getName(), device.getAddress());
        }

        var controller = new DMXIPController(devices.getFirst().address());

        // Run the demos
        runAllDemos(controller);

        // Close the connection to the controller
        controller.close();
    }

    private static void runSerialDemo() {
        var ports = DMXSerialDiscoverTool.getAvailablePorts();
        if (ports.isEmpty()) {
            LOGGER.error("No serial ports found");
            return;
        }

        for (var port : ports) {
            LOGGER.info("Found serial ports {} ({}) at path:{}",
                    port.getName(), port.getDescription(), port.getPath());
        }

        // "tty.usbserial-B003X1DH" // tty.usbserial-BG01OL60 // B003X1DH
        var controller = new DMXSerialController("tty.usbserial-BG01OL60", SerialProtocol.OPEN_DMX_USB);
        LOGGER.info("Controller initialized at {} with protocol {}, connected: {}",
                controller.getAddress(), controller.getProtocolName(), controller.isConnected());

        // Run the demos
        runAllDemos(controller);

        // Close the connection to the controller
        controller.close();
    }

    private static void runAllDemos(DMXController controller) {
        LOGGER.info("***********");
        (new RawBytesDemo()).run(controller);
        LOGGER.info("***********");
        (new RGBLEDSimpleDemo()).run(controller);
        LOGGER.info("***********");
        (new RGBLEDExtendedDemo()).run(controller);
        LOGGER.info("***********");
        (new MovingHeadDemo()).run(controller);
        LOGGER.info("***********");
    }
}