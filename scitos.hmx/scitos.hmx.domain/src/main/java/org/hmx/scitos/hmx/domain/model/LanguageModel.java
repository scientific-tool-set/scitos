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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.hmx.scitos.hmx.domain.ISyntacticalFunctionProvider;

/** Implementation of a mutable syntactical model for a fixed language - enabling its visual representation and modification. */
public class LanguageModel implements ISyntacticalFunctionProvider, Serializable, Cloneable {

    /** The name of this model (usually the represented language). */
    private String name;
    /** If the represented language's text is left-to-right oriented (otherwise right-to-left). */
    private boolean leftToRightOriented;
    /** The recommended font names, that are supposed to be able to display the represented language properly. */
    private final List<String> recommendedFonts;
    /** The contained syntactical functions in their respective groups. */
    private final List<List<AbstractSyntacticalFunctionElement>> functionGroups;

    /**
     * Constructor: initializing an empty model with the given name and text orientation.
     *
     * @param name
     *            the name of this model (usually the represented language)
     * @param leftToRightOriented
     *            if the represented language's text orientation is left-to-right (otherwise right-to-left will be assumed)
     */
    public LanguageModel(final String name, final boolean leftToRightOriented) {
        this.name = name;
        this.leftToRightOriented = leftToRightOriented;
        this.recommendedFonts = new LinkedList<String>();
        this.functionGroups = new LinkedList<List<AbstractSyntacticalFunctionElement>>();
    }

    /**
     * Getter for the name of this model (usually the represented language).
     *
     * @return this model's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for the name of this model (usually the represented language).
     *
     * @param name
     *            this model's name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Getter for the represented language's text orientation.
     *
     * @return if the text orientation is {@code left-to-right} (otherwise {@code right-to-left})
     */
    public boolean isLeftToRightOriented() {
        return this.leftToRightOriented;
    }

    /**
     * Setter for the represented language's text orientation.
     *
     * @param leftToRightOriented
     *            whether the text orientation is {@code left-to-right} (otherwise {@code right-to-left})
     */
    public void setLeftToRightOriented(final boolean leftToRightOriented) {
        this.leftToRightOriented = leftToRightOriented;
    }

    /**
     * Getter for the recommended font names, that are supposed to be able to display text in the represented language properly.
     *
     * @return the names of recommended {@link Font}s
     */
    public List<String> getRecommendedFonts() {
        return this.recommendedFonts;
    }

    /**
     * Setter for the recommended font names, that are supposed to be able to display text in the represented language properly.
     *
     * @param recommendedFonts
     *            the names of recommended {@link Font}s
     */
    public void setRecommendedFonts(final List<String> recommendedFonts) {
        this.recommendedFonts.clear();
        this.recommendedFonts.addAll(recommendedFonts);
    }

    /**
     * Reset the contained syntactical function groups to the given ones.
     *
     * @param functions
     *            the syntactical function groups to set for this model (instead of any currently contained ones)
     * @see #addAll(List)
     */
    public void reset(final List<? extends List<? extends AbstractSyntacticalFunctionElement>> functions) {
        this.functionGroups.clear();
        if (functions != null) {
            this.addAll(functions);
        }
    }

    /**
     * Add the given function groups to this model.
     *
     * @param groups
     *            the syntactical functions to add to this model
     * @see #add(List)
     */
    public void addAll(final List<? extends List<? extends AbstractSyntacticalFunctionElement>> groups) {
        for (final List<? extends AbstractSyntacticalFunctionElement> singleGroup : groups) {
            this.add(singleGroup);
        }
    }

    /**
     * Add the given function group to this model.
     *
     * @param functions
     *            the syntactical function group to add to this model
     */
    public void add(final List<? extends AbstractSyntacticalFunctionElement> functions) {
        this.functionGroups.add(Collections.unmodifiableList(new ArrayList<AbstractSyntacticalFunctionElement>(functions)));
    }

    @Override
    public List<List<AbstractSyntacticalFunctionElement>> provideFunctions() {
        return Collections.unmodifiableList(this.functionGroups);
    }

    @Override
    public LanguageModel clone() {
        final LanguageModel clone = new LanguageModel(this.getName(), this.isLeftToRightOriented());
        clone.setRecommendedFonts(this.getRecommendedFonts());
        clone.addAll(this.provideFunctions());
        return clone;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() + 13 * this.functionGroups.size();
    }

    @Override
    public boolean equals(final Object otherObj) {
        if (this == otherObj) {
            return true;
        }
        if (!(otherObj instanceof LanguageModel)) {
            return false;
        }
        final LanguageModel otherModel = (LanguageModel) otherObj;
        return this.leftToRightOriented == otherModel.leftToRightOriented && this.name.equals(otherModel.name)
                && this.functionGroups.equals(otherModel.functionGroups);
    }
}
