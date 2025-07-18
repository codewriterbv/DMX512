package be.codewriter.dmx512.controller;

import be.codewriter.dmx512.controller.change.DMXChangeListener;
import be.codewriter.dmx512.controller.change.DMXChangeMessage;
import be.codewriter.dmx512.model.DMXUniverse;

import java.util.ArrayList;
import java.util.List;

public interface DMXController {

    List<DMXChangeListener> listeners = new ArrayList<>();

    DMXType getType();

    String getAddress();

    boolean connect();

    void render(DMXUniverse universe);

    void render(int id, byte[] data);

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

