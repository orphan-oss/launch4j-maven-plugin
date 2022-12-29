package com.akathist.maven.plugins.launch4j.generators;

import java.util.regex.Pattern;

public class Launch4jFileVersionGenerator {
    // todo comment on top which will describe

    /**
     * Valid examples:
     *  a
     * Not valid:
     *  b
     */
    private static final String SIMPLE_PROJECT_VERSION_REGEX = "^((\\d(\\.)?)*\\d+)(-\\w+)?$";
    private static final Pattern simpleProjectVersionPattern = Pattern.compile(SIMPLE_PROJECT_VERSION_REGEX, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /**
     * Converts original version into a format "x.x.x.x" ('x' as a number), which is required by Launch4j.
     * For shorter versions like "x.x.x" it will append zeros at the end "x.x.x.0".
     * Every text flags and variations like "-SNAPSHOT" or "-alpha" will be cut off.
     *
     * @param originalVersion
     * @return
     */
    public static String generate(String originalVersion) {
        if(originalVersion == null) {
            return null;
        }

        if(!simpleProjectVersionPattern.matcher(originalVersion).matches()) {
            throw new IllegalArgumentException("'project.version' is in invalid format. Regex pattern: " + SIMPLE_PROJECT_VERSION_REGEX);
        }

        String versionNumbers = originalVersion;

        if(originalVersion.contains("-")) {
            String[] split = originalVersion.split("-");
            versionNumbers = split[0];
        }

        StringBuilder version = new StringBuilder();
        String[] split = versionNumbers.split("\\.");

        for (int i = 0; i < 4; i++) {
            if(i > 0) {
                version.append(".");
            }

            if(i >= split.length) {
                version.append("0");
            } else {
                version.append(split[i]);
            }
        }

        return version.toString();
    }
}
