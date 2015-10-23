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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.hmx.scitos.ais.core.AisModelHandler;
import org.hmx.scitos.ais.core.AisOption;
import org.hmx.scitos.ais.core.i18n.AisMessage;
import org.hmx.scitos.ais.domain.model.DetailCategory;
import org.hmx.scitos.ais.domain.model.Interview;
import org.hmx.scitos.ais.domain.model.MutableDetailCategoryModel;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.view.swing.ScitosApp;
import org.hmx.scitos.view.swing.util.ViewUtil;

/** Dialog for modifying the detail categories of a project while it is already in progress. */
public final class CategoryModelChangeDialog extends JDialog {

    /** Model handler for the targeted project. */
    private final AisModelHandler modelHandler;
    /** Handler instance to set the default for new projects as well. */
    private final AisOption options;
    /** View component displaying the modifiable detail category model. */
    private final DetailCategoryTreeTable treeTable;

    /**
     * Main constructor.
     *
     * @param modelHandler
     *            the model handler for the targeted project
     * @param options
     *            the handler instance to set the default for new projects on
     */
    public CategoryModelChangeDialog(final AisModelHandler modelHandler, final AisOption options) {
        super(ScitosApp.getClient().getFrame(), AisMessage.PROJECT_CHANGE_CATEGORIES.get(), true);
        this.modelHandler = modelHandler;
        this.options = options;
        final JPanel contentPane = new JPanel(new BorderLayout());
        this.treeTable = new DetailCategoryTreeTable(modelHandler.getModel(), true);
        contentPane.add(this.treeTable);
        final JPanel buttonArea = new JPanel(new GridBagLayout());
        buttonArea.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        final JButton cancelButton = new JButton(Message.CANCEL.get());
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                CategoryModelChangeDialog.this.dispose();
            }
        });
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
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                CategoryModelChangeDialog.this.okButtonPressed();
            }
        });
        constraints.gridx = 2;
        constraints.weightx = 0;
        constraints.anchor = GridBagConstraints.BASELINE_TRAILING;
        buttonArea.add(okButton, constraints);
        contentPane.add(buttonArea, BorderLayout.SOUTH);
        this.setContentPane(contentPane);
        this.setMinimumSize(new Dimension(600, 400));
        this.pack();
        ViewUtil.centerOnParent(this);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                cancelButton.requestFocusInWindow();
            }
        });
    }

    /** Apply the detail category model changes after the OK button has been clicked on. */
    void okButtonPressed() {
        if (!this.treeTable.containsValidModel()) {
            return;
        }
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        // count all occurrences of currently applied (old) detail categories
        final Map<Interview, Map<DetailCategory, AtomicLong>> detailsInUse =
                this.modelHandler.countDetailOccurrences(this.modelHandler.getModel().getInterviews());
        // sum up and ignore unused detail categories
        final Map<DetailCategory, AtomicLong> oldDetailOccurences = new HashMap<DetailCategory, AtomicLong>();
        for (final Map<DetailCategory, AtomicLong> singleInterviewDetails : detailsInUse.values()) {
            for (final Entry<DetailCategory, AtomicLong> singleOldDetailCount : singleInterviewDetails.entrySet()) {
                if (singleOldDetailCount.getKey().isSelectable() && singleOldDetailCount.getValue().get() > 0) {
                    final AtomicLong counter = oldDetailOccurences.get(singleOldDetailCount.getKey());
                    if (counter == null) {
                        oldDetailOccurences.put(singleOldDetailCount.getKey(), new AtomicLong(singleOldDetailCount.getValue().get()));
                    } else {
                        counter.addAndGet(singleOldDetailCount.getValue().get());
                    }
                }
            }
        }
        final Entry<MutableDetailCategoryModel, Map<DetailCategory, DetailCategory>> result = this.treeTable.toModelWithMapping();
        final Map<DetailCategory, DetailCategory> mapping = result.getValue();
        boolean manualMatchingRequired = false;
        for (final DetailCategory usedOldCategory : oldDetailOccurences.keySet()) {
            if (!mapping.containsKey(usedOldCategory)) {
                manualMatchingRequired = true;
                break;
            }
        }
        this.setCursor(null);
        final MutableDetailCategoryModel newModel = result.getKey();
        if (manualMatchingRequired) {
            new CategoryModelMatchSubDialog(oldDetailOccurences, newModel, mapping).setVisible(true);
        } else {
            this.applyChanges(newModel, mapping);
        }
    }

    /**
     * Replace the associated project's detail category model with the given state, while replacing existing scorings according to the specified
     * mapping.
     *
     * @param newModel
     *            new detail category model to apply to the associated project
     * @param mapping
     *            which old categories should be replaced by which new categories (assigned old categories without a mapping are being discarded)
     */
    void applyChanges(final MutableDetailCategoryModel newModel, final Map<DetailCategory, DetailCategory> mapping) {
        this.modelHandler.replaceCategoryModel(newModel, mapping);
        if (this.treeTable.isSetAsDefault()) {
            this.options.setDefaultDetailCategoryModel(newModel);
            this.options.persistChanges();
        }
        this.dispose();
    }

    /**
     * Sub dialog to input the explicit mapping from old to new detail categories, if the user made structural changes that would affect already
     * assigned detail categories.
     */
    private class CategoryModelMatchSubDialog extends JDialog {

        /**
         * The currently selected mapping for which old categories should be replaced by which new categories (assigned old categories without a
         * mapping would be discarded).
         */
        final Map<DetailCategory, DetailCategory> mapping;

        /**
         * Main constructor.
         *
         * @param oldDetailOccurences
         *            number of assignments of old detail categories that need replacement
         * @param newModel
         *            the new detail category model to match the old detail categories to
         * @param preset
         *            default mapping of old detail categories that are still existent (even if renamed) in the new detail category model
         */
        CategoryModelMatchSubDialog(final Map<DetailCategory, AtomicLong> oldDetailOccurences, final MutableDetailCategoryModel newModel,
                final Map<DetailCategory, DetailCategory> preset) {
            super(CategoryModelChangeDialog.this, AisMessage.PROJECT_CHANGE_CATEGORIES_MATCHING.get(), true);
            final JPanel contentPane = new JPanel(new GridBagLayout());
            final JLabel descriptionLabel =
                    new JLabel("<html>" + AisMessage.PROJECT_CHANGE_CATEGORIES_MATCHING_DESCRIPTION.get().replaceAll("[\n]", "<br/>"));
            descriptionLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            final GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.gridwidth = 3;
            contentPane.add(descriptionLabel, constraints);
            final Border headerBorder =
                    BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 10, 1, 10),
                            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
            final JLabel leftColumnHeader = new JLabel(AisMessage.PROJECT_CHANGE_CATEGORIES_OLD.get(), SwingConstants.CENTER);
            leftColumnHeader.setOpaque(true);
            leftColumnHeader.setBackground(Color.WHITE);
            leftColumnHeader.setBorder(headerBorder);
            constraints.gridy++;
            constraints.gridwidth = 1;
            constraints.weightx = 1;
            contentPane.add(leftColumnHeader, constraints);
            final JLabel midColumnHeader = new JLabel(AisMessage.PROJECT_CHANGE_CATEGORIES_AFFECTED_TOKENS.get(), SwingConstants.CENTER);
            midColumnHeader.setOpaque(true);
            midColumnHeader.setBackground(Color.WHITE);
            midColumnHeader.setBorder(headerBorder);
            constraints.gridx = 1;
            contentPane.add(midColumnHeader, constraints);
            final JLabel rightColumnHeader = new JLabel(AisMessage.PROJECT_CHANGE_CATEGORIES_NEW.get(), SwingConstants.CENTER);
            rightColumnHeader.setOpaque(true);
            rightColumnHeader.setBackground(Color.WHITE);
            rightColumnHeader.setBorder(headerBorder);
            constraints.gridx = 2;
            contentPane.add(rightColumnHeader, constraints);
            final List<DetailCategory> newSelectableCategories = newModel.provideSelectables();
            final String[] replacementOptions = new String[1 + newSelectableCategories.size()];
            replacementOptions[0] = "";
            for (int index = 0; index < newSelectableCategories.size(); index++) {
                replacementOptions[index + 1] = newSelectableCategories.get(index).getCode();
            }
            final Border leftColumnBorder =
                    BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 1, Color.BLACK),
                            BorderFactory.createEmptyBorder(2, 10, 2, 10));
            final Border midColumnBorder =
                    BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK),
                            BorderFactory.createEmptyBorder(2, 10, 2, 10));
            final Border rightColumnBorder =
                    BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Color.BLACK),
                            BorderFactory.createEmptyBorder(1, 10, 1, 10));
            this.mapping = new HashMap<DetailCategory, DetailCategory>();
            for (final Entry<DetailCategory, AtomicLong> singleEntry : oldDetailOccurences.entrySet()) {
                constraints.gridy++;
                constraints.gridx = 0;
                final JLabel oldCategoryLabel = new JLabel(singleEntry.getKey().getCode());
                oldCategoryLabel.setToolTipText(singleEntry.getKey().getName());
                oldCategoryLabel.setOpaque(true);
                oldCategoryLabel.setBackground(Color.WHITE);
                oldCategoryLabel.setBorder(leftColumnBorder);
                contentPane.add(oldCategoryLabel, constraints);
                constraints.gridx = 1;
                final JLabel affectedTokenLabel = new JLabel(String.valueOf(singleEntry.getValue().get()), SwingConstants.TRAILING);
                affectedTokenLabel.setOpaque(true);
                affectedTokenLabel.setBackground(Color.WHITE);
                affectedTokenLabel.setBorder(midColumnBorder);
                contentPane.add(affectedTokenLabel, constraints);
                final JComboBox replacementComboBox = new JComboBox(replacementOptions);
                if (preset.containsKey(singleEntry.getKey())) {
                    final DetailCategory newCategoryMatchedByDefault = preset.get(singleEntry.getKey());
                    replacementComboBox.setSelectedItem(newCategoryMatchedByDefault.getCode());
                    this.mapping.put(singleEntry.getKey(), newCategoryMatchedByDefault);
                }
                replacementComboBox.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent event) {
                        CategoryModelMatchSubDialog.this.replacementSelected(singleEntry.getKey(), replacementComboBox, newSelectableCategories);
                    }
                });
                constraints.gridx = 2;
                final JPanel comboBoxWrapper = new JPanel(new BorderLayout());
                comboBoxWrapper.setBackground(Color.WHITE);
                comboBoxWrapper.setBorder(rightColumnBorder);
                comboBoxWrapper.add(replacementComboBox);
                contentPane.add(comboBoxWrapper, constraints);
            }
            final JButton cancelButton = new JButton(Message.CANCEL.get());
            cancelButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent event) {
                    CategoryModelMatchSubDialog.this.dispose();
                }
            });
            constraints.fill = GridBagConstraints.NONE;
            constraints.insets = new Insets(5, 15, 5, 15);
            constraints.anchor = GridBagConstraints.BASELINE_LEADING;
            constraints.gridx = 0;
            constraints.gridy++;
            constraints.weightx = 0;
            contentPane.add(cancelButton, constraints);
            final JButton okButton = new JButton(Message.OK.get());
            okButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent event) {
                    CategoryModelMatchSubDialog.this.dispose();
                    CategoryModelChangeDialog.this.applyChanges(newModel, CategoryModelMatchSubDialog.this.mapping);
                }
            });
            constraints.anchor = GridBagConstraints.BASELINE_TRAILING;
            constraints.gridx = 2;
            contentPane.add(okButton, constraints);
            this.setContentPane(contentPane);
            this.pack();
            this.setResizable(false);
            ViewUtil.centerOnParent(this);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    cancelButton.requestFocusInWindow();
                }
            });
        }

        /**
         * Apply the selected mapping from the user interface to the internal member variable.
         *
         * @param oldCategory
         *            old detail category that needs to be replaced
         * @param replacementComboBox
         *            the user interface component offering selectable new detail categories
         * @param newSelectableCategories
         *            all selectable new detail categories to pick from
         */
        void replacementSelected(final DetailCategory oldCategory, final JComboBox replacementComboBox,
                final List<DetailCategory> newSelectableCategories) {
            final String newCategoryCode = (String) replacementComboBox.getSelectedItem();
            if (newCategoryCode.isEmpty()) {
                // selection has been cleared - remove from internal mapping
                this.mapping.remove(oldCategory);
            } else {
                // valid selection - update internal mapping
                for (final DetailCategory singleNewCategory : newSelectableCategories) {
                    if (newCategoryCode.equals(singleNewCategory.getCode())) {
                        this.mapping.put(oldCategory, singleNewCategory);
                        break;
                    }
                }
            }
        }
    }
}
