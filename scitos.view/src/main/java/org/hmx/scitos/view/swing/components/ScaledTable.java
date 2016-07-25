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

package org.hmx.scitos.view.swing.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.hmx.scitos.view.swing.ScitosApp;

/**
 * {@code JTable} extension that is scaling the displayed texts' {@link Font} according to the global (scaling factor) setting.
 */
public class ScaledTable extends JTable {

    /** The minimum width of a single table column. */
    private static final int MIN_COLUMN_WIDTH = 40;

    /**
     * Constructs a {@code ScaledTable} that is initialized with a default data model, a default column model, and a default selection model.
     * 
     * @param alternatingRowBackgroundColors
     *            whether the row should be displayed with alternating background colors
     * @param sortable
     *            whether the columns should be sortable
     */
    public ScaledTable(final boolean alternatingRowBackgroundColors, final boolean sortable) {
        super();
        this.setBorder(null);
        this.setAutoCreateColumnsFromModel(false);
        this.setAutoCreateRowSorter(sortable);
        this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.setRowSelectionAllowed(true);
        this.setColumnSelectionAllowed(false);
        if (alternatingRowBackgroundColors) {
            final AlternateRowRenderer cellRenderer = new AlternateRowRenderer();
            this.setDefaultRenderer(String.class, cellRenderer);
            this.setDefaultRenderer(Long.class, cellRenderer);
        }
    }

    /**
     * Constructs a {@code ScaledTable} that is initialized with the given data model, a default column model, and a default selection model.
     * 
     * @param model
     *            the data model for the table
     * @param alternatingRowBackgroundColors
     *            whether the row should be displayed with alternating background colors
     * @param sortable
     *            whether the columns should be sortable
     */
    public ScaledTable(final TableModel model, final boolean alternatingRowBackgroundColors, final boolean sortable) {
        this(alternatingRowBackgroundColors, sortable);
        this.setModel(model);
        this.setAutoCreateColumnsFromModel(true);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        final float scaleFactor = ScitosApp.getClient() == null ? 1f : ScitosApp.getClient().getContentScaleFactor();
        final Font headerFont = UIManager.getFont("TableHeader.font");
        if (headerFont != null) {
            this.getTableHeader().setFont(new Font(headerFont.getAttributes()).deriveFont(headerFont.getSize2D() * scaleFactor));
        }
        final Font contentFont = UIManager.getFont("Table.font");
        if (contentFont != null) {
            this.setFont(new Font(contentFont.getAttributes()).deriveFont(contentFont.getSize2D() * scaleFactor));
            this.setRowHeight(this.getRowMargin() + (int) Math.ceil(this.getFont().getSize2D() * scaleFactor));
        }
        this.adjustColumns();
    }

    @Override
    public void createDefaultColumnsFromModel() {
        super.createDefaultColumnsFromModel();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                ScaledTable.this.adjustColumns();
            }
        });
    }

    /** Adjust the widths of all columns to fit the respective column's header and contents. */
    void adjustColumns() {
        final int columnCount = this.getColumnCount();
        final int rowCount = this.getRowCount();
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            final TableColumn column = this.getColumnModel().getColumn(columnIndex);
            TableCellRenderer headerRenderer = column.getHeaderRenderer();
            if (headerRenderer == null) {
                headerRenderer = this.getTableHeader().getDefaultRenderer();
            }
            final Object headerValue = column.getHeaderValue();
            final Component headerCell = headerRenderer.getTableCellRendererComponent(this, headerValue, false, false, -1, columnIndex);
            int columnWidth = headerCell.getPreferredSize().width;
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                final TableCellRenderer cellRenderer = this.getCellRenderer(rowIndex, columnIndex);
                columnWidth = Math.max(columnWidth, this.prepareRenderer(cellRenderer, rowIndex, columnIndex).getPreferredSize().width);
            }
            final int preferredWidth = Math.max(MIN_COLUMN_WIDTH, columnWidth) + 4 + 2 * this.getIntercellSpacing().width;
            column.setMaxWidth(preferredWidth + MIN_COLUMN_WIDTH / 2);
            column.setPreferredWidth(preferredWidth);
        }
    }

    /** Cell renderer to apply alternating row background color in order to improve the table's readability. */
    private static final class AlternateRowRenderer extends DefaultTableCellRenderer {

        /** The alternate row color, for easier readability in the result tables. */
        private static final Color ALTERNATE_ROW_COLOR = new Color(239, 239, 239);

        /** Main constructor. */
        AlternateRowRenderer() {
            // nothing to setup
        }

        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus,
                final int row, final int column) {
            final JLabel component = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                final Color rowBackground;
                rowBackground = row % 2 == 0 ? Color.WHITE : ALTERNATE_ROW_COLOR;
                component.setBackground(rowBackground);
            }
            component.setHorizontalAlignment(value instanceof Number ? SwingConstants.TRAILING : SwingConstants.LEADING);
            return component;
        }
    }
}