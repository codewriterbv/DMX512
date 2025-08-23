import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Java port of the Node.js OpenDMX class
 * Controls DMX lighting devices through a stream-based interface
 */
public class OpenDMX {

    // DMX constants
    private static final int DMX_CHANNELS = 513; // 1 start byte + 512 channels
    private static final int MAX_DMX_CHANNEL = 512;
    private static final int MIN_DMX_CHANNEL = 1;

    // Instance variables
    private String streamDevice;
    private FileOutputStream stream;
    private byte[] buffer;
    private List<DeviceEntry> devices;

    /**
     * Constructor - equivalent to Node.js constructor
     *
     * @param streamDevice The device path (default: "/dev/dmx0")
     */
    public OpenDMX(String streamDevice) {
        this.streamDevice = streamDevice != null ? streamDevice : "/dev/dmx0";
        initializeStream();
        initializeBuffer();
        this.devices = new ArrayList<>();
    }

    /**
     * Default constructor using "/dev/dmx0"
     */
    public OpenDMX() {
        this("/dev/dmx0");
    }

    /**
     * Example usage and testing
     */
    public static void main(String[] args) {
        // Create OpenDMX instance
        OpenDMX dmx = new OpenDMX("/dev/dmx0");

        // Create some example devices
        RGBLight light1 = new RGBLight();
        RGBLight light2 = new RGBLight();
        Dimmer dimmer1 = new Dimmer();

        // Set some colors
        light1.setColor(255, 0, 0);    // Red
        light2.setColor(0, 255, 0);    // Green
        dimmer1.setIntensity(128);     // 50% intensity

        // Add devices to DMX universe
        dmx.addDevice(light1, 1);      // Channels 1-3
        dmx.addDevice(light2, 4);      // Channels 4-6
        dmx.addDevice(dimmer1, 7);     // Channel 7

        // Render and send to hardware
        dmx.render();

        System.out.println("DMX frame sent with " + dmx.getDeviceCount() + " devices");

        // Example of changing values and re-rendering
        try {
            Thread.sleep(1000);
            light1.setColor(0, 0, 255);  // Change to blue
            dmx.render();
            System.out.println("Updated light1 to blue");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Clean up
        dmx.close();
    }

    /**
     * Initialize the output stream to the DMX device
     */
    private void initializeStream() {
        try {
            this.stream = new FileOutputStream(streamDevice);
        } catch (FileNotFoundException e) {
            System.err.println("Error: Could not open DMX device: " + streamDevice);
            System.err.println("Make sure the device exists and you have write permissions.");
            // In a production environment, you might want to throw a custom exception here
        }
    }

    /**
     * Initialize the DMX frame buffer
     */
    private void initializeBuffer() {
        // Initialize DMX frame buffer (513 bytes total)
        this.buffer = new byte[DMX_CHANNELS];
        // DMX512 specification requires the first byte to be zero
        // Following 512 bytes are for controlling channels
        this.buffer[0] = 0x00;
        // Initialize all other bytes to 0 (channels off)
        for (int i = 1; i < DMX_CHANNELS; i++) {
            this.buffer[i] = 0x00;
        }
    }

    /**
     * Add a device to the DMX universe
     *
     * @param device       The DMX device to add
     * @param startChannel The starting DMX channel (1-512)
     * @return true if device was added successfully, false otherwise
     */
    public boolean addDevice(DMXDevice device, int startChannel) {
        // Validate start channel
        if (startChannel < MIN_DMX_CHANNEL || startChannel > MAX_DMX_CHANNEL) {
            System.err.println("Invalid start channel: " + startChannel);
            return false;
        }

        int endChannel = startChannel + device.numChannels() - 1;

        // Ensure end channel is within DMX512 range
        if (endChannel > MAX_DMX_CHANNEL) {
            System.err.println("End channel is out of range: " + endChannel);
            return false;
        }

        // Ensure there is no overlap with existing devices
        for (DeviceEntry entry : devices) {
            int sc = entry.getStartChannel();
            int ec = sc + entry.getDevice().numChannels() - 1;

            if (rangesOverlap(startChannel, endChannel, sc, ec)) {
                System.err.println("Device channels overlapping: " +
                        startChannel + "-" + endChannel + " conflicts with " + sc + "-" + ec);
                return false;
            }
        }

        // Add the device
        devices.add(new DeviceEntry(device, startChannel));
        return true;
    }

    /**
     * Render all devices to the DMX frame and send to hardware
     */
    public void render() {
        // Copy all device buffers to DMX frame
        for (DeviceEntry entry : devices) {
            byte[] deviceBuffer = entry.getDevice().getBuffer();
            int startChannel = entry.getStartChannel();

            // Copy device buffer to the main DMX buffer
            // Note: DMX channels are 1-based, but array is 0-based
            // Channel 1 corresponds to buffer[1], etc.
            System.arraycopy(deviceBuffer, 0, buffer, startChannel,
                    Math.min(deviceBuffer.length, MAX_DMX_CHANNEL - startChannel + 1));
        }

        // Write frame to DMX device
        writeFrame();
    }

    /**
     * Write the current DMX frame to the hardware device
     */
    private void writeFrame() {
        if (stream != null) {
            try {
                stream.write(buffer);
                stream.flush(); // Ensure data is sent immediately
            } catch (IOException e) {
                System.err.println("Error writing to DMX device: " + e.getMessage());
            }
        } else {
            System.err.println("DMX stream is not initialized");
        }
    }

    /**
     * Reset the universe to its default state
     */
    public void reset() {
        resetDevices();
        resetBuffer();
    }

    /**
     * Remove all devices from the universe
     */
    public void resetDevices() {
        devices.clear();
    }

    /**
     * Reset the DMX buffer to default state (all channels off)
     */
    public void resetBuffer() {
        initializeBuffer();
    }

    /**
     * Check if two ranges overlap
     *
     * @param x1 Start of first range
     * @param x2 End of first range
     * @param y1 Start of second range
     * @param y2 End of second range
     * @return true if ranges overlap, false otherwise
     */
    private boolean rangesOverlap(int x1, int x2, int y1, int y2) {
        return Math.max(x1, y1) <= Math.min(x2, y2);
    }

    /**
     * Get the current DMX buffer (for debugging/monitoring)
     *
     * @return copy of the current DMX buffer
     */
    public byte[] getBuffer() {
        return buffer.clone();
    }

    /**
     * Get the number of devices currently registered
     *
     * @return number of devices
     */
    public int getDeviceCount() {
        return devices.size();
    }

    /**
     * Get list of all registered devices (read-only)
     *
     * @return list of device entries
     */
    public List<DeviceEntry> getDevices() {
        return new ArrayList<>(devices); // Return a copy to prevent external modification
    }

    /**
     * Set a specific DMX channel value directly
     *
     * @param channel DMX channel (1-512)
     * @param value   Channel value (0-255)
     * @return true if successful, false if channel is out of range
     */
    public boolean setChannel(int channel, int value) {
        if (channel < MIN_DMX_CHANNEL || channel > MAX_DMX_CHANNEL) {
            System.err.println("Channel out of range: " + channel);
            return false;
        }

        if (value < 0 || value > 255) {
            System.err.println("Value out of range: " + value);
            return false;
        }

        buffer[channel] = (byte) (value & 0xFF);
        return true;
    }

    /**
     * Get a specific DMX channel value
     *
     * @param channel DMX channel (1-512)
     * @return channel value (0-255) or -1 if channel is out of range
     */
    public int getChannel(int channel) {
        if (channel < MIN_DMX_CHANNEL || channel > MAX_DMX_CHANNEL) {
            System.err.println("Channel out of range: " + channel);
            return -1;
        }

        return buffer[channel] & 0xFF; // Convert to unsigned
    }

    /**
     * Close the DMX stream and clean up resources
     */
    public void close() {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                System.err.println("Error closing DMX stream: " + e.getMessage());
            }
        }
    }

    /**
     * Finalize method to ensure resources are cleaned up
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    /**
     * Interface that DMX devices must implement
     */
    public interface DMXDevice {
        /**
         * Returns the number of DMX channels this device uses
         */
        int numChannels();

        /**
         * Returns the device's current channel data buffer
         */
        byte[] getBuffer();
    }

    // Example DMX device implementations

    /**
     * Inner class to represent a DMX device entry
     */
    public static class DeviceEntry {
        private DMXDevice device;
        private int startChannel;

        public DeviceEntry(DMXDevice device, int startChannel) {
            this.device = device;
            this.startChannel = startChannel;
        }

        public DMXDevice getDevice() {
            return device;
        }

        public int getStartChannel() {
            return startChannel;
        }
    }

    /**
     * Example: Simple RGB LED device
     */
    public static class RGBLight implements DMXDevice {
        private byte[] buffer = new byte[3]; // RGB = 3 channels

        public void setColor(int red, int green, int blue) {
            buffer[0] = (byte) (red & 0xFF);
            buffer[1] = (byte) (green & 0xFF);
            buffer[2] = (byte) (blue & 0xFF);
        }

        @Override
        public int numChannels() {
            return 3;
        }

        @Override
        public byte[] getBuffer() {
            return buffer.clone();
        }
    }

    /**
     * Example: Simple dimmer device
     */
    public static class Dimmer implements DMXDevice {
        private byte[] buffer = new byte[1]; // 1 channel for intensity

        public void setIntensity(int intensity) {
            buffer[0] = (byte) (intensity & 0xFF);
        }

        @Override
        public int numChannels() {
            return 1;
        }

        @Override
        public byte[] getBuffer() {
            return buffer.clone();
        }
    }
}