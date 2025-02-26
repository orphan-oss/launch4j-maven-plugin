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

import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Details about which jre the executable should call.
 */
public class Jre {

    /**
     * The <path> property is used to specify absolute or relative JRE paths, it does not rely
     * on the current directory or <chdir>.
     * Note: the path is not checked until the actual application execution.
     * The <path> is now required and always used for searching before the registry,
     * to ensure compatibility with the latest runtimes, which by default
     * do not add registry keys during installation.
     */
    @Parameter(required = true)
    String path;

    /**
     * Sets jre's bundledJre64Bit flag
     *
     * @deprecated Replaced with <requires64Bit> which works during path and registry search.
     * @since using Launch4j 3.50
     */
    @Parameter(defaultValue = "false")
    @Deprecated
    String bundledJre64Bit;

    /**
     * Sets jre's bundledJreAsFallback flag
     *
     * @deprecated Removed, path search is always first and registry search second
     *             in order to improve compatibility with modern runtimes
     * @since using Launch4j 3.50
     */
    @Parameter(defaultValue = "false")
    @Deprecated
    String bundledJreAsFallback;

    /**
     * When set to "true", limits the runtimes to 64-Bit only, "false" will use 64-Bit or 32-Bit
     * depending on which is found. This option works with path and registry search.
     * @since version 2.2.0
     */
    @Parameter(defaultValue = "false")
    boolean requires64Bit;

    /**
     * Use this property if you want the executable to search the system for a jre.
     * It names the minimum version acceptable, in x.x.x[_xx] format.
     * <p>
     * If you specify this property without giving a path, then the executable will search for a jre
     * and, none is found, display the java download page.
     * <p>
     * If you include a path also, the executable will try that path before searching for jre matching minVersion.
     * <p>
     * In either case, you can also specify a maxVersion.
     */
    String minVersion;

    /**
     * If you specify minVersion, you can also use maxVersion to further constrain the search for a jre.
     * This property should be in the format x.x.x[_xx].
     */
    String maxVersion;

    /**
     * Allows you to specify a preference for a public JRE or a private JDK runtime.
     * <p>
     * Valid values are:
     * <table border="1">
     * <tr>
     * <td>jreOnly</td>
     * <td>Always use a public JRE</td>
     * </tr>
     * <tr>
     * <td>preferJre</td>
     * <td>Prefer a public JRE, but use a JDK private runtime if it is newer than the public JRE</td>
     * </tr>
     * <tr>
     * <td>preferJdk</td>
     * <td>Prefer a JDK private runtime, but use a public JRE if it is newer than the JDK</td>
     * </tr>
     * <tr>
     * <td>jdkOnly</td>
     * <td>Always use a private JDK runtime (fails if there is no JDK installed)</td>
     * </tr>
     * </table>
     *
     * @deprecated Replaces with <requiresJdk> which works during path and registry search.
     * @since using Launch4j 3.50
     */
    @Parameter(defaultValue = "preferJre")
    @Deprecated
    String jdkPreference;

    /**
     * When set to "true" only a JDK will be used for execution. An additional check will be performed
     * if javac is available during path and registry search.
     * @since version 2.2.0
     */
    @Parameter(defaultValue = "false")
    boolean requiresJdk;

    /**
     * Sets java's initial heap size in MB, like the -Xms flag.
     */
    int initialHeapSize;

    /**
     * Sets java's initial heap size in percent of free memory.
     */
    int initialHeapPercent;

    /**
     * Sets java's maximum heap size in MB, like the -Xmx flag.
     */
    int maxHeapSize;

    /**
     * Sets java's maximum heap size in percent of free memory.
     */
    int maxHeapPercent;

    /**
     * Use this to pass arbitrary options to the java/javaw program.
     * For instance, you can say:
     * <pre>
     * &lt;opt&gt;-Dlaunch4j.exedir="%EXEDIR%"&lt;/opt&gt;
     * &lt;opt&gt;-Dlaunch4j.exefile="%EXEFILE%"&lt;/opt&gt;
     * &lt;opt&gt;-Denv.path="%Path%"&lt;/opt&gt;
     * &lt;opt&gt;-Dsettings="%HomeDrive%%HomePath%\\settings.ini"&lt;/opt&gt;
     * </pre>
     */
    List<String> opts;

    /**
     * Sets JVM version to use: 32 bits, 64 bits or 64/32 bits
     * Possible values: 32, 64, 64/32 - it will fallback to default value if different option was used
     * Default value is: 64/32
     *
     * @deprecated Replaced with <requires64Bit> which works during path and registry search.
     * @since using Launch4j 3.50
     */
    @Parameter(defaultValue = "64/32")
    @Deprecated
    String runtimeBits;

    net.sf.launch4j.config.Jre toL4j() {
        net.sf.launch4j.config.Jre ret = new net.sf.launch4j.config.Jre();

        ret.setPath(path);
        ret.setRequires64Bit(requires64Bit);
        ret.setMinVersion(minVersion);
        ret.setMaxVersion(maxVersion);
        ret.setRequiresJdk(requiresJdk);
        ret.setInitialHeapSize(initialHeapSize);
        ret.setInitialHeapPercent(initialHeapPercent);
        ret.setMaxHeapSize(maxHeapSize);
        ret.setMaxHeapPercent(maxHeapPercent);
        ret.setOptions(opts);

        return ret;
    }

    @Override
    public String toString() {
        return "Jre{" +
                "path='" + path + '\'' +
                ", requires64Bit=" + requires64Bit +
                ", minVersion='" + minVersion + '\'' +
                ", maxVersion='" + maxVersion + '\'' +
                ", requiresJdk=" + requiresJdk +
                ", initialHeapSize=" + initialHeapSize +
                ", initialHeapPercent=" + initialHeapPercent +
                ", maxHeapSize=" + maxHeapSize +
                ", maxHeapPercent=" + maxHeapPercent +
                ", opts=" + opts +
                '}';
    }

    public void deprecationWarning(Log log) {
        if (this.bundledJreAsFallback != null) {
            log.warn("<bundledJreAsFallback/> has been removed! It has no effect!");
        }
        if (this.bundledJre64Bit != null) {
            log.warn("<bundledJre64Bit/> is deprecated, use <requires64Bit/> instead!");
        }
        if (this.runtimeBits != null) {
            log.warn("<runtimeBits/> is deprecated, use <requires64Bit/> instead!");
        }
        if (this.jdkPreference != null) {
            log.warn("<jdkPreference/> is deprecated, use <requiresJdk/> instead!");
        }
    }
}
