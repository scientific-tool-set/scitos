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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/** Custom jtable cell renderer and editor for a key stroke (i.e. keyboard short cut) value. */
public class KeyStrokeColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {

    /** The default jtable cell editor to determine when editing should be started. */
    private final TableCellEditor referenceEditor;
    /** The label to display a key stroke value on, when in render mode. */
    private final JLabel renderLabel;
    /** The label to display a key stroke value on, when in editor mode. Imitating a text input field. */
    final JLabel inputLabel;
    /** The current key stroke value being edited. */
    private KeyStroke value;

    /**
     * Main constructor.
     *
     * @param referenceEditor
     *            the default jtable cell editor to determine when editing should be started
     */
    public KeyStrokeColumn(final TableCellEditor referenceEditor) {
        this.referenceEditor = referenceEditor;
        this.renderLabel = new JLabel("", SwingConstants.TRAILING);
        this.renderLabel.setOpaque(true);
        this.renderLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        this.inputLabel = new JLabel("", SwingConstants.TRAILING);
        this.inputLabel.setOpaque(true);
        this.inputLabel.setFocusable(true);
        this.inputLabel.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(final KeyEvent event) {
                KeyStrokeColumn.this.keyTyped(event.getKeyCode(), event.getModifiers());
            }
        });
    }

    /**
     * Validate the given combination of key code and active modifiers and apply the resulting key stroke to the currently edited row if valid.
     *
     * @param keyCode
     *            pressed key code value
     * @param modifiers
     *            active (i.e. pressed) modifier keys, while the actual key was pressed
     */
    void keyTyped(final int keyCode, final int modifiers) {
        if (KeyEvent.getKeyText(keyCode).matches("([\\w]|(F[0-9]+))")) {
            this.value = KeyStroke.getKeyStroke(keyCode, modifiers, true);
            this.fireEditingStopped();
        }
    }

    @Override
    public boolean isCellEditable(final EventObject event) {
        return this.referenceEditor.isCellEditable(event);
    }

    @Override
    public JLabel getTableCellRendererComponent(final JTable table, final Object keyStroke, final boolean isSelected, final boolean hasFocus,
            final int row, final int column) {
        if (isSelected) {
            this.renderLabel.setBackground(table.getSelectionBackground());
            this.renderLabel.setForeground(table.getSelectionForeground());
        } else {
            this.renderLabel.setBackground(table.getBackground());
            this.renderLabel.setForeground(table.getForeground());
        }
        this.renderLabel.setText(this.getKeyStrokeString((KeyStroke) keyStroke));
        return this.renderLabel;
    }

    @Override
    public JLabel getTableCellEditorComponent(final JTable table, final Object keyStroke, final boolean isSelected, final int row,
            final int column) {
        this.value = (KeyStroke) keyStroke;
        this.inputLabel.setText(this.getKeyStrokeString(this.value));
        this.inputLabel.setBackground(table.getBackground());
        this.inputLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK),
                BorderFactory.createEmptyBorder(0, 2, 0, 2)));
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                KeyStrokeColumn.this.inputLabel.requestFocusInWindow();
            }
        });
        return this.inputLabel;
    }

    @Override
    public KeyStroke getCellEditorValue() {
        return this.value;
    }

    /**
     * Create a textual representation of the given key stroke to be displayed.
     *
     * @param target
     *            key stroke to display
     * @return key stroke as string
     */
    private String getKeyStrokeString(final KeyStroke target) {
        final String keyStrokeText;
        if (target == null) {
            keyStrokeText = "";
        } else {
            final String modifierText;
            if (target.getModifiers() == 0) {
                modifierText = "";
            } else {
                modifierText = KeyEvent.getKeyModifiersText(target.getModifiers()) + '+';
            }
            keyStrokeText = modifierText + KeyEvent.getKeyText(target.getKeyCode());
        }
        return keyStrokeText;
    }
}