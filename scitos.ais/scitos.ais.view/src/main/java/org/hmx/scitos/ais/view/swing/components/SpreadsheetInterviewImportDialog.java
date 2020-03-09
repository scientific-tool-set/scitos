/*
   Copyright (C) 2020 HermeneutiX.org

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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.hmx.scitos.ais.core.AisModelHandler;
import org.hmx.scitos.ais.core.InterviewToCreate;
import org.hmx.scitos.ais.core.i18n.AisMessage;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.view.swing.ScitosApp;
import org.hmx.scitos.view.swing.util.ViewUtil;
import org.jopendocument.dom.spreadsheet.Cell;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

/**
 * Dialog for bulk importing new interviews from an ODS spreadsheet.
 */
public final class SpreadsheetInterviewImportDialog extends JDialog {

    /** Model handler for the targeted project. */
    private final AisModelHandler modelHandler;

    /** Drop-down to select particular sheet from given spreadsheet to import from. */
    private final JComboBox<SheetComboBoxItem> sheetSelection;
    /** Drop-down to select the column containing the participant ids to be associated with the respective imported interviews. */
    private final JComboBox<ColumnComboBoxItem> participantIdColumnSelection = new JComboBox<>();
    /** Drop-down to select the column containing the actual interview text to import. */
    private final JComboBox<ColumnComboBoxItem> textColumnSelection = new JComboBox<>();
    /** Table listing found entries in the specified sheet and its selected columns, enabling the selection of the interviews to import. */
    private final JTable previewTable = new JTable(new PreviewTableModel());

    /**
     * Main constructor.
     *
     * @param modelHandler the model handler for the targeted project
     * @param spreadsheet the spreadsheet from which to import interviews
     */
    public SpreadsheetInterviewImportDialog(final AisModelHandler modelHandler, final SpreadSheet spreadsheet) {
        super(ScitosApp.getClient().getFrame(), AisMessage.PROJECT_IMPORT_INTERVIEWS.get(), true);
        this.modelHandler = modelHandler;
        final JPanel contentPane = new JPanel(new BorderLayout());
        this.sheetSelection = new JComboBox<>(IntStream.range(0, spreadsheet.getSheetCount())
                .mapToObj(spreadsheet::getSheet)
                .filter(sheet -> sheet.getColumnCount() > 1 && sheet.getRowCount() > 1)
                .filter(sheet -> !sheet.getImmutableCellAt(0, 0).isEmpty() && !sheet.getImmutableCellAt(1, 0).isEmpty())
                .map(SheetComboBoxItem::new)
                .toArray(SheetComboBoxItem[]::new));
        contentPane.add(this.createSelectionForm(), BorderLayout.NORTH);
        contentPane.add(this.createInterviewPreviewTable());
        this.handleChangedSheetSelection();

        final JPanel buttonArea = new JPanel(new GridBagLayout());
        buttonArea.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));

        final JButton cancelButton = new JButton(Message.CANCEL.get());
        cancelButton.addActionListener(event -> this.dispose());
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.anchor = GridBagConstraints.BASELINE_LEADING;
        constraints.fill = GridBagConstraints.BOTH;
        buttonArea.add(cancelButton, constraints);

        final JPanel spacing = new JPanel();
        spacing.setOpaque(false);
        constraints.gridx = 1;
        constraints.weightx = 1;
        buttonArea.add(spacing, constraints);

        final JButton okButton = new JButton(Message.OK.get());
        okButton.addActionListener(event -> this.okButtonPressed());
        constraints.gridx = 2;
        constraints.weightx = 0;
        constraints.anchor = GridBagConstraints.BASELINE_TRAILING;
        buttonArea.add(okButton, constraints);

        contentPane.add(buttonArea, BorderLayout.SOUTH);

        this.setContentPane(contentPane);
        this.setMinimumSize(new Dimension(600, 400));
        this.pack();
        ViewUtil.centerOnParent(this);
        SwingUtilities.invokeLater(cancelButton::requestFocusInWindow);
    }

    /**
     * Initialize the form for selecting a target sheet and columns from the provided spreadsheet.
     *
     * @return panel containing the selection form
     */
    private JPanel createSelectionForm() {
        final JPanel selectionFormPanel = new JPanel(new GridLayout(3, 2));
        selectionFormPanel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        selectionFormPanel.add(new JLabel(AisMessage.PROJECT_IMPORT_INTERVIEWS_SHEET.get()));
        this.sheetSelection.setSelectedIndex(0);
        this.sheetSelection.addActionListener(event -> this.handleChangedSheetSelection());
        selectionFormPanel.add(this.sheetSelection);
        selectionFormPanel.add(new JLabel(AisMessage.PROJECT_IMPORT_INTERVIEWS_PARTICIPANT_COLUMN.get()));
        this.participantIdColumnSelection.addActionListener(event -> this.handleChangedColumnSelection());
        selectionFormPanel.add(this.participantIdColumnSelection);
        selectionFormPanel.add(new JLabel(AisMessage.PROJECT_IMPORT_INTERVIEWS_TEXT_COLUMN.get()));
        this.textColumnSelection.addActionListener(event -> this.handleChangedColumnSelection());
        selectionFormPanel.add(this.textColumnSelection);
        return selectionFormPanel;
    }

    /**
     * Initialize the table listing found entries in the specified sheet and its selected columns, enabling the selection of the interviews to import.
     *
     * @return panel containing the table
     */
    private JPanel createInterviewPreviewTable() {
        final JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        this.previewTable.setRowSelectionAllowed(false);
        this.previewTable.setColumnSelectionAllowed(false);
        final TableColumn checkboxColumn = this.previewTable.getColumnModel().getColumn(0);
        checkboxColumn.setMinWidth(30);
        checkboxColumn.setPreferredWidth(60);
        checkboxColumn.setMaxWidth(60);
        final TableColumn participantIdColumn = this.previewTable.getColumnModel().getColumn(1);
        participantIdColumn.setMinWidth(100);
        participantIdColumn.setPreferredWidth(150);
        participantIdColumn.setMaxWidth(300);
        final TableColumn textColumn = this.previewTable.getColumnModel().getColumn(2);
        textColumn.setMinWidth(150);
        textColumn.setPreferredWidth(300);
        tableWrapper.add(new JScrollPane(this.previewTable));
        return tableWrapper;
    }

    /** Update the column drop-downs and table's contents as response to a new sheet having been selected. */
    private void handleChangedSheetSelection() {
        final Sheet selectedSheet = ((SheetComboBoxItem) this.sheetSelection.getSelectedItem()).getSheet();
        this.participantIdColumnSelection.removeAllItems();
        this.textColumnSelection.removeAllItems();
        IntStream.range(0, selectedSheet.getColumnCount())
                .mapToObj(columnIndex -> new ColumnComboBoxItem(columnIndex, selectedSheet.getImmutableCellAt(columnIndex, 0)))
                .filter(item -> !item.getColumnHeader().isEmpty())
                .forEach(item -> {
                    this.participantIdColumnSelection.addItem(item);
                    this.textColumnSelection.addItem(item);
                });
        this.participantIdColumnSelection.setSelectedIndex(0);
        this.textColumnSelection.setSelectedIndex(1);
        ((PreviewTableModel) this.previewTable.getModel()).reset(selectedSheet, 0, 1);
    }

    /** Update the table's contents as response to a new participant id or text column having been selected. */
    private void handleChangedColumnSelection() {
        if (this.sheetSelection.getSelectedIndex() == -1
                || this.participantIdColumnSelection.getSelectedIndex() == -1
                || this.textColumnSelection.getSelectedIndex() == -1) {
            ((PreviewTableModel) this.previewTable.getModel()).reset(null, -1, -1);
        } else {
            final Sheet selectedSheet = ((SheetComboBoxItem) this.sheetSelection.getSelectedItem()).getSheet();
            final int participantIdColumnIndex = ((ColumnComboBoxItem) this.participantIdColumnSelection.getSelectedItem()).getColumnIndex();
            final int textColumnIndex = ((ColumnComboBoxItem) this.textColumnSelection.getSelectedItem()).getColumnIndex();
            ((PreviewTableModel) this.previewTable.getModel()).reset(selectedSheet, participantIdColumnIndex, textColumnIndex);
        }
    }

    /** Create the interviews selected in the preview table after the OK button has been clicked on. */
    private void okButtonPressed() {
        if (this.sheetSelection.getSelectedIndex() == -1
                || this.participantIdColumnSelection.getSelectedIndex() == -1
                || this.textColumnSelection.getSelectedIndex() == -1
                || this.previewTable.getRowCount() == 0) {
            return;
        }
        final List<InterviewToCreate> interviewsToCreate = ((PreviewTableModel) this.previewTable.getModel()).getSelectedInterviews();
        if (!interviewsToCreate.isEmpty()) {
            this.modelHandler.createInterviews(interviewsToCreate);
            this.dispose();
        }
    }

    /** Element in drop-down representing a single sheet in the spreadsheet to import from. */
    private static final class SheetComboBoxItem {

        /** Represented sheet in spreadsheet to import from. */
        private final Sheet sheet;

        /**
         * Constructor.
         *
         * @param sheet represented sheet in spreadsheet to import from
         */
        SheetComboBoxItem(final Sheet sheet) {
            this.sheet = sheet;
        }

        /**
         * Getter for the represented sheet in spreadsheet to import from.
         *
         * @return represented sheet
         */
        Sheet getSheet() {
            return this.sheet;
        }

        @Override
        public String toString() {
            return this.sheet.getName();
        }
    }

    /** Element in drop-down representing a single column on the targeted sheet. */
    private static final class ColumnComboBoxItem {

        /** Index of this column in the targeted sheet. */
        private final int columnIndex;
        /** Header cell associated with the represented column. */
        private final Cell<SpreadSheet> columnHeaderCell;
        /** Text to show in drop-down for the represented column. */
        private final String columnRepresentation;

        /**
         * Constructor.
         *
         * @param columnIndex index of this column in the targeted sheet
         * @param columnHeaderCell header cell associated with the represented column
         */
        ColumnComboBoxItem(final int columnIndex, final Cell<SpreadSheet> columnHeaderCell) {
            this.columnIndex = columnIndex;
            this.columnHeaderCell = columnHeaderCell;
            String headerText = columnHeaderCell.getTextValue();
            if (headerText == null) {
                this.columnRepresentation = null;
            } else {
                headerText = headerText.trim();
                if (headerText.length() > 32) {
                    headerText = headerText.substring(0, 31).trim() + "…";
                }
                this.columnRepresentation = (columnIndex + 1) + ": " + headerText;
            }
        }

        /**
         * Getter for the index of this column in the targeted sheet.
         *
         * @return column index in sheet
         */
        int getColumnIndex() {
            return this.columnIndex;
        }

        /**
         * Getter for the header cell associated with the represented column.
         *
         * @return column header cell
         */
        Cell<SpreadSheet> getColumnHeader() {
            return this.columnHeaderCell;
        }

        @Override
        public String toString() {
            return this.columnRepresentation;
        }
    }

    /** Table model for the display of found interviews for currently selected combination of sheet and target columns to import from. */
    private static final class PreviewTableModel implements TableModel {

        /** Column header texts. */
        private final String[] columnNames = {
            AisMessage.PROJECT_IMPORT_INTERVIEWS_PREVIEW_SELECT.get(),
            AisMessage.PROJECT_IMPORT_INTERVIEWS_PREVIEW_PARTICIPANT.get(),
            AisMessage.PROJECT_IMPORT_INTERVIEWS_PREVIEW_TEXT.get()
        };
        /** Table rows, each representing a potential interview to be imported. */
        private final List<PreviewTableRowModel> data = new ArrayList<>();
        /** Table model listeners to inform about changes to the data being displayed in the associated table. */
        private final List<TableModelListener> listeners = new ArrayList<>();

        /**
         * Getter for those table row that have been selected to be included in the import.
         *
         * @return table rows to create interviews from
         */
        List<InterviewToCreate> getSelectedInterviews() {
            return this.data.stream()
                    .filter(PreviewTableRowModel::isSelected)
                    .collect(Collectors.toList());
        }

        /**
         * Reset the associated table's contents according to the given sheet and column selections.
         *
         * @param targetSheet sheet to check for possible interviews to import
         * @param participantIdColumnIndex index of the column to extract the respective participant ids from
         * @param interviewTextColumnIndex index of the column to extract the respective interview texts from
         */
        void reset(final Sheet targetSheet, final int participantIdColumnIndex, final int interviewTextColumnIndex) {
            this.data.clear();
            if (targetSheet != null) {
                this.data.addAll(IntStream.range(1, targetSheet.getRowCount())
                        .mapToObj(rowIndex -> this.createRowModel(targetSheet, participantIdColumnIndex, interviewTextColumnIndex, rowIndex))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
            }
            final TableModelEvent fullUpdateEvent = new TableModelEvent(this);
            this.listeners.forEach(listener -> listener.tableChanged(fullUpdateEvent));
        }

        /**
         * Create the representation of a single row in the associated preview table.
         *
         * @param sheet sheet to read from
         * @param participantIdColumnIndex index of the column to extract the participant id from
         * @param textColumnIndex index of the column to extract the interview text from
         * @param rowIndex index of the targeted row in the sheet to read from
         * @return successfully parsed interview that may be imported; or null if the table row does not contain values in both columns in that row
         */
        PreviewTableRowModel createRowModel(final Sheet sheet, final int participantIdColumnIndex, final int textColumnIndex, final int rowIndex) {
            final Cell<SpreadSheet> participantIdCell = sheet.getImmutableCellAt(participantIdColumnIndex, rowIndex);
            if (participantIdCell.isEmpty()) {
                return null;
            }
            final Cell<SpreadSheet> interviewTextCell = sheet.getImmutableCellAt(textColumnIndex, rowIndex);
            if (interviewTextCell.isEmpty()) {
                return null;
            }
            return new PreviewTableRowModel(true, participantIdCell.getTextValue(), interviewTextCell.getTextValue());
        }

        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            switch (columnIndex) {
            case 0:
                return Boolean.class;
            default:
                return String.class;
            }
        }

        @Override
        public int getRowCount() {
            return this.data.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(final int columnIndex) {
            return this.columnNames[columnIndex];
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            switch (columnIndex) {
            case 0:
                return this.data.get(rowIndex).isSelected();
            case 1:
                return this.data.get(rowIndex).getParticipantId();
            case 2:
                return this.data.get(rowIndex).getInterviewTextPreview();
            default:
                throw new IllegalArgumentException();
            }
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public void setValueAt(final Object valueToSet, final int rowIndex, final int columnIndex) {
            switch (columnIndex) {
            case 0:
                this.data.get(rowIndex).setSelected(Boolean.TRUE.equals(valueToSet));
                final TableModelEvent event = new TableModelEvent(this, rowIndex, rowIndex, columnIndex, TableModelEvent.UPDATE);
                this.listeners.forEach(listener -> listener.tableChanged(event));
                break;
            default:
                throw new IllegalArgumentException();
            }
        }

        @Override
        public void addTableModelListener(final TableModelListener listener) {
            this.listeners.add(listener);
        }

        @Override
        public void removeTableModelListener(final TableModelListener listener) {
            this.listeners.remove(listener);
        }
    }

    /** Representation of a single interview that may be imported. */
    private static final class PreviewTableRowModel implements InterviewToCreate {

        /** Maximum number of characters to show in the preview table before applying ellipsis (regardless of the actual dialog table width). */
        private static final int TEXT_CUTOFF_LIMIT = 200;

        /** Flag indicating whether the represented interview should be included in the import. */
        private boolean selected;
        /** Participant id to associate the imported interview with. */
        private final String participantId;
        /** Full interview text (only used for actual import). */
        private final String interviewText;
        /** Shortened interview text (only for display purposes within the preview table). **/
        private final String interviewTextPreview;

        /**
         * Constructor.
         *
         * @param selected flag indicating whether the represented interview should be included in the import
         * @param participantId participant id to associate the imported interview with
         * @param interviewText full interview text
         */
        PreviewTableRowModel(final boolean selected, final String participantId, final String interviewText) {
            this.selected = selected;
            this.participantId = participantId;
            this.interviewText = interviewText;
            if (interviewText.length() < TEXT_CUTOFF_LIMIT) {
                this.interviewTextPreview = interviewText;
            } else {
                this.interviewTextPreview = interviewText.substring(0, TEXT_CUTOFF_LIMIT) + "…";
            }
        }

        /**
         * Getter for the flag indicating whether the represented interview should be included in the import.
         *
         * @return whether to include this row in the import
         */
        boolean isSelected() {
            return this.selected;
        }

        /**
         * Setter for the flag indicating whether the represented interview should be included in the import.
         *
         * @param selected whether to include this row in the import
         */
        void setSelected(final boolean selected) {
            this.selected = selected;
        }

        @Override
        public String getParticipantId() {
            return this.participantId;
        }

        @Override
        public String getInterviewText() {
            return this.interviewText;
        }

        /**
         * Getter for the shortened interview text (only for display purposes within the preview table).
         *
         * @return interview text (preview) to show in table
         */
        String getInterviewTextPreview() {
            return this.interviewTextPreview;
        }
    }
}
