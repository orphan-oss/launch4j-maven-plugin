# VersionInfo parameters

### Description.

This file describes the `VersionInfo` parameters.

*   **Type**: `com.akathist.maven.plugins.launch4j.VersionInfo`

Every parameter (including their parent `VersionInfo`) have a default value defined.
To fulfill them by default values you need to make sure that **\<disableVersionInfoDefaults>** inside plugin configuration is set to `false`.

### Parameter Details

#### **\<fileVersion>**

Version number in `x.x.x.x` format.

*   **Type**: `java.lang.String`
*   **Required**: `Yes`
*   **Default**: `${project.version}` converted into a `x.x.x.x` format.

Conversion into a `x.x.x.x` format have specific constraints:
*   `x` as a number
*   shorter project versions like `x.x.x` will have appended zeros (to the 4th level) like `x.x.x.0`
*   every text flag like "-SNAPSHOT" or "-alpha" will be cut off
*   too many nested levels (>4) will be cut off as well. Example input: `1.2.3.4.5.6`, output: `1.2.3.4`.

* * *

#### **\<txtFileVersion>**

Free-form version number, like "1.20.RC1."

*   **Type**: `java.lang.String`
*   **Required**: `Yes`
*   **Default**: `${project.version}`

* * *

#### **\<fileDescription>**

File description shown to the user.

*   **Type**: `java.lang.String`
*   **Required**: `Yes`
*   **Default**: `${project.description}`

* * *

#### **\<copyright>**

Legal copyright.

*   **Type**: `java.lang.String`
*   **Required**: `Yes`
*   **Default**: `Copyright © ${project.inceptionYear}-${currentYear} ${project.organization.name}. All rights reserved.`. 

Where: 
*   `${project.inceptionYear}` is not mandatory.
*   `${currentYear}` is generated programmatically.
*   `${project.organization.name}` is not mandatory.

* * *

#### **\<productVersion>**

Version number in `x.x.x.x` format.

*   **Type**: `java.lang.String`
*   **Required**: `Yes`
*   **Default**: `${project.version}` converted into a `x.x.x.x` format. The same conversion such the one described regarding `fileVersion` parameter above.

* * *

#### **\<txtProductVersion>**

Free-form version number, like "1.20.RC1."

*   **Type**: `java.lang.String`
*   **Required**: `Yes`
*   **Default**: `${project.version}`

* * *

#### **\<productName>**

The product name.

*   **Type**: `java.lang.String`
*   **Required**: `Yes`
*   **Default**: `${project.name}`

* * *

#### **\<companyName>**

The company name.

*   **Type**: `java.lang.String`
*   **Required**: `Yes`
*   **Default**: `${project.organization.name}`

* * *

#### **\<internalName>**

The internal name. For instance, you could use the filename without extension or the module name.

*   **Type**: `java.lang.String`
*   **Required**: `Yes`
*   **Default**: `${project.artifactId}`

* * *

#### **\<originalFilename>**

The original filename without path. Setting this lets you determine whether a user has renamed the file.

*   **Type**: `java.lang.String`
*   **Required**: `Yes`
*   **Default**: last path segment of the `${outfile}` configuration

* * *

#### **\<language>**

Language to be used during installation.

*   **Type**: `java.lang.String`
*   **Required**: `Yes`
*   **Default**: `ENGLISH_US`

* * *

#### **\<trademarks>**

Trademarks of author.

*   **Type**: `java.lang.String`
*   **Required**: `Yes`
*   **Default**: `${project.organization.name}`

* * *