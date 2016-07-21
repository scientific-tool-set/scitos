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
import java.util.List;

import org.hmx.scitos.domain.util.CollectionUtil;
import org.hmx.scitos.domain.util.ComparisonUtil;
import org.hmx.scitos.hmx.domain.ICommentable;
import org.hmx.scitos.hmx.domain.IPropositionParent;
import org.hmx.scitos.hmx.domain.model.RelationTemplate.AssociateRole;

/**
 * Element, that is able to be connected with other {@code AbstractConnectable}s in a super ordinated {@link Relation}; containing its role and weight
 * in the super ordinated {@link Relation}.
 */
public abstract class AbstractConnectable implements Serializable, ICommentable {

    /** The super ordinated relation this is an associate of. */
    private Relation superOrdinatedRelation;
    /** The role in the super ordinated relation. */
    private AssociateRole role;
    /** The additional comment's text. */
    private String comment;

    /**
     * Getter for the super ordinated relation this is an associate of.
     *
     * @return the super ordinated relation
     */
    public Relation getSuperOrdinatedRelation() {
        return this.superOrdinatedRelation;
    }

    /**
     * Setter for the super ordinated relation this is an associate of, including the associated role and weight.
     *
     * @param relation
     *            the super ordinated relation to set
     * @param role
     *            this associate's role and weight in the super ordinated relation
     */
    public void setSuperOrdinatedRelation(final Relation relation, final AssociateRole role) {
        this.superOrdinatedRelation = relation;
        this.role = role;
    }

    /**
     * Getter for this associate's role (and weight) in the super ordinated relation.
     *
     * @return the associated role
     */
    public AssociateRole getRole() {
        return this.role;
    }

    @Override
    public String getComment() {
        return this.comment;
    }

    @Override
    public void setComment(final String comment) {
        if (comment == null || comment.isEmpty()) {
            this.comment = null;
        } else {
            this.comment = comment;
        }
    }

    /**
     * Check if this element is immediately preceding the specified {@code AbstractConnectable}, disregarding any {@code partAfterArrow}s between
     * them.
     *
     * @param follower
     *            the element to check the relative position for
     * @return if this is the given element's prior, disregarding any {@code partAfterArrow}s
     */
    public boolean isConnectablePriorOf(final AbstractConnectable follower) {
        return this.isPriorOf(follower, true);
    }

    /**
     * Checks if this element is immediately preceding the specified {@link AbstractConnectable}.
     *
     * @param follower
     *            the element to check the relative position for
     * @return if this is the given element's prior
     */
    public boolean isPriorOf(final AbstractConnectable follower) {
        return this.isPriorOf(follower, false);
    }

    /**
     * Checks if this element is immediately preceding the specified {@link AbstractConnectable}.
     *
     * @param follower
     *            the element to check the relative position for
     * @param ignorePartAfterArrow
     *            if {@code partAfterArrow}s (i.e. {@link Proposition} parts) should be skipped
     * @return if this is the given element's prior (under the given condition)
     */
    private boolean isPriorOf(final AbstractConnectable follower, final boolean ignorePartAfterArrow) {
        // get first Proposition in the follower
        final Proposition followingProposition;
        if (follower instanceof Proposition) {
            followingProposition = (Proposition) follower;
        } else {
            followingProposition = ((Relation) follower).getFirstPropositionContained();
        }
        return this.getFollowingProposition(ignorePartAfterArrow) == followingProposition;
    }

    /**
     * Get the immediately following {@link Proposition} in the origin text that can be part of a {@link Relation} (i.e. is no {@code partAfterArrow}
     * - {@link Proposition} part).
     *
     * @return direct follower, disregarding {@code partAfterArrow}s
     */
    public Proposition getFollowingConnectableProposition() {
        return this.getFollowingProposition(true);
    }

    /**
     * Get the immediately following {@link Proposition} in the origin text.
     *
     * @return direct follower
     */
    public Proposition getFollowingProposition() {
        return this.getFollowingProposition(false);
    }

    /**
     * Get the immediately following {@link Proposition} in the origin text.
     *
     * @param ignorePartAfterArrow
     *            if {@code partAfterArrow}s (i.e. {@link Proposition} parts) should be skipped
     * @return direct follower
     */
    private Proposition getFollowingProposition(final boolean ignorePartAfterArrow) {
        // get the first Proposition for this Connectable
        final Proposition prior;
        if (this instanceof Proposition) {
            prior = (Proposition) this;
        } else {
            prior = ((Relation) this).getLastPropositionContained();
        }

        List<Proposition> listOfFollowingChildren = prior.getLaterChildren();
        if (listOfFollowingChildren == null || listOfFollowingChildren.isEmpty()) {
            // prior got no later children
            if (prior.getPartAfterArrow() == null) {
                // prior got no later children nor enclosed children
                // browse for follower in same/higher level
                return AbstractConnectable.getFollowingPropositionDisregardingPriorsChildren(prior, ignorePartAfterArrow);
            }
            // prior got enclosed children
            listOfFollowingChildren = prior.getPartAfterArrow().getPriorChildren();
            if (listOfFollowingChildren == null || listOfFollowingChildren.isEmpty()) {
                /*
                 * ERROR : if a proposition is parted by arrows, it must have laterChildren or its second part must have priorChildren, in this case
                 * it got none of these
                 */
                throw new IllegalArgumentException();
            }
        }
        // browse for the very first of the following Propositions
        Proposition followingProposition;
        do {
            followingProposition = listOfFollowingChildren.get(0);
            listOfFollowingChildren = followingProposition.getPriorChildren();
        } while (listOfFollowingChildren != null && !listOfFollowingChildren.isEmpty());
        // followingProposition has no prior children
        return followingProposition;
    }

    /**
     * Get the immediately following {@link Proposition} on the same or higher level.
     *
     * @return the next proposition, disregarding child {@link Proposition}s
     */
    public Proposition getFollowingPropositionDisregardingChildren() {
        final Proposition prior;
        if (this instanceof Proposition) {
            prior = (Proposition) this;
        } else {
            prior = ((Relation) this).getLastPropositionContained();
        }
        return AbstractConnectable.getNextPropositionOnSameOrHigherLevel(prior, false);
    }

    @Override
    public int hashCode() {
        return 3 * (this.role == null ? 0 : this.role.hashCode()) + (this.comment == null ? 0 : this.comment.hashCode());
    }

    @Override
    public boolean equals(final Object otherObj) {
        if (this == otherObj) {
            return true;
        }
        if (!(otherObj instanceof AbstractConnectable)) {
            return false;
        }
        final AbstractConnectable otherConnectable = (AbstractConnectable) otherObj;
        if (this.superOrdinatedRelation == null) {
            return otherConnectable.superOrdinatedRelation == null && this.equalsIgnoreSuperOrdinatedRelation(otherConnectable);
        }
        return this.superOrdinatedRelation.equals(otherConnectable.superOrdinatedRelation);
    }

    /**
     * Check if this is equal to the given {@code AbstractConnectable}, disregarding any super ordinated {@link Relation}.
     *
     * @param otherConnectable
     *            other element to check for equal connection properties
     * @return if the connection weight, role and comment are equal
     */
    protected boolean equalsIgnoreSuperOrdinatedRelation(final AbstractConnectable otherConnectable) {
        return ComparisonUtil.isNullAwareEqual(this.role, otherConnectable.role)
                && ComparisonUtil.isNullOrEmptyAwareEqual(this.comment, otherConnectable.comment);
    }

    /**
     * Get the immediately following {@link Proposition} on the same or higher level of the specified {@link Proposition}.
     *
     * @param prior
     *            {@link Proposition} to look up the follower for
     * @return the following {@link Proposition} on same or higher level
     */
    public static Proposition getNextPropositionOnSameOrHigherLevel(final Proposition prior) {
        return AbstractConnectable.getNextPropositionOnSameOrHigherLevel(prior, false);
    }

    /**
     * Get the immediately following {@link Proposition} on the same or higher level of the specified {@link Proposition}.
     *
     * @param prior
     *            {@link Proposition} to look up the follower for
     * @param ignorePartAfterArrow
     *            if {@code partAfterArrow}s (i.e. {@link Proposition} parts) should be skipped
     * @return the following {@link Proposition} on same or higher level
     */
    private static Proposition getNextPropositionOnSameOrHigherLevel(final Proposition prior, final boolean ignorePartAfterArrow) {
        if (!ignorePartAfterArrow && prior.getPartAfterArrow() != null) {
            return prior.getPartAfterArrow();
        }
        final IPropositionParent parent = prior.getParent();
        // get the first partBeforeArrow
        final Proposition referenceProposition = prior.getFirstPart();
        final List<Proposition> containingList = parent.getContainingList(referenceProposition);
        final int nextListIndex = CollectionUtil.indexOfInstance(containingList, referenceProposition) + 1;
        if (nextListIndex < containingList.size()) {
            // prior got follower on the same level
            return containingList.get(nextListIndex);
        }
        if (parent instanceof Pericope) {
            // prior is the last proposition on the highest level; no follower found
            return null;
        }
        return AbstractConnectable.getNextPropositionOnHigherLevel(referenceProposition, ignorePartAfterArrow);
    }

    /**
     * Get the immediately following {@link Proposition} on its parent proposition's or higher level.
     *
     * @param referenceProposition
     *            {@link Proposition} to look up the follower for
     * @param ignorePartAfterArrow
     *            if {@code partAfterArrow}s (i.e. {@link Proposition} parts) should be skipped
     * @return the following {@link Proposition} on higher level
     */
    private static Proposition getNextPropositionOnHigherLevel(final Proposition referenceProposition, final boolean ignorePartAfterArrow) {
        // prior got no follower on the same level, now check higher level
        final Proposition parentProposition = (Proposition) referenceProposition.getParent();
        final List<Proposition> priorChildren = parentProposition.getPriorChildren();
        if (CollectionUtil.containsInstance(priorChildren, referenceProposition)) {
            // prior is the last child in front of its parent, parent is the next proposition
            if (!ignorePartAfterArrow || parentProposition.getPartBeforeArrow() == null) {
                return parentProposition;
            }
            // parent is a partAfterArrow to ignore
            final List<Proposition> parentsLaterChildren = parentProposition.getLaterChildren();
            if (parentsLaterChildren != null && !parentsLaterChildren.isEmpty()) {
                return parentsLaterChildren.get(0);
            }
            // parent got no later children
            if (parentProposition.getPartAfterArrow() != null) {
                // return the first enclosed child
                return parentProposition.getPartAfterArrow().getPriorChildren().get(0);
            }
            return AbstractConnectable.getNextPropositionOnSameOrHigherLevel(parentProposition, true);
        }
        Proposition parentPartAfterArrow = parentProposition.getPartAfterArrow();
        if (parentPartAfterArrow != null) {
            if (!ignorePartAfterArrow) {
                // targeted proposition is the last later child of the part before arrow; the follower is the respective part after arrow
                return parentPartAfterArrow;
            }
            // parent is (still) parted by arrows
            List<Proposition> enclosedChildren = parentPartAfterArrow.getPriorChildren();
            if (enclosedChildren == null || enclosedChildren.isEmpty()) {
                enclosedChildren = parentPartAfterArrow.getLaterChildren();
            }
            if (enclosedChildren != null && !enclosedChildren.isEmpty()) {
                // follower is the first child after the proposition part after arrows
                return enclosedChildren.get(0);
            }
            // check next proposition part as well
            parentPartAfterArrow = parentPartAfterArrow.getPartAfterArrow();
            if (parentPartAfterArrow != null) {
                return parentPartAfterArrow.getPriorChildren().get(0);
            }
        }
        // no (more) proposition parts, prior's follower is the parent's follower
        return AbstractConnectable.getNextPropositionOnSameOrHigherLevel(parentProposition, ignorePartAfterArrow);
    }

    /**
     * Get the immediately following {@link Proposition} on the same or higher level of the specified {@link Proposition}, disregarding its own later
     * children.
     *
     * @param prior
     *            the {@link Proposition} to look up the follower for
     * @param ignorePartAfterArrow
     *            if {@code partAfterArrow}s (i.e. {@link Proposition} parts) should be skipped
     * @return the follower disregarding the priors own later children
     */
    private static Proposition getFollowingPropositionDisregardingPriorsChildren(final Proposition prior, final boolean ignorePartAfterArrow) {
        Proposition followingProposition = AbstractConnectable.getNextPropositionOnSameOrHigherLevel(prior, ignorePartAfterArrow);
        if (followingProposition == null) {
            return null;
        }
        IPropositionParent priorParent = prior;
        while (((Proposition) priorParent).getParent() instanceof Proposition) {
            final List<Proposition> priorParentLaterChildren = ((Proposition) ((Proposition) priorParent).getParent()).getLaterChildren();
            if (priorParentLaterChildren != null && !priorParentLaterChildren.isEmpty()
                    && priorParentLaterChildren.get(priorParentLaterChildren.size() - 1) == priorParent) {
                // prior is the last later child of its parent
                priorParent = ((Proposition) priorParent).getParent();
                continue;
            }
            break;
        }
        // browse for the very first of the following Propositions
        List<Proposition> followersPriorChildren = followingProposition.getPriorChildren();
        while (followersPriorChildren != null && !followersPriorChildren.isEmpty()
                && !CollectionUtil.containsInstance(followersPriorChildren, priorParent)) {
            // followingProposition still got at least one prior child
            final Proposition firstFollowerPriorChild = followersPriorChildren.get(0);
            // regard if prior is the last prior child of a partAfterArrow
            if (ignorePartAfterArrow) {
                Proposition partAfterArrow = firstFollowerPriorChild.getPartAfterArrow();
                while (partAfterArrow != null) {
                    if (partAfterArrow == priorParent) {
                        break;
                    }
                    partAfterArrow = partAfterArrow.getPartAfterArrow();
                }
                if (partAfterArrow != null) {
                    // prior is an enclosed child and followingProposition is behind the partAfterArrow; return current state
                    break;
                }
            }
            // first prior child of the current result is closer to prior
            followersPriorChildren = firstFollowerPriorChild.getPriorChildren();
            followingProposition = firstFollowerPriorChild;
        }
        return followingProposition;
    }
}
