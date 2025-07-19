package be.codewriter.dmx512.ofl.model;

import java.util.List;

/**
 * OFL definition of a mode
 *
 * @param name      name
 * @param shortName short name
 * @param channels  list of channels
 */
public record Mode(
        String name,
        String shortName,
        List<String> channels
) {
    /**
     * Get the channel index for the given key (case independant)
     *
     * @param key key (name) of the channel
     * @return index or -1 if not found
     */
    public int getChannelIndex(String key) {
        if (channels == null || channels.isEmpty()) {
            return -1;
        }
        var counter = 0;
        for (var channel : channels) {
            if (clean(channel).equalsIgnoreCase(clean(key))) {
                return counter;
            }
            counter++;
        }
        return -1;
    }

    private String clean(String s) {
        return s.trim().toLowerCase().replace("/", "").replace(" ", "");
    }
}
