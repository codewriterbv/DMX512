package be.codewriter.dmx512.helper;

import be.codewriter.dmx512.MotherObjects;
import org.junit.jupiter.api.Test;

import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DMXMessageTest {

    @Test
    void shouldReturnData() {
        var client1 = MotherObjects.fiveChannelClient((byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, 1);
        var client2 = MotherObjects.nineChannelClient((byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44, (byte) 0x55, (byte) 0x66, (byte) 0x77, (byte) 0x88, (byte) 0x99, 10);

        var dmxMessage = new DMXMessage(List.of(client1, client2));
        var data = dmxMessage.getData();
        System.out.printf("Data: " + HexFormat.of().formatHex(data));

        assertAll(
                () -> assertEquals((byte) 0, data[0], "Alignment byte"),
                () -> assertEquals(19, data.length, "Data length"),

                // Client 1
                () -> assertEquals((byte) 0x01, data[client1.getStartChannel()], "Client 1: Red"),
                () -> assertEquals((byte) 0x02, data[client1.getStartChannel() + 1], "Client 1: Green"),
                () -> assertEquals((byte) 0x03, data[client1.getStartChannel() + 2], "Client 1: Blue"),
                () -> assertEquals((byte) 0x04, data[client1.getStartChannel() + 3], "Client 1: Dimmer"),
                () -> assertEquals((byte) 0x05, data[client1.getStartChannel() + 4], "Client 1: Effects"),

                // Client 2
                () -> assertEquals((byte) 0x11, data[client2.getStartChannel()], "Client 2: Red"),
                () -> assertEquals((byte) 0x22, data[client2.getStartChannel() + 1], "Client 2: Green"),
                () -> assertEquals((byte) 0x33, data[client2.getStartChannel() + 2], "Client 2: Blue"),
                () -> assertEquals((byte) 0x44, data[client2.getStartChannel() + 3], "Client 2: Dimmer"),
                () -> assertEquals((byte) 0x55, data[client2.getStartChannel() + 4], "Client 2: Effects"),
                () -> assertEquals((byte) 0x66, data[client2.getStartChannel() + 5], "Client 2: Pan"),
                () -> assertEquals((byte) 0x77, data[client2.getStartChannel() + 6], "Client 2: Tilt"),
                () -> assertEquals((byte) 0x88, data[client2.getStartChannel() + 7], "Client 2: Speed"),
                () -> assertEquals((byte) 0x99, data[client2.getStartChannel() + 8], "Client 2: Gobo")
        );
    }
}
