package be.codewriter.dmx512.helper;

import be.codewriter.dmx512.client.DMXClient;

import java.util.List;

public class DMXMessage {

    public static byte[] build(List<DMXClient> clients) {
        var length = 0;
        for (DMXClient client : clients) {
            var max = client.getStartChannel() + client.getSelectedMode().channels().size();
            length = Math.max(length, max);
        }
        final byte[] dmxData = new byte[length + 1]; // +1 for start code
        dmxData[0] = 0; // DMX start code
        var counter = 1;
        for (DMXClient client : clients) {
            for (var channel : client.getSelectedMode().channels()) {
                dmxData[counter] = client.getValue(channel);
                counter++;
            }
            for (var channel = 0; channel < client.getSelectedMode().channels().size(); channel++) {

            }
        }
        return dmxData;
    }
}
