package org.hmx.scitos.hmx.view.swing;

import org.hmx.scitos.hmx.view.swing.elements.AbstractCommentable;

/**
 *
 */
public interface ICommentHandler {

    /**
     * Store the most recent modifications in the displayed comment area to the previously selected {@link AbstractCommentable commentable element}
     * and mark the given {@code newSelected} view element as the currently selected one.
     *
     * @param newSelected
     *            the newly selected commentable view element
     */
    void handleSelectedCommentable(AbstractCommentable<?> newSelected);
}
