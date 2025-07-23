package be.codewriter.dmx512.model;

import be.codewriter.dmx512.ofl.model.Fixture;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a DMX universe by id and list of {@link DMXClient}
 */
public class DMXUniverse {

    private final int id;
    private final List<DMXClient> clients;

    /**
     * Universe constructor, creates an empty clients list
     *
     * @param id id
     */
    public DMXUniverse(int id) {
        this(id, new ArrayList<>());
    }

    /**
     * Universe constructor, creating a list with only the given client
     *
     * @param id     id
     * @param client {@link DMXClient}
     */
    public DMXUniverse(int id, DMXClient client) {
        this(id, List.of(client));
    }

    /**
     * Universe constructor
     *
     * @param id      id
     * @param clients list if {@link DMXClient}
     */
    public DMXUniverse(int id, List<DMXClient> clients) {
        if (id < 0 || id > 32767) {
            throw new IllegalArgumentException("Universe must be between 0 and 32767");
        }
        this.id = id;
        this.clients = clients;
    }

    /**
     * Add a client to the list
     *
     * @param client {@link DMXClient}
     */
    public void addClient(DMXClient client) {
        this.clients.add(client);
    }

    /**
     * Get all the clients
     *
     * @return list of {@link DMXClient}
     */
    public List<DMXClient> getClients() {
        return this.clients;
    }

    /**
     * Get the clients of the given type of fixture
     *
     * @param fixture
     * @return list of {@link DMXClient}
     */
    public List<DMXClient> getFixtureClients(Fixture fixture) {
        return this.clients.stream()
                .filter(c -> c.getFixture() == fixture)
                .toList();
    }

    /**
     * Update the value for the given channel of all the clients which have the channel with the given name
     *
     * @param key   channel name
     * @param value new value
     */
    public void update(String key, byte value) {
        this.clients.stream()
                .filter(c -> c.hasChannel(key))
                .forEach(c -> c.setValue(key, value));
    }

    /**
     * Update the value for the given channel of the clients of the given fixture type
     *
     * @param fixture {@link Fixture}
     * @param key     channel name
     * @param value   new value
     */
    public void updateFixtures(Fixture fixture, String key, byte value) {
        this.clients.stream()
                .filter(c -> c.getFixture() == fixture && c.hasChannel(key))
                .forEach(c -> c.setValue(key, value));
    }

    /**
     * Get the data
     *
     * @return byte array
     */
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

    /**
     * Get the length
     *
     * @return length
     */
    public int getLength() {
        return getData().length;
    }

    /**
     * Get the id
     *
     * @return id
     */
    public int getId() {
        return id;
    }
}
