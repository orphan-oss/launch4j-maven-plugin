# Launch4j Maven Plugin

Originally hosted at http://9stmaryrd.com/tools/launch4j-maven-plugin/

[![Build Status](https://travis-ci.org/lukaszlenart/launch4j-maven-plugin.svg)](https://travis-ci.org/lukaszlenart/launch4j-maven-plugin)

## Version notes 1.7.3
- uses Maven annotation instead of JavaDoc parameters [#15](../../pull/15)
- upgrades Maven plugins [#15](../../pull/15)
- converts tabs to spaces 5b0619

## Version notes 1.7.2
- adds support for `restartOnCrash` Launch4j's option [#14](../../pull/14)

## Version notes 1.7.1
- launch4j's `abeille` dependency was excluded [#11](../../pull/11)
- versions of several plugins were updated [#11](../../pull/11)
- tabs were converted to spaces [#11](../../pull/11)

## Version notes 1.7
- uses the latest version of Launch4j (3.5.0)
- contains support for `runtimeBits`, see [#6](../../issues/6)
- fixes problem with including dependencies in scope `runtime`, see [#5](../../issues/5)

## Version notes 1.6
- dropped Launch4j source and based on artifacts from Maven Central, see [#8](../../issues/8)
- uses the latest version of Launch4j (3.4.0)
- at least Java 1.7 is required

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
Q: Where can I find -SNAPSHOT builds?

A: Use the Sonatype OSS repo

```xml
<repositories>
    <repository>
        <id>sonatype-nexus-snapshots</id>
        <name>Sonatype Nexus Snapshots</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```
