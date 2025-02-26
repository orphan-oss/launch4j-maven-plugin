/*
 * Maven Launch4j Plugin
 * Copyright (c) 2006 Paul Jungwirth
 * Copyright (c) 2011-2025 Lukasz Lenart
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.akathist.maven.plugins.launch4j;

import com.akathist.maven.plugins.launch4j.generators.CopyrightGenerator;
import com.akathist.maven.plugins.launch4j.generators.Launch4jFileVersionGenerator;
import net.sf.launch4j.config.LanguageID;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Organization;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Information that appears in the Windows Explorer.
 */
public class VersionInfo {
    private static final Map<String, LanguageID> LANGUAGE_TO_LANGUAGE_ID;

    static {
        LANGUAGE_TO_LANGUAGE_ID = new HashMap<>();
        for (LanguageID languageID : LanguageID.values()) {
            LANGUAGE_TO_LANGUAGE_ID.put(languageID.name(), languageID);
        }
    }

    /**
     * Version number in x.x.x.x format.
     */
    @Parameter
    String fileVersion;

    /**
     * Free-form version number, like "1.20.RC1."
     */
    @Parameter
    String txtFileVersion;

    /**
     * File description shown to the user.
     */
    @Parameter
    String fileDescription;

    /**
     * Legal copyright.
     */
    @Parameter
    String copyright;

    /**
     * Version number in x.x.x.x format.
     */
    @Parameter
    String productVersion;

    /**
     * Free-form version number, like "1.20.RC1."
     */
    @Parameter
    String txtProductVersion;

    /**
     * The product name.
     */
    @Parameter
    String productName;

    /**
     * The company name.
     */
    @Parameter
    String companyName;

    /**
     * The internal name. For instance, you could use the filename without extension or the module name.
     */
    @Parameter
    String internalName;

    /**
     * The original filename without path. Setting this lets you determine whether a user has renamed the file.
     */
    @Parameter
    String originalFilename;

    /**
     * Language to be used during installation, default ENGLISH_US
     */
    @Parameter
    String language = LanguageID.ENGLISH_US.name();

    /**
     * Trademarks of author
     */
    @Parameter
    String trademarks;

    private Log log;

    public VersionInfo() {
    }

    public VersionInfo(String fileVersion, String txtFileVersion, String fileDescription,
                       String copyright, String productVersion, String txtProductVersion,
                       String productName, String companyName, String internalName,
                       String originalFilename, String language, String trademarks,
                       Log log) {
        this.fileVersion = fileVersion;
        this.txtFileVersion = txtFileVersion;
        this.fileDescription = fileDescription;
        this.copyright = copyright;
        this.productVersion = productVersion;
        this.txtProductVersion = txtProductVersion;
        this.productName = productName;
        this.companyName = companyName;
        this.internalName = internalName;
        this.originalFilename = originalFilename;
        this.language = language;
        this.trademarks = trademarks;
        this.log = log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    net.sf.launch4j.config.VersionInfo toL4j() {
        net.sf.launch4j.config.VersionInfo ret = new net.sf.launch4j.config.VersionInfo();

        ret.setFileVersion(fileVersion);
        ret.setTxtFileVersion(txtFileVersion);
        ret.setFileDescription(fileDescription);
        ret.setCopyright(copyright);
        ret.setProductVersion(productVersion);
        ret.setTxtProductVersion(txtProductVersion);
        ret.setProductName(productName);
        ret.setCompanyName(companyName);
        ret.setInternalName(internalName);
        ret.setOriginalFilename(originalFilename);
        setLanguage(ret);
        ret.setTrademarks(trademarks);

        return ret;
    }

    private void setLanguage(net.sf.launch4j.config.VersionInfo ret) {
        LanguageID languageID = LANGUAGE_TO_LANGUAGE_ID.get(language);
        if (languageID == null) {
            languageID = LanguageID.ENGLISH_US;
        }
        ret.setLanguage(languageID);
    }

    void tryFillOutByDefaults(MavenProject project, File outfile) {
        if (project == null) {
            throw new IllegalArgumentException("'project' is required, but it is null.");
        }
        if (outfile == null) {
            throw new IllegalArgumentException("'outfile' is required, but it is null.");
        }

        String version = getDefaultWhenSourceIsBlankAndLogWarn(project.getVersion(), "1.0.0", "project.version");
        Organization organization = project.getOrganization();
        String organizationName = "Default organization";
        if(organization == null) {
            logWarningAboutDummyValue("project.organization.name", organizationName);
        } else {
            organizationName = getDefaultWhenSourceIsBlankAndLogWarn(organization.getName(), organizationName, "project.organization.name");
        }

        tryFillOutByDefaultVersionInL4jFormat(version);
        tryFillOutCopyrightByDefaults(
                getDefaultWhenSourceIsBlankAndLogWarn(project.getInceptionYear(), "2020", "project.inceptionYear"),
                organizationName
        );
        tryFillOutOrganizationRelatedDefaults(organizationName);
        tryFillOutSimpleValuesByDefaults(
                version,
                getDefaultWhenSourceIsBlankAndLogWarn(project.getName(), "Java Project", "project.name"),
                getDefaultWhenSourceIsBlankAndLogWarn(project.getArtifactId(), "java-project", "project.artifactId"),
                getDefaultWhenSourceIsBlankAndLogWarn(project.getDescription(), "A Java project.", "project.description")
        );

        String outfileName = getDefaultWhenSourceIsBlankAndLogWarn(outfile.getName(), "app.exe", "outfile");
        originalFilename = getDefaultWhenSourceIsBlank(originalFilename, outfileName);
    }

    private void tryFillOutByDefaultVersionInL4jFormat(String version) {
        String defaultFileVersion = Launch4jFileVersionGenerator.generate(version);

        fileVersion = getDefaultWhenSourceIsBlank(fileVersion, defaultFileVersion);
        productVersion = getDefaultWhenSourceIsBlank(productVersion, defaultFileVersion);
    }

    private void tryFillOutCopyrightByDefaults(String inceptionYear, String organizationName) {
        final String defaultCopyright = CopyrightGenerator.generate(inceptionYear, organizationName);
        copyright = getDefaultWhenSourceIsBlank(copyright, defaultCopyright);
    }

    private void tryFillOutOrganizationRelatedDefaults(String organizationName) {
        companyName = getDefaultWhenSourceIsBlank(companyName, organizationName);
        trademarks = getDefaultWhenSourceIsBlank(trademarks, organizationName);
    }

    private void tryFillOutSimpleValuesByDefaults(String version,
                                                  String name,
                                                  String artifactId,
                                                  String description) {
        txtFileVersion = getDefaultWhenSourceIsBlank(txtFileVersion, version);
        txtProductVersion = getDefaultWhenSourceIsBlank(txtProductVersion, version);
        productName = getDefaultWhenSourceIsBlank(productName, name);
        internalName = getDefaultWhenSourceIsBlank(internalName, artifactId);
        fileDescription = getDefaultWhenSourceIsBlank(fileDescription, description);
    }

    private String getDefaultWhenSourceIsBlank(final String source, final String defaultValue) {
        if (StringUtils.isBlank(source)) {
            return defaultValue;
        }

        return source;
    }

    private String getDefaultWhenSourceIsBlankAndLogWarn(final String source,
                                                         final String defaultValue,
                                                         final String sourceParamName) {
        if (StringUtils.isBlank(source)) {
            logWarningAboutDummyValue(sourceParamName, defaultValue);

            return defaultValue;
        }

        return source;
    }

    private void logWarningAboutDummyValue(final String sourceParamName, final String dummyValue) {
        log.warn("Configuration param ${" + sourceParamName + "} is empty, so a dummy value \"" + dummyValue + "\" " +
                "might be used instead to fulfill some of VersionInfo params by defaults.");
    }

    @Override
    public String toString() {
        return "VersionInfo{" +
                "fileVersion='" + fileVersion + '\'' +
                ", txtFileVersion='" + txtFileVersion + '\'' +
                ", fileDescription='" + fileDescription + '\'' +
                ", copyright='" + copyright + '\'' +
                ", productVersion='" + productVersion + '\'' +
                ", txtProductVersion='" + txtProductVersion + '\'' +
                ", productName='" + productName + '\'' +
                ", companyName='" + companyName + '\'' +
                ", internalName='" + internalName + '\'' +
                ", originalFilename='" + originalFilename + '\'' +
                ", language='" + language + '\'' +
                ", trademarks='" + trademarks + '\'' +
                '}';
    }
}
