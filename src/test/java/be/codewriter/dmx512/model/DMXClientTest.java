package be.codewriter.dmx512.model;

import be.codewriter.dmx512.MotherObjects;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DMXClientTest {

    private static DMXClient client;

    @BeforeAll
    static void setUp() {
        client = MotherObjects.fiveChannelClient((byte) 150, (byte) 5, (byte) 255, (byte) 127, (byte) 128);
    }

    @Test
    void mustReturnCorrectValue() {
        assertAll(
                () -> assertEquals((byte) 150, client.getValue("red"), "red"),
                () -> assertEquals((byte) 150, client.getValue("RED"), "RED"),
                () -> assertEquals((byte) 150, client.getValue("Red"), "Red"),
                () -> assertEquals((byte) 5, client.getValue("green"), "green"),
                () -> assertEquals((byte) 255, client.getValue("blue"), "blue"),
                () -> assertEquals((byte) 127, client.getValue("dimmer"), "dimmer"),
                () -> assertEquals((byte) 128, client.getValue("EFFECTS"), "EFFECTS")
        );
    }

    @Test
    void mustCreateCorrectDMXData() {
        byte[] message = client.getData();

        assertAll(
                () -> assertEquals(5, message.length),
                () -> assertEquals((byte) 150, message[0], "Red"),
                () -> assertEquals((byte) 5, message[1], "Green"),
                () -> assertEquals((byte) 255, message[2], "Blue"),
                () -> assertEquals((byte) 127, message[3], "Dimmer"),
                () -> assertEquals((byte) 128, message[4], "Effects")
        );
    }

    @Test
    void mustReturnCorrectHasChannel() {
        assertAll(
                () -> assertTrue(client.hasChannel("red")),
                () -> assertTrue(client.hasChannel("Red")),
                () -> assertTrue(client.hasChannel("RED")),
                () -> assertTrue(client.hasChannel(" red")),
                () -> assertTrue(client.hasChannel("red ")),
                () -> assertTrue(client.hasChannel(" red ")),
                () -> assertFalse(client.hasChannel("rod"))
        );
    }
}
