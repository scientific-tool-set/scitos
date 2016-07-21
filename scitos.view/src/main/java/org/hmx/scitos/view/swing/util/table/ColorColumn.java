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

package org.hmx.scitos.view.swing.util.table;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * Custom jtable cell renderer and editor for a {@link Color} value.
 */
public class ColorColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {

    /** The black and white border around the actual color, when in render mode. */
    private final Border innerRenderBorder;
    /** The actual label for displaying the color value, when in render mode. */
    private final JLabel renderLabel;

    /** The displayed color value, while in editor mode. */
    Color currentColor;
    /** The button for opening the color chooser dialog, when in editor mode. */
    private final JButton editButton = new JButton();
    /** The color chooser component for actually picking another color value, when in editor mode. */
    final JColorChooser colorChooser = new JColorChooser();
    /** The dialog containing the color chooser component, when in editor mode. */
    private final JDialog dialog;

    /**
     * Constructor.
     */
    public ColorColumn() {
        this.renderLabel = new JLabel();
        this.renderLabel.setOpaque(true);
        final Border outerInnerBorder = BorderFactory.createLineBorder(Color.WHITE, 1);
        final Border innerInnerBorder = BorderFactory.createLineBorder(Color.BLACK, 1);
        this.innerRenderBorder = BorderFactory.createCompoundBorder(outerInnerBorder, innerInnerBorder);
        this.editButton.setBorderPainted(false);
        this.editButton.addActionListener(this);
        this.dialog = JColorChooser.createDialog(this.editButton, "", true, this.colorChooser, new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                ColorColumn.this.currentColor = ColorColumn.this.colorChooser.getColor();
            }
        }, null);
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        this.colorChooser.setColor(this.currentColor);
        // show dialog and wait for user input
        this.dialog.setVisible(true);
        // dialog is closed, return jtable to normal state
        this.fireEditingStopped();
    }

    @Override
    public JLabel getTableCellRendererComponent(final JTable table, final Object color, final boolean isSelected, final boolean hasFocus,
            final int row, final int column) {
        final Color background;
        if (isSelected) {
            background = table.getSelectionBackground();
        } else {
            background = table.getBackground();
        }
        if (color == null) {
            this.renderLabel.setBackground(background);
            this.renderLabel.setBorder(null);
        } else {
            this.renderLabel.setBackground((Color) color);
            this.renderLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 4, 1, 4, background),
                    this.innerRenderBorder));
        }
        return this.renderLabel;
    }

    @Override
    public JButton getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
        this.currentColor = (Color) value;
        this.editButton.setBackground(this.currentColor);
        return this.editButton;
    }

    @Override
    public Color getCellEditorValue() {
        return this.currentColor;
    }
}
