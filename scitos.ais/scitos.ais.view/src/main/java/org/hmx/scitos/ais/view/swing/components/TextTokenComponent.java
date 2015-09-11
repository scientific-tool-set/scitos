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
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.hmx.scitos.ais.domain.model.DetailCategory;
import org.hmx.scitos.ais.domain.model.TextToken;
import org.hmx.scitos.view.swing.ScitosApp;
import org.hmx.scitos.view.swing.components.ScaledLabel;

/**
 * Component representing a single token (usually a word), in an interview. The top half is dedicated to the display of the assigned detail category.
 * The bottom half contains the actual text of the token.
 */
public final class TextTokenComponent extends JPanel {

    /** The represented model object. */
    private final TextToken model;
    /** The (center) top part of the component, showing the assigned detail category. */
    private final ScaledLabel detailLabel;
    /**
     * The bottom half of the component, displaying the token's text. It also shows the changeable {@link #selected} state.
     */
    final ScaledLabel textLabel;
    /** If the token should be displayed as selected, in order to assign a detail category to (i.e. score) it. */
    private boolean selected;

    /**
     * Main constructor.
     *
     * @param model
     *            represented model object
     */
    public TextTokenComponent(final TextToken model) {
        super(new BorderLayout());
        this.model = model;
        this.setBorder(null);
        final ScaledLabel openBracketLabel = new ScaledLabel("(");
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
        this.detailLabel = new ScaledLabel(detailCode);
        this.detailLabel.setOpaque(false);
        this.add(this.detailLabel, BorderLayout.CENTER);
        final ScaledLabel closeBracketLabel = new ScaledLabel(")");
        closeBracketLabel.setOpaque(false);
        closeBracketLabel.setVisible(model.isLastTokenOfDetail() && model.getFollowingToken() != null
                && !model.getFollowingToken().isFirstTokenOfDetail());
        this.add(closeBracketLabel, BorderLayout.LINE_END);
        this.textLabel = new ScaledLabel(model.getText(), "TextPane.font", SwingConstants.CENTER);
        this.textLabel.setOpaque(true);
        this.textLabel.setDoubleBuffered(true);
        this.applyScaledSpacing();
        this.applyTextLabelColor();
        this.add(this.textLabel, BorderLayout.PAGE_END);
        final DetailCategory assigned = model.getDetail();
        if (assigned != null) {
            this.detailLabel.setToolTipText(assigned.getName());
            this.textLabel.setToolTipText(assigned.getName());
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        final Color background = UIManager.getColor("TextPane.background");
        this.setBackground(background == null ? Color.WHITE : new Color(background.getRGB()));
        if (TextTokenComponent.this.textLabel != null) {
            TextTokenComponent.this.applyScaledSpacing();
            TextTokenComponent.this.applyTextLabelColor();
        }
    }

    /**
     * Set the text label's border. This includes the colored top border, depending on the user's preferences for the assigned detail category, and
     * the separators between detail sections (i.e. the first and last token with the same assigned detail category).
     */
    void applyScaledSpacing() {
        final float scaleFactor;
        if (ScitosApp.getClient() == null) {
            scaleFactor = 1f;
        } else {
            scaleFactor = ScitosApp.getClient().getContentScaleFactor();
        }
        final int halfGap = Math.round(scaleFactor);
        final int fullGap = Math.round(2 * scaleFactor);
        final BorderLayout mainLayout = (BorderLayout) this.getLayout();
        mainLayout.setHgap(halfGap);
        mainLayout.setVgap(halfGap);
        this.detailLabel.setBorder(BorderFactory.createEmptyBorder(halfGap, fullGap, halfGap, fullGap));
        final Border topBorder;
        if (this.model.getDetail() == null) {
            topBorder = BorderFactory.createEmptyBorder(fullGap, 0, 0, 0);
        } else {
            topBorder = BorderFactory.createMatteBorder(fullGap, 0, 0, 0, this.model.getDetail().getColor());
        }
        final Border sideBorder;
        final int doubleGap = Math.round(4 * scaleFactor);
        final boolean showSectionStart = this.model.isFirstTokenOfDetail() && this.model.getPreviousToken() != null;
        final boolean showSectionEnd = this.model.isLastTokenOfDetail() && this.model.getFollowingToken() != null;
        if (showSectionStart || showSectionEnd) {
            final int leading = showSectionStart ? fullGap : 0;
            final int trailing = showSectionEnd ? fullGap : 0;
            Color color = UIManager.getColor("TextPane.caretForeground");
            color = color == null ? Color.BLACK : new Color(color.getRGB());
            final Border coloredTopBorder = BorderFactory.createMatteBorder(0, leading, 0, trailing, color);
            final Border emptySpacing = BorderFactory.createEmptyBorder(fullGap, doubleGap - leading, fullGap, doubleGap - trailing);
            sideBorder = BorderFactory.createCompoundBorder(coloredTopBorder, emptySpacing);
        } else {
            sideBorder = BorderFactory.createEmptyBorder(fullGap, doubleGap, fullGap, doubleGap);
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
        final Color foreground = UIManager.getColor(foregroundKey);
        final Color background = UIManager.getColor(backgroundKey);
        this.textLabel.setForeground(foreground == null ? null : new Color(foreground.getRGB()));
        this.textLabel.setBackground(background == null ? null : new Color(background.getRGB()));
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
        this.detailLabel.addMouseListener(listener);
        this.textLabel.addMouseListener(listener);
    }

    @Override
    public synchronized void addMouseMotionListener(final MouseMotionListener listener) {
        super.addMouseMotionListener(listener);
        this.detailLabel.addMouseMotionListener(listener);
        this.textLabel.addMouseMotionListener(listener);
    }
}
