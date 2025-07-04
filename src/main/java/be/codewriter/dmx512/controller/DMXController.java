package be.codewriter.dmx512.controller;

import be.codewriter.dmx512.client.DMXClient;

import java.util.List;

public interface DMXController {

    boolean connect(String address);

    void render(List<DMXClient> clients);

    void sendData(byte[] data);

    void close();

    boolean isConnected();
}

