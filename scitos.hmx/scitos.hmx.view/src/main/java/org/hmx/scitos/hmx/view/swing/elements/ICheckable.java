package org.hmx.scitos.hmx.view.swing.elements;

/** Indicator interface for an user interface element's ability to be selectable via an integrated check box. */
interface ICheckable {

    /**
     * Determine if this element's check box is currently selected.
     *
     * @return if this view element is currently selected
     */
    boolean isChecked();

    /**
     * Reset this view element's check box to be unselected.
     *
     * @see #isChecked()
     */
    void setNotChecked();

}
