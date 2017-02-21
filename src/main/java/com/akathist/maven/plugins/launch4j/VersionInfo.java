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

import net.sf.launch4j.config.LanguageID;

import java.util.HashMap;
import java.util.Map;

/**
 * Information that appears in the Windows Explorer.
 */
public class VersionInfo {

    private static Map<String, LanguageID> LANGUAGE_TO_LANGUAGE_ID;

    static {
        LANGUAGE_TO_LANGUAGE_ID = new HashMap<>();
        for (LanguageID languageID : LanguageID.values()) {
            LANGUAGE_TO_LANGUAGE_ID.put(languageID.name(), languageID);
        }
    }

    /**
     * Version number in x.x.x.x format.
     */
    String fileVersion;

    /**
     * Free-form version number, like "1.20.RC1."
     */
    String txtFileVersion;

    /**
     * File description shown to the user.
     */
    String fileDescription;

    /**
     * Legal copyright.
     */
    String copyright;

    /**
     * Version number in x.x.x.x format.
     */
    String productVersion;

    /**
     * Free-form version number, like "1.20.RC1."
     */
    String txtProductVersion;

    /**
     * The product name.
     */
    String productName;

    /**
     * The company name.
     */
    String companyName;

    /**
     * The internal name. For instance, you could use the filename without extension or the module name.
     */
    String internalName;

    /**
     * The original filename without path. Setting this lets you determine whether a user has renamed the file.
     */
    String originalFilename;

    /**
     * Language to be used during installation, default ENGLISH_US
     */
    String language = LanguageID.ENGLISH_US.name();

    /**
     * Trademarks of author
     */
    String trademarks;

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
