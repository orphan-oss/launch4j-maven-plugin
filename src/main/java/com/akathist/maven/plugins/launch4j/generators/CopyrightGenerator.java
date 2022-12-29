package com.akathist.maven.plugins.launch4j.generators;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Organization;

import java.time.LocalDate;

public class CopyrightGenerator {
    public static String generate(String originalInceptionYear, Organization organization) {
        int buildYear = LocalDate.now().getYear();
        String inceptionYear = generateDefaultInceptionYear(originalInceptionYear);
        String organizationName = generateDefaultOrganizationName(organization);

        return String.format("Copyright © %s%d%s. All rights reserved.", inceptionYear, buildYear, organizationName);
    }

    private static String generateDefaultInceptionYear(String inceptionYear) {
        if(StringUtils.isNotBlank(inceptionYear)) {
            return inceptionYear + "-";
        }

        return "";
    }

    private static String generateDefaultOrganizationName(Organization organization) {
        if(organization != null && organization.getName() != null) {
            return " " + organization.getName();
        }

        return "";
    }
}