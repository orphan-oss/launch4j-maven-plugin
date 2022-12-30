/*
 * Maven Launch4j Plugin
 * Copyright (c) 2006 Paul Jungwirth
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
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

    public VersionInfo() {
    }

    public VersionInfo(String fileVersion, String txtFileVersion, String fileDescription,
                       String copyright, String productVersion, String txtProductVersion,
                       String productName, String companyName, String internalName,
                       String originalFilename, String language, String trademarks) {
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

    public void tryFillOutByDefaults(MavenProject project, File outfile) {
        if (project == null) {
            throw new IllegalArgumentException("'project' is required, but it is null.");
        }
        if (outfile == null) {
            throw new IllegalArgumentException("'outfile' is required, but it is null.");
        }

        String version = project.getVersion();
        Organization organization = project.getOrganization();

        tryFillOutByDefaultVersionInL4jFormat(version);
        tryFillOutCopyrightByDefaults(project.getInceptionYear(), organization);
        tryFillOutOrganizationRelatedDefaults(organization);
        tryFillOutSimpleValuesByDefaults(
                version,
                project.getName(),
                project.getArtifactId(),
                project.getDescription()
        );

        originalFilename = getDefaultWhenOriginalIsBlank(originalFilename, outfile.getName(), "originalFilename", "${project.version}");
    }

    private void tryFillOutByDefaultVersionInL4jFormat(String version) {
        final String defaultFileVersion = Launch4jFileVersionGenerator.generate(version);
        fileVersion = getDefaultWhenOriginalIsBlank(fileVersion, defaultFileVersion, "fileVersion", "${project.version}");
        productVersion = getDefaultWhenOriginalIsBlank(productVersion, defaultFileVersion, "productVersion", "${project.version}");
    }

    private void tryFillOutCopyrightByDefaults(String inceptionYear, Organization organization) {
        final String defaultCopyright = CopyrightGenerator.generate(inceptionYear, organization);
        copyright = getDefaultWhenOriginalIsBlank(copyright, defaultCopyright, "copyright", "${project.inceptionYear},${project.organization.name}");
    }

    private void tryFillOutOrganizationRelatedDefaults(Organization organization) {
        if (organization != null) {
            companyName = getDefaultWhenOriginalIsBlank(companyName, organization.getName(), "companyName", "${project.organization.name}");
            trademarks = getDefaultWhenOriginalIsBlank(trademarks, organization.getName(), "trademarks", "${project.organization.name}");
        }
    }

    private void tryFillOutSimpleValuesByDefaults(String version,
                                                  String name,
                                                  String artifactId,
                                                  String description) {
        txtFileVersion = getDefaultWhenOriginalIsBlank(txtFileVersion, version, "txtFileVersion", "${project.version}");
        txtProductVersion = getDefaultWhenOriginalIsBlank(txtProductVersion, version, "txtProductVersion", "${project.version}");
        productName = getDefaultWhenOriginalIsBlank(productName, name, "productName", "${project.name}");
        internalName = getDefaultWhenOriginalIsBlank(internalName, artifactId, "internalName", "${project.artifactId}");
        fileDescription = getDefaultWhenOriginalIsBlank(fileDescription, description, "fileDescription", "${project.description}");
    }

    private String getDefaultWhenOriginalIsBlank(final String originalValue,
                                                 final String defaultValue,
                                                 final String originalParameterName,
                                                 final String defaultValueFormulaParams) {
        if (StringUtils.isBlank(originalValue)) {
            if(StringUtils.isNotBlank(defaultValue)) {
                return defaultValue;
            }

            throw new IllegalStateException("Please fill the missing configuration values. " +
                    "Error when trying to fulfill default value for VersionInfo parameter:'" + originalParameterName + "' with formula params:'" + defaultValueFormulaParams + "'.");
        }

        return originalValue;
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
