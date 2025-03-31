package be.codewriter.dmx512.network;

public class Device {
    private final String ipAddress;
    private final Protocol protocol;
    private final String name;
    private final int universeCount;

    public Device(String ipAddress, Protocol protocol, String name, int universeCount) {
        this.ipAddress = ipAddress;
        this.protocol = protocol;
        this.name = name;
        this.universeCount = universeCount;
    }

    // Getters
    public String getIpAddress() {
        return ipAddress;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public String getName() {
        return name;
    }

    public int getUniverseCount() {
        return universeCount;
    }
}

