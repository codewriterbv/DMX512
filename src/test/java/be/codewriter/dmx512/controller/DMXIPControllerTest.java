package be.codewriter.dmx512.controller;

import be.codewriter.dmx512.MotherObjects;
import be.codewriter.dmx512.controller.ip.DMXIPController;
import be.codewriter.dmx512.helper.DMXMessage;
import be.codewriter.dmx512.tool.HexTool;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DMXIPControllerTest {

    @Test
    void shouldHaveValidArtNetPoll() {
        DMXIPController controller = new DMXIPController();
        var poll = controller.createDetectPaket();
        assertEquals("41 72 74 2D 4E 65 74 00 00 20 00 0E 00 00", HexTool.toHexString(poll), "Art-Net Poll");
    }

    @Test
    void shouldHaveValidArtNetDataFromClient() {
        var client1 = MotherObjects.fiveChannelClient((byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, 1);
        DMXMessage message = new DMXMessage(client1);
        DMXIPController controller = new DMXIPController();
        var poll = controller.createDataPacket(message);
        assertEquals("41 72 74 2D 4E 65 74 00 00 50 00 0E 00 00 01 00 00 06 01 02 03 04 05 00", HexTool.toHexString(poll), "Art-Net Data from one client");
    }

    @Test
    void shouldHaveValidArtNetDataFromClients() {
        var client1 = MotherObjects.fiveChannelClient((byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, 1);
        var client2 = MotherObjects.nineChannelClient((byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44, (byte) 0x55, (byte) 0x66, (byte) 0x77, (byte) 0x88, (byte) 0x99, 10);
        DMXMessage message = new DMXMessage(List.of(client1, client2));
        DMXIPController controller = new DMXIPController();
        var poll = controller.createDataPacket(message);
        assertEquals("41 72 74 2D 4E 65 74 00 00 50 00 0E 00 00 01 00 00 12 01 02 03 04 05 00 00 00 00 11 22 33 44 55 66 77 88 99", HexTool.toHexString(poll), "Art-Net Data from two clients");
    }

    @Test
    void shouldHaveValidArtNetDataFromBytes() {
        DMXMessage message = new DMXMessage(new byte[]{(byte) 0x01, (byte) 0x02, (byte) 0x03});
        DMXIPController controller = new DMXIPController();
        var poll = controller.createDataPacket(message);
        assertEquals("41 72 74 2D 4E 65 74 00 00 50 00 0E 00 00 01 00 00 04 01 02 03 00", HexTool.toHexString(poll), "Art-Net Data from bytes");
    }

    /*
    @Test
    void shouldHaveValidSACNDataFromBytes() {
        DMXMessage message = new DMXMessage(new byte[]{(byte) 0x01, (byte) 0x02, (byte) 0x03});
        DMXIPController controller = new DMXIPController();
        controller.setProtocol(Protocol.SACN);
        controller.setUniverse(1);
        var poll = controller.createDataPacket(message);
        assertEquals("41 72 74 2D 4E 65 74 00 00 50 00 0E 00 00 00 00 00 03 01 02 03", HexTool.toHexString(poll), "Art-Net Data from bytes");
    }
    */
}
