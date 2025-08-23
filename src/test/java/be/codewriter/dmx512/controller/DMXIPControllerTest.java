package be.codewriter.dmx512.controller;

import be.codewriter.dmx512.MotherObjects;
import be.codewriter.dmx512.controller.ip.DMXIPController;
import be.codewriter.dmx512.controller.ip.IPProtocol;
import be.codewriter.dmx512.model.DMXUniverse;
import be.codewriter.dmx512.tool.HexTool;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DMXIPControllerTest {

    @Test
    void defaultControllerShouldHaveValidArtNetDataFromClient() throws UnknownHostException {
        var client1 = MotherObjects.fiveChannelClient((byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, 1);
        DMXUniverse universe = new DMXUniverse(1, client1);
        DMXIPController controller = new DMXIPController(InetAddress.getByName("127.0.0.1"));
        var poll = controller.createDataPacket(universe);
        assertEquals("41 72 74 2D 4E 65 74 00 00 50 00 0E 00 00 01 00 00 06 01 02 03 04 05 00", HexTool.toHexString(poll), "Art-Net Data from one client");
    }

    @Test
    void defaultControllerShouldHaveValidArtNetDataFromClients() throws UnknownHostException {
        var client1 = MotherObjects.fiveChannelClient((byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, 1);
        var client2 = MotherObjects.nineChannelClient((byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44, (byte) 0x55, (byte) 0x66, (byte) 0x77, (byte) 0x88, (byte) 0x99, 10);
        DMXUniverse universe = new DMXUniverse(1, List.of(client1, client2));
        DMXIPController controller = new DMXIPController(InetAddress.getByName("127.0.0.1"));
        var poll = controller.createDataPacket(universe);
        assertEquals("41 72 74 2D 4E 65 74 00 00 50 00 0E 00 00 01 00 00 12 01 02 03 04 05 00 00 00 00 11 22 33 44 55 66 77 88 99", HexTool.toHexString(poll), "Art-Net Data from two clients");
    }

    @Test
    void defaultControllerShouldHaveValidArtNetDataFromBytes() throws UnknownHostException {
        var data = new byte[]{(byte) 0x01, (byte) 0x02, (byte) 0x03};
        DMXIPController controller = new DMXIPController(InetAddress.getByName("127.0.0.1"));
        var poll = controller.createDataPacket(1, data);
        assertEquals("41 72 74 2D 4E 65 74 00 00 50 00 0E 00 00 01 00 00 04 01 02 03 00", HexTool.toHexString(poll), "Art-Net Data from bytes");
    }

    @Disabled("SACN is todo...")
    void controllerWithSACNProtocolShouldHaveValidSACNDataFromBytes() throws UnknownHostException {
        var data = new byte[]{(byte) 0x01, (byte) 0x02, (byte) 0x03};
        DMXIPController controller = new DMXIPController(InetAddress.getByName("127.0.0.1"), IPProtocol.SACN);
        var poll = controller.createDataPacket(1, data);
        assertEquals("41 72 74 2D 4E 65 74 00 00 50 00 0E 00 00 00 00 00 03 01 02 03", HexTool.toHexString(poll), "Art-Net Data from bytes");
    }
}
