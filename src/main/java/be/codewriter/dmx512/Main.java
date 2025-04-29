package be.codewriter.dmx512;

import be.codewriter.dmx512.client.DMXClient;
import be.codewriter.dmx512.controller.DMXController;
import be.codewriter.dmx512.controller.DMXIPController;
import be.codewriter.dmx512.controller.DMXSerialController;
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
        demoIp();
        demoSerial();
    }

    private static Fixture getFixture() {
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream("led-party-tcl-spot.json")) {
            return OpenFormatLibraryParser.parseFixture(is);
        } catch (Exception ex) {
            LOGGER.error("Error parsing fixture: {}", ex.getMessage());
        }

        return null;
    }

    private static void demoSerial() {
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
            if (controller.connect("/dev/ttyUSB0")) {
                LOGGER.info("Connected to DMX interface");

                runDemo(controller);

                controller.close();
            } else {
                LOGGER.error("Failed to connect to DMX interface");
            }
        } catch (Exception ex) {
            LOGGER.error("Error in the serial demo: {}", ex.getMessage());
        }
    }

    private static void demoIp() {
        var controller = new DMXIPController();

        LOGGER.info("Available IP ports:");
        for (var port : controller.discoverDevices()) {
            LOGGER.info("\t{}", port);
        }

        controller.connect("172.16.1.145"); // IP address of your Art-Net node
        runDemo(controller);
    }

    private static void runDemo(DMXController controller) {
        LOGGER.info("Running demo with controller {}", controller);
        LOGGER.info("\tConnected: {}", controller.isConnected());

        try {
            List<DMXClient> clients = new ArrayList<>();

            // Create some fixtures
            var fixture = getFixture();
            DMXClient rgb1 = new DMXClient(fixture, fixture.modes().getFirst(), 1);
            DMXClient rgb2 = new DMXClient(fixture, fixture.modes().getFirst(), 6);
            clients.addAll(List.of(rgb1, rgb2));

            // Set dimmer full
            rgb1.setValue("dimmer", (byte) 255);
            controller.render(clients);
            Thread.sleep(50);

            // Set colors
            LOGGER.info("Setting RED");
            rgb1.setValue("red", (byte) 255);
            rgb1.setValue("green", (byte) 0);
            rgb1.setValue("blue", (byte) 0);
            controller.render(clients);
            Thread.sleep(3000);

            LOGGER.info("Setting GREEN");
            rgb1.setValue("red", (byte) 0);
            rgb1.setValue("green", (byte) 255);
            rgb1.setValue("blue", (byte) 0);
            controller.render(clients);
            Thread.sleep(3000);

            LOGGER.info("Setting BLUE");
            rgb1.setValue("red", (byte) 0);
            rgb1.setValue("green", (byte) 0);
            rgb1.setValue("blue", (byte) 255);
            controller.render(clients);
            Thread.sleep(3000);

            LOGGER.info("Setting WHITE");
            rgb1.setValue("red", (byte) 255);
            rgb1.setValue("green", (byte) 255);
            rgb1.setValue("blue", (byte) 255);
            controller.render(clients);
            Thread.sleep(3000);

            // Fade effect example
            LOGGER.info("Fading colors");
            rgb1.setValue("red", (byte) 0);
            rgb1.setValue("green", (byte) 0);
            rgb1.setValue("blue", (byte) 0);
            controller.render(clients);
            Thread.sleep(50);

            for (int i = 0; i <= 100; i++) {
                float ratio = i / 100.0f;
                rgb1.setValue("red", (byte) (255 * (1 - ratio)));
                rgb1.setValue("blue", (byte) (255 * ratio));
                //rgb2.setValue("green", (byte) (255 * ratio));
                //rgb2.setValue("blue", (byte) (255 * (1 - ratio)));
                controller.render(clients);
                Thread.sleep(50);
            }
        } catch (Exception e) {
            LOGGER.error("Error in the demo: {}", e.getMessage());
        }
    }
}