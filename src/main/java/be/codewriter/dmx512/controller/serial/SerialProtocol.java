package be.codewriter.dmx512.controller.serial;

/**
 * Available serial protocol types
 */
public enum SerialProtocol {
    /**
     * Simple serial transmission
     */
    OPEN_DMX_USB,
    /**
     * Direct FTDI chip communication
     */
    FTDI_CHIP_DIRECT,
    /**
     * Enttec Open DMX USB (FTDI-based)
     */
    ENTTEC_OPEN_DMX,
    /**
     * Generic serial-based DMX
     */
    GENERIC_SERIAL
}
