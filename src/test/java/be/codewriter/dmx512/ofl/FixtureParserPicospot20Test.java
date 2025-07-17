package be.codewriter.dmx512.ofl;

import be.codewriter.dmx512.ofl.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * https://open-fixture-library.org/fun-generation/picospot-20-led
 */
class FixtureParserPicospot20Test {
    private static final String TEST_FIXTURE_PATH = "ofl/fun-generation/picospot-20-led.json";
    private Fixture fixture;

    @BeforeEach
    void setUp() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(TEST_FIXTURE_PATH)) {
            fixture = OFLParser.parse(is);
        }
    }

    @Test
    void shouldParseBasicFixtureInformation() {
        assertThat(fixture.name()).isEqualTo("PicoSpot 20 LED");
        assertThat(fixture.categories())
                .hasSize(2)
                .containsExactly("Moving Head", "Color Changer");
    }

    @Test
    void shouldParseMetaInformation() {
        Meta meta = fixture.meta();
        assertThat(meta.authors())
                .hasSize(2)
                .containsExactly("LordVonAdel", "Moritz Weirauch");
        assertThat(meta.createDate()).isEqualTo("2019-08-21");
        assertThat(meta.lastModifyDate()).isEqualTo("2024-05-07");
    }

    @Test
    void shouldParseLinks() {
        Links links = fixture.links();
        assertThat(links.manual())
                .hasSize(1)
                .contains("https://images.static-thomann.de/pics/atg/atgdata/document/manual/372642_c_372642_r3_en_online.pdf");
        assertThat(links.productPage())
                .hasSize(1)
                .contains("https://www.thomann.de/intl/fun_generation_picospot_20_led.htm");
        assertThat(links.video())
                .hasSize(1)
                .contains("https://video1.thomann.de//vidiot/02591c1c/video_i4640p10_yd59vqpa.mp4");
    }

    @Test
    void shouldParsePhysicalProperties() {
        Physical physical = fixture.physical();
        assertThat(physical.dimensions())
                .hasSize(3)
                .containsExactly(162, 242, 174);
        assertThat(physical.weight()).isEqualTo(3);
        assertThat(physical.power()).isEqualTo(35);
        assertThat(physical.DMXconnector()).isEqualTo("3-pin");
        assertThat(physical.bulb().type()).isEqualTo("12W white CREE LED");
        assertThat(physical.lens().degreesMinMax())
                .hasSize(2)
                .containsExactly(13, 13);
    }

    @Test
    void shouldParseColorWheel() {
        Wheel colorWheel = fixture.wheels().get("Color Wheel");
        assertThat(colorWheel).isNotNull();
        assertThat(colorWheel.slots())
                .hasSize(8)
                .satisfies(slots -> {
                    Slot firstSlot = slots.get(0);
                    assertThat(firstSlot.type()).isEqualTo("Color");
                    assertThat(firstSlot.name()).isEqualTo("White");
                    assertThat(firstSlot.colors())
                            .hasSize(1)
                            .contains("#ffffff");
                });
    }

    @Test
    void shouldParseGoboWheel() {
        Wheel goboWheel = fixture.wheels().get("Gobo Wheel");
        assertThat(goboWheel).isNotNull();
        assertThat(goboWheel.slots())
                .hasSize(8)
                .satisfies(slots -> {
                    assertThat(slots.get(0).type()).isEqualTo("Open");
                    assertThat(slots.get(1).type()).isEqualTo("Gobo");
                });
    }

    @Test
    void shouldParseChannels() {
        Map<String, Channel> channels = fixture.availableChannels();

        // Test Pan channel
        Channel panChannel = channels.get("Pan");
        assertThat(panChannel).isNotNull();
        assertThat(panChannel.fineChannelAliases())
                .hasSize(1)
                .contains("Pan fine");
        assertThat(panChannel.capability().type()).isEqualTo(CapabilityType.PAN);
        assertThat(panChannel.capability().angleStart()).isEqualTo("0deg");
        assertThat(panChannel.capability().angleEnd()).isEqualTo("540deg");

        // Test Color Wheel channel
        Channel colorWheelChannel = channels.get("Color Wheel");
        assertThat(colorWheelChannel).isNotNull();
        assertThat(colorWheelChannel.capabilities())
                .isNotEmpty()
                .satisfies(caps -> {
                    Capability firstCap = caps.get(0);
                    assertThat(firstCap.dmxRange())
                            .containsExactly(0, 10);
                    assertThat(firstCap.type()).isEqualTo(CapabilityType.WHEEL_SLOT);
                    assertThat(firstCap.slotNumber()).isEqualTo(1);
                });
    }

    @Test
    void shouldHandleWritingAndReadingFixture() throws IOException {
        File tempFile = File.createTempFile("fixture", ".json");
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
                    "name": "Test Fixture",
                    "categories": ["Test Category"],
                    "meta": {
                        "authors": ["Test Author"],
                        "createDate": "2024-01-01",
                        "lastModifyDate": "2024-01-01"
                    }
                }
                """;

        Fixture testFixture = OFLParser.parse(jsonContent);
        assertThat(testFixture.name()).isEqualTo("Test Fixture");
        assertThat(testFixture.categories()).containsExactly("Test Category");
    }

    @Test
    void shouldHandleInvalidJson() {
        String invalidJson = "{ invalid json }";

        assertThatThrownBy(() -> OFLParser.parse(invalidJson))
                .isInstanceOf(IOException.class);
    }
}
