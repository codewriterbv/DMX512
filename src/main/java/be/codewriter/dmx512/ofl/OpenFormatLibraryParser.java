package be.codewriter.dmx512.ofl;

import be.codewriter.dmx512.ofl.model.Fixture;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Parser to load OFL files.
 */
public class OpenFormatLibraryParser {

    private static final ObjectMapper mapper = JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();

    private OpenFormatLibraryParser() {
        // Hide constructor
    }

    /**
     * Parse a fixture from a JSON file
     */
    public static Fixture parseFixture(File file) throws IOException {
        return mapper.readValue(file, Fixture.class);
    }

    /**
     * Parse a fixture from a JSON String
     */
    public static Fixture parseFixture(String jsonString) throws IOException {
        return mapper.readValue(jsonString, Fixture.class);
    }

    /**
     * Parse a fixture from a JSON InputStream
     */
    public static Fixture parseFixture(InputStream is) throws IOException {
        return parseFixture(new String(is.readAllBytes()));
    }

    /**
     * Write a fixture to a JSON file
     */
    public static void writeFixture(Fixture fixture, File file) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, fixture);
    }
}
