package be.codewriter.dmx512.controller;

import be.codewriter.dmx512.client.DMXClient;
import be.codewriter.dmx512.controller.change.DMXChangeListener;
import be.codewriter.dmx512.controller.change.DMXChangeMessage;

import java.util.ArrayList;
import java.util.List;

public interface DMXController {

    List<DMXChangeListener> listeners = new ArrayList<>();

    DMXType getType();

    String getAddress();

    boolean connect();

    void render(DMXClient client);

    void render(List<DMXClient> clients);

    void render(byte[] data);

    void close();

    boolean isConnected();

    default void addListener(DMXChangeListener listener) {
        listeners.add(listener);
    }

    default void removeListener(DMXChangeListener listener) {
        listeners.remove(listener);
    }

    default void notifyListeners(DMXChangeMessage dmxChangeMessage) {
        notifyListeners(dmxChangeMessage, "");
    }

    default void notifyListeners(DMXChangeMessage dmxChangeMessage, String value) {
        listeners.forEach(l -> l.notify(dmxChangeMessage, value));
    }

    enum DMXType {
        IP,
        SERIAL
    }
}

