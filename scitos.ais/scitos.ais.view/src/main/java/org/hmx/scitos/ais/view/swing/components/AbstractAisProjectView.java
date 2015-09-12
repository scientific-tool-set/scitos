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

import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuItem;

import org.hmx.scitos.ais.core.AisOption;
import org.hmx.scitos.ais.core.IModelHandler;
import org.hmx.scitos.ais.core.i18n.AisMessage;
import org.hmx.scitos.ais.domain.model.AisProject;
import org.hmx.scitos.ais.domain.model.Interview;
import org.hmx.scitos.ais.view.swing.AisViewProject;
import org.hmx.scitos.view.ScitosIcon;
import org.hmx.scitos.view.swing.AbstractProjectView;
import org.hmx.scitos.view.swing.MessageHandler;

/**
 * Abstract implementation of a view associated with the AIS module.
 *
 * @param <M>
 *            type of the represented user model element (either the {@link AisProject project} itself, a {@link String} representing a single
 *            participant's interview group, or an {@link Interview interview} instance)
 */
abstract class AbstractAisProjectView<M> extends AbstractProjectView<AisViewProject, M> {

    /** The provider of user defined preferences. */
    final AisOption options;

    /**
     * Main constructor.
     *
     * @param project
     *            associated project instance
     * @param model
     *            user model in the project to represent (either the {@link AisProject project} itself, a string representing a single participant's
     *            interview group, or an {@link Interview interview} instance)
     * @param options
     *            preferences handler, providing the default detail category model for any new project
     * @param layout
     *            layout manager to apply
     */
    protected AbstractAisProjectView(final AisViewProject project, final M model, final AisOption options, final LayoutManager layout) {
        super(project, model, layout);
        this.options = options;
    }

    @Override
    public List<JMenuItem> createEditMenuItems() {
        final List<JMenuItem> editMenuItems = new LinkedList<JMenuItem>();
        final JMenuItem changeCategoryModelItem = new JMenuItem(AisMessage.PROJECT_CHANGE_CATEGORIES.get(), ScitosIcon.CONFIG.create());
        changeCategoryModelItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                final IModelHandler modelHandler = AbstractAisProjectView.this.getProject().getModelHandler();
                final CategoryModelChangeDialog dialog = new CategoryModelChangeDialog(modelHandler, AbstractAisProjectView.this.options);
                dialog.setVisible(true);
            }
        });
        editMenuItems.add(changeCategoryModelItem);
        return editMenuItems;
    }

    @Override
    public List<Component> createToolBarItems() {
        final List<Component> toolBarItems = new LinkedList<Component>();
        final JButton addInterviewButton = new JButton(ScitosIcon.MODEL_ELEMENT_ADD.create());
        addInterviewButton.setToolTipText(AisMessage.INTERVIEW_NEW.get());
        addInterviewButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                AbstractAisProjectView.this.createInterview();
            }
        });
        toolBarItems.add(addInterviewButton);
        return toolBarItems;
    }

    /** Create a new interview and preset the participant id if possible. */
    protected void createInterview() {
        final M modelObject = this.getModel();
        final String preset;
        if (modelObject instanceof Interview) {
            preset = ((Interview) modelObject).getParticipantId();
        } else if (modelObject instanceof String) {
            preset = (String) modelObject;
        } else {
            preset = "";
        }
        final String participantId =
                MessageHandler.showInputDialog(AisMessage.INTERVIEW_NEW_PARTICIPANTID.get(), AisMessage.INTERVIEW_NEW.get(), preset);
        if (participantId != null && !participantId.trim().isEmpty()) {
            this.getProject().getModelHandler().createInterview(participantId.trim());
        }
    }
}