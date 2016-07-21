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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.hmx.scitos.ais.core.i18n.AisMessage;
import org.hmx.scitos.ais.view.swing.AisViewProject;
import org.hmx.scitos.ais.view.swing.PatternAnalysisModel;
import org.hmx.scitos.core.ExportOption.TargetFileType;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.view.ScitosIcon;
import org.hmx.scitos.view.swing.ScitosApp;
import org.hmx.scitos.view.swing.ScitosClient;
import org.hmx.scitos.view.swing.util.ViewUtil;

/** Component displaying the summed up scoring results of a whole project. */
public final class PatternAnalysisPanel extends JPanel {

    /** The alternate row color, for easier readability in the result tables. */
    static final Color ALTERNATE_ROW_COLOR = new Color(239, 239, 239);
    /** The minimum width of a single table column. */
    private static final int MIN_COLUMN_WIDTH = 40;

    /** The associated view project, containing the interviews the displayed results are extracted from. */
    final PatternAnalysisModel model;

    /**
     * Main constructor.
     *
     * @param client
     *            active client instance, to request a save destination for the spreadsheet export from
     * @param project
     *            providing instance of the scored detail categories in the interviews
     */
    public PatternAnalysisPanel(final ScitosClient client, final AisViewProject project) {
        super(new BorderLayout(0, 5));
        this.model = new PatternAnalysisModel(project);
        this.setBorder(null);
        final JTabbedPane tabStack = new JTabbedPane(SwingConstants.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        tabStack.add(AisMessage.ANALYSIS_SUMMARY.get(), this.createTableFromModel(this.model.getSummaryTableModel(), true));
        tabStack.add(AisMessage.ANALYSIS_SEQUENCE.get(), this.createTableFromModel(this.model.getSequenceTableModel(), false));
        tabStack.add(AisMessage.ANALYSIS_PATTERN.get(), this.createTableFromModel(this.model.getPatternTableModel(), true));
        this.add(tabStack);
        this.addHierarchyListener(new HierarchyListener() {

            @Override
            public void hierarchyChanged(final HierarchyEvent event) {
                if ((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && PatternAnalysisPanel.this.isShowing()) {
                    // the panel (tab) is shown again, the displayed values might have change
                    new Thread("Recalculate Scoring Summary") {

                        @Override
                        public void run() {
                            PatternAnalysisPanel.this.refresh();
                        }
                    }.start();
                }
            }
        });

        final JButton exportButton = new JButton(AisMessage.ANALYSIS_EXPORT.get(), ScitosIcon.FILE_ODS.create());
        exportButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                final File target =
                        ViewUtil.getSaveDestination(client.getFrame(), TargetFileType.ODS.getExtension(), Message.MENUBAR_FILE_EXPORT.get(), false);
                if (target != null) {
                    PatternAnalysisPanel.this.model.exportToSpreadSheet(target);
                }
            }
        });
        this.add(exportButton, BorderLayout.SOUTH);
    }

    /** Force the tables to recalculate the displayed results. */
    void refresh() {
        this.model.reload();
    }

    /**
     * Create the scrollable sub view (i.e. tab) containing a table displaying the given model.
     *
     * @param tableModel
     *            table model to display
     * @param sortable
     *            if the columns should be sortable
     * @return scrollable table taking up the whole view
     */
    private JScrollPane createTableFromModel(final TableModel tableModel, final boolean sortable) {
        final JTable tableView = new JTable(tableModel) {

            @Override
            public void updateUI() {
                super.updateUI();
                final float scaleFactor;
                if (ScitosApp.getClient() == null) {
                    scaleFactor = 1f;
                } else {
                    scaleFactor = ScitosApp.getClient().getContentScaleFactor();
                }
                final Font headerFont = UIManager.getFont("TableHeader.font");
                if (headerFont != null) {
                    this.getTableHeader().setFont(new Font(headerFont.getAttributes()).deriveFont(headerFont.getSize2D() * scaleFactor));
                }
                final Font contentFont = UIManager.getFont("Table.font");
                if (contentFont != null) {
                    this.setFont(new Font(contentFont.getAttributes()).deriveFont(contentFont.getSize2D() * scaleFactor));
                    this.setRowHeight(this.getRowMargin() + (int) Math.ceil(this.getFont().getSize2D() * scaleFactor));
                }
                PatternAnalysisPanel.this.adjustColumns(this);
            }

            @Override
            public void createDefaultColumnsFromModel() {
                super.createDefaultColumnsFromModel();
                final JTable self = this;
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        PatternAnalysisPanel.this.adjustColumns(self);
                    }
                });
            }
        };
        tableView.setBorder(null);
        tableView.setAutoCreateColumnsFromModel(true);
        tableView.setAutoCreateRowSorter(sortable);
        tableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableView.setRowSelectionAllowed(true);
        tableView.setColumnSelectionAllowed(false);
        tableView.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        final AlternateRowRenderer cellRenderer = new AlternateRowRenderer();
        tableView.setDefaultRenderer(String.class, cellRenderer);
        tableView.setDefaultRenderer(Long.class, cellRenderer);
        tableView.setFillsViewportHeight(true);
        final JScrollPane scrollableTable = new JScrollPane(tableView);
        scrollableTable.setBorder(null);
        return scrollableTable;
    }

    /**
     * Adjust the widths of all columns of the given table to fit the respective column's header and contents.
     *
     * @param table
     *            the table to adjust the columns for
     */
    void adjustColumns(final JTable table) {
        final int columnCount = table.getColumnCount();
        final int rowCount = table.getRowCount();
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            final TableColumn column = table.getColumnModel().getColumn(columnIndex);
            TableCellRenderer headerRenderer = column.getHeaderRenderer();
            if (headerRenderer == null) {
                headerRenderer = table.getTableHeader().getDefaultRenderer();
            }
            final Object headerValue = column.getHeaderValue();
            final Component headerCell = headerRenderer.getTableCellRendererComponent(table, headerValue, false, false, -1, columnIndex);
            int columnWidth = headerCell.getPreferredSize().width;
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                final TableCellRenderer cellRenderer = table.getCellRenderer(rowIndex, columnIndex);
                columnWidth = Math.max(columnWidth, table.prepareRenderer(cellRenderer, rowIndex, columnIndex).getPreferredSize().width);
            }
            final int preferredWidth = Math.max(PatternAnalysisPanel.MIN_COLUMN_WIDTH, columnWidth) + 4 + 2 * table.getIntercellSpacing().width;
            column.setMaxWidth(preferredWidth + PatternAnalysisPanel.MIN_COLUMN_WIDTH / 2);
            column.setPreferredWidth(preferredWidth);
        }
    }

    /** Cell renderer to apply alternating row background color in order to improve the table's readability. */
    private static final class AlternateRowRenderer extends DefaultTableCellRenderer {

        /** Main constructor. */
        AlternateRowRenderer() {
            // nothing to setup
        }

        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus,
                final int row, final int column) {
            final Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                final Color rowBackGround;
                if (row % 2 == 0) {
                    rowBackGround = Color.WHITE;
                } else {
                    rowBackGround = PatternAnalysisPanel.ALTERNATE_ROW_COLOR;
                }
                component.setBackground(rowBackGround);
            }
            if (value instanceof Number) {
                ((JLabel) component).setHorizontalAlignment(SwingConstants.TRAILING);
            } else {
                ((JLabel) component).setHorizontalAlignment(SwingConstants.LEADING);
            }
            return component;
        }
    }
}
