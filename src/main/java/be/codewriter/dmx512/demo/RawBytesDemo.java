package be.codewriter.dmx512.demo;

import be.codewriter.dmx512.controller.DMXController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This demo assumes a PicoSpot is:
 * - connected on universe 0
 * - configured with DMX address 1
 * - configured for 11 channels
 * <p>
 * 11 channels mode expects:
 * - "Pan"
 * - "Tilt"
 * - "Pan fine"
 * - "Tilt fine"
 * - "Pan/Tilt Speed"
 * - "Color Wheel"
 * - "Gobo Wheel"
 * - "Dimmer"
 * - "Shutter / Strobe"
 * - "Program"
 * - "Program Speed"
 */
public class RawBytesDemo implements DmxDemo {
    Logger LOGGER = LoggerFactory.getLogger(RawBytesDemo.class.getName());

    @Override
    public void run(DMXController controller) {
        LOGGER.info("Starting raw byte demo");

        int universeId = 0;
        fullReset(controller, universeId);

        LOGGER.info("Set all to 0");
        controller.render(universeId, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        sleep(2_000);

        LOGGER.info("Set pan and tilt to 127");
        controller.render(universeId, new byte[]{(byte) 127, (byte) 127, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        sleep(2_000);

        LOGGER.info("Set pan/tilt to 0, color wheel to 44, and dimmer full open");
        controller.render(universeId, new byte[]{0, 0, 0, 0, 0, (byte) 44, 0, (byte) 255, 0, 0, 0});

        sleep(5_000);
    }
}
