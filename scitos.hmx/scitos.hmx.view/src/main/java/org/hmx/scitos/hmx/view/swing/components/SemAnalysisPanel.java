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
import org.hmx.scitos.domain.util.ComparisonUtil;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.domain.model.AbstractConnectable;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.domain.model.Relation;
import org.hmx.scitos.hmx.view.IPericopeView;
import org.hmx.scitos.hmx.view.swing.HmxSwingProject;
import org.hmx.scitos.hmx.view.swing.elements.IConnectable;
import org.hmx.scitos.hmx.view.swing.elements.SemProposition;
import org.hmx.scitos.hmx.view.swing.elements.SemRelation;
import org.hmx.scitos.hmx.view.swing.elements.SemRelationExtender;

/**
 * semantical analysis view displaying the semantical structured analysis consisting of {@link Proposition}s and {@link Relation}s disregarding the
 * syntactical functions, indentations, ClauseItems and syntactical translations in the {@link Proposition}s; offering the opportunity to create, edit
 * and remove {@link Relation}s
 */
public final class SemAnalysisPanel extends AbstractAnalysisPanel {

    private int levels = 0;

    final JScrollPane scrollPane;
    private final JPanel contentArea = new JPanel(new GridBagLayout());
    final JPanel contentHeaders = new JPanel(new GridBagLayout()) {

        @Override
        public void updateUI() {
            if (SemAnalysisPanel.this.contentHeaders != null) {
                SemAnalysisPanel.this.resetHeaders();
            }
        }
    };
    private final SemControl listener;
    private final Set<Integer> foldedLevels = new HashSet<Integer>();

    private List<SemProposition> propositionList;
    private Map<Relation, SemRelation> relationMap;

    /**
     * creates a new {@link SemAnalysisPanel} representing the Pericope contained in the specified project
     *
     * @param project
     *            {@link HmxSwingProject} to display
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
     * initializes the general layout and position of the content to display
     *
     * @return the JScrollPane containing the whole content
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
     * @param level
     *            single relation level to fold/unfold (targets all levels, when is <code>-1</code>)
     * @param fold
     *            if the semantic roles of this/these level(s) should be hidden
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
     * transfers the {@link SemProposition}s contained in the <code>propositionList</code> to the displayed view
     */
    private void displayPropositions() {
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        constraints.gridx = Math.max(1, this.levels);
        for (int i = 0; i < this.propositionList.size(); i++) {
            constraints.gridy = i;
            final SemProposition singleProposition = this.propositionList.get(i);
            if (singleProposition.getRepresented().getSuperOrdinatedRelation() != null) {
                singleProposition.setCheckBoxVisible(false);
            }
            this.contentArea.add(singleProposition, constraints);
        }
    }

    /**
     * builds the <code>relationMap</code> and displays it in the view<br>
     * REQUIREMENT: already created <code>propositionList</code>
     */
    private void displayRelations() {
        this.relationMap = new HashMap<Relation, SemRelation>();
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
     * display the specified {@link Relation} and all of its subordinated {@link AbstractConnectable}s
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
        final SemRelation viewRepresentative = new SemRelation(this.getViewReference(), this, relation, this.foldedLevels);
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
                this.contentArea.add(new SemRelationExtender(), constraints);
                depth--;
            }
        }
    }

    /**
     * adds the specified {@link Proposition} to the list of {@link SemProposition}s WITHOUT adding it to the view
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
            final int beforeArrowPos = ComparisonUtil.indexOfInstance(this.propositionList, viewPartBeforeArrow);
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

    /**
     * @return list containing all representative propositions in this view
     */
    public List<SemProposition> getPropositionList() {
        if (this.propositionList == null) {
            return null;
        }
        final List<SemProposition> val = new ArrayList<SemProposition>();
        for (final SemProposition singleViewProposition : this.propositionList) {
            val.add(singleViewProposition);
        }
        return val;
    }

    /**
     * calculates the maximum number of super ordinated {@link Relation}s
     *
     * @return calculated maximum level
     */
    private int calculateLevels() {
        int max = 0;
        for (final SemProposition singleProposition : this.propositionList) {
            Relation singleSuperordinated = singleProposition.getRepresented().getSuperOrdinatedRelation();
            int depth = 0;
            while (singleSuperordinated != null) {
                depth++;
                singleSuperordinated = singleSuperordinated.getSuperOrdinatedRelation();
            }
            max = Math.max(max, depth);
        }
        return (max + 1);
    }

    /**
     * @return map containing all relations and there representations
     */
    public Map<Relation, SemRelation> getRelationMap() {
        if (this.relationMap == null) {
            return null;
        }
        return new HashMap<Relation, SemRelation>(this.relationMap);
    }

    /**
     * browse the whole view for all checked relations and/or propositions and returns them in the right order
     *
     * @param clicked
     *            source object who called this
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

    /**
     * Make sure the headers for folding/collapsing the relations (hide/show roles) are present and sized properly.
     */
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
