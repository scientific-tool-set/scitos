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

package org.hmx.scitos.hmx.domain.model.originlanguage;

/**
 * Representation of an associated function in the syntactical analysis. Either for a {@link ClauseItem} in its containing {@link Proposition}, or for
 * a subordinated (i.e. indented) {@link Proposition} to its {@code parent} {@link Proposition}.
 */
public class SyntacticalFunction extends AbstractSyntacticalElement<SyntacticalFunctionReference> {

    /** Short identifier that is used as the textual representation of this function in the syntactical analysis. */
    private final String code;
    /** If an element with this syntactical function should be underlined. */
    private final boolean underlined;

    /**
     * Constructor: setting all fields to the given values to achieve immutability.
     *
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
    public SyntacticalFunction(final SyntacticalFunctionReference reference, final String code, final String name, final boolean underlined,
            final String description) {
        super(reference, name, description);
        this.code = code;
        this.underlined = underlined;
    }

    /**
     * Getter for the short identifier that is used as the textual representation of this function in the syntactical analysis.
     *
     * @return the short identifier
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Getter for the flag indicating if an element with this syntactical function should be underlined..
     *
     * @return if an associated element should be underlined
     */
    public boolean isUnderlined() {
        return this.underlined;
    }

    @Override
    public boolean equals(final Object otherObj) {
        if (!super.equals(otherObj)) {
            return false;
        }
        if (!(otherObj instanceof SyntacticalFunction)) {
            return false;
        }
        final SyntacticalFunction otherFunction = (SyntacticalFunction) otherObj;
        return this.code.equals(otherFunction.code)
                && this.underlined == otherFunction.underlined;
    }
}
