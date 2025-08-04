package com.akathist.maven.plugins.launch4j.generators;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(JUnitParamsRunner.class)
public class Launch4jFileVersionGeneratorTest {
    @Test
    public void shouldReturnNull_WhenProjectVersionIsNull() {
        // given
        String projectVersion = null;

        // expect
        assertNull(Launch4jFileVersionGenerator.generate(projectVersion));
    }

    @Test(expected = IllegalArgumentException.class)
    @Parameters({
        "",
        " ",
        "null",
        "alpha-1.2.3",
        "1a.2.3",
        "1.X.3",
        "1.2.3_11",
        "1.2.3;4",
        "1.2.3.4SNAPSHOT",
        "1.2.3.4.SNAPSHOT"
    })
    public void shouldThrowException_WhenProjectVersion_HaveWrongFormat(String projectVersion) {
        // expect throws
        Launch4jFileVersionGenerator.generate(projectVersion);
    }

    @Test
    @Parameters({
        "0, 0.0.0.0",
        "1, 1.0.0.0",
        "2, 2.0.0.0",
        "3.14, 3.14.0.0",
        "4.0.1, 4.0.1.0",
        "55.44.33, 55.44.33.0"
    })
    public void shouldFillMissingPlacesByZeros(String projectVersion, String expected) {
        // when
        final String launch4jFileVersion = Launch4jFileVersionGenerator.generate(projectVersion);

        // then
        assertEquals(expected, launch4jFileVersion);
    }

    @Test
    @Parameters({
        "1-SNAPSHOT, 1.0.0.0",
        "1.2.1-alpha, 1.2.1.0",
        "1.2.3.4-beta, 1.2.3.4",
        "0.0.1-snapshot, 0.0.1.0",
        "1.2.3.4-alpha+001, 1.2.3.4",
        "1.2.3-alpha+001, 1.2.3.0",
        "1.2.3.4-alpha+001, 1.2.3.4",
        "1.2.3+20130313144700, 1.2.3.0",
        "1.2.3.4+20130313144700, 1.2.3.4",
        "1.2.3-beta+exp.sha.5114f85, 1.2.3.0",
        "1.2.3.4-beta+exp.sha.5114f85, 1.2.3.4",
    })
    public void shouldCutOffTextFlags(String projectVersion, String expected) {
        // when
        final String launch4jFileVersion = Launch4jFileVersionGenerator.generate(projectVersion);

        // then
        assertEquals(expected, launch4jFileVersion);
    }

    @Test
    @Parameters({
        "0.0.0.0.1, 0.0.0.0",
        "1.22.333.4444.55555.666666, 1.22.333.4444",
        "9.8.7.6.5-SNAPSHOT, 9.8.7.6",
        "3.0.1.12.44.62.1.0.0.0.1-alpha, 3.0.1.12",
    })
    public void shouldCutOffTooManyNestedDigits(String projectVersion, String expected) {
        // when
        final String launch4jFileVersion = Launch4jFileVersionGenerator.generate(projectVersion);

        // then
        assertEquals(expected, launch4jFileVersion);
    }

    @Test
    @Parameters({
        "302.08, 302.8.0.0",
        "1.02.3, 1.2.3.0",
        "01.02.03.04, 1.2.3.4",
        "10.00.01, 10.0.1.0",
        "0.08.09, 0.8.9.0",
        "302.08-SNAPSHOT, 302.8.0.0",
        "1.02.03.04.05.06, 1.2.3.4",
        "000.001.002, 0.1.2.0"
    })
    public void shouldStripLeadingZerosFromVersionComponents(String projectVersion, String expected) {
        // when
        final String launch4jFileVersion = Launch4jFileVersionGenerator.generate(projectVersion);

        // then
        assertEquals(expected, launch4jFileVersion);
    }

    @Test
    @Parameters({
        "0, 0.0.0.0",
        "00, 0.0.0.0",
        "000, 0.0.0.0",
        "0.0, 0.0.0.0",
        "00.00, 0.0.0.0",
        "000.000, 0.0.0.0"
    })
    public void shouldHandleZeroVersionsCorrectly(String projectVersion, String expected) {
        // when
        final String launch4jFileVersion = Launch4jFileVersionGenerator.generate(projectVersion);

        // then
        assertEquals(expected, launch4jFileVersion);
    }
}