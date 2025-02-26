/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
