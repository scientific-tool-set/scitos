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
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import net.java.dev.designgridlayout.DesignGridLayout;

import org.hmx.scitos.domain.util.CollectionUtil;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.core.option.HmxLanguageOption;
import org.hmx.scitos.hmx.domain.model.LanguageModel;
import org.hmx.scitos.view.ScitosIcon;
import org.hmx.scitos.view.swing.MessageHandler;
import org.hmx.scitos.view.swing.MessageHandler.MessageType;
import org.hmx.scitos.view.swing.util.Validation;
import org.hmx.scitos.view.swing.util.table.TreeTable;
import org.jdesktop.swingx.table.TableColumnExt;

/** UI component displaying the available language models and allowing user-defined models to be added or removed. */
public final class LanguageConfigPanel extends JPanel {

    /** The table model holding the system- and user-defined languages. */
    final LanguageTreeModel languageTreeTableModel;
    /** The table displaying the system- and user-defined languages. */
    final TreeTable treeTable;
    /** Button to enable the language form to edit the currently selected entry. */
    private final JButton editTableSelectionButton = new JButton(HmxMessage.PREFERENCES_LANGUAGE_EDIT.get());
    /** Button to apply the language form's contents to the currently selected entry. */
    private final JButton applyFormChangesButton = new JButton(HmxMessage.PREFERENCES_LANGUAGE_APPLY.get());
    /** Button to disable the language form without applying the changes made. */
    private final JButton discardFormChangesButton = new JButton(HmxMessage.PREFERENCES_LANGUAGE_DISCARD.get());
    /** Labels associated with form fields that should be enabled/disabled with their respective inputs. */
    private final List<JLabel> formLabels = new ArrayList<>(3);
    /** Form input field: name of a language model. */
    private final JTextField languageNameInput = new JTextField(new Validation(128), null, 0);
    /** Form input field: indication whether the origin text is left-to-right oriented. */
    private final JRadioButton leftToRightButton = new JRadioButton(HmxMessage.PREFERENCES_LANGUAGE_ORIENTATION_LTR.get());
    /** Form input field: indication whether the origin text is right-to-left oriented. */
    private final JRadioButton rightToLeftButton = new JRadioButton(HmxMessage.PREFERENCES_LANGUAGE_ORIENTATION_RTL.get());
    /** Grouping for the text orientation radio buttons – ensuring only one is selected. */
    private final ButtonGroup orientationButtonGroup = new ButtonGroup();
    /** Form input field: list of recommended origin text fonts. */
    private final JTextField recommendedFontsInput = new JTextField(new Validation(512), null, 0);
    /** Registered external listeners for changes to the language model selection. */
    private final List<ActionListener> actionListeners = new ArrayList<>(1);

    /**
     * Constructor.
     * 
     * @param options
     *            the global preferences handler providing the configurable language models
     */
    public LanguageConfigPanel(final HmxLanguageOption options) {
        this.setBorder(BorderFactory.createTitledBorder(HmxMessage.PREFERENCES_LANGUAGE_EDIT_LANGUAGES.get()));
        this.languageTreeTableModel = new LanguageTreeModel(options);
        this.treeTable = new TreeTable(this.languageTreeTableModel).setAlwaysExpanded();
        this.initLanguageTable();
        final DesignGridLayout layout = new DesignGridLayout(this);
        layout.row(2).center().add(this.treeTable.createScrollableWrapper()).fill().withOwnRowWidth();
        layout.row().center().add(this.editTableSelectionButton, this.applyFormChangesButton, this.discardFormChangesButton);
        this.editTableSelectionButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                LanguageConfigPanel.this.handleFormAvailability(true);
                LanguageConfigPanel.this.triggerActionListeners();
            }
        });
        this.applyFormChangesButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                LanguageConfigPanel.this.applyFormChanges();
            }
        });
        this.discardFormChangesButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                LanguageConfigPanel.this.handleTableSelection();
            }
        });
        this.formLabels.add(new JLabel(HmxMessage.PREFERENCES_LANGUAGE_NAME.get()));
        this.formLabels.add(new JLabel(HmxMessage.PREFERENCES_LANGUAGE_ORIENTATION.get()));
        this.formLabels.add(new JLabel(HmxMessage.PREFERENCES_LANGUAGE_FONTS.get()));
        layout.row().grid(this.formLabels.get(0)).add(this.languageNameInput, 2);
        layout.row().grid(this.formLabels.get(1)).add(this.leftToRightButton, this.rightToLeftButton);
        layout.row().grid(this.formLabels.get(2)).add(this.recommendedFontsInput, 2);
        this.orientationButtonGroup.add(this.leftToRightButton);
        this.orientationButtonGroup.add(this.rightToLeftButton);
        this.orientationButtonGroup.setSelected(this.leftToRightButton.getModel(), true);
        this.handleFormAvailability(false);
    }

    /**
     * Getter for the current state of the user-defined language models.
     * 
     * @return user-defined language models
     */
    public List<LanguageModel> getUserModels() {
        return this.languageTreeTableModel.getUserLanguageModels();
    }

    /**
     * Getter for the currently selected language model in the respective table.
     * 
     * @return selected language model
     */
    public LanguageModel getSelectedModel() {
        final TreePath selectedPath = this.treeTable.getSelectedPath();
        return this.languageTreeTableModel.getModelForPath(selectedPath);
    }

    /**
     * Check whether the form is currently active and used for changing a selected language model.
     * 
     * @return whether the form is currently active and used for changing a selected language model
     */
    public boolean isInEditMode() {
        return this.languageNameInput.isEnabled();
    }

    /**
     * Check whether the currently selected language model (if one is selected) is a user-defined one.
     * 
     * @return whether a user-defined language model is selected
     */
    public boolean isSelectedModelUserDefined() {
        final TreePath selectedPath = this.treeTable.getSelectedPath();
        return this.languageTreeTableModel.isModelAtPathUserDefined(selectedPath);
    }

    /**
     * Register the given listener to be notified whenever the selected language model changed.
     * 
     * @param listener
     *            the listener to notify
     */
    public void addActionListener(final ActionListener listener) {
        this.actionListeners.add(listener);
    }

    /**
     * Unregister the given listener from notifications regarding changes to the selected language model.
     * 
     * @param listener
     *            the listener to no longer notify
     * @return whether the listener was registered before and has been removed successfully
     */
    public boolean removeActionListener(final ActionListener listener) {
        return this.actionListeners.remove(listener);
    }

    /**
     * Notify all registered action listeners of a change to the selected language model (either another one was selected or the language model was
     * altered itself).
     */
    void triggerActionListeners() {
        final ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, String.valueOf(this.treeTable.getSelectedRow()));
        for (final ActionListener singleListener : this.actionListeners) {
            singleListener.actionPerformed(event);
        }
    }

    /** Initialize the table containing the system- and user-defined language models. */
    private void initLanguageTable() {
        this.treeTable.setTableHeader(null);
        this.treeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.treeTable.addColumn(80, Integer.MAX_VALUE);
        this.treeTable.addColumn(100, 120);
        this.treeTable.addButtonColumn(ScitosIcon.ADD, new AbstractAction() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                final int clickedRowIndex = Integer.valueOf(event.getActionCommand()).intValue();
                final TreePath clickedPath = LanguageConfigPanel.this.treeTable.getPathForRow(clickedRowIndex);
                final TreePath newPath = LanguageConfigPanel.this.languageTreeTableModel.addModelRowClone(clickedPath);
                LanguageConfigPanel.this.treeTable.setSelectedPath(newPath);
                LanguageConfigPanel.this.handleFormAvailability(true);
                LanguageConfigPanel.this.triggerActionListeners();
            }
        });
        final TableColumnExt deleteColumn = this.treeTable.addButtonColumn(ScitosIcon.DELETE, new AbstractAction() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                final int clickedRowIndex = Integer.valueOf(event.getActionCommand()).intValue();
                final TreePath clickedPath = LanguageConfigPanel.this.treeTable.getPathForRow(clickedRowIndex);
                LanguageConfigPanel.this.languageTreeTableModel.deleteModelRow(clickedPath);
                LanguageConfigPanel.this.treeTable.setSelectedPath(null);
            }
        });
        this.treeTable.applyRowHeight(deleteColumn, "");
        this.treeTable.expandAll();
        this.treeTable.setVisibleRowCount(2);
        this.treeTable.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(final TreeSelectionEvent event) {
                LanguageConfigPanel.this.handleTableSelection();
            }
        });
    }

    /**
     * Reset the form's contents to match the currently selected language model, ensure all components are enabled/disabled as appropriate, and
     * trigger any action listeners that have been registered.
     */
    void handleTableSelection() {
        final TreePath selectedPath = this.treeTable.getSelectedPath();
        final String languageName;
        final boolean leftToRightOriented;
        final String recommendedFonts;
        if (selectedPath == null) {
            languageName = "";
            leftToRightOriented = true;
            recommendedFonts = "";
        } else {
            final LanguageModel selectedLanguage = this.languageTreeTableModel.getModelForPath(selectedPath);
            languageName = selectedLanguage.getName();
            leftToRightOriented = selectedLanguage.isLeftToRightOriented();
            recommendedFonts = CollectionUtil.toString(selectedLanguage.getRecommendedFonts(), "; ");
        }
        this.languageNameInput.setText(languageName);
        this.orientationButtonGroup.setSelected((leftToRightOriented ? this.leftToRightButton : this.rightToLeftButton).getModel(), true);
        this.recommendedFontsInput.setText(recommendedFonts);
        this.handleFormAvailability(false);
        this.triggerActionListeners();
    }

    /** Apply the form's contents to the associated language model, disable the form again, and trigger any registered action listeners. */
    void applyFormChanges() {
        final String languageName = this.languageNameInput.getText().trim();
        if (languageName.isEmpty()) {
            MessageHandler.showMessage(HmxMessage.PREFERENCES_LANGUAGE_NAME_MANDATORY.get(), HmxMessage.PREFERENCES_LANGUAGE_NAME.get(),
                    MessageType.WARN);
            return;
        }
        final boolean leftToRightOriented = this.leftToRightButton.isSelected();
        final String[] recommendedFonts = this.recommendedFontsInput.getText().trim().split("([\\s]*[;][\\s]*)+");
        final List<String> fontList = new ArrayList<>(Arrays.asList(recommendedFonts));
        // remove empty entry if there is one
        fontList.remove("");
        final LanguageModel model = this.getSelectedModel();
        model.setName(languageName);
        model.setLeftToRightOriented(leftToRightOriented);
        model.setRecommendedFonts(fontList);
        this.fireSelectedModelRowUpdated();
        this.handleFormAvailability(false);
        this.triggerActionListeners();
    }
    
    /** Trigger the update of the selected table row to incorporate any recent changes. */
    void fireSelectedModelRowUpdated() {
        final LanguageModel model = this.getSelectedModel();
        this.languageTreeTableModel.fireModelRowUpdated(model);
    }

    /**
     * Make sure the correct components are enabled/disabled according to the given flag.
     * 
     * @param enableForm
     *            if the form should be enabled for editing (and the above table disabled) or vice versa
     */
    void handleFormAvailability(final boolean enableForm) {
        this.treeTable.setEnabled(!enableForm);

        this.editTableSelectionButton.setEnabled(!enableForm && this.isSelectedModelUserDefined());
        this.applyFormChangesButton.setEnabled(enableForm);
        this.discardFormChangesButton.setEnabled(enableForm);

        for (final JLabel singleLabel : this.formLabels) {
            singleLabel.setEnabled(enableForm);
        }
        this.languageNameInput.setEnabled(enableForm);
        this.leftToRightButton.setEnabled(enableForm);
        this.rightToLeftButton.setEnabled(enableForm);
        this.recommendedFontsInput.setEnabled(enableForm);
    }
}
