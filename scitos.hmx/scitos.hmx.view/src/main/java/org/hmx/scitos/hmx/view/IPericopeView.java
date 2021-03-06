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

package org.hmx.scitos.hmx.view;

import java.util.List;

import org.hmx.scitos.hmx.core.HmxModelHandler;
import org.hmx.scitos.hmx.domain.ISemanticalRelationProvider;
import org.hmx.scitos.hmx.domain.model.AbstractConnectable;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.view.swing.components.IAnalysisViewSettings;
import org.hmx.scitos.hmx.view.swing.elements.AbstractCommentable;
import org.hmx.scitos.view.swing.IUndoManagedView;

/** Generic interface of a user view regardless of its actual implementation. */
public interface IPericopeView extends ISemanticalRelationProvider, IUndoManagedView {

    /**
     * Getter for the model handler, responsible for all actual model changes and manager of any model change events.
     *
     * @return the associated model handler instance
     */
    HmxModelHandler getModelHandler();

    /**
     * Getter for the view preferences what parts should be displayed.
     *
     * @return view settings to apply
     */
    IAnalysisViewSettings getViewSettings();

    /**
     * Collect the list of selected {@link Proposition}s in the syntactical analysis, if it is currently active (i.e. displayed).
     *
     * @param selectedProposition
     *            single element to be included in the result list regardless of its actual selection state (may be {@code null})
     * @return the currently selected {@link Proposition}s
     */
    List<Proposition> getSelectedPropositions(Proposition selectedProposition);

    /**
     * Collect the list of selected {@link Proposition}s and {@link org.hmx.scitos.hmx.domain.model.Relation Relation}s in the semantical analysis, if
     * it is currently active (i.e. displayed).
     *
     * @param selectedConnectable
     *            single element to be included in the result list regardless of its actual selection state (may be {@code null})
     * @return the currently selected {@link AbstractConnectable}s
     */
    List<? extends AbstractConnectable> getSelectedConnectables(AbstractConnectable selectedConnectable);

    /**
     * Store the most recent modifications in the displayed comment area to the previously selected {@link AbstractCommentable commentable element}
     * and mark the given {@code newSelected} view element as the currently selected one.
     *
     * @param newSelected
     *            the newly selected commentable view element
     */
    void handleSelectedCommentable(AbstractCommentable<?> newSelected);
}
