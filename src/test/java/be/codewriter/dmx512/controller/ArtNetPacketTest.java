package be.codewriter.dmx512.controller;

import be.codewriter.dmx512.MotherObjects;
import be.codewriter.dmx512.controller.ip.packet.ArtNetPacket;
import be.codewriter.dmx512.tool.HexTool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ArtNetPacketTest {

    @Test
    void shouldHaveValidArtNetPoll() {
        var poll = ArtNetPacket.createArtPollPacket();
        assertEquals("41 72 74 2D 4E 65 74 00 00 20 00 0E 00 00", HexTool.toHexString(poll), "Art-Net Poll");
    }

    @Test
    void shouldHaveValidArtNetDataFromClient() {
        var client1 = MotherObjects.fiveChannelClient((byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, 1);
        var poll = ArtNetPacket.createArtNetDMXPacket(client1.getData(), 1);
        assertEquals("41 72 74 2D 4E 65 74 00 00 50 00 0E 00 00 01 00 00 06 01 02 03 04 05 00", HexTool.toHexString(poll), "Art-Net Data from one client");
    }

    @Test
    void shouldHaveValidArtNetDataFromBytes() {
        var data = new byte[]{(byte) 0x01, (byte) 0x02, (byte) 0x03};
        var poll = ArtNetPacket.createArtNetDMXPacket(data, 1);
        assertEquals("41 72 74 2D 4E 65 74 00 00 50 00 0E 00 00 01 00 00 04 01 02 03 00", HexTool.toHexString(poll), "Art-Net Data from bytes");
    }

    @Test
    void shouldCorrectlyExtractDmxDataFromArtNetPacketEvenLength() {
        var client = MotherObjects.sixChannelClient((byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, 1);
        var packet = ArtNetPacket.createArtNetDMXPacket(client.getData(), 0);
        var dataPacket = HexTool.toHexString(packet);
        var dataSent = HexTool.toHexString(client.getData());
        var dataReceived = HexTool.toHexString(ArtNetPacket.extractDmxData(packet));
        assertAll(
                () -> assertEquals("41 72 74 2D 4E 65 74 00 00 50 00 0E 00 00 00 00 00 06 01 02 03 04 05 06", dataPacket),
                () -> assertEquals(dataSent, dataReceived),
                // Art-Net DMX packet is always even-length for DMX data, so length 5 should be padded to 6
                () -> assertEquals(6, ArtNetPacket.getDmxDataLength(packet))
        );
    }

    @Test
    void shouldCorrectlyExtractDmxDataFromArtNetPacketNonEvenLength() {
        var client = MotherObjects.fiveChannelClient((byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, 1);
        var packet = ArtNetPacket.createArtNetDMXPacket(client.getData(), 0);
        var dataPacket = HexTool.toHexString(packet);
        var dataSent = HexTool.toHexString(client.getData());
        var dataReceived = HexTool.toHexString(ArtNetPacket.extractDmxData(packet));
        assertAll(
                () -> assertEquals("41 72 74 2D 4E 65 74 00 00 50 00 0E 00 00 00 00 00 06 01 02 03 04 05 00", dataPacket),
                () -> assertEquals(dataSent, dataReceived),
                // Art-Net DMX packet is always even-length for DMX data, so length 5 should be padded to 6
                () -> assertEquals(6, ArtNetPacket.getDmxDataLength(packet))
        );
    }
}
