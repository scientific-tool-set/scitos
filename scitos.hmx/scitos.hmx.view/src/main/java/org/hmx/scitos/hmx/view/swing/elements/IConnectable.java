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

package org.hmx.scitos.hmx.view.swing.elements;

import org.hmx.scitos.hmx.domain.model.AbstractConnectable;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.domain.model.Relation;

/**
 * Equivalent to the {@link AbstractConnectable} (model element) in the semantical analysis, offering the general functionality to have a super
 * ordinated {@link Relation} and the methods to display them as well.
 *
 * @param <T>
 *            represented model class
 */
public interface IConnectable<T extends AbstractConnectable> extends ICheckable {

    /**
     * Getter for the represented connectable model element.
     *
     * @return the model object represented by this view element
     */
    T getRepresented();

    /**
     * Toggle the integrated check box's visibility on/off.
     *
     * @param visible
     *            if the check box should be visible
     */
    void setCheckBoxVisible(final boolean visible);

    /**
     * Getter for the number of {@link Relation} levels between the represented model element and the underlying {@link Proposition}s. If the
     * represented model element is a {@link Proposition}, this returns {@code 0}.
     *
     * @return distance to the proposition level
     */
    int getDepth();

    /**
     * Getter for the row's index on which to connect on with the next higher super ordinated {@link Relation}. If this element represents a
     * {@link Relation} itself, this is determined by the weights of the associates in it.
     *
     * @return row index to connect on with super ordinated {@link Relation}
     */
    double getConnectY();
}
