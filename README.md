# Launch4j Maven Plugin

Originally hosted at http://9stmaryrd.com/tools/launch4j-maven-plugin/

[![Build Status](https://travis-ci.org/lukaszlenart/launch4j-maven-plugin.svg)](https://travis-ci.org/lukaszlenart/launch4j-maven-plugin)

# Documentation

Please check [this](src/main/resources/README.adoc) document for more detailed info on how to use the plugin. Please also check [Launch4j's Configuration file](http://launch4j.sourceforge.net/docs.html#Configuration_file) page.

# Version Notes

## Version notes 1.7.9
- adds capability of loading Launch4j native external configuration file
```xml
<configuration> 
    <infile>${project.basedir}/etc/bin/config.xml</infile>
 </configuration>
```
By default it will take from ${project.basedir}/src/main/launch4j/${project.artifactId}-launch4j.xml
Plugin execution goal should be set to "install"

## Version notes 1.7.8
- fixes issue with spaces in path to maven repository on non-Windows systems, see [#27](../../issues/27), [#28](../../issues/28)

## Version notes 1.7.7
- once again fixes problem with including dependencies in scope `runtime` (now it should be the final solution), see [#5](../../issues/5)
- adds support for `bundledJreAsFallback` and `bundledJre64Bit` properties, see [#23](../../issues/23)
- upgrades Launch4j to 3.8.0, see [#21](../../issues/21)

## Version notes 1.7.6
- fixes again problem with including dependencies in scope `runtime`, see [#5](../../issues/5)

## Version notes 1.7.5
- allows add custom headers and libraries to working dir  [#22](../../pull/22)

## Version notes 1.7.4
- fixes type in default value for `outfile` parameter  [#17](../../pull/17)

## Version notes 1.7.3
- uses Maven annotation instead of JavaDoc parameters [#15](../../pull/15)
- upgrades Maven plugins [#15](../../pull/15)
- converts tabs to spaces [5b0619](../../commit/5b0619)

## Version notes 1.7.2
- adds support for `restartOnCrash` Launch4j's option [#14](../../pull/14)

## Version notes 1.7.1
- launch4j's `abeille` dependency was excluded [#11](../../pull/11)
- versions of several plugins were updated [#11](../../pull/11)
- tabs were converted to spaces [#11](../../pull/11)

## Version notes 1.7
- uses the latest version of Launch4j (3.5.0)
- contains support for `runtimeBits`, see [#6](../../issues/6)
- ~~fixes problem with including dependencies in scope `runtime`, see [#5](../../issues/5)~~

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
