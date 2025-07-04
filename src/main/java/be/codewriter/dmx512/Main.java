package be.codewriter.dmx512;

import be.codewriter.dmx512.client.DMXClient;
import be.codewriter.dmx512.controller.DMXController;
import be.codewriter.dmx512.controller.ip.DMXIPController;
import be.codewriter.dmx512.controller.serial.DMXSerialController;
import be.codewriter.dmx512.ofl.OpenFormatLibraryParser;
import be.codewriter.dmx512.ofl.model.Fixture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getName());

    public static void main(String[] args) {
        var ipController = setupIpControllers();
        //runDemoPartySpot(ipController);
        runDemoPicoSpotMovingHead(ipController);
        ipController.close();

        //var serialController = setupSerialController();
        //runDemoPartySpot(ipController);
        //runDemoPartySpot(serialController);
        //serialController.close();
    }

    private static Fixture getFixturePartySpot() {
        return getFixture("led-party-tcl-spot.json");
    }

    private static Fixture getFixturePicoSpotMovingHead() {
        return getFixture("picospot-20-led.json");
    }

    private static Fixture getFixture(String fixtureFile) {
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream(fixtureFile)) {
            return OpenFormatLibraryParser.parseFixture(is);
        } catch (Exception ex) {
            LOGGER.error("Error parsing fixture: {}", ex.getMessage());
        }

        return null;
    }

    private static DMXSerialController setupSerialController() {
        try {
            var controller = new DMXSerialController();

            // List available ports
            LOGGER.info("Available serial ports:");
            for (var port : controller.getAvailablePorts()) {
                LOGGER.info("\t{}", port);
            }

            // Connect to the DMX interface
            // On Windows, use something like "COM3"
            // On Linux, use something like "/dev/ttyUSB0"
            if (controller.connect("tty.usbserial-B003X1DH")) {
                LOGGER.info("Connected to DMX interface");

                return controller;
            } else {
                LOGGER.error("Failed to connect to DMX interface");
            }
        } catch (Exception ex) {
            LOGGER.error("Error in the serial demo: {}", ex.getMessage());
        }

        return null;
    }

    private static DMXIPController setupIpControllers() {
        var controller = new DMXIPController();

        LOGGER.info("Available IP ports:");
        for (var device : controller.discoverDevices()) {
            LOGGER.info("\t{}", device);
            controller.connect(device.ipAddress()); // IP address of your Art-Net node
        }

        return controller;
    }

    private static void runDemoPartySpot(DMXController controller) {
        LOGGER.info("Running demo with controller {}", controller);
        LOGGER.info("\tConnected: {}", controller.isConnected());

        try {
            List<DMXClient> clients = new ArrayList<>();

            // Create some fixtures
            var fixture = getFixturePartySpot();
            DMXClient rgb = new DMXClient(fixture, fixture.modes().getFirst(), 1);
            clients.add(rgb);

            // Set dimmer full
            rgb.setValue("dimmer", (byte) 255);
            controller.render(clients);
            Thread.sleep(50);
            // Set effects
            rgb.setValue("effects", (byte) 255);
            controller.render(clients);
            Thread.sleep(50);

            // Set colors
            LOGGER.info("Setting RED");
            rgb.setValue("red", (byte) 255);
            rgb.setValue("green", (byte) 0);
            rgb.setValue("blue", (byte) 0);
            controller.render(clients);
            Thread.sleep(3000);

            LOGGER.info("Setting GREEN");
            rgb.setValue("red", (byte) 0);
            rgb.setValue("green", (byte) 255);
            rgb.setValue("blue", (byte) 0);
            controller.render(clients);
            Thread.sleep(3000);

            LOGGER.info("Setting BLUE");
            rgb.setValue("red", (byte) 0);
            rgb.setValue("green", (byte) 0);
            rgb.setValue("blue", (byte) 255);
            controller.render(clients);
            Thread.sleep(3000);

            LOGGER.info("Setting WHITE");
            rgb.setValue("red", (byte) 255);
            rgb.setValue("green", (byte) 255);
            rgb.setValue("blue", (byte) 255);
            controller.render(clients);
            Thread.sleep(3000);

            // Fade effect example
            LOGGER.info("Fading colors");
            rgb.setValue("red", (byte) 0);
            rgb.setValue("green", (byte) 0);
            rgb.setValue("blue", (byte) 0);
            controller.render(clients);
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
            List<DMXClient> clients = new ArrayList<>();

            // Create some fixtures
            var fixture = getFixturePicoSpotMovingHead();
            var mode = fixture.getModeByName("11-channel");

            if (mode == null) {
                LOGGER.error("No mode found for fixture '{}'", fixture.name());
                return;
            }

            DMXClient client = new DMXClient(fixture, mode, 1);
            clients.add(client);

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
            controller.render(clients);
            Thread.sleep(3000);

            // PAN
            for (int i = 0; i < 255; i++) {
                client.setValue("pan", (byte) i);
                controller.render(clients);
                Thread.sleep(10);
            }

            // TILT
            for (int i = 0; i < 255; i++) {
                client.setValue("tilt", (byte) i);
                controller.render(clients);
                Thread.sleep(10);
            }

            // MIDDLE
            client.setValue("pan", (byte) 127);
            client.setValue("tilt", (byte) 127);
            controller.render(clients);
            Thread.sleep(1000);

            // TILT
            for (int i = 0; i < 255; i++) {
                client.setValue("color wheel", (byte) i);
                controller.render(clients);
                Thread.sleep(250);
            }

            // GOBO
            for (int i = 0; i < 255; i++) {
                client.setValue("gobo wheel", (byte) i);
                controller.render(clients);
                Thread.sleep(250);
            }
        } catch (Exception e) {
            LOGGER.error("Error in the demo: {}", e.getMessage());
        }
    }
}