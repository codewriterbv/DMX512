package be.codewriter.dmx512.model;

import be.codewriter.dmx512.ofl.model.Fixture;
import be.codewriter.dmx512.ofl.model.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DMX client is a light fixture, smoke machine, or other device in a DMX chain.
 */
public class DMXClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(DMXClient.class.getName());

    private final Fixture fixture;
    private final Mode selectedMode;
    private final int address;
    private final byte[] values;

    /**
     * Construct a DMX client at an address with the number of channel values.
     *
     * @param address          the address, min 1, max 255
     * @param numberOfChannels the number of channels used by this client
     */
    public DMXClient(int address, int numberOfChannels) {
        if (address < 1 || address > 255) {
            throw new IllegalArgumentException("Invalid address: " + address);
        }
        this.fixture = null;
        this.selectedMode = null;
        this.address = address;
        this.values = new byte[numberOfChannels];
    }

    /**
     * Construct a DMX client at an address with a fixture definition.
     * The number of channels will be defined by the first mode in the fixture.
     *
     * @param address the address, min 1, max 255
     * @param fixture the fixture
     */
    public DMXClient(int address, Fixture fixture) {
        if (address < 1 || address > 255) {
            throw new IllegalArgumentException("Invalid address: " + address);
        }
        if (fixture == null || fixture.modes() == null || fixture.modes().isEmpty()) {
            throw new IllegalArgumentException("Fixture has no modes, so can't define the number of values must be defined");
        }
        this.fixture = fixture;
        this.selectedMode = fixture.modes().getFirst();
        this.address = address;
        this.values = new byte[selectedMode.channels().size()];
    }

    /**
     * Construct a DMX client at an address with a fixture definition
     *
     * @param address      the address, min 1, max 255
     * @param fixture      the fixture
     * @param selectedMode the selected mode
     */
    public DMXClient(int address, Fixture fixture, Mode selectedMode) {
        if (address < 1 || address > 255) {
            throw new IllegalArgumentException("Invalid address: " + address);
        }
        if (fixture == null) {
            throw new IllegalArgumentException("Fixture must be defined");
        }
        if (selectedMode == null) {
            throw new IllegalArgumentException("Fixture mode must be defined");
        }
        this.fixture = fixture;
        this.selectedMode = selectedMode;
        this.address = address;
        this.values = new byte[selectedMode.channels().size()];
    }

    public void reset() {
        for (int i = 0; i < values.length; i++) {
            values[i] = 0;
        }
    }

    /**
     * Get the fixture
     *
     * @return the fixture
     */
    public Fixture getFixture() {
        return fixture;
    }

    /**
     * Get the selected mode
     *
     * @return the mode
     */
    public Mode getSelectedMode() {
        return selectedMode;
    }

    /**
     * Get the address
     *
     * @return the DMX address
     */
    public int getAddress() {
        return address;
    }

    /**
     * Check if a channel with the given name exists in the selected mode.
     *
     * @param key name of the channel
     * @return channel with the given name exists
     */
    public boolean hasChannel(String key) {
        if (selectedMode == null) {
            LOGGER.error("No mode defined, no channels defined by name");
            return false;
        }
        return selectedMode.getChannelIndex(key) >= 0;
    }

    /**
     * Change the value in the given channel
     *
     * @param idx   index of the channel
     * @param value the new value
     */
    public void setValue(int idx, byte value) {
        if (idx < 0 || idx >= values.length) {
            throw new IllegalArgumentException("The given index is outside of the available range " + idx + "/" + values.length);
        }
        values[idx] = value;
    }

    /**
     * Change the value for the given channel by name
     *
     * @param key   the name of the channel
     * @param value the new value
     */
    public void setValue(String key, byte value) {
        if (selectedMode == null) {
            LOGGER.error("No mode defined, can't set the value");
            return;
        }
        var idx = selectedMode.getChannelIndex(key);
        if (idx == -1) {
            LOGGER.error("Can't find the channel index for key '{}'", key);
            return;
        }
        setValue(idx, value);
    }

    /**
     * Get the value at the given index
     *
     * @param idx index of the channel
     * @return the value for the given index
     */
    public byte getValue(int idx) {
        if (idx < 0 || idx >= values.length) {
            throw new IllegalArgumentException("The given index is outside of the available range " + idx + "/" + values.length);
        }
        return values[idx];
    }

    /**
     * Get the value for the given channel name
     *
     * @param key the name of the channel as defined in the selected mode
     * @return the value for the given channel by name
     */
    public byte getValue(String key) {
        if (selectedMode == null) {
            LOGGER.error("No mode defined, returning value 0");
            return 0;
        }
        var idx = selectedMode.getChannelIndex(key);
        if (idx == -1) {
            LOGGER.error("Can't find the channel index for key '{}', will return value 0", key);
            return 0;
        }
        return getValue(idx);
    }

    /**
     * The length of the DMX data packet of this client
     *
     * @return length
     */
    public int getDataLength() {
        return values.length;
    }

    /**
     * The DMX data packet of this client
     *
     * @return byte array
     */
    public byte[] getData() {
        return values;
    }
}
