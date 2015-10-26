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

import org.hmx.scitos.domain.util.ComparisonUtil;
import org.hmx.scitos.hmx.core.HmxModelHandler;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
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
public final class CombinedAnalysesPanel extends JPanel implements IPericopeView {

    /** The represented project's model handler instance. */
    private final HmxModelHandler modelHandler;
    /**
     * The provider of available semantical {@link RelationTemplate}s, to be offered via the elements' context menus.
     */
    private final ISemanticalRelationProvider relationProvider;

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
    /** The input area at the bottom of the view, allowing the display and modification of a selected element's comment. */
    private final JTextPane commentArea;

    /**
     * The currently active analysis: either {@link #semAnalysisView} or {@link #synAnalysisView}.
     */
    private JPanel activeAnalysisView;
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
        this.setBorder(null);
        // initialize the commentArea to be reachable by commentable components
        this.commentArea = new ScaledTextPane();
        // build the analysis views representing the pericope
        this.semAnalysisView = new SemAnalysisPanel(this);
        this.synAnalysisView = new SynAnalysisPanel(this);
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
        this.switchButton = new JButton() {

            @Override
            public void updateUI() {
                super.updateUI();
                this.setFont(UIManager.getFont("Button.font"));
            }
        };
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

        // initialize the possible button icons
        this.semButtonIcon = new VTextIcon(this.switchButton, HmxMessage.ANALYSIS_SEMANTICAL_BUTTON.get(), VTextIcon.Rotate.NONE);
        this.synButtonIcon = new VTextIcon(this.switchButton, HmxMessage.ANALYSIS_SYNTACTICAL_BUTTON.get(), VTextIcon.Rotate.NONE);

        // default: always start with the syntactical analysis
        this.switchButton.setIcon(this.semButtonIcon);
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

        // default: always start with the syntactical analysis
        this.synAnalysisView.activate();
        this.activeAnalysisView = this.synAnalysisView;
    }

    /**
     * Change the active analysis view by setting their visibility to make sure that only one analysis view is visible; and clear the shown comment.
     */
    void changeActiveAnalysisView() {
        // make sure only one analysis view is visible at the end
        if (this.activeAnalysisView == this.semAnalysisView) {
            this.activeAnalysisView = this.synAnalysisView;
            this.synAnalysisView.activate();
            this.semAnalysisView.deactivate();
            this.switchButton.setIcon(this.semButtonIcon);
        } else {
            this.activeAnalysisView = this.semAnalysisView;
            this.semAnalysisView.activate();
            this.synAnalysisView.deactivate();
            this.switchButton.setIcon(this.synButtonIcon);
        }
        // clear comment area
        this.commentArea.setText(null);
    }

    @Override
    public List<AbstractConnectable> getSelectedConnectables(final AbstractConnectable defaultSelected) {
        if (this.semAnalysisView.isVisible()) {
            return this.semAnalysisView.getChecked(defaultSelected);
        }
        return defaultSelected == null ? Collections.<AbstractConnectable>emptyList() : Collections.singletonList(defaultSelected);
    }

    @Override
    public List<Proposition> getSelectedPropositions(final Proposition defaultSelected) {
        if (this.synAnalysisView.isVisible()) {
            return this.synAnalysisView.getChecked(defaultSelected);
        }
        return defaultSelected == null ? Collections.<Proposition>emptyList() : Collections.singletonList(defaultSelected);
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
        this.handleSelectedCommentable(null);
    }

    /**
     * Fully rebuild the currently displayed representation of the {@link Pericope}.
     */
    public void refresh() {
        if (this.synAnalysisView.isShowing()) {
            this.synAnalysisView.repaintPericope();
        } else if (this.semAnalysisView.isShowing()) {
            this.semAnalysisView.repaintPericope();
        }
    }

    @Override
    public List<List<RelationTemplate>> provideRelationTemplates() {
        return this.relationProvider.provideRelationTemplates();
    }
}
