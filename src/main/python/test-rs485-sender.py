import machine
import time
import array

class DMX512:
    def __init__(self, uart_id=0, tx_pin=0, baudrate=250000):
        """
        Initialize DMX512 controller for DollaTek TTL to RS485 (auto direction control)

        Args:
            uart_id: UART interface number (0 or 1)
            tx_pin: GPIO pin for UART TX
            baudrate: DMX512 standard is 250,000 baud
        """
        self.uart = machine.UART(uart_id, baudrate=baudrate, bits=8, parity=None, stop=2, tx=tx_pin)

        # DMX512 universe - 512 channels + start code
        self.dmx_data = bytearray(513)
        self.dmx_data[0] = 0  # Start code for dimmer data

        # Initialize all channels to 0
        for i in range(1, 513):
            self.dmx_data[i] = 0

    def set_channel(self, channel, value):
        """
        Set a DMX channel value

        Args:
            channel: DMX channel (1-512)
            value: Channel value (0-255)
        """
        if 1 <= channel <= 512:
            self.dmx_data[channel] = max(0, min(255, value))

    def set_rgb(self, start_channel, red, green, blue, dimmer):
        """
        Set RGB values starting at specified channel

        Args:
            start_channel: First channel for Red
            red: Red value (0-255)
            green: Green value (0-255)
            blue: Blue value (0-255)
        """
        self.set_channel(start_channel, red)
        self.set_channel(start_channel + 1, green)
        self.set_channel(start_channel + 2, blue)
        self.set_channel(start_channel + 3, dimmer)


    def log_dmx_data(self, max_channels=50):
        """
        Log DMX data as readable hex string

        Args:
            max_channels: Number of channels to display (default 50 to avoid spam)
        """
        # Convert to hex string with formatting
        hex_data = ' '.join(f'{b:02X}' for b in self.dmx_data[:max_channels + 1])
        print(f"DMX Data (first {max_channels} channels): {hex_data}")

        # Show specific channels that are non-zero
        active_channels = []
        for i in range(1, min(len(self.dmx_data), 513)):
            if self.dmx_data[i] > 0:
                active_channels.append(f"Ch{i}={self.dmx_data[i]:02X}({self.dmx_data[i]})")

        if active_channels:
            print(f"Active channels: {', '.join(active_channels)}")

    def send_dmx(self):
        """
        Send DMX512 frame (auto direction control version)
        """
        # Send BREAK (minimum 88us at 250k baud)
        # Note: MicroPython's UART break might not work perfectly on all versions
        # If break doesn't work, we'll use a workaround
        try:
            self.uart.sendbreak()
            time.sleep_us(100)  # BREAK duration
        except:
            # Workaround: send null bytes at lower baud rate to simulate break
            self.uart.init(baudrate=50000, bits=8, parity=None, stop=2)
            self.uart.write(b'\x00')
            time.sleep_us(100)
            # Restore normal baud rate
            self.uart.init(baudrate=250000, bits=8, parity=None, stop=2)

        # Send MARK AFTER BREAK (minimum 8us)
        time.sleep_us(12)  # MAB duration

        # Send data
        self.uart.write(self.dmx_data)
        # self.log_dmx_data()

        # Small delay to ensure transmission completes
        # The DollaTek module handles direction automatically
        time.sleep_us(50)

def main():
    # Initialize DMX controller (no DE pin needed)
    dmx = DMX512(uart_id=0, tx_pin=0)

    print("DMX512 RGB Test - Channel 23-25")
    print("Sending test patterns to RGB fixture...")

    # Test patterns
    test_patterns = [
        (255, 0, 0),    # Red
        (0, 255, 0),    # Green
        (0, 0, 255),    # Blue
        (255, 255, 0),  # Yellow
        (255, 0, 255),  # Magenta
        (0, 255, 255),  # Cyan
        (255, 255, 255), # White
        (128, 64, 192), # Purple
        (255, 165, 0),  # Orange
        (0, 0, 0)       # Off
    ]

    pattern_names = [
        "Red", "Green", "Blue", "Yellow", "Magenta",
        "Cyan", "White", "Purple", "Orange", "Off"
    ]

    try:
        while True:
            for i, (r, g, b) in enumerate(test_patterns):
                print(f"Setting RGB to {pattern_names[i]}: R={r}, G={g}, B={b}")

                # Set RGB values on channels 23, 24, 25
                dmx.set_rgb(23, r, g, b, 255)

                # Send DMX frame continuously for 2 seconds
                start_time = time.time()
                while time.time() - start_time < 2.0:
                    dmx.send_dmx()
                    time.sleep_ms(25)  # ~40Hz refresh rate

                time.sleep(0.5)  # Brief pause between colors

    except KeyboardInterrupt:
        print("\nStopping DMX transmission...")
        # Turn off all channels
        dmx.set_rgb(23, 0, 0, 0)
        for _ in range(10):  # Send off command multiple times
            dmx.send_dmx()
            time.sleep_ms(25)
        print("DMX transmission stopped.")

def test_single_color():
    """
    Simple test function to set a single color
    """
    dmx = DMX512(uart_id=0, tx_pin=0)

    # Set bright red on channel 23-25
    dmx.set_rgb(23, 255, 0, 0)

    print("Sending bright red to channels 23-25...")
    for _ in range(200):  # Send for ~5 seconds at 40Hz
        dmx.send_dmx()
        time.sleep_ms(25)

    # Turn off
    dmx.set_rgb(23, 0, 0, 0)
    for _ in range(10):
        dmx.send_dmx()
        time.sleep_ms(25)

    print("Test complete.")

def rainbow_effect():
    """
    Create a smooth rainbow effect
    """
    import math

    dmx = DMX512(uart_id=0, tx_pin=0)

    print("Starting rainbow effect on channels 23-25...")

    try:
        hue = 0
        while True:
            # Convert HSV to RGB
            h = hue / 360.0
            s = 1.0
            v = 1.0

            i = int(h * 6.0)
            f = (h * 6.0) - i
            p = v * (1.0 - s)
            q = v * (1.0 - s * f)
            t = v * (1.0 - s * (1.0 - f))

            if i == 0:
                r, g, b = v, t, p
            elif i == 1:
                r, g, b = q, v, p
            elif i == 2:
                r, g, b = p, v, t
            elif i == 3:
                r, g, b = p, q, v
            elif i == 4:
                r, g, b = t, p, v
            else:
                r, g, b = v, p, q

            # Convert to 0-255 range
            red = int(r * 255)
            green = int(g * 255)
            blue = int(b * 255)

            dmx.set_rgb(23, red, green, blue, 255)
            dmx.send_dmx()

            hue = (hue + 2) % 360  # Increment hue
            time.sleep_ms(50)  # 20Hz update rate

    except KeyboardInterrupt:
        print("\nStopping rainbow effect...")
        dmx.set_rgb(23, 0, 0, 0, 255)
        for _ in range(10):
            dmx.send_dmx()
            time.sleep_ms(25)
        print("Rainbow effect stopped.")

# Run the main test
if __name__ == "__main__":
    # Uncomment the test you want to run:
    main()                # Full test pattern cycle
    # test_single_color() # Single red color test
    # rainbow_effect()    # Smooth rainbow effect
