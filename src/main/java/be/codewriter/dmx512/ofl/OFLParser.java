package be.codewriter.dmx512.ofl;

import be.codewriter.dmx512.ofl.model.Fixture;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class OFLParser {

    private static final ObjectMapper mapper = JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();

    private OFLParser() {
        // Hide constructor
    }

    /**
     * Parse a fixture from a JSON file
     */
    public static Fixture parse(File file) throws IOException {
        return mapper.readValue(file, Fixture.class);
    }

    /**
     * Parse a fixture from a JSON String
     */
    public static Fixture parse(String jsonString) throws IOException {
        return mapper.readValue(jsonString, Fixture.class);
    }

    /**
     * Parse a fixture from a JSON InputStream
     */
    public static Fixture parse(InputStream is) throws IOException {
        return parse(new String(is.readAllBytes()));
    }

    /**
     * Write a fixture to a JSON file
     */
    public static void write(Fixture fixture, File file) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, fixture);
    }
}
