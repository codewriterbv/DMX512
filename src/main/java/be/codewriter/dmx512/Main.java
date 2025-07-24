package be.codewriter.dmx512;

import be.codewriter.dmx512.controller.DMXController;
import be.codewriter.dmx512.controller.ip.DMXIPController;
import be.codewriter.dmx512.controller.ip.DMXIPDiscoverTool;
import be.codewriter.dmx512.controller.serial.DMXSerialController;
import be.codewriter.dmx512.controller.serial.DMXSerialDiscoverTool;
import be.codewriter.dmx512.model.DMXClient;
import be.codewriter.dmx512.model.DMXUniverse;
import be.codewriter.dmx512.ofl.OFLParser;
import be.codewriter.dmx512.ofl.model.Fixture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

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
        // runIpDemo();
        runSerialDemo();
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
        int universe = 1;

        // Send raw data
        // The PicoSpot on DMX channel 1 expects 11 values
        /*
        "Pan",
        "Tilt",
        "Pan fine",
        "Tilt fine",
        "Pan/Tilt Speed",
        "Color Wheel",
        "Gobo Wheel",
        "Dimmer",
        "Shutter / Strobe",
        "Program",
        "Program Speed"
        */
        // Set all to 0
        controller.render(universe, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        sleep(2_000);
        // Set pan and tilt to 127
        controller.render(universe, new byte[]{(byte) 127, (byte) 127, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        sleep(2_000);
        // Set pan/tilt back to 0, color wheel to 44, and dimmer full open
        controller.render(universe, new byte[]{0, 0, 0, 0, 0, (byte) 44, 0, (byte) 255, 0, 0, 0});

        sleep(5_000);

        // Use fixtures
        runMinimalFixtureExample(controller);
        sleep(5_000);

        runDemoPartySpot(controller);
        sleep(5_000);

        runDemoPicoSpotMovingHead(controller);
        sleep(5_000);

        // Close the connection to the controller
        controller.close();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Should not arrive here
        }
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

        var controller = new DMXSerialController("tty.usbserial-B003X1DH");
        LOGGER.info("Controller initialized at {} with protocol {}, connected: {}",
                controller.getAddress(), controller.getProtocolName(), controller.isConnected());

        int universe = 1;

        // Send raw data
        // The PicoSpot on DMX channel 1 expects 11 values
        /*
        "Pan",
        "Tilt",
        "Pan fine",
        "Tilt fine",
        "Pan/Tilt Speed",
        "Color Wheel",
        "Gobo Wheel",
        "Dimmer",
        "Shutter / Strobe",
        "Program",
        "Program Speed"
        */
        // Set all to 0
        controller.render(universe, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        sleep(2_000);
        // Set pan and tilt to 127
        controller.render(universe, new byte[]{(byte) 127, (byte) 127, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        sleep(2_000);
        // Set pan/tilt back to 0, color wheel to 44, and dimmer full open
        controller.render(universe, new byte[]{0, 0, 0, 0, 0, (byte) 44, 0, (byte) 255, 0, 0, 0});

        sleep(5_000);

        // Use fixtures
        runMinimalFixtureExample(controller);
        sleep(5_000);

        runDemoPartySpot(controller);
        sleep(5_000);

        runDemoPicoSpotMovingHead(controller);
        sleep(5_000);

        // Close the connection to the controller
        controller.close();
    }

    private static Fixture getFixturePartySpot() {
        return getFixture("led-party-tcl-spot.json");
    }

    private static Fixture getFixturePicoSpotMovingHead() {
        return getFixture("picospot-20-led.json");
    }

    private static Fixture getFixture(String fixtureFile) {
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream(fixtureFile)) {
            return OFLParser.parse(is);
        } catch (Exception ex) {
            LOGGER.error("Error parsing fixture: {}", ex.getMessage());
        }

        return null;
    }

    private static void runMinimalFixtureExample(DMXController controller) {
        // Load a fixture
        Fixture fixture;

        try (InputStream is = Main.class.getClassLoader().getResourceAsStream("led-party-tcl-spot.json")) {
            fixture = OFLParser.parse(is);
        } catch (Exception ex) {
            LOGGER.error("Error parsing fixture: {}", ex.getMessage());
            return;
        }

        // Create a DMX client based on the fixture and DMX channel (23 in this example)
        // The first mode of the fixture will be used in the DMXClient constructor to define the number of channels
        DMXClient client = new DMXClient(23, fixture);
        DMXUniverse universe = new DMXUniverse(1, client);

        // Set to full red
        client.setValue("red", (byte) 255);
        client.setValue("dimmer", (byte) 255);

        // Send the data to the DMX interface
        controller.render(universe);

        // Color change effect
        for (int i = 0; i <= 100; i++) {
            float ratio = i / 100.0f;
            client.setValue("red", (byte) (255 * (1 - ratio)));
            client.setValue("blue", (byte) (255 * ratio));
            controller.render(universe);
            sleep(50);
        }
    }

    private static void runDemoPartySpot(DMXController controller) {
        LOGGER.info("Running demo with controller {}", controller);
        LOGGER.info("\tConnected: {}", controller.isConnected());

        try {
            // Create some fixtures
            var fixture = getFixturePartySpot();
            DMXClient client = new DMXClient(23, fixture);
            DMXUniverse universe = new DMXUniverse(1, client);

            // Set dimmer full
            client.setValue("dimmer", (byte) 255);
            controller.render(universe);
            Thread.sleep(50);
            // Set effects
            client.setValue("effects", (byte) 255);
            controller.render(universe);
            Thread.sleep(50);

            // Set colors
            LOGGER.info("Setting RED");
            client.setValue("red", (byte) 255);
            client.setValue("green", (byte) 0);
            client.setValue("blue", (byte) 0);
            controller.render(universe);
            Thread.sleep(3000);

            LOGGER.info("Setting GREEN");
            client.setValue("red", (byte) 0);
            client.setValue("green", (byte) 255);
            client.setValue("blue", (byte) 0);
            controller.render(universe);
            Thread.sleep(3000);

            LOGGER.info("Setting BLUE");
            client.setValue("red", (byte) 0);
            client.setValue("green", (byte) 0);
            client.setValue("blue", (byte) 255);
            controller.render(universe);
            Thread.sleep(3000);

            LOGGER.info("Setting WHITE");
            client.setValue("red", (byte) 255);
            client.setValue("green", (byte) 255);
            client.setValue("blue", (byte) 255);
            controller.render(universe);
            Thread.sleep(3000);

            // Fade effect example
            LOGGER.info("Fading colors");
            client.setValue("red", (byte) 0);
            client.setValue("green", (byte) 0);
            client.setValue("blue", (byte) 0);
            controller.render(universe);
            Thread.sleep(50);

            /*
            for (int i = 0; i <= 100; i++) {
                float ratio = i / 100.0f;
                rgb1.setValue("red", (byte) (255 * (1 - ratio)));
                rgb1.setValue("blue", (byte) (255 * ratio));
                //rgb2.setValue("green", (byte) (255 * ratio));
                //rgb2.setValue("blue", (byte) (255 * (1 - ratio)));
                controller.render(clients);
                Thread.sleep(50);
            }
            */
        } catch (Exception e) {
            LOGGER.error("Error in the demo: {}", e.getMessage());
        }
    }

    private static void runDemoPicoSpotMovingHead(DMXController controller) {
        LOGGER.info("Running demo with controller {}", controller);
        LOGGER.info("\tConnected: {}", controller.isConnected());

        try {
            // Create some fixtures
            var fixture = getFixturePicoSpotMovingHead();
            var mode = fixture.getModeByName("11-channel");

            if (mode == null) {
                LOGGER.error("No mode found for fixture '{}'", fixture.name());
                return;
            }

            DMXClient client = new DMXClient(1, fixture, mode);
            DMXUniverse universe = new DMXUniverse(1, client);

            /*
            "Pan",
            "Tilt",
            "Pan fine",
            "Tilt fine",
            "Pan/Tilt Speed",
            "Color Wheel",
            "Gobo Wheel",
            "Dimmer",
            "Shutter / Strobe",
            "Program",
            "Program Speed"
            */

            // RESET
            client.setValue("pan", (byte) 0);
            client.setValue("tilt", (byte) 0);
            client.setValue("color wheel", (byte) 0);
            client.setValue("gobo wheel", (byte) 0);
            client.setValue("dimmer", (byte) 255);
            client.setValue("program", (byte) 0);
            controller.render(universe);
            Thread.sleep(3000);

            // PAN
            for (int i = 0; i < 255; i++) {
                client.setValue("pan", (byte) i);
                controller.render(universe);
                Thread.sleep(10);
            }

            // TILT
            for (int i = 0; i < 255; i++) {
                client.setValue("tilt", (byte) i);
                controller.render(universe);
                Thread.sleep(10);
            }

            // MIDDLE
            client.setValue("pan", (byte) 127);
            client.setValue("tilt", (byte) 127);
            controller.render(universe);
            Thread.sleep(1000);

            // COLOR WHEEL
            for (int i = 0; i < 255; i++) {
                client.setValue("color wheel", (byte) i);
                controller.render(universe);
                Thread.sleep(250);
            }

            // GOBO
            for (int i = 0; i < 255; i++) {
                client.setValue("gobo wheel", (byte) i);
                controller.render(universe);
                Thread.sleep(250);
            }
        } catch (Exception e) {
            LOGGER.error("Error in the demo: {}", e.getMessage());
        }
    }
}