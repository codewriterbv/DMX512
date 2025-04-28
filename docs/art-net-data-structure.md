# Art-Net Data Structure

Art-Net uses a specific packet structure to encapsulate DMX data for transmission over IP networks. Here's how the data is structured:

## Art-Net Packet Format

An Art-Net packet consists of a header followed by the DMX data payload:

### Header Components
- **ID String**: Always "Art-Net" followed by a null byte (8 bytes total)
- **OpCode**: 16-bit value identifying packet type (e.g., 0x5000 for ArtDmx data)
- **Protocol Version**: 16-bit value (current version is 14)
- **Sequence**: 8-bit value for packet ordering (can be 0 if not used)
- **Physical**: 8-bit port from which data originated
- **Universe**: 15-bit value identifying the DMX universe (0-32767)
  - Commonly split into Subnet (4 bits) and Universe (4 bits) in older implementations
- **Length**: 16-bit value indicating length of the following DMX data (1-512)

### DMX Data Payload
- Contains up to 512 bytes of DMX channel data
- Each byte represents the value for one DMX channel (0-255)

## Simplified Art-Net Packet Structure
```
0                   1                   2                   3
0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|      'A'      |      'r'      |      't'      |      '-'      |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|      'N'      |      'e'      |      't'      |       0       |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|    OpCode Low |   OpCode High |  ProtVer Low  |  ProtVer High |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|    Sequence   |    Physical   |   Universe Low|  Universe High|
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|    Length Low |   Length High |                               |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+                               |
|                                                               |
|                      DMX512 Data (variable)                   |
|                                                               |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
```

## Network Transport Details

- Art-Net packets are typically sent via UDP
- Default UDP port is 6454
- Broadcast address is typically used for discovery (e.g., 2.255.255.255)
- Unicast can be used for directed communication

## Important Operation Codes (OpCodes)

- **ArtDmx (0x5000)**: DMX data packet
- **ArtPoll (0x2000)**: Discovery packet
- **ArtPollReply (0x2100)**: Response to discovery
- **ArtIpProg (0x5800)**: Program IP settings
- **ArtAddress (0x6000)**: Configure nodes
- **ArtTimeCode (0x9700)**: SMPTE time code
- **ArtCommand (0x2400)**: Text command

## Universe Addressing

The 15-bit universe identifier is conceptually organized as:
- Net (7 bits): 0-127
- Subnet (4 bits): 0-15 
- Universe (4 bits): 0-15

This allows for a total of 128 × 16 × 16 = 32,768 universes.

## Practical Implementation Tips

- Always check the OpCode to determine the packet type
- Verify the ID string is "Art-Net" followed by null
- Most controllers only need to implement ArtDmx (0x5000) to send data
- For receiving, implement ArtPoll response mechanisms
- Use sequence numbers for time-critical applications
- Be aware of byte order (Art-Net is big-endian for multi-byte values)
