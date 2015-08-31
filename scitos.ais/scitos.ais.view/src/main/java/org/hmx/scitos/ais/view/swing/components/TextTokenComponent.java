/*
   Copyright (C) 2015 HermeneutiX.org

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

package org.hmx.scitos.ais.view.swing.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.hmx.scitos.ais.domain.model.TextToken;

/**
 * Component representing a single token (usually a word), in an interview. The top half is dedicated to the display of the assigned detail category.
 * The bottom half contains the actual text of the token.
 */
public final class TextTokenComponent extends JPanel {

    /** The represented model object. */
    private final TextToken model;
    /** The parts this component is comprised of. This array is used to forward registered listeners to these parts. */
    private final JLabel[] labels;
    /**
     * The bottom half of the component, displaying the token's text. It also shows the changeable {@link #selected} state.
     */
    final JLabel textLabel;
    /** If the token should be displayed as selected, in order to assign a detail category to (i.e. score) it. */
    private boolean selected;

    /**
     * Main constructor.
     *
     * @param model
     *            represented model object
     */
    public TextTokenComponent(final TextToken model) {
        super(new BorderLayout(1, 1));
        this.model = model;
        this.setBorder(null);
        final JLabel openBracketLabel = new JLabel("(");
        openBracketLabel.setOpaque(false);
        openBracketLabel.setVisible(model.isFirstTokenOfDetail() && model.getPreviousToken() != null
                && !model.getPreviousToken().isLastTokenOfDetail());
        this.add(openBracketLabel, BorderLayout.LINE_START);
        final String detailCode;
        if (model.getDetail() == null || !model.isFirstTokenOfDetail()) {
            detailCode = " ";
        } else {
            detailCode = model.getDetail().getCode();
        }
        final JLabel detailLabel = new JLabel(detailCode);
        detailLabel.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
        detailLabel.setOpaque(false);
        this.add(detailLabel, BorderLayout.CENTER);
        final JLabel closingBracketLabel = new JLabel(")");
        closingBracketLabel.setOpaque(false);
        closingBracketLabel.setVisible(model.isLastTokenOfDetail() && model.getFollowingToken() != null
                && !model.getFollowingToken().isFirstTokenOfDetail());
        this.add(closingBracketLabel, BorderLayout.LINE_END);
        this.textLabel = new JLabel(model.getText(), SwingConstants.CENTER) {

            @Override
            public void updateUI() {
                super.updateUI();
                if (TextTokenComponent.this.textLabel != null) {
                    TextTokenComponent.this.applyTextLabelBorder();
                    TextTokenComponent.this.applyTextLabelColor();
                }
            }
        };
        this.textLabel.setOpaque(true);
        this.textLabel.setDoubleBuffered(true);
        this.applyTextLabelBorder();
        this.applyTextLabelColor();
        this.add(this.textLabel, BorderLayout.PAGE_END);
        this.labels = new JLabel[] { openBracketLabel, detailLabel, closingBracketLabel, this.textLabel };
    }

    @Override
    public void updateUI() {
        super.updateUI();
        this.setBackground(UIManager.getLookAndFeelDefaults().getColor("TextPane.background"));
    }

    /**
     * Set the text label's border. This includes the colored top border, depending on the user's preferences for the assigned detail category, and
     * the separators between detail sections (i.e. the first and last token with the same assigned detail category).
     */
    void applyTextLabelBorder() {
        final Border topBorder;
        if (this.model.getDetail() == null) {
            topBorder = BorderFactory.createEmptyBorder(2, 0, 0, 0);
        } else {
            topBorder = BorderFactory.createMatteBorder(2, 0, 0, 0, this.model.getDetail().getColor());
        }
        final Border sideBorder;
        final boolean showSectionStart = this.model.isFirstTokenOfDetail() && this.model.getPreviousToken() != null;
        final boolean showSectionEnd = this.model.isLastTokenOfDetail() && this.model.getFollowingToken() != null;
        if (showSectionStart || showSectionEnd) {
            final int leadingSeparator = showSectionStart ? 2 : 0;
            final int trailingSeparator = showSectionEnd ? 2 : 0;
            final Color color = UIManager.getLookAndFeelDefaults().getColor("TextPane.caretForeground");
            sideBorder =
                    BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, leadingSeparator, 0, trailingSeparator, color),
                            BorderFactory.createEmptyBorder(2, 4 - leadingSeparator, 2, 4 - trailingSeparator));
        } else {
            sideBorder = BorderFactory.createEmptyBorder(2, 4, 2, 4);
        }
        this.textLabel.setBorder(BorderFactory.createCompoundBorder(topBorder, sideBorder));
    }

    /**
     * Apply the text label's background color depending on its current {@link #selected} state.
     */
    void applyTextLabelColor() {
        final String foregroundKey;
        final String backgroundKey;
        if (this.selected) {
            foregroundKey = "TextPane.selectionForeground";
            backgroundKey = "TextPane.selectionBackground";
        } else {
            foregroundKey = "TextPane.foreground";
            backgroundKey = "TextPane.background";
        }
        this.textLabel.setForeground(UIManager.getLookAndFeelDefaults().getColor(foregroundKey));
        this.textLabel.setBackground(UIManager.getLookAndFeelDefaults().getColor(backgroundKey));
    }

    /**
     * Getter for the represented model object.
     *
     * @return the represented model
     */
    public TextToken getModel() {
        return this.model;
    }

    /**
     * Getter for the selected flag, indicating if the represented token is currently selected and can therefore get a detail category assigned.
     *
     * @return if the represented token is currently selected
     */
    public boolean isSelected() {
        return this.selected;
    }

    /**
     * Make this component look like a selected/selected text part in a TextPane (or not).
     *
     * @param selected
     *            if it should be displayed like a selected text
     */
    public void setSelected(final boolean selected) {
        if (selected != this.selected) {
            this.selected = selected;
            this.applyTextLabelColor();
            this.textLabel.repaint();
        }
    }

    @Override
    public synchronized void addMouseListener(final MouseListener listener) {
        super.addMouseListener(listener);
        for (final JLabel singleLabel : this.labels) {
            singleLabel.addMouseListener(listener);
        }
    }

    @Override
    public synchronized void addMouseMotionListener(final MouseMotionListener listener) {
        super.addMouseMotionListener(listener);
        for (final JLabel singleLabel : this.labels) {
            singleLabel.addMouseMotionListener(listener);
        }
    }
}
