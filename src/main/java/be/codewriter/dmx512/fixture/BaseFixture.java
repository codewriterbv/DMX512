package be.codewriter.dmx512.fixture;

import be.codewriter.dmx512.controller.DMXController;
import be.codewriter.dmx512.controller.DMXIPController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a generic DMX light fixture
 */
public class BaseFixture {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseFixture.class.getName());

    private final DMXController controller;
    private final int startChannel;
    private final int channelCount;
    private final int[] values;

    /**
     * Create a new DMX fixture
     *
     * @param controller   The DMX controller
     * @param startChannel The first DMX channel used by this fixture
     * @param channelCount The number of channels used by this fixture
     */
    public BaseFixture(DMXController controller, int startChannel, int channelCount) {
        this.controller = controller;
        this.startChannel = startChannel;
        this.channelCount = channelCount;
        this.values = new int[channelCount];
    }

    /**
     * Set a channel value relative to this fixture's start channel
     *
     * @param relativeChannel Channel offset (0 for first channel)
     * @param value           DMX value (0-255)
     */
    public void setChannel(int relativeChannel, int value) {
        if (relativeChannel < 0 || relativeChannel >= channelCount) {
            throw new IllegalArgumentException("Channel offset out of range");
        }
        values[relativeChannel] = value;
        if (controller == null) {
            LOGGER.error("Controller is null");
            return;
        }
        controller.setChannel(startChannel + relativeChannel, value);
    }

    /**
     * Get the start channel for this fixture
     */
    public int getStartChannel() {
        return startChannel;
    }

    /**
     * Get the number of channels used by this fixture
     */
    public int getChannelCount() {
        return channelCount;
    }

    /**
     * Get the last set value relative to this fixture's start channel
     *
     * @param relativeChannel Channel offset (0 for first channel)
     */
    public int getValue(int relativeChannel) {
        return values[relativeChannel];
    }
}

