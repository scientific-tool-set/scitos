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

package org.hmx.scitos.hmx.view.swing.option;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import net.java.dev.designgridlayout.DesignGridLayout;

import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.domain.ISyntacticalFunctionProvider;
import org.hmx.scitos.hmx.domain.model.AbstractSyntacticalFunctionElement;
import org.hmx.scitos.hmx.view.swing.option.SynFunctionTreeModel.TreeTableRow;
import org.hmx.scitos.view.ScitosIcon;
import org.hmx.scitos.view.swing.MessageHandler;
import org.hmx.scitos.view.swing.util.Validation;
import org.hmx.scitos.view.swing.util.table.TreeTable;
import org.jdesktop.swingx.table.TableColumnExt;

/** UI component displaying the syntactical functions of a single language model and allowing those to be added/updated/removed. */
public final class SynFunctionConfigPanel extends JPanel implements ISyntacticalFunctionProvider {

    /**
     * The options to toggle between.
     */
    private enum ActionMode {
        /** Enabled tree table to select an entry in. */
        TABLE,
        /** Enabled function form to edit the selected syntactical function entry. */
        FUNCTION,
        /** Enabled function group form to edit the selected syntactical function group entry. */
        FUNCTION_GROUP
    }

    /** The wrapped tree jtable component. */
    final TreeTable treeTable;
    /** The underlying tree table model containing all functions and groups and enabling them to be added or removed. */
    final SynFunctionTreeModel functionModel = new SynFunctionTreeModel();
    /** Button to add a top level group to the bottom of the tree table. */
    private final JButton addGroupButton = new JButton(HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_ADD_TOPLEVEL_GROUP.get(), ScitosIcon.ADD.create());
    /** Button to enable the function/group form to edit the currently selected entry. */
    private final JButton editTableSelectionButton = new JButton(HmxMessage.PREFERENCES_LANGUAGE_EDIT.get());
    /** Button to apply the function/group form's contents to the currently selected entry. */
    private final JButton applyFormChangesButton = new JButton(HmxMessage.PREFERENCES_LANGUAGE_APPLY.get());
    /** Button to disable the function/group form without applying the changes made. */
    private final JButton discardFormChangesButton = new JButton(HmxMessage.PREFERENCES_LANGUAGE_DISCARD.get());
    /** Form label for the function's or group's name. */
    private final JLabel nameLabel = new JLabel(HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_NAME.get());
    /** Form label for the function's code. */
    private final JLabel codeLabel = new JLabel(HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_CODE.get());
    /** Form label for the function's or group's description. */
    private final JLabel descriptionLabel = new JLabel(HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_DESCRIPTION.get());
    /** Form input for the function's or group's name (maximum of 128 characters length). */
    private final JTextField nameInput = new JTextField(new Validation(128), null, 0);
    /** Form input for the function's code (maximum of 6 characters length). */
    private final JTextField codeInput = new JTextField(new Validation(6), null, 10);
    /** Form input for the flag indicating whether the function's code should be underlined. */
    private final JCheckBox underlineBox = new JCheckBox(HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_UNDERLINE.get());
    /** Form input for the optional description/tooltip text associated with a function/group (maximum of 512 characters length). */
    private final JTextArea descriptionArea = new JTextArea(new Validation(512));
    /** Registered external listeners for changes to the language model. */
    private final List<ActionListener> actionListeners = new ArrayList<ActionListener>(1);

    /** Constructor. */
    public SynFunctionConfigPanel() {
        this.setBorder(BorderFactory.createTitledBorder(HmxMessage.PREFERENCES_LANGUAGE_EDIT_FUNCTIONS.get()));
        this.treeTable = new TreeTable(this.functionModel);
        this.initTable();

        final DesignGridLayout layout = new DesignGridLayout(this);
        layout.row(10).center().add(this.treeTable.createScrollableWrapper()).fill().withOwnRowWidth();
        layout.row().right().add(this.addGroupButton);
        layout.row().center().add(this.editTableSelectionButton, this.applyFormChangesButton, this.discardFormChangesButton);
        layout.row().grid(this.nameLabel).add(this.nameInput, 2);
        layout.row().grid(this.codeLabel).add(this.codeInput).add(this.underlineBox);
        this.descriptionArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        this.descriptionArea.setAutoscrolls(true);
        final JScrollPane scrollableDescription = new JScrollPane(this.descriptionArea);
        scrollableDescription.setBorder(BorderFactory.createLoweredBevelBorder());
        layout.row().grid(this.descriptionLabel).add(scrollableDescription, 2);

        this.initAvailableActions();
        this.handleFormAvailability(ActionMode.TABLE);
    }

    /**
     * Reset the currently displayed syntactical functions for the ones contained in the given provider.
     * 
     * @param provider
     *            the provider for the syntactical functions/functions groups that should be displayed
     */
    public void reset(final ISyntacticalFunctionProvider provider) {
        this.functionModel.reset(provider);
        this.treeTable.expandAll();
    }

    @Override
    public List<List<AbstractSyntacticalFunctionElement>> provideFunctions() {
        return this.functionModel.provideFunctions();
    }

    /**
     * Check whether the form is currently active and used for changing a selected language model.
     * 
     * @return whether the form is currently active and used for changing a selected language model
     */
    public boolean isInEditMode() {
        return this.nameInput.isEnabled();
    }

    /**
     * Register the given listener to be notified whenever the displayed language model changed.
     * 
     * @param listener
     *            the listener to notify
     */
    public void addActionListener(final ActionListener listener) {
        this.actionListeners.add(listener);
    }

    /**
     * Unregister the given listener from notifications regarding changes to the displayed language model.
     * 
     * @param listener
     *            the listener to no longer notify
     * @return whether the listener was registered before and has been removed successfully
     */
    public boolean removeActionListener(final ActionListener listener) {
        return this.actionListeners.remove(listener);
    }

    /**
     * Notify all registered action listeners of a change to the displayed language model (i.e. a function/group was added/changed/moved/removed).
     */
    void triggerActionListeners() {
        final ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");
        for (final ActionListener singleListener : this.actionListeners) {
            singleListener.actionPerformed(event);
        }
    }

    /** Initialize the tree table with its columns. */
    private void initTable() {
        this.treeTable.setTableHeader(null);
        // top level Index or function/group Name
        this.treeTable.addColumn(140, Integer.MAX_VALUE);
        // function Code (empty for groups)
        this.treeTable.addColumn(80, 100);
        // function/group Description
        this.treeTable.addColumnWithToolTip(80, Integer.MAX_VALUE);
        // Button: add function to group (empty for function entries)
        this.treeTable.addButtonColumn(ScitosIcon.ADD, new AbstractAction() {

            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                final int clickedRowIndex = Integer.valueOf(actionEvent.getActionCommand()).intValue();
                final TreePath parentGroupPath = SynFunctionConfigPanel.this.treeTable.getPathForRow(clickedRowIndex);
                final TreePath newPath = SynFunctionConfigPanel.this.functionModel.addSynFunctionRow(parentGroupPath);
                SynFunctionConfigPanel.this.treeTable.setSelectedPath(newPath);
                SynFunctionConfigPanel.this.handleFormAvailability(ActionMode.FUNCTION);
                SynFunctionConfigPanel.this.triggerActionListeners();
            }
        });
        // Button: add function group to group (empty for function entries)
        this.treeTable.addButtonColumn(ScitosIcon.ADD, new AbstractAction() {

            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                final int clickedRowIndex = Integer.valueOf(actionEvent.getActionCommand()).intValue();
                final TreePath parentGroupPath = SynFunctionConfigPanel.this.treeTable.getPathForRow(clickedRowIndex);
                final TreePath newPath = SynFunctionConfigPanel.this.functionModel.addGroupRow(parentGroupPath);
                SynFunctionConfigPanel.this.treeTable.setSelectedPath(newPath);
                SynFunctionConfigPanel.this.handleFormAvailability(ActionMode.FUNCTION_GROUP);
                SynFunctionConfigPanel.this.triggerActionListeners();
            }
        });
        // Button: move function/group up
        this.treeTable.addButtonColumn(ScitosIcon.ARROW_UP, new AbstractAction() {

            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                final int clickedRowIndex = Integer.valueOf(actionEvent.getActionCommand()).intValue();
                SynFunctionConfigPanel.this.moveEntry(clickedRowIndex, false);
            }
        });
        // Button: move function/group down
        this.treeTable.addButtonColumn(ScitosIcon.ARROW_DOWN, new AbstractAction() {

            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                final int clickedRowIndex = Integer.valueOf(actionEvent.getActionCommand()).intValue();
                SynFunctionConfigPanel.this.moveEntry(clickedRowIndex, true);
            }
        });
        // Button: remove function/group
        final TableColumnExt deleteColumn = this.treeTable.addButtonColumn(ScitosIcon.DELETE, new AbstractAction() {

            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                final int clickedRowIndex = Integer.valueOf(actionEvent.getActionCommand()).intValue();
                final TreePath entryPath = SynFunctionConfigPanel.this.treeTable.getPathForRow(clickedRowIndex);
                SynFunctionConfigPanel.this.functionModel.removeEntry(entryPath);
                SynFunctionConfigPanel.this.triggerActionListeners();
            }
        });
        this.treeTable.applyRowHeight(deleteColumn, "");
        this.treeTable.expandAll();
        this.treeTable.setVisibleRowCount(3);
    }

    /**
     * Move the group or entry at the designated path up or down by own step.
     * 
     * @param rowIndex
     *            index of the relation template group or entry row to move
     * @param moveDown
     *            whether the group or entry should be moved down; otherwise moved up
     */
    void moveEntry(final int rowIndex, final boolean moveDown) {
        final TreePath targetPath = this.treeTable.getPathForRow(rowIndex);
        // determine those paths under the same parent that are currently expanded
        final List<TreePath> expandedSiblings = this.treeTable.getExpandedChildren(targetPath.getParentPath());
        // actually move the indicated element
        this.functionModel.moveEntry(targetPath, moveDown);
        // reset the expanded state of the targetPath's siblings
        this.treeTable.expandPaths(expandedSiblings);
        this.triggerActionListeners();
    }

    /** Initialize all the possible interactions with buttons and other components. */
    private void initAvailableActions() {
        this.treeTable.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(final TreeSelectionEvent event) {
                SynFunctionConfigPanel.this.applyTreeTableSelection();
                SynFunctionConfigPanel.this.handleFormAvailability(ActionMode.TABLE);
            }
        });
        this.addGroupButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                final TreePath newEntry = SynFunctionConfigPanel.this.functionModel.addTopLevelGroup();
                SynFunctionConfigPanel.this.treeTable.expandPath(newEntry);
                SynFunctionConfigPanel.this.treeTable.setSelectedPath(newEntry);
                SynFunctionConfigPanel.this.handleFormAvailability(ActionMode.TABLE);
                SynFunctionConfigPanel.this.triggerActionListeners();
            }
        });
        this.editTableSelectionButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                final TreeTableRow selection = SynFunctionConfigPanel.this.getSelectedFunctionOrGroupRow();
                final ActionMode mode = selection instanceof SynFunctionTreeModel.SynFunctionRow ? ActionMode.FUNCTION : ActionMode.FUNCTION_GROUP;
                SynFunctionConfigPanel.this.handleFormAvailability(mode);
            }
        });
        this.applyFormChangesButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                SynFunctionConfigPanel.this.applyFormChanges();
            }
        });
        this.discardFormChangesButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                SynFunctionConfigPanel.this.applyTreeTableSelection();
                SynFunctionConfigPanel.this.handleFormAvailability(ActionMode.TABLE);
            }
        });
    }

    /** Update the form contents according to the currently selected row in the tree table. */
    void applyTreeTableSelection() {
        final SynFunctionTreeModel.TreeTableRow row = this.getSelectedFunctionOrGroupRow();
        if (row == null) {
            this.nameInput.setText(null);
            this.descriptionArea.setText(null);
        } else {
            this.nameInput.setText(row.getName());
            this.descriptionArea.setText(row.getDescription());
        }
        if (row instanceof SynFunctionTreeModel.SynFunctionRow) {
            this.codeInput.setText(((SynFunctionTreeModel.SynFunctionRow) row).getCode());
            this.underlineBox.setSelected(((SynFunctionTreeModel.SynFunctionRow) row).isUnderlined());
        } else {
            this.codeInput.setText(null);
            this.underlineBox.setSelected(false);
        }
    }

    /** Apply any changes made in the form to the selected table entry. */
    void applyFormChanges() {
        final String name = this.nameInput.getText().trim();
        final String code = this.codeInput.getText().trim();
        final SynFunctionTreeModel.TreeTableRow row = this.getSelectedFunctionOrGroupRow();
        if (name.isEmpty()) {
            MessageHandler.showMessage(HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_NAME_MANDATORY.get(), HmxMessage.PREFERENCES_LANGUAGE_APPLY.get(),
                    MessageHandler.MessageType.WARN);
        } else if (row instanceof SynFunctionTreeModel.SynFunctionRow && code.isEmpty()) {
            MessageHandler.showMessage(HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_CODE_MANDATORY.get(), HmxMessage.PREFERENCES_LANGUAGE_APPLY.get(),
                    MessageHandler.MessageType.WARN);
        } else {
            row.setName(name);
            final String description = this.descriptionArea.getText().trim();
            row.setDescription(description.isEmpty() ? null : description);
            if (row instanceof SynFunctionTreeModel.SynFunctionRow) {
                ((SynFunctionTreeModel.SynFunctionRow) row).setCode(code);
                final boolean underlined = this.underlineBox.isSelected();
                ((SynFunctionTreeModel.SynFunctionRow) row).setUnderlined(underlined);
            }
            this.functionModel.updatedRow(this.treeTable.getSelectedPath());
            this.handleFormAvailability(ActionMode.TABLE);
            this.triggerActionListeners();
        }
    }

    /**
     * Look up the currently selected function/group row's internal mutable representation.
     * 
     * @return selected function/group row (or {@code null})
     */
    SynFunctionTreeModel.TreeTableRow getSelectedFunctionOrGroupRow() {
        final TreePath selection = this.treeTable.getSelectedPath();
        return this.functionModel.getFunctionOrGroupRowAtPath(selection);
    }

    /**
     * Make sure the correct components are enabled/disabled according to the given flag.
     * 
     * @param modeToSelect
     *            which components should be enabled
     */
    void handleFormAvailability(final ActionMode modeToSelect) {
        final boolean tableEnabled = modeToSelect == ActionMode.TABLE;
        this.treeTable.setEnabled(tableEnabled);
        this.addGroupButton.setEnabled(tableEnabled);

        this.editTableSelectionButton.setEnabled(tableEnabled && this.getSelectedFunctionOrGroupRow() != null);
        this.applyFormChangesButton.setEnabled(!tableEnabled);
        this.discardFormChangesButton.setEnabled(!tableEnabled);

        final boolean functionFormEnabled = modeToSelect == ActionMode.FUNCTION;
        this.nameLabel.setEnabled(!tableEnabled);
        this.nameInput.setEnabled(!tableEnabled);
        this.codeLabel.setEnabled(functionFormEnabled);
        this.codeInput.setEnabled(functionFormEnabled);
        this.underlineBox.setEnabled(functionFormEnabled);
        this.descriptionLabel.setEnabled(!tableEnabled);
        this.descriptionArea.setEnabled(!tableEnabled);
    }
}
