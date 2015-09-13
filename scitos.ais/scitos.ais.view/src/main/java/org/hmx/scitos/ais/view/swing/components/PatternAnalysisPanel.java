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
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.hmx.scitos.ais.core.i18n.AisMessage;
import org.hmx.scitos.ais.view.swing.AisViewProject;
import org.hmx.scitos.ais.view.swing.PatternAnalysisModel;
import org.hmx.scitos.core.ExportOption.TargetFileType;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.view.ScitosIcon;
import org.hmx.scitos.view.swing.ScitosApp;
import org.hmx.scitos.view.swing.ScitosClient;

/** Component displaying the summed up scoring results of a whole project. */
public final class PatternAnalysisPanel extends JPanel {

    /** The alternate row color, for easier readability in the result tables. */
    static final Color ALTERNATE_ROW_COLOR = new Color(239, 239, 239);

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
        tabStack.add(AisMessage.ANALYSIS_SUMMARY.get(), this.createTableFromModel(this.model.getSummaryTableModel()));
        tabStack.add(AisMessage.ANALYSIS_SEQUENCE.get(), this.createTableFromModel(this.model.getSequenceTableModel()));
        tabStack.add(AisMessage.ANALYSIS_PATTERN.get(), this.createTableFromModel(this.model.getPatternTableModel()));
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
                final File target = client.getSaveDestination(TargetFileType.ODS.getExtension(), Message.MENUBAR_FILE_EXPORT.get());
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
     * @return scrollable table taking up the whole view
     */
    private JScrollPane createTableFromModel(final TableModel tableModel) {
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
                    this.setRowHeight(2 + Math.round(this.getFont().getSize2D() * scaleFactor));
                }
            }
        };
        tableView.setBorder(null);
        tableView.setAutoCreateColumnsFromModel(true);
        tableView.setAutoCreateRowSorter(true);
        tableView.setRowSelectionAllowed(true);
        tableView.setColumnSelectionAllowed(false);
        tableView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final AlternateRowRenderer cellRenderer = new AlternateRowRenderer();
        tableView.setDefaultRenderer(String.class, cellRenderer);
        tableView.setDefaultRenderer(Long.class, cellRenderer);
        tableView.setFillsViewportHeight(true);
        final JScrollPane scrollableTable = new JScrollPane(tableView);
        scrollableTable.setBorder(null);
        return scrollableTable;
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
