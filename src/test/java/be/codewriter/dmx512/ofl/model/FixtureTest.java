package be.codewriter.dmx512.ofl.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FixtureTest {

    @Test
    void mustReturnCorrectChannelIndex() {
        var fixture = new Fixture(
                "Name",
                List.of("Cat 1", "Cate 2"),
                null, // meta
                null, // links
                null, // physical
                null, // wheels
                Map.of(
                        "Red", new Channel(null, null, null, null),
                        "Green", new Channel(null, null, null, null),
                        "Blue", new Channel(null, null, null, null),
                        "Dimmer", new Channel(null, null, null, null),
                        "Effects", new Channel(null, null, null, null)
                ),
                List.of(new Mode(
                        "5-Channel",
                        "5ch",
                        List.of("Red", "Green", "Blue", "Dimmer", "Effects")
                )) // modes
        );

        assertAll(
                () -> assertEquals(0, fixture.getChannelIndex("red")),
                () -> assertEquals(0, fixture.getChannelIndex("RED")),
                () -> assertEquals(0, fixture.getChannelIndex("Red"))
        );
    }
}
