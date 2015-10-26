/*
   Copyright (C) 2015 HermeneutiX.org

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

package org.hmx.scitos.domain;

import java.util.List;
import java.util.Map;

/**
 * Generic of a main model consisting of multiple sub models.
 *
 * @param <M>
 *            type of the multi object model implementation
 * @param <S>
 *            type of the sub model objects
 */
public interface IMultiObjectModel<M extends IMultiObjectModel<M, S>, S extends IModel<S>> extends IModel<M> {

    /**
     * Getter for the grouping key used in {@link #getSubModelObjects()} for the given sub model instance.
     *
     * @param subModel
     *            sub model instance to get the grouping key for (assumed to be of type {@code S})
     * @return grouping key
     */
    String getGroupKey(Object subModel);

    /**
     * Getter for the grouped list of sub model objects.
     *
     * @return contained sub model objects
     * @see #getGroupKey(Object)
     */
    Map<String, ? extends List<? extends S>> getSubModelObjects();
}
