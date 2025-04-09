package be.codewriter.dmx512.ofl.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Root fixture record
public record Fixture(
        String name,
        List<String> categories,
        Meta meta,
        Links links,
        Physical physical,
        @JsonProperty("wheels")
        Map<String, Wheel> wheels,
        @JsonProperty("availableChannels")
        Map<String, Channel> availableChannels,
        List<Mode> modes
) {
    public int getChannelIndex(String key) {
        return new ArrayList<>(availableChannels.keySet())
                .stream()
                .map(String::toUpperCase)
                .toList()
                .indexOf(key.toUpperCase());
    }
}