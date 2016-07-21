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

package org.hmx.scitos.hmx.core;

import org.hmx.scitos.core.i18n.ILocalizableMessage;
import org.hmx.scitos.core.i18n.Translator;
import org.hmx.scitos.hmx.domain.model.Relation;

/**
 * Handler for the (no longer provided) internationalization of the role values of associates in a {@link Relation}. This class is only used for
 * compatibilty reasons when converting an old HermeneutiX file (from when it was a standalone application) into its SciToS equivalent.
 */
class CompatibleRelationRole implements ILocalizableMessage {

    /** The (old) associate role key. */
    private final String key;
    /** The resource bundle wrapper to use. */
    private final Translator<CompatibleRelationRole> translator;

    /**
     * Constructor.
     *
     * @param key
     *            the (old) associate role key
     * @param translator
     *            the resource bundle wrapper to use
     */
    CompatibleRelationRole(final String key, final Translator<CompatibleRelationRole> translator) {
        this.key = key;
        this.translator = translator;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String get() {
        return this.translator.getLocalizedMessage(this);
    }
}
