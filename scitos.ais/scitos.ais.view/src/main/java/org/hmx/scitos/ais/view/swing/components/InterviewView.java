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

package org.hmx.scitos.ais.view.swing.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;

import org.hmx.scitos.ais.core.AisOption;
import org.hmx.scitos.ais.core.i18n.AisMessage;
import org.hmx.scitos.ais.domain.model.DetailCategory;
import org.hmx.scitos.ais.domain.model.Interview;
import org.hmx.scitos.ais.domain.model.TextToken;
import org.hmx.scitos.ais.view.swing.AisViewProject;
import org.hmx.scitos.view.swing.IUndoManagedView;
import org.hmx.scitos.view.swing.MainView;

/**
 * View representing a single {@link Interview autobiographical interview}, offering the input area for its text, and allowing the scoring of it
 * afterwards.
 */
public final class InterviewView extends AbstractAisProjectView<Interview> {

    /** The parent view containing this (sub) view as a tab. */
    private final MainView parentView;
    /**
     * The view content - either the {@link InterviewInputPanel} or {@link InterviewScoringPanel}.
     */
    private IUndoManagedView viewPanel;
    /** The main tool bar items that belong to this view – in order to enable/disable them according to the current selection. */
    private final List<JButton> detailToolBarItems = new LinkedList<>();

    /**
     * Main constructor.
     *
     * @param parent
     *            super ordinated view containing this (sub) view as a tab
     * @param project
     *            associated view project
     * @param interview
     *            represented interview instance
     * @param options
     *            preferences handler, providing the default detail category model for any new project
     */
    public InterviewView(final MainView parent, final AisViewProject project, final Interview interview, final AisOption options) {
        super(project, interview, options, new BorderLayout());
        this.parentView = parent;
        boolean inputMode = true;
        for (final TextToken singleParagraphStart : interview.getText()) {
            TextToken singleToken = singleParagraphStart;
            do {
                if (singleToken.getDetail() != null) {
                    inputMode = false;
                    break;
                }
                singleToken = singleToken.getFollowingToken();
            } while (singleToken != null);
        }
        if (inputMode) {
            this.viewPanel = new InterviewInputPanel(this);
        } else {
            this.viewPanel = new InterviewScoringPanel(this);
        }
        this.add((Component) this.viewPanel);
    }

    /** Transit from the text input mode to the scoring mode. */
    void initScoringMode() {
        this.viewPanel.submitChangesToModel();
        this.removeAll();
        this.viewPanel = new InterviewScoringPanel(this);
        this.add((Component) this.viewPanel);
        this.parentView.revalidateClient(true);
        this.refresh();
    }

    /**
     * Notify whole client about a change that might affect parts of the UI outside of this view's scope - e.g. an (possibly undo-able) edit occurred
     * and should be reflected in the client's menu bar and tool bar.
     */
    void revalidateClient() {
        this.parentView.revalidateClient(false);
    }

    @Override
    public void submitChangesToModel() {
        this.viewPanel.submitChangesToModel();
    }

    @Override
    public void refresh() {
        this.viewPanel.refresh();
    }

    /** Enable/disable the scoring related main tool bar items depending on the current view contents. */
    void handleToolBarOptions() {
        if (!this.detailToolBarItems.isEmpty()) {
            // check if the scoring panel is currently displayed and contains a selection that can be scored
            final boolean validSelection =
                    this.viewPanel instanceof InterviewScoringPanel && ((InterviewScoringPanel) this.viewPanel).containsValidSelection();
            for (final JButton detailCategoryButton : this.detailToolBarItems) {
                detailCategoryButton.setEnabled(validSelection);
            }
        }
    }

    @Override
    public List<Component> createToolBarItems() {
        this.detailToolBarItems.clear();
        // get AIS module's tool bar items
        final List<Component> toolBarItems = super.createToolBarItems();
        if (this.viewPanel instanceof InterviewScoringPanel) {
            // add separator between generic AIS module's tool bar items and actual detail categories for scoring
            toolBarItems.add(null);
            // create a tool bar item for each applicable detail category
            for (final DetailCategory singleCategory : this.getProject().getModelObject().provideSelectables()) {
                this.detailToolBarItems.add(this.createCategoryToolBarItem(singleCategory));
            }
            this.detailToolBarItems.add(this.createCategoryToolBarItem(null));
            toolBarItems.addAll(this.detailToolBarItems);
            toolBarItems.add(toolBarItems.size() - 1, null);
        }
        this.handleToolBarOptions();
        return toolBarItems;
    }

    /**
     * Create a single button for the main tool bar, that is assigning the specified detail category to all selected tokens on click.
     *
     * @param category
     *            detail category to assign when clicked (can be {@code null} to remove any category assignments)
     * @return create button for the main tool bar
     */
    private JButton createCategoryToolBarItem(final DetailCategory category) {
        final JButton button = new JButton(category == null ? AisMessage.SCORE_REMOVE.get() : category.getCode());
        button.setToolTipText(category == null ? AisMessage.SCORE_REMOVE_TOOLTIP.get() : category.getName());
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                InterviewView.this.assignDetailCategoryToSelectedTokens(category);
            }
        });
        return button;
    }

    /**
     * Assign the given category to all currently selected tokens.
     *
     * @param category
     *            detail category to assign (can be {@code null} to remove any category assignments)
     */
    void assignDetailCategoryToSelectedTokens(final DetailCategory category) {
        if (this.viewPanel instanceof InterviewScoringPanel) {
            ((InterviewScoringPanel) this.viewPanel).assignDetailCategoryToSelectedTokens(category);
        }
    }

    @Override
    public boolean canUndo() {
        return this.viewPanel.canUndo();
    }

    @Override
    public boolean canRedo() {
        return this.viewPanel.canRedo();
    }

    @Override
    public void undo() {
        this.viewPanel.undo();
    }

    @Override
    public void redo() {
        this.viewPanel.redo();
    }
}
