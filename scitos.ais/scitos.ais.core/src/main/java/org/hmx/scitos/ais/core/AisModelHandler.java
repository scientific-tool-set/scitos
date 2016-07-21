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

package org.hmx.scitos.ais.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.hmx.scitos.ais.domain.IDetailCategoryProvider;
import org.hmx.scitos.ais.domain.model.AisProject;
import org.hmx.scitos.ais.domain.model.DetailCategory;
import org.hmx.scitos.ais.domain.model.Interview;
import org.hmx.scitos.ais.domain.model.TextToken;
import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.core.IModelHandler;

/**
 * Generic interface of the single point of contact for model changes in the AIS module.
 */
public interface AisModelHandler extends IModelHandler<AisProject> {

    /**
     * Create a blank interview for the participant with the given id. Ensure the interview index stays unique for the participant id.
     *
     * @param participantId
     *            id of the participant associated with the interview to create
     * @return created interview instance
     */
    Interview createInterview(String participantId);

    /**
     * Replace the current text tokens in the specified interview object with the tokens extracted from the given text.
     *
     * @param target
     *            the interview object receiving the parsed tokens
     * @param text
     *            the raw text to extract the new tokens from
     */
    void setInterviewText(Interview target, String text);

    /**
     * Delete the given interview from the handled project.
     *
     * @param interview
     *            the interview to delete
     */
    void deleteInterview(Interview interview);

    /**
     * Assign the specified category to the given tokens.
     *
     * @param interview
     *            the interview containing the targeted tokens
     * @param tokens
     *            all token (in the correct order) that are receiving the given detail category
     * @param category
     *            the detail category to assign to the given tokens
     * @throws HmxException
     *             detail category could not be assigned, due to an invalid selection of tokens
     */
    void assignDetailCategory(Interview interview, List<TextToken> tokens, DetailCategory category) throws HmxException;

    /**
     * Reset the applicable detail category model to the given new model. The specified mapping is used to preserve previously assigned detail
     * categories by replacing old categories with new ones. If no mapping for an old category is specified, the category assignment is discarded.
     *
     * @param newModel
     *            provider of the new detail category model to set
     * @param mappedOldToNew
     *            mapping of old categories to new categories, in order to preserve already existing assignments
     */
    void replaceCategoryModel(IDetailCategoryProvider newModel, Map<DetailCategory, DetailCategory> mappedOldToNew);

    /**
     * Set the specified interview's assigned participant id.
     *
     * @param interview
     *            interview to assign to other participant
     * @param newParticipantId
     *            new participant ID to assign
     */
    void setParticipantId(Interview interview, String newParticipantId);

    /**
     * Replaces the specified participant id with the given new one.
     *
     * @param oldParticipantId
     *            current ID of the participant to rename
     * @param newParticipantId
     *            new ID to assign
     */
    void renameParticipant(String oldParticipantId, String newParticipantId);

    /**
     * Set the specified interview's assigned index and change other indices accordingly.
     *
     * @param interview
     *            interview to set another index for
     * @param newIndex
     *            new index to assign
     */
    void setIndex(Interview interview, int newIndex);

    /**
     * Reset the specified interview to the given state – i.e. replace its contained data/state while preserving the actual reference.
     *
     * @param interview
     *            the instance to reset
     * @param resetState
     *            the interview state to reset to
     */
    void reset(Interview interview, Interview resetState);

    /**
     * Collect the number of occurrences each detail category was assigned per interview. The result includes parent detail categories, that are not
     * selectable by the user. Their respective count is the sum of the occurrences of the selectable child categories.
     *
     * @param interviews
     *            list of interviews to count the assigned detail categories for
     * @return for each given interview: detail category and number of assignments
     */
    Map<Interview, Map<DetailCategory, AtomicLong>> countDetailOccurrences(List<Interview> interviews);

    /**
     * Collect the number of tokens (words) per interview, that are part of detail category assignments.
     *
     * @param interviews
     *            list of interviews to count the tokens with assigned detail categories for
     * @return for each given interview: number of tokens with assigned detail category
     */
    Map<Interview, AtomicLong> countTokensWithAssignedDetail(List<Interview> interviews);

    /**
     * Collect the number of occurrences of detail category patterns per interview.
     *
     * @param interviews
     *            list of interviews to count the contained detail category patterns for
     * @param minLength
     *            minimum number (inclusive) of consecutive detail categories to be included as pattern
     * @param maxLength
     *            maximum number (inclusive) of consecutive detail categories to be included as pattern
     * @return for each given interview: detail category pattern and number of occurrences
     * @see #extractDetailSequence(Interview)
     */
    Map<Interview, Map<List<DetailCategory>, AtomicLong>> extractDetailPattern(List<Interview> interviews, int minLength, int maxLength);

    /**
     * Collect the full sequence of assigned detail categories in the given interview. The order is determined by the first token of the detail
     * category. Causing enclosed categories to be treated equally as following categories.
     *
     * @param interview
     *            the interview to collect the detail category sequence in
     * @return all assigned detail categories in the present order
     */
    List<DetailCategory> extractDetailSequence(Interview interview);

    /**
     * Check if the handled project is equal to the given one.
     *
     * @param otherProject
     *            other project to compare with
     * @return message describing an occurred difference, or {@code null} if both projects are equal
     */
    String validateEquality(AisProject otherProject);
}
