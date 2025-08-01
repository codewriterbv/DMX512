package be.codewriter.dmx512.controller.ip;

import java.net.InetAddress;

/**
 * Discovered IP interface6.
 * Provides traditional getters to be able to links these values to e.g. JavaFX table columns.
 *
 * @param address       network address of the interface
 * @param name          name of the interface
 * @param IPProtocol    {@link IPProtocol}
 * @param universeCount universe count
 */
public record DMXIPDevice(
        InetAddress address,
        String name,
        IPProtocol IPProtocol,
        int universeCount) {
    /**
     * Get the network address
     *
     * @return network address
     */
    public InetAddress getAddress() {
        return address();
    }

    /**
     * Get the name
     *
     * @return name
     */
    public String getName() {
        return name();
    }

    /**
     * Get the protocol
     *
     * @return {@link IPProtocol}
     */
    public IPProtocol getProtocol() {
        return IPProtocol();
    }

    /**
     * Get the universe count
     *
     * @return universe count
     */
    public int getUniverseCount() {
        return universeCount();
    }
}

