package be.codewriter.dmx512.network;

public class Status {
    private final boolean connected;
    private final long packetsSent;
    private final long packetsDropped;
    private final double bandwidth;
    private final String lastError;

    public Status(boolean connected, long packetsSent, long packetsDropped,
                  double bandwidth, String lastError) {
        this.connected = connected;
        this.packetsSent = packetsSent;
        this.packetsDropped = packetsDropped;
        this.bandwidth = bandwidth;
        this.lastError = lastError;
    }

    // Getters
    public boolean isConnected() {
        return connected;
    }

    public long getPacketsSent() {
        return packetsSent;
    }

    public long getPacketsDropped() {
        return packetsDropped;
    }

    public double getBandwidth() {
        return bandwidth;
    }

    public String getLastError() {
        return lastError;
    }
}
