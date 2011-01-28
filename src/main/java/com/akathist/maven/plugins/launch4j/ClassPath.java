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

import java.io.*;
import java.util.*;
import org.apache.maven.model.Dependency;
import org.apache.maven.artifact.Artifact;

public class ClassPath {

	/**
	 * The main class to run. This is not required if you are wrapping an executable jar.
	 *
	 * @parameter
	 */
	String mainClass;

	/**
	 * The launch4j executable sets up a classpath before running your jar, but it must know what the
	 * classpath should be. If you set this property to true, the plugin will indicate a classpath
	 * based on all the dependencies your program will need at runtime. You can augment this classpath
	 * using the preCp and postCp properties.
	 *
	 * @parameter default-value=true
	 */
	boolean addDependencies = true;

	/**
	 * If you want maven to build the classpath from dependencies, you can optionally set the jarLocation,
	 * which is the location of the jars in your distro relative to the executable. So if your distro
	 * has the exe at the top level and all the jars in a lib directory, you could set this to &quot;lib.&quot;
	 * This property does not affect preCp and postCp.
	 *
	 * @parameter
	 */
	String jarLocation;

	/**
	 * Part of the classpath that the executable should give to your application.
	 * Paths are relative to the executable and should be in Windows format (separated by a semicolon).
	 * You don't have to list all your dependencies here; the plugin will include them by default
	 * after this list.
	 *
	 * @parameter
	 */
	String preCp;

	/**
	 * Part of the classpath that the executable should give to your application.
	 * Paths are relative to the executable and should be in Windows format (separated by a semicolon).
	 * You don't have to list all your dependencies here; the plugin will include them by default
	 * before this list.
	 *
	 * @parameter
	 */
	String postCp;

	private void addToCp(List cp, String cpStr) {
		cp.addAll(Arrays.asList(cpStr.split("\\s*;\\s*")));
	}

	net.sf.launch4j.config.ClassPath toL4j(Set dependencies) {
		net.sf.launch4j.config.ClassPath ret = new net.sf.launch4j.config.ClassPath();
		ret.setMainClass(mainClass);
		
		List cp = new ArrayList();
		if (preCp != null) addToCp(cp, preCp);

		if (addDependencies) {
			if (jarLocation == null) jarLocation = "";
			else if ( ! jarLocation.endsWith("/")) jarLocation += "/";

			Iterator i = dependencies.iterator();
			while (i.hasNext()) {
				Artifact dep = (Artifact)i.next();
				if (Artifact.SCOPE_COMPILE.equals(dep.getScope()) ||
					Artifact.SCOPE_RUNTIME.equals(dep.getScope())) {

					String depFilename;
					depFilename = dep.getFile().getName();
					// System.out.println("dep = " + depFilename);
					cp.add(jarLocation + depFilename);
				}
			}
		}

		if (postCp != null) addToCp(cp, postCp);
		ret.setPaths(cp);

		return ret;
	}

}
