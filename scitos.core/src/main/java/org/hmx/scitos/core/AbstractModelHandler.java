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

import java.util.LinkedList;
import java.util.List;

import org.hmx.scitos.domain.IModel;
import org.hmx.scitos.domain.ModelChangeListener;
import org.hmx.scitos.domain.ModelEvent;
import org.hmx.scitos.domain.util.CollectionUtil;

/**
 * Abstract implementation of a model handler, that is supposed to be the single point of contact for all actual changes in one moule's handled model.
 *
 * @param <M>
 *            type of the handled (top level) model object
 */
public abstract class AbstractModelHandler<M extends IModel<M>> implements IModelHandler<M> {

    /** The managed model object. */
    private M model;
    /** The listeners that are notified when a model change occurs. */
    private final List<ModelChangeListener> listeners;

    /**
     * Main constructor.
     *
     * @param model
     *            model object to manage
     */
    protected AbstractModelHandler(final M model) {
        this.model = model;
        this.listeners = new LinkedList<ModelChangeListener>();
    }

    @Override
    public M getModel() {
        return this.model;
    }

    protected void setModel(final M model) {
        this.model = model;
    }

    @Override
    public void addModelChangeListener(final ModelChangeListener listener) {
        if (!CollectionUtil.containsInstance(this.listeners, listener)) {
            this.listeners.add(listener);
        }
    }

    @Override
    public void removeModelChangeListener(final ModelChangeListener listener) {
        final int listenerIndex = CollectionUtil.indexOfInstance(this.listeners, listener);
        if (listenerIndex != -1) {
            this.listeners.remove(listenerIndex);
        }
    }

    /**
     * Notify all registered listener of the change in the given model element.
     *
     * @param <O>
     *            type of the changed model element
     * @param changedElement
     *            model element that has been changed
     * @param updated
     *            if this is just an update of the already existing element
     */
    protected <O> void notifyListeners(final O changedElement, final boolean updated) {
        // create generic model event
        final ModelEvent<O> event = new ModelEvent<O>(changedElement, updated);
        // notify all currently registered ModelChangeListeners
        for (final ModelChangeListener singleListener : this.listeners) {
            singleListener.modelChanged(event);
        }
    }
}
