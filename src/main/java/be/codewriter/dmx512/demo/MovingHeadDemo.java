package be.codewriter.dmx512.demo;

import be.codewriter.dmx512.controller.DMXController;
import be.codewriter.dmx512.model.DMXClient;
import be.codewriter.dmx512.model.DMXUniverse;
import be.codewriter.dmx512.ofl.model.Fixture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This demo assumes a PicoSpot is:
 * - connected on universe 0
 * - configured with DMX address 1
 * <p>
 * The fixture definition gets loaded from picospot-20-led.json
 */
public class MovingHeadDemo implements DmxDemo {
    private static final Logger LOGGER = LoggerFactory.getLogger(MovingHeadDemo.class.getName());

    @Override
    public void run(DMXController controller) {
        LOGGER.info("Starting moving head demo");

        Fixture fixture = getFixture("picospot-20-led.json");
        var mode = fixture.getModeByName("11-channel");

        if (mode == null) {
            LOGGER.error("No mode found for fixture '{}'", fixture.name());
            return;
        }

        DMXClient client = new DMXClient(1, fixture, mode);
        DMXUniverse universe = new DMXUniverse(0, client);
        fullReset(controller, universe);

        LOGGER.info("Fixture is loaded, client and universe created");

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

        LOGGER.info("Resetting client: all channels to 0, expect dimmer full open (255)");
        client.reset();
        client.setValue("dimmer", (byte) 255);
        controller.render(universe);
        sleep(3_000);

        // PAN
        LOGGER.info("Pan full range");
        for (int i = 0; i < 255; i++) {
            client.setValue("pan", (byte) i);
            controller.render(universe);
            sleep(10);
        }

        // TILT
        LOGGER.info("Tilt full range");
        for (int i = 0; i < 255; i++) {
            client.setValue("tilt", (byte) i);
            controller.render(universe);
            sleep(10);
        }

        // MIDDLE
        LOGGER.info("Pand and tilt to middle position");
        client.setValue("pan", (byte) 127);
        client.setValue("tilt", (byte) 127);
        controller.render(universe);
        sleep(1_000);

        // COLOR WHEEL
        LOGGER.info("Color wheel full range");
        for (int i = 0; i < 255; i++) {
            client.setValue("color wheel", (byte) i);
            controller.render(universe);
            sleep(25);
        }

        // GOBO
        LOGGER.info("Gobo wheel full range with green");
        client.setValue("color wheel", (byte) 44);
        for (int i = 0; i < 255; i++) {
            client.setValue("gobo wheel", (byte) i);
            controller.render(universe);
            sleep(25);
        }

        sleep(5_000);
    }
}
