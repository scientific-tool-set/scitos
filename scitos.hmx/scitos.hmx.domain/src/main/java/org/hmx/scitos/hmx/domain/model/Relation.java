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

package org.hmx.scitos.hmx.domain.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.hmx.scitos.hmx.domain.model.RelationTemplate.AssociateRole;

/**
 * Element representing a relation between other {@link AbstractConnectable}s ({@code Relation}s and/or {@link Proposition}s). Each of these
 * associates has a role and weight in this super ordinated {@code Relation}. Between the associates in one {@code Relation} are no gaps (i.e. no
 * unconnected enclosed elements) allowed.
 */
public final class Relation extends AbstractConnectable implements Cloneable, Iterable<AbstractConnectable> {

    /** The subordinated elements forming this relation. */
    private final List<AbstractConnectable> associates = new LinkedList<AbstractConnectable>();

    /**
     * Constructor: for a {@code Relation} between the specified associates with the respective role and weight.
     *
     * @param associates
     *            the subordinated elements that form this relation
     * @param template
     *            provider of the roles and weights to assign to each associate
     */
    public Relation(final List<? extends AbstractConnectable> associates, final RelationTemplate template) {
        this(associates, template.getAssociateRoles(associates.size()));
    }

    /**
     * Constructor: for a {@code Relation} between the specified associates with the given roles and weights.
     *
     * @param associates
     *            the subordinated elements that form this relation
     * @param rolesAndWeights
     *            the roles and weights to assign to each associate
     */
    public Relation(final List<? extends AbstractConnectable> associates, final List<AssociateRole> rolesAndWeights) {
        if (associates != null) {
            this.associates.addAll(associates);
            for (int associateIndex = 0; associateIndex < associates.size(); associateIndex++) {
                associates.get(associateIndex).setSuperOrdinatedRelation(this, rolesAndWeights.get(associateIndex));
            }
        }
    }

    /**
     * Getter for the subordinated model elements.
     *
     * @return subordinated {@link AbstractConnectable}s
     */
    public List<AbstractConnectable> getAssociates() {
        return Collections.unmodifiableList(this.associates);
    }

    /**
     * Setter for the subordinated model elements.
     *
     * @param associates
     *            the subordinated {@link AbstractConnectable}s
     */
    public void setAssociates(final List<AbstractConnectable> associates) {
        this.associates.clear();
        if (associates != null) {
            this.associates.addAll(associates);
        }
    }

    /**
     * Calculate the depth of the relation tree structure under this element.
     *
     * @return the number of relation layers under this one (returns {@code 1} if all associates are {@link Proposition}s)
     */
    public int getTreeDepth() {
        int depth = 0;
        for (final AbstractConnectable singleAssociate : this.associates) {
            if (singleAssociate instanceof Relation) {
                depth = Math.max(depth, ((Relation) singleAssociate).getTreeDepth());
            }
        }
        return depth + 1;
    }

    /**
     * Retrieve the very first {@link Proposition} contained in this relation.
     *
     * @return first {@link Proposition} contained
     */
    public Proposition getFirstPropositionContained() {
        final AbstractConnectable firstAssociate = this.getAssociates().get(0);
        if (firstAssociate instanceof Proposition) {
            return (Proposition) firstAssociate;
        }
        // recursively call this function on the first subordinated relation
        return ((Relation) firstAssociate).getFirstPropositionContained();
    }

    /**
     * Retrieve the very last {@link Proposition} contained in this relation.
     *
     * @return last {@link Proposition} contained
     */
    public Proposition getLastPropositionContained() {
        final AbstractConnectable lastAssociate = this.associates.get(this.associates.size() - 1);
        if (lastAssociate instanceof Proposition) {
            return (Proposition) lastAssociate;
        }
        // recursively call this function on the last subordinated relation
        return ((Relation) lastAssociate).getLastPropositionContained();
    }

    /**
     * Remove this element and all super ordinated {@link Relation}s containing it.
     */
    public void kill() {
        final Relation superOrdinated = this.getSuperOrdinatedRelation();
        if (superOrdinated != null) {
            // recursively kill super ordinated relation
            superOrdinated.kill();
        }
        // reset subordinated elements to belong to no relation
        for (final AbstractConnectable singleAssociate : this.associates) {
            singleAssociate.setSuperOrdinatedRelation(null, null);
        }
    }

    @Override
    public Iterator<AbstractConnectable> iterator() {
        return this.getAssociates().iterator();
    }

    /**
     * Create an empty copy of this {@link Relation} WITHOUT setting its sub ordinated associates.
     *
     * @return the cloned Relation, holding only its comment
     */
    @Override
    public Relation clone() {
        final Relation cloned = new Relation(null, (List<AssociateRole>) null);
        cloned.setComment(this.getComment());
        return cloned;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + this.associates.size();
    }

    @Override
    public boolean equals(final Object otherObj) {
        if (this == otherObj) {
            return true;
        }
        if (!(otherObj instanceof Relation)) {
            return false;
        }
        // equals() of the AbstractConnectable class invokes equalsIgnoreSuperOrdinatedRelation()
        return super.equals(otherObj);
    }

    @Override
    protected boolean equalsIgnoreSuperOrdinatedRelation(final AbstractConnectable otherConnectable) {
        if (!(otherConnectable instanceof Relation)) {
            return false;
        }
        final Relation otherRelation = (Relation) otherConnectable;
        final int associateCount = this.associates.size();
        if (associateCount != otherRelation.associates.size()) {
            return false;
        }
        for (int associateIndex = 0; associateIndex < associateCount; associateIndex++) {
            if (!this.associates.get(associateIndex).equalsIgnoreSuperOrdinatedRelation(otherRelation.associates.get(associateIndex))) {
                return false;
            }
        }
        return true;
    }
}
