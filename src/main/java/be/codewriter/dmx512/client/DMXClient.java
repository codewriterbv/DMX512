package be.codewriter.dmx512.client;

import be.codewriter.dmx512.ofl.model.Fixture;
import be.codewriter.dmx512.ofl.model.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DMXClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(DMXClient.class.getName());

    private final Fixture fixture;
    private final Mode selectedMode;
    private final int address;
    private final byte[] values;

    public DMXClient(int numberOfChannels, int address) {
        if (address < 1 || address > 255) {
            throw new IllegalArgumentException("Invalid address: " + address);
        }
        this.fixture = null;
        this.selectedMode = null;
        this.address = address;
        this.values = new byte[numberOfChannels];
    }

    public DMXClient(Fixture fixture, Mode selectedMode, int address) {
        if (address < 1 || address > 255) {
            throw new IllegalArgumentException("Invalid address: " + address);
        }
        this.fixture = fixture;
        this.selectedMode = selectedMode;
        this.address = address;
        this.values = new byte[selectedMode.channels().size()];
    }

    public Fixture getFixture() {
        return fixture;
    }

    public Mode getSelectedMode() {
        return selectedMode;
    }

    public int getAddress() {
        return address;
    }

    public boolean hasChannel(String key) {
        if (selectedMode == null) {
            LOGGER.error("No mode defined, no channels defined by name");
            return false;
        }
        return selectedMode.getChannelIndex(key) >= 0;
    }

    public void setValue(int idx, byte value) {
        values[idx] = value;
    }

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

    public byte getValue(int idx) {
        return values[idx];
    }

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

    public int getDataLength() {
        return values.length;
    }

    public byte[] getData() {
        return values;
    }
}
