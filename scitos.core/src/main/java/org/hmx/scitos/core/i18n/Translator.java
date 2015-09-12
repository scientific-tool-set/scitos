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

package org.hmx.scitos.core.i18n;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Central handler of the language files.
 *
 * @param <M>
 *            type of handled messages
 */
public final class Translator<M extends ILocalizableMessage> {

    /** The resource bundle's (qualified) base name for the individual language files. */
    private final String baseName;
    /** Resource bundles containing localized values. */
    private final Map<Locale, ResourceBundle> localizedTranslations = Collections.synchronizedMap(new HashMap<Locale, ResourceBundle>(1));

    /**
     * Main constructor.
     * 
     * @param messageType
     *            message type (to deduce the resource bundle's base name from)
     */
    public Translator(final Class<M> messageType) {
        this.baseName = "lang." + messageType.getSimpleName().toLowerCase(Locale.ENGLISH) + 's';
    }

    /**
     * Gets the single translation for the given {@link Message} in the designated {@link Locale}.
     *
     * @param message
     *            single message to get the translation for
     * @param locale
     *            specific {@link Locale} to find the translation for
     * @return message translation
     */
    public String getLocalizedMessage(final M message, final Locale locale) {
        final ResourceBundle bundle;
        if (!this.localizedTranslations.containsKey(locale)) {
            bundle = ResourceBundle.getBundle(this.baseName, locale, new XMLResourceBundleControl());
            this.localizedTranslations.put(locale, bundle);
        } else {
            bundle = this.localizedTranslations.get(locale);
        }
        return bundle.getString(message.getKey());
    }
}
