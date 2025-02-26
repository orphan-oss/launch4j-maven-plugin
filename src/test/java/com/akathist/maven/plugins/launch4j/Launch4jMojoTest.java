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

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;

public class Launch4jMojoTest extends AbstractMojoTestCase {
    public void testPrintOutFulfilledConfiguration() throws Exception {
        File testPom = new File(getBasedir(), "src/test/resources/unit/launch4j-config/launch4j-full-plugin-config.xml");

        Launch4jMojo mojo = (Launch4jMojo) lookupMojo("launch4j", testPom);

        assertNotNull(mojo);

        assertEquals("Launch4jMojo{" +
                "headerType='gui', " +
                "infile=null, " +
                "outfile=${project.build.directory}" + File.separator + "app.exe, " +
                "jar='${project.build.directory}/${project.artifactId}-${project.version}.jar', " +
                "dontWrapJar=false, " +
                "errTitle='null', " +
                "downloadUrl='https://java.com/download', " +
                "supportUrl='null', " +
                "cmdLine='null', " +
                "chdir='null', " +
                "priority='null', " +
                "stayAlive=false, " +
                "restartOnCrash=false, " +
                "icon=null, " +
                "requireAdminRights=false, " +
                "objs=null, " +
                "libs=null, " +
                "vars=null, " +
                "jre=Jre{" +
                "path='%JAVA_HOME%;%PATH%', " +
                "requires64Bit=false, " +
                "minVersion='1.8', " +
                "maxVersion='null', " +
                "requiresJdk=true," +
                " initialHeapSize=0, " +
                "initialHeapPercent=0, " +
                "maxHeapSize=0, " +
                "maxHeapPercent=0, " +
                "opts=[-Dname=Lukasz]" +
                "}, " +
                "classPath=ClassPath{" +
                "mainClass='pl.org.lenart.launch4j.App', " +
                "addDependencies=true, " +
                "jarLocation='null', " +
                "preCp='anything', " +
                "postCp='null'" +
                "}, " +
                "singleInstance=null, " +
                "splash=null, " +
                "versionInfo=VersionInfo{" +
                "fileVersion='1.0.0.0', " +
                "txtFileVersion='${project.version}', " +
                "fileDescription='Launch4j Demo App', " +
                "copyright='Lukasz Lenart', " +
                "productVersion='1.0.0.0', " +
                "txtProductVersion='1.0.0.0', " +
                "productName='App', " +
                "companyName='Lukasz Lenart', " +
                "internalName='app', " +
                "originalFilename='app.exe', " +
                "language='ENGLISH_US', " +
                "trademarks='Luk ™'" +
                "}, " +
                "disableVersionInfoDefaults=true, " +
                "messages=Messages{" +
                "startupErr='null', " +
                "jreVersionErr='null', " +
                "launcherErr='null', " +
                "instanceAlreadyExistsMsg='null', " +
                "jreNotFoundErr='null'" +
                "}, " +
                "manifest=null, " +
                "saveConfig=false, " +
                "configOutfile=null, " +
                "parallelExecution=false, " +
                "skip=false" +
                "}", mojo.toString());
    }
}