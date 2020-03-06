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

package org.hmx.scitos.hmx.domain;

import org.hmx.scitos.hmx.domain.model.originlanguage.SyntacticalFunctionReference;

/** Indicator interface for a model element, that can have a syntactical function in its super ordinated structure. */
public interface ICanHaveSyntacticalFunction {

    /**
     * Getter for the syntactical function, relative to its super ordinated structure.
     *
     * @return the syntactical function
     */
    SyntacticalFunctionReference getFunction();

    /**
     * Setter for the syntactical function.
     *
     * @param syntacticalFunction
     *            the syntactical function to set
     */
    void setFunction(final SyntacticalFunctionReference syntacticalFunction);
}
