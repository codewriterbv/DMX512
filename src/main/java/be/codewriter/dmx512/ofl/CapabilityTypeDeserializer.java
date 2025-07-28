package be.codewriter.dmx512.ofl;

import be.codewriter.dmx512.ofl.model.CapabilityType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * JSON deserializer for a {@link CapabilityType}
 */
public class CapabilityTypeDeserializer extends JsonDeserializer<CapabilityType> {

    private CapabilityTypeDeserializer() {
        // Hide constructor
    }

    @Override
    public CapabilityType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return CapabilityType.fromJson(p.getValueAsString());
    }
}

