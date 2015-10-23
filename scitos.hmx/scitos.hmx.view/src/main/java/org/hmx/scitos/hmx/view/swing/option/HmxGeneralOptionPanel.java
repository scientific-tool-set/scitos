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

package org.hmx.scitos.hmx.view.swing.option;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.hmx.scitos.core.option.OptionHandler;
import org.hmx.scitos.core.util.ConversionUtil;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.core.option.HmxGeneralOption;
import org.hmx.scitos.view.swing.option.AbstractOptionPanel;
import org.hmx.scitos.view.swing.option.AbstractSimpleOptionPanel;
import org.hmx.scitos.view.swing.option.OptionView;
import org.hmx.scitos.view.swing.util.Validation;

/**
 * Panel to be added in the {@code HmX - General} node of the {@link OptionView}.
 */
public final class HmxGeneralOptionPanel extends AbstractSimpleOptionPanel<HmxGeneralOption> {

    final JPanel arrowColorSample = new JPanel();
    final JPanel relationColorSample = new JPanel();
    final JPanel commentedBorderColorSample = new JPanel();
    final JSlider slider = new JSlider();
    private final JCheckBox showSettings = new JCheckBox();
    private final JTextField authorField = new JTextField();

    /**
     * Main constructor: create the general options panel.
     */
    public HmxGeneralOptionPanel() {
        super(new GridBagLayout(), HmxMessage.PREFERENCES_GENERAL);
        this.init();
    }

    /**
     * Initialize all components and their default values.
     */
    private void init() {
        final Box contentBox = new Box(BoxLayout.PAGE_AXIS);
        contentBox
                .add(this.initColorPanel(this.arrowColorSample, HmxMessage.PREFERENCES_GENERAL_ARROW_COLOR, HmxGeneralOption.ARROW_COLOR, false));
        contentBox.add(this.initColorPanel(this.relationColorSample, HmxMessage.PREFERENCES_GENERAL_RELATION_COLOR,
                HmxGeneralOption.RELATION_COLOR, false));
        contentBox.add(this.initColorPanel(this.commentedBorderColorSample, HmxMessage.PREFERENCES_GENERAL_COMMENTED_BORDER_COLOR,
                HmxGeneralOption.COMMENTED_BORDER_COLOR, false));
        contentBox.add(this.initIndentationPanel());
        final JPanel showSettingsPanel = new JPanel(new GridBagLayout());
        showSettingsPanel.setBorder(BorderFactory.createTitledBorder(""));
        this.showSettings.setText(HmxMessage.PREFERENCES_GENERAL_SHOW_INPUT_SETTINGS.get());
        this.showSettings.setSelected(HmxGeneralOption.SHOW_SETTINGS.getValueAsBoolean());
        showSettingsPanel.add(this.showSettings, AbstractOptionPanel.DEFAULT_INSETS);
        showSettingsPanel.add(new JPanel(), AbstractOptionPanel.HORIZONTAL_SPAN);
        contentBox.add(showSettingsPanel);
        // HmxGeneralOption.AUTHOR
        final JPanel authorPanel = new JPanel(new GridBagLayout());
        this.authorField.setDocument(new Validation(100));
        authorPanel.setBorder(BorderFactory.createTitledBorder(HmxMessage.PREFERENCES_GENERAL_AUTHOR.get()));
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.insets = new Insets(2, 5, 2, 5);
        authorPanel.add(this.authorField, constraints);
        contentBox.add(authorPanel);

        this.add(contentBox, AbstractOptionPanel.HORIZONTAL_SPAN);
        final GridBagConstraints spacing = new GridBagConstraints();
        spacing.fill = GridBagConstraints.VERTICAL;
        spacing.weighty = 1;
        spacing.gridy = 1;
        this.add(new JPanel(), spacing);
        this.setMinimumSize(this.getPreferredSize());
    }

    /**
     * Create a panel containing the slider for choosing the width of indentations in the syntactical analysis view and an associated component
     * showing the actual extent of that value.
     *
     * @return created panel
     */
    private JPanel initIndentationPanel() {
        final JPanel indentationPanel = new JPanel(new GridBagLayout());
        indentationPanel.setBorder(BorderFactory.createTitledBorder(HmxMessage.PREFERENCES_GENERAL_INDENTATION.get()));
        this.slider.setMinimum(30);
        this.slider.setMaximum(200);
        final int width = HmxGeneralOption.INDENTATION_WIDTH.getValueAsInteger();
        this.slider.setValue(width);
        // create a panel for showing the current selected indentation width
        final JPanel sampleBar = new JPanel();
        sampleBar.setBackground(Color.WHITE);
        sampleBar.setBorder(BorderFactory.createEtchedBorder());
        final Dimension initialSize = new Dimension(width, 24);
        sampleBar.setPreferredSize(initialSize);
        sampleBar.setSize(initialSize);
        indentationPanel.add(this.slider, AbstractOptionPanel.DEFAULT_INSETS);
        indentationPanel.add(sampleBar, AbstractOptionPanel.DEFAULT_INSETS);
        this.slider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent event) {
                // adjust the size of the sample panel
                final Dimension size = new Dimension(HmxGeneralOptionPanel.this.slider.getValue(), 24);
                sampleBar.setPreferredSize(size);
                sampleBar.setSize(size);
            }
        });
        indentationPanel.add(new JPanel(), AbstractOptionPanel.HORIZONTAL_SPAN);
        return indentationPanel;
    }

    @Override
    protected void validateInput() {
        this.addChosenSetting(HmxGeneralOption.ARROW_COLOR, ConversionUtil.toString(this.arrowColorSample.getBackground()));
        this.addChosenSetting(HmxGeneralOption.RELATION_COLOR, ConversionUtil.toString(this.relationColorSample.getBackground()));
        this.addChosenSetting(HmxGeneralOption.COMMENTED_BORDER_COLOR, ConversionUtil.toString(this.commentedBorderColorSample.getBackground()));
        this.addChosenSetting(HmxGeneralOption.INDENTATION_WIDTH, Integer.toString(this.slider.getValue()));
        this.addChosenSetting(HmxGeneralOption.SHOW_SETTINGS, String.valueOf(this.showSettings.isSelected()));
        this.addChosenSetting(HmxGeneralOption.AUTHOR, this.authorField.getText());
    }

    @Override
    public boolean areChosenSettingsValid() {
        // no invalid selection possible
        return true;
    }

    @Override
    public void persistChanges() {
        OptionHandler.getInstance(HmxGeneralOption.class).persistChanges();
    }
}
