package org.hmx.scitos.core.i18n;

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
class XMLResourceBundleControl extends ResourceBundle.Control {

    @Override
    public List<String> getFormats(final String baseName) {
        return Collections.singletonList("xml");
    }

    @Override
    public ResourceBundle newBundle(final String baseName, final Locale locale, final String format, final ClassLoader loader, final boolean reload)
            throws IllegalAccessException, InstantiationException, IOException {
        if (baseName == null || locale == null || format == null || loader == null) {
            throw new NullPointerException();
        }
        final String bundleName = toBundleName(baseName, locale);
        final String resourceName = toResourceName(bundleName, "xml");
        final ResourceBundle bundle;
        final URL url = loader.getResource(resourceName);
        if (url == null) {
            bundle = null;
        } else {
            final URLConnection connection = url.openConnection();
            if (connection == null) {
                bundle = null;
            } else {
                connection.setUseCaches(!reload);
                final InputStream stream = connection.getInputStream();
                if (stream == null) {
                    bundle = null;
                } else {
                    final BufferedInputStream bis = new BufferedInputStream(stream);
                    try {
                        bundle = new XMLResourceBundle(bis);
                    } finally {
                        bis.close();
                    }
                }
            }
        }
        return bundle;
    }

    /** Resource bundle backed by a properties file in XML form. */
    private static class XMLResourceBundle extends ResourceBundle {

        /** The properties file capable of reading the XML form. */
        private Properties xmlProperties;

        /**
         * Constructor: assuming an XML structured properties file in the given stream.
         * 
         * @param stream
         *            input to load the resource bundle from
         * @throws IOException
         *             error while reading the given stream
         */
        XMLResourceBundle(final InputStream stream) throws IOException {
            this.xmlProperties = new Properties();
            this.xmlProperties.loadFromXML(stream);
        }

        @Override
        protected Object handleGetObject(String key) {
            return this.xmlProperties.getProperty(key);
        }

        @Override
        public Enumeration<String> getKeys() {
            return Collections.enumeration(this.xmlProperties.stringPropertyNames());
        }
    }
}