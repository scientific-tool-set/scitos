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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.hmx.scitos.ais.domain.model.DetailCategory;
import org.hmx.scitos.ais.domain.model.Interview;
import org.hmx.scitos.ais.view.swing.AisViewProject;
import org.hmx.scitos.core.ExportOption.TargetFileType;
import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.view.ScitosIcon;
import org.hmx.scitos.view.swing.MessageHandler;
import org.hmx.scitos.view.swing.ScitosApp;
import org.hmx.scitos.view.swing.ScitosClient;
import org.jopendocument.dom.OOXML;
import org.jopendocument.dom.XMLVersion;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

/** Component displaying the summed up scoring results of a whole project. */
public final class PatternAnalysisPanel extends JPanel {

    /** The alternate row color, for easier readability in the result tables. */
    static final Color ALTERNATE_ROW_COLOR = new Color(239, 239, 239);

    /** The associated view project, containing the interviews the displayed results are extracted from. */
    final AisViewProject project;

    /** The model of the primary table, displaying the number of occurrences of the individual detail categories. */
    final SummaryTableModel summaryTableModel;
    /** The model of the secondary table, displaying the actual sequence of assigned detail categories for each interview. */
    final SequenceTableModel sequenceTableModel;
    /** The model of the tertiary table, displaying the number of pattern occurrences in the assigned detail categories. */
    final PatternTableModel patternTableModel;

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
        this.project = project;
        this.setBorder(null);
        final JTabbedPane tabStack = new JTabbedPane(SwingConstants.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        this.summaryTableModel = new SummaryTableModel();
        tabStack.add(Message.AIS_ANALYSIS_SUMMARY.get(), this.createTableFromModel(this.summaryTableModel));
        this.sequenceTableModel = new SequenceTableModel();
        tabStack.add(Message.AIS_ANALYSIS_SEQUENCE.get(), this.createTableFromModel(this.sequenceTableModel));
        this.patternTableModel = new PatternTableModel();
        tabStack.add(Message.AIS_ANALYSIS_PATTERN.get(), this.createTableFromModel(this.patternTableModel));
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

        final JButton exportButton = new JButton(Message.AIS_ANALYSIS_EXPORT.get(), ScitosIcon.FILE_ODS.create());
        exportButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                final File target = client.getSaveDestination(TargetFileType.ODS.getExtension(), Message.AIS_ANALYSIS_EXPORT.get());
                if (target == null) {
                    // user aborted selection of save target
                    return;
                }
                final SpreadSheet document =
                        SpreadSheet.createEmpty(PatternAnalysisPanel.this.summaryTableModel, OOXML.getLast(XMLVersion.OD).getFormatVersion());
                document.getFirstSheet().setName(Message.AIS_ANALYSIS_SUMMARY.get());
                document.addSheet(Message.AIS_ANALYSIS_SEQUENCE.get()).merge(PatternAnalysisPanel.this.sequenceTableModel, 0, 0, true);
                document.addSheet(Message.AIS_ANALYSIS_PATTERN.get()).merge(PatternAnalysisPanel.this.patternTableModel, 0, 0, true);
                try {
                    document.saveAs(target);
                } catch (final IOException ioex) {
                    MessageHandler.showException(new HmxException(Message.ERROR_EXPORT_FAILED, ioex));
                }
            }
        });
        this.add(exportButton, BorderLayout.SOUTH);
    }

    /** Force the tables to recalculate the displayed results. */
    void refresh() {
        // recollect the underlying values
        this.summaryTableModel.refreshModel();
        this.sequenceTableModel.refreshModel();
        this.patternTableModel.refreshModel();
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

    /** The model of the primary table, displaying the number of occurrences of the individual detail categories. */
    private final class SummaryTableModel extends AbstractTableModel {

        /** The interviews to display the values for – one interview per row. */
        private List<Interview> rows;
        /** The detail categories to display the number of occurrences for – one detail category per column. */
        private List<DetailCategory> columns;
        /** The overall number of tokens with assigned detail categories, for each interview. */
        private Map<Interview, AtomicLong> tokenCounts;
        /** The actual results: number of occurrences for each contained detail category, per interview. */
        private Map<Interview, Map<DetailCategory, AtomicLong>> values;

        /** Main constructor. */
        SummaryTableModel() {
            this.refreshModel();
        }

        /** Recalculate/recollect the displayed table contents. */
        void refreshModel() {
            this.rows = PatternAnalysisPanel.this.project.getModelObject().getInterviews();
            this.columns = PatternAnalysisPanel.this.project.getModelObject().provide();
            this.tokenCounts = PatternAnalysisPanel.this.project.getModelHandler().countTokensWithAssignedDetail(this.rows);
            this.values = PatternAnalysisPanel.this.project.getModelHandler().countDetailOccurrences(this.rows);

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    SummaryTableModel.this.fireTableStructureChanged();
                }
            });
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            final Interview interview = this.rows.get(rowIndex);
            final Object value;
            if (columnIndex == 0) {
                value = PatternAnalysisPanel.this.project.getLabel(interview);
            } else if (columnIndex == 1) {
                value = this.tokenCounts.get(interview).get();
            } else {
                value = this.values.get(interview).get(this.columns.get(columnIndex - 2)).get();
            }
            return value;
        }

        @Override
        public int getRowCount() {
            return this.rows.size();
        }

        @Override
        public int getColumnCount() {
            return 2 + this.columns.size();
        }

        @Override
        public String getColumnName(final int columnIndex) {
            final String columnName;
            if (columnIndex == 0) {
                columnName = Message.AIS_ANALYSIS_TABLE_HEADER_INTERVIEW.get();
            } else if (columnIndex == 1) {
                columnName = Message.AIS_ANALYSIS_TABLE_HEADER_TOKENCOUNT.get();
            } else {
                columnName = this.columns.get(columnIndex - 2).getCode();
            }
            return columnName;
        }

        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            if (columnIndex < 2) {
                return String.class;
            }
            return Long.class;
        }
    }

    /** The model of the secondary table, displaying the actual sequence of assigned detail categories for each interview. */
    private final class SequenceTableModel extends AbstractTableModel {

        /** Maximum sequence length over all interviews. */
        private int rowCount;
        /** The interviews to display the sequences for – one interview per column. */
        private List<Interview> columns;
        /** The actual sequences of assigned detail categories - one sequence per interview/column. */
        private Map<Interview, List<DetailCategory>> values;

        /** Main constructor. */
        SequenceTableModel() {
            this.refreshModel();
        }

        /** Recollect the displayed table contents. */
        void refreshModel() {
            this.columns = PatternAnalysisPanel.this.project.getModelObject().getInterviews();
            this.values = new HashMap<Interview, List<DetailCategory>>();
            this.rowCount = 0;
            for (final Interview singleInterview : this.columns) {
                final List<DetailCategory> sequence = PatternAnalysisPanel.this.project.getModelHandler().extractDetailSequence(singleInterview);
                this.values.put(singleInterview, sequence);
                this.rowCount = Math.max(this.rowCount, sequence.size());
            }
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    SequenceTableModel.this.fireTableStructureChanged();
                }
            });
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            final String cellValue;
            if (!this.columns.isEmpty() && this.rowCount > 0) {
                final Interview interview = this.columns.get(columnIndex);
                final List<DetailCategory> sequence = this.values.get(interview);
                if (rowIndex < sequence.size()) {
                    cellValue = sequence.get(rowIndex).getCode();
                } else {
                    cellValue = "";
                }
            } else if (columnIndex == 0) {
                cellValue = Message.AIS_ANALYSIS_NOT_SCORED.get();
            } else {
                cellValue = "";
            }
            return cellValue;
        }

        @Override
        public int getRowCount() {
            return Math.max(1, this.rowCount);
        }

        @Override
        public int getColumnCount() {
            if (this.rowCount == 0) {
                return 1;
            }
            return this.columns.size();
        }

        @Override
        public String getColumnName(final int columnIndex) {
            if (this.rowCount == 0) {
                return "";
            }
            final Interview interview = this.columns.get(columnIndex);
            return PatternAnalysisPanel.this.project.getLabel(interview);
        }

        @Override
        public Class<String> getColumnClass(final int columnIndex) {
            return String.class;
        }
    }

    /** The model of the tertiary table, displaying the number of pattern occurrences in the assigned detail categories. */
    private final class PatternTableModel extends AbstractTableModel {

        /** Custom comparator to sort the patterns by the detail category order in the model. */
        private final PatternSorter sorter;
        /** The pattern to display the number of occurrences for – one pattern per row. */
        private List<List<DetailCategory>> rows;
        /** The interviews to display the values for – one interview per column. */
        private List<Interview> columns;
        /** The actual results: number of occurrences for each detail category pattern, per interview. */
        private Map<Interview, Map<List<DetailCategory>, AtomicLong>> values;

        /** Main constructor. */
        PatternTableModel() {
            this.sorter = new PatternSorter(PatternAnalysisPanel.this.project.getModelObject().provide());
            this.refreshModel();
        }

        /** Recalculate/recollect the displayed table contents. */
        void refreshModel() {
            this.columns = PatternAnalysisPanel.this.project.getModelObject().getInterviews();
            this.values = PatternAnalysisPanel.this.project.getModelHandler().extractDetailPattern(this.columns, 2, 3);
            final Set<List<DetailCategory>> patterns = new HashSet<List<DetailCategory>>();
            for (final Map<List<DetailCategory>, AtomicLong> singlePatternList : this.values.values()) {
                patterns.addAll(singlePatternList.keySet());
            }
            this.rows = new ArrayList<List<DetailCategory>>(patterns);
            Collections.sort(this.rows, this.sorter);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    PatternTableModel.this.fireTableStructureChanged();
                }
            });
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            Object cellValue;
            if (!this.rows.isEmpty()) {
                final List<DetailCategory> pattern = this.rows.get(rowIndex);
                if (columnIndex == 0) {
                    final StringBuilder patternString = new StringBuilder();
                    for (final DetailCategory singleDetail : pattern) {
                        patternString.append(singleDetail.getCode());
                        patternString.append(' ');
                    }
                    cellValue = patternString.deleteCharAt(patternString.length() - 1).toString();
                } else {
                    final Interview interview = this.columns.get(columnIndex - 1);
                    cellValue = this.values.get(interview).get(pattern);
                    if (cellValue == null) {
                        cellValue = Long.valueOf(0);
                    } else {
                        cellValue = ((AtomicLong) cellValue).get();
                    }
                }
            } else if (columnIndex == 0) {
                cellValue = Message.AIS_ANALYSIS_NOT_SCORED.get();
            } else {
                cellValue = "";
            }
            return cellValue;
        }

        @Override
        public int getRowCount() {
            return Math.max(1, this.rows.size());
        }

        @Override
        public int getColumnCount() {
            return 1 + this.columns.size();
        }

        @Override
        public String getColumnName(final int columnIndex) {
            final String header;
            if (this.rows.isEmpty()) {
                header = "";
            } else if (columnIndex == 0) {
                header = Message.AIS_ANALYSIS_PATTERN.get();
            } else {
                header = PatternAnalysisPanel.this.project.getLabel(this.columns.get(columnIndex - 1));
            }
            return header;
        }

        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            if (columnIndex == 0) {
                return String.class;
            }
            return Long.class;
        }
    }

    /** Custom comparator to sort the patterns by the detail category order in the model. */
    private static final class PatternSorter implements Comparator<List<DetailCategory>> {

        /** Mapped sort rank for each detail category, to speedup the actual sorting. */
        private final Map<DetailCategory, Integer> cachedDetailOrder = new HashMap<DetailCategory, Integer>();

        /**
         * Main constructor.
         *
         * @param categoryOrder
         *            detail categories in the order they should be sorted
         */
        PatternSorter(final List<DetailCategory> categoryOrder) {
            for (int index = 0; index < categoryOrder.size(); index++) {
                this.cachedDetailOrder.put(categoryOrder.get(index), Integer.valueOf(index));
            }
        }

        @Override
        public int compare(final List<DetailCategory> pattern1, final List<DetailCategory> pattern2) {
            final int minPatternLength = Math.min(pattern1.size(), pattern2.size());
            int result = 0;
            for (int index = 0; index < minPatternLength; index++) {
                result = this.cachedDetailOrder.get(pattern1.get(index)).compareTo(this.cachedDetailOrder.get(pattern2.get(index)));
                if (result != 0) {
                    break;
                }
            }
            if (result == 0) {
                result = pattern1.size() - pattern2.size();
            }
            return result;
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
