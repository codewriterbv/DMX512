package be.codewriter.dmx512.ofl.model;

import be.codewriter.dmx512.ofl.CapabilityTypeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

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
