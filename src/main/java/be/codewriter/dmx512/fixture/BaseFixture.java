package be.codewriter.dmx512.fixture;

import be.codewriter.dmx512.controller.DMXController;

/**
 * Represents a generic DMX light fixture
 */
public class BaseFixture {
    private final DMXController controller;
    private final int startChannel;
    private final int channelCount;

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
}

