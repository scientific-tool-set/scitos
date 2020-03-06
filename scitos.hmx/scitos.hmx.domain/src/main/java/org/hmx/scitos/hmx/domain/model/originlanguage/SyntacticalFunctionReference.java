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

package org.hmx.scitos.hmx.domain.model.originlanguage;

import java.util.UUID;

/**
 * Reference to a syntactical function.
 */
public class SyntacticalFunctionReference extends AbstractSyntacticalElementReference {

    /**
     * Constructor: initializing a reference with a random {@code uuid}.
     */
    public SyntacticalFunctionReference() {
        super();
    }

    /**
     * Constructor: initializing a reference with the given {@code uuid}.
     *
     * @param uuid unique reference identifier
     */
    public SyntacticalFunctionReference(final UUID uuid) {
        super(uuid);
    }
}
