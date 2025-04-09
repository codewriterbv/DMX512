package be.codewriter.dmx512.ofl.model;

import java.util.List;

public record Slot(
        String type,
        String name,
        List<String> colors
) {
}
