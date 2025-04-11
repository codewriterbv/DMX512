package be.codewriter.dmx512.network;

public record DMXIpDevice(
        String ipAddress,
        Protocol protocol,
        String name,
        int universeCount) {
}

