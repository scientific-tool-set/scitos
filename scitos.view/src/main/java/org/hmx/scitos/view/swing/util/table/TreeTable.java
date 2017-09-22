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
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import org.hmx.scitos.domain.util.CollectionUtil;
import org.hmx.scitos.view.ScitosIcon;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.treetable.TreeTableModel;

/**
 * Convenience extension of JXTreeTable.
 */
public class TreeTable extends JXTreeTable {

    /**
     * Constructor.
     *
     * @param treeModel model for this JXTreeTable instance
     */
    public TreeTable(final TreeTableModel treeModel) {
        this.setAutoCreateColumnsFromModel(false);
        this.setTreeTableModel(treeModel);
        this.setRootVisible(false);
        this.setOpenIcon(ScitosIcon.FOLDER_OPEN.create());
        this.setClosedIcon(ScitosIcon.FOLDER_OPEN.create());
        this.setLeafIcon(ScitosIcon.CLIPBOARD.create());
        this.setAutoStartEditOnKeyStroke(false);
    }

    /**
     * Ensure that the non-leaf nodes are never collapsed.
     *
     * @return this TreeTable instance (for chaining)
     */
    public TreeTable setAlwaysExpanded() {
        this.setShowsRootHandles(false);
        this.expandAll();
        this.addTreeWillExpandListener(new TreeWillExpandListener() {

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
        this.getTreeTableModel().addTreeModelListener(new TreeModelListener() {

            @Override
            public void treeNodesInserted(final TreeModelEvent event) {
                this.treeStructureChanged(event);
            }

            @Override
            public void treeNodesChanged(final TreeModelEvent event) {
                // nothing to do
            }

            @Override
            public void treeNodesRemoved(final TreeModelEvent event) {
                this.treeStructureChanged(event);
            }

            @Override
            public void treeStructureChanged(final TreeModelEvent event) {
                TreeTable.this.expandAll();
            }
        });
        return this;
    }

    /**
     * Wrap the this tree table in a borderless {@link JScrollPane}.
     *
     * @return the created scroll pane
     */
    public JScrollPane createScrollableWrapper() {
        final JScrollPane scrollableTreeTable = new JScrollPane(this);
        scrollableTreeTable.setBorder(null);
        return scrollableTreeTable;
    }

    /**
     * Add a single column to this tree table with the given attributes.
     *
     * @param minWidth the minimum column width
     * @param maxWidth the maximum column width
     * @return the created table column
     * @see #addColumn(int, int, TableCellRenderer, TableCellEditor)
     */
    public TableColumnExt addColumn(final int minWidth, final int maxWidth) {
        return this.addColumn(minWidth, maxWidth, null, null);
    }

    /**
     * Add a single column to this tree table with the given attributes.
     *
     * @param minWidth the minimum column width
     * @param maxWidth the maximum column width
     * @param renderer the (optional) custom renderer to apply for this column (when not in editable state)
     * @param editor the (optional) custom editor to apply for this column
     * @return the created table column
     */
    public TableColumnExt addColumn(final int minWidth, final int maxWidth, final TableCellRenderer renderer, final TableCellEditor editor) {
        final TableColumnExt column = new TableColumnExt(this.getColumnCount(), minWidth);
        column.setMinWidth(minWidth);
        column.setMaxWidth(maxWidth);
        if (renderer != null) {
            column.setCellRenderer(renderer);
        }
        if (editor != null) {
            column.setCellEditor(editor);
        }
        this.addColumn(column);
        return column;
    }

    /**
     * Add a single column to this tree table with the given attributes and the default table cell renderer (with the addition of a tooltip that
     * displays the cell's value).
     *
     * @param minWidth the minimum column width
     * @param maxWidth the maximum column width
     * @return the created table column
     */
    public TableColumnExt addColumnWithToolTip(final int minWidth, final int maxWidth) {
        final TableCellRenderer renderer = new DefaultTableCellRenderer() {

            @Override
            public JComponent getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
                    final boolean hasFocus, final int row, final int column) {
                final JComponent renderLabel = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (table.isEnabled()) {
                    final String valueString = value == null ? "" : value.toString().trim();
                    if (valueString.isEmpty()) {
                        renderLabel.setToolTipText(null);
                    } else {
                        renderLabel.setToolTipText(valueString);
                    }
                }
                return renderLabel;
            }
        };
        return this.addColumn(minWidth, maxWidth, renderer, null);
    }

    /**
     * Add a single column to this tree table displaying an icon-only button.
     *
     * @param icon the button's icon to display
     * @param action the button's associated on-click action
     * @return the created table column
     */
    public TableColumnExt addButtonColumn(final ScitosIcon icon, final Action action) {
        final IconButtonColumn buttonColumnRenderer = new IconButtonColumn(this, icon.create(), action);
        return this.addColumn(40, 40, buttonColumnRenderer, buttonColumnRenderer);
    }

    /**
     * Set the handled table's row height according to the component rendered in the designated column for the provided value.
     *
     * @param referenceColumn column that determines the desired row height
     * @param referenceValue the value to use when calculating the preferred row height
     */
    public void applyRowHeight(final TableColumnExt referenceColumn, final Object referenceValue) {
        final TableCellRenderer renderer = referenceColumn.getCellRenderer();
        final int columnIndex = CollectionUtil.indexOfInstance(this.getColumns(), referenceColumn);
        final Component renderedElement = renderer.getTableCellRendererComponent(this, referenceValue, false, false, 0, columnIndex);
        this.setRowHeight(renderedElement.getPreferredSize().height);
    }

    /**
     * Determine the tree path to the currently selected row, if there is one.
     *
     * @return selected row's tree path
     */
    public TreePath getSelectedPath() {
        return this.getTreeSelectionModel().getSelectionPath();
    }

    /**
     * Set the selected tree path.
     *
     * @param path designated row's tree path to select
     */
    public void setSelectedPath(final TreePath path) {
        this.getTreeSelectionModel().setSelectionPath(path);
    }

    /**
     * Determine those paths under the same parent that are currently expanded.
     *
     * @param parentPath path to collect the expanded children for
     * @return the child paths that are expanded
     * @see #isExpanded(TreePath)
     */
    public List<TreePath> getExpandedChildren(final TreePath parentPath) {
        final Object parentNode = parentPath.getLastPathComponent();
        final int childCount = this.getTreeTableModel().getChildCount(parentNode);
        final List<TreePath> expandedChildren = new ArrayList<TreePath>(childCount);
        for (int index = 0; index < childCount; index++) {
            final TreePath siblingPath = parentPath.pathByAddingChild(this.getTreeTableModel().getChild(parentNode, index));
            if (this.isExpanded(siblingPath)) {
                expandedChildren.add(siblingPath);
            }
        }
        return expandedChildren;
    }

    /**
     * Expand the nodes at the given path in the tree table.
     *
     * @param targetPaths paths to the nodes to expand
     * @see #expandPath(TreePath)
     */
    public void expandPaths(final List<TreePath> targetPaths) {
        for (final TreePath pathToExpand : targetPaths) {
            this.expandPath(pathToExpand);
        }
    }
}
