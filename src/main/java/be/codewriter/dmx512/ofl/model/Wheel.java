package be.codewriter.dmx512.ofl.model;

import java.util.List;

/**
 * OFL definition of a wheel
 *
 * @param slots list of {@link Slot}
 */
public record Wheel(
        List<Slot> slots
) {
}
