package be.codewriter.dmx512.helper;

import be.codewriter.dmx512.client.DMXClient;

import java.util.List;

public class DMXMessage {

    private final byte[] data;

    public DMXMessage(List<DMXClient> clients) {
        var length = clients.stream()
                .mapToInt(client -> client.getStartChannel() + client.getDataLength())
                .max()
                .orElse(0);
        data = new byte[length];
        for (DMXClient client : clients) {
            var startIndex = client.getStartChannel() - 1;
            length = client.getDataLength();
            for (var idx = 0; idx < length; idx++) {
                data[startIndex + idx] = client.getValue(idx);
            }
        }
    }

    public byte[] getData() {
        return data;
    }

    public int getLength() {
        return data.length;
    }
}
