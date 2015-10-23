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
    private final String name;
    /** If the represented language's text is left-to-right oriented (otherwise right-to-left). */
    private final boolean leftToRightOriented;
    /** The recommended font names, that are supposed to be able to display the represented language properly. */
    private final List<String> recommendedFonts;
    /** The contained syntactical functions in their respective groups. */
    private final List<List<SyntacticalFunction>> functionGroups;

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
        this.functionGroups = new LinkedList<List<SyntacticalFunction>>();
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
     * Getter for the represented language's text orientation.
     *
     * @return if the text orientation is {@code left-to-right} (otherwise {@code right-to-left})
     */
    public boolean isLeftToRightOriented() {
        return this.leftToRightOriented;
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
     * Reset the contained syntactical function groups to the given ones.
     *
     * @param functions
     *            the syntactical function groups to set for this model (instead of any currently contained ones)
     * @see #addAll(List)
     */
    public void reset(final List<? extends List<? extends SyntacticalFunction>> functions) {
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
    public void addAll(final List<? extends List<? extends SyntacticalFunction>> groups) {
        for (final List<? extends SyntacticalFunction> singleGroup : groups) {
            this.add(singleGroup);
        }
    }

    /**
     * Add the given function group to this model.
     *
     * @param functions
     *            the syntactical function group to add to this model
     */
    public void add(final List<? extends SyntacticalFunction> functions) {
        this.functionGroups.add(Collections.unmodifiableList(new ArrayList<SyntacticalFunction>(functions)));
    }

    @Override
    public List<List<SyntacticalFunction>> provideFunctions() {
        return Collections.unmodifiableList(this.functionGroups);
    }

    @Override
    public LanguageModel clone() {
        final LanguageModel clone = new LanguageModel(this.getName(), this.isLeftToRightOriented());
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
