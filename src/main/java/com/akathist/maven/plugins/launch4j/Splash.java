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

public class Splash {

	/**
	 * The path (relative to the executable when distributed) to the splash page image.
	 */
	File file;

	/**
	 * If true, the splash screen will close automatically as soon as an error window or java window appears.
	 * If false, the splash screen will not close until {@link #timeout} sections. Defaults to true.
	 *
	 * @parameter default-value=true;
	 */
	boolean waitForWindow;

	/**
	 * The number of seconds to keep the splash screen open before automatically closing it.
	 * Defaults to 60.
	 *
	 * @parameter default-value=60
	 */
	int timeout;

	/**
	 * If true, an error message will appear if the app hasn't started in {@link #timeout} seconds.
	 * If false, the splash screen will close quietly. Defaults to true.
	 *
	 * @parameter default-value=true
	 */
	boolean timeoutErr;

	net.sf.launch4j.config.Splash toL4j() {
		net.sf.launch4j.config.Splash ret = new net.sf.launch4j.config.Splash();

		ret.setFile(file);
		ret.setWaitForWindow(waitForWindow);
		ret.setTimeout(timeout);
		ret.setTimeoutErr(timeoutErr);

		return ret;
	}

}
