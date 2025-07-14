package be.codewriter.dmx512.network;

import java.net.InetAddress;

public record DMXIPDevice(
        InetAddress address,
        Protocol protocol,
        String name,
        int universeCount) {
    // Traditional getters to be able to links these values to e.g. JavaFX table columns
    public InetAddress getAddress() {
        return address();
    }

    public Protocol getProtocol() {
        return protocol();
    }

    public String getName() {
        return name();
    }

    public int getUniverseCount() {
        return universeCount();
    }
}

