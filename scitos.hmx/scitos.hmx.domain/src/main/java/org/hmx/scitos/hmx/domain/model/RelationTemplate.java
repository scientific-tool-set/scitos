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

package org.hmx.scitos.hmx.domain.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hmx.scitos.domain.util.ComparisonUtil;

/** Template for a kind of relation defined by a number of associates and their respective roles and weights in the relation. */
public final class RelationTemplate implements Serializable {

    /** The first associate's role/weight. */
    private final AssociateRole leadingAssociate;
    /** The recurring associate's role/weight (if there are more than two associates). */
    private final AssociateRole repetitiveAssociate;
    /** The last associate's role/weight. */
    private final AssociateRole trailingAssociate;
    /** The (optional) hint text describing the represented relation type. */
    private final String description;

    /**
     * Constructor: storing the given immutable associate roles and weights.
     *
     * @param leadingAssociate
     *            the first associate's role/weight
     * @param repetitiveAssociate
     *            the recurring associate's role/weight (can be {@code null}, to allow only two-associate-relations)
     * @param trailingAssociate
     *            the last associate's role/weight
     * @param description
     *            the hint text describing the represented relation type (can be {@code null})
     */
    public RelationTemplate(final AssociateRole leadingAssociate, final AssociateRole repetitiveAssociate, final AssociateRole trailingAssociate,
            final String description) {
        this.leadingAssociate = leadingAssociate;
        this.repetitiveAssociate = repetitiveAssociate;
        this.trailingAssociate = trailingAssociate;
        this.description = description;
    }

    /**
     * Check if this relation can consist of more than two associates.
     *
     * @return if a higher value than {@code 2} is valid as argument for {@link #getAssociateRoles(int)}
     */
    public boolean canHaveMoreThanTwoAssociates() {
        return this.repetitiveAssociate != null;
    }

    /**
     * Getter for the associate roles and weights represented by this relation.
     *
     * @param associateCount
     *            number of expected associates
     * @return the given number of associate roles and weights
     * @see #canHaveMoreThanTwoAssociates()
     */
    public List<AssociateRole> getAssociateRoles(final int associateCount) {
        if (associateCount < 1 || associateCount > 2 && !this.canHaveMoreThanTwoAssociates()) {
            throw new IllegalArgumentException();
        }
        final List<AssociateRole> result = new ArrayList<AssociateRole>(associateCount);
        result.add(this.leadingAssociate);
        final int repetitiveAssociatesToAdd = associateCount - 2;
        for (int count = 0; count < repetitiveAssociatesToAdd; count++) {
            result.add(this.repetitiveAssociate);
        }
        result.add(this.trailingAssociate);
        return result;
    }

    /**
     * Getter for the (optional) hint text describing the represented relation type.
     *
     * @return the description text (can be {@code null})
     */
    public String getDescription() {
        return this.description;
    }

    @Override
    public int hashCode() {
        int hashCode = this.leadingAssociate.hashCode();
        if (this.repetitiveAssociate != null) {
            hashCode += 7 * this.repetitiveAssociate.hashCode();
        }
        hashCode += 13 * this.trailingAssociate.hashCode();
        return hashCode;
    }

    @Override
    public boolean equals(final Object otherObj) {
        if (this == otherObj) {
            return true;
        }
        if (!(otherObj instanceof RelationTemplate)) {
            return false;
        }
        final RelationTemplate otherTemplate = (RelationTemplate) otherObj;
        return this.leadingAssociate.equals(otherTemplate.leadingAssociate)
                && ComparisonUtil.isNullAwareEqual(this.repetitiveAssociate, otherTemplate.repetitiveAssociate)
                && this.trailingAssociate.equals(otherTemplate.trailingAssociate)
                && ComparisonUtil.isNullOrEmptyAwareEqual(this.description, otherTemplate.description);
    }

    /** Representation of an element in a relation with its role and weight therein. */
    public static final class AssociateRole implements Serializable {

        /** This associate's role in the super ordinated relation. */
        private final String role;
        /** If this associate has a high weight in the super ordinated relation. */
        private final boolean highWeight;

        /**
         * Constructor: storing the given role and weight.
         *
         * @param role
         *            this associate's role in the super ordinated relation
         * @param highWeight
         *            if this associate has a high weight in the super ordinated relation
         */
        public AssociateRole(final String role, final boolean highWeight) {
            this.role = role;
            this.highWeight = highWeight;
        }

        /**
         * Getter for this associate's role in the super ordinated relation.
         *
         * @return the role in the relation
         */
        public String getRole() {
            return this.role;
        }

        /**
         * Getter for the flag indicating if this associate has a high weight in the super ordinated relation.
         *
         * @return if this is a high weight associate in the relation
         */
        public boolean isHighWeight() {
            return this.highWeight;
        }

        @Override
        public int hashCode() {
            return this.role.hashCode() + (this.highWeight ? 23 : 37);
        }

        @Override
        public boolean equals(final Object otherObj) {
            if (this == otherObj) {
                return true;
            }
            if (!(otherObj instanceof AssociateRole)) {
                return false;
            }
            final AssociateRole otherRole = (AssociateRole) otherObj;
            return this.highWeight == otherRole.highWeight && this.role.equals(otherRole.role);
        }
    }
}
