/*
   Copyright (C) 2015 HermeneutiX.org

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

package org.hmx.scitos.core.option;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Handler for the available user {@link Option settings}.
 *
 * @param <O>
 *            type of handled settings
 */
public final class OptionHandler<O extends Enum<? extends IOptionSetting> & IOptionSetting> {

    /** The path to the represented settings file relative to the jar's directory containing this class. */
    private static final String PATH = "/{0}-settings.xml";
    /**
     * The singleton instances for each {@link IOptionSetting} implementation in use.
     */
    private static final Map<Class<? extends IOptionSetting>, OptionHandler<?>> instances = Collections
            .synchronizedMap(new HashMap<Class<? extends IOptionSetting>, OptionHandler<?>>());

    /** The loaded options from the represented settings file, potentially altered by the user. */
    private final Properties chosenOptions = new Properties();
    /** The path to the represented settings file. */
    private final String filePath;

    /**
     * Getter for the singleton instance of the given {@link IOptionSetting} implementation type.
     *
     * @param optionImplClass
     *            type of the handled settings
     * @param <S>
     *            type of the handled settings
     * @return active instance
     */
    public static <S extends Enum<? extends IOptionSetting> & IOptionSetting> OptionHandler<S> getInstance(final Class<S> optionImplClass) {
        synchronized (OptionHandler.instances) {
            final OptionHandler<S> singleton;
            if (OptionHandler.instances.containsKey(optionImplClass)) {
                singleton = (OptionHandler<S>) OptionHandler.instances.get(optionImplClass);
            } else {
                singleton = new OptionHandler<S>(optionImplClass);
                OptionHandler.instances.put(optionImplClass, singleton);
            }
            return singleton;
        }
    }

    /**
     * Constructor of a singleton instance, avoiding the multiple loading of options files and to keep the ability to set and store properties at
     * runtime.
     *
     * @param optionImplClass
     *            type of handled settings
     */
    private OptionHandler(final Class<O> optionImplClass) {
        this.filePath = OptionHandler.buildOptionFilePath(optionImplClass);
        final File targetFile = new File(this.filePath);
        if (targetFile.exists() && targetFile.canRead()) {
            FileInputStream input = null;
            try {
                input = new FileInputStream(targetFile);
                this.chosenOptions.loadFromXML(input);
            } catch (final IOException e) {
                // no options loaded
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (final IOException ioex) {
                        // at least we tried
                    }
                }
            }
        }
    }

    /**
     * Construct the path, where the options file for the given class is expected to be.
     *
     * @param optionImplClass
     *            specific option class to get the path for
     * @return path where the associated options file is expected to be
     */
    public static String buildOptionFilePath(final Class<?> optionImplClass) {
        // try file next to running jar, independent from current directory
        String jarDir = ".";
        try {
            jarDir = new File(optionImplClass.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        } catch (final URISyntaxException use) {
            use.printStackTrace();
        }
        return jarDir + MessageFormat.format(OptionHandler.PATH, optionImplClass.getSimpleName().toLowerCase());
    }

    /**
     * Getter for the entry in the options file for the given key.
     *
     * @param key
     *            key of the required option entry
     * @return option entry for the key; returns <code>null</code> if no entry found
     */
    public String getProperty(final O key) {
        return this.chosenOptions.getProperty(key.getKey());
    }

    /**
     * Setter for the entry in the options file with the given key. The value is actually NOT persisted, but only stored locally for use at runtime or
     * explicit persisting via {@link #persistChanges()}.
     *
     * @param key
     *            key of the option entry to set
     * @param value
     *            value to set
     */
    public void setProperty(final O key, final String value) {
        this.chosenOptions.setProperty(key.getKey(), value);
    }

    /**
     * Saves the current option entries and their values in the associated options file, to be available at the next application start.
     */
    public void persistChanges() {
        final File targetFile = new File(this.filePath);
        if (!targetFile.exists() || targetFile.canWrite()) {
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(targetFile);
                this.chosenOptions.storeToXML(output, null);
            } catch (final IOException e) {
                // unable to store
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (final IOException ioex) {
                        // at least we tried
                    }
                }
            }
        }
    }
}
