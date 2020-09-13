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
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import net.java.dev.designgridlayout.DesignGridLayout;

import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.domain.ISemanticalRelationProvider;
import org.hmx.scitos.hmx.domain.model.RelationModel;
import org.hmx.scitos.hmx.domain.model.RelationTemplate;
import org.hmx.scitos.hmx.domain.model.RelationTemplate.AssociateRole;
import org.hmx.scitos.view.ScitosIcon;
import org.hmx.scitos.view.swing.MessageHandler;
import org.hmx.scitos.view.swing.MessageHandler.Choice;
import org.hmx.scitos.view.swing.MessageHandler.MessageType;
import org.hmx.scitos.view.swing.util.Validation;
import org.hmx.scitos.view.swing.util.table.TreeTable;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * TreeTable displaying the editable {@link RelationTreeModel}.
 */
public final class RelationTreeTable extends JPanel {

    /** The wrapped tree jtable component. */
    final TreeTable treeTable;
    /** The tree jtable model containing the model entries that are being displayed/modified. */
    final RelationTreeModel treeModel;
    /** Button to enable the relation template form to edit the currently selected entry. */
    private final JButton editTableSelectionButton = new JButton(HmxMessage.PREFERENCES_RELATION_EDIT.get());
    /** Button to apply the relation template form's contents to the currently selected entry. */
    private final JButton applyFormChangesButton = new JButton(HmxMessage.PREFERENCES_RELATION_APPLY.get());
    /** Button to disable the relation template form without applying the changes made. */
    private final JButton discardFormChangesButton = new JButton(HmxMessage.PREFERENCES_RELATION_DISCARD.get());
    /** Labels indicating the expected type of value in form fields. */
    private final List<JLabel> formLabels = new ArrayList<>(4);
    /** Input field for the role with high weight in the relation template (maximum of 32 characters length). */
    private final JTextField highWeightRoleInput = new JTextField(new Validation(32), null, 0);
    /** Input field for the role with low weight in the relation template (maximum of 32 characters length). */
    private final JTextField lowWeightRoleInput = new JTextField(new Validation(32), null, 0);
    /** Check box for determining whether all roles in the relation template are the same high weight one (i.e. no low weight role). */
    private final JCheckBox allSameRoleCheckBox = new JCheckBox(HmxMessage.PREFERENCES_RELATION_HIGH_WEIGHT_ONLY.get());
    /** Check box for determining whether the low weight role can occur multiple times in a single relation. */
    private final JCheckBox repeatingLowWeightRoleCheckBox = new JCheckBox(HmxMessage.PREFERENCES_RELATION_LOW_WEIGHT_REPEAT.get());
    /** Radio button to indicate whether the high weight role is the first in a relation (by default). */
    private final JRadioButton highWeightFirstButton = new JRadioButton(HmxMessage.PREFERENCES_RELATION_HIGH_WEIGHT_FIRST.get());
    /** Radio button to indicate whether the high weight role is the last in a relation (by default). */
    private final JRadioButton highWeightLastButton = new JRadioButton(HmxMessage.PREFERENCES_RELATION_HIGH_WEIGHT_LAST.get());
    /**
     * Button group to ensure that exactly one of the two radio buttons {@link #highWeightFirstButton} and {@link #highWeightLastButton} is selected
     * at any given time.
     */
    private final ButtonGroup radioButtonGrouping = new ButtonGroup();
    /** Input area for the optional description/tooltip text associated with a relation template (maximum of 512 characters length). */
    private final JTextArea descriptionArea = new JTextArea(new Validation(512));

    /**
     * Main constructor.
     *
     * @param relationProvider
     *            the provider of the initial {@link RelationTemplate}s being displayed/modified
     */
    public RelationTreeTable(final ISemanticalRelationProvider relationProvider) {
        this.setBorder(null);
        this.treeModel = new RelationTreeModel(relationProvider);
        this.treeTable = new TreeTable(this.treeModel);
        this.initTable();
        this.radioButtonGrouping.add(this.highWeightFirstButton);
        this.radioButtonGrouping.add(this.highWeightLastButton);
        this.radioButtonGrouping.setSelected(this.highWeightFirstButton.getModel(), true);

        final DesignGridLayout layout = new DesignGridLayout(this);
        layout.row(10).center().add(this.treeTable.createScrollableWrapper()).fill().withOwnRowWidth();
        final JButton addGroupButton = new JButton(HmxMessage.PREFERENCES_RELATION_ADD_GROUP.get(), ScitosIcon.ADD.create());
        addGroupButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                RelationTreeTable.this.addGroup();
            }
        });
        layout.row().right().add(addGroupButton);
        layout.row().center().add(this.editTableSelectionButton, this.applyFormChangesButton, this.discardFormChangesButton);
        this.formLabels.add(new JLabel(HmxMessage.PREFERENCES_RELATION_HIGH_WEIGHT_ROLE.get()));
        this.formLabels.add(new JLabel(HmxMessage.PREFERENCES_RELATION_LOW_WEIGHT_ROLE.get()));
        this.formLabels.add(new JLabel(HmxMessage.PREFERENCES_RELATION_HIGH_WEIGHT_POSITION.get()));
        this.formLabels.add(new JLabel(HmxMessage.PREFERENCES_RELATION_TOOLTIP.get()));
        layout.row().grid(this.formLabels.get(0)).add(this.highWeightRoleInput).add(this.allSameRoleCheckBox);
        layout.row().grid(this.formLabels.get(1)).add(this.lowWeightRoleInput).add(this.repeatingLowWeightRoleCheckBox);
        layout.row().grid(this.formLabels.get(2)).add(this.highWeightFirstButton).add(this.highWeightLastButton);
        this.descriptionArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        this.descriptionArea.setAutoscrolls(true);
        this.descriptionArea.setRows(3);
        final JScrollPane scrollableDescription = new JScrollPane(this.descriptionArea);
        scrollableDescription.setBorder(BorderFactory.createLoweredBevelBorder());
        layout.row().grid(this.formLabels.get(3)).add(scrollableDescription, 2);
        // add appropriate listeners to form components
        this.initFormAreaCapabilities();
        this.handleFormAvailability(false);
    }

    /**
     * Extract the currently represented relation model (i.e. groups of relation templates).
     * 
     * @return copy of the displayed relation groups and entries
     */
    public RelationModel getRelationModel() {
        final RelationModel model = new RelationModel();
        model.addAll(this.treeModel.provideRelationTemplates());
        return model;
    }

    /** Initialize the tree table with its columns. */
    private void initTable() {
        // Index
        this.treeTable.addColumn(100, 120);
        // Defined RelationTemplate as presented in the context menu in the semantical analysis view (empty for group)
        this.treeTable.addColumn(200, Integer.MAX_VALUE);
        // Button: add template to group (empty for template entry)
        this.treeTable.addButtonColumn(ScitosIcon.ADD, new AbstractAction() {

            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                final int clickedRowIndex = Integer.valueOf(actionEvent.getActionCommand()).intValue();
                final TreePath parentGroupPath = RelationTreeTable.this.treeTable.getPathForRow(clickedRowIndex);
                RelationTreeTable.this.addTemplateEntryToGroup(parentGroupPath);
            }
        });
        // Button: move template/group up
        this.treeTable.addButtonColumn(ScitosIcon.ARROW_UP, new AbstractAction() {

            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                final int clickedRowIndex = Integer.valueOf(actionEvent.getActionCommand()).intValue();
                final TreePath entryPath = RelationTreeTable.this.treeTable.getPathForRow(clickedRowIndex);
                RelationTreeTable.this.moveEntry(entryPath, false);
            }
        });
        // Button: move template/group down
        this.treeTable.addButtonColumn(ScitosIcon.ARROW_DOWN, new AbstractAction() {

            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                final int clickedRowIndex = Integer.valueOf(actionEvent.getActionCommand()).intValue();
                final TreePath entryPath = RelationTreeTable.this.treeTable.getPathForRow(clickedRowIndex);
                RelationTreeTable.this.moveEntry(entryPath, true);
            }
        });
        // Button: remove template/group
        final TableColumnExt deleteColumn = this.treeTable.addButtonColumn(ScitosIcon.DELETE, new AbstractAction() {

            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                final int clickedRowIndex = Integer.valueOf(actionEvent.getActionCommand()).intValue();
                final TreePath entryPath = RelationTreeTable.this.treeTable.getPathForRow(clickedRowIndex);
                RelationTreeTable.this.removeEntry(entryPath);
            }
        });
        this.treeTable.applyRowHeight(deleteColumn, "");
        this.treeTable.expandAll();
        this.treeTable.setVisibleRowCount(3);
    }

    /** Add appropriate listeners to form components. */
    private void initFormAreaCapabilities() {
        this.treeTable.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(final TreeSelectionEvent event) {
                RelationTreeTable.this.updateFormFields(RelationTreeTable.this.getSelectedTemplateEntry());
                RelationTreeTable.this.handleFormAvailability(false);
            }
        });
        this.editTableSelectionButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                RelationTreeTable.this.handleFormAvailability(true);
            }
        });
        this.applyFormChangesButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                RelationTreeTable.this.applyFormChanges();
            }
        });
        this.discardFormChangesButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                RelationTreeTable.this.updateFormFields(RelationTreeTable.this.getSelectedTemplateEntry());
                RelationTreeTable.this.handleFormAvailability(false);
            }
        });
        this.allSameRoleCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                RelationTreeTable.this.handleFormAvailability(true);
            }
        });
        this.repeatingLowWeightRoleCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                RelationTreeTable.this.handleFormAvailability(true);
            }
        });
    }

    /**
     * Determine the path of the currently selected relation template entry.
     * 
     * @return path to the selected relation template entry; or {@code null} if there is no selection or the selected entry is not a single relation
     *         template
     */
    TreePath getSelectedTemplateEntryPath() {
        final TreePath selectedPath = this.treeTable.getSelectedPath();
        if (selectedPath == null || selectedPath.getPathCount() != 3) {
            return null;
        }
        return selectedPath;
    }

    /**
     * Get the currently selected relation template.
     * 
     * @return selection relation template; or {@code null} if there is no selection or the selected entry is not a single relation template
     * @see #getSelectedTemplateEntryPath()
     */
    RelationTemplate getSelectedTemplateEntry() {
        final TreePath selectedPath = this.getSelectedTemplateEntryPath();
        if (selectedPath == null) {
            return null;
        }
        return this.treeModel.getTemplateAtPath(selectedPath);
    }

    /**
     * Update the form contents to represent the given relation template.
     * 
     * @param selection
     *            the relation template to represent in the form (can be {@code null}, resulting in the form being cleared/reset)
     */
    void updateFormFields(final RelationTemplate selection) {
        final String highWeightRole;
        final boolean allSameRole;
        final String lowWeightRole;
        final boolean repeatingLowWeightRole;
        final JRadioButton radioButtonToSelect;
        final String description;
        if (selection == null) {
            // clear form contents / reset to defaults
            highWeightRole = "";
            allSameRole = false;
            lowWeightRole = "";
            repeatingLowWeightRole = false;
            radioButtonToSelect = this.highWeightFirstButton;
            description = "";
        } else {
            final List<AssociateRole> roles = selection.getAssociateRoles(2);
            if (roles.get(0).isHighWeight()) {
                highWeightRole = roles.get(0).getRole();
                allSameRole = roles.get(1).isHighWeight();
                if (allSameRole) {
                    // both roles are high weight, i.e. there is no low weight role
                    lowWeightRole = null;
                } else {
                    lowWeightRole = roles.get(1).getRole();
                }
                repeatingLowWeightRole = false;
                radioButtonToSelect = this.highWeightFirstButton;
            } else {
                // first role is low weight, therefore the second must be high weight
                highWeightRole = roles.get(1).getRole();
                allSameRole = false;
                lowWeightRole = roles.get(0).getRole();
                repeatingLowWeightRole = selection.canHaveMoreThanTwoAssociates();
                radioButtonToSelect = this.highWeightLastButton;
            }
            description = selection.getDescription();
        }
        this.highWeightRoleInput.setText(highWeightRole);
        this.allSameRoleCheckBox.setSelected(allSameRole);
        this.lowWeightRoleInput.setText(lowWeightRole);
        this.repeatingLowWeightRoleCheckBox.setSelected(repeatingLowWeightRole);
        this.radioButtonGrouping.setSelected(radioButtonToSelect.getModel(), true);
        this.descriptionArea.setText(description);
    }

    /**
     * Make sure the correct components are enabled/disabled according to the given flag and the respective state of the available check boxes.
     * 
     * @param enableForm
     *            if the form should be enabled for editing (and the above table disabled) or vice versa
     */
    void handleFormAvailability(final boolean enableForm) {
        this.treeTable.setEnabled(!enableForm);

        this.editTableSelectionButton.setEnabled(!enableForm && this.getSelectedTemplateEntry() != null);
        this.applyFormChangesButton.setEnabled(enableForm);
        this.discardFormChangesButton.setEnabled(enableForm);

        for (final JLabel singleLabel : this.formLabels) {
            singleLabel.setEnabled(enableForm);
        }
        this.highWeightRoleInput.setEnabled(enableForm);
        this.allSameRoleCheckBox.setEnabled(enableForm && !this.repeatingLowWeightRoleCheckBox.isSelected());
        final boolean dependentFieldsEnabled = enableForm && !this.allSameRoleCheckBox.isSelected();
        this.lowWeightRoleInput.setEnabled(dependentFieldsEnabled);
        this.repeatingLowWeightRoleCheckBox.setEnabled(dependentFieldsEnabled);
        this.highWeightFirstButton.setEnabled(dependentFieldsEnabled);
        this.highWeightLastButton.setEnabled(dependentFieldsEnabled);
        this.descriptionArea.setEnabled(enableForm);
    }

    /**
     * Build the currently defined relation template from the form's contents (showing an error message if they are invalid) and replacing the
     * currently selected table entry accordingly. The form will be disabled in case of a successful update, i.e. the table will be enabled again.
     */
    void applyFormChanges() {
        final AssociateRole leadingAssociate;
        final AssociateRole repetitiveAssociate;
        final AssociateRole trailingAssociate;
        final String highWeightRole = this.highWeightRoleInput.getText().trim();
        if (this.allSameRoleCheckBox.isSelected()) {
            if (highWeightRole.isEmpty()) {
                // the high weight role label is mandatory
                MessageHandler.showMessage(HmxMessage.ERROR_PREFERENCES_RELATION_HIGHWEIGHT_ROLE.get(), Message.ERROR.get(), MessageType.ERROR);
                return;
            }
            leadingAssociate = new AssociateRole(highWeightRole, true);
            repetitiveAssociate = leadingAssociate;
            trailingAssociate = leadingAssociate;
        } else {
            final String lowWeightRole = this.lowWeightRoleInput.getText().trim();
            if (highWeightRole.isEmpty() || lowWeightRole.isEmpty()) {
                // the role labels are mandatory
                MessageHandler.showMessage(HmxMessage.ERROR_PREFERENCES_RELATION_ROLES.get(), Message.ERROR.get(), MessageType.ERROR);
                return;
            }
            if (this.highWeightFirstButton.isSelected()) {
                leadingAssociate = new AssociateRole(highWeightRole, true);
                trailingAssociate = new AssociateRole(lowWeightRole, false);
            } else {
                leadingAssociate = new AssociateRole(lowWeightRole, false);
                trailingAssociate = new AssociateRole(highWeightRole, true);
            }
            if (this.repeatingLowWeightRoleCheckBox.isSelected()) {
                repetitiveAssociate = new AssociateRole(lowWeightRole, false);
            } else {
                repetitiveAssociate = null;
            }
        }
        final String description = this.descriptionArea.getText();
        final RelationTemplate result = new RelationTemplate(leadingAssociate, repetitiveAssociate, trailingAssociate, description);
        final TreePath selectedPath = this.getSelectedTemplateEntryPath();
        this.treeModel.updateTemplateEntry(selectedPath, result);
        this.handleFormAvailability(false);
    }

    /** Add a new top level group. */
    void addGroup() {
        final TreePath groupPath = this.treeModel.addGroupEntry();
        this.treeTable.expandPath(groupPath);
        this.treeTable.setSelectedPath(groupPath);
    }

    /**
     * Add a new relation template entry to the group at the given path.
     * 
     * @param groupPath
     *            designated parent path for the new relation template entry
     */
    void addTemplateEntryToGroup(final TreePath groupPath) {
        final TreePath templatePath = this.treeModel.addTemplateEntryToGroup(groupPath);
        this.treeTable.expandPath(groupPath);
        this.treeTable.setSelectedPath(templatePath);
        this.handleFormAvailability(true);
    }

    /**
     * Move the group or entry at the designated path up or down by own step.
     * 
     * @param targetPath
     *            path to the relation template group or entry to move
     * @param moveDown
     *            whether the group or entry should be moved down; otherwise moved up
     */
    void moveEntry(final TreePath targetPath, final boolean moveDown) {
        final List<TreePath> expandedSiblings;
        if (targetPath.getPathCount() == 2) {
            // determine those paths under the same parent that are currently expanded (only relevant for groups)
            expandedSiblings = this.treeTable.getExpandedChildren(targetPath.getParentPath());
        } else {
            // template nodes can have no children - no need do to anything here
            expandedSiblings = Collections.emptyList();
        }
        // actually move the indicated element
        this.treeModel.moveEntry(targetPath, moveDown);
        // reset the expanded state of the targetPath's siblings
        this.treeTable.expandPaths(expandedSiblings);
    }

    /**
     * Delete the group or entry at the designated path.
     * 
     * @param targetPath
     *            path to the relation template group or entry to delete
     */
    void removeEntry(final TreePath targetPath) {
        final Object node = targetPath.getLastPathComponent();
        // delete only template entries or empty groups without confirmation
        if (this.treeModel.isLeaf(node)
                || Choice.YES == MessageHandler.showConfirmDialog(HmxMessage.PREFERENCES_RELATION_REMOVE_GROUP_CONFIRM.get(),
                        HmxMessage.PREFERENCES_RELATION_REMOVE_GROUP.get() + " (" + this.treeModel.getValueAt(node, 0) + ')')) {
            this.treeModel.removeEntry(targetPath);
        }
    }
}
