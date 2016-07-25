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

package org.hmx.scitos.ais.view.swing;

import java.io.File;
import java.util.Arrays;

import javax.inject.Inject;

import org.hmx.scitos.ais.core.AisOption;
import org.hmx.scitos.ais.core.ModelHandlerImpl;
import org.hmx.scitos.ais.core.i18n.AisMessage;
import org.hmx.scitos.ais.domain.model.AisProject;
import org.hmx.scitos.ais.domain.model.Interview;
import org.hmx.scitos.ais.view.swing.components.InterviewView;
import org.hmx.scitos.ais.view.swing.components.ParticipantInterviewGroupView;
import org.hmx.scitos.ais.view.swing.components.ProjectOverView;
import org.hmx.scitos.domain.IModel;
import org.hmx.scitos.view.ContextMenuBuilder;
import org.hmx.scitos.view.ContextMenuBuilder.CMenuItemAction;
import org.hmx.scitos.view.IViewProject;
import org.hmx.scitos.view.service.IMultiModelProjectViewService;
import org.hmx.scitos.view.service.IProjectViewService;
import org.hmx.scitos.view.swing.MessageHandler;
import org.hmx.scitos.view.swing.ScitosClient;

/**
 * Implementation of the {@link IProjectViewService} for the AIS module.
 */
public class ProjectViewServiceImpl implements IMultiModelProjectViewService<AisViewProject, AisProject, Interview> {

    /** The associated active client instance, to forward to created views. */
    private final ScitosClient client;
    /** The preferences handler, providing the default detail category model for any new project. */
    private final AisOption options;

    /**
     * Main constructor.
     *
     * @param client
     *            associated active client instance
     * @param options
     *            preferences handler, providing the default detail category model for any new project
     */
    @Inject
    public ProjectViewServiceImpl(final ScitosClient client, final AisOption options) {
        this.client = client;
        this.options = options;
    }

    @Override
    public AisViewProject createEmptyProject() {
        final AisProject mainModel = new AisProject("", this.options.provide());
        final AisViewProject project = this.createProject(mainModel, null);
        project.setOpenTabElements(Arrays.asList(mainModel));
        return project;
    }

    @Override
    public AisViewProject createProject(final IModel<?> model, final File savePath) {
        return new AisViewProject(this.client, new ModelHandlerImpl((AisProject) model), savePath);
    }

    @Override
    public ContextMenuBuilder createContextMenu(final IViewProject<?> project, final Object element) {
        if (element == project) {
            // no additional popup entries for the project itself
            return null;
        }
        final ContextMenuBuilder menu = new ContextMenuBuilder(project.getLabel(element));
        menu.addItem(AisMessage.INTERVIEW_CHANGE_PARTICIPANTID.get(), new CMenuItemAction() {

            @Override
            public void processSelectEvent() {
                ProjectViewServiceImpl.this.renameParticipant((AisViewProject) project, element);
            }
        });
        if (element instanceof Interview) {
            menu.addSeparator();
            menu.addItem(AisMessage.INTERVIEW_DELETE.get(), new CMenuItemAction() {

                @Override
                public void processSelectEvent() {
                    if (MessageHandler.Choice.YES == MessageHandler.showConfirmDialog(AisMessage.INTERVIEW_DELETE_WARNING.get(),
                            AisMessage.INTERVIEW_DELETE.get() + " - " + project.getLabel(element))) {
                        ((AisViewProject) project).getModelHandler().deleteInterview((Interview) element);
                    }
                }
            });
        }
        return menu;
    }

    /**
     * Change a participant's id, while preserving its associated interviews in the given view project.
     *
     * @param project
     *            the view project to modify the participant's id in
     * @param element
     *            either the targeted participant's id or an interview associated to the participant being modified
     */
    void renameParticipant(final AisViewProject project, final Object element) {
        final String currentParticipantId;
        if (element instanceof Interview) {
            currentParticipantId = ((Interview) element).getParticipantId();
        } else {
            currentParticipantId = element.toString();
        }
        final String modifiedParticipantId =
                MessageHandler.showInputDialog(AisMessage.INTERVIEW_CHANGE_PARTICIPANTID_DESCRIPTION.get(),
                        AisMessage.INTERVIEW_CHANGE_PARTICIPANTID.get() + " - " + project.getLabel(element), currentParticipantId);
        if (modifiedParticipantId == null || modifiedParticipantId.trim().isEmpty() || currentParticipantId.equals(modifiedParticipantId)) {
            return;
        }
        if (element instanceof Interview) {
            project.getModelHandler().setParticipantId((Interview) element, modifiedParticipantId.trim());
        } else {
            project.getModelHandler().renameParticipant(currentParticipantId, modifiedParticipantId.trim());
        }
    }

    @Override
    public ProjectOverView createProjectView(final IViewProject<?> project) {
        return new ProjectOverView(this.client, (AisViewProject) project, this.options);
    }

    @Override
    public ParticipantInterviewGroupView createModelGroupView(final IViewProject<?> project, final String participantId) {
        return new ParticipantInterviewGroupView(this.client, (AisViewProject) project, participantId, this.options);
    }

    @Override
    public InterviewView createModelView(final IViewProject<?> project, final IModel<?> model) {
        return new InterviewView(this.client.getMainView(), (AisViewProject) project, (Interview) model, this.options);
    }
}
