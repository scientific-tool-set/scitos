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

package org.hmx.scitos.core;

import org.hmx.scitos.domain.IModel;
import org.hmx.scitos.domain.ModelChangeListener;

/**
 * Base interface for the single point of contact for model changes in a SciToS module.
 *
 * @param <M>
 *            type of the managed model object
 */
public interface IModelHandler<M extends IModel<?>> {

    /**
     * Getter for the managed model object.
     *
     * @return managed model object
     */
    M getModel();

    /**
     * Add the given {@link ModelChangeListener} to the collection of listeners, that are notified when a model change occurs.
     *
     * @param listener
     *            listener to add
     */
    void addModelChangeListener(ModelChangeListener listener);

    /**
     * Remove the given {@link ModelChangeListener} from the collection of listeners, that are notified when a model change occurs.
     *
     * @param listener
     *            listener to remove
     */
    void removeModelChangeListener(ModelChangeListener listener);

}
