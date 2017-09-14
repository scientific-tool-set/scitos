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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.hmx.scitos.core.UndoManager;
import org.hmx.scitos.domain.ModelChangeListener;
import org.hmx.scitos.domain.ModelEvent;

import org.hmx.scitos.domain.util.ComparisonUtil;
import org.hmx.scitos.hmx.core.HmxModelHandler;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.core.option.HmxGeneralOption;
import org.hmx.scitos.hmx.domain.ICommentable;
import org.hmx.scitos.hmx.domain.ISemanticalRelationProvider;
import org.hmx.scitos.hmx.domain.model.AbstractConnectable;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.domain.model.RelationTemplate;
import org.hmx.scitos.hmx.view.IPericopeView;
import org.hmx.scitos.hmx.view.swing.elements.AbstractCommentable;
import org.hmx.scitos.view.swing.components.ScaledLabel;
import org.hmx.scitos.view.swing.components.ScaledTextPane;
import org.hmx.scitos.view.swing.util.VTextIcon;

/**
 * The view representation of a {@link Pericope} in the analysis mode – containing both the syntactical and semantical analysis.
 */
public final class CombinedAnalysesPanel extends JPanel implements IPericopeView, ModelChangeListener {

    /** The represented project's model handler instance. */
    private final HmxModelHandler modelHandler;
    /**
     * The provider of available semantical {@link RelationTemplate}s, to be offered via the elements' context menus.
     */
    private final ISemanticalRelationProvider relationProvider;
    /** The undo manager for the whole model. */
    private final UndoManager<Pericope> undoManager;
    /** Flag indicating that an un-do or re-do operation is currently in progress. */
    private boolean undoInProgress = false;

    /** The button to switch between the syntactical and semantical analysis. */
    private final JButton switchButton;
    /**
     * The vertical label for the {@link #switchButton} to switch from the syntactical to the semantical analysis.
     */
    private final VTextIcon semButtonIcon;
    /**
     * The vertical label for the {@link #switchButton} to switch from the semantical to the syntactical analysis.
     */
    private final VTextIcon synButtonIcon;
    /** The view component representing the semantical analysis. */
    private final SemAnalysisPanel semAnalysisView;
    /** The view component representing the syntactical analysis. */
    private final SynAnalysisPanel synAnalysisView;
    /**
     * The input area at the bottom of the view, allowing the display and modification of a selected element's comment.
     */
    private final JTextPane commentArea;

    /**
     * Flag indicating whether the label fields of propositions should be displayed or not.
     */
    private boolean showingPropositionLabels = HmxGeneralOption.SHOW_PROPOSITION_LABELS.getValueAsBoolean();
    /**
     * Flag indicating whether the translation fields of propositions should be displayed or not.
     */
    private boolean showingPropositionTranslations = HmxGeneralOption.SHOW_PROPOSITION_TRANSLATIONS.getValueAsBoolean();
    /**
     * The most recently selected commentable model element currently associated with the {@link #commentArea}.
     */
    private AbstractCommentable<?> lastSelectedCommentable;

    /**
     * Constructor.
     *
     * @param modelHandler
     *            the represented project's model handler instance
     * @param relationProvider
     *            the provider of available semantical {@link RelationTemplate}s, to be offered via the elements' context menus
     */
    public CombinedAnalysesPanel(final HmxModelHandler modelHandler, final ISemanticalRelationProvider relationProvider) {
        super(new GridBagLayout());
        this.modelHandler = modelHandler;
        this.relationProvider = relationProvider;
        this.undoManager = new UndoManager<Pericope>(this.modelHandler.getModel());
        this.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(final AncestorEvent event) {
                // ensure logging of model change events by the UndoManager
                modelHandler.addModelChangeListener(CombinedAnalysesPanel.this);
            }

            @Override
            public void ancestorRemoved(final AncestorEvent event) {
                /*
                 * unregister UndoManager as long as nothing is shown (nothing can be changed); this is to avoid multiple of these listeners if the
                 * respective tabs are being closed and re-opened repeatedly
                 */
                modelHandler.removeModelChangeListener(CombinedAnalysesPanel.this);
            }

            @Override
            public void ancestorMoved(final AncestorEvent event) {
                // we don't care about any movement
            }
        });

        this.setBorder(null);
        // initialize the commentArea to be reachable by commentable components
        this.commentArea = new ScaledTextPane();
        // build the analysis views representing the pericope
        this.semAnalysisView = new SemAnalysisPanel(this);
        this.synAnalysisView = new SynAnalysisPanel(this);

        this.switchButton = new JButton() {

            @Override
            public void updateUI() {
                super.updateUI();
                this.setFont(UIManager.getFont("Button.font"));
            }
        };
        this.switchButton.setName("Switch Analysis Button");
        // initialize the possible button icons
        this.semButtonIcon = new VTextIcon(this.switchButton, HmxMessage.ANALYSIS_SEMANTICAL_BUTTON.get(), VTextIcon.Rotate.NONE);
        this.synButtonIcon = new VTextIcon(this.switchButton, HmxMessage.ANALYSIS_SYNTACTICAL_BUTTON.get(), VTextIcon.Rotate.NONE);

        // default: always start with the syntactical analysis
        this.switchButton.setIcon(this.semButtonIcon);
        this.initView();
        this.synAnalysisView.activate();
    }

    /**
     * Initialize the whole layout.
     */
    private void initView() {
        // arrange the analysis views and the button for switching between them
        final JPanel topArea = new JPanel(new GridBagLayout());

        final GridBagConstraints doubleSpan = new GridBagConstraints();
        doubleSpan.fill = GridBagConstraints.BOTH;
        doubleSpan.weightx = 1;
        doubleSpan.weighty = 1;
        // add the semantical analysis to the left
        doubleSpan.gridx = 0;
        doubleSpan.gridy = 0;
        topArea.add(this.semAnalysisView, doubleSpan);
        // add the button for switching between the analysis to the mid
        this.switchButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                CombinedAnalysesPanel.this.changeActiveAnalysisView();
            }
        });
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.VERTICAL;
        constraints.weighty = 1;
        constraints.gridx = 1;
        constraints.gridy = 0;
        topArea.add(this.switchButton, constraints);

        // add the syntactical analysis to the right
        doubleSpan.gridx = 2;
        doubleSpan.gridy = 0;
        topArea.add(this.synAnalysisView, doubleSpan);
        // add the comment area for both views
        final JPanel commentPanel = new JPanel(new GridBagLayout());
        // initialize spacings
        final int sideSpacing = this.switchButton.getPreferredSize().width;
        commentPanel.setBorder(BorderFactory.createEmptyBorder(0, sideSpacing, 10, sideSpacing));
        final JLabel topicLabel = new ScaledLabel(HmxMessage.ANALYSIS_COMMENT_TOPIC.get());
        topicLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        commentPanel.add(topicLabel);
        this.commentArea.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        final JScrollPane scrollableComment = new JScrollPane(this.commentArea);
        scrollableComment.setBorder(BorderFactory.createLoweredBevelBorder());
        scrollableComment.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        doubleSpan.gridx = 0;
        doubleSpan.gridy = 1;
        commentPanel.add(scrollableComment, doubleSpan);
        commentPanel.setPreferredSize(new Dimension(1, 80));

        final JSplitPane splitArea = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topArea, commentPanel);
        splitArea.setBorder(null);
        splitArea.setResizeWeight(1);
        this.add(splitArea, doubleSpan);
    }

    @Override
    public void modelChanged(final ModelEvent<?> event) {
        // ignore change event thrown by the own undo/redo action
        if (!this.undoInProgress) {
            this.undoManager.undoableEditHappened(this.modelHandler.getModel());
        }
    }

    /**
     * Change the active analysis view by setting their visibility to make sure that only one analysis view is visible; and clear the shown comment.
     */
    void changeActiveAnalysisView() {
        // clear comment area
        this.handleSelectedCommentable(null);
        // make sure only one analysis view is visible at the end
        if (this.semAnalysisView.isShowing()) {
            this.synAnalysisView.activate();
            this.semAnalysisView.deactivate();
            this.switchButton.setIcon(this.semButtonIcon);
        } else {
            this.semAnalysisView.activate();
            this.synAnalysisView.deactivate();
            this.switchButton.setIcon(this.synButtonIcon);
        }
    }

    @Override
    public List<AbstractConnectable> getSelectedConnectables(final AbstractConnectable defaultSelected) {
        if (this.semAnalysisView.isShowing()) {
            return this.semAnalysisView.getChecked(defaultSelected);
        }
        if (defaultSelected == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(defaultSelected);
    }

    @Override
    public List<Proposition> getSelectedPropositions(final Proposition defaultSelected) {
        if (this.synAnalysisView.isVisible()) {
            return this.synAnalysisView.getChecked(defaultSelected);
        }
        if (defaultSelected == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(defaultSelected);
    }

    @Override
    public void handleSelectedCommentable(final AbstractCommentable<?> newSelected) {
        if (this.lastSelectedCommentable != null) {
            final String newCommentToSet = this.commentArea.getText();
            final ICommentable lastSelectedModelElement = this.lastSelectedCommentable.getRepresented();
            if (!ComparisonUtil.isNullOrEmptyAwareEqual(lastSelectedModelElement.getComment(), newCommentToSet)) {
                // store new comment in the last selected element
                this.getModelHandler().setComment(lastSelectedModelElement, newCommentToSet);
                this.lastSelectedCommentable.setToolTipText(newCommentToSet);
            }
            if (this.lastSelectedCommentable.isShowing()) {
                // reset non-selection border of the last selected
                this.lastSelectedCommentable.setDefaultBorder();
            }
        }
        this.lastSelectedCommentable = newSelected;
        if (newSelected == null) {
            this.commentArea.setText(null);
        } else {
            this.commentArea.setText(newSelected.getRepresented().getComment());
            newSelected.setCommentBorder();
        }
    }

    @Override
    public HmxModelHandler getModelHandler() {
        return this.modelHandler;
    }

    @Override
    public void submitChangesToModel() {
        final AbstractAnalysisPanel activeAnalysisView = this.getActiveAnalysisView();
        if (activeAnalysisView != null) {
            activeAnalysisView.submitChangesToModel();
        }
        // also take care of any newly entered comment specifically
        this.handleSelectedCommentable(null);
    }

    /**
     * Fully rebuild the currently displayed representation of the {@link Pericope}.
     */
    @Override
    public void refresh() {
        final AbstractAnalysisPanel activeAnalysisView = this.getActiveAnalysisView();
        if (activeAnalysisView != null) {
            activeAnalysisView.repaintPericope();
        }
    }

    @Override
    public boolean isShowingPropositionLabels() {
        return this.showingPropositionLabels;
    }

    @Override
    public boolean isShowingPropositionTranslations() {
        return this.showingPropositionTranslations;
    }

    /**
     * Toggle the visibility of the label fields for all propositions. This causes a full rebuild of the displayed Pericope.
     *
     * @see #refresh()
     */
    public void togglePropositionLabelVisibility() {
        this.showingPropositionLabels = !this.showingPropositionLabels;
        this.refresh();
    }

    /**
     * Toggle the visibility of the translation fields for all propositions. This causes a full rebuild of the displayed Pericope.
     *
     * @see #refresh()
     */
    public void togglePropositionTranslationVisibility() {
        this.showingPropositionTranslations = !this.showingPropositionTranslations;
        this.refresh();
    }

    /**
     * Determine the currently active analysis view (either {@link #synAnalysisView} or {@link #semAnalysisView}).
     *
     * @return currently active/displayed analysisView
     */
    private AbstractAnalysisPanel getActiveAnalysisView() {
        if (this.synAnalysisView.isShowing()) {
            return this.synAnalysisView;
        }
        if (this.semAnalysisView.isShowing()) {
            return this.semAnalysisView;
        }
        return null;
    }

    @Override
    public boolean canUndo() {
        return this.undoManager.canUndo();
    }

    @Override
    public boolean canRedo() {
        return this.undoManager.canRedo();
    }

    @Override
    public void undo() {
        // ensure that the any pending change is being reverted instead of the previously submit one
        this.submitChangesToModel();
        this.undoInProgress = true;
        try {
            this.getModelHandler().resetModel(this.undoManager.undo());
        } finally {
            this.undoInProgress = false;
        }
    }

    @Override
    public void redo() {
        // ignore any potentially pending change here (otherwise "Redo" might not be allowed anymore)
        this.undoInProgress = true;
        try {
            this.getModelHandler().resetModel(this.undoManager.redo());
        } finally {
            this.undoInProgress = false;
        }
    }

    @Override
    public List<List<RelationTemplate>> provideRelationTemplates() {
        return this.relationProvider.provideRelationTemplates();
    }
}
