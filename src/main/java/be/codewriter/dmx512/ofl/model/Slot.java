package be.codewriter.dmx512.ofl.model;

import java.util.List;

/**
 * OFL definition of a slot
 *
 * @param type   type
 * @param name   name
 * @param colors list of colors
 */
public record Slot(
        String type,
        String name,
        List<String> colors
) {
}
