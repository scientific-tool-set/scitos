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

package org.hmx.scitos.ais.view.swing.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.AbstractMap.SimpleEntry;
import java.util.EventObject;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreePath;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.IBarRow;

import org.hmx.scitos.ais.core.i18n.AisMessage;
import org.hmx.scitos.ais.domain.IDetailCategoryProvider;
import org.hmx.scitos.ais.domain.model.DetailCategory;
import org.hmx.scitos.ais.domain.model.MutableDetailCategoryModel;
import org.hmx.scitos.view.ScitosIcon;
import org.hmx.scitos.view.swing.MessageHandler;
import org.hmx.scitos.view.swing.MessageHandler.MessageType;
import org.hmx.scitos.view.swing.util.Validation;
import org.hmx.scitos.view.swing.util.table.ColorColumn;
import org.hmx.scitos.view.swing.util.table.KeyStrokeColumn;
import org.hmx.scitos.view.swing.util.table.TreeTable;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * TreeTable displaying the editable {@link DetailCategoryTreeModel}.
 */
public final class DetailCategoryTreeTable extends JPanel {

    /** The wrapped tree jtable component. */
    final TreeTable treeTable;
    /** The leading (i.e. hierarchical) column for the detail category code. */
    private final TableColumnExt codeColumn;
    /** The tree jtable model containing the model entries that are being displayed/modified. */
    final DetailCategoryTreeModel treeModel;
    /** The check box below the tree jtable component, to allow to set the resulting model as default for new projects. */
    private final JCheckBox setAsDefaultCheckBox = new JCheckBox(AisMessage.DETAIL_CATEGORIES_AS_DEFAULT.get());

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
        super(null);
        this.setBorder(null);
        this.treeModel = new DetailCategoryTreeModel(categoryProvider);
        this.treeTable = new TreeTable(this.treeModel) {

            @Override
            public TableCellEditor getCellEditor(final int row, final int column) {
                final TableCellEditor editor = super.getCellEditor(row, column);
                if (column == 0) {
                    return new CategoryCodeEditor(editor);
                }
                return editor;
            }
        };
        this.treeTable.setAlwaysExpanded();
        this.treeTable.setVisibleRowCount(5);
        this.codeColumn = this.treeTable.addColumn(90, 110);
        this.treeTable.addColumn(200, Integer.MAX_VALUE);
        final ColorColumn colorColumn = new ColorColumn();
        this.treeTable.addColumn(50, 50, colorColumn, colorColumn);
        final KeyStrokeColumn shortColumnRenderer = new KeyStrokeColumn(this.treeTable.getDefaultEditor(String.class));
        this.treeTable.addColumn(80, 100, shortColumnRenderer, shortColumnRenderer);
        this.treeTable.addButtonColumn(ScitosIcon.ADD, new AbstractAction() {

            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                final int clickedRowIndex = Integer.valueOf(actionEvent.getActionCommand()).intValue();
                final TreePath parentPath = DetailCategoryTreeTable.this.treeTable.getPathForRow(clickedRowIndex);
                DetailCategoryTreeTable.this.addChildCategory(parentPath);
            }
        });
        final TableColumnExt deleteColumn = this.treeTable.addButtonColumn(ScitosIcon.DELETE, new AbstractAction() {

            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                final int clickedRowIndex = Integer.valueOf(actionEvent.getActionCommand()).intValue();
                final TreePath entryPath = DetailCategoryTreeTable.this.treeTable.getPathForRow(clickedRowIndex);
                DetailCategoryTreeTable.this.removeCategory(entryPath);
            }
        });
        this.treeTable.applyRowHeight(deleteColumn, "");
        this.setAsDefaultCheckBox.setSelected(!showSetAsDefaultCheckBox);
        final JButton addRootCategoryButton = new JButton(AisMessage.DETAIL_CATEGORY_ADD_ROOT.get(), ScitosIcon.ADD.create());
        addRootCategoryButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                DetailCategoryTreeTable.this.addRootCategory();
            }
        });
        final DesignGridLayout layout = new DesignGridLayout(this).withoutConsistentWidthAcrossNonGridRows();
        layout.row().center().add(this.treeTable.createScrollableWrapper()).fill();
        final IBarRow buttonRow = layout.row().bar().right(addRootCategoryButton);
        if (showSetAsDefaultCheckBox) {
            buttonRow.left(this.setAsDefaultCheckBox);
        }
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
        final boolean result = this.treeModel.isValid();
        if (!result) {
            MessageHandler.showMessage(AisMessage.DETAIL_CATEGORIES_INVALID.get(), "", MessageType.WARN);
        }
        return result;
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
}
