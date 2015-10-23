package org.hmx.scitos.hmx.view.swing.elements;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JPanel;

import org.hmx.scitos.hmx.core.option.HmxGeneralOption;

/**
 * simple {@link JPanel} with a colored horizontal in a vertically centered position to fill gaps between a {@link SemRelation} and its subordinated
 * elements
 */
public final class SemRelationExtender extends JPanel {

    /**
     * relation line color
     */
    private final Color color;

    /**
     * creates a new {@link SemRelationExtender} without any special functions
     */
    public SemRelationExtender() {
        super(null);
        this.setBorder(null);
        this.color = HmxGeneralOption.RELATION_COLOR.getValueAsColor();
    }

    /**
     * draw the colored line from the left to the right of the {@link JPanel} to fill a gap of one depth level
     *
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    @Override
    protected void paintComponent(final Graphics graphics) {
        final Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setColor(this.color);
        final Rectangle line =
                new Rectangle(0, (this.getSize().height - SemRelation.COMMENT_BORDER.getBorderInsets(this).bottom) / 2, this.getSize().width,
                        (2 * SemRelation.HALF_LINE_THICKNESS));
        graphics2D.draw(line);
        graphics2D.fill(line);
    }
}
