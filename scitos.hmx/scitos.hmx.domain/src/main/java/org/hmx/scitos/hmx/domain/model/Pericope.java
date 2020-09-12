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

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.hmx.scitos.domain.IModel;
import org.hmx.scitos.domain.util.CollectionUtil;
import org.hmx.scitos.domain.util.ComparisonUtil;
import org.hmx.scitos.hmx.domain.ICommentable;
import org.hmx.scitos.hmx.domain.IPropositionParent;
import org.hmx.scitos.hmx.domain.ISyntacticalFunctionProvider;

/**
 * The main document of the SciToS module HermeneutiX, containing all top level {@link Proposition}s and some general information like origin
 * language, author, title, comment, and {@link Font} to use.
 */
public final class Pericope implements IModel<Pericope>, IPropositionParent, ICommentable, ISyntacticalFunctionProvider {

    /** The origin text language's syntactical model. */
    private LanguageModel languageModel;
    /** The top level propositions, that in turn can contain more subordinated propositions. */
    private final List<Proposition> text = new LinkedList<>();
    /**
     * The origin text's font, to allow the analysis of languages that require a special {@link Font} in order to be displayed properly.
     */
    private Font font;
    /** The author of this model. */
    private String author;
    /** The title of this model. */
    private String title;
    /** The general comment of this model. */
    private String comment;

    /**
     * Constructor: for an empty, uninitialized model that requires {@link #init(String, LanguageModel, Font)} to be called separately.
     */
    public Pericope() {
        // nothing to do here, as we need this empty reference for when it is newly created
    }

    /**
     * Initialize this model to prepare it for the actual analysis.
     *
     * @param originText
     *            origin text to be inserted in {@link Proposition}s
     * @param language
     *            the origin text language's syntactical model to set
     * @param originTextFont
     *            the {@link Font} to apply for the origin text
     */
    public void init(final String originText, final LanguageModel language, final Font originTextFont) {
        this.languageModel = new LanguageModel(language.getName(), language.isLeftToRightOriented());
        this.languageModel.addAll(language.provideFunctions());
        this.setFont(originTextFont);
        this.text.clear();
        if (originText != null) {
            this.addNewPropositions(originText, false);
        }
    }

    /**
     * Add the given origin text to this model.
     *
     * @param originText
     *            the origin text to be inserted in {@link Proposition}s
     * @param inFront
     *            if the additional text should be prepended in front (otherwise: it will be appended at the end)
     */
    public void addNewPropositions(final String originText, final boolean inFront) {
        this.addNewPropositions(this.buildPropositionsFromText(originText), inFront);
    }

    /**
     * Add the given origin text to this model.
     *
     * @param originText
     *            the origin text to be inserted
     * @param inFront
     *            if the additional text should be prepended in front (otherwise: it will be appended at the end)
     */
    public void addNewPropositions(final List<Proposition> originText, final boolean inFront) {
        for (final Proposition singleProposition : originText) {
            // make sure the proposition contains the correct parent value
            singleProposition.setParent(this);
        }
        if (inFront) {
            this.text.addAll(0, originText);
        } else {
            this.text.addAll(originText);
        }
    }

    /**
     * Construct the representing {@code Proposition}s from the given text. Each line in the given text will be represented by a single
     * {@code Proposition}.
     *
     * @param originText
     *            the origin text to be converted to a list of {@code Proposition}s
     * @return represented text
     */
    private List<Proposition> buildPropositionsFromText(final String originText) {
        final List<Proposition> propositions = new ArrayList<>();
        for (final String originTextPart : originText.split(System.getProperty("line.separator"))) {
            if (originTextPart.trim().isEmpty()) {
                // empty lines should be ignored
                continue;
            }
            final List<ClauseItem> items = new ArrayList<>();
            for (String singleItemText : originTextPart.split("\t")) {
                // empty items should be ignored
                singleItemText = singleItemText.trim().replaceAll("\\s+", " ");
                if (!singleItemText.isEmpty()) {
                    items.add(new ClauseItem(null, singleItemText));
                }
            }
            propositions.add(new Proposition(this, items));
        }
        return propositions;
    }

    /**
     * Getter for the top level {@link Proposition}s.
     *
     * @return this model's top level {@link Proposition}s
     */
    public List<Proposition> getText() {
        return Collections.unmodifiableList(this.text);
    }

    /**
     * Collects all {@link Proposition}s in a single list disregarding any indentations or splittings while preserving the original order of the
     * represented text.
     *
     * @return ordered list of all {@code Proposition}s
     */
    public List<Proposition> getFlatText() {
        final List<Proposition> result = new LinkedList<>();
        for (final Proposition singleTopLevelProposition : this.text) {
            // recursively fill result list
            this.collectFlatText(singleTopLevelProposition, result);
        }
        return result;
    }

    /**
     * Append the given {@link Proposition} and all its child {@link Proposition}s to the given list, disregarding any indentations or splittings
     * while preserving the original order of the represented text.
     *
     * @param target
     *            the {@link Proposition} to include in the given list, including any subordinated ones
     * @param flatText
     *            ordered list of {@link Proposition}s to extend
     * @see #getFlatText()
     */
    private void collectFlatText(final Proposition target, final List<Proposition> flatText) {
        // first: the subordinated propositions in front of the target
        for (final Proposition singlePriorChild : target.getPriorChildren()) {
            this.collectFlatText(singlePriorChild, flatText);
        }
        // second: the target proposition itself
        flatText.add(target);
        // third: the subordinated propositions behind the target
        for (final Proposition singleLaterChild : target.getLaterChildren()) {
            this.collectFlatText(singleLaterChild, flatText);
        }
        // fourth: the possible part-after-arrow of the target
        if (target.getPartAfterArrow() != null) {
            this.collectFlatText(target.getPartAfterArrow(), flatText);
        }
    }

    /**
     * Collect all {@link Relation}s in one list, disregarding any hierarchy while establishing a simple order: each subtree is resolved to preserve
     * the positions of each subordinated {@link Relation} followed by the super ordinated relation itself.
     *
     * @return ordered list of all relations
     */
    public List<Relation> getFlatRelations() {
        final List<Relation> result = new LinkedList<>();
        // get the first Proposition
        AbstractConnectable currentFocus = this.getPropositionAt(0);
        while (currentFocus != null) {
            // get the highest relation over the current focused Proposition
            while (currentFocus.getSuperOrdinatedRelation() != null) {
                currentFocus = currentFocus.getSuperOrdinatedRelation();
            }
            if (currentFocus instanceof Relation) {
                this.collectFlatRelations((Relation) currentFocus, result);
            }
            // iterate whole pericope
            currentFocus = currentFocus.getFollowingConnectableProposition();
        }
        return result;
    }

    /**
     * Append the given {@link Relation} and all its subordinated {@link Relation}s to the given list. Per convention: the subordinated elements come
     * first, followed by the given super ordinated one.
     *
     * @param subtreeRoot
     *            the {@link Relation} to be inserted after all its subordinated {@link Relation}s in the given list
     * @param list
     *            ordered list of {@link Relation}s to extend
     * @see #getFlatRelations()
     */
    private void collectFlatRelations(final Relation subtreeRoot, final List<Relation> list) {
        for (final AbstractConnectable singleAssociate : subtreeRoot) {
            if (singleAssociate instanceof Relation) {
                this.collectFlatRelations((Relation) singleAssociate, list);
            }
        }
        list.add(subtreeRoot);
    }

    /**
     * Getter for the origin text's language.
     *
     * @return the language of the origin text
     */
    public String getLanguage() {
        return this.languageModel == null ? null : this.languageModel.getName();
    }

    /**
     * Getter for the origin text's orientation.
     *
     * @return if the text orientation is {@code left-to-right} (otherwise {@code right-to-left})
     */
    public boolean isLeftToRightOriented() {
        return this.languageModel == null || this.languageModel.isLeftToRightOriented();
    }

    @Override
    public List<List<AbstractSyntacticalFunctionElement>> provideFunctions() {
        return this.languageModel == null ? Collections.<List<AbstractSyntacticalFunctionElement>>emptyList() : this.languageModel
                .provideFunctions();
    }

    /**
     * Getter for the origin text's {@link Font}.
     *
     * @return the {@link Font} of the origin text
     */
    public Font getFont() {
        return this.font;
    }

    /**
     * Setter for the origin text's {@link Font}.
     *
     * @param originTextFont
     *            the {@link Font} of the origin text to set
     */
    public void setFont(final Font originTextFont) {
        this.font = originTextFont;
    }

    /**
     * Getter for the author's name.
     *
     * @return the name of the author
     */
    public String getAuthor() {
        return this.author;
    }

    /**
     * Setter for the author's name.
     *
     * @param authorName
     *            the name of the author to set
     */
    public void setAuthor(final String authorName) {
        this.author = authorName;
    }

    /**
     * Getter for the overall title.
     *
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Setter for the overall title.
     *
     * @param title
     *            the title to set
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    @Override
    public String getComment() {
        return this.comment;
    }

    @Override
    public void setComment(final String comment) {
        this.comment = comment;
    }

    @Override
    public void insertChildPropositionBeforeFollower(final Proposition toInsert, final Proposition follower) {
        // make sure to deal with first part of following proposition
        final Proposition following = follower.getFirstPart();
        final int pos = CollectionUtil.indexOfInstance(this.text, following);
        if (pos == -1) {
            // follower not contained
            throw new IllegalArgumentException();
        }
        toInsert.setParent(this);
        toInsert.setFunction(null);
        this.text.add(pos, toInsert);
    }

    @Override
    public void insertChildPropositionAfterPrior(final Proposition toInsert, final Proposition prior) {
        // make sure to deal with first part of prior proposition
        final Proposition before = prior.getFirstPart();
        final int pos = CollectionUtil.indexOfInstance(this.text, before);
        if (pos == -1) {
            // prior not contained
            throw new IllegalArgumentException();
        }
        toInsert.setParent(this);
        toInsert.setFunction(null);
        this.text.add(pos + 1, toInsert);
    }

    @Override
    public void removeChildProposition(final Proposition deleted) {
        final int index = CollectionUtil.indexOfInstance(this.text, deleted);
        if (index != -1) {
            this.text.remove(index);
        } else {
            deleted.getPartBeforeArrow().removeChildProposition(deleted);
        }
    }

    @Override
    public List<Proposition> getContainingList(final Proposition childProposition) {
        if (CollectionUtil.containsInstance(this.text, childProposition)) {
            return this.getText();
        }
        return null;
    }

    /**
     * Find the {@link Proposition} at the specified position, relative to the whole model.
     *
     * @param index
     *            index of the targeted proposition
     * @return the proposition at the specified position
     * @see #indexOfProposition(Proposition)
     */
    public Proposition getPropositionAt(final int index) {
        if (this.text.isEmpty()) {
            return null;
        }
        // get the first Proposition
        Proposition proposition = this.text.get(0);
        List<Proposition> priorChildren = proposition.getPriorChildren();
        while (!priorChildren.isEmpty()) {
            proposition = priorChildren.get(0);
            priorChildren = proposition.getPriorChildren();
        }
        // iterate through text for the specified number of steps
        for (int stepsTaken = 0; stepsTaken < index; stepsTaken++) {
            proposition = proposition.getFollowingProposition();
        }
        return proposition;
    }

    /**
     * Determine the specified {@link Proposition}'s position in this model.
     *
     * @param target
     *            {@link Proposition} to look for
     * @return the proposition's index in this model
     * @see #getPropositionAt(int)
     */
    public int indexOfProposition(final Proposition target) {
        int position = 0;
        Proposition current = this.getPropositionAt(0);
        while (current != target) {
            current = current.getFollowingProposition();
            position++;
        }
        return position;
    }

    @Override
    public Pericope clone() {
        if (this.text.isEmpty()) {
            throw new IllegalStateException("Model has not been initialized yet.");
        }
        final Pericope cloned = new Pericope();
        cloned.init(null, this.languageModel, new Font(this.font.getAttributes()));
        for (final Proposition singleProposition : this.text) {
            final Proposition clonedProposition = singleProposition.clone();
            clonedProposition.setParent(cloned);
            cloned.text.add(clonedProposition);
        }
        cloned.title = this.title;
        cloned.author = this.author;
        cloned.comment = this.comment;
        /*
         * after cloning the whole Pericope the associates of the relations need to be set separately
         */
        final List<Relation> originRelations = this.getFlatRelations();
        if (originRelations != null) {
            final List<Relation> clonedRelations = new ArrayList<>(originRelations.size());
            for (final Relation singleRelation : originRelations) {
                clonedRelations.add(singleRelation.clone());
            }
            // get the first Proposition
            AbstractConnectable currentFocus = this.getPropositionAt(0);
            do {
                // get the highest relation over the current focused Proposition
                while (currentFocus.getSuperOrdinatedRelation() != null) {
                    currentFocus = currentFocus.getSuperOrdinatedRelation();
                }
                if (currentFocus instanceof Relation) {
                    this.cloneAssociations((Relation) currentFocus, originRelations, cloned, clonedRelations);
                }
                // iterate over whole pericope
                currentFocus = currentFocus.getFollowingConnectableProposition();
            } while (currentFocus != null);
        }
        return cloned;
    }

    /**
     * Connect the cloned {@link Relation}s and {@link Proposition}s with each other, based on their respective origin associations.
     *
     * @param subTreeHead
     *            original {@link Relation} to rebuild the associations from
     * @param originRelations
     *            list of original {@link Relation}s
     * @param clonedPericope
     *            cloned model to set the associations in
     * @param clonedRelations
     *            list of cloned {@link Relation}s, in the same order as the original ones
     * @see #clone()
     */
    private void cloneAssociations(final Relation subTreeHead, final List<Relation> originRelations, final Pericope clonedPericope,
            final List<Relation> clonedRelations) {
        final List<AbstractConnectable> topAssociates = subTreeHead.getAssociates();
        final List<AbstractConnectable> clonedAssociates = new ArrayList<>(topAssociates.size());
        final Relation clonedSubTreeHead = clonedRelations.get(CollectionUtil.indexOfInstance(originRelations, subTreeHead));
        for (final AbstractConnectable originAssociate : topAssociates) {
            final AbstractConnectable clonedAssociate;
            if (originAssociate instanceof Relation) {
                clonedAssociate = clonedRelations.get(CollectionUtil.indexOfInstance(originRelations, originAssociate));
                this.cloneAssociations((Relation) originAssociate, originRelations, clonedPericope, clonedRelations);
            } else {
                clonedAssociate = clonedPericope.getPropositionAt(this.indexOfProposition((Proposition) originAssociate));
            }
            clonedAssociate.setSuperOrdinatedRelation(clonedSubTreeHead, originAssociate.getRole());
            clonedAssociates.add(clonedAssociate);
        }
        clonedSubTreeHead.setAssociates(clonedAssociates);
    }

    @Override
    public int hashCode() {
        int result = this.languageModel.hashCode();
        result += this.author == null ? 0 : this.author.hashCode();
        result *= 3;
        result += this.title == null ? 0 : this.title.hashCode();
        result *= 5;
        result += this.comment == null ? 0 : this.comment.hashCode();
        return result * this.text.size();
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
        if (!(otherObj instanceof Pericope)) {
            return false;
        }
        final Pericope otherPericope = (Pericope) otherObj;
        return this.languageModel.equals(otherPericope.languageModel) && ComparisonUtil.isNullOrEmptyAwareEqual(this.author, otherPericope.author)
                && ComparisonUtil.isNullOrEmptyAwareEqual(this.title, otherPericope.title)
                && ComparisonUtil.isNullOrEmptyAwareEqual(this.comment, otherPericope.comment)
                && (ignoreChildren || this.text.equals(otherPericope.text));
    }
}
