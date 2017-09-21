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

package org.hmx.scitos.hmx.view.swing.components;

import java.util.List;

import javax.swing.SwingUtilities;

import org.hmx.scitos.domain.ModelChangeListener;
import org.hmx.scitos.domain.ModelEvent;
import org.hmx.scitos.hmx.domain.model.AbstractConnectable;
import org.hmx.scitos.hmx.domain.model.ClauseItem;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.domain.model.Relation;
import org.hmx.scitos.hmx.view.swing.elements.IConnectable;
import org.hmx.scitos.hmx.view.swing.elements.SemProposition;
import org.hmx.scitos.hmx.view.swing.elements.ViewRelation;

/**
 * Listener for the {@link SemAnalysisPanel} to react on changed model elements.
 */
public final class SemControl implements ModelChangeListener {

    /** targeted semantical analysis view. */
    final SemAnalysisPanel semArea;

    /**
     * Constructor: storing the targeted semantical analysis view to display the listened changes in.
     *
     * @param panel
     *            targeted semantical analysis view
     */
    SemControl(final SemAnalysisPanel panel) {
        this.semArea = panel;
    }

    /**
     * Create the listening functions for the defined {@link SemAnalysisPanel}, responsible for the whole handling of event representing a change in
     * the model.
     *
     * @param event
     *            thrown {@link ModelEvent} containing the changed model element
     */
    @Override
    public void modelChanged(final ModelEvent<?> event) {
        final Object target = event.getTarget();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (target instanceof Pericope || target instanceof Relation) {
                    // request whole rebuild
                    SemControl.this.semArea.repaintPericope();
                } else if (target instanceof Proposition) {
                    SemControl.this.refreshProposition((Proposition) target);
                } else if (target instanceof ClauseItem) {
                    SemControl.this.refreshProposition(((ClauseItem) target).getParent());
                }
                // the target should be no instance of another class
            }
        });
    }

    /**
     * Handle a {@link ModelEvent} containing a {@link Proposition}.
     *
     * @param target
     *            proposition to refresh in view
     */
    void refreshProposition(final Proposition target) {
        final SemProposition representative = SemControl.getRepresentative(this.semArea, target);
        if (representative == null) {
            // edited proposition currently not displayed
            return;
        }
        representative.refreshLabelText();
        representative.refreshOriginText();
        representative.refreshTranslation();
        representative.refreshComment();
        representative.setVisible(false);
        representative.setVisible(true);
        this.semArea.resetHeaders();
    }

    /**
     * Get the view representation ({@link SemProposition}/{@link ViewRelation}) of the specified model object ({@link Proposition}/{@link Relation}).
     *
     * @param semArea
     *            analysis view to search in
     * @param target
     *            {@link Proposition}/{@link Relation} to look for
     * @return view component ({@link SemProposition}/ {@link ViewRelation}) representing target
     */
    public static IConnectable<?> getRepresentative(final SemAnalysisPanel semArea, final AbstractConnectable target) {
        if (target instanceof Proposition) {
            return SemControl.getRepresentative(semArea, (Proposition) target);
        } else if (target instanceof Relation) {
            return SemControl.getRepresentative(semArea, (Relation) target);
        }
        // unsupported model object
        throw new IllegalArgumentException();
    }

    /**
     * Get the representing view component for the given {@link Proposition}.
     *
     * @param semArea
     *            analysis view to search in
     * @param target
     *            {@link Proposition} to look for
     * @return {@link SemProposition} representing target
     */
    public static SemProposition getRepresentative(final SemAnalysisPanel semArea, final Proposition target) {
        final List<SemProposition> viewPropositions = semArea.getPropositionList();
        for (final SemProposition singleViewProposition : viewPropositions) {
            if (target == singleViewProposition.getRepresented()) {
                // found representative in the syntactical analysis view
                return singleViewProposition;
            }
        }
        // target does not exist in the syntactical analysis view
        return null;
    }

    /**
     * Get the representation of the specified {@link Relation} in the semantical analysis view.
     *
     * @param semArea
     *            analysis view to search in
     * @param target
     *            {@link Relation} to look for
     * @return {@link ViewRelation} representing target
     */
    public static ViewRelation getRepresentative(final SemAnalysisPanel semArea, final Relation target) {
        return semArea.getRelationMap().get(target);
    }
}
