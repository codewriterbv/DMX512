package be.codewriter.dmx512.fixture;

import be.codewriter.dmx512.controller.DMXController;

/**
 * Represents an RGB DMX light fixture
 */
public class RGBFixture extends BaseFixture {
    private static final int RED_OFFSET = 0;
    private static final int GREEN_OFFSET = 1;
    private static final int BLUE_OFFSET = 2;

    /**
     * Create a new RGB fixture
     *
     * @param controller   The DMX controller
     * @param startChannel The first DMX channel used by this fixture
     */
    public RGBFixture(DMXController controller, int startChannel) {
        super(controller, startChannel, 3); // RGB fixtures typically use 3 channels
    }

    /**
     * Set the RGB color
     *
     * @param red   Red value (0-255)
     * @param green Green value (0-255)
     * @param blue  Blue value (0-255)
     */
    public void setColor(int red, int green, int blue) {
        setChannel(RED_OFFSET, red);
        setChannel(GREEN_OFFSET, green);
        setChannel(BLUE_OFFSET, blue);
    }

    /**
     * Set the RGB color using a single integer
     *
     * @param rgb RGB color in format 0xRRGGBB
     */
    public void setColor(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        setColor(red, green, blue);
    }
}

