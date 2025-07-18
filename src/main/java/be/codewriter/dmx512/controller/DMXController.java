package be.codewriter.dmx512.controller;

import be.codewriter.dmx512.controller.change.DMXChangeListener;
import be.codewriter.dmx512.controller.change.DMXStatusChangeMessage;
import be.codewriter.dmx512.model.DMXUniverse;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface used by all controllers provided by the library
 */
public interface DMXController {

    /**
     * List of listeners to be notified of any change
     */
    List<DMXChangeListener> listeners = new ArrayList<>();

    /**
     * Get the type of controller
     *
     * @return {@link DMXControllerType}
     */
    DMXControllerType getType();

    /**
     * Get the address of the controller
     *
     * @return address
     */
    String getAddress();

    /**
     * Start connection
     *
     * @return success
     */
    boolean connect();

    /**
     * Render the given universe on the controller
     *
     * @param universe universe
     */
    void render(DMXUniverse universe);

    /**
     * Render the given data on the given universe id on the controller
     *
     * @param id   universe id
     * @param data data
     */
    void render(int id, byte[] data);

    /**
     * Close the connection
     */
    void close();

    /**
     * Get the connection status
     *
     * @return is connected
     */
    boolean isConnected();

    /**
     * Add a change listener
     *
     * @param listener {@link DMXChangeListener}
     */
    default void addListener(DMXChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a change listener
     *
     * @param listener {@link DMXChangeListener}
     */
    default void removeListener(DMXChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify the listeners of a change
     *
     * @param dmxStatusChangeMessage {@link DMXStatusChangeMessage}
     */
    default void notifyListeners(DMXStatusChangeMessage dmxStatusChangeMessage) {
        notifyListeners(dmxStatusChangeMessage, "");
    }

    /**
     * Notify the listeners of a change with the given value
     *
     * @param dmxStatusChangeMessage {@link DMXStatusChangeMessage}
     * @param value                  value
     */
    default void notifyListeners(DMXStatusChangeMessage dmxStatusChangeMessage, String value) {
        listeners.forEach(l -> l.notify(dmxStatusChangeMessage, value));
    }

    /**
     * List of available controller types
     */
    enum DMXControllerType {
        /**
         * IP to DMX
         */
        IP,
        /**
         * Serial (USB) to DMX
         */
        SERIAL
    }
}

