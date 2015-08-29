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

import java.awt.Color;
import java.io.Serializable;

import javax.swing.KeyStroke;

import org.hmx.scitos.domain.util.ComparisonUtil;

/**
 * Category that can be assigned to a {@link TextToken} or any number of them, that comprise a specific type of information conveyed in an
 * autobiographical {@link Interview}.
 */
public final class DetailCategory implements Serializable {

    /** Super ordinated detail category this is a specific subset of. */
    private final DetailCategory parent;
    /** Short identifier that is used as the textual representation of this detail category. */
    private final String code;
    /** More elaborate description what this detail category represents. */
    private final String name;
    /** If this detail category can be directly assigned to tokens when scoring interviews. */
    private final boolean selectable;
    /** Highlight color displayed for tokens that have this category assigned in scored interviews. */
    private final Color color;
    /** The short cut for assigning this detail category to any selected tokens when scoring an interview. */
    private final KeyStroke shortCut;

    /**
     * Main constructor.
     * 
     * @param parent
     *            the detail category this is sub category of; or <code>null</code> if it is a top level category itself
     * @param code
     *            the short label displayed inside an interview scoring (e.g. E2, I4, ...)
     * @param name
     *            the actual name or short description of what this detail category represents (e.g. temporal statement, local statement, ...)
     * @param selectable
     *            if this category is selectable directly in an interview scoring - otherwise it is only a placeholder for its subordinated detail
     *            categories
     * @param color
     *            the color used to highlight tokens with this category assigned
     * @param shortCut
     *            the keyboard shortcut, to assign this detail category to all selected tokens in a scored interview
     */
    public DetailCategory(final DetailCategory parent, final String code, final String name, final boolean selectable, final Color color,
            final KeyStroke shortCut) {
        this.parent = parent;
        this.code = code.intern();
        this.name = name;
        this.selectable = selectable;
        this.color = color;
        this.shortCut = shortCut;
    }

    /**
     * Getter for the detail category this is sub category of (can be <code>null</code>).
     * 
     * @return the parent
     */
    public DetailCategory getParent() {
        return this.parent;
    }

    /**
     * Getter for the short label displayed inside an interview scoring.
     * 
     * @return the details code (e.g. E2, I4, ...)
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Getter for the actual name or short description of what this detail category represents.
     * 
     * @return the name or description (e.g. temporal statement, local statement, ...)
     */
    public String getName() {
        return this.name;
    }

    /**
     * Getter for the flag indicating if this category is selectable directly in an interview scoring. Otherwise it is only a placeholder for its
     * subordinated detail categories.
     * 
     * @return if this detail category can be assigned in an interview scoring
     */
    public boolean isSelectable() {
        return this.selectable;
    }

    /**
     * Getter for the color used to highlight tokens with this category assigned.
     * 
     * @return the highlighting color for tokens with this category.
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * Getter for the keyboard shortcut, to assign this detail category to all selected tokens in a scored interview.
     * 
     * @return the associated keyboard short cut
     */
    public KeyStroke getShortCut() {
        return this.shortCut;
    }

    @Override
    public int hashCode() {
        return this.getCode().hashCode();
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof DetailCategory)) {
            return false;
        }
        final DetailCategory otherCategory = (DetailCategory) otherObject;
        return this.isSelectable() == otherCategory.isSelectable() && this.getCode().equals(otherCategory.getCode())
                && ComparisonUtil.isNullAwareEqual(this.getName(), otherCategory.getName())
                && ComparisonUtil.isNullAwareEqual(this.getColor(), otherCategory.getColor())
                && ComparisonUtil.isNullAwareEqual(this.getShortCut(), otherCategory.getShortCut())
                && ComparisonUtil.isNullAwareEqual(this.getParent(), otherCategory.getParent());
    }

    @Override
    public String toString() {
        return this.getCode();
    }
}
