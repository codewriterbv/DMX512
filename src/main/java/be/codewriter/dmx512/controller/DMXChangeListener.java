package be.codewriter.dmx512.controller;

public interface DMXChangeListener {
    void notify(DMXChangeMessage DMXChangeMessage, String value);
}
