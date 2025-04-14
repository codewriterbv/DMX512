package be.codewriter.dmx512.network;

public record Status(boolean connected,
                     long packetsSent, long packetsDropped, double bandwidth, String lastError) {

}
