package be.codewriter.dmx512.ofl;

import be.codewriter.dmx512.ofl.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * https://open-fixture-library.org/eurolite/led-party-tcl-spot
 */
class FixtureParserLedPartyTclTest {
    private static final String TEST_FIXTURE_PATH = "ofl/eurolite/led-party-tcl-spot.json";
    private Fixture fixture;

    @BeforeEach
    void setUp() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(TEST_FIXTURE_PATH)) {
            fixture = OFLParser.parse(is);
        }
    }

    @Test
    void shouldParseBasicFixtureInformation() {
        assertThat(fixture.name()).isNotEmpty();
        assertThat(fixture.categories())
                .isNotEmpty()
                .contains("Color Changer");
    }

    @Test
    void shouldParseMetaInformation() {
        Meta meta = fixture.meta();
        assertThat(meta.authors()).isNotEmpty();
        assertThat(meta.createDate()).isNotEmpty();
        assertThat(meta.lastModifyDate()).isNotEmpty();
    }

    @Test
    void shouldParseLinks() {
        Links links = fixture.links();
        assertThat(links.manual()).isNotEmpty();
        assertThat(links.productPage()).isNotEmpty();
    }

    @Test
    void shouldParsePhysicalProperties() {
        Physical physical = fixture.physical();
        assertThat(physical.dimensions()).hasSize(3);
        assertThat(physical.weight()).isPositive();
        assertThat(physical.power()).isPositive();
        assertThat(physical.dmxConnector()).isNotEmpty();
        assertThat(physical.bulb()).isNotNull();
    }

    @Test
    void shouldParseChannels() {
        assertThat(fixture.availableChannels())
                .isNotEmpty()
                .satisfies(channels -> {
                    // Test common channels like dimmer, strobe, etc.
                    assertThat(channels).containsKey("Dimmer");

                    Channel dimmerChannel = channels.get("Dimmer");
                    assertThat(dimmerChannel.capability())
                            .satisfies(cap -> {
                                assertThat(cap.type()).isEqualTo(CapabilityType.INTENSITY);
                            });
                });
    }

    @Test
    void shouldParseModes() {
        assertThat(fixture.modes())
                .isNotEmpty()
                .allSatisfy(mode -> {
                    assertThat(mode.name()).isNotEmpty();
                    assertThat(mode.channels()).isNotEmpty();
                });
    }

    @Test
    void shouldHandleWritingAndReadingFixture() throws IOException {
        File tempFile = File.createTempFile("ledparspot", ".json");
        tempFile.deleteOnExit();

        // Write the fixture to a temporary file
        OFLParser.write(fixture, tempFile);

        // Read it back
        Fixture readFixture = OFLParser.parse(tempFile);

        // Verify the read fixture matches the original
        assertThat(readFixture)
                .usingRecursiveComparison()
                .isEqualTo(fixture);
    }

    @Test
    void shouldParseFromJsonString() throws IOException {
        String jsonContent = """
                {
                    "name": "Test LED PAR Spot",
                    "categories": ["Color Changer"],
                    "meta": {
                        "authors": ["Test Author"],
                        "createDate": "2024-01-01",
                        "lastModifyDate": "2024-01-01"
                    }
                }
                """;

        Fixture testFixture = OFLParser.parse(jsonContent);
        assertThat(testFixture.name()).isEqualTo("Test LED PAR Spot");
        assertThat(testFixture.categories()).contains("Color Changer");
    }

    @Test
    void shouldHandleInvalidJson() {
        String invalidJson = "{ invalid json }";

        assertThatThrownBy(() -> OFLParser.parse(invalidJson))
                .isInstanceOf(IOException.class);
    }
}
