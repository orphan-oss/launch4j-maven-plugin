package com.akathist.maven.plugins.launch4j.generators;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class CopyrightGeneratorTest {
    private static final String COPYRIGHT_PREFIX = "Copyright © ";
    private static final String COPYRIGHT_POSTFIX = ". All rights reserved.";

    private String buildYear;

    @Before
    public void initializeBuildYear() {
        buildYear = String.valueOf(
                LocalDate.now().getYear()
        );
    }

    @Test
    public void shouldContain_BuildYear() {
        // when
        final String copyright = CopyrightGenerator.generate(null, null);

        // then
        String expected = concatAndWrapWithCopyright(buildYear);
        assertEquals(expected, copyright);
    }

    @Test
    public void shouldContain_InceptionYear_And_BuildYear() {
        // given
        final String projectInceptionYear = "2019";

        // when
        final String copyright = CopyrightGenerator.generate(projectInceptionYear, null);

        // then
        String expected = concatAndWrapWithCopyright(
                projectInceptionYear, "-", buildYear
        );
        assertEquals(expected, copyright);
    }

    @Test
    public void shouldContain_BuildYear_And_OrganizationName() {
        // given
        final String organizationName = "SoftwareMill";

        // when
        final String copyright = CopyrightGenerator.generate(null, organizationName);

        // then
        String expected = concatAndWrapWithCopyright(
                buildYear, " ", organizationName
        );
        assertEquals(expected, copyright);
    }

    @Test
    public void shouldContain_InceptionYear_And_BuildYear_And_OrganizationName() {
        // given
        final String projectInceptionYear = "2020";
        final String organizationName = "Orphan OSS";

        // when
        final String copyright = CopyrightGenerator.generate(projectInceptionYear, organizationName);

        // then
        String expected = concatAndWrapWithCopyright(
                projectInceptionYear, "-", buildYear, " ", organizationName
        );
        assertEquals(expected, copyright);
    }

    private String concatAndWrapWithCopyright(String... elements) {
        StringBuilder builder = new StringBuilder(COPYRIGHT_PREFIX);

        for (String element : elements) {
            builder.append(element);
        }

        builder.append(COPYRIGHT_POSTFIX);

        return builder.toString();
    }
}
