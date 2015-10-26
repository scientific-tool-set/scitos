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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.hmx.scitos.core.i18n.ILocalizableMessage;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.core.option.IOptionSetting;
import org.hmx.scitos.core.util.ConversionUtil;
import org.hmx.scitos.view.swing.util.SplitFrame;

/**
 * Abstract panel in the application's preferences dialog, that handles a number of simple settings, represented in an Enum.
 *
 * @param <S>
 *            the managed enumerated settings
 */
public abstract class AbstractSimpleOptionPanel<S extends Enum<? extends IOptionSetting> & IOptionSetting> extends AbstractOptionPanel {

    /**
     * All settings chosen in this instance, which will be saved when the {@link SplitFrame} is closed by the {@code OK}-button.
     */
    private final Map<S, String> chosenSettings = new HashMap<S, String>();

    /**
     * Main constructor.
     *
     * @param layout
     *            layout manager to apply
     * @param title
     *            message to be displayed in the OptionView's SplitFrame's tree as node label
     */
    protected AbstractSimpleOptionPanel(final LayoutManager layout, final ILocalizableMessage title) {
        super(layout, title);
    }

    /**
     * Create a titled setting component for a {@link Color} value, including the given {@code colorSample}.
     *
     * @param colorSample
     *            the panel to display any chosen {@link Color} value on
     * @param title
     *            the title text to apply
     * @param setting
     *            the preferences entry being represented
     * @param allowTransparent
     *            if the given preferences entry allows transparency
     * @return created component to be inserted into a surrounding parent
     */
    protected final JPanel initColorPanel(final JPanel colorSample, final ILocalizableMessage title, final S setting,
            final boolean allowTransparent) {
        final JPanel groupPanel = new JPanel(new GridBagLayout());
        groupPanel.setBorder(BorderFactory.createTitledBorder(title.get()));
        // create a panel for showing the current selected color
        colorSample.setPreferredSize(new Dimension(50, allowTransparent ? 50 : 20));
        colorSample.setBorder(null);
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridheight = 2;
        constraints.insets = AbstractOptionPanel.DEFAULT_INSETS.insets;
        groupPanel.add(colorSample, constraints);
        constraints.insets = new Insets(0, 0, 0, 0);
        final JToggleButton transparentButton;
        if (allowTransparent) {
            constraints.gridheight = 1;
            constraints.gridy = 1;
            transparentButton = new JToggleButton(Message.PREFERENCES_SETTING_TRANSPARENT_COLOR.get());
            transparentButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent event) {
                    colorSample.setBackground(null);
                    transparentButton.setSelected(true);
                }
            });
            groupPanel.add(transparentButton, constraints);
            constraints.gridy = 0;
        } else {
            transparentButton = null;
        }
        final Color colorSetting = setting.getValueAsColor();
        colorSample.setBackground(colorSetting);
        if (transparentButton != null) {
            transparentButton.setSelected(colorSetting == null);
        }
        // create a button for choosing the color
        final JButton changeColorButton = new JButton(Message.PREFERENCES_SETTING_CHANGE_COLOR.get());
        final JColorChooser colorChooser = new JColorChooser();
        changeColorButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                colorChooser.setColor(colorSample.getBackground());
                JColorChooser.createDialog(AbstractSimpleOptionPanel.this, title.get(), true, colorChooser, new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent okEvent) {
                        colorSample.setBackground(colorChooser.getColor());
                        if (transparentButton != null) {
                            transparentButton.setSelected(false);
                        }

                    }
                }, null).setVisible(true);
            }
        });
        constraints.gridx = 1;
        groupPanel.add(changeColorButton, constraints);
        constraints.gridx = 2;
        constraints.gridheight = 2;
        constraints.weightx = 1;
        groupPanel.add(new JPanel(), constraints);
        return groupPanel;
    }

    /**
     * Remember the chosen setting value for the specified key.
     *
     * @param key
     *            which option has been defined
     * @param value
     *            chosen value for this setting
     */
    protected final void addChosenSetting(final S key, final String value) {
        this.chosenSettings.put(key, value);
    }

    /**
     * Remember the chosen {@link Color} setting value for the specified key.
     *
     * @param key
     *            which option has been defined
     * @param value
     *            chosen {@link Color} value for this setting
     */
    protected final void addChosenSetting(final S key, final Color value) {
        this.addChosenSetting(key, ConversionUtil.toString(value));
    }

    /**
     * Remember the chosen numeric setting value for the specified key.
     *
     * @param key
     *            which option has been defined
     * @param value
     *            chosen numeric value for this setting
     */
    protected final void addChosenSetting(final S key, final int value) {
        this.addChosenSetting(key, String.valueOf(value));
    }

    /**
     * Check if there is already a setting value stored for the specified key.
     *
     * @param key
     *            option key to check for stored option value
     * @return if the key is stored
     */
    protected final boolean containsChosenSettingKey(final S key) {
        return this.chosenSettings.containsKey(key);
    }

    /**
     * Request the stored option value for the specified key.
     *
     * @param key
     *            option key to get the stored option value for
     * @return setting value for the specified key
     */
    protected final String getChosenSetting(final S key) {
        return this.chosenSettings.get(key);
    }

    @Override
    public void submitChosenSettings() {
        // store all chosen settings in the options file
        for (final S singleKey : this.chosenSettings.keySet()) {
            singleKey.setValue(this.chosenSettings.get(singleKey));
        }
        this.persistChanges();
    }

    /** Persist all selected setting values to a file in order to be able to load them at the next application start. */
    protected abstract void persistChanges();
}
