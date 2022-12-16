# Launch4j Maven plugin – launch4j:launch4j

**Description**:

Wraps a jar in a Windows executable.

**Attributes**:

*   Requires a Maven project to be executed.
*   Requires dependency resolution of artifacts in scope: `runtime`.
*   The goal is thread-safe and supports parallel builds.
*   Binds by default to the [lifecycle phase](http://maven.apache.org/ref/current/maven-core/lifecycles.html): `package`.

### Parameter Details

#### **\<chdir>**

Changes to the given directory, relative to the executable, before running your jar. If set to `.` the current directory will be where the executable is. If omitted, the directory will not be changed.

*   **Type**: `java.lang.String`
*   **Required**: `No`

* * *

#### **\<classPath>**

Details about the classpath your application should have. This is required if you are not wrapping a jar.

*   **Type**: `com.akathist.maven.plugins.launch4j.ClassPath`
*   **Required**: `No`

* * *

#### **\<cmdLine>**

Constant command line arguments to pass to your program's main method. Actual command line arguments entered by the user will appear after these.

*   **Type**: `java.lang.String`
*   **Required**: `No`

* * *

#### **\<configOutfile>**

If `saveConfig` is set to true, config will be written to this file

*   **Type**: `java.io.File`
*   **Required**: `No`
*   **Default**: `${project.build.directory}/launch4j-config.xml`

* * *

#### **\<dontWrapJar>**

Whether the executable should wrap the jar or not.

*   **Type**: `boolean`
*   **Required**: `No`
*   **Default**: `false`

* * *

#### **\<downloadUrl>**

downloadUrl (?).

*   **Type**: `java.lang.String`
*   **Required**: `No`

* * *

#### **\<errTitle>**

The title of the error popup if something goes wrong trying to run your program, like if java can't be found. If this is a console app and not a gui, then this value is used to prefix any error messages, as in ${errTitle}: ${errorMessage}.

*   **Type**: `java.lang.String`
*   **Required**: `No`

* * *

#### **\<headerType>**

Whether you want a gui or console app. Valid values are "gui" and "console." If you say gui, then launch4j will run your app from javaw instead of java in order to avoid opening a DOS window. Choosing gui also enables other options like taskbar icon and a splash screen.

*   **Type**: `java.lang.String`
*   **Required**: `No`

* * *

#### **\<icon>**

The icon to use in the taskbar. Must be in ico format.

*   **Type**: `java.io.File`
*   **Required**: `No`

* * *

#### **\<infile>**

The name of the Launch4j native configuration file The path, if relative, is relative to the pom.xml.

*   **Type**: `java.io.File`
*   **Required**: `No`

* * *

#### **\<jar>**

The jar to bundle inside the executable. The path, if relative, is relative to the pom.xml. If you don't want to wrap the jar, then this value should be the runtime path to the jar relative to the executable. You should also set dontWrapJar to true. You can only bundle a single jar. Therefore, you should either create a jar that contains your own code plus all your dependencies, or you should distribute your dependencies alongside the executable.

*   **Type**: `java.lang.String`
*   **Required**: `No`
*   **Default**: `${project.build.directory}/${project.build.finalName}.jar`

* * *

#### **\<jre>**

Details about the supported jres.

*   **Type**: `com.akathist.maven.plugins.launch4j.Jre`
*   **Required**: `No`

* * *

#### **\<libs>**

Win32 libraries to include. Used for custom headers only.

*   **Type**: `java.util.List`
*   **Required**: `No`

* * *

#### **\<manifest>**

Windows manifest file (a XML file) with the same name as .exe file (myapp.exe.manifest)

*   **Type**: `java.io.File`
*   **Required**: `No`

* * *

#### **\<messages>**

Various messages you can display.

*   **Type**: `com.akathist.maven.plugins.launch4j.Messages`
*   **Required**: `No`

* * *

#### **\<objs>**

Object files to include. Used for custom headers only.

*   **Type**: `java.util.List`
*   **Required**: `No`

* * *

#### **\<outfile>**

The name of the executable you want launch4j to produce. The path, if relative, is relative to the pom.xml.

*   **Type**: `java.io.File`
*   **Required**: `No`
*   **Default**: `${project.build.directory}/${project.artifactId}.exe`

* * *

#### **\<parallelExecution>**

If set to true, a synchronized block will be used to protect resources

*   **Type**: `boolean`
*   **Required**: `No`
*   **Default**: `false`

* * *

#### **\<pluginArtifacts>**

The dependencies of this plugin. Used to get the Launch4j artifact version.

*   **Type**: `java.util.List`
*   **Required**: `No`
*   **Default**: `${plugin.artifacts}`

* * *

#### **\<priority>**

Priority class of windows process. Valid values are "normal" (default), "idle" and "high".

*   **Type**: `java.lang.String`
*   **Required**: `No`
*   **Default**: `normal`

* * *

#### **\<restartOnCrash>**

If true, when the application exits, any exit code other than 0 is considered a crash and the application will be started again.

*   **Type**: `boolean`
*   **Required**: `No`
*   **Default**: `false`

* * *

#### **\<saveConfig>**

If set to true it will save final config into a XML file

*   **Type**: `boolean`
*   **Required**: `No`
*   **Default**: `false`

* * *

#### **\<singleInstance>**

Details about whether to run as a single instance.

*   **Type**: `com.akathist.maven.plugins.launch4j.SingleInstance`
*   **Required**: `No`

* * *

#### **\<skip>**

If set to true, execution of the plugin will be skipped

*   **Type**: `boolean`
*   **Required**: `No`
*   **Default**: `false`

* * *

#### **\<splash>**

Details about the splash screen.

*   **Type**: `com.akathist.maven.plugins.launch4j.Splash`
*   **Required**: `No`

* * *

#### **\<stayAlive>**

If true, the executable waits for the java application to finish before returning its exit code. Defaults to false for gui applications. Has no effect for console applications, which always wait.

*   **Type**: `boolean`
*   **Required**: `No`
*   **Default**: `false`

* * *

#### **\<supportUrl>**

supportUrl (?).

*   **Type**: `java.lang.String`
*   **Required**: `No`

* * *

#### **\<vars>**

Variables to set.

*   **Type**: `java.util.List`
*   **Required**: `No`

* * *

#### **\<versionInfo>**

Lots of information you can attach to the windows process.

*   **Type**: `com.akathist.maven.plugins.launch4j.VersionInfo`
*   **Required**: `No`

#### **\<disableVersionInfoDefaults>**

If `disableVersionInfoDefaults` is set to true, it will prevent filling out the VersionInfo params with default values.

*   **Type**: `boolean`
*   **Required**: `No`
*   **Default**: `false`

* * *