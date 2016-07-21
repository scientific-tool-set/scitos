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

package org.hmx.scitos.view.swing.option;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JPanel;

import org.hmx.scitos.core.i18n.ILocalizableMessage;

/** Abstract panel in the application's preferences dialog. */
public abstract class AbstractOptionPanel extends JPanel {

    /** Constraints containing default insets (i.e. inner borders). */
    protected static final GridBagConstraints DEFAULT_INSETS = new GridBagConstraints();
    /** Constraints enabling full horizontal span. */
    protected static final GridBagConstraints HORIZONTAL_SPAN = new GridBagConstraints();

    static {
        AbstractOptionPanel.DEFAULT_INSETS.insets = new Insets(2, 5, 2, 5);
        AbstractOptionPanel.HORIZONTAL_SPAN.fill = GridBagConstraints.HORIZONTAL;
        AbstractOptionPanel.HORIZONTAL_SPAN.weightx = 1;
    }

    /** The node label in the tree of the application's preferences dialog. */
    private final ILocalizableMessage title;

    /**
     * Main constructor.
     *
     * @param layout
     *            layout manager to apply
     * @param title
     *            the node label in the tree of the application's preferences dialog
     */
    protected AbstractOptionPanel(final LayoutManager layout, final ILocalizableMessage title) {
        super(layout);
        this.title = title;
        this.setBorder(null);
    }

    /**
     * Indicate whether this option panel handles any potential resizing or scrolling requirements itself.
     * 
     * @return if the option panel is capable of adjusting to any dialog size
     */
    protected boolean isResizeCapable() {
        // default: let the containing dialog add a scrollable wrapper to the individual option panel implementation
        return false;
    }

    /**
     * Ensure that any user input is grabbed from the user interface and being regarded when {@link #areChosenSettingsValid()} or
     * {@link #submitChosenSettings()} is called.
     */
    protected abstract void validateInput();

    /**
     * Check if the chosen settings are fit to be submitted. Display a fitting message dialog, if not.
     *
     * @return if the chosen settings are valid
     */
    public abstract boolean areChosenSettingsValid();

    /**
     * Submit the chosen settings to the current options handler in order to activate them now and save them to a file in order to be able to load
     * them on next application start.
     */
    public abstract void submitChosenSettings();

    /** Returns the stored title message, to be displayed in the OptionView's SplitFrame's tree as node label. */
    @Override
    public String toString() {
        return this.title.get();
    }
}
