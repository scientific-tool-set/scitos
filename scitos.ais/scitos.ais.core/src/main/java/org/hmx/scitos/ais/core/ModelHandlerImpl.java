/*
   Copyright (C) 2016-2020 HermeneutiX.org

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

package org.hmx.scitos.ais.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.hmx.scitos.ais.core.i18n.AisMessage;
import org.hmx.scitos.ais.domain.IDetailCategoryProvider;
import org.hmx.scitos.ais.domain.model.AisProject;
import org.hmx.scitos.ais.domain.model.DetailCategory;
import org.hmx.scitos.ais.domain.model.Interview;
import org.hmx.scitos.ais.domain.model.TextToken;
import org.hmx.scitos.core.AbstractModelHandler;
import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.domain.util.CollectionUtil;
import org.hmx.scitos.domain.util.ComparisonUtil;

/**
 * Single manager for handling model changes in an {@link AisProject}.
 */
public final class ModelHandlerImpl extends AbstractModelHandler<AisProject> implements AisModelHandler {

    /** RegEx: where to split an interview's text into separate paragraphs (at line separators). */
    private static final String REGEX_PARAGRAPH_SEPARATOR = "[\\s\\p{Z}]*[" + System.getProperty("line.separator") + "]([\\s\\p{Z}]*["
            + System.getProperty("line.separator") + "]*[\\s\\p{Z}]*)+";
    /** RegEx: where to split an interview's paragraph text into separate tokens (at whitespaces). */
    private static final String REGEX_TOKEN_SEPARATOR = "[\\s\\p{Z}]+";

    /**
     * Main constructor.
     *
     * @param model
     *            managed (top level) model object
     */
    public ModelHandlerImpl(final AisProject model) {
        super(model);
    }

    /**
     * Create a single {@link Interview} with the given participant id and initial text and add it to the underlying data model.
     *
     * @param participantId participant id the new interview should be associated with
     * @param text initial interview text to set (may still be altered afterwards and can be {@code null} here)
     * @return created {@link Interview} instance
     */
    private synchronized Interview createInterview(final String participantId, final String text) {
        // get rid of leading/trailing whitespaces
        final String cleanId = participantId.trim();
        // determine the highest interview index for this participant, that is in use
        int maxUsedIndex = 0;
        for (final Interview singleInterview : this.getModel().getInterviews()) {
            if (cleanId.equals(singleInterview.getParticipantId())) {
                maxUsedIndex = Math.max(maxUsedIndex, singleInterview.getIndex());
            }
        }
        // instantiate a new Interview for the given participant with the next higher index
        final Interview interview = new Interview(cleanId, maxUsedIndex + 1);
        if (text != null) {
            final List<TextToken> paragraphs = this.determineTokensFromText(text);
            interview.setText(paragraphs);
        }
        final List<Interview> interviews = new ArrayList<>(this.getModel().getInterviews());
        interviews.add(interview);
        this.getModel().setInterviews(interviews);
        this.notifyListeners(interview, false);
        return interview;
    }

    @Override
    public Interview createInterview(final String participantId) {
        return this.createInterview(participantId, null);
    }

    @Override
    public List<Interview> createInterviews(final Collection<? extends InterviewToCreate> interviewsToCreate) {
        final List<Interview> newInterviews = interviewsToCreate.stream()
                .map(toCreate -> this.createInterview(toCreate.getParticipantId(), toCreate.getInterviewText()))
                .collect(Collectors.toList());
        return newInterviews;
    }

    @Override
    public synchronized void setInterviewText(final Interview interview, final String text) {
        final List<TextToken> paragraphs = this.determineTokensFromText(text);
        // add the collected paragraphs to the given interview
        interview.setText(paragraphs);
        // trigger model change event for the modified interview
        this.notifyListeners(interview, true);
    }

    /**
     * Collect the tokenized paragraphs from the given text.
     *
     * @param text interview text to split into paragraphs and separate tokens
     * @return tokenized text
     */
    private List<TextToken> determineTokensFromText(final String text) {
        final List<TextToken> paragraphs = new LinkedList<>();
        // split text into paragraphs at line separators
        for (final String singleParagraph : text.split(ModelHandlerImpl.REGEX_PARAGRAPH_SEPARATOR)) {
            // separate paragraph into tokens (ideally words) at whitespaces
            final String[] texts = singleParagraph.split(ModelHandlerImpl.REGEX_TOKEN_SEPARATOR);
            // tokens are stored as a double linked list (each token knows the previous and following token)
            final TextToken firstToken = new TextToken(texts[0]).setFirstTokenOfDetail(true);
            // iterate over all tokens to build that double linked list for this paragraph
            TextToken previousToken = firstToken;
            for (final String singleTokenText : Arrays.asList(texts).subList(1, texts.length)) {
                final TextToken currentToken = new TextToken(singleTokenText).setPreviousToken(previousToken);
                previousToken.setFollowingToken(currentToken);
                previousToken = currentToken;
            }
            previousToken.setLastTokenOfDetail(true);
            // add paragraph represented by the first token (of the double linked list) to the list of paragraphs
            paragraphs.add(firstToken);
        }
        return paragraphs;
    }

    @Override
    public void deleteInterview(final Interview interview) {
        // collect all interviews of the model (to modify and reset this list to the model)
        final List<Interview> interviews = new ArrayList<>(this.getModel().getInterviews());
        // get the list of interviews for the same participant (as they might be affected by changing indices)
        final List<Interview> affectedInterviews = this.getModel().getSubModelObjects().get(interview.getParticipantId());
        if (affectedInterviews.size() > interview.getIndex()) {
            // reduce the index of all following interviews of the same participant by one
            for (final Interview followingInterview : affectedInterviews.subList(interview.getIndex(), affectedInterviews.size())) {
                followingInterview.setIndex(followingInterview.getIndex() - 1);
            }
        }
        // remove the targeted interview from the model's list of interviews
        interviews.remove(CollectionUtil.indexOfInstance(interviews, interview));
        // set the modified list of interviews on the model
        this.getModel().setInterviews(interviews);
        // trigger model change event for the whole model (as multiple interviews might be changed)
        this.notifyListeners(this.getModel(), false);
    }

    @Override
    public synchronized void assignDetailCategory(final Interview interview, final List<TextToken> tokens, final DetailCategory category)
            throws HmxException {
        // check if the designated tokens are interrupted by other tokens
        boolean isContiguous = true;
        // assume the given tokens are in the same paragraph and the correct order
        TextToken expectedToken = tokens.get(0);
        for (final TextToken singleToken : tokens) {
            if (expectedToken == singleToken) {
                expectedToken = expectedToken.getFollowingToken();
            } else {
                // encountered another token that is not in the given list
                isContiguous = false;
                break;
            }
        }
        if (isContiguous) {
            // assigning a category to a contiguous selection always works
            this.assignDetailCategoryToContiguousTokens(tokens, category);
        } else {
            // assigning a category to an interrupted selection may yield a HmxException, if the selection is deemed invalid
            this.assignDetailCategoryToInterruptedTokenRange(tokens, category);
        }
        // trigger model change event for the modified interview
        this.notifyListeners(interview, true);
    }

    @Override
    public synchronized void replaceCategoryModel(final IDetailCategoryProvider newModel, final Map<DetailCategory, DetailCategory> mappedOldToNew) {
        // set the new detail categories on the project's model
        this.getModel().setCategories(newModel.provide());
        // remove or replace all old detail categories in all interviews
        for (final Interview singleInterview : this.getModel().getInterviews()) {
            // iterate over each paragraph
            for (final TextToken paragraphStart : singleInterview.getText()) {
                // iterate over double linked list of tokens (each token knows the previous and following token)
                TextToken currentToken = paragraphStart;
                while (currentToken != null) {
                    // check if the token has an assigned (old) detail category and if a mapping was provided for it
                    if (currentToken.getDetail() != null && mappedOldToNew.containsKey(currentToken.getDetail())) {
                        // apply mapping to set new category instead of old (leave start/end unchanged)
                        currentToken.setDetail(mappedOldToNew.get(currentToken.getDetail()));
                    } else if (currentToken.getDetail() != null) {
                        // no mapping for old category provided, remove it while making sure the scoring stays valid
                        this.assignDetailCategoryToContiguousTokens(Arrays.asList(currentToken), null);
                    }
                    currentToken = currentToken.getFollowingToken();
                }
            }
        }
        // trigger model change event on the whole model
        this.notifyListeners(this.getModel(), true);
    }

    /**
     * Assign the specified detail category to the given token range. The given tokens are assumed to be in the correct, uninterrupted order as they
     * are appearing in a single paragraph of an interview.
     *
     * @param tokens
     *            the range of tokens to assign the specified detail category to, thereby replacing any already assigned detail category
     * @param category
     *            the detail category to assign
     */
    private void assignDetailCategoryToContiguousTokens(final List<TextToken> tokens, final DetailCategory category) {
        // validate affected sections (that end or start in the selected token range)
        for (final Entry<DetailCategory, CategoryConflictHandler> singleSection : this.collectIntersectedCategories(tokens).entrySet()) {
            final int startsToMoveCount = singleSection.getValue().getNumberOfStartedButNotEndedConflicts();
            if (startsToMoveCount > 0) {
                // more sections of this category are being opened than closed in the selected tokens
                this.moveCategorySectionStartToTheRight(tokens.get(tokens.size() - 1).getFollowingToken(), singleSection.getKey(),
                        startsToMoveCount);
            }
            final int endsToMoveCount = singleSection.getValue().getNumberOfEndedButNotStartedConflicts();
            if (endsToMoveCount > 0) {
                // less sections of this category are being opened than closed in the selected tokens: move section ends to the left
                this.moveCategorySectionEndToTheLeft(tokens.get(0).getPreviousToken(), singleSection.getKey(), endsToMoveCount);
            }
        }
        // replace currently assigned categories and clear start/end markers for all targeted tokens
        tokens.forEach(selectedToken -> selectedToken.setDetail(category).setFirstTokenOfDetail(false).setLastTokenOfDetail(false));
        // set start marker if separation is required
        final TextToken firstTarget = tokens.get(0);
        final TextToken lastBeforeSelection = firstTarget.getPreviousToken();
        if (lastBeforeSelection != null && lastBeforeSelection.getDetail() == null) {
            // previous token's category == null, only separate if assigned category != null
            lastBeforeSelection.setLastTokenOfDetail(category != null);
            firstTarget.setFirstTokenOfDetail(category != null);
        } else {
            // paragraph starts or the previous token has an assigned category, we need separation
            firstTarget.setFirstTokenOfDetail(true);
        }
        // set end marker if separation is required
        final TextToken lastTarget = tokens.get(tokens.size() - 1);
        final TextToken firstAfterSelection = lastTarget.getFollowingToken();
        if (firstAfterSelection != null && firstAfterSelection.getDetail() == null) {
            // following token's category == null, only separate if assigned category != null
            lastTarget.setLastTokenOfDetail(category != null);
            firstAfterSelection.setFirstTokenOfDetail(category != null);
        } else {
            // paragraph ends or the following token has an assigned category, we need separation
            lastTarget.setLastTokenOfDetail(true);
        }
    }

    /**
     * Assign the specified detail category to the given token range. The given tokens are assumed to be in the correct order as they are appearing in
     * a single paragraph of an interview. But at least one other token between the first and last token of the given range is not part of the given
     * tokens – i.e. the token range is interrupted by other tokens that should maintain their currently assigned detail categories.
     *
     * @param tokens
     *            the tokens to assign the specified detail category to, thereby replacing any already assigned detail category
     * @param category
     *            the detail category to assign
     * @throws HmxException
     *             assigning a detail category to the given interrupted token range would result in an invalid model state (i.e. alternate assignments
     *             like ABAB), which cannot be represented by the assumed hierarchical structure
     */
    private void assignDetailCategoryToInterruptedTokenRange(final List<TextToken> tokens, final DetailCategory category) throws HmxException {
        final List<List<TextToken>> parts = this.collectInterruptedSelectionParts(tokens);
        /*
         * an interrupted selection can only work if there are no intersections with other interrupted category ranges, i.e. any opened details in the
         * first selected part can be resolved in the enclosed section, any closed details in the last selected part can also be resolved in the
         * enclosed section, and after resolving these the enclosed section must be self-contained
         */
        final int maxEnclosedPartIndex = parts.size() - 2;
        // check all intersected detail category assignments for unresolvable conflicts
        for (int leadPartIndex = 0; leadPartIndex < maxEnclosedPartIndex; leadPartIndex += 2) {
            final List<TextToken> leadingSelection = parts.get(leadPartIndex);
            final List<TextToken> enclosedUnselectedSection = parts.get(leadPartIndex + 1);
            final List<TextToken> trailingSelection = parts.get(leadPartIndex + 2);
            // throw an Exception if this would result in an invalid model state
            this.checkValidityOfIntersectedCategoryAssignments(leadingSelection, enclosedUnselectedSection, trailingSelection);
        }
        // now, that the previous run did not yield an error, actually resolve those intersected detail category assignments
        this.resolveIntersectedCategoryAssignments(parts);
        // the currently assigned categories should be replaced
        tokens.forEach(selectedToken -> selectedToken.setDetail(category).setFirstTokenOfDetail(false).setLastTokenOfDetail(false));
        // set start/end markers for the whole selection
        if (category != null) {
            // set start marker on first token in first selected part (i.e. token range)
            parts.get(0).get(0).setFirstTokenOfDetail(true);
            // set end marker on last token in last selected part (i.e. token range)
            final List<TextToken> lastSelectedPart = parts.get(parts.size() - 1);
            lastSelectedPart.get(lastSelectedPart.size() - 1).setLastTokenOfDetail(true);
        }
        // set start/end markers where appropriate; skip each second part, as it contains the unselected tokens
        for (int selectedPartIndex = 0; selectedPartIndex < parts.size(); selectedPartIndex += 2) {
            final List<TextToken> selectedPart = parts.get(selectedPartIndex);
            final TextToken firstTarget = selectedPart.get(0);
            final TextToken lastBeforeSelection = firstTarget.getPreviousToken();
            if (lastBeforeSelection != null && lastBeforeSelection.getDetail() == null) {
                // previous token has no assigned category
                lastBeforeSelection.setLastTokenOfDetail(category != null);
                if (category == null) {
                    // no separation between tokens without assigned category
                    firstTarget.setFirstTokenOfDetail(false);
                }
            } else if (category == null) {
                // separate firstTarget from previous non-null token
                firstTarget.setFirstTokenOfDetail(true);
            }
            final TextToken lastTarget = selectedPart.get(selectedPart.size() - 1);
            final TextToken firstAfterSelection = lastTarget.getFollowingToken();
            if (firstAfterSelection != null && firstAfterSelection.getDetail() == null) {
                // following token has no assigned category
                firstAfterSelection.setFirstTokenOfDetail(category != null);
                if (category == null) {
                    // no separation between tokens without assigned category
                    lastTarget.setLastTokenOfDetail(false);
                }
            } else if (category == null) {
                // separate lastTarget from following non-null token
                lastTarget.setLastTokenOfDetail(true);
            }
        }
    }

    /**
     * Resolve any detail category assignments from the odd numbered token list parts (i.e. first, third, fifth, ...) in order to assign another (new)
     * detail category to same
     *
     * @param parts
     *            the text token range parts alternating selected (i.e. going to be changed) and unselected (i.e. should maintain their current
     *            assigned detail categories)
     * @see #assignDetailCategoryToInterruptedTokenRange(List, DetailCategory)
     */
    private void resolveIntersectedCategoryAssignments(final List<List<TextToken>> parts) {
        final int maxEnclosedPartIndex = parts.size() - 2;
        for (int leadPartIndex = 0; leadPartIndex < maxEnclosedPartIndex; leadPartIndex += 2) {
            // resolve conflicts in first selected section
            final List<TextToken> leadingSelection = parts.get(leadPartIndex);
            for (final Entry<DetailCategory, CategoryConflictHandler> conflict : this.collectIntersectedCategories(leadingSelection).entrySet()) {
                final int startsToMoveCount = conflict.getValue().getNumberOfStartedButNotEndedConflicts();
                if (startsToMoveCount > 0) {
                    // more sections of this category are being opened than closed in the selected tokens
                    final TextToken firstEnclosedToken = parts.get(leadPartIndex + 1).get(0);
                    this.moveCategorySectionStartToTheRight(firstEnclosedToken, conflict.getKey(), startsToMoveCount);
                }
                final int endsToMoveCount = conflict.getValue().getNumberOfEndedButNotStartedConflicts();
                if (endsToMoveCount > 0 && leadPartIndex == 0) {
                    // less sections of this category are being opened than closed in the selected tokens (only apply for first selection part)
                    final TextToken lastTokenBeforeParts = leadingSelection.get(0).getPreviousToken();
                    this.moveCategorySectionEndToTheLeft(lastTokenBeforeParts, conflict.getKey(), endsToMoveCount);
                }
            }
            // resolve conflicts in last selected section
            final List<TextToken> trailingSelection = parts.get(leadPartIndex + 2);
            for (final Entry<DetailCategory, CategoryConflictHandler> conflict : this.collectIntersectedCategories(trailingSelection).entrySet()) {
                final int startsToMoveCount = conflict.getValue().getNumberOfStartedButNotEndedConflicts();
                if (startsToMoveCount > 0 && leadPartIndex + 1 == maxEnclosedPartIndex) {
                    // more sections of this category are being opened than closed in the selected tokens (only apply for last selection part)
                    final TextToken firstTokenAfterParts = trailingSelection.get(trailingSelection.size() - 1).getFollowingToken();
                    this.moveCategorySectionStartToTheRight(firstTokenAfterParts, conflict.getKey(), startsToMoveCount);
                }
                final int endsToMoveCount = conflict.getValue().getNumberOfEndedButNotStartedConflicts();
                if (endsToMoveCount > 0) {
                    // less sections of this category are being opened than closed in the selected tokens: move section ends to the left
                    final TextToken lastEnclosedToken = trailingSelection.get(0).getPreviousToken();
                    this.moveCategorySectionEndToTheLeft(lastEnclosedToken, conflict.getKey(), endsToMoveCount);
                }
            }
        }
    }

    /**
     * Assemble a list of consecutive tokens from the given list of selected one, producing a result list of alternating selected and enclosed,
     * unselected tokens. The result is always an odd number of parts (one, three, five, ...).
     *
     * @param selectedTokens
     *            the tokens to be deemed as selected, and to determine any enclosed tokens from
     * @return list of consecutive tokens with the the selected ones in every odd part (first, third, fifth, ...) and the enclosed tokens in every
     *         even part (second, fourth, ...)
     */
    private List<List<TextToken>> collectInterruptedSelectionParts(final List<TextToken> selectedTokens) {
        final List<List<TextToken>> parts = new LinkedList<>();
        // determine the first token out of the targeted range to end the following loop on
        final TextToken firstTokenAfterSelection = selectedTokens.get(selectedTokens.size() - 1).getFollowingToken();
        List<TextToken> currentPart = new LinkedList<>();
        // alternate between selected and unselected sections (i.e. token ranges)
        boolean currentPartSelected = true;
        TextToken currentToken = selectedTokens.get(0);
        do {
            if (currentPartSelected != CollectionUtil.containsInstance(selectedTokens, currentToken)) {
                // switch between selected/unselected section
                currentPartSelected = !currentPartSelected;
                // add current part to result list
                parts.add(currentPart);
                // create a new part list
                currentPart = new LinkedList<>();
            }
            currentPart.add(currentToken);
            currentToken = currentToken.getFollowingToken();
        } while (currentToken != firstTokenAfterSelection);
        // don't forget the last part list
        parts.add(currentPart);
        return parts;
    }

    /**
     * Check if the given two selection parts (each with 1..n tokens) with enclosed unselected tokens (1..n tokens) is valid to be part of a category
     * assignment. The given tokens are assumed to be in the correct order (leadingSelection, enclosedUnselectedSection, trailingSection).
     *
     * @param leadingSelection
     *            the selected tokens – supposedly receiving a new detail category assignment – in front of the enclosed tokens
     * @param enclosedUnselectedSection
     *            the unselected tokens – that should maintain their assigned detail categories – enclosed by selected tokens
     * @param trailingSelection
     *            the selected tokens – supposedly receiving a new detail category assignment – following the enclosed tokens
     * @throws HmxException
     *             assigning a detail category to the given interrupted token range would result in an invalid model state (i.e. alternate assignments
     *             like ABAB), which cannot be represented by the assumed hierarchical structure
     */
    private void checkValidityOfIntersectedCategoryAssignments(final List<TextToken> leadingSelection,
            final List<TextToken> enclosedUnselectedSection, final List<TextToken> trailingSelection) throws HmxException {
        // collect category assignments that need to be resolved from the leading and trailing selection
        final Map<DetailCategory, CategoryConflictHandler> leadingConflicts = this.collectIntersectedCategories(leadingSelection);
        final Map<DetailCategory, CategoryConflictHandler> trailingConflicts = this.collectIntersectedCategories(trailingSelection);
        // collect open category assignments from the enclosed unselected tokens
        final Map<DetailCategory, CategoryConflictHandler> enclosedConflicts = this.collectIntersectedCategories(enclosedUnselectedSection);
        for (final Entry<DetailCategory, CategoryConflictHandler> singleConflict : enclosedConflicts.entrySet()) {
            final DetailCategory conflictingDetail = singleConflict.getKey();
            final int startsToMoveCount = singleConflict.getValue().getNumberOfStartedButNotEndedConflicts();
            final int endsToMoveCount = singleConflict.getValue().getNumberOfEndedButNotStartedConflicts();
            // detail category is not starting but ending in the enclosed part
            final boolean requiredDetailStartNotMet = endsToMoveCount > 0
                    && (!leadingConflicts.containsKey(conflictingDetail) || endsToMoveCount < leadingConflicts.get(conflictingDetail)
                    .getNumberOfStartedButNotEndedConflicts());
            // detail category is starting but not ending in the enclosed part
            final boolean requiredDetailEndNotMet = startsToMoveCount > 0
                    && (!trailingConflicts.containsKey(conflictingDetail) || startsToMoveCount < trailingConflicts.get(conflictingDetail)
                    .getNumberOfEndedButNotStartedConflicts());
            if (requiredDetailStartNotMet || requiredDetailEndNotMet) {
                // the intersected detail category cannot be resolved
                throw new HmxException(AisMessage.ERROR_AIS_SELECTION_INVALID);
            }
        }
        // also regard category assignments, with neither start nor end in the enclosed section
        final DetailCategory enclosedOrphan = this.collectPossibleOrphanCategory(enclosedUnselectedSection);
        if (enclosedOrphan != null
                && (!leadingConflicts.containsKey(enclosedOrphan) || !trailingConflicts.containsKey(enclosedOrphan)
                || leadingConflicts.get(enclosedOrphan).getNumberOfStartedButNotEndedConflicts() < 1 || trailingConflicts.get(
                enclosedOrphan).getNumberOfEndedButNotStartedConflicts() < 1)) {
            // the enclosed detail category could not be resolved
            throw new HmxException(AisMessage.ERROR_AIS_SELECTION_INVALID);
        }
    }

    /**
     * Collect the intersected detail category assignments, that are not fully contained in the given token range. Each category contained in the
     * resulting map either starts in the given range and ends outside of (i.e. after) the given range (+1 for each opened detail without a close), or
     * starts outside of (i.e. before) the given range and ends in the given range (-1 for each closed detail without a start).
     *
     * @param tokenRange
     *            range of tokens to check for not-self-contained detail categories
     * @return not-self-contained detail categories: {@code +1} for each opened detail without a close, and {@code -1} for each closed detail without
     *         a start
     */
    private Map<DetailCategory, CategoryConflictHandler> collectIntersectedCategories(final List<TextToken> tokenRange) {
        final Map<DetailCategory, CategoryConflictHandler> affectedSections = new HashMap<>();
        for (final TextToken selectedToken : tokenRange) {
            // ignore null categories and single tokens that are start and stop in themselves
            if (selectedToken.getDetail() == null || selectedToken.isFirstTokenOfDetail() == selectedToken.isLastTokenOfDetail()) {
                continue;
            }
            if (!affectedSections.containsKey(selectedToken.getDetail())) {
                affectedSections.put(selectedToken.getDetail(), new CategoryConflictHandler());
            }
            affectedSections.get(selectedToken.getDetail()).addCategoryChange(selectedToken.isFirstTokenOfDetail());
        }
        // discard fully contained category assignments
        final Iterator<CategoryConflictHandler> resultCountIterator = affectedSections.values().iterator();
        while (resultCountIterator.hasNext()) {
            if (!resultCountIterator.next().hasUnresolvedConflicts()) {
                resultCountIterator.remove();
            }
        }
        return affectedSections;
    }

    /**
     * Collect the intersected detail category assignments, that are not fully contained in the given token range. Each category contained in the
     * resulting map either starts in the given range and ends outside of (i.e. after) the given range (+1 for each opened detail without a close), or
     * starts outside of (i.e. before) the given range and ends in the given range (-1 for each closed detail without a start).
     *
     * @param tokenRange
     *            range of tokens to check for not-self-contained detail categories
     * @return not-self-contained detail category that has neither start nor end in the given range
     * @throws HmxException
     *             the token range contains a detail category that neither starts or ends there (and throwExceptionOnOrphanedToken was set to true)
     */
    private DetailCategory collectPossibleOrphanCategory(final List<TextToken> tokenRange) throws HmxException {
        final Map<DetailCategory, AtomicInteger> affectedSections = new HashMap<>();
        final Set<DetailCategory> possibleOrphans = new HashSet<>();
        for (final TextToken selectedToken : tokenRange) {
            if (selectedToken.getDetail() != null && !selectedToken.isFirstTokenOfDetail() && !selectedToken.isLastTokenOfDetail()
                    && (!affectedSections.containsKey(selectedToken.getDetail()) || affectedSections.get(selectedToken.getDetail()).get() <= 0)) {
                possibleOrphans.add(selectedToken.getDetail());
            }
            // ignore null categories and single tokens that are start and stop in themselves
            if (selectedToken.getDetail() != null && selectedToken.isFirstTokenOfDetail() != selectedToken.isLastTokenOfDetail()) {
                if (!affectedSections.containsKey(selectedToken.getDetail())) {
                    affectedSections.put(selectedToken.getDetail(), new AtomicInteger(0));
                }
                if (selectedToken.isFirstTokenOfDetail()) {
                    // +1 for each opened detail
                    affectedSections.get(selectedToken.getDetail()).incrementAndGet();
                } else if (selectedToken.isLastTokenOfDetail()) {
                    // -1 for each closed detail
                    affectedSections.get(selectedToken.getDetail()).decrementAndGet();
                    possibleOrphans.remove(selectedToken.getDetail());
                }
            }
        }
        if (possibleOrphans.isEmpty()) {
            return null;
        }
        // there can be at most one possible orphan in a given token range
        return possibleOrphans.iterator().next();
    }

    /**
     * Move the start of a detail category section (of multiple tokens) to the right, in order to remove the leading token(s) from the section, while
     * maintaining the overall model's validity.
     *
     * @param firstPossibleTarget
     *            token directly behind the section part being removed
     * @param sectionCategory
     *            the detail category of the section to fix
     * @param steps
     *            the number of sections with the same detail category to fix in this method (must be a positive number {@code >0} )
     */
    private void moveCategorySectionStartToTheRight(final TextToken firstPossibleTarget, final DetailCategory sectionCategory, final int steps) {
        int stepsRemaining = steps;
        TextToken currentToken = firstPossibleTarget;
        DetailCategory previousCategory = null;
        do {
            // move section starts to the right
            if (sectionCategory.equals(currentToken.getDetail()) && !currentToken.isFirstTokenOfDetail()
                    && !ComparisonUtil.isNullAwareEqual(previousCategory, currentToken.getDetail())) {
                currentToken.setFirstTokenOfDetail(true);
                stepsRemaining--;
            }
            // continue with the next to the right
            previousCategory = currentToken.getDetail();
            // this should never be null - otherwise the structure we started from was already invalid
            currentToken = currentToken.getFollowingToken();
        } while (stepsRemaining > 0);
    }

    /**
     * Move the end of a detail category section (of multiple tokens) to the left, in order to remove the trailing token(s) from the section, while
     * maintaining the overall model's validity.
     *
     * @param firstPossibleTarget
     *            token directly in front of the section part being removed
     * @param sectionCategory
     *            the detail category of the section to fix
     * @param steps
     *            the number of sections with the same detail category to fix in this method (must be a positive number {@code >0} )
     */
    private void moveCategorySectionEndToTheLeft(final TextToken firstPossibleTarget, final DetailCategory sectionCategory, final int steps) {
        int stepsRemaining = steps;
        TextToken currentToken = firstPossibleTarget;
        DetailCategory followingCategory = null;
        do {
            // move section ends to the left
            if (sectionCategory.equals(currentToken.getDetail()) && !currentToken.isLastTokenOfDetail()
                    && !ComparisonUtil.isNullAwareEqual(followingCategory, currentToken.getDetail())) {
                currentToken.setLastTokenOfDetail(true);
                stepsRemaining--;
            }
            // continue with the next to the left
            followingCategory = currentToken.getDetail();
            // this should never be null - otherwise the structure we started from was already invalid
            currentToken = currentToken.getPreviousToken();
        } while (stepsRemaining > 0);
    }

    @Override
    public void setParticipantId(final Interview interview, final String newParticipantId) {
        final Map<String, List<Interview>> groupedInterviews = this.getModel().getSubModelObjects();
        // get all interviews for the interview's current participant
        final List<Interview> oldInterviews = groupedInterviews.get(interview.getParticipantId());
        if (oldInterviews.size() > interview.getIndex()) {
            // reduce index of all following interviews by one
            for (final Interview followingInterview : oldInterviews.subList(interview.getIndex(), oldInterviews.size())) {
                followingInterview.setIndex(followingInterview.getIndex() - 1);
            }
        }
        // apply new participant id and determine the new index
        this.setInterviewsParticipantIdAndIndex(groupedInterviews.get(newParticipantId), Arrays.asList(interview), newParticipantId);
        // trigger model change event for the whole model
        this.notifyListeners(this.getModel(), false);
    }

    @Override
    public void renameParticipant(final String oldParticipantId, final String newParticipantId) {
        final Map<String, List<Interview>> groupedInterviews = this.getModel().getSubModelObjects();
        // apply new participant id and determine the new indices
        this.setInterviewsParticipantIdAndIndex(groupedInterviews.get(newParticipantId), groupedInterviews.get(oldParticipantId), newParticipantId);
        // trigger model change event for the whole model
        this.notifyListeners(this.getModel(), false);
    }

    /**
     * Assign the {@code interviewsToAdd} to the given participant id, while ensuring that the {@code index} attribute of the added interviews is
     * consistent with already assigned interviews.
     *
     * @param previousInterviews
     *            the interviews already assigned to the given participant id
     * @param interviewsToAdd
     *            the interviews to be added/assigned to the given participant id
     * @param participantId
     *            the participant id to assign the interviews to
     */
    private void setInterviewsParticipantIdAndIndex(final List<Interview> previousInterviews, final List<Interview> interviewsToAdd,
            final String participantId) {
        int index = 1;
        if (previousInterviews != null) {
            index += previousInterviews.size();
        }
        for (final Interview singleAddition : interviewsToAdd) {
            singleAddition.setParticipantId(participantId);
            singleAddition.setIndex(index);
            index++;
        }
    }

    @Override
    public void setIndex(final Interview interview, final int newIndex) {
        final int oldIndex = interview.getIndex();
        final int offset;
        if (newIndex < oldIndex) {
            // interview is moved up - indices of interviews in between are increased by one
            offset = 1;
        } else {
            // interview is moved down - indices of interviews in between are decreased by one
            offset = -1;
        }
        final int affectedRangeStart = Math.min(newIndex, oldIndex) - 1;
        final int affectedRangeEnd = Math.max(newIndex, oldIndex);
        final List<Interview> participantsInterviews = this.getModel().getSubModelObjects().get(interview.getParticipantId());
        participantsInterviews.subList(affectedRangeStart, affectedRangeEnd)
                .forEach(affectedInterview -> affectedInterview.setIndex(affectedInterview.getIndex() + offset));
        interview.setIndex(newIndex);
        // trigger model change event for the whole model
        this.notifyListeners(this.getModel(), false);
    }

    @Override
    public void reset(final Interview interview, final Interview resetState) {
        interview.reset(resetState);
        // trigger model change event for the modified interview
        this.notifyListeners(interview, false);
    }

    @Override
    public Map<Interview, Map<DetailCategory, AtomicLong>> countDetailOccurrences(final List<Interview> interviews) {
        final Map<Interview, Map<DetailCategory, AtomicLong>> result;
        result = new LinkedHashMap<>(interviews.size());
        final List<DetailCategory> categories = this.getModel().provide();
        for (final Interview singleInterview : interviews) {
            final Map<DetailCategory, AtomicLong> occurences = new LinkedHashMap<>();
            // initialize counter for each category
            for (final DetailCategory singleCategory : categories) {
                occurences.put(singleCategory, new AtomicLong(0));
            }
            // iterate through interview's whole detail category sequence
            for (final DetailCategory singleDetail : this.extractDetailSequence(singleInterview)) {
                DetailCategory tokenCategory = singleDetail;
                do {
                    occurences.get(tokenCategory).incrementAndGet();
                    // count for each parent category as well
                    tokenCategory = tokenCategory.getParent();
                } while (tokenCategory != null);
            }
            result.put(singleInterview, occurences);
        }
        return result;
    }

    @Override
    public Map<Interview, AtomicLong> countTokensWithAssignedDetail(final List<Interview> interviews) {
        final Map<Interview, AtomicLong> result = new LinkedHashMap<>(interviews.size());
        for (final Interview singleInterview : interviews) {
            final AtomicLong counter = new AtomicLong();
            for (final TextToken singleParagraph : singleInterview.getText()) {
                TextToken currentToken = singleParagraph;
                do {
                    if (currentToken.getDetail() != null) {
                        counter.incrementAndGet();
                    }
                    currentToken = currentToken.getFollowingToken();
                } while (currentToken != null);
            }
            result.put(singleInterview, counter);
        }
        return result;
    }

    @Override
    public Map<Interview, Map<List<DetailCategory>, AtomicLong>> extractDetailPattern(final List<Interview> interviews, final int minLength,
            final int maxLength) {
        final Map<Interview, Map<List<DetailCategory>, AtomicLong>> result = new LinkedHashMap<>();
        // iterate over all given interviews
        for (final Interview singleInterview : interviews) {
            final Map<List<DetailCategory>, AtomicLong> patternOccurences = new HashMap<>();
            final List<List<DetailCategory>> currentPattern = new LinkedList<>();
            // iterate over the whole detail category sequence
            for (final DetailCategory singleDetail : this.extractDetailSequence(singleInterview)) {
                currentPattern.add(new ArrayList<>(maxLength));
                final Iterator<List<DetailCategory>> patternIterator = currentPattern.iterator();
                do {
                    final List<DetailCategory> singlePattern = patternIterator.next();
                    // append the current detail category from the sequence to each pattern
                    singlePattern.add(singleDetail);
                    final int patternSize = singlePattern.size();
                    if (patternSize > maxLength) {
                        // drop too long pattern
                        patternIterator.remove();
                    } else if (minLength <= patternSize) {
                        // ensure the existence of a counter for the correct sized pattern
                        AtomicLong currentValue = patternOccurences.get(singlePattern);
                        if (currentValue == null) {
                            currentValue = new AtomicLong(0);
                            patternOccurences.put(new ArrayList<>(singlePattern), currentValue);
                        }
                        // increase counter for the correct sized pattern by one
                        currentValue.incrementAndGet();
                    }
                } while (patternIterator.hasNext());
            }
            result.put(singleInterview, patternOccurences);
        }
        return result;
    }

    @Override
    public List<DetailCategory> extractDetailSequence(final Interview interview) {
        final List<DetailCategory> sequence = new LinkedList<>();
        // iterate over all paragraphs
        for (final TextToken singleParagraph : interview.getText()) {
            // iterate over all tokens of the current paragraph
            TextToken currentToken = singleParagraph;
            do {
                if (currentToken.isFirstTokenOfDetail() && currentToken.getDetail() != null) {
                    // add assigned detail category to the sequence, for the assigned token range starting here
                    sequence.add(currentToken.getDetail());
                }
                currentToken = currentToken.getFollowingToken();
            } while (currentToken != null);
        }
        return sequence;
    }

    @Override
    public String validateEquality(final AisProject otherProject) {
        if (!this.getModel().provide().equals(otherProject.provide())) {
            return "Detail Categories do not match";
        }
        if (this.getModel().getInterviews().size() != otherProject.getInterviews().size()) {
            return new StringBuilder("Number of Interviews does not match:    ").append(this.getModel().getInterviews().size()).append(" != ")
                    .append(otherProject.getInterviews().size()).toString();
        }
        // avoid differing sort order failing this check
        final List<Interview> oneInterviews = new ArrayList<>(this.getModel().getInterviews());
        final List<Interview> otherInterviews = new ArrayList<>(otherProject.getInterviews());
        Collections.sort(oneInterviews);
        Collections.sort(otherInterviews);
        // compare each interview with its counter part at the same list position
        final Iterator<Interview> oneInterviewIterator = oneInterviews.iterator();
        final Iterator<Interview> otherInterviewIterator = otherInterviews.iterator();
        while (oneInterviewIterator.hasNext()) {
            final Interview oneInterview = oneInterviewIterator.next();
            final String error = this.validateEquality(oneInterview, otherInterviewIterator.next());
            if (error != null) {
                return new StringBuilder(oneInterview.toString()).append('\n').append(error).toString();
            }
        }
        return null;
    }

    /**
     * Check if the given interviews are equal.
     *
     * @param oneInterview
     *            the interview to check against
     * @param otherInterview
     *            other interview to compare with
     * @return message describing an occurred difference, or {@code null} if both interviews are equal
     */
    String validateEquality(final Interview oneInterview, final Interview otherInterview) {
        if (oneInterview.getIndex() != otherInterview.getIndex() || !oneInterview.getParticipantId().equals(otherInterview.getParticipantId())) {
            return new StringBuilder("Participant/Index does not match:    ").append(otherInterview.getParticipantId()).append(" (")
                    .append(otherInterview.getIndex()).append(')').toString();
        }
        if (oneInterview.getText().size() != otherInterview.getText().size()) {
            return new StringBuilder("Number of paragraphs differ    ").append(oneInterview.getText().size()).append(" != ")
                    .append(otherInterview.getText().size()).toString();
        }
        // compare each paragraph with its counter part at the same list position
        final Iterator<TextToken> oneParagraphIterator = oneInterview.getText().iterator();
        final Iterator<TextToken> otherParagraphIterator = otherInterview.getText().iterator();
        while (oneParagraphIterator.hasNext()) {
            final String error = this.validateEquality(oneParagraphIterator.next(), otherParagraphIterator.next());
            if (error != null) {
                return error;
            }
        }
        return null;
    }

    /**
     * Check if the given paragraphs are equal.
     *
     * @param oneParagraphStart
     *            the paragraph (represented by its first token) to check against
     * @param otherParagraphStart
     *            other paragraph (represented by its first token) to compare with
     * @return message describing an occurred difference, or {@code null} if both paragraphs are equal
     */
    String validateEquality(final TextToken oneParagraphStart, final TextToken otherParagraphStart) {
        TextToken oneToken = oneParagraphStart;
        TextToken otherToken = otherParagraphStart;
        do {
            if (!oneToken.getText().equals(otherToken.getText())) {
                return new StringBuilder("A Token's text does not match:    '").append(oneToken.getText()).append("' != '")
                        .append(otherToken.getText()).append('\'').toString();
            }
            if (oneToken.isFirstTokenOfDetail() != otherToken.isFirstTokenOfDetail()
                    || oneToken.isLastTokenOfDetail() != otherToken.isLastTokenOfDetail()
                    || oneToken.getDetail() != otherToken.getDetail()
                    && (oneToken.getDetail() == null || otherToken.getDetail() == null || !oneToken.getDetail().getCode()
                    .equals(otherToken.getDetail().getCode()))) {
                final StringBuilder error = new StringBuilder("A Token's Detail Category assignment does not match: '");
                error.append(oneToken.getText()).append("'    ");
                this.appendCategoryStringForToken(error, oneToken);
                error.append(" != ");
                this.appendCategoryStringForToken(error, otherToken);
                return error.toString();
            }
            oneToken = oneToken.getFollowingToken();
            otherToken = otherToken.getFollowingToken();
        } while (oneToken != null && otherToken != null);
        if (oneToken == null && otherToken == null) {
            return null;
        }
        return "A Paragraph's Token count does not match";
    }

    /**
     * Append a textual representation of the given token's detail category assignment to the specified string builder.
     *
     * @param builder
     *            the string builder to append the category assignment text to
     * @param token
     *            the token whose detail category assignment should be appended
     * @see #validateEquality(Interview, Interview)
     */
    private void appendCategoryStringForToken(final StringBuilder builder, final TextToken token) {
        if (token.isFirstTokenOfDetail()) {
            builder.append('|');
        }
        if (token.getDetail() == null) {
            builder.append(" --- ");
        } else {
            builder.append(' ').append(token.getDetail().getCode()).append(' ');
        }
        if (token.isLastTokenOfDetail()) {
            builder.append('|');
        }
    }

    /** Conflict handler class for collecting detail category sections that are being started but not ended and/or ended but not started. */
    private static class CategoryConflictHandler {

        /** Registered unresolved detail category changes, i.e. started but not ended and/or ended but not started. */
        final LinkedList<Boolean> categoryChanges = new LinkedList<>();

        /**
         * Add another start or end of a detail category to this conflict handler.
         *
         * @param sectionStarted
         *            whether the respective detail category section is starting
         */
        void addCategoryChange(final boolean sectionStarted) {
            if (this.categoryChanges.isEmpty() || !this.categoryChanges.getLast() || sectionStarted) {
                this.categoryChanges.add(sectionStarted);
            } else {
                // previous change was a section start, the current change ends that section again
                this.categoryChanges.removeLast();
            }
        }

        /**
         * Check whether this handler instance contains unresolved conflicts.
         *
         * @return if at least one section was started but not ended or ended but not started
         */
        boolean hasUnresolvedConflicts() {
            return !this.categoryChanges.isEmpty();
        }

        /**
         * Return the number of conflicting sections that are ending but have not been started before.
         *
         * @return number of conflicting section ends
         */
        int getNumberOfEndedButNotStartedConflicts() {
            return (int) this.categoryChanges.stream().filter(singleChange -> !singleChange).count();
        }

        /**
         * Return the number of conflicting sections that are starting but not ending afterwards.
         *
         * @return number of conflicting section starts
         */
        int getNumberOfStartedButNotEndedConflicts() {
            return (int) this.categoryChanges.stream().filter(singleChange -> singleChange).count();
        }
    }
}
