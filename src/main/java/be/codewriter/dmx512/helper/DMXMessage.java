package be.codewriter.dmx512.helper;

import be.codewriter.dmx512.client.DMXClient;

import java.util.List;

public class DMXMessage {

    public static byte[] build(List<DMXClient> clients) {
        var length = clients.stream()
                .mapToInt(client -> client.getStartChannel() + client.getDataLength())
                .max()
                .orElse(0);
        final byte[] dmxData = new byte[length];
        dmxData[0] = 0; // DMX sta1 code
        for (DMXClient client : clients) {
            var startIndex = client.getStartChannel();
            length = client.getDataLength();
            for (var idx = 0; idx < length; idx++) {
                dmxData[startIndex + idx] = client.getValue(idx);
            }
        }
        return dmxData;
    }
}
