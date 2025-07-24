# Simple LCD Test for Raspberry Pi Pico
# MicroPython implementation

import machine
import utime
from machine import Pin, I2C

# LCD Configuration
LCD_I2C_ID = 0     # I2C0
LCD_SDA_PIN = 0    # GP0
LCD_SCL_PIN = 1    # GP1
LCD_ADDRESS = 0x27 # Common I2C address for LCD

class I2cLcd:
    def __init__(self, i2c, addr, num_lines, num_columns):
        self.i2c = i2c
        self.addr = addr
        self.num_lines = num_lines
        self.num_columns = num_columns

        print(f"Initializing LCD at address 0x{addr:02x}")

        # Test I2C connection first
        try:
            self.i2c.writeto(self.addr, bytes([0x00]))
            print("LCD responded to I2C")
        except OSError as e:
            print(f"LCD not responding: {e}")
            return

        # LCD initialization sequence
        print("Starting LCD initialization...")
        utime.sleep_ms(50)  # Wait for LCD to power up

        # Initialize in 4-bit mode (standard HD44780 procedure)
        self._write_nibble(0x03)  # Function set: 8-bit (first attempt)
        utime.sleep_ms(5)
        self._write_nibble(0x03)  # Function set: 8-bit (second attempt)
        utime.sleep_ms(1)
        self._write_nibble(0x03)  # Function set: 8-bit (third attempt)
        utime.sleep_ms(1)
        self._write_nibble(0x02)  # Function set: 4-bit mode
        utime.sleep_ms(1)

        # Now in 4-bit mode, send full commands
        self._write_command(0x28)  # Function set: 4-bit, 2-line, 5x8 font
        utime.sleep_ms(1)
        self._write_command(0x08)  # Display off
        utime.sleep_ms(1)
        self._write_command(0x01)  # Clear display
        utime.sleep_ms(2)
        self._write_command(0x06)  # Entry mode: increment cursor
        utime.sleep_ms(1)
        self._write_command(0x0C)  # Display on, cursor off, blink off
        utime.sleep_ms(1)

        print("LCD initialization complete")

    def _write_nibble(self, nibble):
        """Write 4-bit nibble to LCD via I2C expander"""
        # PCF8574 I2C expander connections (common mapping):
        # P7 P6 P5 P4 P3 P2 P1 P0
        # D7 D6 D5 D4 BL EN RW RS

        try:
            # Prepare data: nibble in upper 4 bits, backlight on (P3=1)
            data = (nibble << 4) | 0x08  # Shift nibble left, set backlight bit

            # Pulse Enable (P2): high then low
            self.i2c.writeto(self.addr, bytes([data | 0x04]))  # Enable high
            utime.sleep_us(1)
            self.i2c.writeto(self.addr, bytes([data & ~0x04])) # Enable low
            utime.sleep_us(50)

        except OSError as e:
            print(f"I2C write error: {e}")

    def _write_command(self, cmd):
        """Write command to LCD (RS=0)"""
        # Send upper nibble
        self._write_nibble((cmd >> 4) & 0x0F)
        # Send lower nibble
        self._write_nibble(cmd & 0x0F)

    def _write_data(self, data):
        """Write data to LCD (RS=1)"""
        try:
            # Upper nibble with RS=1
            upper = ((data >> 4) & 0x0F) << 4 | 0x09  # Data + backlight + RS
            self.i2c.writeto(self.addr, bytes([upper | 0x04]))  # Enable high
            utime.sleep_us(1)
            self.i2c.writeto(self.addr, bytes([upper & ~0x04])) # Enable low
            utime.sleep_us(50)

            # Lower nibble with RS=1
            lower = (data & 0x0F) << 4 | 0x09  # Data + backlight + RS
            self.i2c.writeto(self.addr, bytes([lower | 0x04]))  # Enable high
            utime.sleep_us(1)
            self.i2c.writeto(self.addr, bytes([lower & ~0x04])) # Enable low
            utime.sleep_us(50)

        except OSError as e:
            print(f"I2C write error: {e}")

    def clear(self):
        """Clear LCD display"""
        self._write_command(0x01)
        utime.sleep_ms(2)  # Clear command needs extra time

    def home(self):
        """Return cursor to home position"""
        self._write_command(0x02)
        utime.sleep_ms(2)

    def move_to(self, col, row):
        """Move cursor to specific position"""
        if row == 0:
            addr = 0x80 + col  # First line starts at 0x80
        elif row == 1:
            addr = 0xC0 + col  # Second line starts at 0xC0
        else:
            return
        self._write_command(addr)

    def putstr(self, string):
        """Write string to LCD"""
        for char in string:
            self._write_data(ord(char))

    def putchar(self, char):
        """Write single character to LCD"""
        self._write_data(ord(char))

def test_lcd():
    """Test LCD functionality"""
    print("=== LCD Test Starting ===")

    # Initialize I2C with slower speed for reliability
    print("Initializing I2C...")
    i2c = I2C(LCD_I2C_ID, sda=Pin(LCD_SDA_PIN), scl=Pin(LCD_SCL_PIN), freq=100000)

    # Scan for I2C devices
    print("Scanning I2C bus...")
    devices = i2c.scan()
    print(f"Found I2C devices: {[hex(d) for d in devices]}")

    if not devices:
        print("No I2C devices found! Check wiring.")
        return

    # Try to find LCD at common addresses
    lcd_addr = LCD_ADDRESS
    if lcd_addr not in devices:
        print(f"LCD not found at 0x{lcd_addr:02x}")
        # Try common alternatives
        common_addresses = [0x3F, 0x26, 0x20, 0x24, 0x25]
        for addr in common_addresses:
            if addr in devices:
                print(f"Trying address 0x{addr:02x}")
                lcd_addr = addr
                break
        else:
            print("No LCD found at any common address!")
            return

    # Initialize LCD
    print(f"Initializing LCD at address 0x{lcd_addr:02x}")
    lcd = I2cLcd(i2c, lcd_addr, 2, 16)

    # Test sequence
    print("Starting LCD tests...")

    # Test 1: Clear and basic text
    print("Test 1: Basic text")
    lcd.clear()
    lcd.move_to(0, 0)
    lcd.putstr("Hello World!")
    lcd.move_to(0, 1)
    lcd.putstr("LCD Test")
    utime.sleep(3)

    # Test 2: Character positions
    print("Test 2: Character positions")
    lcd.clear()
    for i in range(16):
        lcd.move_to(i, 0)
        lcd.putchar(str(i % 10))
        lcd.move_to(i, 1)
        lcd.putchar(chr(ord('A') + i))
        utime.sleep(0.2)
    utime.sleep(2)

    # Test 3: Scrolling text
    print("Test 3: Scrolling text")
    long_text = "This is a long scrolling message for testing the LCD display!"
    for i in range(len(long_text) - 15):
        lcd.clear()
        lcd.move_to(0, 0)
        lcd.putstr(long_text[i:i+16])
        lcd.move_to(0, 1)
        lcd.putstr(f"Position: {i:02d}")
        utime.sleep(0.3)

    # Test 4: Counter
    print("Test 4: Counter")
    for count in range(20):
        lcd.clear()
        lcd.move_to(0, 0)
        lcd.putstr(f"Counter: {count}")
        lcd.move_to(0, 1)
        timestamp = utime.localtime()
        time_str = f"{timestamp[4]:02d}:{timestamp[5]:02d}"
        lcd.putstr(f"Time: {time_str}")
        utime.sleep(1)

    # Test 5: Final message
    print("Test 5: Final message")
    lcd.clear()
    lcd.move_to(0, 0)
    lcd.putstr("LCD Test")
    lcd.move_to(0, 1)
    lcd.putstr("Complete!")

    print("=== LCD Test Complete ===")

def main():
    """Main function"""
    # Status LED
    led = Pin(25, Pin.OUT)

    try:
        # Blink LED to show we're starting
        for i in range(3):
            led.on()
            utime.sleep(0.2)
            led.off()
            utime.sleep(0.2)

        # Run LCD test
        test_lcd()

        # Success - steady LED
        led.on()
        print("Test completed successfully!")

    except Exception as e:
        print(f"Test failed with error: {e}")
        # Error - fast blinking LED
        for i in range(10):
            led.on()
            utime.sleep(0.1)
            led.off()
            utime.sleep(0.1)

if __name__ == "__main__":
    main()