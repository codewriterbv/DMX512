package be.codewriter.dmx512.controller;

public interface DMXController {
    boolean connect(String address);

    void setChannel(int channel, int value);

    void setChannels(int startChannel, int[] values);

    void render();

    void close();

    int getChannel(int channel);

    boolean isConnected();
}

