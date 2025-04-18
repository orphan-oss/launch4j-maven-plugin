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
package com.akathist.maven.plugins.launch4j;

import net.sf.launch4j.config.LanguageID;
import org.apache.maven.model.Organization;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class VersionInfoTest {
    // VersionInfo test params
    private String fileVersion = "1.0.0.0";
    private String txtFileVersion = "1.0.0.0";
    private String fileDescription = "Launch4j Test Application";
    private String copyright = "Copyright Orphan OSS";
    private String productVersion = "1.0.0.0";
    private String txtProductVersion = "1.0.0.0";
    private String productName = "Test App";
    private String companyName = "Orphan OSS Company";
    private String internalName = "app";
    private String originalFilename = "app.exe";
    private String language = LanguageID.ENGLISH_US.name();
    private String trademarks = "Test ™";

    // Mocks
    @Mock
    Organization organization;
    @Mock
    MavenProject project;
    @Mock
    File outfile;
    @Mock
    Log log;

    // Subject
    private VersionInfo versionInfo;

    @Before
    public void buildVersionInfoFromTestParams() {
        versionInfo = new VersionInfo(fileVersion, txtFileVersion, fileDescription,
                copyright, productVersion, txtProductVersion,
                productName, companyName, internalName,
                originalFilename, language, trademarks,
                log);
    }

    @Test
    public void shouldConvertIntoL4jFormatProperly() {
        // when
        net.sf.launch4j.config.VersionInfo l4jVersionInfo = versionInfo.toL4j();

        // then
        assertEquals(versionInfo.fileVersion, l4jVersionInfo.getFileVersion());
        assertEquals(versionInfo.txtFileVersion, l4jVersionInfo.getTxtFileVersion());
        assertEquals(versionInfo.fileDescription, l4jVersionInfo.getFileDescription());
        assertEquals(versionInfo.copyright, l4jVersionInfo.getCopyright());
        assertEquals(versionInfo.productVersion, l4jVersionInfo.getProductVersion());
        assertEquals(versionInfo.txtProductVersion, l4jVersionInfo.getTxtProductVersion());
        assertEquals(versionInfo.productName, l4jVersionInfo.getProductName());
        assertEquals(versionInfo.companyName, l4jVersionInfo.getCompanyName());
        assertEquals(versionInfo.internalName, l4jVersionInfo.getInternalName());
        assertEquals(versionInfo.originalFilename, l4jVersionInfo.getOriginalFilename());
        assertEquals(versionInfo.trademarks, l4jVersionInfo.getTrademarks());
        assertEquals(versionInfo.language, l4jVersionInfo.getLanguage().name());
    }

    @Test
    public void shouldConvertIntoL4jFormat_For_All_Languages() {
        for (LanguageID languageId : LanguageID.values()) {
            // given
            versionInfo.language = languageId.name();

            // when
            net.sf.launch4j.config.VersionInfo l4jVersionInfo = versionInfo.toL4j();

            // then
            assertEquals(languageId, l4jVersionInfo.getLanguage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowException_WhenTryingToFillOutDefaults_WithEmptyProject() {
        // expect throws
        versionInfo.tryFillOutByDefaults(null, outfile);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowException_WhenTryingToFillOutDefaults_WithEmptyOutfile() {
        // expect throws
        versionInfo.tryFillOutByDefaults(project, null);
    }

    @Test
    public void should_Not_FillOut_ByDefaultVersion_InL4jFormat_When_VersionInfoPropsWere_Filled() {
        // given
        String projectVersion = "4.3.2.1";
        doReturn(projectVersion).when(project).getVersion();

        // when
        versionInfo.tryFillOutByDefaults(project, outfile);

        // then
        assertNotEquals(projectVersion, versionInfo.fileVersion);
        assertEquals(fileVersion, versionInfo.fileVersion);
        assertNotEquals(projectVersion, versionInfo.productVersion);
        assertEquals(productVersion, versionInfo.productVersion);
    }

    @Test
    public void shouldFillOut_ByDefaultVersion_InL4jFormat_When_VersionInfoPropsWere_Empty() {
        // given
        String projectVersion = "1.2.3.4";
        doReturn(projectVersion).when(project).getVersion();

        versionInfo.fileVersion = null;
        versionInfo.productVersion = null;

        // when
        versionInfo.tryFillOutByDefaults(project, outfile);

        // then
        assertEquals(projectVersion, versionInfo.fileVersion);
        assertNotEquals(fileVersion, versionInfo.fileVersion);
        assertEquals(projectVersion, versionInfo.productVersion);
        assertNotEquals(productVersion, versionInfo.productVersion);
    }

    @Test
    public void should_Not_FillOut_Copyright_ByDefault_When_ItWas_Filled() {
        // given
        String projectInceptionYear = "2017";
        doReturn(projectInceptionYear).when(project).getInceptionYear();

        String organizationName = "Another OSS";
        doReturn(organizationName).when(organization).getName();
        doReturn(organization).when(project).getOrganization();

        // when
        versionInfo.tryFillOutByDefaults(project, outfile);

        // then
        assertNotNull(versionInfo.copyright);
        assertEquals(copyright, versionInfo.copyright);
        assertFalse(versionInfo.copyright.contains(projectInceptionYear));
        assertFalse(versionInfo.copyright.contains(organizationName));
    }

    @Test
    public void shouldFillOut_Copyright_ByDefault_When_ItWas_Empty() {
        // given
        String projectInceptionYear = "2019";
        doReturn(projectInceptionYear).when(project).getInceptionYear();

        String organizationName = "Some OSS";
        doReturn(organizationName).when(organization).getName();
        doReturn(organization).when(project).getOrganization();

        versionInfo.copyright = null;

        // when
        versionInfo.tryFillOutByDefaults(project, outfile);

        // then
        assertNotNull(versionInfo.copyright);
        assertNotEquals(copyright, versionInfo.copyright);
        assertTrue(versionInfo.copyright.contains(projectInceptionYear));
        assertTrue(versionInfo.copyright.contains(organizationName));
    }

    @Test
    public void should_Not_FillOutByDefaults_From_OrganizationName_When_VersionInfoPropsWere_Filled() {
        // given
        String organizationName = "Example OSS";
        doReturn(organizationName).when(organization).getName();
        doReturn(organization).when(project).getOrganization();

        // when
        versionInfo.tryFillOutByDefaults(project, outfile);

        // then
        assertNotEquals(organizationName, versionInfo.companyName);
        assertEquals(companyName, versionInfo.companyName);
        assertNotEquals(organizationName, versionInfo.trademarks);
        assertEquals(trademarks, versionInfo.trademarks);
    }

    @Test
    public void shouldFillOutByDefaults_From_OrganizationName_When_OrganizationWas_Filled() {
        // given
        String organizationName = "Other OSS";
        doReturn(organizationName).when(organization).getName();
        doReturn(organization).when(project).getOrganization();

        versionInfo.companyName = null;
        versionInfo.trademarks = null;

        // when
        versionInfo.tryFillOutByDefaults(project, outfile);

        // then
        assertEquals(organizationName, versionInfo.companyName);
        assertEquals(organizationName, versionInfo.trademarks);
    }

    @Test
    public void should_Not_FillOutByDefaults_SimpleValues_From_MavenProject_When_VersionInfoPropsWere_Filled() {
        // given
        String projectVersion = "1.21.1";
        doReturn(projectVersion).when(project).getVersion();

        String projectName = "launch4j-test-app";
        doReturn(projectName).when(project).getName();

        String projectArtifactId = "launch4j-test";
        doReturn(projectArtifactId).when(project).getArtifactId();

        String projectDescription = "Launch4j Test App";
        doReturn(projectDescription).when(project).getDescription();

        // when
        versionInfo.tryFillOutByDefaults(project, outfile);

        // then
        assertNotEquals(projectVersion, versionInfo.txtFileVersion);
        assertEquals(txtFileVersion, versionInfo.txtFileVersion);
        assertNotEquals(projectVersion, versionInfo.txtProductVersion);
        assertEquals(txtProductVersion, versionInfo.txtProductVersion);
        assertNotEquals(projectName, versionInfo.productName);
        assertEquals(productName, versionInfo.productName);
        assertNotEquals(projectArtifactId, versionInfo.internalName);
        assertEquals(internalName, versionInfo.internalName);
        assertNotEquals(projectDescription, versionInfo.fileDescription);
        assertEquals(fileDescription, versionInfo.fileDescription);
    }

    @Test
    public void shouldFillOutByDefaults_SimpleValues_From_MavenProject_When_VersionInfoPropsWere_Empty() {
        // given
        String projectVersion = "1.21.1";
        doReturn(projectVersion).when(project).getVersion();
        versionInfo.txtFileVersion = null;
        versionInfo.txtProductVersion = null;

        String projectName = "launch4j-test-app";
        doReturn(projectName).when(project).getName();
        versionInfo.productName = null;

        String projectArtifactId = "launch4j-test";
        doReturn(projectArtifactId).when(project).getArtifactId();
        versionInfo.internalName = null;

        String projectDescription = "Launch4j Test App";
        doReturn(projectDescription).when(project).getDescription();
        versionInfo.fileDescription = null;

        // when
        versionInfo.tryFillOutByDefaults(project, outfile);

        // then
        assertEquals(projectVersion, versionInfo.txtFileVersion);
        assertEquals(projectVersion, versionInfo.txtProductVersion);
        assertEquals(projectName, versionInfo.productName);
        assertEquals(projectArtifactId, versionInfo.internalName);
        assertEquals(projectDescription, versionInfo.fileDescription);
    }

    @Test
    public void should_Not_FillOut_ByDefault_LastSegmentOfOutfilePath_When_OriginalFilenameWas_Filled() {
        // given
        String outfileName = "testApp.exe";
        doReturn(outfileName).when(outfile).getName();

        // when
        versionInfo.tryFillOutByDefaults(project, outfile);

        // then
        assertNotEquals(outfileName, versionInfo.originalFilename);
        assertEquals(originalFilename, versionInfo.originalFilename);
    }

    @Test
    public void shouldFillOut_ByDefault_LastSegmentOfOutfilePath_When_OriginalFilenameWas_Empty() {
        // given
        String outfileName = "testApp.exe";
        doReturn(outfileName).when(outfile).getName();
        versionInfo.originalFilename = null;

        // when
        versionInfo.tryFillOutByDefaults(project, outfile);

        // then
        assertEquals(outfileName, versionInfo.originalFilename);
    }

    @Test
    public void shouldLogWarningsAboutDummyValues() {
        // given
        ArgumentCaptor<String> logMessageCaptor = ArgumentCaptor.forClass(String.class);
        List<String> missingParamNames = Arrays.asList(
                "project.version",
                "project.name",
                "project.artifactId",
                "project.description",
                "project.inceptionYear",
                "project.organization.name",
                "outfile"
        );

        // when
        versionInfo.tryFillOutByDefaults(project, outfile);

        // then
        verify(log, times(missingParamNames.size())).warn(logMessageCaptor.capture());
        List<String> logMessages = logMessageCaptor.getAllValues();


        missingParamNames.forEach(missingParamName -> {
            assertTrue(logMessages.stream().anyMatch(message -> message.contains(missingParamName)));
        });
    }

    @Test
    public void shouldFillOut_ByDummyValues_When_OriginalValues_Empty_And_ProjectParams_Empty() {
        // given
        final String buildYear = String.valueOf(LocalDate.now().getYear());

        VersionInfo emptyValuesVersionInfo = new VersionInfo();
        emptyValuesVersionInfo.setLog(log);

        // when
        emptyValuesVersionInfo.tryFillOutByDefaults(project, outfile);

        // then
        assertEquals("1.0.0.0", emptyValuesVersionInfo.fileVersion);
        assertEquals("1.0.0", emptyValuesVersionInfo.txtFileVersion);
        assertEquals("A Java project.", emptyValuesVersionInfo.fileDescription);
        assertEquals("Copyright © 2020-" + buildYear + " Default organization. All rights reserved.", emptyValuesVersionInfo.copyright);
        assertEquals("1.0.0.0", emptyValuesVersionInfo.productVersion);
        assertEquals("1.0.0", emptyValuesVersionInfo.txtProductVersion);
        assertEquals("Java Project", emptyValuesVersionInfo.productName);
        assertEquals("Default organization", emptyValuesVersionInfo.companyName);
        assertEquals("java-project", emptyValuesVersionInfo.internalName);
        assertEquals("Default organization", emptyValuesVersionInfo.trademarks);
        assertEquals("app.exe", emptyValuesVersionInfo.originalFilename);
    }

    @Test
    public void shouldGenerateString_WithTestParams() {
        // when
        String result = versionInfo.toString();

        // then
        assertNotNull(result);
        assertTrue(containsParam(result, "fileVersion", fileVersion));
        assertTrue(containsParam(result, "txtFileVersion", txtFileVersion));
        assertTrue(containsParam(result, "fileDescription", fileDescription));
        assertTrue(containsParam(result, "copyright", copyright));
        assertTrue(containsParam(result, "productVersion", productVersion));
        assertTrue(containsParam(result, "txtProductVersion", txtProductVersion));
        assertTrue(containsParam(result, "productName", productName));
        assertTrue(containsParam(result, "companyName", companyName));
        assertTrue(containsParam(result, "internalName", internalName));
        assertTrue(containsParam(result, "originalFilename", originalFilename));
        assertTrue(containsParam(result, "language", language));
        assertTrue(containsParam(result, "trademarks", trademarks));
    }

    private boolean containsParam(String result, String paramName, String paramValue) {
        return result.contains(paramName + "='" + paramValue + "'");
    }
}
