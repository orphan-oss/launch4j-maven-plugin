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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.sf.launch4j.Builder;
import net.sf.launch4j.BuilderException;
import net.sf.launch4j.config.Config;
import net.sf.launch4j.config.ConfigPersister;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Wraps a jar in a Windows executable.
 */
@Mojo(name = "launch4j", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class Launch4jMojo extends AbstractMojo {

    private static final String LAUNCH4J_ARTIFACT_ID = "launch4j";

    private static final String LAUNCH4J_GROUP_ID = "net.sf.launch4j";

    /**
     * The dependencies required by the project.
     */
    @Parameter(defaultValue = "${project.artifacts}", required = true, readonly = true)
    private Set<Artifact> dependencies;

    /**
     * The user's current project.
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     * The user's plugins (including, I hope, this one).
     */
    @Parameter(defaultValue = "${project.build.plugins}", required = true, readonly = true)
    private List<Artifact> plugins;

    /**
     * Used to look up Artifacts in the remote repository.
     */
    @Component(role = ArtifactFactory.class)
    private ArtifactFactory factory;

    /**
     * The user's local repository.
     */
    @Parameter(defaultValue = "${localRepository}", required = true, readonly = true)
    private ArtifactRepository localRepository;

    /**
     * The artifact resolver used to grab the binary bits that launch4j needs.
     */
    @Component(role = ArtifactResolver.class)
    private ArtifactResolver resolver;

    /**
     * The dependencies of this plugin.
     * Used to get the Launch4j artifact version.
     */
    @Parameter(defaultValue = "${plugin.artifacts}")
    private java.util.List<Artifact> pluginArtifacts;

    /**
     * The base of the current project.
     */
    @Parameter(defaultValue = "${project.basedir}", required = true, readonly = true)
    private File basedir;

    /**
     * Whether you want a gui or console app.
     * Valid values are "gui" and "console."
     * If you say gui, then launch4j will run your app from javaw instead of java
     * in order to avoid opening a DOS window.
     * Choosing gui also enables other options like taskbar icon and a splash screen.
     */
    @Parameter(required = true)
    private String headerType;

    /**
     * The name of the executable you want launch4j to produce.
     * The path, if relative, is relative to the pom.xml.
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.artifactId}.exe")
    private File outfile;

    /**
     * The jar to bundle inside the executable.
     * The path, if relative, is relative to the pom.xml.
     * <p>
     * If you don't want to wrap the jar, then this value should be the runtime path
     * to the jar relative to the executable. You should also set dontWrapJar to true.
     * <p>
     * You can only bundle a single jar. Therefore, you should either create a jar that contains
     * your own code plus all your dependencies, or you should distribute your dependencies alongside
     * the executable.
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}.jar")
    private String jar;

    /**
     * Whether the executable should wrap the jar or not.
     */
    @Parameter(defaultValue = "false")
    private boolean dontWrapJar;

    /**
     * The title of the error popup if something goes wrong trying to run your program,
     * like if java can't be found. If this is a console app and not a gui, then this value
     * is used to prefix any error messages, as in ${errTitle}: ${errorMessage}.
     */
    @Parameter
    private String errTitle;

    /**
     * downloadUrl (?).
     */
    @Parameter
    private String downloadUrl;

    /**
     * supportUrl (?).
     */
    @Parameter
    private String supportUrl;

    /**
     * Constant command line arguments to pass to your program's main method.
     * Actual command line arguments entered by the user will appear after these.
     */
    @Parameter
    private String cmdLine;

    /**
     * Changes to the given directory, relative to the executable, before running your jar.
     * If set to <code>.</code> the current directory will be where the executable is.
     * If omitted, the directory will not be changed.
     */
    @Parameter
    private String chdir;

    /**
     * Priority class of windows process.
     * Valid values are "normal" (default), "idle" and "high".
     *
     * @see <a href="http://msdn.microsoft.com/en-us/library/windows/desktop/ms685100(v=vs.85).aspx">MSDN: Scheduling Priorities</a>
     */
    @Parameter(defaultValue = "normal")
    private String priority;


    /**
     * If true, the executable waits for the java application to finish before returning its exit code.
     * Defaults to false for gui applications. Has no effect for console applications, which always wait.
     */
    @Parameter(defaultValue = "false")
    private boolean stayAlive;

    /**
     * If true, when the application exits, any exit code other than 0 is considered a crash and
     * the application will be started again.
     */
    @Parameter(defaultValue = "false")
    private boolean restartOnCrash;

    /**
     * The icon to use in the taskbar. Must be in ico format.
     */
    @Parameter
    private File icon;

    /**
     * Object files to include. Used for custom headers only.
     */
    @Parameter
    private List<String> objs;

    /**
     * Win32 libraries to include. Used for custom headers only.
     */
    @Parameter
    private List<String> libs;

    /**
     * Variables to set.
     */
    @Parameter
    private List<String> vars;

    /**
     * Details about the supported jres.
     */
    @Parameter(required = true)
    private Jre jre;

    /**
     * Details about the classpath your application should have.
     * This is required if you are not wrapping a jar.
     */
    @Parameter
    private ClassPath classPath;

    /**
     * Details about whether to run as a single instance.
     */
    @Parameter
    private SingleInstance singleInstance;

    /**
     * Details about the splash screen.
     */
    @Parameter
    private Splash splash;

    /**
     * Lots of information you can attach to the windows process.
     */
    @Parameter
    private VersionInfo versionInfo;

    /**
     * Various messages you can display.
     */
    @Parameter
    private Messages messages;

    /**
     * Windows manifest file (a XML file) with the same name as .exe file (myapp.exe.manifest)
     */
    @Parameter
    private File manifest;

    private File getJar() {
        return new File(jar);
    }

    @Override
    public void execute() throws MojoExecutionException {
        if (getLog().isDebugEnabled()) {
            printState();
        }

        File workdir = setupBuildEnvironment();
        
        Config c = new Config();

        c.setHeaderType(headerType);
        c.setOutfile(outfile);
        c.setJar(getJar());
        c.setDontWrapJar(dontWrapJar);
        c.setErrTitle(errTitle);
        c.setDownloadUrl(downloadUrl);
        c.setSupportUrl(supportUrl);
        c.setCmdLine(cmdLine);
        c.setChdir(chdir);
        c.setPriority(priority);
        c.setStayAlive(stayAlive);
        c.setRestartOnCrash(restartOnCrash);
        c.setManifest(manifest);
        c.setIcon(icon);
        c.setHeaderObjects(relativizeAndCopy(workdir, objs));
        c.setLibs(relativizeAndCopy(workdir, libs));
        c.setVariables(vars);

        if (classPath != null) {
            c.setClassPath(classPath.toL4j(dependencies));
        }
        if (jre != null) {
            c.setJre(jre.toL4j());
        }
        if (singleInstance != null) {
            c.setSingleInstance(singleInstance.toL4j());
        }
        if (splash != null) {
            c.setSplash(splash.toL4j());
        }
        if (versionInfo != null) {
            c.setVersionInfo(versionInfo.toL4j());
        }
        if (messages != null) {
            c.setMessages(messages.toL4j());
        }

        ConfigPersister.getInstance().setAntConfig(c, getBaseDir());
        Builder b = new Builder(new MavenLog(getLog()), workdir);

        try {
            b.build();
        } catch (BuilderException e) {
            getLog().error(e);
            throw new MojoExecutionException("Failed to build the executable; please verify your configuration.", e);
        }
    }

    /**
     * Prepares a little directory for launch4j to do its thing. Launch4j needs a bunch of object files
     * (in the w32api and head directories) and the ld and windres binaries (in the bin directory).
     * The tricky part is that launch4j picks this directory based on where its own jar is sitting.
     * In our case, the jar is going to be sitting in the user's ~/.m2 repository. That's okay: we know
     * maven is allowed to write there. So we'll just add our things to that directory.
     * <p>
     * This approach is not without flaws.
     * It risks two processes writing to the directory at the same time.
     * But fortunately, once the binary bits are in place, we don't do any more writing there,
     * and launch4j doesn't write there either.
     * Usually ~/.m2 will only be one system or another.
     * But if it's an NFS mount shared by several system types, this approach will break.
     * <p>
     * Okay, so here is a better proposal: package the plugin without these varying binary files,
     * and put each set of binaries in its own tarball. Download the tarball you need to ~/.m2 and
     * unpack it. Then different systems won't contend for the same space. But then I'll need to hack
     * the l4j code so it permits passing in a work directory and doesn't always base it on
     * the location of its own jarfile.
     *
     * @return the work directory.
     */
    private File setupBuildEnvironment() throws MojoExecutionException {
        Artifact binaryBits = chooseBinaryBits();
        retrieveBinaryBits(binaryBits);
        return unpackWorkDir(binaryBits);
    }

    /**
     * Unzips the given artifact in-place and returns the newly-unzipped top-level directory.
     * Writes a marker file to prevent unzipping more than once.
     */
    private File unpackWorkDir(Artifact a) throws MojoExecutionException {
        File platJar = a.getFile();
        File dest = platJar.getParentFile();
        File marker = new File(dest, platJar.getName() + ".unpacked");

        // If the artifact is a SNAPSHOT, then a.getVersion() will report the long timestamp,
        // but getFile() will be 1.1-SNAPSHOT.
        // Since getFile() doesn't use the timestamp, all timestamps wind up in the same place.
        // Therefore we need to expand the jar every time, if the marker file is stale.
        if (marker.exists() && marker.lastModified() > platJar.lastModified()) {
            // if (marker.exists() && marker.platJar.getName().indexOf("SNAPSHOT") == -1) {
            getLog().info("Platform-specific work directory already exists: " + dest.getAbsolutePath());
        } else {
            JarFile jf = null;
            try {
                // trying to use plexus-archiver here is a miserable waste of time:
                jf = new JarFile(platJar);
                Enumeration<JarEntry> en = jf.entries();
                while (en.hasMoreElements()) {
                    JarEntry je = en.nextElement();
                    File outFile = new File(dest, je.getName());
                    File parent = outFile.getParentFile();
                    if (parent != null) parent.mkdirs();
                    if (je.isDirectory()) {
                        outFile.mkdirs();
                    } else {
                        InputStream in = jf.getInputStream(je);
                        byte[] buf = new byte[1024];
                        int len;
                        FileOutputStream fout = null;
                        try {
                            fout = new FileOutputStream(outFile);
                            while ((len = in.read(buf)) >= 0) {
                                fout.write(buf, 0, len);
                            }
                            in.close();
                            fout.close();
                            fout = null;
                        } finally {
                            if (fout != null) {
                                try {
                                    fout.close();
                                } catch (IOException e2) {
                                    // ignore
                                }
                            }
                        }
                        outFile.setLastModified(je.getTime());
                    }
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Error unarchiving " + platJar, e);
            } finally {
                try {
                    if (jf != null) jf.close();
                } catch (IOException e) {
                    // ignore
                }
            }

            try {
                marker.createNewFile();
                marker.setLastModified(new Date().getTime());
            } catch (IOException e) {
                getLog().warn("Trouble creating marker file " + marker, e);
            }
        }

        String n = platJar.getName();
        File workdir = new File(dest, n.substring(0, n.length() - 4));
        setPermissions(workdir);
        return workdir;
    }

    /**
     * Chmods the helper executables ld and windres on systems where that is necessary.
     */
    private void setPermissions(File workdir) {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            Runtime r = Runtime.getRuntime();
            try {
                r.exec("chmod 755 " + workdir + "/bin/ld").waitFor();
                r.exec("chmod 755 " + workdir + "/bin/windres").waitFor();
            } catch (InterruptedException e) {
                getLog().warn("Interrupted while chmodding platform-specific binaries", e);
            } catch (IOException e) {
                getLog().warn("Unable to set platform-specific binaries to 755", e);
            }
        }
    }

    /**
     * If custom header objects or libraries shall be linked, they need to sit inside the launch4j working dir.
     */
    private List<String> relativizeAndCopy(File workdir, List<String> paths) throws MojoExecutionException {
        if(paths == null) return null;
        
        List<String> result = new ArrayList<>();
        for(String path : paths) {
            Path source = basedir.toPath().resolve(path);
            Path dest = workdir.toPath().resolve(basedir.toPath().relativize(source));

            if(!source.startsWith(basedir.toPath())) {
                throw new MojoExecutionException("File must reside in the project directory: " + path);
            }

            if (Files.exists(source)) {
                try {
                    Path target = Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                    result.add(workdir.toPath().relativize(target).toString());
                } catch (IOException e) {
                    throw new MojoExecutionException("Can't copy file to workdir", e);
                }
            } else {
                result.add(path);
            }
        }

        return result;
    }
	
    /**
     * Downloads the platform-specific parts, if necessary.
     */
    private void retrieveBinaryBits(Artifact a) throws MojoExecutionException {
        try {
            resolver.resolve(a, project.getRemoteArtifactRepositories(), localRepository);

        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException("Can't find platform-specific components", e);
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("Can't retrieve platform-specific components", e);
        }
    }

    /**
     * Decides which platform-specific bundle we need, based on the current operating system.
     */
    private Artifact chooseBinaryBits() throws MojoExecutionException {
        String plat;
        String os = System.getProperty("os.name");
        getLog().debug("OS = " + os);

        // See here for possible values of os.name:
        // http://lopica.sourceforge.net/os.html
        if (os.startsWith("Windows")) {
            plat = "win32";
        } else if ("Linux".equals(os)) {
            plat = "linux";
        } else if ("Solaris".equals(os) || "SunOS".equals(os)) {
            plat = "solaris";
        } else if ("Mac OS X".equals(os) || "Darwin".equals(os)) {
            plat = "mac";
        } else {
            throw new MojoExecutionException("Sorry, Launch4j doesn't support the '" + os + "' OS.");
        }

        return factory.createArtifactWithClassifier(LAUNCH4J_GROUP_ID, LAUNCH4J_ARTIFACT_ID,
                getLaunch4jVersion(), "jar", "workdir-" + plat);
    }


    private File getBaseDir() {
        return basedir;
    }

    /**
     * Just prints out how we were configured.
     */
    private void printState() {
        Log log = getLog();

        log.debug("headerType = " + headerType);
        log.debug("outfile = " + outfile);
        log.debug("jar = " + jar);
        log.debug("dontWrapJar = " + dontWrapJar);
        log.debug("errTitle = " + errTitle);
        log.debug("downloadUrl = " + downloadUrl);
        log.debug("supportUrl = " + supportUrl);
        log.debug("cmdLine = " + cmdLine);
        log.debug("chdir = " + chdir);
        log.debug("priority = " + priority);
        log.debug("stayAlive = " + stayAlive);
        log.debug("restartOnCrash = " + restartOnCrash);
        log.debug("icon = " + icon);
        log.debug("objs = " + objs);
        log.debug("libs = " + libs);
        log.debug("vars = " + vars);
        if (singleInstance != null) {
            log.debug("singleInstance.mutexName = " + singleInstance.mutexName);
            log.debug("singleInstance.windowTitle = " + singleInstance.windowTitle);
        } else {
            log.debug("singleInstance = null");
        }
        if (jre != null) {
            log.debug("jre.path = " + jre.path);
            log.debug("jre.minVersion = " + jre.minVersion);
            log.debug("jre.maxVersion = " + jre.maxVersion);
            log.debug("jre.jdkPreference = " + jre.jdkPreference);
            log.debug("jre.initialHeapSize = " + jre.initialHeapSize);
            log.debug("jre.initialHeapPercent = " + jre.initialHeapPercent);
            log.debug("jre.maxHeapSize = " + jre.maxHeapSize);
            log.debug("jre.maxHeapPercent = " + jre.maxHeapPercent);
            log.debug("jre.opts = " + jre.opts);
        } else {
            log.debug("jre = null");
        }
        if (classPath != null) {
            log.debug("classPath.mainClass = " + classPath.mainClass);
            log.debug("classPath.addDependencies = " + classPath.addDependencies);
            log.debug("classPath.jarLocation = " + classPath.jarLocation);
            log.debug("classPath.preCp = " + classPath.preCp);
            log.debug("classPath.postCp = " + classPath.postCp);
        } else {
            log.info("classpath = null");
        }
        if (splash != null) {
            log.debug("splash.file = " + splash.file);
            log.debug("splash.waitForWindow = " + splash.waitForWindow);
            log.debug("splash.timeout = " + splash.timeout);
            log.debug("splash.timoutErr = " + splash.timeoutErr);
        } else {
            log.debug("splash = null");
        }
        if (versionInfo != null) {
            log.debug("versionInfo.fileVersion = " + versionInfo.fileVersion);
            log.debug("versionInfo.txtFileVersion = " + versionInfo.txtFileVersion);
            log.debug("versionInfo.fileDescription = " + versionInfo.fileDescription);
            log.debug("versionInfo.copyright = " + versionInfo.copyright);
            log.debug("versionInfo.productVersion = " + versionInfo.productVersion);
            log.debug("versionInfo.txtProductVersion = " + versionInfo.txtProductVersion);
            log.debug("versionInfo.productName = " + versionInfo.productName);
            log.debug("versionInfo.companyName = " + versionInfo.companyName);
            log.debug("versionInfo.internalName = " + versionInfo.internalName);
            log.debug("versionInfo.originalFilename = " + versionInfo.originalFilename);
        } else {
            log.debug("versionInfo = null");
        }
        if (messages != null) {
            log.debug("messages.startupErr = " + messages.startupErr);
            log.debug("messages.bundledJreErr = " + messages.bundledJreErr);
            log.debug("messages.jreVersionErr = " + messages.jreVersionErr);
            log.debug("messages.launcherErr = " + messages.launcherErr);
            log.debug("messages.instanceAlreadyExistsMsg = " + messages.instanceAlreadyExistsMsg);
        } else {
            log.debug("messages = null");
        }
    }

    /**
     * The Launch4j version used by the plugin.
     * We want to download the platform-specific bundle whose version matches the Launch4j version,
     * so we have to figure out what version the plugin is using.
     *
     * @return
     * @throws MojoExecutionException
     */
    private String getLaunch4jVersion() throws MojoExecutionException {
        String version = null;

        for (Artifact artifact : pluginArtifacts) {
            if (LAUNCH4J_GROUP_ID.equals(artifact.getGroupId()) &&
                    LAUNCH4J_ARTIFACT_ID.equals(artifact.getArtifactId())
                    && "core".equals(artifact.getClassifier())) {

                version = artifact.getVersion();
                getLog().debug("Found launch4j version " + version);
                break;
            }
        }

        if (version == null) {
            throw new MojoExecutionException("Impossible to find which Launch4j version to use");
        }

        return version;
    }
}
