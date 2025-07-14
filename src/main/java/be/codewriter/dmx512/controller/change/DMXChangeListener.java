package be.codewriter.dmx512.controller.change;

public interface DMXChangeListener {
    void notify(DMXChangeMessage dmxChangeMessage, String value);
}
