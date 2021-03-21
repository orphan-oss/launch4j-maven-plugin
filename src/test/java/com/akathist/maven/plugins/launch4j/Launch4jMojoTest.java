package com.akathist.maven.plugins.launch4j;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;

public class Launch4jMojoTest extends AbstractMojoTestCase {

    protected void setUp() throws Exception {
        // required
        super.setUp();
    }

    protected void tearDown() throws Exception {
        // required
        super.tearDown();
    }

    public void testWorkingDir() throws Exception {
        File pom = getTestFile( "src/test/resources/test-plugin-pom.xml" );

        Launch4jMojo mojo = (Launch4jMojo) lookupMojo("launch4j", pom);

        assertNotNull(mojo);
    }

}
