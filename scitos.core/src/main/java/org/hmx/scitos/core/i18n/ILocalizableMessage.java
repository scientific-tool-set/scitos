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

import java.util.Locale;

/**
 * Identifier interface for a type of message that can be handled by a {@link Translator}.
 */
interface ILocalizableMessage {

    /**
     * Getter for the actual language file key.
     *
     * @return actual language file key
     */
    String getKey();

    /**
     * Getter for the message's translation in the default {@link Locale}.
     *
     * @return translation of this message
     */
    String get();

    /**
     * Getter for the message's translation in the given {@link Locale}.
     *
     * @param specificLocale
     *            {@link Locale} to get the translation for
     * @return translation of this message
     */
    String get(Locale specificLocale);
}
