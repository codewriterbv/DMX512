package be.codewriter.dmx512.ofl.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Channel(
        @JsonProperty("fineChannelAliases")
        List<String> fineChannelAliases,
        Capability capability,
        List<Capability> capabilities,
        Double defaultValue
) {
}
