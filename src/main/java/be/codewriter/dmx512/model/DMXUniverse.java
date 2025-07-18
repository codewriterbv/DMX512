package be.codewriter.dmx512.model;

import java.util.ArrayList;
import java.util.List;

public class DMXUniverse {

    private final int id;
    private final List<DMXClient> clients;

    /**
     * Universe constructor, creates an empty clients list
     *
     * @param id
     */
    public DMXUniverse(int id) {
        this(id, new ArrayList<>());
    }

    /**
     * Universe constructor, creating a list with only the given client
     *
     * @param id
     * @param client
     */
    public DMXUniverse(int id, DMXClient client) {
        this(id, List.of(client));
    }

    /**
     * Universe constructor
     *
     * @param id
     * @param clients
     */
    public DMXUniverse(int id, List<DMXClient> clients) {
        if (id < 0 || id > 32767) {
            throw new IllegalArgumentException("Universe must be between 0 and 32767");
        }
        this.id = id;
        this.clients = clients;
    }

    public byte[] getData() {
        var length = clients.stream()
                .mapToInt(client -> client.getAddress() + client.getDataLength() - 1)
                .max()
                .orElse(0);
        var data = new byte[length];
        for (DMXClient client : clients) {
            var startIndex = client.getAddress() - 1;
            length = client.getDataLength();
            for (var idx = 0; idx < length; idx++) {
                data[startIndex + idx] = client.getValue(idx);
            }
        }
        return data;
    }

    public int getLength() {
        return getData().length;
    }

    public int getId() {
        return id;
    }
}
