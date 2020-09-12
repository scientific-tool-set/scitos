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

package org.hmx.scitos.ais.domain.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.hmx.scitos.domain.IModel;

/**
 * Representation of a single autobiographical interview and its scoring.
 */
public final class Interview implements IModel<Interview>, Comparable<Interview> {

    /** The id of the associated participant. */
    private String participantId;
    /** The index (i.e. number) of this interview in the project containing it. */
    private int index;
    /** The actual interview including assigned details (i.e. applied scoring). */
    private final List<TextToken> text = new LinkedList<>();

    /**
     * Main constructor.
     *
     * @param participantId
     *            the participant's id this interview is associated with
     * @param index
     *            the index (i.e. number) of this interview in the project containing it
     */
    public Interview(final String participantId, final int index) {
        this.setParticipantId(participantId);
        this.setIndex(index);
    }

    /**
     * Getter for the participant's id this interview is associated with.
     *
     * @return id of the associated participant
     */
    public String getParticipantId() {
        return this.participantId;
    }

    /**
     * Setter for the participant's id this interview is associated with.
     *
     * @param value
     *            id of the associated participant
     * @return self reference
     */
    public Interview setParticipantId(final String value) {
        this.participantId = value;
        return this;
    }

    /**
     * Getter for the index (i.e. number) of this interview in the project containing it.
     *
     * @return index of this interview
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * Setter for the index (i.e. number) of this interview in the project containing it.
     *
     * @param value
     *            index of this interview
     * @return self reference
     */
    public Interview setIndex(final int value) {
        this.index = value;
        return this;
    }

    /**
     * Getter for the actual interview firstTextToken, including the assigned details (i.e. applied scoring).
     *
     * @return the (scored) interview firstTextToken
     */
    public List<TextToken> getText() {
        return Collections.unmodifiableList(this.text);
    }

    /**
     * Setter for the actual interview text.
     *
     * @param text
     *            the interview text to set
     * @return self reference
     */
    public Interview setText(final List<TextToken> text) {
        this.text.clear();
        if (text != null) {
            this.text.addAll(text);
        }
        return this;
    }

    /**
     * Preserve the current instance, but replace the contained state and values by the ones in the given other interview instance.
     *
     * @param replacingState
     *            other interview instance to replace this' state with
     * @return self reference
     */
    public Interview reset(final Interview replacingState) {
        this.setParticipantId(replacingState.getParticipantId());
        this.setIndex(replacingState.getIndex());
        final List<TextToken> copiedParagraphs = new LinkedList<>();
        for (final TextToken singleParagraph : replacingState.getText()) {
            copiedParagraphs.add(singleParagraph.clone());
        }
        this.setText(copiedParagraphs);
        return this;
    }

    @Override
    public int compareTo(final Interview otherInterview) {
        int result = this.getParticipantId().compareTo(otherInterview.getParticipantId());
        if (result == 0) {
            result = this.getIndex() - otherInterview.getIndex();
        }
        return result;
    }

    @Override
    public int hashCode() {
        return this.getParticipantId().hashCode() + 13 * this.getIndex();
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof Interview)) {
            return false;
        }
        final Interview otherInterview = (Interview) otherObject;
        if (this.index != otherInterview.index || !this.participantId.equals(otherInterview.participantId)
                || this.text.size() != otherInterview.text.size()) {
            return false;
        }
        final Iterator<TextToken> otherParagraphIterator = otherInterview.text.iterator();
        for (final TextToken oneParagraphStart : this.text) {
            if (!this.areParagraphsEqual(oneParagraphStart, otherParagraphIterator.next())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check the equality of the given paragraphs, represented by their respective first tokens.
     *
     * @param oneParagraphStart
     *            the paragraph to check against
     * @param otherParagraphStart
     *            the other paragraph to compare with
     * @return if both paragraphs are equal
     */
    private boolean areParagraphsEqual(final TextToken oneParagraphStart, final TextToken otherParagraphStart) {
        TextToken oneToken = oneParagraphStart;
        TextToken otherToken = otherParagraphStart;
        do {
            // check token text and detail category assignment
            if (!oneToken.getText().equals(otherToken.getText())
                    || oneToken.isFirstTokenOfDetail() != otherToken.isFirstTokenOfDetail()
                    || oneToken.isLastTokenOfDetail() != otherToken.isLastTokenOfDetail()
                    || oneToken.getDetail() != otherToken.getDetail()
                    && (oneToken.getDetail() == null || otherToken.getDetail() == null || !oneToken.getDetail().getCode()
                            .equals(otherToken.getDetail().getCode()))) {
                return false;
            }
            // iterate through whole (linked) list of tokens
            oneToken = oneToken.getFollowingToken();
            otherToken = otherToken.getFollowingToken();
        } while (oneToken != null && otherToken != null);
        // check if number not one of the paragraphs was longer than the other
        return oneToken == null && otherToken == null;
    }

    @Override
    public Interview clone() {
        return new Interview(null, -1).reset(this);
    }

    @Override
    public String toString() {
        return new StringBuilder("Interview ").append(this.getParticipantId()).append(" (").append(this.getIndex()).append(')').toString();
    }
}
