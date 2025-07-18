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
public class OFLParser {

    private static final ObjectMapper mapper = JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();

    private OFLParser() {
        // Hide constructor
    }

    /**
     * Parse a fixture from a JSON file
     *
     * @param file file to be loaded
     * @return {@link Fixture}
     * @throws IOException
     */
    public static Fixture parse(File file) throws IOException {
        return mapper.readValue(file, Fixture.class);
    }

    /**
     * Parse a fixture from a JSON String
     *
     * @param jsonString json string
     * @return {@link Fixture}
     * @throws IOException
     */
    public static Fixture parse(String jsonString) throws IOException {
        return mapper.readValue(jsonString, Fixture.class);
    }

    /**
     * Parse a fixture from a JSON InputStream
     *
     * @param is inputstream
     * @return {@link Fixture}
     * @throws IOException
     */
    public static Fixture parse(InputStream is) throws IOException {
        return parse(new String(is.readAllBytes()));
    }

    /**
     * Write a fixture to a JSON file
     *
     * @param fixture {@link Fixture}
     * @param file    file to be created
     * @throws IOException
     */
    public static void write(Fixture fixture, File file) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, fixture);
    }
}
