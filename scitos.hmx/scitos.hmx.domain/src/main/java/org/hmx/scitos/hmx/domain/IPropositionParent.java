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

import java.util.List;

import org.hmx.scitos.hmx.domain.model.Proposition;

/**
 * Indicator interface for model elements that can contain {@link Proposition}s, allowing their modification.
 */
public interface IPropositionParent {

    /**
     * Select and return the list that contains the specified {@link Proposition}.
     *
     * @param childProposition
     *            the {@link Proposition} to look for
     * @return the list of child propositions that contains the given one (returns {@code null} if the given {@code childProposition} is no child of
     *         this element)
     */
    List<Proposition> getContainingList(final Proposition childProposition);

    /**
     * Add the given {@link Proposition} in front of a specified follower {@link Proposition}.
     *
     * @param toInsert
     *            the {@link Proposition} to add in this element
     * @param follower
     *            the already contained {@link Proposition}, which will be the immediate follower of the inserted one
     */
    void insertChildPropositionBeforeFollower(final Proposition toInsert, final Proposition follower);

    /**
     * Add the given {@link Proposition} behind a specified prior {@link Proposition}.
     *
     * @param toInsert
     *            the {@link Proposition} to add in this element
     * @param prior
     *            the already contained {@link Proposition}, which will be the immediate prior of the inserted one
     */
    void insertChildPropositionAfterPrior(final Proposition toInsert, final Proposition prior);

    /**
     * Remove the specified {@link Proposition} from this element.
     *
     * @param toDelete
     *            the {@link Proposition} to remove
     */
    void removeChildProposition(final Proposition toDelete);

    /**
     * Check if this element is equal to the given {@code Object}.
     *
     * @param otherObj
     *            other element to be compared with
     * @param ignoreChildren
     *            if contained child propositions should be ignored in the comparison
     * @return if this element is equal to the given {@code Object}
     */
    boolean equals(Object otherObj, boolean ignoreChildren);
}
