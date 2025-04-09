package be.codewriter.dmx512.ofl.model;

import java.util.List;

public record Physical(
        List<Integer> dimensions,
        Float weight,
        Integer power,
        String DMXconnector,
        Bulb bulb,
        Lens lens
) {
}
