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
import java.util.ArrayList;
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
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.hmx.scitos.core.UndoManager;

import org.hmx.scitos.domain.ModelChangeListener;
import org.hmx.scitos.domain.ModelEvent;
import org.hmx.scitos.domain.util.CollectionUtil;
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
import org.hmx.scitos.hmx.view.swing.elements.ViewRelation;
import org.hmx.scitos.hmx.view.swing.elements.ViewProposition;
import org.hmx.scitos.hmx.view.swing.elements.ViewRelationExtender;
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
     * Additional panel ensuring the trailing alignment of the propositions while relations are shown.
     */
    private final JPanel leftContentSpacing = new JPanel(null);
    /**
     * Additional panel ensuring the leading alignment of the propositions while relations are hidden.
     */
    private final JPanel rightContentSpacing = new JPanel(null);
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
     * @param modelHandler the represented project's model handler instance
     * @param relationProvider the provider of available semantical {@link RelationTemplate}s, to be offered via the elements' context menus
     * @param viewSettings user settings determining what parts to show
     */
    protected AnalysisPanel(final HmxModelHandler modelHandler, final ISemanticalRelationProvider relationProvider,
            final IAnalysisViewSettings viewSettings) {
        super(new GridLayout(0, 1));
        this.modelHandler = modelHandler;
        this.relationProvider = relationProvider;
        this.viewSettings = viewSettings;
        this.undoManager = new UndoManager<Pericope>(modelHandler.getModel());

        this.scrollPane = this.initScrollableContent();
        // initialize the commentArea to be reachable by commentable components
        this.commentArea = new ScaledTextPane();

        final JSplitPane splitArea = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.scrollPane, this.initCommentPanel());
        splitArea.setBorder(null);
        splitArea.setResizeWeight(1);
        this.add(splitArea);
        this.setBorder(null);

        this.refresh();
        this.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(final AncestorEvent event) {
                // ensure logging of model change events by the UndoManager
                modelHandler.addModelChangeListener(AnalysisPanel.this);
                // reset size of comment area on every tab change; TODO handle this more elegantly in the future
                splitArea.setDividerLocation(-1);
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
    }

    /**
     * Initialize the whole layout.
     *
     * @return the created scroll pane
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
        bottomSpace.weighty = 1;
        bottomSpace.gridx = 1;
        bottomSpace.gridy = 1;
        background.add(new JPanel(null), bottomSpace);
        final GridBagConstraints rightSpace = new GridBagConstraints();
        rightSpace.weightx = 1;
        rightSpace.gridx = 2;
        rightSpace.gridy = 0;
        background.add(this.rightContentSpacing, rightSpace);
        // wrap the headers to get equal behavior
        final JPanel headerView = new JPanel(new GridBagLayout());
        // make sure it is always on the right
        final GridBagConstraints leftSpace = new GridBagConstraints();
        leftSpace.weightx = 1;
        leftSpace.gridx = 0;
        leftSpace.gridy = 0;
        background.add(this.leftContentSpacing, leftSpace);
        headerView.add(new JPanel(null), leftSpace);
        headerView.add(this.contentHeaders, mainConstraints);
        headerView.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
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
        scrollablePane.setBorder(null);
        scrollablePane.setColumnHeaderView(headerView);
        scrollablePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        return scrollablePane;
    }

    /**
     * Initialize the comment area at the bottom.
     *
     * @return panel containing the scrollable comment text area
     */
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
        doubleSpan.weightx = 1;
        doubleSpan.weighty = 1;
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
            this.undoManager.undoableEditHappened(this.getModelHandler().getModel());
        }
        // deal with different kinds of targets and event types
        final Object target = event.getTarget();
        SwingUtilities.invokeLater(new Thread("HmX View Model Update") {

            @Override
            public void run() {
                if (target instanceof ClauseItem) {
                    final ViewProposition targetParent = AnalysisPanel.this.getRepresentative(((ClauseItem) target).getParent());
                    if (targetParent != null) {
                        targetParent.refreshClauseItem((ClauseItem) target);
                        return;
                    }
                } else if (target instanceof Proposition && AnalysisPanel.this.getRepresentative((Proposition) target).refresh()) {
                    return;
                }
                AnalysisPanel.this.refresh();
            }
        });
    }

    /**
     * Fully rebuild the displayed representation of the current {@link Pericope}.
     */
    @Override
    public void refresh() {
        // remember vertical position
        final int verticalPosition = this.scrollPane.getVerticalScrollBar().getValue();
        // clear view
        this.submitChangesToModel();
        this.setVisible(false);
        final boolean showingRelations = this.viewSettings.isShowingRelations();
        this.leftContentSpacing.setVisible(showingRelations);
        this.rightContentSpacing.setVisible(!showingRelations);
        this.contentArea.removeAll();
        // get the currently used origin text font
        this.propositionList = new ArrayList<ViewProposition>();
        // fill the propositionList
        int propositionIndexOffset = 0;
        for (final Proposition singleTopLevelProposition : this.getModelHandler().getModel().getText()) {
            propositionIndexOffset = this.addViewPropositionToList(singleTopLevelProposition, propositionIndexOffset, 0);
        }
        // show pericope
        this.levels = this.calculateLevels();
        this.displayPropositions();
        this.relationMap = new HashMap<Relation, ViewRelation>();
        if (this.viewSettings.isShowingRelations()) {
            this.displayRelations();
        }
        this.resetHeaders();
        // reset vertical position
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                AnalysisPanel.this.setVisible(true);
                AnalysisPanel.this.scrollPane.getVerticalScrollBar().setValue(verticalPosition);
            }
        });
    }

    /**
     * Transfer the {@link ViewProposition}s contained in the {@link #propositionList} to the displayed view.
     */
    private void displayPropositions() {
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        constraints.gridx = Math.max(1, this.levels);
        constraints.gridy = 0;
        for (final ViewProposition singleProposition : this.propositionList) {
            this.contentArea.add(singleProposition, constraints);
            constraints.gridy++;
        }
    }

    /**
     * Build the {@link #relationMap} and display it; assuming the already created {@link #propositionList}.
     */
    private void displayRelations() {
        ViewProposition singleProposition = this.propositionList.get(0);
        while (singleProposition != null) {
            Relation singleRelation = singleProposition.getRepresented().getSuperOrdinatedRelation();
            final Proposition follower;
            if (singleRelation == null) {
                follower = singleProposition.getRepresented().getFollowingConnectableProposition();
            } else {
                // get the highest relation over the singleProposition
                while (singleRelation.getSuperOrdinatedRelation() != null) {
                    singleRelation = singleRelation.getSuperOrdinatedRelation();
                }
                // add highest and all of its subordinated relations in map
                this.insertRelationTree(singleRelation);
                follower = singleRelation.getLastPropositionContained().getFollowingConnectableProposition();
            }
            if (follower == null) {
                break;
            }
            singleProposition = this.getRepresentative(follower);
        }
    }

    /**
     * Display the specified {@link Relation} and all of its subordinated {@link AbstractConnectable}s.
     *
     * @param relation
     *            {@link Relation} to display
     */
    private void insertRelationTree(final Relation relation) {
        // insert all subordinated relations first
        for (final AbstractConnectable singleAssociate : relation) {
            if (singleAssociate instanceof Relation) {
                this.insertRelationTree((Relation) singleAssociate);
            }
        }
        final ViewRelation viewRepresentative = new ViewRelation(this, relation, this.foldedLevels);
        // insert the relation itself in the map
        this.relationMap.put(relation, viewRepresentative);
        // build constraints
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridheight = (int) (viewRepresentative.getLastGridY() - viewRepresentative.getFirstGridY() + 1);
        constraints.gridx = this.levels - viewRepresentative.getDepth();
        constraints.gridy = (int) (viewRepresentative.getFirstGridY() - 0.5);
        // display
        this.contentArea.add(viewRepresentative, constraints);
        viewRepresentative.setVisible(false);
        viewRepresentative.setVisible(true);
        // extend lines if there is a gap between relation and its associates
        for (final AbstractConnectable singleAssociate : relation) {
            int depth = viewRepresentative.getDepth() - 1;
            final IConnectable<?> associateRepresentative = this.getRepresentative(singleAssociate);
            final double connectY = associateRepresentative.getConnectY();
            if ((connectY % 1) == 0) {
                constraints.gridheight = 2;
            } else {
                constraints.gridheight = 1;
            }
            constraints.gridy = (int) (connectY - 0.5);
            while (depth > associateRepresentative.getDepth()) {
                constraints.gridx = this.levels - depth;
                this.contentArea.add(new ViewRelationExtender(), constraints);
                depth--;
            }
        }
    }

    /**
     * Calculate the maximum number of super ordinated {@link Relation}s.
     *
     * @return calculated maximum level
     */
    private int calculateLevels() {
        int max = 0;
        for (final ViewProposition singleProposition : this.propositionList) {
            Relation singleSuperordinated = singleProposition.getRepresented().getSuperOrdinatedRelation();
            int depth;
            for (depth = 0; singleSuperordinated != null; depth++) {
                singleSuperordinated = singleSuperordinated.getSuperOrdinatedRelation();
            }
            max = Math.max(max, depth);
        }
        return (max + 1);
    }

    /**
     * Add the specified {@link Proposition} to the list of {@link ViewProposition}s WITHOUT adding it to the view.
     *
     * @param proposition {@link Proposition} to add to list
     * @param offset next free proposition index
     * @param level level of nested indentation of the given proposition towards the top-level propositions (which are on level 0)
     * @return new {@code offset}, i.e. the highest now occupied proposition index + 1
     */
    private int addViewPropositionToList(final Proposition proposition, final int offset, final int level) {
        // first: add all prior children
        final List<Proposition> priorChildren = proposition.getPriorChildren();
        int propositionOffset = offset;
        if (priorChildren != null) {
            for (final Proposition singlePriorChild : priorChildren) {
                propositionOffset = this.addViewPropositionToList(singlePriorChild, propositionOffset, level + 1);
            }
        }
        // second: add the proposition itself
        final Proposition partBeforeArrow = proposition.getPartBeforeArrow();
        final ViewProposition viewProposition;
        if (partBeforeArrow == null) {
            viewProposition = ViewProposition.createSynPropositionByLevel(this, proposition, propositionOffset, level);
        } else {
            final ViewProposition viewPartBeforeArrow = this.getRepresentative(partBeforeArrow);
            viewProposition = ViewProposition.createSynPropositionByPartBeforeArrow(this, proposition, propositionOffset, viewPartBeforeArrow);
            // show arrows
            final int beforeArrowPos = CollectionUtil.indexOfInstance(this.propositionList, viewPartBeforeArrow);
            final List<ViewProposition> propositionsAfterPriorPart = this.propositionList.subList(beforeArrowPos + 1, this.propositionList.size());
            // count number of arrows to set
            int arrowCount = 0;
            // ignore partAfterArrows
            for (final ViewProposition enclosedProposition : propositionsAfterPriorPart) {
                if (enclosedProposition == viewProposition) {
                    break;
                }
                if (enclosedProposition.getRepresented().getPartBeforeArrow() == null) {
                    arrowCount++;
                }
            }
            // show arrows in part before arrow and target
            viewPartBeforeArrow.setRightArrowCount(arrowCount);
            viewProposition.setLeftArrowCount(arrowCount);
        }
        propositionOffset++;
        this.propositionList.add(viewProposition);
        // third: add all later children
        final List<Proposition> laterChildren = proposition.getLaterChildren();
        if (laterChildren != null) {
            for (final Proposition singleLaterChild : laterChildren) {
                propositionOffset = this.addViewPropositionToList(singleLaterChild, propositionOffset, level + 1);
            }
        }
        // finally: add the part after arrow
        final Proposition partAfterArrow = proposition.getPartAfterArrow();
        if (partAfterArrow != null) {
            propositionOffset = this.addViewPropositionToList(partAfterArrow, propositionOffset, level);
        }
        return propositionOffset;
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

    /**
     * Find the view representation of the given connectable (i.e. proposition or relation).
     *
     * @param target model element
     * @return view representation
     */
    public IConnectable<?> getRepresentative(final AbstractConnectable target) {
        if (target instanceof Proposition) {
            return this.getRepresentative((Proposition) target);
        } else if (target instanceof Relation) {
            return this.getRepresentative((Relation) target);
        }
        // unsupported model object
        throw new IllegalArgumentException();
    }

    /**
     * Find the view representation of the given proposition.
     *
     * @param target model element
     * @return view representation
     */
    ViewProposition getRepresentative(final Proposition target) {
        for (final ViewProposition singleProposition : this.propositionList) {
            if (singleProposition.getRepresented() == target) {
                return singleProposition;
            }
        }
        return null;
    }

    /**
     * Find the view representation of the given relation.
     *
     * @param target model element
     * @return view representation
     */
    ViewRelation getRepresentative(final Relation target) {
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
    void resetHeaders() {
        // remove old headers
        this.contentHeaders.removeAll();
        // no relations mean no headers
        final boolean showHeaders = this.levels > 1 && this.viewSettings.isShowingRelations();
        this.contentHeaders.setVisible(showHeaders);
        if (!showHeaders) {
            return;
        }
        // calculate width of each column
        final Map<Integer, Integer> maxColumnSize = new HashMap<Integer, Integer>();
        final GridBagLayout contentLayout = (GridBagLayout) this.contentArea.getLayout();
        // check each component for its width and determine maximum per column
        for (final Component singleComponent : this.contentArea.getComponents()) {
            final int column = contentLayout.getConstraints(singleComponent).gridx;
            final int componentWidth = singleComponent.getPreferredSize().width;
            final int columnSize;
            if (maxColumnSize.containsKey(column)) {
                columnSize = Math.max(componentWidth, maxColumnSize.get(column));
            } else {
                columnSize = componentWidth;
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
