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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.hmx.scitos.ais.core.AisOption;
import org.hmx.scitos.ais.domain.model.Interview;
import org.hmx.scitos.ais.view.swing.AisViewProject;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.domain.IProvider;
import org.hmx.scitos.view.ScitosIcon;
import org.hmx.scitos.view.swing.ScitosClient;
import org.hmx.scitos.view.swing.components.ScaledLabel;

/**
 * Tab view: representing a single participant, that has multiple interviews assigned.
 */
public final class ParticipantInterviewGroupView extends AbstractAisProjectView<String> {

    /** The normal, raised border around each interview in the list. */
    private static final Border BORDER_DEFAULT = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED),
            BorderFactory.createEmptyBorder(5, 5, 5, 5));
    /** The alternative, lowered border around a single interview, to indicate that it has been moved up/down last. */
    private static final Border BORDER_CLICKED = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),
            BorderFactory.createEmptyBorder(5, 5, 5, 5));

    /** The panel listing the actual interviews (excluding the topic label). */
    final JPanel contentArea;
    /**
     * Index of the interview that should be highlighted with the {@link #BORDER_CLICKED}, as it was the last target of an upward/downward movement.
     */
    int lastClickedIndex = -1;

    /**
     * Main constructor.
     *
     * @param client
     *            client instance containing this view as a tab in its MainView
     * @param project
     *            managed view project
     * @param participantId
     *            id of the represented participant
     * @param options
     *            preferences handler, providing the default detail category model for any new project
     */
    public ParticipantInterviewGroupView(final ScitosClient client, final AisViewProject project, final String participantId,
            final AisOption options) {
        super(project, participantId, options, new BorderLayout());
        final ScaledLabel topicLabel = new ScaledLabel(Message.AIS_INTERVIEW_CHANGE_ORDER.get());
        topicLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        this.add(topicLabel, BorderLayout.NORTH);
        this.contentArea = new JPanel(new GridBagLayout());
        this.contentArea.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 5));
        final JScrollPane scrollableContent = new JScrollPane(this.contentArea);
        scrollableContent.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollableContent.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollableContent.setBorder(null);
        this.add(scrollableContent);
        this.addHierarchyListener(new HierarchyListener() {

            @Override
            public void hierarchyChanged(final HierarchyEvent event) {
                if (ParticipantInterviewGroupView.this.isShowing()) {
                    ParticipantInterviewGroupView.this.refresh();
                } else {
                    ParticipantInterviewGroupView.this.lastClickedIndex = -1;
                    ParticipantInterviewGroupView.this.contentArea.removeAll();
                }
            }
        });
        this.refresh();
    }

    @Override
    public void refresh() {
        this.contentArea.removeAll();
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        final List<Interview> interviews = new ArrayList<Interview>(this.getProject().getModelObject().getSubModelObjects().get(this.getModel()));
        final int interviewCount = interviews.size();
        for (int listIndex = 0; listIndex < interviewCount; listIndex++) {
            constraints.gridy = listIndex;
            this.contentArea.add(this.createInterviewPanel(interviews.get(listIndex), interviewCount), constraints);
        }
        final JPanel spacing = new JPanel(null);
        spacing.setMinimumSize(new Dimension(1, 1));
        constraints.weighty = 1;
        constraints.gridy = interviewCount;
        this.contentArea.add(spacing, constraints);
    }

    /**
     * Create a panel representing the given interview and offering the option to adjust its index up and/or down.
     *
     * @param interview
     *            the interview to represent in the created panel
     * @param interviewCount
     *            total number of interviews, to hide the 'move down' button for the last interview
     * @return created panel
     */
    private JPanel createInterviewPanel(final Interview interview, final int interviewCount) {
        final int interviewIndex = interview.getIndex();
        final JPanel interviewWrapper = new JPanel(new GridBagLayout());
        // interviewPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        if (interviewIndex == this.lastClickedIndex) {
            interviewWrapper.setBorder(ParticipantInterviewGroupView.BORDER_CLICKED);
        } else {
            interviewWrapper.setBorder(ParticipantInterviewGroupView.BORDER_DEFAULT);
        }
        final GridLayout buttonLayout = new GridLayout(2, 1);
        buttonLayout.setVgap(5);
        final JPanel buttonGroup = new JPanel(buttonLayout);
        buttonGroup.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 15));
        final JButton moveUpButton = new JButton(ScitosIcon.ARROW_UP.create());
        if (interviewIndex == 1) {
            moveUpButton.setVisible(false);
        } else {
            moveUpButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent event) {
                    ParticipantInterviewGroupView.this.swapInterviews(interview, false);
                }
            });
        }
        buttonGroup.add(moveUpButton);
        final JButton moveDownButton = new JButton(ScitosIcon.ARROW_DOWN.create());
        if (interviewIndex == interviewCount) {
            moveDownButton.setVisible(false);
        } else {
            moveDownButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent event) {
                    ParticipantInterviewGroupView.this.swapInterviews(interview, true);
                }
            });
        }
        buttonGroup.add(moveDownButton);
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        interviewWrapper.add(buttonGroup, constraints);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridx = 1;
        final InterviewPanel interviewPanel = new InterviewPanel(new IProvider<Interview>() {

            @Override
            public Interview provide() {
                return interview;
            }
        }, this.getProject().getModelHandler());
        interviewPanel.setPreferredSize(new Dimension(1, 1));
        interviewWrapper.add(interviewPanel, constraints);
        return interviewWrapper;
    }

    /**
     * Adjust the given interview's index by one – either up or down, effectively swapping the index with previous or following interview of the same
     * participant.
     *
     * @param interview
     *            targeted interview to move up or down
     * @param swapWithFollowingInterview
     *            <code>true</code> – move interview down (index + 1)<br/>
     *            <code>false</code> – move interview up (index - 1)
     */
    void swapInterviews(final Interview interview, final boolean swapWithFollowingInterview) {
        final int newIndex;
        if (swapWithFollowingInterview) {
            newIndex = interview.getIndex() + 1;
        } else {
            newIndex = interview.getIndex() - 1;
        }
        // remember the new index of the modified interview, to highlight it after receiving the triggered model change event
        this.lastClickedIndex = newIndex;
        this.getProject().getModelHandler().setIndex(interview, newIndex);
    }

    @Override
    public void submitChangesToModel() {
        // nothing to do, since all changes are immediately transferred to the model
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
