package be.codewriter.dmx512.controller;

import java.util.ArrayList;
import java.util.List;

public class DMXChangeNotifier {
    List<DMXChangeListener> listeners = new ArrayList<>();

    public void addListener(DMXChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DMXChangeListener listener) {
        listeners.remove(listener);
    }

    protected void notifyListeners(DMXChangeMessage dmxChangeMessage) {
        notifyListeners(dmxChangeMessage, "");
    }

    protected void notifyListeners(DMXChangeMessage dmxChangeMessage, String value) {
        listeners.forEach(l -> l.notify(dmxChangeMessage, value));
    }
}
