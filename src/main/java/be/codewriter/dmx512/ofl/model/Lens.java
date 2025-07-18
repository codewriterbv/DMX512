package be.codewriter.dmx512.ofl.model;

import java.util.List;

/**
 * OFL definition of a lens
 *
 * @param degreesMinMax minimum/maximum possible beam angle in degrees
 */
public record Lens(
        List<Integer> degreesMinMax
) {
}
