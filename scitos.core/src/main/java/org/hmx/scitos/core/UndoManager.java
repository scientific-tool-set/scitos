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

import java.util.Deque;
import java.util.LinkedList;

import org.hmx.scitos.core.option.Option;
import org.hmx.scitos.domain.IModel;

/**
 * Manager for a number of model states to offer the ability to undo and redo single actions.<br>
 * The number of stored model states available for these rollbacks depends on the associated user preference.
 *
 * @param <M>
 *            class representing the managed model object
 */
public final class UndoManager<M extends IModel<M>> {

    /**
     * The collection of previous model object states, available for {@link #undo()}.
     */
    private final Deque<M> availableUndos = new LinkedList<>();
    /**
     * The collection of previous model object states, which were rolled back and are available for {@link #redo()}.
     */
    private final Deque<M> availableRedos = new LinkedList<>();
    /** The maximum of stored undo-able model changes. */
    private int limit;

    /**
     * Constructor: generates a new instance for the specified model object.
     *
     * @param initialState
     *            initial state of the managed model object
     */
    public UndoManager(final M initialState) {
        this.setLimit(Option.UNDO_LIMIT.getValueAsInteger());
        // store current model object state
        this.reset(initialState);
    }

    /**
     * Discard all undo-/redo-able model states and start from the (new) given initial state.
     *
     * @param initialState
     *            (new) initial state of the managed model object
     */
    public void reset(final M initialState) {
        this.availableRedos.clear();
        this.availableUndos.clear();
        this.availableUndos.add(initialState.clone());
    }

    /**
     * Setter for the maximum number of stored model versions available for {@link #undo()} and {@link #redo()}.
     *
     * @param limit
     *            maximum to set
     */
    public void setLimit(final int limit) {
        // the first entry in the availableUndos collection is always the current state
        this.limit = Math.max(1, limit + 1);
        this.dropExcessiveUndoableStates();
    }

    /**
     * Store the new model state for a potential {@link #undo()}.
     *
     * @param changedState
     *            new state to remember
     */
    public void undoableEditHappened(final M changedState) {
        this.availableUndos.addFirst(changedState.clone());
        this.dropExcessiveUndoableStates();
        // drop redos
        this.availableRedos.clear();
    }

    /**
     * Execute rollback to the latest undo-able change.
     *
     * @return previous model state
     * @throws IllegalStateException
     *             no available states from earlier model changes (or limit is set to '0')
     */
    public M undo() throws IllegalStateException {
        if (!this.canUndo()) {
            throw new IllegalStateException();
        }
        // remove current state from list and store as available redo
        this.availableRedos.addFirst(this.availableUndos.pollFirst());
        // return previous state
        return this.availableUndos.getFirst();
    }

    /**
     * Revert last {@link #undo()}.
     *
     * @return previously rolled back model state
     * @throws IllegalStateException
     *             no available states from earlier undo() calls
     */
    public M redo() throws IllegalStateException {
        if (!this.canRedo()) {
            throw new IllegalStateException();
        }
        // remove state from list
        final M previousState = this.availableRedos.pollFirst();
        // store as available undo
        this.availableUndos.addFirst(previousState);
        this.dropExcessiveUndoableStates();
        return previousState;
    }

    /**
     * Check if a rollback of the last model change is possible.
     *
     * @return {@link #undo()} possible
     */
    public boolean canUndo() {
        // the first entry in the availableUndos is always the current state
        return this.availableUndos.size() > 1;
    }

    /**
     * Check if the last model action was an {@link #undo()}.
     *
     * @return {@link #redo()} possible
     */
    public boolean canRedo() {
        return !this.availableRedos.isEmpty();
    }

    /**
     * Ensure that the defined limit is applied.
     */
    private void dropExcessiveUndoableStates() {
        while (this.availableUndos.size() > this.limit) {
            // limit reached: drop excessive state
            this.availableUndos.removeLast();
        }
    }
}
