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

import java.util.UUID;

/**
 * Wrapper for a {@link UUID}, just to have a properly typed reference to a syntactical function or function group.
 */
public class AbstractSyntacticalElementReference {

    /** Actual/wrapped reference identifier. */
    private final UUID uuid;

    /**
     * Constructor: initializing a reference with a random {@code uuid}.
     */
    protected AbstractSyntacticalElementReference() {
        this(UUID.randomUUID());
    }

    /**
     * Constructor: initializing a reference with the given {@code uuid}.
     *
     * @param uuid unique reference identifier
     */
    protected AbstractSyntacticalElementReference(final UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Getter for the actual/wrapped reference identifier.
     *
     * @return unique reference identifier
     */
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    @Override
    public boolean equals(final Object otherReference) {
        if (this == otherReference) {
            return true;
        }
        if (!(otherReference instanceof AbstractSyntacticalElementReference)) {
            return false;
        }
        return this.uuid.equals(((AbstractSyntacticalElementReference) otherReference).uuid);
    }
}
