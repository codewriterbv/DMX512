package be.codewriter.dmx512.fixture;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RGBFixtureTest {

    @Test
    void shouldSetColorFromIntegers() {
        var fixture = new RGBFixture(null, 3);
        fixture.setColor(255, 150, 9);
        assertAll(
                () -> assertEquals(255, fixture.getRed(), "Red"),
                () -> assertEquals(150, fixture.getGreen(), "Green"),
                () -> assertEquals(9, fixture.getBlue(), "Blue"),
                () -> assertEquals(new Color(255, 150, 9), fixture.getColor(), "Color")
        );
    }

    @Test
    void shouldSetColorFromSingleInteger() {
        var fixture = new RGBFixture(null, 3);
        fixture.setColor((new Color(255, 150, 9)).getRGB());
        assertAll(
                () -> assertEquals(255, fixture.getRed(), "Red"),
                () -> assertEquals(150, fixture.getGreen(), "Green"),
                () -> assertEquals(9, fixture.getBlue(), "Blue"),
                () -> assertEquals(new Color(255, 150, 9), fixture.getColor(), "Color")
        );
    }

    @Test
    void shouldSetColorFromColor() {
        var fixture = new RGBFixture(null, 3);
        fixture.setColor(new Color(255, 150, 9));
        assertAll(
                () -> assertEquals(255, fixture.getRed(), "Red"),
                () -> assertEquals(150, fixture.getGreen(), "Green"),
                () -> assertEquals(9, fixture.getBlue(), "Blue"),
                () -> assertEquals(new Color(255, 150, 9), fixture.getColor(), "Color")
        );
    }
}
