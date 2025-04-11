package be.codewriter.dmx512.ofl.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.List;

// Root fixture record
public record Fixture(
        String name,
        List<String> categories,
        Meta meta,
        Links links,
        Physical physical,
        @JsonProperty("wheels")
        LinkedHashMap<String, Wheel> wheels,
        @JsonProperty("availableChannels")
        LinkedHashMap<String, Channel> availableChannels,
        List<Mode> modes
) {
    public int getChannelIndex(String key) {
        var counter = 0;
        for (var entry : availableChannels.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key.trim())) {
                return counter;
            }
            counter++;
        }
        return -1;
    }

    public Mode getMode(String name) {
        return modes.stream()
                .filter(mode -> mode.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}