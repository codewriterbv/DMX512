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
public class FixtureParserLedPartyTclTest {
    private static final String TEST_FIXTURE_PATH = "ofl/eurolite/led-party-tcl-spot.json";
    private Fixture fixture;

    @BeforeEach
    void setUp() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(TEST_FIXTURE_PATH)) {
            fixture = OpenFormatLibraryParser.parseFixture(is);
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
        assertThat(physical.DMXconnector()).isNotEmpty();
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
        OpenFormatLibraryParser.writeFixture(fixture, tempFile);

        // Read it back
        Fixture readFixture = OpenFormatLibraryParser.parseFixture(tempFile);

        // Verify the read fixture matches the original
        assertThat(readFixture)
                .usingRecursiveComparison()
                .isEqualTo(fixture);
    }

    /*
    @Test
    void shouldParseChannelCapabilities() {
        assertThat(fixture.availableChannels())
                .hasValueSatisfying((Channel channel) -> {
                    if (channel.capabilities() != null) {
                        assertThat(channel.capabilities())
                                .allSatisfy(capability -> {
                                    assertThat(capability.type()).isNotEmpty();
                                    if (capability.dmxRange() != null) {
                                        assertThat(capability.dmxRange()).hasSize(2);
                                    }
                                });
                    }
                });
    }
    */

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

        Fixture testFixture = OpenFormatLibraryParser.parseFixture(jsonContent);
        assertThat(testFixture.name()).isEqualTo("Test LED PAR Spot");
        assertThat(testFixture.categories()).contains("Color Changer");
    }

    @Test
    void shouldHandleInvalidJson() {
        String invalidJson = "{ invalid json }";

        assertThatThrownBy(() -> OpenFormatLibraryParser.parseFixture(invalidJson))
                .isInstanceOf(IOException.class);
    }
}
