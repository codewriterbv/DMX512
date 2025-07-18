package be.codewriter.dmx512;

import be.codewriter.dmx512.model.DMXClient;
import be.codewriter.dmx512.ofl.model.Channel;
import be.codewriter.dmx512.ofl.model.Fixture;
import be.codewriter.dmx512.ofl.model.Mode;

import java.util.LinkedHashMap;
import java.util.List;

public class MotherObjects {

    public static DMXClient fiveChannelClient(byte red, byte green, byte blue, byte dimmer, byte effects) {
        return fiveChannelClient(red, green, blue, dimmer, effects, 1);
    }

    public static DMXClient fiveChannelClient(byte red, byte green, byte blue, byte dimmer, byte effects, int startChannel) {
        var fixture = fiveChannelFixture();
        var client = new DMXClient(fixture, fixture.modes().getFirst(), startChannel);

        client.setValue("Red", red);
        client.setValue("Green", green);
        client.setValue("Blue", blue);
        client.setValue("Dimmer", dimmer);
        client.setValue("Effects", effects);

        return client;
    }

    public static DMXClient nineChannelClient(byte red, byte green, byte blue, byte dimmer, byte effects, byte pan, byte tilt, byte speed, byte gobo) {
        return nineChannelClient(red, green, blue, dimmer, effects, pan, tilt, speed, gobo, 1);
    }

    public static DMXClient nineChannelClient(byte red, byte green, byte blue, byte dimmer, byte effects, byte pan, byte tilt, byte speed, byte gobo, int startChannel) {
        var fixture = nineChannelFixture();
        var client = new DMXClient(fixture, fixture.modes().getFirst(), startChannel);

        client.setValue("Red", red);
        client.setValue("Green", green);
        client.setValue("Blue", blue);
        client.setValue("Dimmer", dimmer);
        client.setValue("Effects", effects);
        client.setValue("Pan", pan);
        client.setValue("Tilt", tilt);
        client.setValue("Pan/Tilt Speed", speed);
        client.setValue("Gobo", gobo);

        return client;
    }

    public static Fixture fiveChannelFixture() {
        LinkedHashMap<String, Channel> channels = new LinkedHashMap<>();
        channels.put("Red", new Channel(null, null, null, null));
        channels.put("Green", new Channel(null, null, null, null));
        channels.put("Blue", new Channel(null, null, null, null));
        channels.put("Dimmer", new Channel(null, null, null, null));
        channels.put("Effects", new Channel(null, null, null, null));

        return new Fixture(
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

    public static Fixture nineChannelFixture() {
        LinkedHashMap<String, Channel> channels = new LinkedHashMap<>();
        channels.put("Red", new Channel(null, null, null, null));
        channels.put("Green", new Channel(null, null, null, null));
        channels.put("Blue", new Channel(null, null, null, null));
        channels.put("Dimmer", new Channel(null, null, null, null));
        channels.put("Effects", new Channel(null, null, null, null));
        channels.put("Pan", new Channel(null, null, null, null));
        channels.put("Tilt", new Channel(null, null, null, null));
        channels.put("Pan/Tilt Speed", new Channel(null, null, null, null));
        channels.put("Gobo", new Channel(null, null, null, null));

        return new Fixture(
                "Name",
                List.of("Cat 1", "Cate 2"),
                null, // meta
                null, // links
                null, // physical
                null, // wheels
                channels,
                List.of(new Mode(
                        "9-Channel",
                        "9ch",
                        List.of("Red", "Green", "Blue", "Dimmer", "Effects", "Pan", "Tilt", "Pan/Tilt Speed", "Gobo")
                )) // modes
        );
    }
}
