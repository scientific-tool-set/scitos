/*
   Copyright (C) 2016 HermeneutiX.org

   This file is part of SciToS.

   SciToS is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   SciToS is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with SciToS. If not, see <http://www.gnu.org/licenses/>.
 */

package org.hmx.scitos.core.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/** Helper class for handling files on the classpath. */
public final class ClassPathUtil {

    /**
     * Collect the full resource paths of files, matching the given regular expression, in the package of the specified class.
     *
     * @param clazz
     *            the class to look up neighboring files for (determines the package in which to search)
     * @param fileNameRegex
     *            regular expression to be matched by looked up files
     * @return absolute resource paths for files that match the given regular expression
     * @see Class#getResource(String)
     * @see Class#getResourceAsStream(String)
     */
    public static List<String> getFileResourcePaths(final Class<?> clazz, final String fileNameRegex) {
        final List<String> paths = new LinkedList<>();
        // check if this is being executed from within a jar file
        final Pattern pattern = Pattern.compile(fileNameRegex);
        final File jarFile = new File(clazz.getProtectionDomain().getCodeSource().getLocation().getPath());
        String packagePath = clazz.getPackage().getName().replaceAll("[.]", "/");
        if (jarFile.isFile()) {
            JarFile jar = null;
            try {
                // iterate over ALL entries in the jar file
                jar = new JarFile(jarFile);
                packagePath = packagePath + '/';
                final Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    final String entryPath = entries.nextElement().getName();
                    if (entryPath.startsWith(packagePath)) {
                        final String fileName = entryPath.substring(packagePath.length());
                        // skip sub packages and files that don't match the regular expression
                        if (!fileName.contains("/") && pattern.matcher(fileName).matches()) {
                            // remember absolute path for matching file
                            paths.add('/' + entryPath);
                        }
                    }
                }
            } catch (final IOException expected) {
                // we should be able to access the JAR file from within it, and never get this exception
            } finally {
                if (jar != null) {
                    try {
                        jar.close();
                    } catch (final IOException expected) {
                        // at least we tried
                    }
                }
            }
        } else {
            // we are inside an IDE and need to handle this differently
            packagePath = '/' + packagePath;
            // get the URL for the targeted package
            final URL url = clazz.getResource(packagePath);
            if (url != null) {
                packagePath = packagePath + '/';
                try {
                    // iterate all files that are in the given package
                    for (final File singleFile : new File(url.toURI()).listFiles()) {
                        final String name = singleFile.getName();
                        // skip sub packages and files that don't match the regular expression
                        if (singleFile.isFile() && pattern.matcher(name).matches()) {
                            // remember absolute path for matching file
                            paths.add(packagePath + singleFile.getName());
                        }
                    }
                } catch (final URISyntaxException expected) {
                    // never happens
                }
            }
        }
        return paths;
    }
}
