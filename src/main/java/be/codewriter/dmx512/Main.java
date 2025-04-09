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
        demoSerial();
        demoIp();
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
            DMXSerialController controller = new DMXSerialController();
            List<DMXClient> clients = new ArrayList<>();

            // List available ports
            LOGGER.info("Available ports:");
            for (var port : controller.getAvailablePorts()) {
                LOGGER.info("\t{}", port);
            }

            // Connect to the DMX interface
            // On Windows, use something like "COM3"
            // On Linux, use something like "/dev/ttyUSB0"
            if (controller.connect("/dev/ttyUSB0")) {
                LOGGER.info("Connected to DMX interface");

                // Create some fixtures
                var fixture = getFixture();
                DMXClient rgb1 = new DMXClient(fixture, fixture.modes().get(0), 0);
                DMXClient rgb2 = new DMXClient(fixture, fixture.modes().get(0), 5);
                clients.addAll(List.of(rgb1, rgb2));

                // Set colors
                rgb1.setValue("red", 255);
                rgb2.setValue("blue", 255);

                // Send the data to the DMX interface
                controller.render(clients);

                // Fade effect example
                for (int i = 0; i <= 100; i++) {
                    float ratio = i / 100.0f;
                    rgb1.setValue("red", (int) (255 * (1 - ratio)));
                    rgb1.setValue("blue", (int) (255 * ratio));
                    rgb2.setValue("green", (int) (255 * ratio));
                    rgb2.setValue("blue", (int) (255 * (1 - ratio)));
                    controller.render(clients);
                    Thread.sleep(50);
                }

                controller.close();
            } else {
                LOGGER.error("Failed to connect to DMX interface");
            }
        } catch (Exception ex) {
            LOGGER.error("Error in the serial demo: {}", ex.getMessage());
        }
    }

    private static void demoIp() {
        DMXController controller = new DMXIPController();
        controller.connect("192.168.1.100");  // IP address of your Art-Net node
    }
}