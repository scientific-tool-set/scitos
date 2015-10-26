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
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.AbstractMap.SimpleEntry;
import java.util.EventObject;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import org.hmx.scitos.ais.core.i18n.AisMessage;
import org.hmx.scitos.ais.domain.IDetailCategoryProvider;
import org.hmx.scitos.ais.domain.model.DetailCategory;
import org.hmx.scitos.ais.domain.model.MutableDetailCategoryModel;
import org.hmx.scitos.view.ScitosIcon;
import org.hmx.scitos.view.swing.MessageHandler;
import org.hmx.scitos.view.swing.MessageHandler.MessageType;
import org.hmx.scitos.view.swing.util.Validation;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * TreeTable displaying the editable {@link DetailCategoryTreeModel}.
 */
public final class DetailCategoryTreeTable extends JPanel {

    /** The actual tree jtable component. */
    final JXTreeTable treeTable;
    /** The leading (i.e. hierarchical) column for the detail category code. */
    private final TableColumnExt codeColumn;
    /** The tree jtable model containing the model entries that are being displayed/modified. */
    final DetailCategoryTreeModel treeModel;
    /** The check box below the tree jtable component, to allow to set the resulting model as default for new projects. */
    private final JCheckBox setAsDefaultCheckBox;

    /**
     * Main constructor.
     *
     * @param categoryProvider
     *            the provider of the initial detail category model being displayed/modified
     * @param showSetAsDefaultCheckBox
     *            if the check box below the tree jtable component should be shown, in order to allow to set the resulting model as default for new
     *            projects
     */
    public DetailCategoryTreeTable(final IDetailCategoryProvider categoryProvider, final boolean showSetAsDefaultCheckBox) {
        super(new BorderLayout());
        this.treeModel = new DetailCategoryTreeModel(categoryProvider);
        this.setBorder(null);
        this.treeTable = new JXTreeTable(this.treeModel) {

            @Override
            public TableCellEditor getCellEditor(final int row, final int column) {
                final TableCellEditor editor = super.getCellEditor(row, column);
                if (column == 0) {
                    return new CategoryCodeEditor(editor);
                }
                return editor;
            }
        };
        this.treeTable.setAutoCreateColumnsFromModel(false);
        this.treeTable.setRootVisible(false);
        this.treeTable.setOpenIcon(ScitosIcon.FOLDER_OPEN.create());
        this.treeTable.setLeafIcon(ScitosIcon.CATEGORY.create());
        this.treeTable.setShowsRootHandles(false);
        this.treeTable.setAutoStartEditOnKeyStroke(false);

        for (final TableColumn singleColumn : this.treeTable.getColumns()) {
            this.treeTable.removeColumn(singleColumn);
        }
        this.codeColumn = this.createColumn(0, 90, 110, null, null);
        this.treeTable.addColumn(this.codeColumn);
        this.treeTable.addColumn(this.createColumn(1, 200, Integer.MAX_VALUE, null, null));
        final ColorColumn colorColumn = new ColorColumn();
        this.treeTable.addColumn(this.createColumn(2, 50, 50, colorColumn, colorColumn));
        final KeyStrokeColumn shortColumnRenderer = new KeyStrokeColumn(this.treeTable.getDefaultEditor(String.class));
        this.treeTable.addColumn(this.createColumn(3, 80, 100, shortColumnRenderer, shortColumnRenderer));
        final IconButtonColumn addButtonColumnRenderer = new IconButtonColumn(this.treeTable, ScitosIcon.ADD.create(), new AbstractAction() {

            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                final int clickedRowIndex = Integer.valueOf(actionEvent.getActionCommand()).intValue();
                DetailCategoryTreeTable.this.addChildCategory(DetailCategoryTreeTable.this.treeTable.getPathForRow(clickedRowIndex));
            }
        });
        this.treeTable.addColumn(this.createColumn(4, 40, 40, addButtonColumnRenderer, addButtonColumnRenderer));
        final IconButtonColumn deleteButtonColumnRenderer = new IconButtonColumn(this.treeTable, ScitosIcon.DELETE.create(), new AbstractAction() {

            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                final int clickedRowIndex = Integer.valueOf(actionEvent.getActionCommand()).intValue();
                DetailCategoryTreeTable.this.removeCategory(DetailCategoryTreeTable.this.treeTable.getPathForRow(clickedRowIndex));
            }
        });
        this.treeTable.addColumn(this.createColumn(5, 40, 40, deleteButtonColumnRenderer, deleteButtonColumnRenderer));
        final Component dummyDeleteButton = deleteButtonColumnRenderer.getTableCellRendererComponent(this.treeTable, "", false, false, 0, 5);
        this.treeTable.setRowHeight(dummyDeleteButton.getPreferredSize().height);
        this.treeTable.expandAll();
        this.treeTable.setVisibleRowCount(this.treeTable.getRowCount());
        this.treeTable.addTreeWillExpandListener(new TreeWillExpandListener() {

            @Override
            public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {
                // expanding is alright (it should always be expanded anyway)
            }

            @Override
            public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {
                // never collapse
                throw new ExpandVetoException(event);
            }
        });
        final JScrollPane scrollableTreeTable = new JScrollPane(this.treeTable);
        scrollableTreeTable.setBorder(null);
        this.add(scrollableTreeTable);
        final JPanel buttonWrapper = new JPanel(new GridBagLayout());
        buttonWrapper.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        this.setAsDefaultCheckBox = new JCheckBox(AisMessage.DETAIL_CATEGORIES_AS_DEFAULT.get());
        this.setAsDefaultCheckBox.setSelected(!showSetAsDefaultCheckBox);
        this.setAsDefaultCheckBox.setVisible(showSetAsDefaultCheckBox);
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.anchor = GridBagConstraints.BASELINE_LEADING;
        buttonWrapper.add(this.setAsDefaultCheckBox, constraints);
        final JPanel spacing = new JPanel();
        spacing.setOpaque(false);
        constraints.weightx = 1;
        constraints.gridx = 1;
        buttonWrapper.add(spacing, constraints);
        final JButton addRootCategoryButton = new JButton(AisMessage.DETAIL_CATEGORY_ADD_ROOT.get(), ScitosIcon.ADD.create());
        addRootCategoryButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                DetailCategoryTreeTable.this.addRootCategory();
            }
        });
        constraints.gridx = 2;
        constraints.weightx = 0;
        constraints.anchor = GridBagConstraints.BASELINE_TRAILING;
        buttonWrapper.add(addRootCategoryButton, constraints);
        this.add(buttonWrapper, BorderLayout.SOUTH);
    }

    /**
     * Create a single column for the tree jtable component with the given attributes.
     *
     * @param columnIndex
     *            the associated column index in the tree jtable model
     * @param minWidth
     *            the minimum column width
     * @param maxWidth
     *            the maximum column width
     * @param renderer
     *            the custom renderer to apply for this column (when not in editable state)
     * @param editor
     *            the custom editor to apply for this column
     * @return the create tree jtable column
     */
    private TableColumnExt createColumn(final int columnIndex, final int minWidth, final int maxWidth, final TableCellRenderer renderer,
            final TableCellEditor editor) {
        final TableColumnExt column = new TableColumnExt(columnIndex, minWidth);
        column.setMinWidth(minWidth);
        column.setMaxWidth(maxWidth);
        if (renderer != null) {
            column.setCellRenderer(renderer);
        }
        if (editor != null) {
            column.setCellEditor(editor);
        }
        return column;
    }

    /**
     * Check if the check box below the tree jtable component (to set the resulting model as default for new projects) is selected.
     *
     * @return if the check box is selected
     */
    public boolean isSetAsDefault() {
        return this.setAsDefaultCheckBox.isSelected();
    }

    /** Add a new top level detail category row. */
    void addRootCategory() {
        this.addChildCategory(new TreePath(this.treeModel.getRoot()));
    }

    /**
     * Add a new detail category row, subordinated under the given parent path.
     *
     * @param parentPath
     *            path to the category row the new one should be subordinated to
     */
    void addChildCategory(final TreePath parentPath) {
        this.treeModel.addChildCategoryRow(parentPath);
        final int minWidth = 70 + 20 * this.treeModel.getTreeDepth();
        if (this.codeColumn.getWidth() < minWidth) {
            this.codeColumn.setWidth(minWidth);
        }
        this.codeColumn.setMaxWidth(Math.max(minWidth, this.codeColumn.getMaxWidth()));
        this.codeColumn.setMinWidth(minWidth);
        this.treeTable.expandAll();
        this.treeTable.setVisibleRowCount(this.treeTable.getRowCount());
        this.revalidate();
    }

    /**
     * Remove the detail category row at the given path.
     *
     * @param targetPath
     *            path to the category row that should be removed
     */
    void removeCategory(final TreePath targetPath) {
        this.treeModel.removeCategoryRow(targetPath);
        this.codeColumn.setMinWidth(70 + 20 * this.treeModel.getTreeDepth());
        this.treeTable.expandAll();
        this.treeTable.setVisibleRowCount(this.treeTable.getRowCount());
        this.revalidate();
    }

    /**
     * Check if the current state of the contained detail category model is valid and can be extracted. If not, a message dialog is displayed.
     *
     * @return if the detail category model is valid
     * @see #toModel()
     * @see #toModelWithMapping()
     */
    boolean containsValidModel() {
        if (this.treeModel.isValid()) {
            return true;
        }
        MessageHandler.showMessage(AisMessage.DETAIL_CATEGORIES_INVALID.get(), "", MessageType.WARN);
        return false;
    }

    /**
     * Extract the current detail category model.
     *
     * @return the detail category model as it is currently displayed
     */
    public MutableDetailCategoryModel toModel() {
        return this.toModelWithMapping().getKey();
    }

    /**
     * Extract the current detail category model, including a mapping from old to new detail categories for all old detail category rows, that still
     * exist.
     *
     * @return the detail category model as it is currently displayed and the mapping which old categories should be replaced by which new ones
     */
    public SimpleEntry<MutableDetailCategoryModel, Map<DetailCategory, DetailCategory>> toModelWithMapping() {
        return this.treeModel.toModelWithMapping();
    }

    /** Custom jtable cell editor for the hierarchical column, allowing the input of a category's code value. */
    private class CategoryCodeEditor implements TableCellEditor {

        /** The default cell editor of the hierarchical column. */
        private final TableCellEditor wrappedEditor;
        /** The index of the currently edited row. */
        private int editedRow;
        /**
         * Flag if the stopped editing should submit the current value, or ignore it.
         * <ul>
         * <li>{@code null} - after initialization, editing still in progress</li>
         * <li>{@code true} - {@link #cancelCellEditing()} has been invoked to stop the editing (ignore current value)</li>
         * <li>{@code false} - {@link #stopCellEditing()} has been invoked to stop the editing (submit current value)</li>
         * </ul>
         */
        private Boolean wasCancelled;
        /** The actual input field. */
        JTextField input;

        /**
         * Main constructor.
         *
         * @param wrappedEditor
         *            the default cell editor of the hierarchical column
         */
        CategoryCodeEditor(final TableCellEditor wrappedEditor) {
            this.wrappedEditor = wrappedEditor;
        }

        @Override
        public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row,
                final int column) {
            this.editedRow = row;
            this.wasCancelled = null;
            this.input = (JTextField) this.wrappedEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
            // allow only up to five characters/symbols
            this.input.setDocument(new Validation(5));
            this.input.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            this.input.setText((String) value);
            // make sure the ENTER key validates the input as well
            final String enterKeyAction = KeyEvent.getKeyText(KeyEvent.VK_ENTER);
            this.input.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), enterKeyAction);
            this.input.getActionMap().put(enterKeyAction, new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent event) {
                    if (CategoryCodeEditor.this.stopCellEditing()) {
                        table.getModel().setValueAt(CategoryCodeEditor.this.input.getText().trim(), row, column);
                    }
                }
            });
            return this.input;
        }

        @Override
        public boolean stopCellEditing() {
            final String editingValue = this.input.getText().trim();
            if ((this.wasCancelled == null || !this.wasCancelled) && (editingValue.isEmpty() || this.isCodeAlreadyDefined(editingValue))) {
                this.input.setBorder(BorderFactory.createLineBorder(Color.RED));
                this.input.selectAll();
                MessageHandler.showMessage(AisMessage.DETAIL_CATEGORY_CODE_DUPLICATE.get(), "", MessageType.ERROR);
                this.input.requestFocusInWindow();
                return false;
            }
            this.wasCancelled = false;
            return this.wrappedEditor.stopCellEditing();
        }

        /**
         * Check if the given category code is already in use, in order to avoid using the same code for multiple detail categories.
         *
         * @param code
         *            inserted code value to check
         * @return if the code can be applied to the currently edited row without invalidating the model
         */
        private boolean isCodeAlreadyDefined(final String code) {
            final JTable table = (JTable) this.input.getParent();
            final int rowCount = table.getRowCount();
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                if (code.equalsIgnoreCase((String) table.getValueAt(rowIndex, 0)) && this.editedRow != rowIndex) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void cancelCellEditing() {
            this.wrappedEditor.cancelCellEditing();
            this.wasCancelled = true;
        }

        @Override
        public Object getCellEditorValue() {
            return this.wrappedEditor.getCellEditorValue();
        }

        @Override
        public void addCellEditorListener(final CellEditorListener listener) {
            this.wrappedEditor.addCellEditorListener(listener);
        }

        @Override
        public void removeCellEditorListener(final CellEditorListener listener) {
            this.wrappedEditor.removeCellEditorListener(listener);
        }

        @Override
        public boolean isCellEditable(final EventObject anEvent) {
            return this.wrappedEditor.isCellEditable(anEvent);
        }

        @Override
        public boolean shouldSelectCell(final EventObject anEvent) {
            return this.wrappedEditor.shouldSelectCell(anEvent);
        }
    }

    /**
     * Custom jtable cell renderer and editor for a {@link Color} value.
     */
    private static class ColorColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {

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
         * Main constructor.
         */
        ColorColumn() {
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
        public JButton getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row,
                final int column) {
            this.currentColor = (Color) value;
            this.editButton.setBackground(this.currentColor);
            return this.editButton;
        }

        @Override
        public Color getCellEditorValue() {
            return this.currentColor;
        }
    }

    /** Custom jtable cell renderer and editor for a key stroke (i.e. keyboard short cut) value. */
    private static class KeyStrokeColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {

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
        KeyStrokeColumn(final TableCellEditor referenceEditor) {
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

    /** Custom table cell renderer and editor, displaying an icon button and executing the associated action when pressed. */
    private static class IconButtonColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener, MouseListener {

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
        IconButtonColumn(final JTable table, final Icon icon, final Action action) {
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
        public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus,
                final int row, final int column) {
            if (value != null) {
                this.renderButton.setToolTipText(value.toString());
                if (isSelected) {
                    this.renderButton.setForeground(table.getSelectionForeground());
                    this.renderButton.setBackground(table.getSelectionBackground());
                } else {
                    this.renderButton.setForeground(table.getForeground());
                    this.renderButton.setBackground(table.getSelectionBackground());
                }
                return this.renderButton;
            }
            if (isSelected) {
                this.noButtonLabel.setBackground(table.getSelectionBackground());
            } else {
                this.noButtonLabel.setBackground(table.getBackground());
            }
            return this.noButtonLabel;
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
}
