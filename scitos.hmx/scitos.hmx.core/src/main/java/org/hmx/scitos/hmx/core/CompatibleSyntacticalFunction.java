/*
   Copyright (C) 2017 HermeneutiX.org

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

import org.hmx.scitos.hmx.domain.model.originlanguage.SyntacticalFunction;
import org.hmx.scitos.hmx.domain.model.originlanguage.SyntacticalFunctionReference;

/**
 * Extension of a syntactical function by the old function's key as of HermeneutiX up to version 1.12.
 */
class CompatibleSyntacticalFunction extends SyntacticalFunction {

    /** The old function's message key to allow backwards compatibility. */
    private final String oldKey;

    /**
     * Constructor: wraps the given syntactical function for backwards compatibility.
     *
     * @param oldKey
     *            the old function's message key to allow backwards compatibility
     * @param reference
     *            display language independent reference to this syntactical function
     * @param code
     *            the short identifier used in the syntactical analysis
     * @param name
     *            the full identifier displayed when selecting this function in the syntactical analysis
     * @param underlined
     *            if an element with this syntactical function should be underlined
     * @param description
     *            the description of this function's meaning
     */
    CompatibleSyntacticalFunction(final String oldKey, final SyntacticalFunctionReference reference, final String code, final String name,
            final boolean underlined, final String description) {
        super(reference, code, name, underlined, description);
        this.oldKey = oldKey;
    }

    /**
     * Getter for the old function's message key from when HermeneutiX was a standalone application.
     *
     * @return the old function's message key
     */
    String getOldKey() {
        return this.oldKey;
    }

    @Override
    public String toString() {
        return new StringBuilder("oldKey: '").append(this.oldKey).append("' - ").append(super.toString()).toString();
    }
}
