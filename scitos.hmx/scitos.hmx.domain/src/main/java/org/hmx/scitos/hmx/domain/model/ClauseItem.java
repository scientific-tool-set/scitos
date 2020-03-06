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
import java.io.Serializable;

import org.hmx.scitos.domain.util.ComparisonUtil;
import org.hmx.scitos.hmx.domain.ICanHaveSyntacticalFunction;
import org.hmx.scitos.hmx.domain.ICommentable;
import org.hmx.scitos.hmx.domain.model.originlanguage.SyntacticalFunctionReference;

/**
 * Clause item containing a part of the origin text and its syntactical function for the {@link Proposition} it is a part of.
 */
public final class ClauseItem implements ICanHaveSyntacticalFunction, ICommentable, Cloneable, Serializable {

    /** The available font styles to highlight an item. */
    public enum Style {
        /**
         * Equivalent to {@link Font#PLAIN plain} font style.
         */
        PLAIN,
        /**
         * Equivalent to {@link Font#BOLD bold} font style.
         */
        BOLD,
        /**
         * Equivalent to {@link Font#ITALIC italic} font style.
         */
        ITALIC,
        /**
         * Equivalent to the combination {@link Font#BOLD bold} and {@link Font#ITALIC italic} font style.
         */
        BOLD_ITALIC;
    }

    /** The proposition this belongs to. */
    private Proposition parent;
    /** The contained origin text part. */
    private String originText;
    /** The syntactical function in the proposition. */
    private SyntacticalFunctionReference function;
    /** The applied font style. */
    private Style fontStyle = Style.PLAIN;
    /** The additional comment's text. */
    private String comment;

    /**
     * Constructor.
     *
     * @param parent
     *            the proposition this is a part of
     * @param originText
     *            the contained origin text part
     */
    public ClauseItem(final Proposition parent, final String originText) {
        this.setParent(parent);
        this.setOriginText(originText);
    }

    /**
     * Getter for the proposition this is a part of.
     *
     * @return the proposition this belongs to
     */
    public Proposition getParent() {
        return this.parent;
    }

    /**
     * Setter for the proposition this is a part of.
     *
     * @param parentProposition
     *            the proposition this belongs to
     */
    public void setParent(final Proposition parentProposition) {
        this.parent = parentProposition;
    }

    /**
     * Getter for the contained origin text part.
     *
     * @return the contained origin text part
     */
    public String getOriginText() {
        return this.originText;
    }

    /**
     * Setter for the contained origin text part.
     *
     * @param originTextPart
     *            the contained origin text part
     */
    public void setOriginText(final String originTextPart) {
        this.originText = originTextPart;
    }

    @Override
    public SyntacticalFunctionReference getFunction() {
        return this.function;
    }

    @Override
    public void setFunction(final SyntacticalFunctionReference syntacticalFunction) {
        this.function = syntacticalFunction;
    }

    /**
     * Getter for the applied font style.
     *
     * @return the applied font style
     */
    public Style getFontStyle() {
        return this.fontStyle;
    }

    /**
     * Setter for the applied font style.
     *
     * @param style
     *            the font style to apply
     */
    public void setFontStyle(final Style style) {
        this.fontStyle = style;
    }

    @Override
    public String getComment() {
        return this.comment;
    }

    @Override
    public void setComment(final String comment) {
        this.comment = comment;
    }

    /**
     * Create a copy of this {@link ClauseItem}, WITHOUT setting its parent {@link Proposition}.
     *
     * @return cloned {@link ClauseItem} without parent {@link Proposition}
     */
    @Override
    public ClauseItem clone() {
        final ClauseItem cloned = new ClauseItem(null, this.originText);
        cloned.function = this.function;
        cloned.fontStyle = this.fontStyle;
        cloned.comment = this.comment;
        return cloned;
    }

    @Override
    public String toString() {
        return this.getOriginText();
    }

    @Override
    public int hashCode() {
        int result = this.originText.hashCode() * 3;
        result += this.function == null ? 0 : this.function.hashCode();
        result *= 5;
        result += this.comment == null ? 0 : this.comment.hashCode();
        result *= 7;
        return result + this.fontStyle.ordinal();
    }

    @Override
    public boolean equals(final Object otherObj) {
        if (this == otherObj) {
            return true;
        }
        if (!(otherObj instanceof ClauseItem)) {
            return false;
        }
        final ClauseItem otherItem = (ClauseItem) otherObj;
        return this.fontStyle == otherItem.fontStyle
                && this.originText.equals(otherItem.originText)
                && ComparisonUtil.isNullAwareEqual(this.function, otherItem.function)
                && ComparisonUtil.isNullOrEmptyAwareEqual(this.comment, otherItem.comment);
    }
}
