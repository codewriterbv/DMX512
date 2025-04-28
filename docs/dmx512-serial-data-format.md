# DMX512 Serial Data Format

The DMX512 data that gets encapsulated in Art-Net packets follows a specific serial format established by the DMX512 standard. Here's a detailed explanation of this data format:

## DMX512 Frame Structure

A complete DMX512 frame consists of:

1. **Break**: A low signal (logical 0) held for at least 88 microseconds
2. **Mark After Break (MAB)**: A high signal (logical 1) for at least 8 microseconds
3. **Start Code**: A single byte indicating the type of data that follows
4. **DMX Channel Data**: Up to 512 bytes, one byte per channel
5. **Mark Time Between Frames (MTBF)**: Optional idle time between frames

## Start Code

- The Start Code is typically 0x00 for standard DMX lighting control
- Other values indicate special data types (e.g., 0xCC for RDM, 0x01 for text)
- In Art-Net, the Start Code is usually assumed to be 0x00 and not explicitly included

## DMX Channel Data

- Each channel is represented by a single byte (8 bits)
- Values range from 0 to 255 (0x00 to 0xFF)
- Channel 1 is sent first, followed sequentially up to a maximum of 512 channels
- For fixtures, these values typically represent:
  - Intensity/dimmer levels
  - Color values (RGB/CMY)
  - Position data (pan/tilt)
  - Effect parameters (strobe, gobo, etc.)

## Slot Format (Individual Channel Data)

Each DMX channel (slot) follows the standard asynchronous serial format:
- 1 start bit (logical 0)
- 8 data bits (least significant bit first)
- 2 stop bits (logical 1)
- No parity bit

## Timing Specifications

- Standard DMX512 uses a baud rate of 250 kbps
- Minimum break time: 88 μs
- Minimum MAB time: 8 μs
- Maximum packet time (full universe): ~23 ms
- Maximum refresh rate (full universe): ~44 Hz

## Art-Net Implementation

When DMX512 data is encapsulated in Art-Net:
1. The Break and MAB are not transmitted (they're only used in physical DMX512)
2. The Start Code is typically omitted (assumed to be 0x00)
3. Only the channel data (up to 512 bytes) is included in the Art-Net packet
4. The Length field in the Art-Net header specifies how many DMX channels are included

The DMX data simply appears as a sequence of bytes in the Art-Net packet, with each byte representing the value for the corresponding DMX channel, starting from channel 1.
