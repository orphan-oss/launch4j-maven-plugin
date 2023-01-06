package com.akathist.maven.plugins.launch4j.generators;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Launch4jFileVersionGenerator {
    private static final int REQUIRED_NESTED_VERSION_LEVELS = 4;
    private static final String SIMPLE_PROJECT_VERSION_REGEX = "^((\\d(\\.)?)*\\d+)(-\\w+)?$";
    private static final Pattern simpleProjectVersionPattern = Pattern.compile(
            SIMPLE_PROJECT_VERSION_REGEX, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private Launch4jFileVersionGenerator() {
    }

    /**
     * Converts projectVersion into a format "x.x.x.x" ('x' as a number), which is required by Launch4j.
     * <p>
     * For shorter versions like "x.x.x" it will append zeros (to the 4th level) at the end like "x.x.x.0".
     * Every text flag like "-SNAPSHOT" or "-alpha" will be cut off.
     * Too many nested numbers (more than 4 levels) will be cut off as well: "1.2.3.4.5.6" into "1.2.3.4".
     * <p>
     * Param should be taken from MavenProject property:
     * @param projectVersion as ${project.version}
     * @return a string representing a file version of format x.x.x.x
     */
    public static String generate(String projectVersion) {
        if(projectVersion == null) {
            return null;
        }
        if(!simpleProjectVersionPattern.matcher(projectVersion).matches()) {
            throw new IllegalArgumentException("'project.version' is in invalid format. Regex pattern: " + SIMPLE_PROJECT_VERSION_REGEX);
        }

        String versionLevels = removeTextFlags(projectVersion);
        String limitedVersionLevels = cutOffTooManyNestedLevels(versionLevels);

        return appendMissingNestedLevelsByZeros(limitedVersionLevels);
    }

    private static String removeTextFlags(String version) {
        if(version.contains("-")) {
            String[] parts = version.split("-");
            return parts[0];
        }

        return version;
    }

    private static String cutOffTooManyNestedLevels(String versionLevels) {
        String[] levels = versionLevels.split("\\.");

        if(levels.length > REQUIRED_NESTED_VERSION_LEVELS) {
            List<String> limitedLevels = Arrays.asList(levels)
                    .subList(0, REQUIRED_NESTED_VERSION_LEVELS);
            return String.join(".", limitedLevels);
        }

        return versionLevels;
    }

    private static String appendMissingNestedLevelsByZeros(String versionLevels) {
        String[] levels = versionLevels.split("\\.");

        StringBuilder filledLevels = new StringBuilder(versionLevels);
        for (int i = levels.length; i < REQUIRED_NESTED_VERSION_LEVELS; i++) {
            filledLevels.append(".0");
        }

        return filledLevels.toString();
    }
}
