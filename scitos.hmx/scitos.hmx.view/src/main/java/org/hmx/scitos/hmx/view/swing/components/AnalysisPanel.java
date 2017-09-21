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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.hmx.scitos.core.UndoManager;

import org.hmx.scitos.domain.ModelChangeListener;
import org.hmx.scitos.domain.ModelEvent;
import org.hmx.scitos.domain.util.ComparisonUtil;
import org.hmx.scitos.hmx.core.HmxModelHandler;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.domain.ICommentable;
import org.hmx.scitos.hmx.domain.ISemanticalRelationProvider;
import org.hmx.scitos.hmx.domain.model.AbstractConnectable;
import org.hmx.scitos.hmx.domain.model.ClauseItem;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.domain.model.Relation;
import org.hmx.scitos.hmx.domain.model.RelationTemplate;
import org.hmx.scitos.hmx.view.IPericopeView;
import org.hmx.scitos.hmx.view.swing.elements.AbstractCommentable;
import org.hmx.scitos.hmx.view.swing.elements.IConnectable;
import org.hmx.scitos.hmx.view.swing.elements.ViewClauseItem;
import org.hmx.scitos.hmx.view.swing.elements.ViewRelation;
import org.hmx.scitos.hmx.view.swing.elements.ViewProposition;
import org.hmx.scitos.view.swing.components.ScaledLabel;
import org.hmx.scitos.view.swing.components.ScaledTextPane;

/**
 * Panel displaying the whole pericope for the actual analysis.
 */
public final class AnalysisPanel extends JPanel implements IPericopeView, ModelChangeListener {

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

    /**
     * The current maximum depth of the represented tree of {@link Relation}s.
     */
    private int levels = 0;
    /** The single main component allowing everything to be scrolled. */
    final JScrollPane scrollPane;
    /** The actual container for view elements. */
    private final JPanel contentArea = new JPanel(new GridBagLayout());
    /**
     * The header bar allowing to expand/collapse {@link Relation} columns by toggling the roles' visibility.
     */
    final JPanel contentHeaders = new JPanel(new GridBagLayout()) {

        @Override
        public void updateUI() {
            if (AnalysisPanel.this.contentHeaders != null) {
                // reset to apply potentially changed scale factor
                AnalysisPanel.this.resetHeaders();
            }
        }
    };
    /**
     * Additional panels ensuring the trailing alignment of the propositions while relations are being displayed.
     */
    private final JPanel[] relationSpacings = new JPanel[] { new JPanel(null), new JPanel(null) };
    /**
     * The input area at the bottom of the view, allowing the display and modification of a selected element's comment.
     */
    private final JTextPane commentArea;

    /**
     * View settings indicating what parts of the analysis should be displayed or not.
     */
    private final IAnalysisViewSettings viewSettings;
    /**
     * The indices of currently collapsed {@link Relation} columns, i.e. where the associate roles are currently hidden to reduce required horizontal
     * space.
     */
    private final Set<Integer> foldedLevels = new HashSet<Integer>();
    /**
     * Complete list of currently contained view components representing the {@link Pericope}'s {@link Proposition}s.
     */
    private List<ViewProposition> propositionList;
    /**
     * Complete mapping of {@link Relation}s to their representing view components.
     */
    private Map<Relation, ViewRelation> relationMap;
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
    protected AnalysisPanel(final HmxModelHandler modelHandler, final ISemanticalRelationProvider relationProvider,
            final IAnalysisViewSettings viewSettings) {
        super(new GridLayout(0, 1));
        this.modelHandler = modelHandler;
        this.relationProvider = relationProvider;
        this.viewSettings = viewSettings;
        this.undoManager = new UndoManager<Pericope>(this.modelHandler.getModel());
        this.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(final AncestorEvent event) {
                // ensure logging of model change events by the UndoManager
                modelHandler.addModelChangeListener(AnalysisPanel.this);
            }

            @Override
            public void ancestorRemoved(final AncestorEvent event) {
                /*
                 * unregister UndoManager as long as nothing is shown (nothing can be changed); this is to avoid multiple of these listeners if the
                 * respective tabs are being closed and re-opened repeatedly
                 */
                modelHandler.removeModelChangeListener(AnalysisPanel.this);
            }

            @Override
            public void ancestorMoved(final AncestorEvent event) {
                // we don't care about any movement
            }
        });

        this.setBorder(null);
        this.scrollPane = this.initScrollableContent();
        // initialize the commentArea to be reachable by commentable components
        this.commentArea = new ScaledTextPane();

        final JSplitPane splitArea = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.scrollPane, this.initCommentPanel());
        splitArea.setBorder(null);
        splitArea.setResizeWeight(1);
        final GridBagConstraints doubleSpan = new GridBagConstraints();
        doubleSpan.fill = GridBagConstraints.BOTH;
        doubleSpan.gridx = 0;
        doubleSpan.gridy = 1;
        this.add(splitArea, doubleSpan);
    }

    /**
     * Initialize the whole layout.
     */
    private JScrollPane initScrollableContent() {
        // make the whole analysis view scrollable
        final JPanel background = new JPanel(new GridBagLayout());
        // initialize content area
        final GridBagConstraints mainConstraints = new GridBagConstraints();
        mainConstraints.anchor = GridBagConstraints.FIRST_LINE_END;
        mainConstraints.fill = GridBagConstraints.BOTH;
        mainConstraints.gridx = 1;
        mainConstraints.gridy = 0;
        background.add(this.contentArea, mainConstraints);
        // make sure it is always at the top
        final GridBagConstraints bottomSpace = new GridBagConstraints();
        bottomSpace.fill = GridBagConstraints.VERTICAL;
        bottomSpace.weighty = 1;
        bottomSpace.gridx = 1;
        bottomSpace.gridy = 1;
        background.add(new JPanel(null), bottomSpace);
        // wrap the headers to get equal behavior
        final JPanel headerView = new JPanel(new GridBagLayout());
        // make sure it is always on the right
        final GridBagConstraints leftSpace = new GridBagConstraints();
        leftSpace.fill = GridBagConstraints.HORIZONTAL;
        leftSpace.weightx = 1;
        leftSpace.gridx = 0;
        leftSpace.gridy = 0;
        background.add(relationSpacings[0], leftSpace);
        headerView.add(relationSpacings[1], leftSpace);
        headerView.add(this.contentHeaders, mainConstraints);
        headerView.setBorder(new MatteBorder(0, 0, 1, 0, Color.BLACK));
        final ComponentOrientation orientation;
        if (this.getModelHandler().getModel().isLeftToRightOriented()) {
            orientation = ComponentOrientation.LEFT_TO_RIGHT;
        } else {
            orientation = ComponentOrientation.RIGHT_TO_LEFT;
        }
        background.setComponentOrientation(orientation);
        this.contentArea.setComponentOrientation(orientation);
        headerView.setComponentOrientation(orientation);
        this.contentHeaders.setComponentOrientation(orientation);

        final JScrollPane scrollablePane = new JScrollPane(background);
        scrollablePane.setColumnHeaderView(headerView);
        scrollablePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(scrollablePane);
        return scrollablePane;
    }

    private JPanel initCommentPanel() {
        // add the comment area for both views
        final JPanel commentPanel = new JPanel(new GridBagLayout());
        commentPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5));
        final JLabel topicLabel = new ScaledLabel(HmxMessage.ANALYSIS_COMMENT_TOPIC.get());
        topicLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        commentPanel.add(topicLabel);
        this.commentArea.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        final JScrollPane scrollableComment = new JScrollPane(this.commentArea);
        scrollableComment.setBorder(BorderFactory.createLoweredBevelBorder());
        scrollableComment.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        final GridBagConstraints doubleSpan = new GridBagConstraints();
        doubleSpan.fill = GridBagConstraints.BOTH;
        doubleSpan.gridx = 0;
        doubleSpan.gridy = 1;
        commentPanel.add(scrollableComment, doubleSpan);
        commentPanel.setPreferredSize(new Dimension(1, 80));
        return commentPanel;
    }

    @Override
    public HmxModelHandler getModelHandler() {
        return this.modelHandler;
    }

    @Override
    public IAnalysisViewSettings getViewSettings() {
        return this.viewSettings;
    }

    @Override
    public void modelChanged(final ModelEvent<?> event) {
        // ignore change event thrown by the own undo/redo action
        if (!this.undoInProgress) {
            this.undoManager.undoableEditHappened(this.modelHandler.getModel());
        }
        // deal with different kinds of targets and event types
        final Object target = event.getTarget();
        SwingUtilities.invokeLater(new Thread("HmX View Model Update") {

            @Override
            public void run() {
                final ViewProposition proposition;
                if (target instanceof ClauseItem) {
                    proposition = AnalysisPanel.this.getRepresentative(((ClauseItem) target).getParent());
                    if (proposition != null) {
                        for (final ViewClauseItem singleItem : proposition.getItems()) {
                            if (singleItem.getRepresented() == target) {
                                singleItem.refresh();
                                return;
                            }
                        }
                    }
                } else if (target instanceof Proposition) {
                    proposition = AnalysisPanel.this.getRepresentative((Proposition) target);
                } else {
                    proposition = null;
                }
                if (proposition == null || !proposition.refresh()) {
                    AnalysisPanel.this.refresh();
                }
            }
        });
    }

    /**
     * Fully rebuild the displayed representation of the current {@link Pericope}.
     */
    public void refresh() {
        final boolean showingRelations = this.viewSettings.isShowingRelations();
        this.relationSpacings[0].setVisible(showingRelations);
        this.relationSpacings[1].setVisible(showingRelations);
    }

    @Override
    public void submitChangesToModel() {
        // the propositions might have pending changes (e.g. the label and translation fields)
        if (this.propositionList != null) {
            for (final ViewProposition singleProposition : this.propositionList) {
                singleProposition.submitChangesToModel();
            }
        }
        // also take care of any newly entered comment specifically
        this.handleSelectedCommentable(null);
    }

    @Override
    public List<Proposition> getSelectedPropositions(final Proposition defaultSelected) {
        final List<Proposition> result = new LinkedList<Proposition>();
        for (ViewProposition singleProposition : this.propositionList) {
            if (singleProposition.isChecked() || singleProposition.getRepresented() == defaultSelected) {
                result.add(singleProposition.getRepresented());
            }
        }
        return result;
    }

    @Override
    public List<AbstractConnectable> getSelectedConnectables(final AbstractConnectable defaultSelected) {
        final List<AbstractConnectable> list = new LinkedList<AbstractConnectable>();
        // REQUIREMENT: whole model is represented in the view
        AbstractConnectable nextToCheck = this.propositionList.get(0).getRepresented();
        while (nextToCheck != null) {
            // get checkable element
            while (nextToCheck.getSuperOrdinatedRelation() != null) {
                nextToCheck = nextToCheck.getSuperOrdinatedRelation();
            }
            if (nextToCheck == defaultSelected || this.getRepresentative(nextToCheck).isChecked()) {
                list.add(nextToCheck);
            }
            // get next element
            nextToCheck = nextToCheck.getFollowingConnectableProposition();
        }
        return list;
    }

    IConnectable<?> getRepresentative(AbstractConnectable target) {
        if (target instanceof Proposition) {
            return this.getRepresentative((Proposition) target);
        } else if (target instanceof Relation) {
            return this.getRepresentative((Relation) target);
        }
        // unsupported model object
        throw new IllegalArgumentException();
    }

    ViewProposition getRepresentative(Proposition target) {
        for (final ViewProposition singleProposition : this.propositionList) {
            if (singleProposition.getRepresented() == target) {
                return singleProposition;
            }
        }
        return null;
    }

    ViewRelation getRepresentative(Relation target) {
        return this.relationMap.get(target);
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

    /** Make sure the headers for expanding/collapsing the relations (hide/show roles) are present and sized properly. */
    public void resetHeaders() {
        // remove old headers
        this.contentHeaders.removeAll();
        if (this.levels < 2) {
            // no relations mean no headers
            this.contentHeaders.setVisible(false);
            return;
        }
        this.contentHeaders.setVisible(true);
        // calculate width of each column
        final Map<Integer, Integer> maxColumnSize = new HashMap<Integer, Integer>();
        final GridBagLayout contentLayout = (GridBagLayout) this.contentArea.getLayout();
        // check each component for its width and determine maximum per column
        for (final Component singleComponent : this.contentArea.getComponents()) {
            final int column = contentLayout.getConstraints(singleComponent).gridx;
            int columnSize = singleComponent.getPreferredSize().width;
            if (maxColumnSize.containsKey(column)) {
                columnSize = Math.max(columnSize, maxColumnSize.get(column));
            }
            maxColumnSize.put(column, columnSize);
        }
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = this.levels;
        // no folding/collapsing for propositions
        final boolean foldAll = this.foldedLevels.isEmpty();
        final HmxMessage messageKey = foldAll ? HmxMessage.MENU_FOLD_RELATION_ALL : HmxMessage.MENU_UNFOLD_RELATION_ALL;
        final JButton foldAllButton = new JButton(messageKey.get());
        foldAllButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                AnalysisPanel.this.foldRelationRolesOnLevel(-1, foldAll);
            }
        });
        foldAllButton.setPreferredSize(new Dimension(maxColumnSize.get(this.levels), foldAllButton.getPreferredSize().height));
        this.contentHeaders.add(foldAllButton, constraints);

        final String foldToolTip = HmxMessage.MENU_FOLD_RELATION_LEVEL.get();
        final String unfoldToolTip = HmxMessage.MENU_UNFOLD_RELATION_LEVEL.get();
        // insert fold/collapse button for each relation level
        for (int level = 1; level < this.levels; level++) {
            final boolean fold = !this.foldedLevels.contains(level);
            final JPanel headerPanel = new JPanel();
            final JCheckBox box = new JCheckBox();
            box.setSelected(fold);
            box.setToolTipText(fold ? foldToolTip : unfoldToolTip);
            final int levelIndex = level;
            box.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent event) {
                    AnalysisPanel.this.foldRelationRolesOnLevel(levelIndex, fold);
                }
            });
            headerPanel.add(box, BorderLayout.CENTER);
            constraints.gridx--;
            // apply width of each column to its header
            headerPanel.setPreferredSize(new Dimension(maxColumnSize.get(constraints.gridx), headerPanel.getPreferredSize().height));
            this.contentHeaders.add(headerPanel, constraints);
        }
    }

    /**
     * Hide/show the associate roles on the {@link Relation} column at the given index.
     *
     * @param level
     *            single relation level to fold/unfold (targets all levels, when is {@code -1})
     * @param fold
     *            if the semantic roles of the indicated {@code level} should be hidden
     */
    public void foldRelationRolesOnLevel(final int level, final boolean fold) {
        final boolean onlySelectedLevel = -1 != level;
        if (fold && onlySelectedLevel) {
            if (this.foldedLevels.contains(level)) {
                // nothing changed
                return;
            }
            this.foldedLevels.add(level);
        } else if (fold) {
            for (int i = 0; i < this.levels; i++) {
                this.foldedLevels.add(i);
            }
        } else if (onlySelectedLevel) {
            this.foldedLevels.remove(level);
        } else {
            this.foldedLevels.clear();
        }
        final GridBagLayout headerLayout = (GridBagLayout) this.contentHeaders.getLayout();
        for (final Component singleHeader : this.contentHeaders.getComponents()) {
            final int currentLevel = this.levels - headerLayout.getConstraints(singleHeader).gridx;
            if (singleHeader instanceof JCheckBox && level == currentLevel) {
                final boolean folded = this.foldedLevels.contains(currentLevel);
                final JCheckBox box = (JCheckBox) singleHeader;
                if (box.isSelected() == folded) {
                    box.setSelected(!folded);
                }
                break;
            }
        }
        this.refresh();
    }
}
