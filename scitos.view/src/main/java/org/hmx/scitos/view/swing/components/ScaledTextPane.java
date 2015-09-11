package org.hmx.scitos.view.swing.components;

import java.awt.Font;

import javax.swing.JTextPane;
import javax.swing.UIManager;

import org.hmx.scitos.view.swing.ScitosApp;

/** TextPane extension that is scaling the displayed text's font according to the global setting. */
public class ScaledTextPane extends JTextPane {

    @Override
    public void updateUI() {
        super.updateUI();
        final Font defaultFont = UIManager.getFont("TextPane.font");
        if (defaultFont != null) {
            final float scaleFactor;
            if (ScitosApp.getClient() == null) {
                scaleFactor = 1f;
            } else {
                scaleFactor = ScitosApp.getClient().getContentScaleFactor();
            }
            this.setFont(new Font(defaultFont.getAttributes()).deriveFont(defaultFont.getSize2D() * scaleFactor));
        }
    }
}
