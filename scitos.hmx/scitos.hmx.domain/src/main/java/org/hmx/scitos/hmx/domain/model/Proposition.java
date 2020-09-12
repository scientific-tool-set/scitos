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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.hmx.scitos.domain.util.CollectionUtil;
import org.hmx.scitos.domain.util.ComparisonUtil;
import org.hmx.scitos.hmx.domain.ICanHaveSyntacticalFunction;
import org.hmx.scitos.hmx.domain.IPropositionParent;

/**
 * Element in a {@link Pericope}, consisting of {@link ClauseItem}s, a label text, translations for the syntactical and semantical analysis, and
 * references to syntactically subordinated {@code Proposition}s.
 */
public final class Proposition extends AbstractConnectable implements Cloneable, Iterable<ClauseItem>, IPropositionParent,
        ICanHaveSyntacticalFunction {

    /** The maximum number of characters for a proposition's label. */
    public static final int MAX_LABEL_LENGTH = 5;
    /** The maximum number of characters for a proposition's translations. */
    public static final int MAX_TRANSLATION_LENGTH = 512;

    /**
     * The included {@link ClauseItem}s containing the represented origin text part.
     */
    private final List<ClauseItem> items = new LinkedList<>();
    /** The identifying label. */
    private String label;
    /** The syntactical indentation function. */
    private SyntacticalFunction function;
    /** The syntactical translation text. */
    private String synTranslation;
    /** The semantical translation text. */
    private String semTranslation;
    /**
     * The super ordinated parent element ({@link Pericope} or {@link Proposition}).
     */
    private IPropositionParent parent;
    /**
     * The list of indented (i.e. syntactically subordinated) {@link Proposition}s in front of this one.
     */
    private List<Proposition> priorChildren;
    /**
     * The list of indented (i.e. syntactically subordinated) {@link Proposition}s following this one.
     */
    private List<Proposition> laterChildren;
    /**
     * The part of this {@link Proposition} after any enclosed (later) children.
     */
    private Proposition partAfterArrow;
    /**
     * The part of this {@link Proposition} before any enclosed (prior) children.
     */
    private Proposition partBeforeArrow;

    /**
     * Constructor: with the reference to its parent and the list of included {@link ClauseItem}s.
     *
     * @param parent
     *            {@link Pericope} or {@link Proposition} it is subordinated to
     * @param items
     *            the included {@link ClauseItem}s
     */
    public Proposition(final IPropositionParent parent, final List<ClauseItem> items) {
        this.setParent(parent);
        this.setItems(items);
    }

    /**
     * Getter for the included {@link ClauseItem}s.
     *
     * @return the included {@link ClauseItem}s
     */
    public List<ClauseItem> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    /**
     * Setter for the included {@link ClauseItem}s.
     *
     * @param items
     *            the included {@link ClauseItem}s to set
     */
    public void setItems(final List<ClauseItem> items) {
        this.items.clear();
        this.items.addAll(items);
        for (final ClauseItem singleItem : this.items) {
            singleItem.setParent(this);
        }
    }

    /**
     * Getter for the identifying label.
     *
     * @return the label text
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Setter for the identifying label.
     *
     * @param label
     *            the label text to set
     */
    public void setLabel(final String label) {
        this.label = label;
    }

    /**
     * Getter for the indentation function (related to its parent {@link Proposition}).
     */
    @Override
    public SyntacticalFunction getFunction() {
        return this.function;
    }

    /**
     * Setter for the indentation function (related to its parent {@link Proposition}).
     */
    @Override
    public void setFunction(final SyntacticalFunction syntacticalFunction) {
        this.function = syntacticalFunction;
    }

    /**
     * Getter for the translation text in the syntactical analysis.
     *
     * @return the syntactical translation text
     */
    public String getSynTranslation() {
        return this.synTranslation;
    }

    /**
     * Setter for the translation text in the syntactical analysis.
     *
     * @param syntacticalTranslation
     *            syntactical translation text to set
     */
    public void setSynTranslation(final String syntacticalTranslation) {
        this.synTranslation = syntacticalTranslation;
    }

    /**
     * Getter for the translation text in the semantical analysis.
     *
     * @return the semantical translation text
     */
    public String getSemTranslation() {
        return this.semTranslation;
    }

    /**
     * Setter for the translation text in the semantical analysis.
     *
     * @param semanticalTranslation
     *            semantical translation text to set
     */
    public void setSemTranslation(final String semanticalTranslation) {
        this.semTranslation = semanticalTranslation;
    }

    /**
     * Getter for the containing parent ({@link Pericope} or {@link Proposition}).
     *
     * @return the element this is subordinated to
     */
    public IPropositionParent getParent() {
        return this.parent;
    }

    /**
     * Setter for the containing parent ({@link Pericope} or {@link Proposition}).
     *
     * @param parent
     *            the parent element to set
     */
    public void setParent(final IPropositionParent parent) {
        this.parent = parent;
        if (this.partAfterArrow != null) {
            this.partAfterArrow.setParent(parent);
        }
    }

    /**
     * Getter for the indented (i.e. syntactically subordinated) {@link Proposition}s in front of this one.
     *
     * @return the preceding child propositions
     */
    public List<Proposition> getPriorChildren() {
        if (this.priorChildren == null || this.priorChildren.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(this.priorChildren);
    }

    /**
     * Setter for the indented (i.e. syntactically subordinated) {@link Proposition}s in front of this one.
     *
     * @param priorChildren
     *            the preceding children to set
     */
    public void setPriorChildren(final List<Proposition> priorChildren) {
        if (priorChildren == null || priorChildren.isEmpty()) {
            this.priorChildren = null;
        } else {
            this.priorChildren = new ArrayList<>(priorChildren);
            for (final Proposition singleChild : this.priorChildren) {
                singleChild.setParent(this);
            }
        }
    }

    /**
     * Getter for the indented (i.e. syntactically subordinated) {@link Proposition}s following this one.
     *
     * @return the following child propositions
     */
    public List<Proposition> getLaterChildren() {
        if (this.laterChildren == null || this.laterChildren.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(this.laterChildren);
    }

    /**
     * Setter for the indented (i.e. syntactically subordinated) {@link Proposition}s following this one.
     *
     * @param laterChildren
     *            the following children to set
     */
    public void setLaterChildren(final List<Proposition> laterChildren) {
        if (laterChildren == null || laterChildren.isEmpty()) {
            this.laterChildren = null;
        } else {
            this.laterChildren = new ArrayList<>(laterChildren);
            for (final Proposition singleChild : this.laterChildren) {
                singleChild.setParent(this);
            }
        }
    }

    /**
     * Getter for this element's first part. That might be this {@link Proposition} if no {@code partBeforeArrow} exists or the first non-{@code null}
     * {@code partBeforeArrow}.
     * 
     * @return this element's first non-{@code null} {@code partBeforeArrow} or a self-reference
     */
    public Proposition getFirstPart() {
        if (this.getPartBeforeArrow() == null) {
            return this;
        }
        return this.getPartBeforeArrow().getFirstPart();
    }

    /**
     * Getter for this element's last part. That might be this {@link Proposition} if no {@code partAfterArrow} exists or the last non-{@code null}
     * {@code partAfterArrow}.
     * 
     * @return this element's last non-{@code null} {@code partAfterArrow} or a self-reference
     */
    public Proposition getLastPart() {
        if (this.getPartAfterArrow() == null) {
            return this;
        }
        return this.getPartAfterArrow().getLastPart();
    }

    /**
     * Getter for this element's part before any enclosed children.
     *
     * @return this element's part before the enclosed children
     * @see #setPartAfterArrow(Proposition)
     */
    public Proposition getPartBeforeArrow() {
        return this.partBeforeArrow;
    }

    /**
     * Getter for this element's part after any enclosed children.
     *
     * @return this element's part after the enclosed children
     */
    public Proposition getPartAfterArrow() {
        return this.partAfterArrow;
    }

    /**
     * Set this element's part after the enclosed children and declare this to be its respective {@code partBeforeArrow}.
     *
     * @param partAfterArrow
     *            {@link Proposition} part to set
     */
    public void setPartAfterArrow(final Proposition partAfterArrow) {
        if (partAfterArrow == null) {
            if (this.partAfterArrow != null) {
                this.partAfterArrow.partBeforeArrow = null;
                this.partAfterArrow = null;
            }
        } else {
            this.partAfterArrow = partAfterArrow;
            this.partAfterArrow.partBeforeArrow = this;
            this.partAfterArrow.setParent(this.getParent());
        }
    }

    @Override
    public List<Proposition> getContainingList(final Proposition childProposition) {
        final List<Proposition> result = this.getInternalContainingList(childProposition);
        if (result == null) {
            return null;
        }
        // make sure the internal order cannot be altered via this method
        return Collections.unmodifiableList(result);
    }

    /**
     * Select and return the internal (!) list that contains the specified {@link Proposition}. For a write-protected version of the respective list,
     * that does not allow adding/removing entries, use the {@link #getContainingList(Proposition)} method instead.
     *
     * @param childProposition
     *            the {@link Proposition} to look for
     * @return the list of child propositions that contains the given one
     */
    private List<Proposition> getInternalContainingList(final Proposition childProposition) {
        if (this.priorChildren != null && CollectionUtil.containsInstance(this.priorChildren, childProposition)) {
            return this.priorChildren;
        }
        if (this.laterChildren != null && CollectionUtil.containsInstance(this.laterChildren, childProposition)) {
            return this.laterChildren;
        }
        if (this.partAfterArrow != null) {
            return this.partAfterArrow.getInternalContainingList(childProposition);
        }
        // specified element is no child of this proposition
        return null;
    }

    /**
     * Add the designated {@link Proposition} as the leading prior child.
     *
     * @param childProposition
     *            the preceding child to add
     */
    public void addFirstPriorChild(final Proposition childProposition) {
        this.addPriorChild(childProposition, true);
    }

    /**
     * Add the designated {@link Proposition} as the trailing prior child.
     *
     * @param childProposition
     *            the preceding child to add
     */
    public void addLastPriorChild(final Proposition childProposition) {
        this.addPriorChild(childProposition, false);
    }

    /**
     * Add the designated {@link Proposition} as a prior child.
     *
     * @param childProposition
     *            the preceding child to add
     * @param asLeadingChild
     *            if the new prior child should be the first in the list of preceding children (otherwise it will be the last, closest to its parent)
     */
    private void addPriorChild(final Proposition childProposition, final boolean asLeadingChild) {
        if (this.priorChildren == null) {
            this.priorChildren = new LinkedList<>();
        }
        childProposition.setParent(this);
        if (asLeadingChild) {
            this.priorChildren.add(0, childProposition);
        } else {
            this.priorChildren.add(childProposition);
        }
    }

    /**
     * Add the designated {@link Proposition} as the leading later child.
     *
     * @param childProposition
     *            the following child to add
     */
    public void addFirstLaterChild(final Proposition childProposition) {
        this.addLaterChild(childProposition, true);
    }

    /**
     * Add the designated {@link Proposition} as the trailing later child.
     *
     * @param childProposition
     *            the following child to add
     */
    public void addLastLaterChild(final Proposition childProposition) {
        this.addLaterChild(childProposition, false);
    }

    /**
     * Add the designated {@link Proposition} as a later child.
     *
     * @param childProposition
     *            the following child to add
     * @param asLeadingChild
     *            if the new later child should be the first (closest to its parent) in the list of following children (otherwise it will be the last)
     */
    private void addLaterChild(final Proposition childProposition, final boolean asLeadingChild) {
        if (this.laterChildren == null) {
            this.laterChildren = new LinkedList<>();
        }
        childProposition.setParent(this);
        if (asLeadingChild) {
            this.laterChildren.add(0, childProposition);
        } else {
            this.laterChildren.add(childProposition);
        }
    }

    @Override
    public void insertChildPropositionBeforeFollower(final Proposition toInsert, final Proposition follower) {
        // make sure to deal with first part of following proposition
        final Proposition following = follower.getFirstPart();
        final List<Proposition> containingList = this.getInternalContainingList(following);
        containingList.add(CollectionUtil.indexOfInstance(containingList, following), toInsert);
        toInsert.setParent(this);
    }

    @Override
    public void insertChildPropositionAfterPrior(final Proposition toInsert, final Proposition prior) {
        // make sure to deal with first part of prior proposition
        final Proposition before = prior.getFirstPart();
        final List<Proposition> containingList = this.getInternalContainingList(before);
        if (containingList == null) {
            // prior not contained
            throw new IllegalArgumentException();
        }
        toInsert.setParent(this);
        containingList.add(CollectionUtil.indexOfInstance(containingList, before) + 1, toInsert);
    }

    @Override
    public void removeChildProposition(final Proposition toDelete) {
        final List<Proposition> containingList = this.getInternalContainingList(toDelete);
        if (containingList != null) {
            containingList.remove(CollectionUtil.indexOfInstance(containingList, toDelete));
        } else if (this.partAfterArrow == toDelete) {
            this.setPartAfterArrow(null);
        } else {
            this.partAfterArrow.removeChildProposition(toDelete);
        }
    }

    /**
     * Add the specified {@link ClauseItem} after the designated prior {@link ClauseItem}.
     *
     * @param toInsert
     *            new {@link ClauseItem} to add
     * @param prior
     *            already contained {@link ClauseItem} in front of the new one
     */
    public void insertClauseItemAfterPrior(final ClauseItem toInsert, final ClauseItem prior) {
        toInsert.setParent(this);
        this.items.add(CollectionUtil.indexOfInstance(this.items, prior) + 1, toInsert);
    }

    /**
     * Remove the specified {@link ClauseItem}.
     *
     * @param toDelete
     *            the {@link ClauseItem} to remove
     */
    public void removeClauseItems(final List<ClauseItem> toDelete) {
        for (final ClauseItem singleItem : toDelete) {
            this.items.remove(CollectionUtil.indexOfInstance(this.items, singleItem));
        }
    }

    @Override
    public Iterator<ClauseItem> iterator() {
        return this.getItems().iterator();
    }

    /**
     * Create a copy of this {@link Proposition} WITHOUT setting its parent {@link Pericope}.
     *
     * @return cloned {@link Proposition} without model (and possible parent {@link Pericope})
     */
    @Override
    public Proposition clone() {
        final List<ClauseItem> clonedItems = new ArrayList<>(this.items.size());
        for (final ClauseItem singleItem : this.items) {
            clonedItems.add(singleItem.clone());
        }
        final Proposition cloned = new Proposition(null, clonedItems);
        cloned.label = this.label;
        cloned.function = this.function;
        cloned.semTranslation = this.semTranslation;
        cloned.synTranslation = this.synTranslation;
        cloned.setComment(this.getComment());
        if (this.priorChildren != null) {
            cloned.priorChildren = new ArrayList<>(this.priorChildren.size());
            for (final Proposition singlePriorChild : this.priorChildren) {
                final Proposition clonedChild = singlePriorChild.clone();
                clonedChild.setParent(cloned);
                cloned.priorChildren.add(clonedChild);
            }
        }
        if (this.laterChildren != null) {
            cloned.laterChildren = new ArrayList<>(this.laterChildren.size());
            for (final Proposition singleLaterChild : this.laterChildren) {
                final Proposition clonedChild = singleLaterChild.clone();
                clonedChild.setParent(cloned);
                cloned.laterChildren.add(clonedChild);
            }
        }
        if (this.partAfterArrow != null) {
            cloned.partAfterArrow = this.partAfterArrow.clone();
            cloned.partAfterArrow.partBeforeArrow = cloned;
        }
        return cloned;
    }

    @Override
    public String toString() {
        return this.items.toString();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result += this.label == null ? 0 : this.label.hashCode();
        result *= 3;
        result += this.function == null ? 0 : this.function.hashCode();
        result *= 5;
        result += this.synTranslation == null ? 0 : this.synTranslation.hashCode();
        result *= 7;
        result += this.semTranslation == null ? 0 : this.semTranslation.hashCode();
        return result * this.items.size();
    }

    @Override
    public boolean equals(final Object otherObj) {
        return this.equals(otherObj, false);
    }

    @Override
    public boolean equals(final Object otherObj, final boolean ignoreChildren) {
        if (this == otherObj) {
            return true;
        }
        if (!(otherObj instanceof Proposition)) {
            return false;
        }
        final Proposition otherProposition = (Proposition) otherObj;
        if (this.partBeforeArrow == null && otherProposition.partBeforeArrow == null) {
            // first of the (possibly multiple) proposition parts established
            return this.equalsIgnorePartBeforeArrow(otherProposition, ignoreChildren);
        }
        if (this.partBeforeArrow != null && otherProposition.partBeforeArrow != null) {
            // check part before arrow as well
            return this.partBeforeArrow.equals(otherProposition.partBeforeArrow, ignoreChildren);
        }
        return false;
    }

    /**
     * Check if this is equal to the given {@code Proposition}, disregarding any potential {@code partBeforeArrow}.
     *
     * @param otherProposition
     *            the other element to compare with
     * @param ignoreChildren
     *            if subordinated {@link Proposition}s should also be excluded from this check
     * @return if the given {@link Proposition} is equal to this one (under the given constraints)
     */
    private boolean equalsIgnorePartBeforeArrow(final Proposition otherProposition, final boolean ignoreChildren) {
        if (this.partAfterArrow == null) {
            if (otherProposition.partAfterArrow != null) {
                return false;
            }
        } else if (otherProposition.partAfterArrow == null
                || !this.partAfterArrow.equalsIgnorePartBeforeArrow(otherProposition.partAfterArrow, ignoreChildren)) {
            return false;
        }
        return super.equals(otherProposition) && ComparisonUtil.isNullOrEmptyAwareEqual(this.label, otherProposition.label)
                && ComparisonUtil.isNullAwareEqual(this.function, otherProposition.function)
                && ComparisonUtil.isNullOrEmptyAwareEqual(this.synTranslation, otherProposition.synTranslation)
                && ComparisonUtil.isNullOrEmptyAwareEqual(this.semTranslation, otherProposition.semTranslation)
                && ComparisonUtil.isNullOrEmptyAwareEqual(this.items, otherProposition.items)
                && (ignoreChildren || ComparisonUtil.isNullOrEmptyAwareEqual(this.priorChildren, otherProposition.priorChildren))
                && (ignoreChildren || ComparisonUtil.isNullOrEmptyAwareEqual(this.laterChildren, otherProposition.laterChildren))
                && this.parent.equals(otherProposition.parent, true);
    }
}
