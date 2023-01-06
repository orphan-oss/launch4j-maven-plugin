package com.akathist.maven.plugins.launch4j.generators;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Organization;

import java.time.LocalDate;

public class CopyrightGenerator {
    private CopyrightGenerator() {
    }

    /**
     * Parameters should be taken from MavenProject properties:
     * @param projectInceptionYear as ${project.inceptionYear}
     * @param projectOrganization as ${project.organization}
     * @return a string representing copyrights
     */
    public static String generate(String projectInceptionYear, Organization projectOrganization) {
        String inceptionYear = generateInceptionYear(projectInceptionYear);
        int buildYear = LocalDate.now().getYear();
        String organizationName = generateOrganizationName(projectOrganization);

        return String.format("Copyright © %s%d%s. All rights reserved.", inceptionYear, buildYear, organizationName);
    }

    private static String generateInceptionYear(String projectInceptionYear) {
        if(StringUtils.isNotBlank(projectInceptionYear)) {
            return projectInceptionYear + "-";
        }

        return "";
    }

    private static String generateOrganizationName(Organization projectOrganization) {
        if(projectOrganization != null && StringUtils.isNotBlank(projectOrganization.getName())) {
            return " " + projectOrganization.getName();
        }

        return "";
    }
}
