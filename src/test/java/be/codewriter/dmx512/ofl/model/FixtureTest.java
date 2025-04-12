package be.codewriter.dmx512.ofl.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FixtureTest {

    private static Fixture fixture;

    @BeforeAll
    static void setUp() {
        LinkedHashMap<String, Channel> channels = new LinkedHashMap<>();
        channels.put("Red", new Channel(null, null, null, null));
        channels.put("Green", new Channel(null, null, null, null));
        channels.put("Blue", new Channel(null, null, null, null));
        channels.put("Dimmer", new Channel(null, null, null, null));
        channels.put("Effects", new Channel(null, null, null, null));

        fixture = new Fixture(
                "Name",
                List.of("Cat 1", "Cate 2"),
                null, // meta
                null, // links
                null, // physical
                null, // wheels
                channels,
                List.of(new Mode(
                        "6-Channel",
                        "6ch",
                        List.of("Red", "Green", "Blue", "Dimmer", "Effects", "Pan/Tilt Speed")
                )) // modes
        );
    }

    @Test
    void mustReturnCorrectChannelIndex() {
        assertAll(
                () -> assertEquals(0, fixture.getMode("6-Channel").getChannelIndex("red")),
                () -> assertEquals(0, fixture.getMode("6-Channel").getChannelIndex("RED")),
                () -> assertEquals(0, fixture.getMode("6-Channel").getChannelIndex("Red")),
                () -> assertEquals(1, fixture.getMode("6-Channel").getChannelIndex("Green")),
                () -> assertEquals(2, fixture.getMode("6-Channel").getChannelIndex("blue")),
                () -> assertEquals(3, fixture.getMode("6-Channel").getChannelIndex(" di mm / er ")),
                () -> assertEquals(5, fixture.getMode("6-Channel").getChannelIndex("Pan/Tilt Speed"))
        );
    }

    @Test
    void mustReturnCorrectMode() {
        assertAll(
                () -> assertEquals("6ch", fixture.getMode("6-Channel").shortName()),
                () -> assertNull(fixture.getMode("11-Channel"))
        );
    }
}
