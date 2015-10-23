package org.hmx.scitos.view.swing.components;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.UIManager;

import org.hmx.scitos.view.swing.ScitosApp;

/** Label extension that is scaling the displayed text's font according to the global setting. */
public class ScaledLabel extends JLabel {

    /** The LookAndFeel default Font key. */
    private final String fontKey;
    private Font baseFont = null;

    /**
     * Constructor.
     *
     * @param text
     *            the text to be displayed by the label
     */
    public ScaledLabel(final String text) {
        this(text, "Label.font");
    }

    /**
     * Constructor.
     *
     * @param text
     *            the text to be displayed by the label
     * @param fontKey
     *            the LookAndFeel default key for the designated font
     */
    public ScaledLabel(final String text, final String fontKey) {
        super(text);
        this.fontKey = fontKey;
        this.validateScaleFactor();
    }

    /**
     * Constructor.
     *
     * @param text
     *            the text to be displayed by the label
     * @param horizontalAlignment
     *            One of the following constants defined in SwingConstants: LEFT, CENTER, RIGHT, LEADING or TRAILING.
     */
    public ScaledLabel(final String text, final int horizontalAlignment) {
        this(text, "Label.font", horizontalAlignment);
    }

    /**
     * Constructor.
     *
     * @param text
     *            the text to be displayed by the label
     * @param fontKey
     *            the LookAndFeel default key for the designated font
     * @param horizontalAlignment
     *            One of the following constants defined in SwingConstants: LEFT, CENTER, RIGHT, LEADING or TRAILING.
     */
    public ScaledLabel(final String text, final String fontKey, final int horizontalAlignment) {
        super(text, horizontalAlignment);
        this.fontKey = fontKey;
        this.validateScaleFactor();
    }

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
        Font toApply = this.baseFont;
        // avoid NullPointer due to the font or the respective key being null on initialization
        if (toApply == null && this.fontKey != null) {
            toApply = UIManager.getFont(this.fontKey);
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
