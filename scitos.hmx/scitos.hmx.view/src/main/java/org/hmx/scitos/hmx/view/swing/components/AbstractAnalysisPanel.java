package org.hmx.scitos.hmx.view.swing.components;

import java.awt.LayoutManager;

import javax.swing.JPanel;

import org.hmx.scitos.domain.ModelChangeListener;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.view.IPericopeView;

/** Abstract base panel for a single analysis. */
abstract class AbstractAnalysisPanel extends JPanel {

    /** The view project instance this belongs to. */
    private final IPericopeView viewReference;

    /**
     * Constructor: initializing the invisible and empty panel with the given layout manager.
     *
     * @param project
     *            the view project this belongs to
     * @param layout
     *            the main layout manager to apply
     */
    protected AbstractAnalysisPanel(final IPericopeView viewReference, final LayoutManager layout) {
        super(layout);
        this.setVisible(false);
        this.viewReference = viewReference;
    }

    /**
     * Getter for the view project instance this belongs to.
     *
     * @return associated view project
     */
    public IPericopeView getViewReference() {
        return this.viewReference;
    }

    /**
     * Add the {@link #getModelChangeListener() ModelChangeListener} to the associated project and display the current state of the {@link Pericope}.
     *
     * @see #repaintPericope()
     */
    public void activate() {
        this.setVisible(true);
        this.getViewReference().getModelHandler().addModelChangeListener(this.getModelChangeListener());
        this.repaintPericope();
    }

    /**
     * Remove the {@link #getModelChangeListener() ModelChangeListener} from the associated project and make this panel invisible.
     */
    public void deactivate() {
        this.setVisible(false);
        this.getViewReference().getModelHandler().removeModelChangeListener(this.getModelChangeListener());
    }

    /**
     * Fully rebuild the displayed representation of the current {@link Pericope}.
     */
    public abstract void repaintPericope();

    /**
     * Getter for the {@link ModelChangeListener} handling updates of this panel.
     *
     * @return the associated model listener
     * @see #activate()
     * @see #deactivate()
     */
    protected abstract ModelChangeListener getModelChangeListener();
}
