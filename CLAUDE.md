# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Maven plugin that wraps JAR files into Windows executables using Launch4j. The plugin allows generating
Windows `.exe` files from Java applications as part of the Maven build process.

**Requirements:**

- Java 17 (specified in `<maven.compiler.target>` in pom.xml)
- Maven 3.6.x or higher

## Build Commands

```bash
# Build the project
./mvnw clean install

# Run tests only
./mvnw test

# Run a single test class
./mvnw test -Dtest=Launch4jMojoTest

# Run a single test method
./mvnw test -Dtest=Launch4jMojoTest#testConfigurationWithIcon

# Skip tests during build
./mvnw clean install -DskipTests

# Generate site documentation
./mvnw site
```

## Architecture

### Core Components

- **`Launch4jMojo`** (`src/main/java/.../Launch4jMojo.java`) - The main Maven Mojo that executes during the `package`
  phase. Handles:
    - Loading configuration (either from POM or external Launch4j XML config via `<infile>`)
    - Downloading platform-specific Launch4j binaries (win32, linux, linux64, mac, solaris)
    - Building the Windows executable using Launch4j's `Builder`

- **Configuration POJOs** - Mirror Launch4j's XML configuration structure:
    - `ClassPath` - Classpath configuration with Maven dependency support
    - `Jre` - JRE path and version requirements
    - `VersionInfo` - Windows executable version information
    - `Splash` - Splash screen configuration
    - `SingleInstance` - Mutex-based single instance support
    - `Messages` - Custom error messages

- **`generators/`** - Default value generators:
    - `CopyrightGenerator` - Generates copyright string from project metadata
    - `Launch4jFileVersionGenerator` - Converts Maven version to Windows version format (x.x.x.x)

### Plugin Configuration

The plugin binds to the `package` phase by default. Key configuration parameters:

- `headerType` - `gui` or `console` (default: `console`)
- `outfile` - Output executable path
- `jar` - Input JAR file
- `skip` / `-DskipLaunch4j` - Skip plugin execution
- `parallelExecution` - Synchronize execution for thread safety

### Test Structure

Tests use `maven-plugin-testing-harness` with mock Maven projects in `src/test/resources/unit/launch4j-config/`.

## Platform-Specific Binaries

Launch4j requires platform-specific binaries (ld, windres) that are downloaded as Maven artifacts with classifiers like
`workdir-win32`, `workdir-linux64`, `workdir-mac`. These are unpacked to the local Maven repository and reused.
