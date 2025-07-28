import machine
import select
import sys
import time


class DMX512Receiver:
    def __init__(self, uart_id=0, rx_pin=1, baudrate=250000):
        """
        Initialize DMX512 receiver for DollaTek TTL to RS485 (auto direction control)

        Args:
            uart_id: UART interface number (0 or 1)
            rx_pin: GPIO pin for UART RX
            baudrate: DMX512 standard is 250,000 baud
        """
        self.uart = machine.UART(uart_id, baudrate=baudrate, bits=8, parity=None, stop=2, rx=rx_pin)

        # DMX512 universe - 512 channels + start code
        self.dmx_data = bytearray(513)
        self.last_dmx_data = bytearray(513)

        # Reception state tracking
        self.frame_count = 0
        self.error_count = 0
        self.last_frame_time = 0
        self.receiving_frame = False
        self.bytes_received = 0

        # Buffer for incoming data
        self.rx_buffer = bytearray()

        print(f"DMX512 Receiver initialized on UART{uart_id}, RX pin GPIO{rx_pin}")
        print(f"Baudrate: {baudrate}, waiting for DMX data...")

    def clear_uart_buffer(self):
        """Clear any existing data in UART buffer"""
        while self.uart.any():
            self.uart.read(1)

    def detect_break(self, timeout_ms=50):
        """
        Detect DMX BREAK signal by looking for extended low period
        Returns True if break detected, False if timeout
        """
        start_time = time.ticks_ms()

        while time.ticks_diff(time.ticks_ms(), start_time) < timeout_ms:
            if self.uart.any():
                data = self.uart.read(1)
                if data and data[0] == 0x00:
                    # Potential start of frame (could be start code or break)
                    return True
            time.sleep_ms(1)

        return False

    def read_dmx_frame(self):
        """
        Read a complete DMX frame
        Returns True if frame successfully received, False otherwise
        """
        try:
            # Clear any stale data
            self.clear_uart_buffer()

            # Wait for data with timeout
            poll = select.poll()
            poll.register(self.uart, select.POLLIN)

            # Wait up to 100ms for data
            result = poll.poll(100)
            if not result:
                return False

            # Read available data
            if self.uart.any():
                data = self.uart.read()
                if not data:
                    return False

                # Look for start code (0x00) at beginning
                if len(data) > 0 and data[0] == 0x00:
                    # Valid DMX frame should start with 0x00
                    frame_data = data[:513]  # Limit to max DMX frame size

                    # Copy to our DMX data buffer
                    for i, byte_val in enumerate(frame_data):
                        if i < 513:
                            self.dmx_data[i] = byte_val

                    self.bytes_received = len(frame_data)
                    self.frame_count += 1
                    self.last_frame_time = time.ticks_ms()

                    return True

            poll.unregister(self.uart)
            return False

        except Exception as e:
            self.error_count += 1
            print(f"Error reading DMX frame: {e}")
            return False

    def log_dmx_data(self, max_channels=50, show_all=False):
        """
        Log received DMX data as readable hex string

        Args:
            max_channels: Number of channels to display
            show_all: If True, show all channels regardless of value
        """
        if self.bytes_received == 0:
            print("No DMX data received yet")
            return

        print(f"\n=== DMX Frame #{self.frame_count} ===")
        print(f"Bytes received: {self.bytes_received}")
        print(f"Start code: 0x{self.dmx_data[0]:02X}")

        # Show hex dump of first N channels
        if max_channels > 0:
            end_channel = min(max_channels + 1, self.bytes_received)
            hex_data = ' '.join(f'{b:02X}' for b in self.dmx_data[:end_channel])
            print(f"First {end_channel-1} channels: {hex_data}")

        # Show active channels (non-zero values)
        active_channels = []
        for i in range(1, min(self.bytes_received, 513)):
            if self.dmx_data[i] > 0 or show_all:
                active_channels.append(f"Ch{i}={self.dmx_data[i]:02X}({self.dmx_data[i]})")

        if active_channels:
            print(f"Active channels: {', '.join(active_channels[:20])}")  # Limit to first 20
            if len(active_channels) > 20:
                print(f"... and {len(active_channels) - 20} more")
        else:
            print("No active channels (all zero)")

    def log_changes_only(self):
        """
        Log only channels that have changed since last frame
        """
        changes = []
        for i in range(min(len(self.dmx_data), len(self.last_dmx_data))):
            if self.dmx_data[i] != self.last_dmx_data[i]:
                if i == 0:
                    changes.append(f"StartCode: {self.last_dmx_data[i]:02X}→{self.dmx_data[i]:02X}")
                else:
                    changes.append(f"Ch{i}: {self.last_dmx_data[i]}→{self.dmx_data[i]}")

        if changes:
            print(f"Frame #{self.frame_count} Changes: {', '.join(changes[:10])}")
            if len(changes) > 10:
                print(f"... and {len(changes) - 10} more changes")

        # Update last frame data
        self.last_dmx_data[:len(self.dmx_data)] = self.dmx_data[:len(self.dmx_data)]

    def get_rgb_channels(self, start_channel):
        """
        Get RGB values from specific channels

        Args:
            start_channel: First channel for Red

        Returns:
            Tuple of (red, green, blue) values
        """
        if start_channel + 2 < len(self.dmx_data):
            r = self.dmx_data[start_channel]
            g = self.dmx_data[start_channel + 1]
            b = self.dmx_data[start_channel + 2]
            return (r, g, b)
        return (0, 0, 0)

    def print_stats(self):
        """Print reception statistics"""
        current_time = time.ticks_ms()
        if self.last_frame_time > 0:
            time_since_last = time.ticks_diff(current_time, self.last_frame_time)
            print(f"\nStats: Frames={self.frame_count}, Errors={self.error_count}, Last frame {time_since_last}ms ago")
        else:
            print(f"\nStats: Frames={self.frame_count}, Errors={self.error_count}, No frames received yet")

def monitor_dmx_simple():
    """
    Simple DMX monitor - logs all received frames
    """
    receiver = DMX512Receiver(uart_id=0, rx_pin=1)

    print("=== Simple DMX Monitor ===")
    print("Listening for DMX512 data... Press Ctrl+C to stop")

    try:
        while True:
            if receiver.read_dmx_frame():
                receiver.log_dmx_data(max_channels=30)
                print("-" * 50)
            else:
                # No data received, brief pause
                time.sleep_ms(50)

    except KeyboardInterrupt:
        print("\nStopping DMX monitor...")
        receiver.print_stats()

def monitor_dmx_changes():
    """
    DMX monitor that only shows changes between frames
    """
    receiver = DMX512Receiver(uart_id=0, rx_pin=1)

    print("=== DMX Change Monitor ===")
    print("Listening for DMX512 changes... Press Ctrl+C to stop")

    try:
        while True:
            if receiver.read_dmx_frame():
                receiver.log_changes_only()
            else:
                time.sleep_ms(50)

    except KeyboardInterrupt:
        print("\nStopping DMX change monitor...")
        receiver.print_stats()

def monitor_rgb_fixture(rgb_start_channel=23):
    """
    Monitor specific RGB fixture channels

    Args:
        rgb_start_channel: Starting channel for RGB fixture
    """
    receiver = DMX512Receiver(uart_id=0, rx_pin=1)

    print(f"=== RGB Fixture Monitor (Channels {rgb_start_channel}-{rgb_start_channel+2}) ===")
    print("Monitoring RGB values... Press Ctrl+C to stop")

    last_rgb = (0, 0, 0)

    try:
        while True:
            if receiver.read_dmx_frame():
                current_rgb = receiver.get_rgb_channels(rgb_start_channel)

                if current_rgb != last_rgb:
                    r, g, b = current_rgb
                    print(f"Frame #{receiver.frame_count}: RGB({r:3d}, {g:3d}, {b:3d}) | Hex(0x{r:02X}, 0x{g:02X}, 0x{b:02X})")
                    last_rgb = current_rgb
            else:
                time.sleep_ms(50)

    except KeyboardInterrupt:
        print("\nStopping RGB fixture monitor...")
        receiver.print_stats()

def debug_raw_uart():
    """
    Debug function to show raw UART data
    """
    uart = machine.UART(0, baudrate=250000, bits=8, parity=None, stop=2, rx=1)

    print("=== Raw UART Debug ===")
    print("Showing raw UART data... Press Ctrl+C to stop")

    try:
        byte_count = 0
        while True:
            if uart.any():
                data = uart.read()
                if data:
                    hex_data = ' '.join(f'{b:02X}' for b in data)
                    print(f"Bytes {byte_count}-{byte_count+len(data)-1}: {hex_data}")
                    byte_count += len(data)
            else:
                time.sleep_ms(10)

    except KeyboardInterrupt:
        print(f"\nReceived {byte_count} total bytes")

def test_loopback():
    """
    Test function that can work with the transmitter code running on another Pico
    """
    receiver = DMX512Receiver(uart_id=0, rx_pin=1)

    print("=== DMX Loopback Test ===")
    print("Connect transmitter A+/B- to receiver A+/B-")
    print("Monitoring for test patterns... Press Ctrl+C to stop")

    pattern_colors = {
        (255, 0, 0): "Red",
        (0, 255, 0): "Green",
        (0, 0, 255): "Blue",
        (255, 255, 0): "Yellow",
        (255, 0, 255): "Magenta",
        (0, 255, 255): "Cyan",
        (255, 255, 255): "White",
        (0, 0, 0): "Off"
    }

    try:
        while True:
            if receiver.read_dmx_frame():
                rgb = receiver.get_rgb_channels(23)  # Monitor channels 23-25
                color_name = pattern_colors.get(rgb, f"Custom RGB{rgb}")

                print(f"Frame #{receiver.frame_count}: {color_name} - RGB{rgb}")

                # Show frame rate
                if receiver.frame_count % 40 == 0:  # Every ~1 second at 40Hz
                    receiver.print_stats()
            else:
                time.sleep_ms(25)

    except KeyboardInterrupt:
        print("\nStopping loopback test...")
        receiver.print_stats()

# Run the selected test
if __name__ == "__main__":
    # Uncomment the test you want to run:
    monitor_dmx_simple()        # Show all received DMX frames
    # monitor_dmx_changes()     # Show only changes between frames
    # monitor_rgb_fixture(23)   # Monitor RGB fixture on channels 23-25
    # debug_raw_uart()          # Show raw UART data
    # test_loopback()           # Test with transmitter running
