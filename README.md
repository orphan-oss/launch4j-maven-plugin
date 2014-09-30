# Launch4j Maven Plugin

Originally hosted at http://9stmaryrd.com/tools/launch4j-maven-plugin/

[![Build Status](https://travis-ci.org/lukaszlenart/launch4j-maven-plugin.svg)](https://travis-ci.org/lukaszlenart/launch4j-maven-plugin)

## Version notes 1.6
- dropped Launch4j source and based on artifacts from Maven Central
- uses the latest version of Launch4j (3.4.0)

# FAQ
Q: I cannot build my project because `dsol-xml` dependency is missing?

A: Add this repository to your `~/.m2/settings.xml`

```xml
<repositories>
    <repository>
        <id>dsol-xml</id>
        <name>Simulation @ TU Delft</name>
        <url>http://simulation.tudelft.nl/maven/</url>
    </repository>
</repositories>
```
