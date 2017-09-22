/*
   Copyright (C) 2017 HermeneutiX.org

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

package org.hmx.scitos.hmx.view.swing.components;

import org.hmx.scitos.hmx.core.option.HmxGeneralOption;

/**
 * Collection of independent view toggles for an {@link AnalysisPanel}.
 */
class AnalysisViewSettings implements IAnalysisViewSettings {

    /** Toggle: label fields of propositions. */
    private boolean showingPropositionLabels;
    /** Toggle: syntactic functions of propositions to other propositions. */
    private boolean showingPropositionIndentations;
    /** Toggle: semantic relations over propositions. */
    private boolean showingRelations;
    /** Toggle: individual clause items in propositions. */
    private boolean showingClauseItems;
    /** Toggle: syntactic translation fields of propositions. */
    private boolean showingSyntacticTranslations;
    /** Toggle: semantic translation fields of propositions. */
    private boolean showingSemanticTranslations;

    /**
     * Constructor: defaulting the view toggles to the "Syntactical Analysis" preset.
     */
    AnalysisViewSettings() {
        this.showingPropositionLabels = HmxGeneralOption.SHOW_PROPOSITION_LABELS.getValueAsBoolean();
    }

    /**
     * Show individual clause items and indentation functions of propostions, but hide relations and semantical translations.
     *
     * @param preset view settings to apply (ignoring the label visibility if the given preset is
     * @return self reference
     */
    final AnalysisViewSettings applyViewPreset(final IAnalysisViewSettings preset) {
        // the presets for the syntactical and semantical analyses should preserve the current label visibility
        if (preset != IAnalysisViewSettings.SYNTACTICAL_ANALYSIS && preset != IAnalysisViewSettings.SEMANTICAL_ANALYSIS) {
            this.setShowingPropositionLabels(preset.isShowingPropositionLabels());
        }
        this.setShowingPropositionIndentations(preset.isShowingPropositionIndentations());
        this.setShowingRelations(preset.isShowingRelations());
        this.setShowingClauseItems(preset.isShowingClauseItems());
        this.setShowingSyntacticTranslations(preset.isShowingSyntacticTranslations());
        this.setShowingSemanticTranslations(preset.isShowingSemanticTranslations());
        return this;
    }

    /**
     * Check whether this collection of settings represents the given preset.
     *
     * @param preset settings preset to compare with
     * @return whether this matches the given preset
     */
    final boolean matchesPreset(final IAnalysisViewSettings preset) {
        final boolean matchingLabelVisibility = this.isShowingPropositionLabels() == preset.isShowingPropositionLabels()
                || preset == IAnalysisViewSettings.SYNTACTICAL_ANALYSIS || preset == IAnalysisViewSettings.SEMANTICAL_ANALYSIS;
        final boolean result = matchingLabelVisibility
                && this.isShowingPropositionIndentations() == preset.isShowingPropositionIndentations()
                && this.isShowingRelations() == preset.isShowingRelations()
                && this.isShowingClauseItems() == preset.isShowingClauseItems()
                && this.isShowingSyntacticTranslations() == preset.isShowingSyntacticTranslations()
                && this.isShowingSemanticTranslations() == preset.isShowingSemanticTranslations();
        return result;
    }

    @Override
    public boolean isShowingPropositionLabels() {
        return this.showingPropositionLabels;
    }

    /**
     * Set view toggle for the label fields of propositions.
     *
     * @param show whether the label fields of propositions should be shown
     * @return the given view toggle value
     */
    public boolean setShowingPropositionLabels(final boolean show) {
        this.showingPropositionLabels = show;
        return show;
    }

    @Override
    public boolean isShowingClauseItems() {
        return this.showingClauseItems;
    }

    /**
     * Set view toggle for the individual clause items in propositions.
     *
     * @param show whether the individual clause items in propositions should be shown
     * @return the given view toggle value
     */
    public boolean setShowingClauseItems(final boolean show) {
        this.showingClauseItems = show;
        return show;
    }

    @Override
    public boolean isShowingSyntacticTranslations() {
        return this.showingSyntacticTranslations;
    }

    /**
     * Set view toggle for the syntactic translation fields of propositions.
     *
     * @param show whether the syntactic translation fields of propositions should be shown
     * @return the given view toggle value
     */
    public boolean setShowingSyntacticTranslations(final boolean show) {
        this.showingSyntacticTranslations = show;
        return show;
    }

    @Override
    public boolean isShowingSemanticTranslations() {
        return this.showingSemanticTranslations;
    }

    /**
     * Set view toggle for the semantic translation fields of propositions.
     *
     * @param show whether the semantic translation fields of propositions should be shown
     * @return the given view toggle value
     */
    public boolean setShowingSemanticTranslations(final boolean show) {
        this.showingSemanticTranslations = show;
        return show;
    }

    @Override
    public boolean isShowingPropositionIndentations() {
        return this.showingPropositionIndentations;
    }

    /**
     * Set view toggle for the syntactic functions of propositions to other propositions.
     *
     * @param show whether the syntactic functions of propostiions to other propositions should be shown
     * @return the given view toggle value
     */
    public boolean setShowingPropositionIndentations(final boolean show) {
        this.showingPropositionIndentations = show;
        return show;
    }

    @Override
    public boolean isShowingRelations() {
        return this.showingRelations;
    }

    /**
     * Set view toggle for the semantic relations over propositions.
     *
     * @param show whether the semantic relations over propositions should be shown
     * @return the given view toggle value
     */
    public boolean setShowingRelations(final boolean show) {
        this.showingRelations = show;
        return show;
    }
}
