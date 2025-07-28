package be.codewriter.dmx512.demo;

import be.codewriter.dmx512.controller.DMXController;
import be.codewriter.dmx512.model.DMXClient;
import be.codewriter.dmx512.model.DMXUniverse;
import be.codewriter.dmx512.ofl.model.Fixture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This demo assumes a LED Party Spot is:
 * - connected on universe 0
 * - configured with DMX address 23
 * <p>
 * The fixture definition gets loaded from led-party-tcl-spot.json
 */
public class RGBLEDExtendedDemo implements DmxDemo {
    private static final Logger LOGGER = LoggerFactory.getLogger(RGBLEDExtendedDemo.class.getName());

    @Override
    public void run(DMXController controller) {
        LOGGER.info("Starting extended RGB demo");

        Fixture fixture = getFixture("led-party-tcl-spot.json");
        DMXClient client = new DMXClient(23, fixture);
        DMXUniverse universe = new DMXUniverse(0, client);
        fullReset(controller, universe);

        LOGGER.info("Fixture is loaded, client and universe created");

        LOGGER.info("Resetting client: all channels to 0, except dimmer full open (255)");
        client.setValue("dimmer", (byte) 255);
        controller.render(universe);
        sleep(50);

        LOGGER.info("Setting RED");
        client.setValue("red", (byte) 255);
        client.setValue("green", (byte) 0);
        client.setValue("blue", (byte) 0);
        controller.render(universe);
        sleep(3_000);

        LOGGER.info("Setting GREEN");
        client.setValue("red", (byte) 0);
        client.setValue("green", (byte) 255);
        client.setValue("blue", (byte) 0);
        controller.render(universe);
        sleep(3_000);

        LOGGER.info("Setting BLUE");
        client.setValue("red", (byte) 0);
        client.setValue("green", (byte) 0);
        client.setValue("blue", (byte) 255);
        controller.render(universe);
        sleep(3_000);

        LOGGER.info("Setting WHITE");
        client.setValue("red", (byte) 255);
        client.setValue("green", (byte) 255);
        client.setValue("blue", (byte) 255);
        controller.render(universe);
        sleep(3_000);

        LOGGER.info("Set effects to fastest strobo");
        client.setValue("effects", (byte) 255);
        controller.render(universe);
        sleep(5_000);

        LOGGER.info("Stopping strobo effect");
        client.setValue("effects", (byte) 0);
        controller.render(universe);
        sleep(3_000);

        LOGGER.info("Fade effect of red and blue example");
        client.setValue("red", (byte) 0);
        client.setValue("green", (byte) 0);
        client.setValue("blue", (byte) 0);
        controller.render(universe);
        sleep(50);

        for (int i = 0; i <= 100; i++) {
            float ratio = i / 100.0f;
            client.setValue("red", (byte) (255 * (1 - ratio)));
            client.setValue("blue", (byte) (255 * ratio));
            controller.render(universe);
            sleep(50);
        }

        sleep(5_000);
    }
}
