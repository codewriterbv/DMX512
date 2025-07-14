# DMX512 Java Library

Java library for the DMX512 protocol. Can be used to connect with USB-to-DMX and IP-to-DMX controllers. It can load fixture definitions from the Open Fixture Library (OFL) to easily create a Java (user interface) application that can interact with many types of DMX devices.

## Computer to DMX Devices

* Serial
  * [Enttec Open DMX USB Interface](https://www.thomann.de/be/enttec_open_dmx_usb_interface.htm)
  * [DSD TECH SH-RS09B USB to DMX Cable](https://www.amazon.com.be/gp/product/B07WV6P5W6/)
* Network
  * [JUNELIONY ArtNet 1024 2-Port Sulite DMX LAN512 2-Port ArtNet Converter](https://www.amazon.com.be/dp/B0CYPQ2Z4V)
  
## Fixtures

Uses the [Open Fixture Library (OFL)](https://open-fixture-library.org/) to create DMX fixtures as Java objects. Fixtures can be parsed from the [OFL JSON format](https://github.com/OpenLightingProject/open-fixture-library/blob/master/docs/fixture-format.md).

## Sample use

Below you can find some sample implementations based on this library. Check [Main.java](src/main/java/be/codewriter/dmx512/Main.java) for more of these examples. Check the [DMX512-Demo repository](https://github.com/codewriterbv/DMX512-Demo) for an example if you want to create a user interface with JavaFX.

### Send Raw Data

You can send a byte array directly via the controller. Create an array with the expected length by your device and fill in the values. 

This is an example for a PicoSpot on channel 1 = the data starts at index 0 of the byte array.

```java
var controller = new DMXIPController(InetAddress.getByName("172.16.1.144"));

// The PicoSpot on DMX channel 1 expects 11 values
/*
"Pan",
"Tilt",
"Pan fine",
"Tilt fine",
"Pan/Tilt Speed",
"Color Wheel",
"Gobo Wheel",
"Dimmer",
"Shutter / Strobe",
"Program",
"Program Speed"
*/
// Set all to 0
controller.render(new byte[]{(byte) 0, (byte) 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
sleep(2_000);
// Set pan and tilt to 127
controller.render(new byte[]{(byte) 127, (byte) 127, 0, 0, 0, 0, 0, 0, 0, 0, 0});
sleep(2_000);
// Set color wheel to 44 and dimmer full op
controller.render(new byte[]{0, 0, 0, 0, 0, (byte) 44, 0, (byte) 255, 0, 0, 0});
```

### Using Fixtures and Modes

By using a fixture, loaded from an OFL file, it becomes a lot easier to change the data. You can use the name of the channel (e.g. "red", "dimmer", ...) and don't need to know the index of the data in the byte array.

This is a minimal example:

```java
var controller = new DMXIPController(InetAddress.getByName("172.16.1.144"));

// Load a fixture
Fixture fixture = OpenFormatLibraryParser
        .parseFixture(new File("/your/path/to/led-party-tcl-spot.json"));

// Create a DMX client based on the fixture, a mode, and DMX channel (23 in this example)
DMXClient client = new DMXClient(fixture, fixture.modes().getFirst(), 23);

// This fixture has only one mode with the following channels:
// "channels": [
//   "Red",
//   "Green",
//   "Blue",
//   "Dimmer",
//   "Effects"
// ]
              
// Set to full red
client.setValue("red", (byte) 255);
client.setValue("dimmer", (byte) 255);

// Send the data to the DMX interface
controller.render(client);

// Color change effect
for (int i = 0; i <= 100; i++) {
    float ratio = i / 100.0f;
    client.setValue("red", (byte) (255 * (1 - ratio)));
    client.setValue("blue", (byte) (255 * ratio));
    controller.render(client);
    sleep(50);
}

controller.close();
```

## Using this Library in your Project

### From Maven Repository

```xml
<dependency>
    <groupId>be.codewriter</groupId>
    <artifactId>dmx512</artifactId>
    <version>${dmx512.version}</version>
</dependency>
```

### From GitHub Repository

A new artifact is created with GitHub Actions and is available from [GitHub Packages on github.com/codewriterbv/DMX512/packages/2453902](https://github.com/codewriterbv/DMX512/packages/2453902)

1. Add dependency to your `pom.xml`:
    ```xml
    <dependency>
        <groupId>be.codewriter</groupId>
        <artifactId>dmx512</artifactId>
        <version>0.0.1-beta</version>
    </dependency>
    ```
2. Add repository to your `pom.xml`:
    ```xml
    <repositories>
        <repository>
            <id>github</id>
            <name>GitHub Maven Packages</name>
            <url>https://maven.pkg.github.com/codewriterbv/DMX512</url>
            <!-- Add the following only if you want to use snapshot versions -->
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
    ```
3. The GitHub repository requires credentials to get packages as [described here](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-to-github-packages). Add a GitHub token to your `settings.xml` in the `.m2` directory:
    ```xml
    <settings>
      ...
      <servers>
        ...
        <server>
            <id>github</id>
            <username>YOUR_USER_NAME</username>
            <password>YOUR_TOKEN</password>
        </server>
      </servers>
    </settings>
    ```

## About

Created by [CodeWriter bv](https://codewriter.be/).
