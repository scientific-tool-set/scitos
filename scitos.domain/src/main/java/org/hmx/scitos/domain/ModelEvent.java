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

import java.io.Serializable;

/**
 * Event containing a changed model element.
 *
 * @param <O>
 *            type of the changed model element
 */
public class ModelEvent<O> implements Serializable {

    /** The changed model element. */
    private final O target;
    /** Indicator, if this is just an update of the already existing element. */
    private final boolean updated;

    /**
     * Constructor: creates an event that reflects changes in the given model element.
     *
     * @param target
     *            changed model element
     * @param updated
     *            if this is just an update of the already existing element
     */
    public ModelEvent(final O target, final boolean updated) {
        this.target = target;
        this.updated = updated;
    }

    /**
     * Getter for the changed model element.
     *
     * @return changed model element
     */
    public O getTarget() {
        return this.target;
    }

    /**
     * Getter for the updated flag.
     * 
     * @return if this is just an update of the already existing element
     */
    public boolean isUpdated() {
        return this.updated;
    }
}