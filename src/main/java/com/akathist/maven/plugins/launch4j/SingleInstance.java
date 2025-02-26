/*
 * Maven Launch4j Plugin
 * Copyright (c) 2006 Paul Jungwirth
 * Copyright (c) 2011-2025 Lukasz Lenart
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
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

import org.apache.maven.plugins.annotations.Parameter;

/**
 * Details about running your application as a single instance.
 */
public class SingleInstance {

    @Parameter
    String mutexName;

    @Parameter
    String windowTitle;

    net.sf.launch4j.config.SingleInstance toL4j() {
        net.sf.launch4j.config.SingleInstance ret = new net.sf.launch4j.config.SingleInstance();

        ret.setMutexName(mutexName);
        ret.setWindowTitle(windowTitle);

        return ret;
    }

}
