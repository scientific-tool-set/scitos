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

/**
 * Wrapper for a number of view settings.
 */
public interface IAnalysisViewSettings {

    /** Preset: showing all details. */
    IAnalysisViewSettings SHOW_ALL = new ReadOnlyViewSettings(true, true, true, true, true, true);
    /** Preset: showing all details. */
    IAnalysisViewSettings HIDE_ALL = new ReadOnlyViewSettings(false, false, false, false, false, false);
    /** Preset: syntactical analysis. */
    IAnalysisViewSettings SYNTACTICAL_ANALYSIS = new ReadOnlyViewSettings(true, true, false, true, true, false);
    /** Preset: semantical analysis. */
    IAnalysisViewSettings SEMANTICAL_ANALYSIS = new ReadOnlyViewSettings(true, false, true, false, false, true);

    /**
     * Get view toggle for the label fields of propositions.
     *
     * @return whether the label fields of propositions should be shown
     */
    boolean isShowingPropositionLabels();

    /**
     * Get view toggle for the individual clause items in propositions.
     *
     * @return whether the individual clause items in propositions should be shown
     */
    boolean isShowingClauseItems();

    /**
     * Get view toggle for the syntactic translation fields of propositions.
     *
     * @return whether the syntactic translation fields of propositions should be shown
     */
    boolean isShowingSyntacticTranslations();

    /**
     * Get view toggle for the semantic translation fields of propositions.
     *
     * @return whether the semantic translation fields of propositions should be shown
     */
    boolean isShowingSemanticTranslations();

    /**
     * Get view toggle for the syntactic functions of propositions to other propositions.
     *
     * @return whether the syntactic functions of propostiions to other propositions should be shown
     */
    boolean isShowingPropositionIndentations();

    /**
     * Get view toggle for the semantic relations over propositions.
     *
     * @return whether the semantic relations over propositions should be shown
     */
    boolean isShowingRelations();

    /**
     * Read-only implementation of the {@link IAnalysisViewSettings} interface, for offering unmodifiable presets.
     */
    class ReadOnlyViewSettings implements IAnalysisViewSettings {

        /** View Setting: label fields of propositions. */
        private final boolean showingPropositionLabels;
        /** View Setting: syntactic functions of propositions to other propositions. */
        private final boolean showingPropositionIndentations;
        /** View Setting: semantic relations over propositions. */
        private final boolean showingRelations;
        /** View Setting: individual clause items in propositions. */
        private final boolean showingClauseItems;
        /** View Setting: syntactic translation fields of propositions. */
        private final boolean showingSyntacticTranslations;
        /** View Setting: semantic translation fields of propositions. */
        private final boolean showingSemanticTranslations;

        /**
         * Constructor.
         *
         * @param showingPropositionLabels preset value: label fields of propositions
         * @param showingPropositionIndentations preset value: syntactic functions of propositions to other propositions
         * @param showingRelations preset value: semantic relations over propositions
         * @param showingClauseItems preset value: individual clause items in propositions
         * @param showingSyntacticTranslations preset value: syntactic translation fields of propositions
         * @param showingSemanticTranslations preset value: semantic translation fields of propositions
         */
        ReadOnlyViewSettings(final boolean showingPropositionLabels, final boolean showingPropositionIndentations, final boolean showingRelations,
                final boolean showingClauseItems, final boolean showingSyntacticTranslations, final boolean showingSemanticTranslations) {
            this.showingPropositionLabels = showingPropositionLabels;
            this.showingPropositionIndentations = showingPropositionIndentations;
            this.showingRelations = showingRelations;
            this.showingClauseItems = showingClauseItems;
            this.showingSyntacticTranslations = showingSyntacticTranslations;
            this.showingSemanticTranslations = showingSemanticTranslations;
        }

        @Override
        public boolean isShowingPropositionLabels() {
            return this.showingPropositionLabels;
        }

        @Override
        public boolean isShowingPropositionIndentations() {
            return this.showingPropositionIndentations;
        }

        @Override
        public boolean isShowingRelations() {
            return this.showingRelations;
        }

        @Override
        public boolean isShowingClauseItems() {
            return this.showingClauseItems;
        }

        @Override
        public boolean isShowingSyntacticTranslations() {
            return this.showingSyntacticTranslations;
        }

        @Override
        public boolean isShowingSemanticTranslations() {
            return this.showingSemanticTranslations;
        }
    }
}
