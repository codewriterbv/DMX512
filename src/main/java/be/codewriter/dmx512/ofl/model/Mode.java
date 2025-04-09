package be.codewriter.dmx512.ofl.model;

import java.util.List;

public record Mode(
        String name,
        String shortName,
        List<String> channels
) {
}
