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

package org.hmx.scitos.core.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.hmx.scitos.core.XmlResourceBundleControl;
import org.hmx.scitos.core.option.Option;
import org.hmx.scitos.core.util.ClassPathUtil;

/**
 * Central handler of the language files.
 *
 * @param <M>
 *            type of handled messages
 */
public final class Translator<M extends ILocalizableMessage> {

    /** Resource bundles containing localized values. */
    private final ResourceBundle bundle;

    /**
     * Main constructor.
     *
     * @param messageType
     *            message type (to deduce the resource bundle's base name from)
     */
    public Translator(final Class<M> messageType) {
        this(messageType, Option.TRANSLATION.getValueAsLocale());
    }

    /**
     * Constructor for a specific {@code Locale}.
     *
     * @param messageType
     *            message type (to deduce the resource bundle's base name from)
     * @param locale
     *            the locale to retrieve the translated messages for
     */
    public Translator(final Class<M> messageType, final Locale locale) {
        this.bundle = ResourceBundle.getBundle(messageType.getName(), locale, new XmlResourceBundleControl());
    }

    /**
     * Gets the single translation for the given {@link Message} in the designated {@link Locale}.
     *
     * @param message
     *            single message to get the translation for
     * @return message translation
     */
    public String getLocalizedMessage(final M message) {
        return this.bundle.getString(message.getKey());
    }

    /**
     * Determine the Locales for which message files are provided. This excludes the default translation.
     *
     * @param messageType
     *            the type of messages to check available languages/countries for
     * @return Locales with explicit translations for the given message type
     */
    public static List<Locale> getAvailableLocales(final Class<? extends ILocalizableMessage> messageType) {
        final List<String> messageFiles = ClassPathUtil.getFileResourcePaths(messageType, messageType.getSimpleName() + "_.+[.]xml");
        final List<Locale> availableLocales = new ArrayList<Locale>(messageFiles.size());
        // the full file path starts like the full class name, plus a leading slash '/' and the trailing underscore '_'
        final int prefixLength = messageType.getName().length() + 2;
        for (final String messageFilePath : messageFiles) {
            // get rid of the parent path and the '.xml' suffix
            final String identifier = messageFilePath.substring(prefixLength, messageFilePath.length() - 4);
            // construct the Locale from the identifier String
            final String language;
            final String country;
            final String variant;
            final int countryIndex = identifier.indexOf('_');
            if (countryIndex == -1) {
                language = identifier;
                country = "";
                variant = "";
            } else {
                language = identifier.substring(0, countryIndex);
                final int variantIndex = identifier.indexOf('_', countryIndex + 1);
                if (variantIndex == -1) {
                    country = identifier.substring(countryIndex + 1);
                    variant = "";
                } else {
                    country = identifier.substring(countryIndex + 1, variantIndex);
                    variant = identifier.substring(variantIndex + 1);
                }
            }
            availableLocales.add(new Locale(language, country, variant));
        }
        return availableLocales;
    }
}
