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

package org.hmx.scitos.hmx.view.swing.elements;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JPanel;

import org.hmx.scitos.hmx.core.option.HmxGeneralOption;

/**
 * simple {@link JPanel} with a colored horizontal line in a vertically centered position to fill gaps between a {@link ViewRelation} and its
 * subordinated elements.
 */
public final class ViewRelationExtender extends JPanel {

    /**
     * relation line color.
     */
    private final Color color;

    /**
     * creates a new {@link SemRelationExtender} without any special functions.
     */
    public ViewRelationExtender() {
        super(null);
        this.setBorder(null);
        this.color = HmxGeneralOption.RELATION_COLOR.getValueAsColor();
    }

    /**
     * draw the colored line from the left to the right of the {@link JPanel} to fill a gap of one depth level.
     *
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    @Override
    protected void paintComponent(final Graphics graphics) {
        final Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setColor(this.color);
        final Rectangle line =
                new Rectangle(0, (this.getSize().height - ViewRelation.COMMENT_BORDER.getBorderInsets(this).bottom) / 2, this.getSize().width,
                        (2 * ViewRelation.HALF_LINE_THICKNESS));
        graphics2D.draw(line);
        graphics2D.fill(line);
    }
}
