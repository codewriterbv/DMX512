package be.codewriter.dmx512.serial;

public record SerialConnection(String name, String path, String description, String manufacturer,
                               String serialNumber, String portDescription, String portLocation) {
}
