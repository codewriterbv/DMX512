package be.codewriter.dmx512.demo;

import be.codewriter.dmx512.controller.DMXController;
import be.codewriter.dmx512.model.DMXClient;
import be.codewriter.dmx512.model.DMXUniverse;
import be.codewriter.dmx512.ofl.model.Fixture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This demo assumes
 * - two PicoSpots are:
 * - connected on universe 0
 * - configured with DMX address 1 and 12
 * - two LED Party Spots are:
 * - connected on universe 1
 * - configured with DMX address 23 and 28
 * <p>
 * The fixture definition gets loaded from picospot-20-led.json and led-party-tcl-spot.json
 */
public class IPTwoUniversesDemo implements DmxDemo {
    private static final Logger LOGGER = LoggerFactory.getLogger(IPTwoUniversesDemo.class.getName());

    @Override
    public void run(DMXController controller) {
        LOGGER.info("Starting demo with four fixtures on two universes");

        Fixture movingHead = getFixture("picospot-20-led.json");
        var movingHeadMode = movingHead.getModeByName("11-channel");
        Fixture rgb = getFixture("led-party-tcl-spot.json");

        if (movingHeadMode == null) {
            LOGGER.error("No mode found for fixture '{}'", movingHead.name());
            return;
        }

        DMXClient movingHead1 = new DMXClient(1, movingHead, movingHeadMode);
        DMXClient movingHead2 = new DMXClient(12, movingHead, movingHeadMode);
        DMXUniverse universe1 = new DMXUniverse(0, List.of(movingHead1, movingHead2));
        fullReset(controller, universe1);

        DMXClient rgb1 = new DMXClient(23, rgb);
        DMXClient rgb2 = new DMXClient(28, rgb);
        DMXUniverse universe2 = new DMXUniverse(1, List.of(rgb1, rgb2));
        fullReset(controller, universe2);

        LOGGER.info("Fixtures are loaded, clients and universes created");

        // Dimmer full open
        LOGGER.info("Dimmer full open on all fixtures");
        movingHead1.setValue("dimmer", (byte) 255);
        movingHead2.setValue("dimmer", (byte) 255);
        controller.render(universe1);

        rgb1.setValue("dimmer", (byte) 255);
        rgb2.setValue("dimmer", (byte) 255);
        controller.render(universe2);
        sleep(1_000);

        LOGGER.info("Set moving heads to center position");
        movingHead1.setValue("pan", (byte) 127);
        movingHead1.setValue("tilt", (byte) 127);
        movingHead2.setValue("pan", (byte) 127);
        movingHead2.setValue("tilt", (byte) 127);
        controller.render(universe1);
        sleep(1_000);

        LOGGER.info("Moving heads green and red");
        movingHead1.setValue("color wheel", (byte) 44);
        movingHead2.setValue("color wheel", (byte) 11);
        controller.render(universe1);
        sleep(1_000);

        LOGGER.info("RGBs green and red");
        rgb1.setValue("green", (byte) 255);
        rgb2.setValue("red", (byte) 255);
        controller.render(universe2);

        sleep(5_000);
    }
}
