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

package org.hmx.scitos.view.swing;

/**
 * General interface of a view, that can (possibly) handle un-do and re-do operations.
 */
public interface IUndoManagedView {

    /**
     * Fully refresh the current view elements, regarding the (possibly changed) contents.
     */
    void refresh();

    /**
     * Ensure all currently displayed changes are represented as such in the underlying model objects - in order to be able to save the current state.
     */
    void submitChangesToModel();

    /**
     * Check if a rollback of the last model change is possible.
     *
     * @return {@link #undo()} possible
     */
    boolean canUndo();

    /**
     * Check if the last model action was an {@link #undo()}.
     *
     * @return {@link #redo()} possible
     */
    boolean canRedo();

    /**
     * Execute rollback to the latest undo-able change.
     */
    void undo();

    /**
     * Revert last {@link #undo()}.
     */
    void redo();
}
