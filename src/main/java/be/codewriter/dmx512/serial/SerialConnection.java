package be.codewriter.dmx512.serial;

public record SerialConnection(String path, String name, String description) {
    // Traditional getters to be able to links these values to e.g. JavaFX table columns
    public String getPath() {
        return path();
    }

    public String getName() {
        return name();
    }

    public String getDescription() {
        return description();
    }
}
