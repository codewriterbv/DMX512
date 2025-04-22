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

    protected void notifyListeners(DMXChangeMessage DMXChangeMessage) {
        notifyListeners(DMXChangeMessage, "");
    }

    protected void notifyListeners(DMXChangeMessage DMXChangeMessage, String value) {
        listeners.forEach(l -> l.notify(DMXChangeMessage, value));
    }
}
