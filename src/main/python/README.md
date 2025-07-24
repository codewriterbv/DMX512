# DMX Test Application

Inspired by:

* [Raspberry Pi Pico W DMX Controller and DMX Quick Start for Beginners](https://www.instructables.com/Raspberry-Pi-Pico-W-DMX-Controller-and-DMX-Quick-S/)
* [Source code on GitHub](https://github.com/john4242/artnet-pico-gateway)

RS485 to Pico interface:

* Amazon: [DollaTek RS485 Converter Module Arduino](https://www.amazon.com.be/-/en/DollaTek-RS485-Converter-Module-Arduino/dp/B099DRKBGQ/ref=sr_1_10)

## Wiring for DollaTek Module:

### DollaTek TTL to RS485

- VCC → 3.3V (or 5V if your module requires it)
- GND → GND
- RXD → GP4 (Pico TX)
- TXD → GP5 (Pico RX)
- A/B terminals → Your RS485 network

### I2C LCD

- VCC → 3.3V or 5V
- GND → GND
- SDA → GP0
- SCL → GP1

### ILI9341 TFT Module

- VCC → 3.3V
- GND → GND
- CS → GP17
- RESET → GP20
- DC → GP16
- SDI(MOSI) → GP19
- SCK → GP18
- LED → 3.3V (backlight)


![](raspberry-pi-pico-gpio.png)

## Prepare Pico for MicroPython

* Download the correct MicroPython UF2 file for your board from the [Raspberry Pi website](https://www.raspberrypi.com/documentation/microcontrollers/micropython.html).
* Restart the Pico while pressing "BOOTSEL".
* Drop the downloaded UF2 file on the Pico device that appears on your computer.

## Programm the Pico

* [Download](https://thonny.org/) and open Thonny IDE.
* Select the "RP2024" in the lower right corner.
* Copy `main.py` to the Pico with Thonny.


