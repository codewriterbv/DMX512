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
public class RGBLEDSimpleDemo implements DmxDemo {
    private static final Logger LOGGER = LoggerFactory.getLogger(RGBLEDSimpleDemo.class.getName());

    @Override
    public void run(DMXController controller) {
        LOGGER.info("Starting simple RGB demo");

        Fixture fixture = getFixture("led-party-tcl-spot.json");
        DMXClient client = new DMXClient(23, fixture);
        DMXUniverse universe = new DMXUniverse(0, client);
        fullReset(controller, universe);

        LOGGER.info("Fixture is loaded, client and universe created");

        LOGGER.info("Resetting client: all channels to 0, except dimmer full open (255)");
        client.reset();
        client.setValue("dimmer", (byte) 255);
        controller.render(universe);
        sleep(1_000);

        LOGGER.info("Setting RED");
        client.setValue("red", (byte) 255);
        controller.render(universe);
        sleep(3_000);

        LOGGER.info("Fade RED down");
        for (int i = 255; i >= 0; i--) {
            client.setValue("red", (byte) i);
            controller.render(universe);
            sleep(25);
        }

        sleep(5_000);
    }
}
