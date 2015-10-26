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

package org.hmx.scitos.ais.domain.model;

import java.io.Serializable;

import org.hmx.scitos.domain.util.ComparisonUtil;

/**
 * Representation of a text token (usually a single word) in an autobiographical interview scoring, that can be assigned a {@link DetailCategory}.
 */
public final class TextToken implements Cloneable, Serializable {

    /**
     * The preceding token in the interview's paragraph (is {@code null} if this is the first token in the paragraph).
     */
    private TextToken previousToken;
    /**
     * The next token in the interview's paragraph (is {@code null} if this is the last token in the paragraph).
     */
    private TextToken followingToken;
    /** The text this token is comprised of (usually a single word). */
    private final String text;
    /** The assigned detail category (i.e. the actual scoring). */
    private DetailCategory detail = null;
    /** If this is the first token in the section with the assigned detail category. */
    private boolean firstTokenOfDetail = false;
    /** If this is the last token in the section with the assigned detail category. */
    private boolean lastTokenOfDetail = false;

    /**
     * Main constructor.
     *
     * @param text
     *            the text this token is comprised of (usually a single word)
     */
    public TextToken(final String text) {
        this.text = text;
    }

    /**
     * Getter for the preceding token.
     *
     * @return the previous token (is {@code null} if this is the first token in the paragraph)
     */
    public TextToken getPreviousToken() {
        return this.previousToken;
    }

    /**
     * Setter for the preceding token.
     *
     * @param token
     *            the previous token
     * @return self reference
     */
    public TextToken setPreviousToken(final TextToken token) {
        this.previousToken = token;
        return this;
    }

    /**
     * Getter for the next token.
     *
     * @return the following token (is {@code null} if this is the last token in the paragraph)
     */
    public TextToken getFollowingToken() {
        return this.followingToken;
    }

    /**
     * Setter for the next token.
     *
     * @param token
     *            the following token
     * @return self reference
     */
    public TextToken setFollowingToken(final TextToken token) {
        this.followingToken = token;
        return this;
    }

    /**
     * Getter for the text this token is comprised of (usually a single word).
     *
     * @return the token's text
     */
    public String getText() {
        return this.text;
    }

    /**
     * Getter for the assigned detail category.
     *
     * @return the assigned detail (can be {@code null})
     */
    public DetailCategory getDetail() {
        return this.detail;
    }

    /**
     * Setter for the assigned detail category.
     *
     * @param detail
     *            the detail to assign
     * @return self reference
     */
    public TextToken setDetail(final DetailCategory detail) {
        this.detail = detail;
        return this;
    }

    /**
     * Getter for the flag indicating if this token is the first of the section marked with the assigned detail category.
     *
     * @return if this is the first element in the token group with the assigned detail
     * @see #getDetail()
     */
    public boolean isFirstTokenOfDetail() {
        return this.firstTokenOfDetail;
    }

    /**
     * Setter for the flag indicating if this token is the first of the section marked with the assigned detail category.
     *
     * @param value
     *            if this is the first element in the token section with the assigned detail
     * @return self reference
     */
    public TextToken setFirstTokenOfDetail(final boolean value) {
        this.firstTokenOfDetail = value;
        return this;
    }

    /**
     * Getter for the flag indicating if this token is the last of the section marked with the assigned detail category.
     *
     * @return if this is the last element in the token section with the assigned detail
     * @see #getDetail()
     */
    public boolean isLastTokenOfDetail() {
        return this.lastTokenOfDetail;
    }

    /**
     * Setter for the flag indicating if this token is the last of the group marked with the assigned detail category.
     *
     * @param value
     *            if this is the last element in the token group with the assigned detail
     * @return self reference
     */
    public TextToken setLastTokenOfDetail(final boolean value) {
        this.lastTokenOfDetail = value;
        return this;
    }

    @Override
    public TextToken clone() {
        final TextToken cloned = new TextToken(this.getText()).setDetail(this.getDetail());
        cloned.setFirstTokenOfDetail(this.isFirstTokenOfDetail()).setLastTokenOfDetail(this.isLastTokenOfDetail());
        if (this.getFollowingToken() != null) {
            final TextToken followerClone = this.getFollowingToken().clone();
            followerClone.setPreviousToken(cloned);
            cloned.setFollowingToken(followerClone);
        }
        return cloned;
    }

    @Override
    public int hashCode() {
        return this.getText().hashCode();
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof TextToken)) {
            return false;
        }
        final TextToken otherToken = (TextToken) otherObject;
        if (!this.getText().equals(otherToken.getText()) || !ComparisonUtil.isNullAwareEqual(this.getDetail(), otherToken.getDetail())) {
            return false;
        }
        return this.isFirstTokenOfDetail() == otherToken.isFirstTokenOfDetail() && this.isLastTokenOfDetail() == otherToken.isLastTokenOfDetail();
    }

    @Override
    public String toString() {
        return this.getText();
    }
}
