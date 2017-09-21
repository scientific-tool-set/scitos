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
}
