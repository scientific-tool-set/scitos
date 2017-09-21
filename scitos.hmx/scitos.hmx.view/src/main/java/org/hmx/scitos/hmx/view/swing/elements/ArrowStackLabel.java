/*
   Copyright (C) 2017 HermeneutiX.org

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

import java.awt.Font;
import javax.swing.SwingConstants;
import org.hmx.scitos.hmx.core.option.HmxGeneralOption;
import org.hmx.scitos.view.swing.components.ScaledLabel;

/**
 * A stack of arrows pointing up or down to indicate the connection of two {@link Proposition} parts with enclosed children.
 */
final class ArrowStackLabel extends ScaledLabel {

    /** The base font size to apply – will be scaled with global setting. */
    private static final int BASE_ARROW_FONT_SIZE = 20;

    /** If the displayed arrows are pointing from bottom to top; else top to bottom. */
    private final boolean upward;
    /** The current number of arrows being displayed. */
    private int arrowCount = 0;

    /**
     * Constructor.
     *
     * @param pointsUp flag if the arrows should point upwards
     * @param count number of arrows to display
     */
    ArrowStackLabel(final boolean pointsUp, final int count) {
        super(ArrowStackLabel.buildArrowString(pointsUp, count), SwingConstants.CENTER);
        this.upward = pointsUp;
        this.setFont(new Font("Times New Roman", Font.BOLD, ArrowStackLabel.BASE_ARROW_FONT_SIZE));
        this.setForeground(HmxGeneralOption.ARROW_COLOR.getValueAsColor());
        this.setArrowCount(count);
    }

    /**
     * Setter for the number of displayed arrows.
     *
     * @param count number of displayed arrows to set
     */
    void setArrowCount(final int count) {
        if (this.arrowCount != count) {
            this.arrowCount = count;
            if (count < 1) {
                this.setVisible(false);
                this.setText("");
            } else {
                this.setText(ArrowStackLabel.buildArrowString(this.upward, count));
                this.setSize(this.getPreferredSize());
                this.setVisible(true);
            }
        }
    }

    /**
     * Construct a string with the given number of arrows pointing in the indicating direction.
     *
     * @param pointsUp if the arrows should point upwards, otherwise downwards
     * @param arrowCount number arrows to include
     * @return string of unicode arrows
     */
    private static String buildArrowString(final boolean pointsUp, final int arrowCount) {
        if (arrowCount < 1) {
            return "";
        }
        final char singleArrow = pointsUp ? '\u2191' : '\u2193';
        final StringBuffer arrows = new StringBuffer(arrowCount);
        for (int i = 0; i < arrowCount; i++) {
            arrows.append(singleArrow);
        }
        return arrows.toString();
    }
}
