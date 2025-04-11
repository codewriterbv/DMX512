package be.codewriter.dmx512.client;

import be.codewriter.dmx512.helper.DMXMessage;
import be.codewriter.dmx512.ofl.model.Channel;
import be.codewriter.dmx512.ofl.model.Fixture;
import be.codewriter.dmx512.ofl.model.Mode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DMXClientTest {

    private static DMXClient client;

    @BeforeAll
    static void setUp() {
        LinkedHashMap<String, Channel> channels = new LinkedHashMap<>();
        channels.put("Red", new Channel(null, null, null, null));
        channels.put("Green", new Channel(null, null, null, null));
        channels.put("Blue", new Channel(null, null, null, null));
        channels.put("Dimmer", new Channel(null, null, null, null));
        channels.put("Effects", new Channel(null, null, null, null));

        var fixture = new Fixture(
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

        client = new DMXClient(fixture, fixture.modes().getFirst(), 0);

        client.setValue("red", (byte) 150);
        client.setValue("green", (byte) 5);
        client.setValue("blue", (byte) 255);
        client.setValue("Dimmer", (byte) 127);
        client.setValue("effects", (byte) 128);
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
    void mustCreateCorrectDMXMessage() {
        byte[] message = DMXMessage.build(List.of(client));

        assertAll(
                () -> assertEquals(6, message.length),
                () -> assertEquals((byte) 0, message[0], "Align message"),
                () -> assertEquals((byte) 150, message[1], "Red"),
                () -> assertEquals((byte) 5, message[2], "Green"),
                () -> assertEquals((byte) 255, message[3], "Blue"),
                () -> assertEquals((byte) 127, message[4], "Dimmer"),
                () -> assertEquals((byte) 128, message[5], "Effects")
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
