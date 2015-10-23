package org.hmx.scitos.hmx.view.swing.components;

import java.util.List;

import javax.swing.SwingUtilities;

import org.hmx.scitos.domain.ModelChangeListener;
import org.hmx.scitos.domain.ModelEvent;
import org.hmx.scitos.hmx.domain.model.ClauseItem;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.view.swing.elements.SynItem;
import org.hmx.scitos.hmx.view.swing.elements.SynProposition;

/**
 * Listener for the {@link SynAnalysisPanel} to react on changed model elements.
 */
public final class SynControl implements ModelChangeListener {

    /** targeted syntactical analysis view */
    final SynAnalysisPanel synArea;

    /**
     * stores the targeted syntactical analysis view to display the listened changes in
     *
     * @param panel
     *            targeted syntactical analysis view
     */
    SynControl(final SynAnalysisPanel panel) {
        this.synArea = panel;
    }

    /**
     * creates the listener for the defined {@link SynAnalysisPanel}, responsible for the whole handling of event representing a change in the model
     *
     * @param event
     *            thrown {@link ModelEvent} containing the changed model element
     */
    @Override
    public void modelChanged(final ModelEvent<?> event) {
        // deal with different kinds of targets and event types
        final Object target = event.getTarget();
        SwingUtilities.invokeLater(new Thread("HmX Syntactical Model Update") {

            @Override
            public void run() {
                // manage the handling of the event
                if (target instanceof Pericope) {
                    SynControl.this.synArea.repaintPericope();
                } else if (target instanceof Proposition) {
                    SynControl.this.refreshProposition((Proposition) target);
                } else if (target instanceof ClauseItem) {
                    SynControl.this.refreshClauseItem((ClauseItem) target);
                }
                // ignore (target instanceof org.hermeneutix.model.Relation)
                // target should be no other kind of instance
            }
        });
    }

    /**
     * handles a {@link ModelEvent} containing a {@link Proposition}
     *
     * @param target
     *            {@link Proposition} to refresh in view
     */
    void refreshProposition(final Proposition target) {
        final SynProposition representative = SynControl.getRepresentative(this.synArea, target);
        if (representative == null) {
            // edited proposition currently not displayed
            return;
        }
        if (representative.refreshFunction()) {
            // first time function label with an icon is added
            this.synArea.repaintPericope();
            return;
        }
        representative.refreshLabelText();
        representative.refreshTranslation();
        representative.refreshComment();
        // remove all items in view
        final SynItem[] viewItems = representative.getItems();
        for (final SynItem singleViewItem : viewItems) {
            representative.removeItem(singleViewItem);
        }
        // reinsert all items in view
        for (final ClauseItem singleModelItem : target) {
            representative.insertItem(singleModelItem);
        }
    }

    /**
     * handles a {@link ModelEvent} containing a {@link ClauseItem}
     *
     * @param target
     *            {@link ClauseItem} to refresh in view
     */
    void refreshClauseItem(final ClauseItem target) {
        final SynItem representative = SynControl.getRepresentative(this.synArea, target);
        if (representative != null) {
            representative.refreshFontStyle();
            representative.refreshFunction();
            representative.refreshOriginText();
            representative.refreshComment();
        }
    }

    /**
     * browses all {@link SynProposition}s in the syntactical analysis view for the specified {@link Proposition}
     *
     * @param synArea
     *            syntactical analysis view containing the searched representation
     * @param target
     *            {@link Proposition} to look for
     * @return {@link SynProposition} representing the target
     */
    public static SynProposition getRepresentative(final SynAnalysisPanel synArea, final Proposition target) {
        final List<SynProposition> viewPropositions = synArea.getPropositionList();
        for (final SynProposition singleViewProposition : viewPropositions) {
            if (target == singleViewProposition.getRepresented()) {
                // found representative in the syntactical analysis view
                return singleViewProposition;
            }
        }
        // target does not exist in the syntactical analysis view
        return null;
    }

    /**
     * browses all {@link SynProposition}s in the syntactical analysis view for the parent of the specified {@link ClauseItem} and the contained
     * representative of the item
     *
     * @param synArea
     *            syntactical analysis view containing the searched representation
     * @param target
     *            {@link ClauseItem} to look for
     * @return {@link SynItem} representing the target
     */
    public static SynItem getRepresentative(final SynAnalysisPanel synArea, final ClauseItem target) {
        final SynProposition parentRepresentative = SynControl.getRepresentative(synArea, target.getParent());
        if (parentRepresentative != null) {
            final SynItem[] viewItems = parentRepresentative.getItems();
            for (final SynItem singleViewItem : viewItems) {
                if (target == singleViewItem.getRepresented()) {
                    return singleViewItem;
                }
            }
        }
        // target does not exist in the syntactical analysis view
        return null;
    }
}
