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

package org.hmx.scitos.hmx.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hmx.scitos.domain.util.ComparisonUtil;

/**
 * Representation of a group of functions in the syntactical analysis.
 * 
 * @see SyntacticalFunction
 */
public class SyntacticalFunctionGroup extends AbstractSyntacticalFunctionElement {

    /** Functions belonging to this function group. */
    private final List<AbstractSyntacticalFunctionElement> subFunctions;

    /**
     * Constructor: setting all fields to the given values to achieve immutability.
     *
     * @param name
     *            the full identifier displayed in the syntactical analysis
     * @param description
     *            the description of this function group's meaning
     * @param subFunctions
     *            the functions/function groups belonging to this function group
     */
    public SyntacticalFunctionGroup(final String name, final String description,
            final List<? extends AbstractSyntacticalFunctionElement> subFunctions) {
        super(name, description);
        this.subFunctions = Collections.unmodifiableList(new ArrayList<AbstractSyntacticalFunctionElement>(subFunctions));
    }

    /**
     * Getter for the functions/function groups belonging to this function group.
     *
     * @return the subordinated functions/function groups
     */
    public List<AbstractSyntacticalFunctionElement> getSubFunctions() {
        return this.subFunctions;
    }

    @Override
    public boolean equals(final Object otherObj) {
        if (!super.equals(otherObj)) {
            return false;
        }
        if (!(otherObj instanceof SyntacticalFunctionGroup)) {
            return false;
        }
        final SyntacticalFunctionGroup otherFunction = (SyntacticalFunctionGroup) otherObj;
        return ComparisonUtil.isNullOrEmptyAwareEqual(this.subFunctions, otherFunction.subFunctions);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("' - name: '").append(this.getName()).append("' - description: '").append(this.getDescription());
        builder.append("' - subFunctions: ").append(this.getSubFunctions().size());
        return builder.toString();
    }
}
