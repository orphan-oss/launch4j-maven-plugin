package com.akathist.maven.plugins.launch4j;

import net.sf.launch4j.config.LanguageID;
import org.apache.maven.model.Organization;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;

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

    // Subject
    private VersionInfo versionInfo;

    @Before
    public void buildVersionInfoFromTestParams() {
        versionInfo = new VersionInfo(fileVersion, txtFileVersion, fileDescription,
                copyright, productVersion, txtProductVersion,
                productName, companyName, internalName,
                originalFilename, language, trademarks);
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
    public void should_Not_FillOutByDefaults_From_MavenProject_OrganizationName_When_OrganizationWas_Empty() {
        // given
        versionInfo.companyName = null;
        versionInfo.trademarks = null;

        // when
        versionInfo.tryFillOutByDefaults(project, outfile);

        // then
        assertNull(versionInfo.companyName);
        assertNull(versionInfo.trademarks);
    }

    @Test
    public void should_Not_FillOutByDefaults_From_OrganizationName_When_VersionInfoPropsWere_Filled() {
        // given
        String organizationName = "Example OSS";
        doReturn(organizationName).when(organization).getName();

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

        versionInfo.companyName = null;
        versionInfo.trademarks = null;

        // when
        versionInfo.tryFillOutByDefaults(project, outfile);

        // then
        assertEquals(organizationName, versionInfo.companyName);
        assertEquals(organizationName, versionInfo.trademarks);
    }

    @Test
    public void should_Not_FillOutByDefaults_SimpleValues_From_MavenProject_When_ProjectPropsWere_Empty() {
        // given
        doReturn(null).when(project).getVersion();
        versionInfo.txtFileVersion = null;
        versionInfo.txtProductVersion = null;

        doReturn(null).when(project).getName();
        versionInfo.productName = null;

        doReturn(null).when(project).getArtifactId();
        versionInfo.internalName = null;

        doReturn(null).when(project).getDescription();
        versionInfo.fileDescription = null;

        // when
        versionInfo.tryFillOutByDefaults(project, outfile);

        // then
        assertNull(versionInfo.txtFileVersion);
        assertNull(versionInfo.txtProductVersion);
        assertNull(versionInfo.productName);
        assertNull(versionInfo.internalName);
        assertNull(versionInfo.fileDescription);
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

    // TODO: exception tryFillOutOriginalFileNameByDefault

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