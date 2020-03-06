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

package org.hmx.scitos.hmx.domain.model.originlanguage;

import java.awt.Font;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.hmx.scitos.domain.util.CollectionUtil;
import org.hmx.scitos.hmx.domain.ISyntacticalFunctionProvider;

/** Implementation of a mutable syntactical model for a fixed language - enabling its visual representation and modification. */
public class LanguageModel implements ISyntacticalFunctionProvider, Serializable, Cloneable {

    /** If the represented language's text is left-to-right oriented (otherwise right-to-left). */
    private boolean leftToRightOriented;
    /** The recommended font names, that are supposed to be able to display the represented language properly. */
    private final List<String> recommendedFonts;
    private final List<List<AbstractSyntacticalElementReference>> functionGroups;
    /** All included display profiles. */
    private final List<SyntacticalDisplayProfile> displayProfiles;
    /**
     * Display profile that should be applied by default, if no {@link #activeDisplayProfile} has been selected yet. This is mainly for new projects
     * being started with this origin text language.
     */
    private SyntacticalDisplayProfile defaultDisplayProfile;
    /** Display profile that should be applied in the user interface when looking-up the syntactical functions (names, codes, etc.). */
    private SyntacticalDisplayProfile activeDisplayProfile;

    /**
     * Constructor: initializing an empty model with the given name and text orientation.
     *
     * @param leftToRightOriented if the represented language's text orientation is left-to-right (otherwise right-to-left will be assumed)
     */
    public LanguageModel(final boolean leftToRightOriented, final List<List<AbstractSyntacticalElementReference>> functionGroups) {
        this.leftToRightOriented = leftToRightOriented;
        this.recommendedFonts = new LinkedList<String>();
        this.functionGroups = new ArrayList<List<AbstractSyntacticalElementReference>>(functionGroups.size());
        for (final List<AbstractSyntacticalElementReference> singleGroup : functionGroups) {
            this.functionGroups.add(new ArrayList<AbstractSyntacticalElementReference>(singleGroup));
        }
        this.displayProfiles = new LinkedList<SyntacticalDisplayProfile>();
    }

    /**
     * Getter for the name of this model (usually the represented language).
     *
     * @return this model's name
     */
    public String getName() {
        return this.getActiveDisplayProfile().getName();
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
     * @param leftToRightOriented whether the text orientation is {@code left-to-right} (otherwise {@code right-to-left})
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
        return Collections.unmodifiableList(this.recommendedFonts);
    }

    /**
     * Setter for the recommended font names, that are supposed to be able to display text in the represented language properly.
     *
     * @param recommendedFonts the names of recommended {@link Font}s
     */
    public void setRecommendedFonts(final List<String> recommendedFonts) {
        this.recommendedFonts.clear();
        this.recommendedFonts.addAll(recommendedFonts);
    }

    /**
     * Getter for the included display profiles.
     *
     * @return included display profiles
     */
    public List<SyntacticalDisplayProfile> getDisplayProfiles() {
        return Collections.unmodifiableList(this.displayProfiles);
    }

    /**
     * Add the given display profile to this origin text language model. If it is the first profile, it will also automatically become the default and
     * active one.
     *
     * @param displayProfile profile to add
     * @throws IllegalArgumentException if given profile does not match the model structure by the previous profile(s)
     */
    public void addDisplayProfile(final SyntacticalDisplayProfile displayProfile) {
        if (this.defaultDisplayProfile == null) {
            this.defaultDisplayProfile = displayProfile;
            this.activeDisplayProfile = displayProfile;
        } else if (!displayProfile.representsSameModel(this.functionGroups)) {
            throw new IllegalArgumentException("Given profile does not match already contained model.");
        }
        this.displayProfiles.add(displayProfile);
    }

    /**
     * Getter for display profile that should be applied by default, if no active one has been selected yet. This is mainly for new projects being
     * started with this origin text language.
     *
     * @return default display profile
     */
    public SyntacticalDisplayProfile getDefaultDisplayProfile() {
        return this.defaultDisplayProfile;
    }

    /**
     * Getter for display profile that has been explicitly selected to be applied. This is mainly for currently displayed projects being used.
     *
     * @return active display profile
     */
    public SyntacticalDisplayProfile getActiveDisplayProfile() {
        return this.activeDisplayProfile;
    }

    /**
     * Setter for the display profile to be applied in the user interface.
     *
     * @param activeProfile active display profile
     */
    public void setActiveDisplayProfile(final SyntacticalDisplayProfile activeProfile) {
        if (activeProfile == null) {
            this.activeDisplayProfile = this.defaultDisplayProfile;
        } else if (CollectionUtil.containsInstance(this.displayProfiles, activeProfile)) {
            this.activeDisplayProfile = activeProfile;
        } else {
            throw new IllegalArgumentException("Given profile is not included in this language model.");
        }
    }

    @Override
    public String getSyntacticalFunctionCodeByReference(final SyntacticalFunctionReference reference) {
        return this.getActiveDisplayProfile().getSyntacticalFunctionCodeByReference(reference);
    }

    @Override
    public List<List<AbstractSyntacticalElement<?>>> provideFunctions() {
        final List<List<AbstractSyntacticalElement<?>>> result = new ArrayList<List<AbstractSyntacticalElement<?>>>(this.functionGroups.size());
        for (final List<AbstractSyntacticalElementReference> singleGroup : this.functionGroups) {
            result.add(this.getActiveDisplayProfile().lookupFunctions(singleGroup));
        }
        return result;
    }

    @Override
    public LanguageModel clone() {
        final LanguageModel clonedModel = new LanguageModel(this.leftToRightOriented, this.functionGroups);
        clonedModel.setRecommendedFonts(this.recommendedFonts);
        /* Since the SyntacticalFunctionDisplayProfile class is immutable, we can just re-use the same instances. */
        // the first added profile is automatically the default and active one
        clonedModel.addDisplayProfile(this.defaultDisplayProfile);
        // now add all other profiles as well
        for (final SyntacticalDisplayProfile singleProfile : this.getDisplayProfiles()) {
            if (singleProfile == this.defaultDisplayProfile) {
                continue;
            }
            clonedModel.addDisplayProfile(singleProfile);
            if (singleProfile == this.activeDisplayProfile) {
                // the default profile is not the active one, need to reset this here accordingly
                clonedModel.setActiveDisplayProfile(singleProfile);
            }
        }
        return clonedModel;
    }

    @Override
    public int hashCode() {
        return this.defaultDisplayProfile.hashCode();
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
        return this.leftToRightOriented == otherModel.leftToRightOriented
                && this.functionGroups.equals(otherModel.functionGroups)
                && this.defaultDisplayProfile.equals(otherModel.defaultDisplayProfile)
                && this.activeDisplayProfile.equals(otherModel.activeDisplayProfile)
                && this.displayProfiles.equals(otherModel.displayProfiles);
    }
}
