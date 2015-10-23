package org.hmx.scitos.hmx.view.swing.elements;

import java.awt.LayoutManager;

import javax.swing.JPanel;

import org.hmx.scitos.hmx.domain.ICommentable;

/**
 * {@link JPanel} with two different borders to show whether it is the currently selected commentable element or not.
 *
 * @param <T>
 *            represented commentable model class
 */
public abstract class AbstractCommentable<T extends ICommentable> extends JPanel {

    /**
     * Constructor: applying the specified layout.
     *
     * @param layout
     *            {@link LayoutManager} to use
     */
    protected AbstractCommentable(final LayoutManager layout) {
        super(layout);
    }

    /**
     * @return represented commentable model element
     */
    public abstract T getRepresented();

    /** Set the default border to show that it is not the currently selected element. */
    public abstract void setDefaultBorder();

    /** Set the lowered comment border to show that it is the currently selected element. */
    public abstract void setCommentBorder();
}
