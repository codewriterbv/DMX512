package be.codewriter.dmx512.ofl.model;

import be.codewriter.dmx512.MotherObjects;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FixtureTest {

    private static Fixture fixture;

    @BeforeAll
    static void setUp() {
        fixture = MotherObjects.nineChannelFixture();
    }

    @Test
    void mustReturnCorrectChannelIndex() {
        assertAll(
                () -> assertEquals(0, fixture.getMode("9-Channel").getChannelIndex("red")),
                () -> assertEquals(0, fixture.getMode("9-Channel").getChannelIndex("RED")),
                () -> assertEquals(0, fixture.getMode("9-Channel").getChannelIndex("Red")),
                () -> assertEquals(1, fixture.getMode("9-Channel").getChannelIndex("Green")),
                () -> assertEquals(2, fixture.getMode("9-Channel").getChannelIndex("blue")),
                () -> assertEquals(3, fixture.getMode("9-Channel").getChannelIndex(" di mm / er ")),
                () -> assertEquals(7, fixture.getMode("9-Channel").getChannelIndex("Pan/Tilt Speed"))
        );
    }

    @Test
    void mustReturnCorrectMode() {
        assertAll(
                () -> assertEquals("9ch", fixture.getMode("9-Channel").shortName()),
                () -> assertNull(fixture.getMode("11-Channel"))
        );
    }
}
