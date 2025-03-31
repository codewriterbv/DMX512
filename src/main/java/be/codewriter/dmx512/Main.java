package be.codewriter.dmx512;

import be.codewriter.dmx512.controller.DMXController;
import be.codewriter.dmx512.controller.DMXIPController;
import be.codewriter.dmx512.controller.DMXSerialController;
import be.codewriter.dmx512.fixture.RGBFixture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getName());

    public static void main(String[] args) {
        demoSerial();
        demoIp();
    }

    private static void demoSerial() {
        DMXSerialController controller = new DMXSerialController();

        // List available ports
        LOGGER.info("Available ports:");
        for (String port : controller.listAvailablePorts()) {
            LOGGER.info("\t{}", port);
        }

        // Connect to the DMX interface
        // On Windows, use something like "COM3"
        // On Linux, use something like "/dev/ttyUSB0"
        if (controller.connect("/dev/ttyUSB0")) {
            LOGGER.info("Connected to DMX interface");

            try {
                // Create some fixtures
                RGBFixture light1 = new RGBFixture(controller, 1);
                RGBFixture light2 = new RGBFixture(controller, 4);

                // Set colors
                light1.setColor(255, 0, 0);  // Red
                light2.setColor(0, 0, 255);  // Blue

                // Send the data to the DMX interface
                controller.render();

                // Fade effect example
                for (int i = 0; i <= 100; i++) {
                    float ratio = i / 100.0f;
                    light1.setColor((int) (255 * (1 - ratio)), 0, (int) (255 * ratio));
                    light2.setColor((int) (255 * ratio), 0, (int) (255 * (1 - ratio)));
                    controller.render();
                    Thread.sleep(50);
                }

            } catch (Exception e) {
                LOGGER.error("Error sending DMX data: {}", e.getMessage());
            } finally {
                // Always close the connection
                controller.close();
            }
        } else {
            LOGGER.error("Failed to connect to DMX interface");
        }
    }

    private static void demoIp() {
        DMXController controller = new DMXIPController();
        controller.connect("192.168.1.100");  // IP address of your Art-Net node
    }
}