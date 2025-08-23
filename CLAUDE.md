# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the Launch4j Maven Plugin, which wraps Java JAR files in Windows executables using the Launch4j utility. The plugin integrates Launch4j functionality into the Maven build process, allowing developers to create Windows executables as part of their Maven builds.

## Core Architecture

### Main Components

- **Launch4jMojo.java** (`src/main/java/com/akathist/maven/plugins/launch4j/Launch4jMojo.java`): The main Maven plugin class that handles the launch4j goal execution
- **Configuration Classes**: POJO classes for plugin configuration (ClassPath, Jre, VersionInfo, etc.)
- **Generator Classes** (`src/main/java/com/akathist/maven/plugins/launch4j/generators/`): Handle generation of copyright and version info
- **ResourceIO** (`src/main/java/com/akathist/maven/plugins/launch4j/tools/ResourceIO.java`): Utility for handling resources

### Key Plugin Structure

- Plugin binds to the `package` phase by default
- Supports both GUI and console application types
- Handles dependency resolution in `runtime` and `compile` scopes
- Thread-safe execution with optional parallel execution control
- Can load configuration from external Launch4j XML files via `<infile>` parameter

## Build and Development Commands

### Standard Maven Commands
```bash
# Build the plugin
mvn clean compile

# Run tests
mvn test

# Full build with tests
mvn clean test

# Package the plugin
mvn clean package

# Install to local repository
mvn clean install
```

### Testing
- Uses JUnit 4.13.2 with JUnitParams and Mockito
- Maven Plugin Testing Harness for integration tests
- Test files located in `src/test/java/`

### Documentation Generation
```bash
# Generate plugin documentation site
mvn site

# Generate plugin documentation reports
mvn project-info-reports:dependencies
mvn plugin:report
```

## Important Configuration Details

### Maven Plugin Configuration
- The plugin uses Maven Plugin API 3.9.11
- Minimum Java version: 1.8
- Launch4j version: 3.50
- Supports Maven 3.6.x and above

### Key Plugin Parameters
- `<infile>`: Load configuration from external Launch4j XML file (default: `${project.basedir}/src/main/resources/${project.artifactId}-launch4j.xml`)
- `<outfile>`: Output executable path (default: `${project.build.directory}/${project.artifactId}.exe`)
- `<jar>`: JAR file to wrap (default: `${project.build.directory}/${project.build.finalName}.jar`)
- `<skip>`: Skip plugin execution (can also use `-DskipLaunch4j` property)
- `<disableVersionInfoDefaults>`: Disable automatic VersionInfo defaults

### ClassPath Configuration
- `<addDependencies>`: Include Maven dependencies in classpath (default: true)
- `<jarLocation>`: Prefix for JAR paths (useful for lib/ directories)
- `<preCp>` and `<postCp>`: Add custom classpath entries

## Architecture Notes

### Plugin Execution Flow
1. Validates configuration and parameters
2. Resolves platform-specific Launch4j binaries from Maven repositories
3. Builds configuration object from Maven plugin parameters
4. Optionally loads external Launch4j configuration file
5. Applies default values for VersionInfo if not disabled
6. Executes Launch4j Builder to create Windows executable

### Platform Support
- Runs on Windows, Linux, macOS, and Solaris
- Downloads platform-specific Launch4j binaries as Maven artifacts
- Linux 64-bit uses `linux64` platform, 32-bit uses `linux32`

### Version Info Defaults
The plugin automatically provides default values for VersionInfo parameters based on Maven project properties:
- File version from project version
- Product name from project name
- Company/copyright from organization
- Description from project description

This behavior can be disabled with `<disableVersionInfoDefaults>true</disableVersionInfoDefaults>`.

## Documentation References

- Main documentation: `src/main/resources/README.adoc`
- Parameter reference: `src/main/resources/MOJO.md`
- Version info details: `src/main/resources/VERSIONINFO.md`
- Launch4j documentation: http://launch4j.sourceforge.net/docs.html