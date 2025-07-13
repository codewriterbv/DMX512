package be.codewriter.dmx512.controller;

public interface DMXChangeListener {
    void notify(DMXChangeMessage dmxChangeMessage, String value);
}
