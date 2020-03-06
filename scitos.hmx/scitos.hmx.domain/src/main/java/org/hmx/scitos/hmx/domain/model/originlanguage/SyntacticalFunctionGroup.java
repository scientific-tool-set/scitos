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

/**
 * Representation of a group of functions in the syntactical analysis.
 *
 * @see SyntacticalFunction
 */
public class SyntacticalFunctionGroup extends AbstractSyntacticalElement<SyntacticalFunctionGroupReference> {

    /** Functions belonging to this function group. */
    private final List<AbstractSyntacticalElement<?>> subFunctions;

    /**
     * Constructor: setting all fields to the given values to achieve immutability.
     *
     * @param reference
     *            display language independent reference to this syntactical function
     * @param name
     *            the full identifier displayed in the syntactical analysis
     * @param description
     *            the description of this function group's meaning
     * @param subFunctions
     *            the functions/function groups belonging to this function group
     */
    public SyntacticalFunctionGroup(final SyntacticalFunctionGroupInfo info, final List<? extends AbstractSyntacticalElement<?>> subFunctions) {
        super(info.getReference(), info.getName(), info.getDescription());
        this.subFunctions = new ArrayList<AbstractSyntacticalElement<?>>(subFunctions);
    }

    /**
     * Getter for the functions/function groups belonging to this function group.
     *
     * @return the subordinated functions/function groups
     */
    public List<AbstractSyntacticalElement<?>> getSubFunctions() {
        return Collections.unmodifiableList(this.subFunctions);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("' - name: '").append(this.getName()).append("' - description: '").append(this.getDescription());
        builder.append("' - subFunctions: ").append(this.getSubFunctions().size());
        return builder.toString();
    }
}
