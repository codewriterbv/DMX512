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
    public Mode getModeByName(String name) {
        return modes.stream()
                .filter(mode -> mode.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}