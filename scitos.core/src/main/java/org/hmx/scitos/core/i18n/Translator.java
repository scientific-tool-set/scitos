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

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Central handler of the language files.
 *
 * @param <M>
 *            type of handled messages
 */
final class Translator<M extends Enum<? extends ILocalizableMessage> & ILocalizableMessage> {

    /** The xml tag representing a single language entry. */
    private static final String TAG_ENTRY = "entry";
    /** The attribute name holding the unique language file key for the respective entry in the language file. */
    private static final String ATTRIBUTE_KEY = "key";

    /** The pattern for locating the individual (localized) language files. */
    private final String filePathPattern;
    /** Cached translations: default values. */
    private final Map<M, String> defaultTranslations = new HashMap<M, String>();
    /** Cached translations: localized values. */
    private final Map<Locale, Map<M, String>> localizedTranslations = Collections.synchronizedMap(new HashMap<Locale, Map<M, String>>(1));

    /**
     * Main constructor.
     *
     * @param messages
     *            all messages to be translated
     */
    Translator(final Collection<M> messages) {
        final String baseFileName = messages.iterator().next().getClass().getSimpleName().toLowerCase() + 's';
        final String defaultFilePath = "/lang/" + baseFileName + ".xml";
        // load the default language values first (to fall back on if the specific language file is incomplete)
        final Map<M, String> translations = this.loadTranslations(messages, defaultFilePath);
        if (translations != null) {
            this.defaultTranslations.putAll(translations);
        }
        this.filePathPattern = "/lang/" + baseFileName + "_{0}.xml";
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
    String getLocalizedMessage(final M message, final Locale locale) {
        synchronized (this.localizedTranslations) {
            if (!this.localizedTranslations.containsKey(locale)) {
                final String languageFilePath = MessageFormat.format(this.filePathPattern, Locale.getDefault().getLanguage());
                final Map<M, String> translations = this.loadTranslations(this.defaultTranslations.keySet(), languageFilePath);
                this.localizedTranslations.put(locale, translations);
            }
            final Map<M, String> translations = this.localizedTranslations.get(locale);
            final String value = translations.get(message);
            if (value == null) {
                translations.put(message, message.getKey());
            }
            return value;
        }
    }

    /**
     * Collects all translations associated with the language file at the given path and fills out potential gaps with the default translations.
     *
     * @param messages
     *            all expected messages
     * @param targetPath
     *            where to find the targeted language file
     * @return collection of all translations from the given language file (or the default translations if no such file existed)
     */
    private Map<M, String> loadTranslations(final Collection<M> messages, final String targetPath) {
        final InputStream targetStream = Translator.class.getResourceAsStream(targetPath);
        if (targetStream == null) {
            // avoid multiple lookups by referencing default
            return this.defaultTranslations;
        }
        try {
            final Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(targetStream);
            final NodeList entries = xml.getDocumentElement().getElementsByTagName(Translator.TAG_ENTRY);
            final int entryCount = entries.getLength();
            final Map<String, String> languageFileEntries = new HashMap<String, String>(entryCount);
            for (int i = 0; i < entryCount; i++) {
                final Node singleEntry = entries.item(i);
                languageFileEntries.put(((Element) singleEntry).getAttribute(Translator.ATTRIBUTE_KEY), singleEntry.getTextContent());
            }
            final Map<M, String> translations;
            if (this.defaultTranslations == null) {
                translations = new HashMap<M, String>(languageFileEntries.size());
            } else {
                // set default texts as starting point
                translations = new HashMap<M, String>(this.defaultTranslations);
            }
            // override defaults with given translations
            for (final M singleMessage : messages) {
                if (languageFileEntries.containsKey(singleMessage.getKey())) {
                    translations.put(singleMessage, languageFileEntries.get(singleMessage.getKey()));
                }
            }
            return translations;
        } catch (final IOException ioex) {
            // error while parse the file at the given path
            ioex.printStackTrace();
        } catch (final SAXException saxex) {
            // error while parse the file at the given path
            saxex.printStackTrace();
        } catch (final ParserConfigurationException pce) {
            // error while initializing a DocumentBuilder instance
            pce.printStackTrace();
        } finally {
            if (targetStream != null) {
                try {
                    targetStream.close();
                } catch (final IOException ioex) {
                    // at least we tried
                }
            }
        }
        // avoid multiple lookups by referencing default
        return this.defaultTranslations;
    }
}
