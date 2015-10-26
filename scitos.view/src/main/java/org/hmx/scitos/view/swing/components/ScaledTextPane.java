package org.hmx.scitos.view.swing.components;

import java.awt.Font;

import javax.swing.JTextPane;
import javax.swing.UIManager;

import org.hmx.scitos.view.swing.ScitosApp;

/**
 * {@link JTextPane} extension that is scaling the displayed text's {@link Font} according to the global setting.
 */
public class ScaledTextPane extends JTextPane {

    /**
     * The specific {@link Font} that has been set via the {@link #setFont(Font)} method from outside this class.
     */
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

    /** (Re-)Apply the global content size factor to the font used to display the label's text. */
    private void validateScaleFactor() {
        final Font toApply;
        if (this.baseFont == null) {
            toApply = UIManager.getFont("TextPane.font");
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
