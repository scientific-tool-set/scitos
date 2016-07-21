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

package org.hmx.scitos.view.swing.components;

import java.awt.Font;

import javax.swing.JTextField;
import javax.swing.UIManager;

import org.hmx.scitos.view.swing.ScitosApp;

/**
 * {@link JTextField} extension that is scaling the displayed text's {@link Font} according to the global setting.
 */
public class ScaledTextField extends JTextField {

    /**
     * The specific {@link Font} that has been set via the {@link #setFont(Font)} method from outside this class.
     */
    private Font baseFont = null;

    @Override
    public void setFont(final Font font) {
        this.baseFont = font;
        this.validateScaleFactor();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        this.validateScaleFactor();
    }

    /** (Re-)Apply the global content size factor to the font used to display the label's text. */
    private void validateScaleFactor() {
        final Font toApply;
        if (this.baseFont == null) {
            toApply = UIManager.getFont("TextField.font");
        } else {
            toApply = this.baseFont;
        }
        if (toApply != null) {
            final float scaleFactor;
            if (ScitosApp.getClient() == null) {
                scaleFactor = 1f;
            } else {
                scaleFactor = ScitosApp.getClient().getContentScaleFactor();
            }
            super.setFont(new Font(toApply.getAttributes()).deriveFont(toApply.getSize2D() * scaleFactor));
        }
    }
}
