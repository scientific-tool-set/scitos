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

import org.hmx.scitos.domain.util.ComparisonUtil;
import org.hmx.scitos.hmx.domain.model.ClauseItem;
import org.hmx.scitos.hmx.domain.model.Proposition;

/**
 * Single function or group of functions in the syntactical analysis. Either for a {@link ClauseItem} in its containing {@link Proposition}, or for a
 * subordinated (i.e. indented) {@link Proposition} to its {@code parent} {@link Proposition}.
 *
 * @param <R> reference type
 */
public abstract class AbstractSyntacticalElement<R extends AbstractSyntacticalElementReference> {

    /** Internal reference for matching this element to its equivalent in another display profile. */
    private final R reference;
    /** Full identifier for this function, displayed for selection in the syntactical analysis. */
    private final String name;
    /** More elaborate description what this syntactical function represents. */
    private final String description;

    /**
     * Constructor: setting the name and description to the given values to achieve immutability.
     *
     * @param reference
     *            display language independent reference to this syntactical function/function group
     * @param name
     *            the full identifier displayed when selecting this function in the syntactical analysis
     * @param description
     *            the description of this function's meaning
     */
    protected AbstractSyntacticalElement(final R reference, final String name, final String description) {
        this.reference = reference;
        this.name = name;
        this.description = description;
    }

    /**
     * Getter for the display language independent reference to this syntactical function.
     *
     * @return the reference
     */
    public R getReference() {
        return this.reference;
    }

    /**
     * Getter for the full identifier that is used when displaying available functions in the syntactical analysis..
     *
     * @return the full identifier
     */
    public String getName() {
        return this.name;
    }

    /**
     * Getter for the more elaborate description what this syntactical function represents.
     *
     * @return the description of this function's meaning
     */
    public String getDescription() {
        return this.description;
    }

    @Override
    public int hashCode() {
        return this.reference.hashCode();
    }

    @Override
    public boolean equals(final Object otherObj) {
        if (this == otherObj) {
            return true;
        }
        if (!(otherObj instanceof AbstractSyntacticalElement)) {
            return false;
        }
        final AbstractSyntacticalElement<?> otherFunction = (AbstractSyntacticalElement) otherObj;
        return this.reference.equals(otherFunction.reference)
                && this.name.equals(otherFunction.name)
                && ComparisonUtil.isNullOrEmptyAwareEqual(this.description, otherFunction.description);
    }
}
