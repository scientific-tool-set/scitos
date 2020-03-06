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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.hmx.scitos.domain.util.ComparisonUtil;

/**
 * Reference to a group of functions in the syntactical analysis.
 *
 * @see SyntacticalFunctionReference
 */
public class SyntacticalFunctionGroupReference extends AbstractSyntacticalElementReference {

    /** Functions belonging to this function group. */
    private final List<AbstractSyntacticalElementReference> subFunctions;

    /**
     * Constructor: initializing a reference with a random {@code uuid}.
     *
     * @param subFunctions the functions/function groups belonging to this function group
     */
    public SyntacticalFunctionGroupReference(final List<? extends AbstractSyntacticalElementReference> subFunctions) {
        this(UUID.randomUUID(), subFunctions);
    }

    /**
     * Constructor: initializing a reference with the given {@code uuid}.
     *
     * @param uuid unique reference identifier
     * @param subFunctions the functions/function groups belonging to this function group
     */
    public SyntacticalFunctionGroupReference(final UUID uuid, final List<? extends AbstractSyntacticalElementReference> subFunctions) {
        super(uuid);
        this.subFunctions = Collections.unmodifiableList(new ArrayList<AbstractSyntacticalElementReference>(subFunctions));
    }

    /**
     * Getter for the functions/function groups belonging to this function group.
     *
     * @return the subordinated functions/function groups
     */
    public List<AbstractSyntacticalElementReference> getSubFunctions() {
        return this.subFunctions;
    }

    @Override
    public boolean equals(final Object otherObj) {
        if (!super.equals(otherObj) || !(otherObj instanceof SyntacticalFunctionGroupReference)) {
            return false;
        }
        final SyntacticalFunctionGroupReference otherFunction = (SyntacticalFunctionGroupReference) otherObj;
        return ComparisonUtil.isNullOrEmptyAwareEqual(this.subFunctions, otherFunction.subFunctions);
    }
}
