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

import java.util.List;

import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.core.IModelHandler;
import org.hmx.scitos.hmx.domain.ICanHaveSyntacticalFunction;
import org.hmx.scitos.hmx.domain.ICommentable;
import org.hmx.scitos.hmx.domain.model.AbstractConnectable;
import org.hmx.scitos.hmx.domain.model.ClauseItem;
import org.hmx.scitos.hmx.domain.model.ClauseItem.Style;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.domain.model.Relation;
import org.hmx.scitos.hmx.domain.model.RelationTemplate;
import org.hmx.scitos.hmx.domain.model.SyntacticalFunction;

/**
 * Generic interface of the single point of contact for model changes in the HmX module.
 */
public interface HmxModelHandler extends IModelHandler<Pericope> {

    /**
     * Set the title, author, and comment of the whole managed model, as well as the font used for the origin text.
     *
     * @param title
     *            title to set
     * @param author
     *            author to set
     * @param comment
     *            comment text to set
     * @param originTextFontFamily
     *            origin text's font type to set
     * @param originTextFontSize
     *            size of the origin text's font to set
     */
    void setMetaData(String title, String author, String comment, String originTextFontFamily, int originTextFontSize);

    /**
     * Subordinate the given {@code target} {@link Proposition} under the specified {@code parent} in the syntactical analysis and set its indentation
     * function to the given value. This may influence indentations of {@link Proposition}s between {@code target} and {@code parent}.
     *
     * @param target
     *            {@link Proposition} to subordinate
     * @param parent
     *            parent to set for target
     * @param function
     *            indentation function to set for target
     * @throws HmxException
     *             impossible to indent target under parent
     */
    void indentPropositionUnderParent(Proposition target, Proposition parent, SyntacticalFunction function) throws HmxException;

    /**
     * Remove a single indentation of the {@code target} in the syntactical analysis. This may also affect other {@link Proposition}s with
     * indentations that become invalid by removing the {@code target}'s one.
     *
     * @param target
     *            {@link Proposition} to be indented one time less
     * @throws HmxException
     *             impossible to remove an indentation
     */
    void removeOneIndentation(Proposition target) throws HmxException;

    /**
     * Check if the removal of one indentation of the target in the syntactical analysis would affect another {@link Proposition}'s indentation.
     *
     * @param target
     *            {@link Proposition} to be indented one time less
     * @return if it would affect another {@link Proposition}'s indentation
     * @throws HmxException
     *             impossible to remove an indentation
     * @see #removeOneIndentation(Proposition)
     */
    boolean removeOneIndentationAffectsOthers(Proposition target) throws HmxException;

    /**
     * Merge the two given {@link Proposition}s, which need to be the same kind of children to the same parent.
     *
     * @param prop1
     *            one {@link Proposition} to merge with the other
     * @param prop2
     *            other {@link Proposition} to merge with the one
     * @throws HmxException
     *             impossible to merge the designated {@link Proposition}s
     */
    void mergePropositions(Proposition prop1, Proposition prop2) throws HmxException;

    /**
     * Split the selected {@link Proposition} after the designated {@link ClauseItem} and remove all {@link Relation}s that will become invalid by
     * this change.
     *
     * @param target
     *            {@link Proposition} to split
     * @param lastItemInFirstPart
     *            {@link ClauseItem} representing the end of the first part
     * @throws HmxException
     *             impossible to split {@link Proposition}
     */
    void splitProposition(Proposition target, ClauseItem lastItemInFirstPart) throws HmxException;

    /**
     * Restore the standalone state of the given {@link Proposition} part.
     *
     * @param partAfterArrow
     *            {@link Proposition} part to restore
     * @throws HmxException
     *             error occured in splitting process
     */
    void resetStandaloneStateOfPartAfterArrow(Proposition partAfterArrow) throws HmxException;

    /**
     * Merge the given {@link ClauseItem} with its preceding item.
     *
     * @param target
     *            {@link ClauseItem} to be merged with its prior
     * @throws HmxException
     *             no preceding item to merge with
     */
    void mergeClauseItemWithPrior(ClauseItem target) throws HmxException;

    /**
     * Merge the given {@link ClauseItem} with its following item.
     *
     * @param itemOne
     *            {@link ClauseItem} to be merged with its follower
     * @throws HmxException
     *             no next item to merge with
     */
    void mergeClauseItemWithFollower(ClauseItem itemOne) throws HmxException;

    /**
     * Split the targeted {@link ClauseItem} after the given origin text part.
     *
     * @param target
     *            {@link ClauseItem} to split
     * @param firstPart
     *            part of the origin text that should be left in the target
     */
    void splitClauseItem(ClauseItem target, String firstPart);

    /**
     * Set the label text of the given {@link Proposition}.
     *
     * @param target
     *            {@link Proposition} to set the label text in
     * @param labelText
     *            label text to set
     */
    void setLabelText(Proposition target, String labelText);

    /**
     * Set the translation text for the syntactical analysis in the given {@link Proposition}.
     *
     * @param target
     *            {@link Proposition} to set the syntactical translation text in
     * @param synTranslation
     *            syntactical translation text to set
     */
    void setSynTranslation(Proposition target, String synTranslation);

    /**
     * Set the translation text for the semantical analysis in the given {@link Proposition}.
     *
     * @param target
     *            {@link Proposition} to set the semantical translation text in
     * @param semTranslation
     *            semantical translation text to set
     */
    void setSemTranslation(Proposition target, String semTranslation);

    /**
     * Store the given comment text in the specified {@link ICommentable}.
     *
     * @param target
     *            element to be commented
     * @param comment
     *            comment text to set
     */
    void setComment(ICommentable target, String comment);

    /**
     * Set the function of the given element ({@link ClauseItem} or {@link Proposition}).
     *
     * @param target
     *            element to set the syntactical function on
     * @param function
     *            function to set
     */
    void setSyntacticalFunction(ICanHaveSyntacticalFunction target, SyntacticalFunction function);

    /**
     * Set the highlighting font style of the given {@link ClauseItem} to the specified value.
     *
     * @param target
     *            {@link ClauseItem} to edit
     * @param style
     *            font style to set
     */
    void setClauseItemFontStyle(ClauseItem target, Style style);

    /**
     * Create a {@link Relation} over the targeted {@code associates}, by setting their {@code roles} and {@code weights} to the specified values.
     *
     * @param associates
     *            {@link AbstractConnectable}s to combine under new super ordinated relation
     * @param template
     *            template providing the roles and weights of the {@code associates} in the new {@link Relation}
     * @throws HmxException
     *             given {@code associates} cannot be connected in a new relation
     */
    void createRelation(List<? extends AbstractConnectable> associates, RelationTemplate template) throws HmxException;

    /**
     * Rotate the functions (with their weights) between all associates of the specified {@link Relation} from top to bottom by one step.
     *
     * @param target
     *            super ordinated {@link Relation} to change functions in
     */
    void rotateAssociateRoles(Relation target);

    /**
     * Alter the {@code roles} and {@code weights} of the associates in the given {@link Relation}, while preserving everything else.
     *
     * @param target
     *            super ordinated {@link Relation} to change the {@code roles}/{@code weights} for
     * @param template
     *            template providing the roles and weights of the {@code associates} for the new {@link Relation} type
     */
    void alterRelationType(Relation target, RelationTemplate template);

    /**
     * Remove the given {@link Relation} and all its super ordinated {@link Relation}s containing it.
     *
     * @param target
     *            {@link Relation} to remove
     */
    void removeRelation(Relation target);

    /**
     * Append/prepend the given additional origin text.
     *
     * @param originText
     *            origin text to be inserted in {@link Proposition}s
     * @param inFront
     *            if the additional text should be prepended (else: appended)
     */
    void addNewPropositions(String originText, boolean inFront);

    /**
     * Append/prepend the given {@link Pericope} to the managed model.
     *
     * @param otherContent
     *            other {@link Pericope} to be merged with
     * @param inFront
     *            if the additional text should be prepended in front of the existing text (otherwise: it will be appended to the end)
     * @throws HmxException
     *             differing languages defined in both models
     */
    void mergeWithOtherPericope(Pericope otherContent, boolean inFront) throws HmxException;

    /**
     * Remove the given {@link Proposition}s and all of their super ordinated semantical {@link Relation}s.
     *
     * @param targets
     *            {@link Proposition}s to remove
     * @throws HmxException
     *             invalid selection of {@link Proposition}s to be removed
     */
    void removePropositions(List<Proposition> targets) throws HmxException;
}
