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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;

import org.hmx.scitos.ais.core.i18n.AisMessage;
import org.hmx.scitos.ais.view.swing.AisViewProject;
import org.hmx.scitos.ais.view.swing.PatternAnalysisModel;
import org.hmx.scitos.core.ExportOption.TargetFileType;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.view.ScitosIcon;
import org.hmx.scitos.view.swing.ScitosClient;
import org.hmx.scitos.view.swing.components.ScaledTable;
import org.hmx.scitos.view.swing.util.ViewUtil;

/** Component displaying the summed up scoring results of a whole project. */
public final class PatternAnalysisPanel extends JPanel {

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
        final ScaledTable tableView = new ScaledTable(tableModel, true, sortable);
        tableView.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        tableView.setFillsViewportHeight(true);
        final JScrollPane scrollableTable = new JScrollPane(tableView);
        scrollableTable.setBorder(null);
        return scrollableTable;
    }
}
