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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;

import org.hmx.scitos.domain.ModelChangeListener;
import org.hmx.scitos.domain.util.CollectionUtil;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.domain.model.AbstractConnectable;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.domain.model.Relation;
import org.hmx.scitos.hmx.view.IPericopeView;
import org.hmx.scitos.hmx.view.swing.elements.IConnectable;
import org.hmx.scitos.hmx.view.swing.elements.SemProposition;
import org.hmx.scitos.hmx.view.swing.elements.ViewRelation;
import org.hmx.scitos.hmx.view.swing.elements.ViewRelationExtender;

/**
 * Semantical analysis view displaying the semantical structured analysis consisting of {@link Proposition}s and {@link Relation}s. This is
 * disregarding the syntactical functions, indentations, individual ClauseItems and syntactical translations in the {@link Proposition}s. Instead,
 * offering the opportunity to create, edit and remove {@link Relation}s.
 */
public final class SemAnalysisPanel extends AbstractAnalysisPanel {

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
            if (SemAnalysisPanel.this.contentHeaders != null) {
                // reset to apply potentially changed scale factor
                SemAnalysisPanel.this.resetHeaders();
            }
        }
    };
    /** The model change listener responsible for keeping this panel up to date as long as it is active. */
    private final SemControl listener;
    /**
     * The indices of currently collapsed {@link Relation} columns, i.e. where the associate roles are currently hidden to reduce required horizontal
     * space.
     */
    private final Set<Integer> foldedLevels = new HashSet<Integer>();
    /**
     * Complete list of currently contained view components representing the {@link Pericope}'s {@link Proposition}s.
     */
    private List<SemProposition> propositionList;
    /**
     * Complete mapping of {@link Relation}s to their representing view components.
     */
    private Map<Relation, ViewRelation> relationMap;

    /**
     * Constructor.
     *
     * @param viewReference
     *            super ordinated view this panel belongs to
     */
    public SemAnalysisPanel(final IPericopeView viewReference) {
        super(viewReference, new GridLayout(0, 1));
        this.setVisible(false);
        this.listener = new SemControl(this);
        this.scrollPane = this.initView();
    }

    @Override
    protected ModelChangeListener getModelChangeListener() {
        return this.listener;
    }

    /**
     * Initialize the general layout and position of the content to display.
     *
     * @return the {@link JScrollPane} containing the whole content
     */
    private JScrollPane initView() {
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
        // make sure it is always on the right
        final GridBagConstraints leftSpace = new GridBagConstraints();
        leftSpace.fill = GridBagConstraints.HORIZONTAL;
        leftSpace.weightx = 1;
        leftSpace.gridx = 0;
        leftSpace.gridy = 0;
        background.add(new JPanel(null), leftSpace);
        // wrap the headers to get equal behavior
        final JPanel headerView = new JPanel(new GridBagLayout());
        headerView.add(new JPanel(null), leftSpace);
        headerView.add(this.contentHeaders, mainConstraints);
        headerView.setBorder(new MatteBorder(0, 0, 1, 0, Color.BLACK));

        final ComponentOrientation orientation;
        if (this.getViewReference().getModelHandler().getModel().isLeftToRightOriented()) {
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
        this.repaintPericope();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.propositionList = null;
        this.relationMap = null;
    }

    @Override
    public void repaintPericope() {
        // remember vertical position
        final int verticalPosition = this.scrollPane.getVerticalScrollBar().getValue();
        // clear view
        this.getViewReference().submitChangesToModel();
        this.scrollPane.setVisible(false);
        this.contentArea.removeAll();
        // get the currently used origin text font
        this.propositionList = new ArrayList<SemProposition>();
        // fill the propositionList
        for (final Proposition singleTopLevelProposition : this.getViewReference().getModelHandler().getModel().getText()) {
            this.addSemPropositionToList(singleTopLevelProposition);
        }
        this.levels = this.calculateLevels();
        // show pericope
        this.displayPropositions();
        this.displayRelations();
        this.resetHeaders();
        // reset vertical position
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                SemAnalysisPanel.this.scrollPane.setVisible(true);
                SemAnalysisPanel.this.scrollPane.getVerticalScrollBar().setValue(verticalPosition);
            }
        });
    }

    /**
     * Transfer the {@link SemProposition}s contained in the {@link #propositionList} to the displayed view.
     */
    private void displayPropositions() {
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        constraints.gridx = Math.max(1, this.levels);
        constraints.gridy = 0;
        for (final SemProposition singleProposition : this.propositionList) {
            if (singleProposition.getRepresented().getSuperOrdinatedRelation() != null) {
                singleProposition.setCheckBoxVisible(false);
            }
            this.contentArea.add(singleProposition, constraints);
            constraints.gridy++;
        }
    }

    /**
     * Build the {@link #relationMap} and display it; assuming the already created {@link #propositionList}.
     */
    private void displayRelations() {
        this.relationMap = new HashMap<Relation, ViewRelation>();
        SemProposition singleProposition = this.propositionList.get(0);
        while (singleProposition != null) {
            Relation singleRelation = singleProposition.getRepresented().getSuperOrdinatedRelation();
            if (singleRelation == null) {
                final Proposition follower = singleProposition.getRepresented().getFollowingConnectableProposition();
                if (follower == null) {
                    break;
                }
                singleProposition = SemControl.getRepresentative(this, follower);
            } else {
                // get the highest relation over the singleProposition
                while (singleRelation.getSuperOrdinatedRelation() != null) {
                    singleRelation = singleRelation.getSuperOrdinatedRelation();
                }
                // add highest and all of its subordinated relations in map
                this.insertRelationTree(singleRelation);
                final Proposition follower = singleRelation.getLastPropositionContained().getFollowingConnectableProposition();
                if (follower == null) {
                    break;
                }
                singleProposition = SemControl.getRepresentative(this, follower);
            }
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
        final ViewRelation viewRepresentative = new ViewRelation(this.getViewReference(), this, relation, this.foldedLevels);
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
            final IConnectable<?> associateRepresentative = SemControl.getRepresentative(this, singleAssociate);
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
     * Add the specified {@link Proposition} to the list of {@link SemProposition}s WITHOUT adding it to the view.
     *
     * @param proposition
     *            {@link Proposition} to add to list
     */
    private void addSemPropositionToList(final Proposition proposition) {
        // first: add all prior children
        final List<Proposition> priorChildren = proposition.getPriorChildren();
        if (priorChildren != null) {
            for (final Proposition singlePriorChild : priorChildren) {
                this.addSemPropositionToList(singlePriorChild);
            }
        }
        // second: add the proposition itself
        final SemProposition viewProposition = new SemProposition(this.getViewReference(), this, proposition);
        if (this.propositionList == null) {
            this.propositionList = new ArrayList<SemProposition>();
        }
        this.propositionList.add(viewProposition);
        final Proposition partBeforeArrow = proposition.getPartBeforeArrow();
        if (partBeforeArrow != null) {
            final SemProposition viewPartBeforeArrow = SemControl.getRepresentative(this, partBeforeArrow);
            // show arrows
            final int listSize = this.propositionList.size();
            final int beforeArrowPos = CollectionUtil.indexOfInstance(this.propositionList, viewPartBeforeArrow);
            // count number of arrows to set
            int arrowCount = listSize - beforeArrowPos - 1;
            // ignore partAfterArrows
            for (int i = beforeArrowPos + 1; i < listSize; i++) {
                if (this.propositionList.get(i).getRepresented().getPartBeforeArrow() != null) {
                    arrowCount--;
                }
            }
            // show arrows in part before arrow
            viewPartBeforeArrow.setRightArrowCount(arrowCount);
            // show arrows in target
            viewProposition.setLeftArrowCount(arrowCount);
        }
        // third: add all later children
        final List<Proposition> laterChildren = proposition.getLaterChildren();
        if (laterChildren != null) {
            for (final Proposition singleLaterChild : laterChildren) {
                this.addSemPropositionToList(singleLaterChild);
            }
        }
        // finally: add the part after arrow
        final Proposition partAfterArrow = proposition.getPartAfterArrow();
        if (partAfterArrow != null) {
            this.addSemPropositionToList(partAfterArrow);
        }
    }

    @Override
    public void submitChangesToModel() {
        // only the propositions might have any pending changes (e.g. the label and translation fields)
        if (this.propositionList != null) {
            for (final SemProposition singleProposition : this.propositionList) {
                singleProposition.submitChangesToModel();
            }
        }
    }

    /**
     * Expose a mutable copy of the internal list of displayed {@link SemProposition}s.
     *
     * @return list containing all {@link Proposition} components in this view
     */
    public List<SemProposition> getPropositionList() {
        if (this.propositionList == null) {
            return null;
        }
        return new ArrayList<SemProposition>(this.propositionList);
    }

    /**
     * Calculate the maximum number of super ordinated {@link Relation}s.
     *
     * @return calculated maximum level
     */
    private int calculateLevels() {
        int max = 0;
        for (final SemProposition singleProposition : this.propositionList) {
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
     * Expose a mutable copy of the mapping of displayed {@link Relation} and their respective components.
     *
     * @return map containing all relations and their representations
     */
    public Map<Relation, ViewRelation> getRelationMap() {
        if (this.relationMap == null) {
            return null;
        }
        return new HashMap<Relation, ViewRelation>(this.relationMap);
    }

    /**
     * Collect all currently checked {@link Relation}s and/or {@link Proposition}s in the right order.
     *
     * @param clicked
     *            single element to include in the result list (regardless of its checked state)
     * @return list containing all checked elements sorted from top to bottom
     */
    public List<AbstractConnectable> getChecked(final AbstractConnectable clicked) {
        final List<AbstractConnectable> list = new LinkedList<AbstractConnectable>();
        // REQUIREMENT: whole model is represented in the view
        AbstractConnectable nextToCheck = this.propositionList.get(0).getRepresented();
        while (nextToCheck != null) {
            // get checkable element
            while (nextToCheck.getSuperOrdinatedRelation() != null) {
                nextToCheck = nextToCheck.getSuperOrdinatedRelation();
            }
            if (nextToCheck == clicked || SemControl.getRepresentative(this, nextToCheck).isChecked()) {
                list.add(nextToCheck);
            }
            // get next element
            nextToCheck = nextToCheck.getFollowingConnectableProposition();
        }
        return list;
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
                SemAnalysisPanel.this.foldRelationRolesOnLevel(-1, foldAll);
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
                    SemAnalysisPanel.this.foldRelationRolesOnLevel(levelIndex, fold);
                }
            });
            headerPanel.add(box, BorderLayout.CENTER);
            constraints.gridx--;
            // apply width of each column to its header
            headerPanel.setPreferredSize(new Dimension(maxColumnSize.get(constraints.gridx), headerPanel.getPreferredSize().height));
            this.contentHeaders.add(headerPanel, constraints);
        }
    }
}
