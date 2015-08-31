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

package org.hmx.scitos.view.swing.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * FlowLayout subclass that fully supports wrapping of components.
 *
 * @author Rob Camick (<a href="https://tips4java.wordpress.com/2008/11/06/wrap-layout/">Source</a>)
 */
public class WrapLayout extends FlowLayout {

    /**
     * Constructs a new <code>WrapLayout</code> with a left alignment and a default 5-unit horizontal and vertical gap.
     */
    public WrapLayout() {
        super();
    }

    /**
     * Constructs a new <code>WrapLayout</code> with the specified alignment and a default 5-unit horizontal and vertical gap. The value of the
     * alignment argument must be one of {@link FlowLayout#CENTER}, {@link FlowLayout#LEADING}, or {@link FlowLayout#TRAILING}.
     *
     * @param align
     *            the alignment value
     */
    public WrapLayout(final int align) {
        super(align);
    }

    /**
     * Creates a new <code>WrapLayout</code> with the indicated alignment and the indicated horizontal and vertical gaps. The value of the alignment
     * argument must be one of {@link FlowLayout#CENTER}, {@link FlowLayout#LEADING}, or {@link FlowLayout#TRAILING}.
     *
     * @param align
     *            the alignment value
     * @param hgap
     *            the horizontal gap between components
     * @param vgap
     *            the vertical gap between components
     */
    public WrapLayout(final int align, final int hgap, final int vgap) {
        super(align, hgap, vgap);
    }

    /**
     * Returns the preferred dimensions for this layout given the <i>visible</i> components in the specified target container.
     *
     * @param target
     *            the component which needs to be laid out
     * @return the preferred dimensions to lay out the subcomponents of the specified container
     */
    @Override
    public Dimension preferredLayoutSize(final Container target) {
        return this.layoutSize(target, true);
    }

    /**
     * Returns the minimum dimensions needed to layout the <i>visible</i> components contained in the specified target container.
     *
     * @param target
     *            the component which needs to be laid out
     * @return the minimum dimensions to lay out the subcomponents of the specified container
     */
    @Override
    public Dimension minimumLayoutSize(final Container target) {
        final Dimension minimum = this.layoutSize(target, false);
        minimum.width -= this.getHgap() + 1;
        return minimum;
    }

    /**
     * Returns the minimum or preferred dimension needed to layout the target container.
     *
     * @param target
     *            target to get layout size for
     * @param preferred
     *            should preferred size be calculated
     * @return the dimension to layout the target container
     */
    private Dimension layoutSize(final Container target, final boolean preferred) {
        synchronized (target.getTreeLock()) {
            /*
             * Each row must fit with the width allocated to the container. When the container width = 0, the preferred width of the container has not
             * yet been calculated so lets ask for the maximum.
             */
            int targetWidth;
            Container container = target;
            while (container.getSize().width == 0 && container.getParent() != null) {
                container = container.getParent();
            }
            targetWidth = container.getSize().width;
            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE;
            }
            final int hgap = this.getHgap();
            final int vgap = this.getVgap();
            final Insets insets = target.getInsets();
            final int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
            final int maxWidth = targetWidth - horizontalInsetsAndGap;
            // Fit components into the allowed width
            final Dimension containerSize = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;
            for (final Component member : target.getComponents()) {
                if (member.isVisible()) {
                    final Dimension memberSize;
                    if (preferred) {
                        memberSize = member.getPreferredSize();
                    } else {
                        memberSize = member.getMinimumSize();
                    }
                    // Can't add the component to current row. Start a new row.
                    if (rowWidth + memberSize.width > maxWidth) {
                        this.addRow(containerSize, rowWidth, rowHeight);
                        rowWidth = 0;
                        rowHeight = 0;
                    }
                    // Add a horizontal gap for all components after the first
                    if (rowWidth != 0) {
                        rowWidth += hgap;
                    }
                    rowWidth += memberSize.width;
                    rowHeight = Math.max(rowHeight, memberSize.height);
                }
            }
            this.addRow(containerSize, rowWidth, rowHeight);
            containerSize.width += horizontalInsetsAndGap;
            containerSize.height += insets.top + insets.bottom + vgap * 2;
            /*
             * When using a scroll pane or the DecoratedLookAndFeel we need to make sure the preferred size is less than the size of the target
             * container so shrinking the container size works correctly. Removing the horizontal gap is an easy way to do this.
             */
            final Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
            if (scrollPane != null && target.isValid()) {
                containerSize.width -= (hgap + 1);
            }
            return containerSize;
        }
    }

    /**
     * A new row has been completed. Use the dimensions of this row to update the preferred size for the container.
     *
     * @param dim
     *            update the width and height when appropriate
     *
     * @param rowWidth
     *            the width of the row to add
     *
     * @param rowHeight
     *            the height of the row to add
     */
    private void addRow(final Dimension dim, final int rowWidth, final int rowHeight) {
        dim.width = Math.max(dim.width, rowWidth);
        if (dim.height > 0) {
            dim.height += this.getVgap();
        }
        dim.height += rowHeight;
    }
}
