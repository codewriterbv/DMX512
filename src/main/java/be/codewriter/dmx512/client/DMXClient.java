package be.codewriter.dmx512.client;

import be.codewriter.dmx512.ofl.model.Fixture;
import be.codewriter.dmx512.ofl.model.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DMXClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(DMXClient.class.getName());

    private final Fixture fixture;
    private final Mode selectedMode;
    private final int startChannel;
    private final byte[] values;

    public DMXClient(Fixture fixture, Mode selectedMode, int startChannel) {
        if (startChannel < 0 || startChannel > 255) {
            throw new IllegalArgumentException("Value must be between 0 and 255");
        }
        this.fixture = fixture;
        this.selectedMode = selectedMode;
        this.startChannel = startChannel;
        this.values = new byte[fixture.availableChannels().size()];
    }

    public Fixture getFixture() {
        return fixture;
    }

    public Mode getSelectedMode() {
        return selectedMode;
    }

    public int getStartChannel() {
        return startChannel;
    }

    public boolean hasChannel(String key) {
        return fixture.getChannelIndex(key) >= 0;
    }

    public void setValue(int idx, byte value) {
        values[idx] = value;
    }

    public void setValue(String key, byte value) {
        var idx = fixture.getChannelIndex(key);
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
        var idx = fixture.getChannelIndex(key);
        if (idx == -1) {
            LOGGER.error("Can't find the channel index for key '{}', will return value 0", key);
            return 0;
        }
        return getValue(idx);
    }
}
