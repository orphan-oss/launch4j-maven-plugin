package com.akathist.maven.plugins.launch4j.generators;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;

public class CopyrightGenerator {
    private CopyrightGenerator() {
    }

    /**
     * Parameters should be taken from MavenProject properties:
     * @param projectInceptionYear as ${project.inceptionYear}
     * @param projectOrganizationName as ${project.organization.name}
     * @return a string representing copyrights
     */
    public static String generate(String projectInceptionYear, String projectOrganizationName) {
        String inceptionYear = generateInceptionYear(projectInceptionYear);
        int buildYear = LocalDate.now().getYear();
        String organizationName = generateOrganizationName(projectOrganizationName);

        return String.format("Copyright © %s%d%s. All rights reserved.", inceptionYear, buildYear, organizationName);
    }

    private static String generateInceptionYear(String projectInceptionYear) {
        if(StringUtils.isNotBlank(projectInceptionYear)) {
            return projectInceptionYear + "-";
        }

        return "";
    }

    private static String generateOrganizationName(String projectOrganizationName) {
        if(StringUtils.isNotBlank(projectOrganizationName)) {
            return " " + projectOrganizationName;
        }

        return "";
    }
}
