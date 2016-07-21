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

package org.hmx.scitos.core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/** Resource bundle handler, that only handles property files in XML form. */
public final class XmlResourceBundleControl extends ResourceBundle.Control {

    /** Default constructor. */
    public XmlResourceBundleControl() {
        super();
    }

    @Override
    public List<String> getFormats(final String baseName) {
        return Collections.singletonList("xml");
    }

    @Override
    public Locale getFallbackLocale(final String baseName, final Locale locale) {
        if (baseName == null || locale == null) {
            throw new NullPointerException();
        }
        // avoid falling back on the default locale
        return null;
    }

    @Override
    public ResourceBundle newBundle(final String baseName, final Locale locale, final String format, final ClassLoader loader, final boolean reload)
            throws IllegalAccessException, InstantiationException, IOException {
        if (baseName == null || locale == null || format == null || loader == null) {
            throw new NullPointerException();
        }
        ResourceBundle bundle = null;
        final String bundleName = this.toBundleName(baseName, locale);
        final String resourceName = this.toResourceName(bundleName, format);
        final URL url = loader.getResource(resourceName);
        if (url != null) {
            final URLConnection connection = url.openConnection();
            if (connection != null) {
                connection.setUseCaches(!reload);
                final InputStream stream = connection.getInputStream();
                if (stream != null) {
                    try {
                        final BufferedInputStream bis = new BufferedInputStream(stream);
                        try {
                            bundle = new XmlResourceBundle(bis);
                        } finally {
                            bis.close();
                        }
                    } finally {
                        stream.close();
                    }
                }
            }
        }
        return bundle;
    }

    /** Resource bundle backed by a properties file in XML form. */
    private static final class XmlResourceBundle extends ResourceBundle {

        /** The properties file capable of reading the XML form. */
        private final Properties xmlProperties;

        /**
         * Constructor: assuming an XML structured properties file in the given stream.
         *
         * @param stream
         *            input to load the resource bundle from
         * @throws IOException
         *             error while reading the given stream
         */
        XmlResourceBundle(final InputStream stream) throws IOException {
            this.xmlProperties = new Properties();
            this.xmlProperties.loadFromXML(stream);
        }

        @Override
        protected Object handleGetObject(final String key) {
            return this.xmlProperties.getProperty(key);
        }

        @Override
        public Enumeration<String> getKeys() {
            return Collections.enumeration(this.xmlProperties.stringPropertyNames());
        }
    }
}