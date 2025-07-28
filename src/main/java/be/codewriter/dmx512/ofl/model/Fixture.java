package be.codewriter.dmx512.ofl.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * OFL definition of a fixture
 *
 * @param name              name
 * @param categories        list of categories
 * @param meta              {@link Meta}
 * @param links             {@link Links}
 * @param physical          {@link Physical}
 * @param wheels            map of name and {@link Wheel}
 * @param availableChannels map of name and {@link Channel}
 * @param modes             list of {@link Mode}
 */
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
    /**
     * Find the given mode by name
     *
     * @param name name of the mode
     * @return the {@link Mode} or null
     */
    public Mode getModeByName(String name) {
        return modes.stream()
                .filter(mode -> mode.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}