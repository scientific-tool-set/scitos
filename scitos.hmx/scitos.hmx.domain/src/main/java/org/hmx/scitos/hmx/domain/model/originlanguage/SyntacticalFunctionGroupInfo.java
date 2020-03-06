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

/**
 * Display information for the referenced syntactical function group – but without the complete knowledge of included sub functions.
 *
 * @see SyntacticalFunctionGroup
 */
public class SyntacticalFunctionGroupInfo extends AbstractSyntacticalElement<SyntacticalFunctionGroupReference> {

    /**
     * Constructor: setting the reference, name, and description to the given values.
     *
     * @param reference
     *            display language independent reference to this syntactical function group
     * @param name
     *            the full identifier displayed when selecting this function group in the syntactical analysis
     * @param description
     *            the description of this function group's meaning
     */
    public SyntacticalFunctionGroupInfo(final SyntacticalFunctionGroupReference reference, final String name, final String description) {
        super(reference, name, description);
    }
}
