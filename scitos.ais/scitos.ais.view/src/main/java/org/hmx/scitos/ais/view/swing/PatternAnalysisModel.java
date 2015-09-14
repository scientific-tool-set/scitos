package org.hmx.scitos.ais.view.swing;

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

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.hmx.scitos.ais.core.i18n.AisMessage;
import org.hmx.scitos.ais.domain.model.DetailCategory;
import org.hmx.scitos.ais.domain.model.Interview;
import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.view.swing.MessageHandler;
import org.jopendocument.dom.OOXML;
import org.jopendocument.dom.XMLVersion;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

/**
 * Summarizing result model for a single {@link AisViewProject view project} in the AIS module.
 */
public final class PatternAnalysisModel {

    /** The associated view project, containing the interviews the displayed results are extracted from. */
    final AisViewProject project;

    /** The model of the primary table, containing the number of occurrences of the individual detail categories. */
    private final SummaryTableModel summaryTableModel;
    /** The model of the secondary table, containing the actual sequence of assigned detail categories for each interview. */
    private final SequenceTableModel sequenceTableModel;
    /** The model of the tertiary table, containing the number of pattern occurrences in the assigned detail categories. */
    private final PatternTableModel patternTableModel;

    /**
     * Main constructor.
     * 
     * @param project
     *            providing instance of the scored detail categories in the interviews
     */
    public PatternAnalysisModel(final AisViewProject project) {
        this.project = project;
        this.summaryTableModel = new SummaryTableModel();
        this.sequenceTableModel = new SequenceTableModel();
        this.patternTableModel = new PatternTableModel();
    }

    /**
     * Getter for the table model, that contains the number of detail category occurrences in the scored interviews.
     * 
     * @return the summary table model
     */
    public SummaryTableModel getSummaryTableModel() {
        return this.summaryTableModel;
    }

    /**
     * Getter for the table model, that contains the actual detail categories in their order for each interview.
     * 
     * @return the sequence table model
     */
    public SequenceTableModel getSequenceTableModel() {
        return this.sequenceTableModel;
    }

    /**
     * Getter for the table model, that contains the number of occurrences for recognized detail category patterns from the scored interviews.
     * 
     * @return the pattern table model
     */
    public PatternTableModel getPatternTableModel() {
        return this.patternTableModel;
    }

    /** Enforce recollection/recalculation of the table model values. */
    public void reload() {
        // recollect the underlying values
        this.summaryTableModel.refreshModel();
        this.sequenceTableModel.refreshModel();
        this.patternTableModel.refreshModel();
    }

    /**
     * Export the contained tables into an Open Document Spreadsheet.
     * 
     * @param target
     *            the selected file destination to save to
     */
    public void exportToSpreadSheet(final File target) {
        final SpreadSheet document = SpreadSheet.createEmpty(this.summaryTableModel, OOXML.getLast(XMLVersion.OD).getFormatVersion());
        document.getFirstSheet().setName(AisMessage.ANALYSIS_SUMMARY.get());
        document.addSheet(AisMessage.ANALYSIS_SEQUENCE.get()).merge(this.sequenceTableModel, 0, 0, true);
        document.addSheet(AisMessage.ANALYSIS_PATTERN.get()).merge(this.patternTableModel, 0, 0, true);
        try {
            document.saveAs(target);
        } catch (final IOException ioex) {
            MessageHandler.showException(new HmxException(AisMessage.ERROR_EXPORT_FAILED, ioex));
        }
    }

    /** The model of the primary table, displaying the number of occurrences of the individual detail categories. */
    public final class SummaryTableModel extends AbstractTableModel {

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
            this.rows = PatternAnalysisModel.this.project.getModelObject().getInterviews();
            this.columns = PatternAnalysisModel.this.project.getModelObject().provide();
            this.tokenCounts = PatternAnalysisModel.this.project.getModelHandler().countTokensWithAssignedDetail(this.rows);
            this.values = PatternAnalysisModel.this.project.getModelHandler().countDetailOccurrences(this.rows);

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
                value = PatternAnalysisModel.this.project.getLabel(interview);
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
                columnName = AisMessage.ANALYSIS_TABLE_HEADER_INTERVIEW.get();
            } else if (columnIndex == 1) {
                columnName = AisMessage.ANALYSIS_TABLE_HEADER_TOKENCOUNT.get();
            } else {
                columnName = this.columns.get(columnIndex - 2).getCode();
            }
            return columnName;
        }

        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            if (columnIndex == 0) {
                return String.class;
            }
            return Long.class;
        }
    }

    /** The model of the secondary table, displaying the actual sequence of assigned detail categories for each interview. */
    public final class SequenceTableModel extends AbstractTableModel {

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
            this.columns = PatternAnalysisModel.this.project.getModelObject().getInterviews();
            this.values = new HashMap<Interview, List<DetailCategory>>();
            this.rowCount = 0;
            for (final Interview singleInterview : this.columns) {
                final List<DetailCategory> sequence = PatternAnalysisModel.this.project.getModelHandler().extractDetailSequence(singleInterview);
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
                cellValue = AisMessage.ANALYSIS_NOT_SCORED.get();
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
            return PatternAnalysisModel.this.project.getLabel(interview);
        }

        @Override
        public Class<String> getColumnClass(final int columnIndex) {
            return String.class;
        }
    }

    /** The model of the tertiary table, displaying the number of pattern occurrences in the assigned detail categories. */
    public final class PatternTableModel extends AbstractTableModel {

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
            this.sorter = new PatternSorter(PatternAnalysisModel.this.project.getModelObject().provide());
            this.refreshModel();
        }

        /** Recalculate/recollect the displayed table contents. */
        void refreshModel() {
            this.columns = PatternAnalysisModel.this.project.getModelObject().getInterviews();
            this.values = PatternAnalysisModel.this.project.getModelHandler().extractDetailPattern(this.columns, 2, 3);
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
                cellValue = AisMessage.ANALYSIS_NOT_SCORED.get();
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
                header = AisMessage.ANALYSIS_PATTERN.get();
            } else {
                header = PatternAnalysisModel.this.project.getLabel(this.columns.get(columnIndex - 1));
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
}
