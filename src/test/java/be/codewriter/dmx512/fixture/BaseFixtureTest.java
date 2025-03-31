package be.codewriter.dmx512.fixture;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseFixtureTest {

    @Test
    void shouldSetChannel() {
        var fixture = new BaseFixture(null, 3, 5);
        assertAll(
                () -> assertEquals(3, fixture.getStartChannel(), "Start channel"),
                () -> assertEquals(5, fixture.getChannelCount(), "Channel count")
        );
    }
}
