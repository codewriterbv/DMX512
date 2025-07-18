package be.codewriter.dmx512.controller.change;

/**
 * Available types of DMX status changes
 */
public enum DMXStatusChangeMessage {
    /**
     * The DMX changed to connected state
     */
    CONNECTED,
    /**
     * The DMX changed to disconnected state
     */
    DISCONNECTED,
    /**
     * The DMX has received data
     */
    DATA_RECEIVED
}
