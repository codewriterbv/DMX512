package be.codewriter.dmx512.ofl.model;

import be.codewriter.dmx512.ofl.CapabilityTypeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

/**
 * OFL definition of a capability
 *
 * @param type            {@link CapabilityType}
 * @param angleStart      start angle
 * @param angleEnd        end angle
 * @param speedStart      speed start
 * @param speedEnd        speed end
 * @param dmxRange        list of dmx range s
 * @param slotNumber      slot number
 * @param slotNumberStart slot number start
 * @param slotNumberEnd   slot number end
 * @param effectName      effect name
 * @param shutterEffect   shutter effect
 */
public record Capability(
        @JsonDeserialize(using = CapabilityTypeDeserializer.class)
        CapabilityType type,
        String angleStart,
        String angleEnd,
        String speedStart,
        String speedEnd,
        List<Integer> dmxRange,
        Integer slotNumber,
        Integer slotNumberStart,
        Integer slotNumberEnd,
        String effectName,
        String shutterEffect
) {
}
