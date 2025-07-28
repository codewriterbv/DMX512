package be.codewriter.dmx512.controller.serial;

/**
 * Discovered serial interface.
 * Provides traditional getters to be able to links these values to e.g. JavaFX table columns.
 *
 * @param path        path to use the interface
 * @param name        name of the interface
 * @param description description
 */
public record SerialConnection(String path, String name, String description) {
    /**
     * Get the path
     *
     * @return path
     */
    public String getPath() {
        return path();
    }

    /**
     * Get the name
     *
     * @return name
     */
    public String getName() {
        return name();
    }

    /**
     * Get the description
     *
     * @return description
     */
    public String getDescription() {
        return description();
    }
}
