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
                        "5-Channel",
                        "5ch",
                        List.of("Red", "Green", "Blue", "Dimmer", "Effects")
                )) // modes
        );
    }

    @Test
    void mustReturnCorrectChannelIndex() {
        assertAll(
                () -> assertEquals(0, fixture.getMode("5-Channel").getChannelIndex("red")),
                () -> assertEquals(0, fixture.getMode("5-Channel").getChannelIndex("RED")),
                () -> assertEquals(0, fixture.getMode("5-Channel").getChannelIndex("Red")),
                () -> assertEquals(1, fixture.getMode("5-Channel").getChannelIndex("Green")),
                () -> assertEquals(2, fixture.getMode("5-Channel").getChannelIndex("blue")),
                () -> assertEquals(3, fixture.getMode("5-Channel").getChannelIndex(" di mm / er ")),
                () -> assertEquals(4, fixture.getMode("5-Channel").getChannelIndex("effects "))
        );
    }

    @Test
    void mustReturnCorrectMode() {
        assertAll(
                () -> assertEquals("5ch", fixture.getMode("5-Channel").shortName()),
                () -> assertNull(fixture.getMode("11-Channel"))
        );
    }
}
