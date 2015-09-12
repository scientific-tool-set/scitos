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

package org.hmx.scitos.ais.view.swing.components;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.hmx.scitos.ais.core.AisOption;
import org.hmx.scitos.ais.core.i18n.AisMessage;
import org.hmx.scitos.ais.view.swing.AisViewProject;
import org.hmx.scitos.view.ScitosIcon;
import org.hmx.scitos.view.swing.ScitosClient;

/**
 * Tab view: overview of the {@link AisViewProject project} and buttons for creating new contained interviews and generating summaries/exports.
 */
public final class ProjectOverView extends AbstractAisProjectView<AisViewProject> {

    /** Contained result panel displaying the summed up detail category assignments (i.e. scorings). */
    private final PatternAnalysisPanel analysisPanel;

    /**
     * Main constructor.
     *
     * @param client
     *            client instance containing this view as a tab in its MainView
     * @param project
     *            managed view project
     * @param options
     *            preferences handler, providing the default detail category model for any new project
     */
    public ProjectOverView(final ScitosClient client, final AisViewProject project, final AisOption options) {
        super(project, project, options, new BorderLayout());
        this.analysisPanel = new PatternAnalysisPanel(client, project);
        this.add(this.analysisPanel);
        final JButton addInterviewButton = new JButton(AisMessage.INTERVIEW_NEW.get(), ScitosIcon.MODEL_ELEMENT_ADD.create());
        addInterviewButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                ProjectOverView.this.createInterview();
            }
        });
        this.add(addInterviewButton, BorderLayout.SOUTH);
        this.refresh();
    }

    @Override
    public void refresh() {
        this.analysisPanel.refresh();
        this.analysisPanel.setVisible(!this.getProject().getModelObject().getInterviews().isEmpty());
    }

    @Override
    public void submitChangesToModel() {
        // no changes to submit
    }

    @Override
    public boolean canUndo() {
        return false;
    }

    @Override
    public boolean canRedo() {
        return false;
    }

    @Override
    public void undo() {
        throw new CannotUndoException();
    }

    @Override
    public void redo() {
        throw new CannotRedoException();
    }
}
