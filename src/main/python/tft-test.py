
# Fixed TFT ILI9341 Test - Corrected Mirroring Issue
# MicroPython implementation

import machine
import utime
from machine import Pin, SPI
import framebuf

# TFT ILI9341 Configuration
TFT_SPI_ID = 0
TFT_SCK_PIN = 18   # GP18 - SPI Clock
TFT_MOSI_PIN = 19  # GP19 - SPI MOSI
TFT_CS_PIN = 17    # GP17 - Chip Select
TFT_DC_PIN = 16    # GP16 - Data/Command
TFT_RST_PIN = 20   # GP20 - Reset

class ILI9341:
    def __init__(self, spi, cs, dc, rst, rotation=1):
        self.spi = spi
        self.cs = cs
        self.dc = dc
        self.rst = rst
        self.rotation = rotation

        # Set dimensions based on rotation
        if rotation in [0, 2]:  # Portrait
            self.width = 240
            self.height = 320
        else:  # Landscape
            self.width = 320
            self.height = 240

        print(f"Display size: {self.width}x{self.height}, rotation: {rotation}")

        print("Initializing TFT pins...")
        # Initialize control pins
        self.cs.init(Pin.OUT, value=1)
        self.dc.init(Pin.OUT, value=0)
        self.rst.init(Pin.OUT, value=1)

        print("Creating framebuffer...")
        # Create framebuffer (16-bit RGB565 color)
        self.buffer = bytearray(self.width * self.height * 2)
        self.framebuf = framebuf.FrameBuffer(self.buffer, self.width, self.height, framebuf.RGB565)

        print("Initializing display...")
        self._init_display()
        print("TFT initialization complete!")

    def _write_cmd(self, cmd):
        """Send command to display"""
        self.cs.value(0)
        self.dc.value(0)  # Command mode
        self.spi.write(bytes([cmd]))
        self.cs.value(1)

    def _write_data(self, data):
        """Send data to display"""
        self.cs.value(0)
        self.dc.value(1)  # Data mode
        if isinstance(data, int):
            self.spi.write(bytes([data]))
        else:
            self.spi.write(data)
        self.cs.value(1)

    def _init_display(self):
        """Initialize ILI9341 display"""
        print("Resetting display...")
        # Hardware reset
        self.rst.value(0)
        utime.sleep_ms(10)
        self.rst.value(1)
        utime.sleep_ms(120)

        print("Sending initialization commands...")

        # Software reset
        self._write_cmd(0x01)
        utime.sleep_ms(5)

        # Display off
        self._write_cmd(0x28)

        # Power control A
        self._write_cmd(0xCB)
        self._write_data(bytes([0x39, 0x2C, 0x00, 0x34, 0x02]))

        # Power control B
        self._write_cmd(0xCF)
        self._write_data(bytes([0x00, 0xC1, 0x30]))

        # Driver timing control A
        self._write_cmd(0xE8)
        self._write_data(bytes([0x85, 0x00, 0x78]))

        # Driver timing control B
        self._write_cmd(0xEA)
        self._write_data(bytes([0x00, 0x00]))

        # Power on sequence control
        self._write_cmd(0xED)
        self._write_data(bytes([0x64, 0x03, 0x12, 0x81]))

        # Pump ratio control
        self._write_cmd(0xF7)
        self._write_data(0x20)

        # Power control 1
        self._write_cmd(0xC0)
        self._write_data(0x23)

        # Power control 2
        self._write_cmd(0xC1)
        self._write_data(0x10)

        # VCOM control 1
        self._write_cmd(0xC5)
        self._write_data(bytes([0x3E, 0x28]))

        # VCOM control 2
        self._write_cmd(0xC7)
        self._write_data(0x86)

        # Memory Access Control - FIXED MIRRORING
        self._write_cmd(0x36)
        if self.rotation == 0:    # Portrait
            self._write_data(0x48)    # MY=0, MX=1, MV=0, ML=0, BGR=1, MH=0
        elif self.rotation == 1:  # Landscape (corrected)
            self._write_data(0x28)    # MY=0, MX=0, MV=1, ML=0, BGR=1, MH=0
        elif self.rotation == 2:  # Portrait flipped
            self._write_data(0x88)    # MY=1, MX=0, MV=0, ML=0, BGR=1, MH=0
        elif self.rotation == 3:  # Landscape flipped
            self._write_data(0xE8)    # MY=1, MX=1, MV=1, ML=0, BGR=1, MH=0

        # Pixel Format Set (16-bit color)
        self._write_cmd(0x3A)
        self._write_data(0x55)

        # Frame Rate Control
        self._write_cmd(0xB1)
        self._write_data(bytes([0x00, 0x18]))

        # Display Function Control
        self._write_cmd(0xB6)
        self._write_data(bytes([0x08, 0x82, 0x27]))

        # Gamma Set
        self._write_cmd(0x26)
        self._write_data(0x01)

        # Positive Gamma Correction
        self._write_cmd(0xE0)
        self._write_data(bytes([0x0F, 0x31, 0x2B, 0x0C, 0x0E, 0x08, 0x4E, 0xF1,
                               0x37, 0x07, 0x10, 0x03, 0x0E, 0x09, 0x00]))

        # Negative Gamma Correction
        self._write_cmd(0xE1)
        self._write_data(bytes([0x00, 0x0E, 0x14, 0x03, 0x11, 0x07, 0x31, 0xC1,
                               0x48, 0x08, 0x0F, 0x0C, 0x31, 0x36, 0x0F]))

        # Sleep Out
        self._write_cmd(0x11)
        utime.sleep_ms(120)

        # Display On
        self._write_cmd(0x29)
        utime.sleep_ms(10)

        print("Display commands sent, clearing screen...")
        self.fill(0x0000)  # Clear to black
        self.show()

    def fill(self, color):
        """Fill entire screen with color"""
        self.framebuf.fill(color)

    def pixel(self, x, y, color):
        """Set pixel at x,y to color"""
        if 0 <= x < self.width and 0 <= y < self.height:
            self.framebuf.pixel(x, y, color)

    def text(self, string, x, y, color=0xFFFF):
        """Draw text at position"""
        self.framebuf.text(string, x, y, color)

    def line(self, x1, y1, x2, y2, color):
        """Draw line from (x1,y1) to (x2,y2)"""
        self.framebuf.line(x1, y1, x2, y2, color)

    def rect(self, x, y, w, h, color):
        """Draw rectangle outline"""
        self.framebuf.rect(x, y, w, h, color)

    def fill_rect(self, x, y, w, h, color):
        """Draw filled rectangle"""
        self.framebuf.fill_rect(x, y, w, h, color)

    def show(self):
        """Update display with framebuffer contents"""
        # Set column address (X coordinates)
        self._write_cmd(0x2A)  # Column Address Set
        self._write_data(bytes([0x00, 0x00, (self.width-1) >> 8, (self.width-1) & 0xFF]))

        # Set page address (Y coordinates)
        self._write_cmd(0x2B)  # Page Address Set
        self._write_data(bytes([0x00, 0x00, (self.height-1) >> 8, (self.height-1) & 0xFF]))

        # Start memory write
        self._write_cmd(0x2C)  # Memory Write

        # Send framebuffer data
        self.cs.value(0)
        self.dc.value(1)  # Data mode
        self.spi.write(self.buffer)
        self.cs.value(1)

def test_orientation():
    """Test display orientation with clear directional indicators"""
    print("=== Testing Display Orientation ===")

    # Initialize SPI
    spi = SPI(TFT_SPI_ID, baudrate=40000000,
              sck=Pin(TFT_SCK_PIN), mosi=Pin(TFT_MOSI_PIN))

    # Create display in landscape mode
    tft = ILI9341(spi, Pin(TFT_CS_PIN), Pin(TFT_DC_PIN), Pin(TFT_RST_PIN), rotation=1)

    # Colors
    BLACK = 0x0000
    WHITE = 0xFFFF
    RED = 0xF800
    GREEN = 0x07E0
    BLUE = 0x001F
    YELLOW = 0xFFE0

    print("Drawing orientation test pattern...")

    # Clear screen
    tft.fill(BLACK)

    # Draw arrow pointing RIGHT (should point to the actual right side)
    arrow_x = 50
    arrow_y = tft.height // 2

    # Arrow body (horizontal line)
    tft.fill_rect(arrow_x, arrow_y - 5, 100, 10, WHITE)

    # Arrow head (triangle pointing right)
    for i in range(20):
        tft.line(arrow_x + 100, arrow_y - i, arrow_x + 100 + i, arrow_y, WHITE)
        tft.line(arrow_x + 100, arrow_y + i, arrow_x + 100 + i, arrow_y, WHITE)

    # Label the arrow
    tft.text("RIGHT", arrow_x + 130, arrow_y - 5, YELLOW)

    # Draw corner labels (these should appear in correct corners)
    tft.text("TOP-LEFT", 5, 5, RED)
    tft.text("TOP-RIGHT", tft.width - 80, 5, GREEN)
    tft.text("BTM-LEFT", 5, tft.height - 15, BLUE)
    tft.text("BTM-RIGHT", tft.width - 80, tft.height - 15, YELLOW)

    # Draw border
    tft.rect(0, 0, tft.width, tft.height, WHITE)

    # Center info
    center_x = tft.width // 2
    center_y = tft.height // 2
    tft.text("CENTER", center_x - 30, center_y + 30, WHITE)
    tft.fill_rect(center_x - 2, center_y - 2, 4, 4, RED)  # Center dot

    # Display info
    tft.text(f"{tft.width}x{tft.height}", 10, 30, WHITE)
    tft.text("Landscape Mode", 10, 50, WHITE)

    tft.show()

    print("Test pattern displayed!")
    print("Check if:")
    print("- Arrow points to the RIGHT side of the screen")
    print("- Corner labels are in the correct corners")
    print("- Text reads normally (not mirrored)")
    print("- Full screen area is used")

def test_text_direction():
    """Test text reading direction"""
    print("=== Testing Text Direction ===")

    spi = SPI(TFT_SPI_ID, baudrate=40000000,
              sck=Pin(TFT_SCK_PIN), mosi=Pin(TFT_MOSI_PIN))

    tft = ILI9341(spi, Pin(TFT_CS_PIN), Pin(TFT_DC_PIN), Pin(TFT_RST_PIN), rotation=1)

    # Define all colors needed
    BLACK = 0x0000
    WHITE = 0xFFFF
    RED = 0xF800
    GREEN = 0x07E0
    YELLOW = 0xFFE0

    tft.fill(BLACK)

    # Test normal text reading
    tft.text("ABCDEFGHIJKLMNOP", 10, 20, WHITE)
    tft.text("0123456789", 10, 40, RED)
    tft.text("This text should", 10, 60, GREEN)
    tft.text("read normally", 10, 80, GREEN)
    tft.text("from LEFT to RIGHT", 10, 100, GREEN)

    # Test numbers in sequence
    for i in range(10):
        tft.text(str(i), 10 + i * 25, 140, YELLOW)

    # Alphabet test
    alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    for i, char in enumerate(alphabet[:20]):  # First 20 chars
        tft.text(char, 10 + i * 15, 180, WHITE)

    tft.text("Text Direction Test", 50, 200, RED)

    tft.show()

    print("Text direction test displayed!")
    print("All text should read normally from left to right")

def main():
    """Main function"""
    led = Pin(25, Pin.OUT)

    try:
        # Blink LED to show we're starting
        for i in range(3):
            led.on()
            utime.sleep(0.2)
            led.off()
            utime.sleep(0.2)

        print("Running orientation test...")
        test_orientation()
        utime.sleep(5)

        print("Running text direction test...")
        test_text_direction()

        # Success - steady LED
        led.on()
        print("Tests completed successfully!")

        while True:
            utime.sleep(1)

    except Exception as e:
        print(f"Test failed with error: {e}")
        import sys
        sys.print_exception(e)

        # Error - fast blinking LED
        for i in range(20):
            led.on()
            utime.sleep(0.1)
            led.off()
            utime.sleep(0.1)

if __name__ == "__main__":
    main()