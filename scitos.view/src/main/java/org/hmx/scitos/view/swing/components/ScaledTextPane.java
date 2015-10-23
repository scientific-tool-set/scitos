package org.hmx.scitos.view.swing.components;

import java.awt.Font;

import javax.swing.JTextPane;
import javax.swing.UIManager;

import org.hmx.scitos.view.swing.ScitosApp;

/** TextPane extension that is scaling the displayed text's font according to the global setting. */
public class ScaledTextPane extends JTextPane {

    private Font baseFont = null;

    @Override
    public void setFont(final Font font) {
        this.baseFont = font;
        this.validateScaleFactor();
    }

    /**
     * Getter for the unscaled base {@link Font}, that was applied last via {@link #setFont(Font)}.
     *
     * @return unscaled applied {@link Font}
     */
    public Font getUnscaledFont() {
        return this.baseFont;
    }

    @Override
    public void updateUI() {
        super.updateUI();
        this.validateScaleFactor();
    }

    private void validateScaleFactor() {
        Font toApply = this.baseFont;
        if (toApply == null) {
            toApply = UIManager.getFont("TextPane.font");
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
