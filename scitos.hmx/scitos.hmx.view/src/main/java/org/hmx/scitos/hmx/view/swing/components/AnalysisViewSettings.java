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
    /** Toggle: individual clause items in propositions. */
    private boolean showingClauseItems;
    /** Toggle: syntactic translation fields of propositions. */
    private boolean showingSyntacticTranslations;
    /** Toggle: semantic translation fields of propositions. */
    private boolean showingSemanticTranslations;
    /** Toggle: syntactic functions of propositions to other propositions. */
    private boolean showingPropositionIndentations;
    /** Toggle: semantic relations over propositions. */
    private boolean showingRelations;

    /**
     * Constructor: defaulting the view toggles to the "Syntactical Analysis" preset.
     */
    AnalysisViewSettings() {
        this.showingPropositionLabels = HmxGeneralOption.SHOW_PROPOSITION_LABELS.getValueAsBoolean();
        this.showingSyntacticTranslations = HmxGeneralOption.SHOW_PROPOSITION_TRANSLATIONS.getValueAsBoolean();
        this.switchToSyntacticalAnalysis();
    }

    /**
     * Show individual clause items and indentation functions of propostions, but hide relations and semantical translations.
     */
    final void switchToSyntacticalAnalysis() {
        this.showingClauseItems = true;
        this.showingSyntacticTranslations = this.showingSyntacticTranslations || this.showingSemanticTranslations;
        this.showingSemanticTranslations = false;
        this.showingPropositionIndentations = true;
        this.showingRelations = false;
    }

    /**
     * Show relations, but hide individual clause items and indentation functions of propositions.
     */
    final void switchToSemanticalAnalysis() {
        this.showingClauseItems = false;
        this.showingSyntacticTranslations = false;
        this.showingSemanticTranslations = this.showingSyntacticTranslations || this.showingSemanticTranslations;
        this.showingPropositionIndentations = false;
        this.showingRelations = true;
    }

    /**
     * Show everything.
     */
    final void showAll() {
        this.showingPropositionLabels = true;
        this.showingClauseItems = true;
        this.showingSyntacticTranslations = true;
        this.showingSemanticTranslations = true;
        this.showingPropositionIndentations = true;
        this.showingRelations = true;
    }

    /**
     * Hide everything but the origin text of each proposition.
     */
    final void hideAll() {
        this.showingPropositionLabels = false;
        this.showingClauseItems = false;
        this.showingSyntacticTranslations = false;
        this.showingSemanticTranslations = false;
        this.showingPropositionIndentations = false;
        this.showingRelations = false;
    }

    @Override
    public boolean isShowingPropositionLabels() {
        return this.showingPropositionLabels;
    }

    /**
     * Set view toggle for the label fields of propositions.
     *
     * @param show whether the label fields of propositions should be shown
     */
    public void setShowingPropositionLabels(final boolean show) {
        this.showingPropositionLabels = show;
    }

    @Override
    public boolean isShowingClauseItems() {
        return this.showingClauseItems;
    }

    /**
     * Set view toggle for the individual clause items in propositions.
     *
     * @param show whether the individual clause items in propositions should be shown
     */
    public void setShowingClauseItems(final boolean show) {
        this.showingClauseItems = show;
    }

    @Override
    public boolean isShowingSyntacticTranslations() {
        return this.showingSyntacticTranslations;
    }

    /**
     * Set view toggle for the syntactic translation fields of propositions.
     *
     * @param show whether the syntactic translation fields of propositions should be shown
     */
    public void setShowingSyntacticTranslations(final boolean show) {
        this.showingSyntacticTranslations = show;
    }

    @Override
    public boolean isShowingSemanticTranslations() {
        return this.showingSemanticTranslations;
    }

    /**
     * Set view toggle for the semantic translation fields of propositions.
     *
     * @param show whether the semantic translation fields of propositions should be shown
     */
    public void setShowingSemanticTranslations(final boolean show) {
        this.showingSemanticTranslations = show;
    }

    @Override
    public boolean isShowingPropositionIndentations() {
        return this.showingPropositionIndentations;
    }

    /**
     * Set view toggle for the syntactic functions of propositions to other propositions.
     *
     * @param show whether the syntactic functions of propostiions to other propositions should be shown
     */
    public void setShowingPropositionIndentations(final boolean show) {
        this.showingPropositionIndentations = show;
    }

    @Override
    public boolean isShowingRelations() {
        return this.showingRelations;
    }

    /**
     * Set view toggle for the semantic relations over propositions.
     *
     * @param show whether the semantic relations over propositions should be shown
     */
    public void setShowingRelations(final boolean show) {
        this.showingRelations = show;
    }
}
