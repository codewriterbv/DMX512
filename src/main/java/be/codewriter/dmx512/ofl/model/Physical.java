package be.codewriter.dmx512.ofl.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * OFL definition of the physical values of a fixture
 *
 * @param dimensions   list of dimensions
 * @param weight       weight
 * @param power        power consumption
 * @param dmxConnector connector type
 * @param bulb         {@link Bulb}
 * @param lens         {@link Lens}
 */
public record Physical(
        List<Integer> dimensions,
        Float weight,
        Integer power,
        @JsonProperty("DMXconnector")
        String dmxConnector,
        Bulb bulb,
        Lens lens
) {
}
