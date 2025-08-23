package be.codewriter.dmx512.controller.change;

/**
 * Interface to be used by all classes that want to be informed of DMX changes
 */
public interface DMXChangeListener {
    /**
     * Notify the change
     *
     * @param dmxStatusChangeMessage {@link DMXStatusChangeMessage}
     * @param value                  value
     */
    void notify(DMXStatusChangeMessage dmxStatusChangeMessage, String value);

    /**
     * Notify the change
     *
     * @param dmxStatusChangeMessage {@link DMXStatusChangeMessage}
     * @param data                   byte array containing DMX data
     */
    void notify(DMXStatusChangeMessage dmxStatusChangeMessage, byte[] data);
}
