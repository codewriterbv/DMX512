package be.codewriter.dmx512.ofl.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * OFL definition of a channel
 *
 * @param fineChannelAliases list of fine channel aliases
 * @param capability         {@link Capability}
 * @param capabilities       list of {@link Capability}
 * @param defaultValue       default value
 */
public record Channel(
        @JsonProperty("fineChannelAliases")
        List<String> fineChannelAliases,
        Capability capability,
        List<Capability> capabilities,
        Double defaultValue
) {
}
