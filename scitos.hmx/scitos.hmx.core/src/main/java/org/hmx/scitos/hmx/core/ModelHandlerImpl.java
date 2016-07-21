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

package org.hmx.scitos.hmx.core;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.hmx.scitos.core.AbstractModelHandler;
import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.domain.ModelEvent;
import org.hmx.scitos.domain.util.CollectionUtil;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.domain.ICanHaveSyntacticalFunction;
import org.hmx.scitos.hmx.domain.ICommentable;
import org.hmx.scitos.hmx.domain.IPropositionParent;
import org.hmx.scitos.hmx.domain.model.AbstractConnectable;
import org.hmx.scitos.hmx.domain.model.ClauseItem;
import org.hmx.scitos.hmx.domain.model.ClauseItem.Style;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.domain.model.Relation;
import org.hmx.scitos.hmx.domain.model.RelationTemplate;
import org.hmx.scitos.hmx.domain.model.RelationTemplate.AssociateRole;
import org.hmx.scitos.hmx.domain.model.SyntacticalFunction;

/**
 * utility class to manage all possible changes in the model.
 */
public final class ModelHandlerImpl extends AbstractModelHandler<Pericope> implements HmxModelHandler {

    /**
     * Constructor: remembering reference to managed {@code model}.
     *
     * @param model
     *            targeted {@link Pericope} to manipulate
     */
    public ModelHandlerImpl(final Pericope model) {
        super(model);
    }

    @Override
    public void setMetaData(final String title, final String author, final String comment, final String originTextFontFamily,
            final int originTextFontSize) {
        this.getModel().setTitle(title);
        this.getModel().setAuthor(author);
        this.getModel().setComment(comment);
        final Font newFont = new Font(originTextFontFamily, Font.PLAIN, originTextFontSize);
        if (this.getModel().getFont().equals(newFont)) {
            this.notifyListeners(this.getModel(), true);
        } else {
            this.getModel().setFont(newFont);
            this.notifyListeners(this.getModel(), false);
        }
    }

    @Override
    public void indentPropositionUnderParent(final Proposition target, final Proposition parent, final SyntacticalFunction function)
            throws HmxException {
        final IPropositionParent formerParent = target.getParent();
        Proposition parentPart = parent;
        do {
            if (formerParent == parentPart) {
                // target is already indented under parent
                this.setSyntacticalFunction(target, function);
                return;
            }
            parentPart = parentPart.getPartAfterArrow();
        } while (parentPart != null);
        if (formerParent == parent.getParent()) {
            /* case: target and designated parent have the same parent */
            final List<Proposition> containingList = formerParent.getContainingList(target);
            final int posTarget = CollectionUtil.indexOfInstance(containingList, target);
            final int posParent = CollectionUtil.indexOfInstance(containingList, parent);
            if (posParent == -1 || Math.abs(posTarget - posParent) != 1) {
                /*
                 * target and designated parent cannot be found in the same list or are unconnected
                 */
                throw new HmxException(HmxMessage.ERROR_INDENT);
            }
            target.setFunction(function);
            formerParent.removeChildProposition(target);
            if (posTarget < posParent) {
                // target is in front of its new parent
                parent.addFirstPriorChild(target);
            } else {
                // target is behind its new parent
                parent.getLastPart().addLastLaterChild(target);
            }
            // trigger a full model rebuild
            this.notifyListeners(this.getModel(), false);
            return;
        }
        /*
         * case: target and designated parent have not the same parent; ensure that the target is not indented to one of its own children
         */
        IPropositionParent checkParent = parent.getParent();
        while (checkParent instanceof Proposition) {
            if (checkParent == target) {
                throw new HmxException(HmxMessage.ERROR_INDENT_UNDER_OWN_CHILD);
            }
            checkParent = ((Proposition) checkParent).getParent();
        }
        /*
         * check if no other propositions exist on same or higher level between target and parent and find out which one is in front of the other
         */
        parentPart = parent;
        do {
            if (this.haveNoPropositionsInBetweenOnSameOrHigherLevel(target, parentPart)) {
                // target is in front of its new parent and no other Proposition on a higher or same level is in between
                formerParent.removeChildProposition(target);
                parentPart.addLastPriorChild(target);
                break;
            } else if (this.haveNoPropositionsInBetweenOnSameOrHigherLevel(parentPart, target)) {
                if (parentPart.getPartAfterArrow() == null) {
                    // target is behind its new parent no (directly) enclosed child
                    formerParent.removeChildProposition(target);
                    parentPart.addFirstLaterChild(target);
                } else {
                    // target is behind its new parent; as an enclosed child it should be in the partAfterArrow's list of prior children
                    formerParent.removeChildProposition(target);
                    parentPart.getPartAfterArrow().addFirstPriorChild(target);
                }
                break;
            }
            parentPart = parentPart.getPartAfterArrow();
        } while (parentPart != null);
        if (parentPart == null) {
            throw new HmxException(HmxMessage.ERROR_INDENT);
        }
        target.setFunction(function);
        // trigger a full model rebuild
        this.notifyListeners(this.getModel(), false);
    }

    /**
     * Check if the {@link Proposition}s in the given order, have no {@link Proposition}s between them that are on the same or higher level. This
     * ignores the scenario where both {@link Proposition}s have the same parent or one of the two is the parent (or parent's parent...) of the other.
     * 
     * @param propOne
     *            leading {@link Proposition} to check
     * @param propTwo
     *            trailing {@link Proposition} to check
     * @return if the given {@link Proposition}s in that order, have no {@link Proposition}s between them on the same or higher level
     * @see #indentPropositionUnderParent(Proposition, Proposition, SyntacticalFunction)
     */
    private boolean haveNoPropositionsInBetweenOnSameOrHigherLevel(final Proposition propOne, final Proposition propTwo) {
        boolean result = false;
        Proposition follower = propOne.getFollowingPropositionDisregardingChildren();
        if (follower != null) {
            while (!result) {
                if (follower == propTwo) {
                    // there are no Propositions in between on the same or higher level (this will end the loop)
                    result = true;
                } else {
                    final List<Proposition> followersPriorChildren = follower.getPriorChildren();
                    if (followersPriorChildren.isEmpty()) {
                        // order of the given Propositions is incorrect or there is at least one Proposition on the same or higher level between them
                        break;
                    }
                    follower = followersPriorChildren.get(0);
                }
            }
        }
        return result;
    }

    @Override
    public void removeOneIndentation(final Proposition target) throws HmxException {
        if (target.getParent() instanceof Pericope) {
            throw new HmxException(HmxMessage.ERROR_UNINDENT_PERICOPE);
        }
        final Proposition parent = ((Proposition) target.getParent()).getFirstPart();
        /*
         * target has to be one of the first or one of the last children
         */
        final IPropositionParent parentsParent = parent.getParent();
        final List<Proposition> priorChildren = parent.getPriorChildren();
        if (CollectionUtil.containsInstance(priorChildren, target)) {
            /*
             * case: target is a prior child
             */
            // removes the indentation of all other prior children before and finally the target itself
            for (final Proposition singleChild : new ArrayList<Proposition>(priorChildren).subList(0,
                    CollectionUtil.indexOfInstance(priorChildren, target) + 1)) {
                parent.removeChildProposition(singleChild);
                // adding child in front of its former parent in the list
                parentsParent.insertChildPropositionBeforeFollower(singleChild, parent);
            }
            // trigger a full model rebuild
            this.notifyListeners(this.getModel(), false);
            return;
        }

        // the removing of indentations of enclosed children is impossible, make sure to regard only REAL followers
        final List<Proposition> laterChildren = parent.getLastPart().getLaterChildren();
        if (!CollectionUtil.containsInstance(laterChildren, target)) {
            /*
             * case: target is an enclosed child
             */
            throw new HmxException(HmxMessage.ERROR_UNINDENT_ENCLOSED);
        }
        /*
         * case: target is a later child
         */
        // removes the indentation of all other later children after and finally the target itself
        final int position = CollectionUtil.indexOfInstance(laterChildren, target);
        for (int i = laterChildren.size(); i > position; i--) {
            final Proposition singleChild = laterChildren.get(i - 1);
            parent.removeChildProposition(singleChild);
            // adding child behind its former parent in the list of children
            parentsParent.insertChildPropositionAfterPrior(singleChild, parent);
        }
        // trigger a full model rebuild
        this.notifyListeners(this.getModel(), false);
    }

    @Override
    public boolean removeOneIndentationAffectsOthers(final Proposition target) throws HmxException {
        if (target.getParent() instanceof Pericope) {
            throw new HmxException(HmxMessage.ERROR_UNINDENT_PERICOPE);
        }
        final Proposition parent = (Proposition) target.getParent();
        /*
         * target has to be one of the first or one of the last children
         */
        List<Proposition> children = parent.getFirstPart().getPriorChildren();
        if (CollectionUtil.containsInstance(children, target)) {
            // case: target is a prior child
            return CollectionUtil.indexOfInstance(children, target) != 0;
        }

        // the removing of indentations of enclosed children is impossible, make sure to regard only REAL followers
        children = parent.getLastPart().getLaterChildren();
        if (CollectionUtil.containsInstance(children, target)) {
            // case: target is a later child
            return CollectionUtil.indexOfInstance(children, target) != (children.size() - 1);
        }
        // case: target is an enclosed child
        throw new HmxException(HmxMessage.ERROR_UNINDENT_ENCLOSED);
    }

    @Override
    public void mergePropositions(final Proposition prop1, final Proposition prop2) throws HmxException {
        if (prop1 == prop2) {
            // already merged
            return;
        }
        /*
         * make sure the propositions are the first parts, if they are already merged with enclosed children; should not be necessary cause user
         * cannot select a partAfterArrow
         */
        final Proposition propOne = prop1.getFirstPart();
        final Proposition propTwo = prop2.getFirstPart();
        final IPropositionParent parent = propOne.getParent();
        if (parent != propTwo.getParent()) {
            // the propositions have different parents, check if they are connected anyway
            if (!this.checkForConnection(propOne, propTwo) && !this.checkForConnection(propTwo, propOne)) {
                throw new HmxException(HmxMessage.ERROR_MERGE_PROPS);
            }
            // merged successfully
            this.notifyListeners(this.getModel(), false);
            return;
        }
        final List<Proposition> containingList = parent.getContainingList(propOne);
        if (!CollectionUtil.containsInstance(containingList, propTwo)) {
            // the propositions are not in the same list of children
            throw new HmxException(HmxMessage.ERROR_MERGE_PROPS);
        }
        /*
         * propositions have the same parent and are in the same list of children
         */
        final Proposition firstPart;
        final Proposition secondPart;
        // make sure the propositions are in the right order
        if (CollectionUtil.indexOfInstance(containingList, propOne) < CollectionUtil.indexOfInstance(containingList, propTwo)) {
            firstPart = propOne;
            secondPart = propTwo;
        } else {
            firstPart = propTwo;
            secondPart = propOne;
        }
        // get the last part after arrow to check for prior
        final Proposition firstAfterArrow = firstPart.getLastPart();
        // delegate merging
        if (firstAfterArrow.isPriorOf(secondPart)) {
            // no other propositions found between firstPart and secondPart
            this.mergeConnectedPropositionsIntoOne(firstAfterArrow, secondPart);
            secondPart.getParent().removeChildProposition(secondPart);
        } else {
            // there are other propositions between firstPart and secondPart
            this.mergePropositionsWithEnclosedChildren(firstPart, secondPart);
        }
        // trigger a full model rebuild
        this.notifyListeners(this.getModel(), false);
    }

    /**
     * Check if the given {@link Proposition}s are connected in the indicated order and MERGE THEM if possible. This includes a check for
     * {@code partAfterArrow}s, which are merged as well, if one of the specified {@link Proposition}s was the last (i.e. only) enclosed child.
     *
     * @param prop1
     *            first (i.e. preceding) {@link Proposition} to check
     * @param prop2
     *            second (i.e. following) {@link Proposition} to check
     * @return if successfully merged
     */
    private boolean checkForConnection(final Proposition prop1, final Proposition prop2) {
        // check if prop1 or one of its partAfterArrows is the prior of prop2
        Proposition prop1Part = prop1;
        Proposition prop2Part = prop2;
        while (prop1Part != null && !prop1Part.isPriorOf(prop2Part)) {
            // check next partAfterArrow of prop1
            prop1Part = prop1Part.getPartAfterArrow();
        }
        if (prop1Part == null) {
            prop1Part = prop1;
            prop2Part = prop2.getPartAfterArrow();
            while (prop2Part != null && !prop1Part.isPriorOf(prop2Part)) {
                // check next partAfterArrow of prop2
                prop2Part = prop2Part.getPartAfterArrow();
            }
            if (prop2Part == null) {
                return false;
            }
        }
        // prop1 is the direct prior of prop2
        this.mergeConnectedPropositions(prop1Part, prop2Part);
        return true;
    }

    /**
     * Merge the given {@link Proposition}s by appending the {@code secondPart} to the {@code firstPart} WITHOUT REMOVING the {@code secondPart} from
     * its parent, which needs to be called separately. This method assumes both {@link Proposition}s being adjacent to one another and preserves any
     * (other) child {@link Proposition}s and handles potentially affected {@link Proposition} parts or enclosed children.
     * 
     * @param prop1Part
     *            leading {@link Proposition} to receive the other {@link Proposition}'s {@link ClauseItem}s, translations, and child
     *            {@link Proposition}s
     * @param prop2Part
     *            trailing {@link Proposition} to be merged into the other {@link Proposition}
     */
    private void mergeConnectedPropositions(final Proposition prop1Part, final Proposition prop2Part) {
        if (prop1Part == prop2Part.getParent()) {
            // prop2 is the first later child of prop1
            final List<Proposition> laterChildren = prop1Part.getLaterChildren();
            this.mergeConnectedPropositionsIntoOne(prop1Part, prop2Part);
            /*
             * if prop1 had more than one later child; reinsert them at the end of the later children of the former prop2
             */
            for (int i = 1; i < laterChildren.size(); i++) {
                prop1Part.addLastLaterChild(laterChildren.get(i));
            }
        } else if (prop1Part.getParent() == prop2Part) {
            // prop1 is the last prior child of prop2
            final List<Proposition> priorChildren = prop2Part.getPriorChildren();
            final Proposition partBeforeArrow = prop2Part.getPartBeforeArrow();
            /*
             * if prop2 had more than one prior child; transfer them to the beginning of prior children of prop1
             */
            for (int i = priorChildren.size() - 2; i > -1; i--) {
                prop1Part.addFirstPriorChild(priorChildren.get(i));
            }
            if (partBeforeArrow == null) {
                // set level of prop1 equal to prop2
                prop2Part.getParent().insertChildPropositionBeforeFollower(prop1Part, prop2Part);
                // merge prop1 and prop2
                this.mergeConnectedPropositionsIntoOne(prop1Part, prop2Part);
                prop2Part.getParent().removeChildProposition(prop2Part);
            } else {
                /*
                 * prop1 is the last enclosed child, but not the only one; set prop1 to be the new partAfterArrow of prop2's partBeforeArrow
                 */
                partBeforeArrow.setPartAfterArrow(prop1Part);
                this.mergeConnectedPropositionsIntoOne(prop1Part, prop2Part);
            }
        } else {
            // prop1 and prop2 are not directly subordinated to each other
            final Proposition partAfterArrow = prop1Part.getPartAfterArrow();
            final boolean propTwoIsSingleEnclosedChild = partAfterArrow != null && prop2Part.isPriorOf(partAfterArrow);
            final Proposition partBeforeArrow = prop2Part.getPartBeforeArrow();
            this.mergeConnectedPropositionsIntoOne(prop1Part, prop2Part);
            prop2Part.getParent().removeChildProposition(prop2Part);
            if (propTwoIsSingleEnclosedChild) {
                /*
                 * prop2 is the last enclosed child of prop1 and its part after arrow; merge all three
                 */
                // merge combined prop1 and prop2 with partAfterArrow
                this.mergeConnectedPropositionsIntoOne(prop1Part, partAfterArrow);
            } else if (partAfterArrow != null) {
                /*
                 * add the former part after arrow of prop1 at the end of the combined proposition, cause the current part after arrow is the part
                 * formerly specified for prop2
                 */
                final Proposition prop1LastPart = prop1Part.getLastPart();
                prop1LastPart.setPartAfterArrow(partAfterArrow);
                if (partAfterArrow.getPriorChildren().isEmpty()) {
                    // merge those two partAfterArrows as they are connected (i.e. they have no enclosed children between them)
                    this.mergeConnectedPropositionsIntoOne(prop1LastPart, partAfterArrow);
                }
            } else if (partBeforeArrow != null) {
                /*
                 * re-instate the combined proposition as the partAfterArrow instead of the former prop2, as prop1 was an enclosed child, that was
                 * indented under another enclosed child of prop2 and its partBeforeArrow
                 */
                prop1Part.getParent().removeChildProposition(prop1Part);
                final List<Proposition> priorChildren = new ArrayList<Proposition>(prop2Part.getPriorChildren());
                priorChildren.addAll(prop1Part.getPriorChildren());
                prop1Part.setPriorChildren(priorChildren);
                prop1Part.setFunction(null);
                partBeforeArrow.setPartAfterArrow(prop1Part);
            }
        }
    }

    /**
     * Merge the given {@link Proposition}s by appending the {@code secondPart} to the {@code firstPart} WITHOUT REMOVING the {@code secondPart} from
     * its parent, which needs to be called separately.
     *
     * @param firstPart
     *            {@link Proposition} which appears earlier in the {@link Pericope}
     * @param secondPart
     *            {@link Proposition} to be added at the end of the firstPart
     * @see #mergeConnectedPropositions(Proposition, Proposition)
     */
    private void mergeConnectedPropositionsIntoOne(final Proposition firstPart, final Proposition secondPart) {
        // merge ClauseItems
        final List<ClauseItem> items = new ArrayList<ClauseItem>(firstPart.getItems());
        items.addAll(secondPart.getItems());
        firstPart.setItems(items);
        if (firstPart.getLabel() == null || firstPart.getLabel().isEmpty()) {
            firstPart.setLabel(secondPart.getLabel());
        }
        firstPart.setSynTranslation(this.mergeText(firstPart.getSynTranslation(), secondPart.getSynTranslation(), ' '));
        firstPart.setSemTranslation(this.mergeText(firstPart.getSemTranslation(), secondPart.getSemTranslation(), ' '));
        firstPart.setComment(this.mergeText(firstPart.getComment(), secondPart.getComment(), '\n'));
        // transfer function (related to its parent proposition)
        if (firstPart.getFunction() == null && !(firstPart.getParent() instanceof Pericope)) {
            firstPart.setFunction(secondPart.getFunction());
        }
        firstPart.setLaterChildren(secondPart.getLaterChildren());
        // transfer part after arrow
        firstPart.setPartAfterArrow(secondPart.getPartAfterArrow());
        final Relation superOrdinated = secondPart.getSuperOrdinatedRelation();
        if (firstPart.getSuperOrdinatedRelation() == null && superOrdinated != null) {
            final List<AbstractConnectable> associates = new ArrayList<AbstractConnectable>(superOrdinated.getAssociates());
            associates.set(CollectionUtil.indexOfInstance(associates, secondPart), firstPart);
            firstPart.setSuperOrdinatedRelation(superOrdinated, secondPart.getRole());
            superOrdinated.setAssociates(associates);
        } else if (superOrdinated != null) {
            superOrdinated.kill();
        }
    }

    /**
     * Combine the two texts by separating them with the given character. If one of the texts is {@code null}, the other one is returned.
     * 
     * @param textOne
     *            leading text to be merged
     * @param textTwo
     *            trailing text to be merged
     * @param separatorChar
     *            whitespace character to insert between both texts if they are both not {@code null}
     * @return merged text
     */
    private String mergeText(final String textOne, final String textTwo, final char separatorChar) {
        final String result;
        if (textOne == null) {
            result = textTwo;
        } else if (textTwo == null) {
            result = textOne;
        } else {
            result = (textOne + separatorChar + textTwo).trim();
        }
        return result;
    }

    /**
     * Merge the given {@link Proposition}s by enclosing the {@link Proposition}s between them. The {@code secondPart} will become the
     * {@code firstPart}'s {@code partAfterArrow}. All {@link Relation}s containing the {@code secondPart} will be removed.
     *
     * @param firstPart
     *            {@link Proposition} which appears earlier in the {@link Pericope}
     * @param secondPart
     *            {@link Proposition} to become the {@code partAfterArrow} of the {@code firstPart}
     */
    private void mergePropositionsWithEnclosedChildren(final Proposition firstPart, final Proposition secondPart) {
        // make sure the second proposition is in no relation
        final Relation relationOfSecond = secondPart.getSuperOrdinatedRelation();
        if (relationOfSecond != null) {
            relationOfSecond.kill();
        }
        // deal with propositions on same level between them
        // make sure the secondPart is added to the end of the firstPart, if the firstPart is already merged with enclosed children
        final Proposition firstProposition = firstPart.getLastPart();
        final IPropositionParent parent = firstPart.getParent();
        final List<Proposition> containingList = parent.getContainingList(firstPart);
        final int start = CollectionUtil.indexOfInstance(containingList, firstPart);
        final int end = CollectionUtil.indexOfInstance(containingList, secondPart) - 1;
        // iterate all propositions on same level between them (from back to front to avoid invalid indices)
        for (int i = end; i > start; i--) {
            // convert to prior child of the secondPart (with model info)
            final Proposition toConvert = containingList.get(i);
            parent.removeChildProposition(toConvert);
            secondPart.addFirstPriorChild(toConvert);
        }
        // convert later children of firstPart to prior children of secondPart
        final List<Proposition> firstLaterChildren = firstProposition.getLaterChildren();
        if (!firstLaterChildren.isEmpty()) {
            final List<Proposition> secondPriorChildren = secondPart.getPriorChildren();
            if (secondPriorChildren.isEmpty()) {
                secondPart.setPriorChildren(firstLaterChildren);
            } else {
                // make sure all enclosed children are in the same list to keep the ability to merge and indent of enclosed
                final List<Proposition> combinedChildren = new LinkedList<Proposition>(firstLaterChildren);
                combinedChildren.addAll(secondPriorChildren);
                secondPart.setPriorChildren(combinedChildren);
            }
            firstProposition.setLaterChildren(null);
        }
        // finish model changes
        parent.removeChildProposition(secondPart);
        secondPart.setFunction(null);
        firstProposition.setPartAfterArrow(secondPart);
    }

    @Override
    public void splitProposition(final Proposition target, final ClauseItem lastItemInFirstPart) throws HmxException {
        final List<ClauseItem> firstPartItems = target.getItems();
        if (!CollectionUtil.containsInstance(firstPartItems, lastItemInFirstPart)) {
            throw new IllegalArgumentException();
        }
        final int lastItemIndex = CollectionUtil.indexOfInstance(firstPartItems, lastItemInFirstPart);
        if (lastItemIndex + 1 < firstPartItems.size()) {
            final List<ClauseItem> secondPartItems = new ArrayList<ClauseItem>(firstPartItems.subList(lastItemIndex + 1, firstPartItems.size()));
            // removes the duplicates of the secondPart from the firstPart
            target.removeClauseItems(secondPartItems);
            // make sure the propositions are still in the right order, transfer target's later children to the new proposition
            final List<Proposition> laterChildren = target.getLaterChildren();
            target.setLaterChildren(null);
            final Proposition secondPart = new Proposition(target.getParent(), secondPartItems);
            secondPart.setLaterChildren(laterChildren);

            // avoid gaps in super ordinated relations
            AbstractConnectable currentFocus = target;
            Relation superOrdinated;
            while ((superOrdinated = currentFocus.getSuperOrdinatedRelation()) != null) {
                final List<AbstractConnectable> associates = superOrdinated.getAssociates();
                if (currentFocus != associates.get(associates.size() - 1)) {
                    /*
                     * target is not the last associate in one of its super ordinated relations; this relation will become invalid by splitting the
                     * targeted proposition; it needs to be removed
                     */
                    superOrdinated.kill();
                    break;
                }
                currentFocus = superOrdinated;
            }
            // transfer part after arrow
            target.setPartAfterArrow(null);
            secondPart.setPartAfterArrow(target.getPartAfterArrow());

            // finish model changes
            target.getParent().insertChildPropositionAfterPrior(secondPart, target);
            // trigger a full model rebuild
            this.notifyListeners(this.getModel(), false);
            return;
        }
        final Proposition partAfterArrow = target.getPartAfterArrow();
        if (partAfterArrow == null) {
            // lastItemInFirstPart is last item in target proposition
            throw new HmxException(HmxMessage.ERROR_SPLIT_PROP);
        }
        /*
         * the split position is the end of one proposition part; execute split by reseting the part after arrows standalone state
         */
        // avoid gaps in super ordinated relations
        AbstractConnectable currentFocus = partAfterArrow.getFollowingConnectableProposition();
        if (currentFocus != null) {
            Relation superOrdinated;
            while ((superOrdinated = currentFocus.getSuperOrdinatedRelation()) != null) {
                if (currentFocus != superOrdinated.getAssociates().get(0)) {
                    /*
                     * follower is not the first associate in one of its super ordinated relations; this relation will become invalid by splitting the
                     * targeted proposition; it needs to be removed
                     */
                    superOrdinated.kill();
                    break;
                }
                currentFocus = superOrdinated;
            }
        }
        // finish model changes
        target.setPartAfterArrow(null);
        target.getParent().insertChildPropositionAfterPrior(partAfterArrow, target);
        // trigger a full model rebuild
        this.notifyListeners(this.getModel(), false);
    }

    @Override
    public void resetStandaloneStateOfPartAfterArrow(final Proposition partAfterArrow) throws HmxException {
        final List<ClauseItem> items = partAfterArrow.getPartBeforeArrow().getItems();
        this.splitProposition(partAfterArrow.getPartBeforeArrow(), items.get(items.size() - 1));
    }

    @Override
    public void mergeClauseItemWithPrior(final ClauseItem itemTwo) throws HmxException {
        final Proposition parent = itemTwo.getParent();
        final List<ClauseItem> parentItems = parent.getItems();
        final int itemIndex = CollectionUtil.indexOfInstance(parentItems, itemTwo);
        if (itemIndex > 0) {
            this.mergeClauseItems(parentItems.get(itemIndex - 1), itemTwo);
        } else {
            // designated clause item got no prior
            throw new HmxException(HmxMessage.ERROR_MERGE_ITEMS_NO_PRIOR);
        }
    }

    @Override
    public void mergeClauseItemWithFollower(final ClauseItem itemOne) throws HmxException {
        final Proposition parent = itemOne.getParent();
        final List<ClauseItem> parentItems = parent.getItems();
        final int itemIndex = CollectionUtil.indexOfInstance(parentItems, itemOne);
        if (itemIndex + 1 < parentItems.size()) {
            this.mergeClauseItems(itemOne, parentItems.get(itemIndex + 1));
        } else {
            // designated clause item got no follower
            throw new HmxException(HmxMessage.ERROR_MERGE_ITEMS_NO_FOLLOWER);
        }
    }

    /**
     * Merge the given {@link ClauseItem}s by appending {@code itemTwo} at the end of {@code itemOne}, Triggering an update {@link ModelEvent} for the
     * containing {@link Proposition}.
     *
     * @param itemOne
     *            {@link ClauseItem} representing the beginning of the merged
     * @param itemTwo
     *            {@link ClauseItem} representing the end of the merged
     */
    private void mergeClauseItems(final ClauseItem itemOne, final ClauseItem itemTwo) {
        itemOne.setOriginText(this.mergeText(itemOne.getOriginText(), itemTwo.getOriginText(), ' '));
        if (itemOne.getFunction() == null) {
            itemOne.setFunction(itemTwo.getFunction());
        }
        // finish model changes by using the event creating methods
        final Proposition parent = itemOne.getParent();
        parent.removeClauseItems(Collections.singletonList(itemTwo));
        // trigger refresh of changed proposition
        this.notifyListeners(parent, true);
    }

    @Override
    public void splitClauseItem(final ClauseItem target, final String firstPart) {
        if (firstPart.isEmpty() || !target.getOriginText().startsWith(firstPart) || target.getOriginText().equals(firstPart)) {
            // leftPart is not the first origin text part of the target item (or the whole text)
            throw new IllegalArgumentException();
        }
        final Proposition parent = target.getParent();
        final ClauseItem toInsert = new ClauseItem(parent, target.getOriginText().substring(firstPart.length()).trim());
        // finishing the split by executing it in the model
        target.setOriginText(firstPart);
        parent.insertClauseItemAfterPrior(toInsert, target);
        // trigger refresh of changed proposition
        this.notifyListeners(parent, true);
    }

    @Override
    public void setLabelText(final Proposition target, final String labelText) {
        target.setLabel(labelText);
        // trigger refresh of changed proposition
        this.notifyListeners(target, true);
    }

    @Override
    public void setSynTranslation(final Proposition target, final String synTranslation) {
        target.setSynTranslation(synTranslation);
        // trigger refresh of changed proposition
        this.notifyListeners(target, true);
    }

    @Override
    public void setSemTranslation(final Proposition target, final String semTranslation) {
        target.setSemTranslation(semTranslation);
        // trigger refresh of changed proposition
        this.notifyListeners(target, true);
    }

    @Override
    public void setComment(final ICommentable target, final String comment) {
        target.setComment(comment);
        // trigger refresh of changed model element
        this.notifyListeners(target, true);
    }

    @Override
    public void setSyntacticalFunction(final ICanHaveSyntacticalFunction target, final SyntacticalFunction function) {
        target.setFunction(function);
        // trigger refresh of changed item
        this.notifyListeners(target, true);
    }

    @Override
    public void setClauseItemFontStyle(final ClauseItem target, final Style style) {
        target.setFontStyle(style);
        // trigger refresh of changed item
        this.notifyListeners(target, true);
    }

    @Override
    public void createRelation(final List<? extends AbstractConnectable> associates, final RelationTemplate template) throws HmxException {
        final int associateCount = associates.size();
        if (associateCount < 2 || associateCount > 2 && !template.canHaveMoreThanTwoAssociates()) {
            throw new IllegalArgumentException();
        }
        // no associate is allowed to have a super ordinated relation
        for (final AbstractConnectable singleAssociate : associates) {
            if (singleAssociate.getSuperOrdinatedRelation() != null) {
                // already has an super ordinated relation
                throw new HmxException(HmxMessage.ERROR_RELATION_UNCONNECTED);
            }
        }
        // make sure each associate is the direct prior of the next associate
        final int lastIndex = associates.size() - 1;
        for (int associateIndex = 0; associateIndex < lastIndex; associateIndex++) {
            final Proposition toCompare;
            if (associates.get(associateIndex + 1) instanceof Proposition) {
                toCompare = (Proposition) associates.get(associateIndex + 1);
            } else {
                // get the representative proposition
                toCompare = ((Relation) associates.get(associateIndex + 1)).getFirstPropositionContained();
            }
            // check if the two propositions are direct neighbors
            if (!associates.get(associateIndex).isConnectablePriorOf(toCompare)) {
                // at least two of the associates are not direct neighbors
                throw new HmxException(HmxMessage.ERROR_RELATION_UNCONNECTED);
            }
        }
        // associates are immediate neighbors and can be part of a relation
        final Relation newRelation = new Relation(associates, template);
        // trigger refresh of changed proposition
        this.notifyListeners(newRelation, false);
    }

    @Override
    public void rotateAssociateRoles(final Relation target) {
        final List<AbstractConnectable> associates = target.getAssociates();
        // remember first associates role and weight
        AssociateRole role = associates.get(0).getRole();
        for (final AbstractConnectable singleAssociate : associates) {
            // swap role and weight with the role and weight of the prior
            final AssociateRole tempRole = singleAssociate.getRole();
            singleAssociate.setSuperOrdinatedRelation(target, role);
            role = tempRole;
        }
        // set first associates role and weight to the former ones of the last
        associates.get(0).setSuperOrdinatedRelation(target, role);
        // trigger full model rebuild
        this.notifyListeners(target, true);
    }

    @Override
    public void alterRelationType(final Relation target, final RelationTemplate template) {
        final List<AbstractConnectable> associates = target.getAssociates();
        final List<AssociateRole> rolesAndWeights = template.getAssociateRoles(associates.size());
        for (int i = 0; i < associates.size(); i++) {
            associates.get(i).setSuperOrdinatedRelation(target, rolesAndWeights.get(i));
        }
        // trigger refresh of the changed relation
        this.notifyListeners(target, true);
    }

    @Override
    public void removeRelation(final Relation target) {
        target.kill();
        // trigger full model rebuild
        this.notifyListeners(this.getModel(), false);
    }

    @Override
    public void addNewPropositions(final String originText, final boolean inFront) {
        this.getModel().addNewPropositions(originText, inFront);
        // trigger full model rebuild
        this.notifyListeners(this.getModel(), false);
    }

    @Override
    public void mergeWithOtherPericope(final Pericope otherPericope, final boolean inFront) throws HmxException {
        if (!this.getModel().getLanguage().equals(otherPericope.getLanguage())) {
            throw new HmxException(HmxMessage.ERROR_MERGE_PERICOPES_LANGUAGE_CONFLICT);
        }
        this.getModel().addNewPropositions(otherPericope.getText(), inFront);
        // trigger full model rebuild
        this.notifyListeners(this.getModel(), false);
    }

    @Override
    public void removePropositions(final List<Proposition> targets) throws HmxException {
        // ensure all conditions are fulfilled
        for (final Proposition singleProposition : targets) {
            // 1. shall not be indented
            if (!(singleProposition.getParent() instanceof Pericope)
            // 2. shall not be part of a multi-part-proposition
                    || singleProposition.getPartBeforeArrow() != null || singleProposition.getPartAfterArrow() != null
                    // 3. shall not have sub ordinated propositions
                    || !singleProposition.getPriorChildren().isEmpty() || !singleProposition.getLaterChildren().isEmpty()) {
                throw new HmxException(HmxMessage.ERROR_PROPOSITIONS_DELETE_CONDITIONS_NOT_MET);
            }
        }
        if (targets.isEmpty()) {
            throw new HmxException(HmxMessage.ERROR_PROPOSITIONS_DELETE_NONE_SELECTED);
        }
        // make sure, that at least one proposition will be left
        if (this.getModel().getFlatText().size() == targets.size()) {
            throw new HmxException(HmxMessage.ERROR_PROPOSITIONS_DELETE_ALL_SELECTED);
        }
        for (final Proposition singleProposition : targets) {
            // kill relation of this proposition
            if (singleProposition.getSuperOrdinatedRelation() != null) {
                singleProposition.getSuperOrdinatedRelation().kill();
            }
            // remove targeted proposition from pericope
            singleProposition.getParent().removeChildProposition(singleProposition);
        }
        // trigger full model rebuild
        this.notifyListeners(this.getModel(), false);
    }
}
