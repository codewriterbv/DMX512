package be.codewriter.dmx512.ofl.model;

import java.util.List;

public record Mode(
        String name,
        String shortName,
        List<String> channels
) {
    public int getChannelIndex(String key) {
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
