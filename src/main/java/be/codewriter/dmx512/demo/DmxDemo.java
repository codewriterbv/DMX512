package be.codewriter.dmx512.demo;

import be.codewriter.dmx512.Main;
import be.codewriter.dmx512.controller.DMXController;
import be.codewriter.dmx512.model.DMXUniverse;
import be.codewriter.dmx512.ofl.OFLParser;
import be.codewriter.dmx512.ofl.model.Fixture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public interface DmxDemo {
    Logger LOGGER = LoggerFactory.getLogger(DmxDemo.class.getName());

    void run(DMXController controller);

    default void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Should not arrive here
        }
    }

    default Fixture getFixture(String fixtureFile) {
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream(fixtureFile)) {
            return OFLParser.parse(is);
        } catch (Exception ex) {
            LOGGER.error("Error parsing fixture: {}", ex.getMessage());
        }

        return null;
    }

    default void fullReset(DMXController controller, DMXUniverse universe) {
        fullReset(controller, universe.getId());
    }

    default void fullReset(DMXController controller, int universeId) {
        LOGGER.info("Reset all data in universe {} to remove any result from a previous test", universeId);
        controller.render(universeId, new byte[512]);
        sleep(1_000);
    }
}
