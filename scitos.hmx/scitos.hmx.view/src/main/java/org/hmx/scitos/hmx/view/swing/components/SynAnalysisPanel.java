package org.hmx.scitos.hmx.view.swing.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.hmx.scitos.domain.ModelChangeListener;
import org.hmx.scitos.domain.util.ComparisonUtil;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.view.IPericopeView;
import org.hmx.scitos.hmx.view.swing.elements.SynProposition;

/**
 * View displaying the Syntactical Structured Analysis consisting of {@link Proposition}s disregarding super ordinated Relations and semantical
 * translations; offering the opportunity to edit the whole syntactical structure displayed.
 */
public final class SynAnalysisPanel extends AbstractAnalysisPanel {

    /** The model change listener responsible for keeping this view up to date while it is active. */
    private final SynControl listener;
    /**
     * Complete list of all displayed {@link Proposition}s in the correct (i.e. text) order.
     */
    private List<SynProposition> propositionList = null;
    /** The single main view element allowing all its contents to be scrolled. */
    JScrollPane scrollPane = null;
    /**
     * The actual container of the view components representing {@link Proposition}s.
     */
    private final Box contentPane = new Box(BoxLayout.PAGE_AXIS);

    /**
     * Constructor.
     *
     * @param viewReference
     *            super ordinated view this panel belongs to
     */
    public SynAnalysisPanel(final IPericopeView viewReference) {
        super(viewReference, new GridLayout(0, 1));
        // collect all possible syntactical Functions
        this.listener = new SynControl(this);
        this.initView();
    }

    @Override
    protected ModelChangeListener getModelChangeListener() {
        return this.listener;
    }

    /**
     * Initialize the general layout and position of the content to display.
     */
    private void initView() {
        final JPanel scrollable = new JPanel(new GridBagLayout());
        this.contentPane.setBorder(null);
        final GridBagConstraints mainConstraints = new GridBagConstraints();
        mainConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        mainConstraints.fill = GridBagConstraints.HORIZONTAL;
        mainConstraints.weightx = 1;
        scrollable.add(this.contentPane, mainConstraints);

        final GridBagConstraints spacing = new GridBagConstraints();
        spacing.fill = GridBagConstraints.VERTICAL;
        spacing.weighty = 1;
        spacing.gridy = 1;
        scrollable.add(new JPanel(), spacing);

        this.scrollPane = new JScrollPane(scrollable);
        this.scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(this.scrollPane);
    }

    @Override
    public void repaintPericope() {
        // remember vertical position
        final int verticalPosition = this.scrollPane.getVerticalScrollBar().getValue();
        // clear view
        this.getViewReference().submitChangesToModel();
        this.scrollPane.setVisible(false);
        this.contentPane.removeAll();
        // get currently used origin text font
        this.propositionList = new ArrayList<SynProposition>();
        // rebuild proposition list and display propositions
        for (final Proposition singleTopLevelProposition : this.getViewReference().getModelHandler().getModel().getText()) {
            this.addSynProposition(singleTopLevelProposition, 0);
        }
        // reset vertical position
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                SynAnalysisPanel.this.scrollPane.setVisible(true);
                SynAnalysisPanel.this.scrollPane.getVerticalScrollBar().setValue(verticalPosition);
            }
        });
    }

    /**
     * Add the specified {@link Proposition} and all of its (further indented) child {@link Proposition}s to the view.
     *
     * @param proposition
     *            {@link Proposition} to insert in view
     * @param level
     *            indentation level
     */
    private void addSynProposition(final Proposition proposition, final int level) {
        // first: add all prior children
        for (final Proposition singlePriorChild : proposition.getPriorChildren()) {
            this.addSynProposition(singlePriorChild, level + 1);
        }
        // second: add the proposition itself
        final SynProposition viewProposition = SynProposition.createSynPropositionByLevel(this.getViewReference(), proposition, level);
        this.propositionList.add(viewProposition);
        this.contentPane.add(viewProposition);
        // third: add all later children
        for (final Proposition singleLaterChild : proposition.getLaterChildren()) {
            this.addSynProposition(singleLaterChild, level + 1);
        }
        // finally: add the part after arrow
        final Proposition partAfterArrow = proposition.getPartAfterArrow();
        if (partAfterArrow != null) {
            this.addSynPropositionAfterArrow(partAfterArrow, level, viewProposition);
        }
    }

    /**
     * Add the specified {@link Proposition} and all of its (further indented) child {@link Proposition}s to the view.
     *
     * @param proposition
     *            {@link Proposition} to insert in view
     * @param level
     *            indentation level of the {@code partBeforeArrow}
     * @param partBeforeArrow
     *            view representation of the partBeforeArrow
     */
    private void addSynPropositionAfterArrow(final Proposition proposition, final int level, final SynProposition partBeforeArrow) {
        // first: add prior children
        for (final Proposition singlePriorChild : proposition.getPriorChildren()) {
            this.addSynProposition(singlePriorChild, level + 1);
        }
        // second: add the proposition itself
        final SynProposition viewProposition =
                SynProposition.createSynPropositionByPartBeforeArrow(this.getViewReference(), proposition, partBeforeArrow);
        this.propositionList.add(viewProposition);
        // show arrows
        final int listSize = this.propositionList.size();
        final int beforeArrowPos = ComparisonUtil.indexOfInstance(this.propositionList, partBeforeArrow);
        // count number of arrows to set
        int arrowCount = listSize - beforeArrowPos - 1;
        // ignore partAfterArrows
        for (int i = beforeArrowPos + 1; i < listSize; i++) {
            if (this.propositionList.get(i).getRepresented().getPartBeforeArrow() != null) {
                arrowCount--;
            }
        }
        // show arrows in part before arrow
        partBeforeArrow.setRightArrowCount(arrowCount);
        // show arrows in target
        viewProposition.setLeftArrowCount(arrowCount);
        // show proposition in view
        this.contentPane.add(viewProposition);
        // third: add later children
        for (final Proposition singleLaterChild : proposition.getLaterChildren()) {
            this.addSynProposition(singleLaterChild, level + 1);
        }
        // finally: add part after arrow
        final Proposition partAfterArrow = proposition.getPartAfterArrow();
        if (partAfterArrow != null) {
            this.addSynPropositionAfterArrow(partAfterArrow, level, viewProposition);
        }
    }

    /**
     * Collect all checked {@link Proposition}s in the displayed syntactical analysis.
     *
     * @param clicked
     *            {@link Proposition} requesting the list, that is included in the result list regardless of it being checked or not (ignored, if it
     *            is {@code null})
     * @return list of all checked {@link Proposition}s in the correct (i.e. text) order
     */
    public List<Proposition> getChecked(final Proposition clicked) {
        final List<Proposition> checked = new LinkedList<Proposition>();
        for (final SynProposition singleProposition : this.propositionList) {
            if (singleProposition.isChecked() || singleProposition.getRepresented() == clicked) {
                checked.add(singleProposition.getRepresented());
            }
        }
        return checked;
    }

    /**
     * Getter for the complete list of all displayed {@link Proposition}s in the correct (i.e. text) order.
     * 
     * @return list of all displayed {@link SynProposition}s
     */
    public List<SynProposition> getPropositionList() {
        return Collections.unmodifiableList(this.propositionList);
    }
}
