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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/** Custom table cell renderer and editor, displaying an icon button and executing the associated action when pressed. */
public class IconButtonColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener, MouseListener {

    /** The table component this renderer/editor belongs to. */
    final JTable jtable;
    /** The action to be executed on button press. */
    private final Action action;

    /** The icon button being displayed, when in render mode. */
    private final JButton renderButton;
    /** The icon button being displayed, when in editor mode. */
    private final JButton editButton;
    /** The label being displayed in rows, where no button/action should be available. */
    private final JLabel noButtonLabel;
    /** The value of the currently edited cell (which is never being changed). */
    private Object editorValue;
    /** If the registered mouse click happened on the associated column – in order to validate the mouse released event. */
    private boolean isButtonColumnEditor;

    /**
     * Create the IconButtonColumn to be used as a renderer and editor. The renderer and editor will automatically be installed on the TableColumn
     * of the specified column.
     *
     * @param table
     *            the table containing the button renderer/editor
     * @param icon
     *            the Icon to display on the button
     * @param action
     *            the Action to be invoked when the button is invoked
     */
    public IconButtonColumn(final JTable table, final Icon icon, final Action action) {
        this.jtable = table;
        this.action = action;
        this.renderButton = new JButton(icon);
        this.editButton = new JButton(icon);
        this.editButton.setFocusable(false);
        this.editButton.addActionListener(this);
        this.noButtonLabel = new JLabel();
        this.noButtonLabel.setOpaque(true);
        table.addMouseListener(this);
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row,
            final int column) {
        this.editorValue = value;
        this.editButton.setToolTipText(value.toString());
        return this.editButton;
    }

    @Override
    public Object getCellEditorValue() {
        return this.editorValue;
    }

    //
    // Implement TableCellRenderer interface
    //
    @Override
    public JComponent getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus,
            final int row, final int column) {
        final JComponent renderComponent;
        if (value == null) {
            renderComponent = this.noButtonLabel;
        } else {
            this.renderButton.setToolTipText(value.toString());
            renderComponent = this.renderButton;
        }
        if (isSelected) {
            renderComponent.setForeground(table.getSelectionForeground());
            renderComponent.setBackground(table.getSelectionBackground());
        } else {
            renderComponent.setForeground(table.getForeground());
            renderComponent.setBackground(table.getBackground());
        }
        renderComponent.setEnabled(table.isEnabled());
        return renderComponent;
    }

    //
    // Implement ActionListener interface
    //
    /*
     * The button has been pressed. Stop editing and invoke the custom Action
     */
    @Override
    public void actionPerformed(final ActionEvent event) {
        final int row = this.jtable.convertRowIndexToModel(this.jtable.getEditingRow());
        this.fireEditingStopped();
        // Invoke the Action
        this.action.actionPerformed(new ActionEvent(this.jtable, ActionEvent.ACTION_PERFORMED, String.valueOf(row)));
    }

    //
    // Implement MouseListener interface
    //
    /*
     * When the mouse is pressed the editor is invoked. If you then then drag the mouse to another cell before releasing it, the editor is still
     * active. Make sure editing is stopped when the mouse is released.
     */
    @Override
    public void mousePressed(final MouseEvent event) {
        this.isButtonColumnEditor = this.jtable.isEditing() && this.jtable.getCellEditor() == this;
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
        if (this.isButtonColumnEditor && this.jtable.isEditing()) {
            this.jtable.getCellEditor().stopCellEditing();
        }
        this.isButtonColumnEditor = false;
    }

    @Override
    public void mouseClicked(final MouseEvent event) {
        // nothing to do
    }

    @Override
    public void mouseEntered(final MouseEvent event) {
        // nothing to do
    }

    @Override
    public void mouseExited(final MouseEvent event) {
        // nothing to do
    }
}