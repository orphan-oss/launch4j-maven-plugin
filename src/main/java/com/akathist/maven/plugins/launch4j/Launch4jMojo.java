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

import net.sf.launch4j.Builder;
import net.sf.launch4j.BuilderException;
import net.sf.launch4j.config.Config;
import net.sf.launch4j.config.ConfigPersister;
import net.sf.launch4j.config.ConfigPersisterException;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.repository.LocalArtifactRequest;
import org.eclipse.aether.repository.LocalArtifactResult;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;

/**
 * Wraps a jar in a Windows executable.
 */
@Mojo(
        name = "launch4j",
        defaultPhase = LifecyclePhase.PACKAGE,
        requiresDependencyResolution = ResolutionScope.RUNTIME,
        threadSafe = true
)
public class Launch4jMojo extends AbstractMojo {

    @Parameter(defaultValue = "launch4j", required = true)
    private String launch4jArtifactId;

    @Parameter(defaultValue = "net.sf.launch4j", required = true)
    private String launch4jGroupId;

    // intentionally non-static non-final so it can be hacked with reflection if someone really needs to
    private String DEF_REQADMMAN_RES = "META-INF/resources/manifest-requireAdminRights-v1.xml";

    // intentionally non-static non-final so it can be hacked with reflection if someone really needs to
    private String DEF_REQADMMAN_FILE = "target/classes/META-INF/manifest-requireAdminRights.xml";
    
    /**
     * Maven Session.
     */
    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    private MavenSession session;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", required = true, readonly = true)
    private List<RemoteRepository> repositories;

    /**
     * The dependencies required by the project.
     */
    @Parameter(defaultValue = "${project.artifacts}", required = true, readonly = true)
    private Set<org.apache.maven.artifact.Artifact> dependencies;

    /**
     * The user's current project.
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     * Used to look up Artifacts in the remote repository.
     */
    @Component(role = RepositorySystem.class)
    private RepositorySystem repositorySystem;

    /**
     * The user's local repository.
     */
    @Parameter(defaultValue = "${repositorySystemSession}", required = true, readonly = true)
    private RepositorySystemSession repositorySystemSession;

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
    private List<org.apache.maven.artifact.Artifact> oldPluginArtifacts;

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
    @Parameter(defaultValue = "console", required = true)
    private String headerType;

    /**
     * The name of the Launch4j native configuration file
     * The path, if relative, is relative to the pom.xml.
     */
    @Parameter
    private File infile;

    /**
     * The name of the executable you want launch4j to produce.
     * The path, if relative, is relative to the pom.xml.
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.artifactId}.exe")
    private File outfile;

    /**
     * The jar to bundle inside the executable.
     * The path, if relative, is relative to the pom.xml.
     * <p/>
     * If you don't want to wrap the jar, then this value should be the runtime path
     * to the jar relative to the executable. You should also set dontWrapJar to true.
     * <p/>
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
    @Parameter(defaultValue = "${project.name}")
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
     * Whether the executable should ask for admin rights (Windows only).
     */
    @Parameter(defaultValue = "false")
    private boolean requireAdminRights;

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
    @Parameter
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
     * If set to true, it will prevent filling out the VersionInfo params with default values.
     */
    @Parameter(defaultValue = "false")
    private boolean disableVersionInfoDefaults;

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

    /**
     * If set to true it will save final config into a XML file
     */
    @Parameter(defaultValue = "false")
    private boolean saveConfig = false;

    /**
     * If {@link #saveConfig} is set to true, config will be written to this file
     */
    @Parameter(defaultValue = "${project.build.directory}/launch4j-config.xml")
    private File configOutfile;

    /**
     * If set to true, a synchronized block will be used to protect resources
     */
    @Parameter(defaultValue = "false")
    private boolean parallelExecution = false;

    /**
     * If set to true, execution of the plugin will be skipped
     */
    @Parameter(defaultValue = "false")
    private boolean skip = false;

    /**
     * If set to true, make best effort to skip execution if other plugins are
     * generating native image / exe file
     */
    @Parameter(defaultValue = "false")
    private boolean skipIfNativeImage = false;

    private File getJar() {
        return new File(jar);
    }

    @Override
    public void execute() throws MojoExecutionException {
        if (parallelExecution) {
            synchronized (Launch4jMojo.class) {
                doExecute();
            }
        } else {
            doExecute();
        }
    }

    private void doExecute() throws MojoExecutionException {
        if (this.skipExecution()) {
            getLog().debug("Skipping execution of the plugin");
            return;
        }
	
	if (this.skipIfNativeImageParam()) {
	    getLog().info("skipIfNativeImage enabled. Consider rewriting your pom with profiles");
	    getLog().debug("Checking if any other plugin generates a native image / exe file");
	    if (isNativeImageGenerationActive()) {
		return;
	    }
	}
	
        processRequireAdminRights();

        fillSensibleJreDefaults();

        if (!disableVersionInfoDefaults) {
            try {
                if(versionInfo == null) {
                    versionInfo = new VersionInfo();
                }
                versionInfo.setLog(getLog());
                versionInfo.tryFillOutByDefaults(project, outfile);
            } catch (RuntimeException exception) {
                throw new MojoExecutionException("Cannot fill out VersionInfo by defaults", exception);
            }
        }

        final File workDir = setupBuildEnvironment();
        if (infile != null) {
            if (infile.exists()) {
                try {
                    if (getLog().isDebugEnabled()) {
                        getLog().debug("Trying to load Launch4j native configuration using file=" + infile.getAbsolutePath());
                    }
                    // load launch4j config file from <infile>
                    ConfigPersister.getInstance().load(infile);

                    // overwrite several properties analogous to the ANT task
                    // https://sourceforge.net/p/launch4j/git/ci/master/tree/src/net/sf/launch4j/ant/Launch4jTask.java#l84

                    // retrieve the loaded configuration for manipulation
                    Config c = ConfigPersister.getInstance().getConfig();

                    String jarDefaultValue = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName() + ".jar";
                    if (jar != null && !jar.equals(jarDefaultValue)) {
                        getLog().debug("Overwriting config file property 'jar' (='" + c.getJar().getAbsolutePath() + "') with local value '" + getJar().getAbsolutePath() + "'");
                        // only overwrite when != defaultValue (should be != null anytime because of the default value)
                        c.setJar(getJar());
                    }

                    File outFileDefaultValue = new File(project.getBuild().getDirectory() + "/" + project.getArtifactId() + ".exe");
                    if (outfile != null && !outfile.getAbsolutePath().equals(outFileDefaultValue.getAbsolutePath())) {
                        // only overwrite when != defaultValue (should be != null anytime because of the default value)
                        getLog().debug("Overwriting config file property 'outfile' (='" + c.getOutfile().getAbsolutePath() + "') with local value '" + outfile.getAbsolutePath() + "'");
                        c.setOutfile(outfile);
                    }

                    if (icon != null) {
                    	c.setIcon(icon);
                    }

                    if (versionInfo != null) {
                        if (versionInfo.fileVersion != null) {
                            getLog().debug("Overwriting config file property 'versionInfo.fileVersion' (='" + c.getVersionInfo().getFileVersion() + "') with local value '" + versionInfo.fileVersion + "'");
                            c.getVersionInfo().setFileVersion(versionInfo.fileVersion);
                        }
                        if (versionInfo.txtFileVersion != null) {
                            getLog().debug("Overwriting config file property 'versionInfo.txtFileVersion' (='" + c.getVersionInfo().getTxtFileVersion() + "') with local value '" + versionInfo.txtFileVersion + "'");
                            c.getVersionInfo().setTxtFileVersion(versionInfo.txtFileVersion);
                        }
                        if (versionInfo.productVersion != null) {
                            getLog().debug("Overwriting config file property 'versionInfo.productVersion' (='" + c.getVersionInfo().getProductVersion() + "') with local value '" + versionInfo.productVersion + "'");
                            c.getVersionInfo().setProductVersion(versionInfo.productVersion);
                        }
                        if (versionInfo.txtProductVersion != null) {
                            getLog().debug("Overwriting config file property 'versionInfo.txtProductVersion' (='" + c.getVersionInfo().getTxtProductVersion() + "') with local value '" + versionInfo.txtProductVersion + "'");
                            c.getVersionInfo().setTxtProductVersion(versionInfo.txtProductVersion);
                        }
                    }

                    ConfigPersister.getInstance().setAntConfig(c, infile.getParentFile());

                } catch (ConfigPersisterException e) {
                    getLog().error(e);
                    throw new MojoExecutionException("Could not load Launch4j native configuration file", e);
                }
            } else {
                throw new MojoExecutionException("Launch4j native configuration file [" + infile.getAbsolutePath() + "] does not exist!");
            }
        } else {
            final Config c = new Config();

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
            c.setHeaderObjects(relativizeAndCopy(workDir, objs));
            c.setLibs(relativizeAndCopy(workDir, libs));
            c.setVariables(vars);

            if (classPath != null) {
                c.setClassPath(classPath.toL4j(dependencies));
            }
            if (jre != null) {
                jre.deprecationWarning(getLog());
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
                if (messages.bundledJreErr != null) {
                    getLog().warn("<bundledJreErr/> is deprecated, use <jreNotFoundErr/> instead!");
                }
                c.setMessages(messages.toL4j());
            }
            ConfigPersister.getInstance().setAntConfig(c, getBaseDir());
        }

        if (getLog().isDebugEnabled()) {
            printState();
        }

        final Builder builder = new Builder(new MavenLog(getLog()), workDir);
        try {
            builder.build();
        } catch (BuilderException e) {
            getLog().error(e);
            throw new MojoExecutionException("Failed to build the executable; please verify your configuration.", e);
        }

        if (saveConfig) {
            try {
                ConfigPersister.getInstance().save(configOutfile);
            } catch (ConfigPersisterException e) {
                throw new MojoExecutionException("Cannot save config into a XML file", e);
            }
        }
    }
    
    private void fillSensibleJreDefaults() throws MojoExecutionException {
        if (jre == null) {
            jre = new Jre();
        }

        if (jre.path == null) {
            String pathDef = "%JAVA_HOME%;%PATH%";
            getLog().warn("jre.path not set, defaulting to \"" + pathDef + "\"");
            jre.path = pathDef;
        }
    }

    private void processRequireAdminRights() throws MojoExecutionException {
        if (requireAdminRights) {
            getLog().warn("Modifying the resulting exe to always require Admin rights.");
            getLog().warn("Make sure it's necessary. Consider writing your own manifest file.");

            if (manifest != null) {
                getLog().warn("manifest param is already set, overriding. Make sure that's what's intended.");
            }

            try {
                File metaInfDir = new File(basedir, "target/classes/META-INF");
                metaInfDir.mkdir();
                
                File manFile = new File(basedir, DEF_REQADMMAN_FILE);
                byte[] manBytes = FileUtils.readResourceAsBytes(DEF_REQADMMAN_RES);

                FileUtils.writeBytesIfDiff(manFile, manBytes);

                byte[] savedBytes = FileUtils.readBytes(manFile);
                if (Arrays.equals(manBytes, savedBytes)) {
                    getLog().info("Manifest file written to " + manFile);
                }

                manifest = manFile;
            } catch (Exception e) {
                getLog().error(e);
                throw new MojoExecutionException(e);
            }
        }
    }
	
    /**
     * Prepares a little directory for launch4j to do its thing. Launch4j needs a bunch of object files
     * (in the w32api and head directories) and the ld and windres binaries (in the bin directory).
     * The tricky part is that launch4j picks this directory based on where its own jar is sitting.
     * In our case, the jar is going to be sitting in the user's ~/.m2 repository. That's okay: we know
     * maven is allowed to write there. So we'll just add our things to that directory.
     * <p/>
     * This approach is not without flaws.
     * It risks two processes writing to the directory at the same time.
     * But fortunately, once the binary bits are in place, we don't do any more writing there,
     * and launch4j doesn't write there either.
     * Usually ~/.m2 will only be one system or another.
     * But if it's an NFS mount shared by several system types, this approach will break.
     * <p/>
     * Okay, so here is a better proposal: package the plugin without these varying binary files,
     * and put each set of binaries in its own tarball. Download the tarball you need to ~/.m2 and
     * unpack it. Then different systems won't contend for the same space. But then I'll need to hack
     * the l4j code so it permits passing in a work directory and doesn't always base it on
     * the location of its own jarfile.
     *
     * @return the work directory.
     */
    private File setupBuildEnvironment() throws MojoExecutionException {
        createParentFolder();
        Artifact binaryBits = chooseBinaryBits();
        if (retrieveBinaryBits(binaryBits)) {
            return unpackWorkDir(binaryBits);
        } else {
            throw new MojoExecutionException("Artifact: " + binaryBits + " is not available!");
        }
    }

    private void createParentFolder() {
        if (outfile != null) {
            File parent = outfile.getParentFile();
            if (!parent.exists()) {
                getLog().debug("Parent " + parent.getPath() + " does not exist, creating it!");
                boolean created = parent.mkdirs();
                if (created) {
                    getLog().debug("Parent " + parent.getPath() + " has been created!");
                } else {
                    getLog().warn("Cannot create parent " + parent.getPath() + "!");
                }
            }
        }
    }

    /**
     * Unzips the given artifact in-place and returns the newly-unzipped top-level directory.
     * Writes a marker file to prevent unzipping more than once.
     */
    private File unpackWorkDir(Artifact artifact) throws MojoExecutionException {

        getLog().debug("Trying normal search first, all-repo search if normal fails");
        LocalArtifactRequest request = new LocalArtifactRequest(artifact, null, null);
        LocalArtifactResult localArtifact = repositorySystemSession.getLocalRepositoryManager().find(repositorySystemSession, request);
        if (localArtifact == null || localArtifact.getFile() == null) {
            getLog().warn("Cannot obtain file path to " + artifact + ", trying all-repo search");

            request = new LocalArtifactRequest(artifact, repositories, null);
            localArtifact = repositorySystemSession.getLocalRepositoryManager().find(repositorySystemSession, request);
            if (localArtifact == null || localArtifact.getFile() == null) {
                String err = "Cannot obtain file path to " + artifact + " with both normal and all-repo search";
                getLog().error(err);
                throw new MojoExecutionException(err);
            }
        }

        boolean artifactIsSnapshot = !artifact.getVersion().equals(artifact.getBaseVersion());
	
        getLog().debug("Unpacking " + localArtifact + " into " + localArtifact.getFile());
        File platJar = localArtifact.getFile();
        File dest = platJar.getParentFile();
        File marker = new File(dest, platJar.getName() + ".unpacked");
        String n = platJar.getName();
        File workdir = new File(dest, n.substring(0, n.length() - 4));

        // If the artifact is a SNAPSHOT, then a.getVersion() will report the long timestamp,
        // but getFile() will be 1.1-SNAPSHOT.
        // Since getFile() doesn't use the timestamp, all timestamps wind up in the same place.
	
        // WRONG. getFile returns names like
        // "lbfork-launch4j-3.53-20240105.004437-1-workdir-win32.jar" as of
        // 2024-01-05.
        // QUESTION: maybe it depends on Maven's version? Need to support both.
        // FIX: if it contains expanded version replace it back by expandable version.
        if (artifactIsSnapshot && workdir.toString().contains(artifact.getVersion())) {
            String oldWorkdirStr = workdir.toString();
            String newWorkdirStr = oldWorkdirStr.replace(artifact.getVersion(), artifact.getBaseVersion());
            getLog().info("Unexpected workdir, correcting from " + oldWorkdirStr + " to " + newWorkdirStr);
            workdir = new File(newWorkdirStr);
        }
	
        // Therefore we need to expand the jar every time, if the marker file is stale.
        if (marker.exists() && marker.lastModified() > platJar.lastModified()) {
            // if (marker.exists() && marker.platJar.getName().indexOf("SNAPSHOT") == -1) {
            getLog().info("Platform-specific work directory already exists: " + workdir.getAbsolutePath());
        } else {
            // trying to use plexus-archiver here is a miserable waste of time:
            try (JarFile jf = new JarFile(platJar)) {
                Enumeration<JarEntry> en = jf.entries();
                while (en.hasMoreElements()) {
                    JarEntry je = en.nextElement();
                    File outFile = new File(dest, je.getName());
                    if (!outFile.toPath().normalize().startsWith(dest.toPath().normalize())) {
                        throw new RuntimeException("Bad zip entry");
                    }
                    File parent = outFile.getParentFile();
                    if (parent != null) parent.mkdirs();
                    if (je.isDirectory()) {
                        outFile.mkdirs();
                    } else {
                        try (InputStream in = jf.getInputStream(je)) {
                            try (FileOutputStream fout = new FileOutputStream(outFile)) {
                                byte[] buf = new byte[1024];
                                int len;
                                while ((len = in.read(buf)) >= 0) {
                                    fout.write(buf, 0, len);
                                }
                            }
                        }
                        outFile.setLastModified(je.getTime());
                    }
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Error unarchiving " + platJar, e);
            }

            try {
                marker.createNewFile();
                marker.setLastModified(new Date().getTime());
            } catch (IOException e) {
                getLog().warn("Trouble creating marker file " + marker, e);
            }
        }

        setPermissions(workdir);
        getLog().info("Using workdir " + workdir);
        return workdir;
    }

    /**
     * Chmods the helper executables ld and windres on systems where that is necessary.
     */
    private void setPermissions(File workdir) {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            try {
                new ProcessBuilder("chmod", "755", workdir + "/bin/ld").start().waitFor();
                new ProcessBuilder("chmod", "755", workdir + "/bin/windres").start().waitFor();
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
        if (paths == null) return null;

        List<String> result = new ArrayList<>();
        for (String path : paths) {
            Path source = basedir.toPath().resolve(path);
            Path dest = workdir.toPath().resolve(basedir.toPath().relativize(source));

            if (!source.startsWith(basedir.toPath())) {
                throw new MojoExecutionException("File must reside in the project directory: " + path);
            }

            if (Files.exists(source)) {
                try {
                    Files.createDirectories(dest.getParent());
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
    private boolean retrieveBinaryBits(Artifact a) throws MojoExecutionException {

        getLog().debug("Retrieving artifact: " + a + " stored in " + a.getFile());

        try {
            ArtifactRequest request = new ArtifactRequest(a, repositories, null);
            return repositorySystem.resolveArtifact(repositorySystemSession, request).isResolved();
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException("Illegal Argument Exception", e);
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
        String arch = System.getProperty("os.arch");
        getLog().debug("OS = " + os);
        getLog().debug("Architecture = " + arch);

        // See here for possible values of os.name:
        // http://lopica.sourceforge.net/os.html
        if (os.startsWith("Windows")) {
            plat = "win32";
        } else if ("Linux".equals(os)) {
            if ("amd64".equals(arch)) {
                plat = "linux64";
            } else {
                plat = "linux";
            }
        } else if ("Solaris".equals(os) || "SunOS".equals(os)) {
            plat = "solaris";
        } else if ("Mac OS X".equals(os) || "Darwin".equals(os)) {
            plat = "mac";
        } else {
            throw new MojoExecutionException("Sorry, Launch4j doesn't support the '" + os + "' OS.");
        }

        Artifact artifact = new DefaultArtifact(launch4jGroupId, launch4jArtifactId, "workdir-" + plat, "jar", getLaunch4jVersion());
        try {
            ArtifactRequest request = new ArtifactRequest(artifact, repositories, null);

            return repositorySystem.resolveArtifact(repositorySystemSession, request).getArtifact();
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException(e);
        }
    }

    private File getBaseDir() {
        return basedir;
    }

    /**
     * Just prints out how we were configured.
     */
    private void printState() {
        Log log = getLog();
        Config c = ConfigPersister.getInstance().getConfig();

        log.debug("headerType = " + c.getHeaderType());
        log.debug("outfile = " + c.getOutfile());
        log.debug("jar = " + c.getJar());
        log.debug("dontWrapJar = " + c.isDontWrapJar());
        log.debug("errTitle = " + c.getErrTitle());
        log.debug("downloadUrl = " + c.getDownloadUrl());
        log.debug("supportUrl = " + c.getSupportUrl());
        log.debug("cmdLine = " + c.getCmdLine());
        log.debug("chdir = " + c.getChdir());
        log.debug("priority = " + c.getPriority());
        log.debug("stayAlive = " + c.isStayAlive());
        log.debug("restartOnCrash = " + c.isRestartOnCrash());
        log.debug("icon = " + c.getIcon());
        log.debug("objs = " + c.getHeaderObjects());
        log.debug("libs = " + c.getLibs());
        log.debug("vars = " + c.getVariables());
        if (c.getSingleInstance() != null) {
            log.debug("singleInstance.mutexName = " + c.getSingleInstance().getMutexName());
            log.debug("singleInstance.windowTitle = " + c.getSingleInstance().getWindowTitle());
        } else {
            log.debug("singleInstance = null");
        }
        if (c.getJre() != null) {
            log.debug("jre.path = " + c.getJre().getPath());
            log.debug("jre.minVersion = " + c.getJre().getMinVersion());
            log.debug("jre.maxVersion = " + c.getJre().getMaxVersion());
            log.debug("jre.requiresJdk = " + c.getJre().getRequiresJdk());
            log.debug("jre.requires64Bit = " + c.getJre().getRequires64Bit());
            log.debug("jre.initialHeapSize = " + c.getJre().getInitialHeapSize());
            log.debug("jre.initialHeapPercent = " + c.getJre().getInitialHeapPercent());
            log.debug("jre.maxHeapSize = " + c.getJre().getMaxHeapSize());
            log.debug("jre.maxHeapPercent = " + c.getJre().getMaxHeapPercent());
            log.debug("jre.opts = " + c.getJre().getOptions());
        } else {
            log.debug("jre = null");
        }
        if (c.getClassPath() != null) {
            log.debug("classPath.mainClass = " + c.getClassPath().getMainClass());
        }
        if (classPath != null) {
            log.debug("classPath.addDependencies = " + classPath.addDependencies);
            log.debug("classPath.jarLocation = " + classPath.jarLocation);
            log.debug("classPath.preCp = " + classPath.preCp);
            log.debug("classPath.postCp = " + classPath.postCp);
        } else {
            log.info("classpath = null");
        }
        if (c.getSplash() != null) {
            log.debug("splash.file = " + c.getSplash().getFile());
            log.debug("splash.waitForWindow = " + c.getSplash().getWaitForWindow());
            log.debug("splash.timeout = " + c.getSplash().getTimeout());
            log.debug("splash.timoutErr = " + c.getSplash().isTimeoutErr());
        } else {
            log.debug("splash = null");
        }
        if (c.getVersionInfo() != null) {
            log.debug("versionInfo.fileVersion = " + c.getVersionInfo().getFileVersion());
            log.debug("versionInfo.txtFileVersion = " + c.getVersionInfo().getTxtFileVersion());
            log.debug("versionInfo.fileDescription = " + c.getVersionInfo().getFileDescription());
            log.debug("versionInfo.copyright = " + c.getVersionInfo().getCopyright());
            log.debug("versionInfo.productVersion = " + c.getVersionInfo().getProductVersion());
            log.debug("versionInfo.txtProductVersion = " + c.getVersionInfo().getTxtProductVersion());
            log.debug("versionInfo.productName = " + c.getVersionInfo().getProductName());
            log.debug("versionInfo.companyName = " + c.getVersionInfo().getCompanyName());
            log.debug("versionInfo.internalName = " + c.getVersionInfo().getInternalName());
            log.debug("versionInfo.originalFilename = " + c.getVersionInfo().getOriginalFilename());
            log.debug("versionInfo.language = " + c.getVersionInfo().getLanguage());
            log.debug("versionInfo.languageIndex = " + c.getVersionInfo().getLanguageIndex());
            log.debug("versionInfo.trademarks = " + c.getVersionInfo().getTrademarks());
        } else {
            log.debug("versionInfo = null");
        }
        if (c.getMessages() != null) {
            log.debug("messages.startupErr = " + c.getMessages().getStartupErr());
            log.debug("messages.jreNotFoundErr = " + c.getMessages().getJreNotFoundErr());
            log.debug("messages.jreVersionErr = " + c.getMessages().getJreVersionErr());
            log.debug("messages.launcherErr = " + c.getMessages().getLauncherErr());
            log.debug("messages.instanceAlreadyExistsMsg = " + c.getMessages().getInstanceAlreadyExistsMsg());
        } else {
            log.debug("messages = null");
        }
    }

    /**
     * A version of the Launch4j used by the plugin.
     * We want to download the platform-specific bundle whose version matches the Launch4j version,
     * so we have to figure out what version the plugin is using.
     *
     * @return version of Launch4j
     * @throws MojoExecutionException when version is null
     */
    private String getLaunch4jVersion() throws MojoExecutionException {
        String version = null;

        Set<Artifact> pluginArtifacts = oldPluginArtifacts.stream().map(old ->
                new DefaultArtifact(old.getGroupId(), old.getArtifactId(), old.getClassifier(), null, old.getVersion())
        ).collect(Collectors.toSet());

        for (Artifact artifact : pluginArtifacts) {
            if (launch4jGroupId.equals(artifact.getGroupId()) &&
                    launch4jArtifactId.equals(artifact.getArtifactId())
                    && "core".equals(artifact.getClassifier())) {

                version = artifact.getVersion();
                getLog().info("Found launch4j version " + version);
                break;
            }
        }

        if (version == null) {
            throw new MojoExecutionException("Impossible to find which Launch4j version to use, no compatible version found in classpath");
        }

        return version;
    }

    /**
     * Checks if execution of the plugin should be skipped
     *
     * @return true to skip execution
     */
    private boolean skipExecution() {
        getLog().debug("skip = " + this.skip);
	String sysProp = System.getProperty("skipLaunch4j");
        getLog().debug("skipLaunch4j = " + sysProp);

	return skip || (sysProp != null && !sysProp.equalsIgnoreCase("false"));
	
    }
    
    private boolean skipIfNativeImageParam() {
        getLog().debug("skipIfNativeImage = " + this.skipIfNativeImage);
	String sysProp = System.getProperty("skipLaunch4jIfNativeImage");
        getLog().debug("skipLaunch4jIfNativeImage = " + sysProp);
	
        return skipIfNativeImage || (sysProp != null && !sysProp.equalsIgnoreCase("false"));
    }
    
   private boolean isNativeImageGenerationActive() {
	String graalKey = "org.graalvm.buildtools:native-maven-plugin";
	Log log = getLog();
	MavenProject curProj = session.getCurrentProject();
	Build build = curProj.getBuild();
	String packaging = curProj.getPackaging();

	if (packaging.equalsIgnoreCase("native-image")) {
	    log.info("Current project's packaging is set to native-image, skipping.");
	    return true;
	}

	// check if there's a Graal plugin and if it has any enabled compile executions
	Plugin graalPlugin = build.getPluginsAsMap().get(graalKey);

	// check if 1) there's a goal with "compile", 2) with no skip params, 3) and no skip sysprops
	if (graalPlugin != null) {
            for (PluginExecution pe : graalPlugin.getExecutions()) {
	        String goalsStr = pe.getGoals().toString().toLowerCase();
	        String confStr = pe.getConfiguration().toString().toLowerCase();
	    
	        if (!goalsStr.contains("compile") || pe.getPhase() == null) {
                    continue;
	        }
		if (confStr.contains("<skip>true") || confStr.contains("<skipNativeBuild>true"))
		{
		    continue;
		}
		String sysProp = System.getProperty("skipNativeBuild");
		if (sysProp != null && !sysProp.equalsIgnoreCase("false")) {
		    continue;
		}
		
                log.info("There is an active Graal plugin execution, skipping.");
		return true;
            }
	}

	return false;
    }


    @Override
    public String toString() {
        return "Launch4jMojo{" +
                "headerType='" + headerType + '\'' +
                ", infile=" + infile +
                ", outfile=" + outfile +
                ", jar='" + jar + '\'' +
                ", dontWrapJar=" + dontWrapJar +
                ", errTitle='" + errTitle + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", supportUrl='" + supportUrl + '\'' +
                ", cmdLine='" + cmdLine + '\'' +
                ", chdir='" + chdir + '\'' +
                ", priority='" + priority + '\'' +
                ", stayAlive=" + stayAlive +
                ", restartOnCrash=" + restartOnCrash +
                ", icon=" + icon +
                ", requireAdminRights=" + requireAdminRights +
                ", objs=" + objs +
                ", libs=" + libs +
                ", vars=" + vars +
                ", jre=" + jre +
                ", classPath=" + classPath +
                ", singleInstance=" + singleInstance +
                ", splash=" + splash +
                ", versionInfo=" + versionInfo +
                ", disableVersionInfoDefaults=" + disableVersionInfoDefaults +
                ", messages=" + messages +
                ", manifest=" + manifest +
                ", saveConfig=" + saveConfig +
                ", configOutfile=" + configOutfile +
                ", parallelExecution=" + parallelExecution +
                ", skip=" + skip +
                '}';
    }
}
