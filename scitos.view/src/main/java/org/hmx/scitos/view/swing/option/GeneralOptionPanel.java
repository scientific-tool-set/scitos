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

package org.hmx.scitos.view.swing.option;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.core.option.Option;
import org.hmx.scitos.core.option.OptionHandler;
import org.hmx.scitos.view.swing.util.Validation;

/**
 * Panel to be added in the <code>General</code> node of the {@link OptionView}, offering the choice of the LookAndFeel and the maximum number of
 * stored model states available for undo-actions.
 */
public final class GeneralOptionPanel extends AbstractSimpleOptionPanel<Option> {

    /** The actual dialog this is displayed in (in order to apply a selected LookAndFeel). */
    final JDialog dialog;
    /** The combo box to select the installed LookAndFeels. */
    final JComboBox lookAndFeelBox = new JComboBox();
    /** The input field to configure the maximum of model states (per tab view) that are stored and available for undo/redo. */
    final JTextField undoCountField = new JTextField();
    /** The combo box to select the Locale applied for the translation of the user interface. */
    final JComboBox localeBox = new JComboBox();

    /** The installed LookAndFeels to choose from. */
    Map<String, String> lookAndFeels;
    /** The available Locales (i.e. languages/countries) to choose from. */
    Map<String, Locale> locales;

    /**
     * Main constructor: create the general options panel.
     *
     * @param optionDialog
     *            the dialog containing this panel (in order to change its LookAndFeel)
     * @param viewParent
     *            parent {@link JFrame} to refresh the chosen LookAndFeel on
     */
    public GeneralOptionPanel(final JDialog optionDialog, final JFrame viewParent) {
        super(new GridBagLayout(), Message.PREFERENCES_GENERAL);
        this.dialog = optionDialog;
        this.init(viewParent);
    }

    /**
     * Initialize all components and their default values.
     *
     * @param viewParent
     *            parent {@link JFrame} to refresh the chosen LookAndFeel
     */
    private void init(final JFrame viewParent) {
        final Box contentBox = new Box(BoxLayout.PAGE_AXIS);
        // SwingLookAndFeel
        contentBox.add(this.initLookAndFeelPanel(viewParent));
        // Undo Limit
        final JPanel undoCountPanel = new JPanel(new GridBagLayout());
        undoCountPanel.setBorder(BorderFactory.createTitledBorder(Message.PREFERENCES_GENERAL_UNDO.get()));
        this.undoCountField.setDocument(new Validation(3, "[^0-9]"));
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.insets = new Insets(2, 5, 2, 5);
        undoCountPanel.add(this.undoCountField, constraints);
        final String undoCountValue;
        if (this.containsChosenSettingKey(Option.UNDO_LIMIT)) {
            undoCountValue = this.getChosenSetting(Option.UNDO_LIMIT);
        } else {
            undoCountValue = Option.UNDO_LIMIT.getValue();
        }
        this.undoCountField.setText(undoCountValue);
        contentBox.add(undoCountPanel);
        // Translation
        contentBox.add(this.initTranslationPanel());

        this.add(contentBox, AbstractOptionPanel.HORIZONTAL_SPAN);
        final GridBagConstraints spacing = new GridBagConstraints();
        spacing.fill = GridBagConstraints.VERTICAL;
        spacing.weighty = 1;
        spacing.gridy = 1;
        this.add(new JPanel(), spacing);
        this.setMinimumSize(this.getPreferredSize());
    }

    /**
     * Initialize the LookAndFeel selection.
     *
     * @param viewParent
     *            the main client window to apply the selected LookAndFeel to
     * @return created panel containing the LookAndFeel selection
     */
    private JPanel initLookAndFeelPanel(final JFrame viewParent) {
        final JPanel lookAndFeelPanel = new JPanel(new GridBagLayout());
        lookAndFeelPanel.setBorder(BorderFactory.createTitledBorder(Message.PREFERENCES_GENERAL_LOOK_AND_FEEL.get()));
        this.lookAndFeelBox.setEditable(false);
        // get all available look and feels
        final LookAndFeelInfo[] installedUIs = UIManager.getInstalledLookAndFeels();
        this.lookAndFeels = new TreeMap<String, String>();
        for (final LookAndFeelInfo singleLaF : installedUIs) {
            final String shortName = singleLaF.getName();
            if (!"Nimbus".equals(shortName)) {
                // mapping the display name to the full class name of the look and feel
                this.lookAndFeels.put(shortName, singleLaF.getClassName());
            }
        }
        for (final String shortName : this.lookAndFeels.keySet()) {
            // adding the short name of the look and feel in the combo box
            this.lookAndFeelBox.addItem(shortName);
        }
        String selected;
        if (this.containsChosenSettingKey(Option.LOOK_AND_FEEL)) {
            selected = this.getChosenSetting(Option.LOOK_AND_FEEL);
        } else {
            selected = Option.LOOK_AND_FEEL.getValue();
            if (selected == null || selected.isEmpty()) {
                // select the current displayed look and feel
                selected = UIManager.getLookAndFeel().getClass().getName();
            }
        }
        for (final Entry<String, String> singleLookAndFeel : this.lookAndFeels.entrySet()) {
            if (singleLookAndFeel.getValue().equals(selected)) {
                this.lookAndFeelBox.setSelectedItem(singleLookAndFeel.getKey());
                break;
            }
        }
        this.lookAndFeelBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                try {
                    UIManager.setLookAndFeel(GeneralOptionPanel.this.lookAndFeels.get(GeneralOptionPanel.this.lookAndFeelBox.getSelectedIndex()));
                    SwingUtilities.updateComponentTreeUI(GeneralOptionPanel.this.dialog);
                    SwingUtilities.updateComponentTreeUI(viewParent);
                    viewParent.validate();
                } catch (final ClassNotFoundException cnfex) {
                    // ignore
                } catch (final InstantiationException iex) {
                    // ignore
                } catch (final IllegalAccessException iaex) {
                    // ignore
                } catch (final UnsupportedLookAndFeelException ulafex) {
                    // ignore
                }
            }
        });
        lookAndFeelPanel.add(this.lookAndFeelBox, AbstractOptionPanel.DEFAULT_INSETS);
        lookAndFeelPanel.add(new JPanel(), AbstractOptionPanel.HORIZONTAL_SPAN);
        return lookAndFeelPanel;
    }

    /**
     * Initialize the Translation selection.
     *
     * @return created panel containing the Translation selection
     */
    private JPanel initTranslationPanel() {
        final JPanel translationPanel = new JPanel(new GridBagLayout());
        translationPanel.setBorder(BorderFactory.createTitledBorder(Message.PREFERENCES_GENERAL_TRANSLATION.get()));
        this.localeBox.setEditable(false);
        // get all available look and feels
        final List<Locale> availableLocales = Message.getAvailableLocales();
        // include default translation: English language
        availableLocales.add(new Locale("en"));
        this.locales = new TreeMap<String, Locale>();
        for (final Locale singleLocale : availableLocales) {
            // mapping the display name to the actual Locale
            this.locales.put(singleLocale.getDisplayName(Option.TRANSLATION.getValueAsLocale()), singleLocale);
        }
        this.localeBox.addItem(Message.PREFERENCES_GENERAL_TRANSLATION_SYSTEM_DEFAULT.get());
        for (final String displayName : this.locales.keySet()) {
            // adding the display name of the Locale in the combo box
            this.localeBox.addItem(displayName);
        }
        String selected;
        if (this.containsChosenSettingKey(Option.TRANSLATION)) {
            selected = this.getChosenSetting(Option.TRANSLATION);
        } else {
            selected = Option.TRANSLATION.getValue();
        }
        // select System Default by default
        this.localeBox.setSelectedIndex(0);
        for (final Entry<String, Locale> singleLocale : this.locales.entrySet()) {
            if (singleLocale.getValue().toString().equals(selected)) {
                // selection value is a valid Locale, select it
                this.localeBox.setSelectedItem(singleLocale.getKey());
                break;
            }
        }
        translationPanel.add(this.localeBox, AbstractOptionPanel.DEFAULT_INSETS);
        final GridBagConstraints constraints = (GridBagConstraints) AbstractOptionPanel.DEFAULT_INSETS.clone();
        constraints.gridx = 1;
        translationPanel.add(new JLabel(Message.PREFERENCES_RESTART_REQUIRED.get()), constraints);
        final GridBagConstraints spacingConstraints = (GridBagConstraints) AbstractOptionPanel.HORIZONTAL_SPAN.clone();
        spacingConstraints.gridx = 2;
        translationPanel.add(new JPanel(), spacingConstraints);
        return translationPanel;
    }

    /** Store the chosen settings in the associated map. */
    @Override
    protected void validateInput() {
        // transfer settings in the chosenSettingsMap
        this.addChosenSetting(Option.LOOK_AND_FEEL, this.lookAndFeels.get(this.lookAndFeelBox.getSelectedItem()));
        this.addChosenSetting(Option.UNDO_LIMIT, this.undoCountField.getText());
        final Locale translation = this.locales.get(this.localeBox.getSelectedItem());
        this.addChosenSetting(Option.TRANSLATION, translation == null ? null : translation.toString());
    }

    @Override
    public boolean areChosenSettingsValid() {
        // no invalid selection possible
        return true;
    }

    @Override
    public void persistChanges() {
        OptionHandler.getInstance(Option.class).persistChanges();
    }
}
