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

### Using Fixtures and Modes

For an easy example, see the code in this repository in [`be.codewriter.dmx512.Main`](src/main/java/be/codewriter/dmx512/Main.java). This is a simplified version:

```java
DMXSerialController controller = new DMXSerialController();
List<DMXClient> clients = new ArrayList<>();

// List available ports
LOGGER.info("Available ports:");

for (var port : controller.getAvailablePorts()) {
    LOGGER.info("\t{}",port);
}

// Connect to the DMX interface
// On Windows, use something like "COM3"
// On Linux, use something like "/dev/ttyUSB0"
if (controller.connect("/dev/ttyUSB0")) {
    LOGGER.info("Connected to DMX interface");

    // Create some fixtures
    var fixture;
    
    try (InputStream is = Main.class.getClassLoader().getResourceAsStream("led-party-tcl-spot.json")) {
        fixture = OpenFormatLibraryParser.parseFixture(is);
    } catch (Exception ex) {
        LOGGER.error("Error parsing fixture: {}",ex.getMessage());
    }

    // 0 is the DMX address of the fixture
    DMXClient rgb = new DMXClient(fixture, fixture.modes().getFirst(), 0);

    // Set to full red
    rgb.setValue("red",(byte) 255);

    // Send the data to the DMX interface
    controller.render(List.of(rgb));

    // Fade effect example
    for (int i = 0; i <=100;i++) {      
        float ratio = i / 100.0f;
        rgb.setValue("red",(byte) (255*(1-ratio)));
        rgb.setValue("blue",(byte) (255*ratio));
        controller.render(List.of(rgb));
        Thread.sleep(50);
    }
    
    controller.close();
} else {
    LOGGER.error("Failed to connect to DMX interface");
}
```

For a more extended example, with a JavaFX user interface, see the [DMX512-Demo repository](https://github.com/codewriterbv/DMX512-Demo).

## Using this Library in your Project

### From Maven Repository

NOT AVAILABLE YET.

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
