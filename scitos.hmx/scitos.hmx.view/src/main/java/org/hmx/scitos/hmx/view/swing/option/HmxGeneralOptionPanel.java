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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.java.dev.designgridlayout.DesignGridLayout;

import org.hmx.scitos.core.option.OptionHandler;
import org.hmx.scitos.core.util.ConversionUtil;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.core.option.HmxGeneralOption;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.view.swing.option.AbstractOptionPanel;
import org.hmx.scitos.view.swing.option.AbstractSimpleOptionPanel;
import org.hmx.scitos.view.swing.option.OptionView;
import org.hmx.scitos.view.swing.util.Validation;

/**
 * Panel to be added in the {@code HmX - General} node of the {@link OptionView}.
 */
public final class HmxGeneralOptionPanel extends AbstractSimpleOptionPanel<HmxGeneralOption> {

    /**
     * Sample panel displaying the currently selected value for {@link HmxGeneralOption#ARROW_COLOR}.
     */
    final JPanel arrowColorSample = new JPanel();
    /**
     * Sample panel displaying the currently selected value for {@link HmxGeneralOption#RELATION_COLOR}.
     */
    final JPanel relationColorSample = new JPanel();
    /**
     * Sample panel displaying the currently selected value for {@link HmxGeneralOption#COMMENTED_BORDER_COLOR}.
     */
    final JPanel commentedBorderColorSample = new JPanel();
    /**
     * Checkbox to determine whether the proposition labels should be visible by default in the analysis views
     * ({@link HmxGeneralOption#SHOW_PROPOSITION_LABELS}).
     */
    private final JCheckBox showLabels = new JCheckBox(HmxMessage.PREFERENCES_GENERAL_PROPOSITIONS_SHOW_LABELS.get());
    /**
     * Checkbox to determine whether the proposition translations should be visible by default in the analysis views
     * ({@link HmxGeneralOption#SHOW_PROPOSITION_TRANSLATIONs}).
     */
    private final JCheckBox showTranslations = new JCheckBox(HmxMessage.PREFERENCES_GENERAL_PROPOSITIONS_SHOW_TRANSLATIONS.get());
    /**
     * Checkbox to determine if the settings area should be visible by default for new projects ({@link HmxGeneralOption#SHOW_SETTINGS}).
     */
    private final JCheckBox showSettings = new JCheckBox(HmxMessage.PREFERENCES_GENERAL_INPUT_SHOW_SETTINGS.get());
    /**
     * Input field for the default value for a new project's author ({@link HmxGeneralOption#AUTHOR}).
     */
    private final JTextField authorField = new JTextField(new Validation(100), null, 0);
    /**
     * Selection component for the width of a single indentation of a {@link Proposition} ({@link HmxGeneralOption#INDENTATION_WIDTH}).
     */
    final JSlider indentationSizeSlider = new JSlider(30, 200);

    /**
     * Constructor.
     */
    public HmxGeneralOptionPanel() {
        super(new GridBagLayout(), HmxMessage.PREFERENCES_GENERAL);
        final Box contentBox = new Box(BoxLayout.PAGE_AXIS);
        final JPanel generalOptionsGroup = new JPanel();
        generalOptionsGroup.setBorder(BorderFactory.createTitledBorder(HmxMessage.PREFERENCES_GENERAL.get()));
        final DesignGridLayout generalOptionsLayout = new DesignGridLayout(generalOptionsGroup);
        generalOptionsLayout.row().grid(new JLabel(HmxMessage.PREFERENCES_GENERAL_ARROW_COLOR.get()))
                .add(this.initColorPanel(this.arrowColorSample, HmxMessage.PREFERENCES_GENERAL_ARROW_COLOR, HmxGeneralOption.ARROW_COLOR, false));
        generalOptionsLayout
                .row()
                .grid(new JLabel(HmxMessage.PREFERENCES_GENERAL_RELATION_COLOR.get()))
                .add(this.initColorPanel(this.relationColorSample, HmxMessage.PREFERENCES_GENERAL_RELATION_COLOR, HmxGeneralOption.RELATION_COLOR,
                        false));
        generalOptionsLayout
                .row()
                .grid(new JLabel(HmxMessage.PREFERENCES_GENERAL_COMMENTED_BORDER_COLOR.get()))
                .add(this.initColorPanel(this.commentedBorderColorSample, HmxMessage.PREFERENCES_GENERAL_COMMENTED_BORDER_COLOR,
                        HmxGeneralOption.COMMENTED_BORDER_COLOR, false));
        this.showLabels.setSelected(HmxGeneralOption.SHOW_PROPOSITION_LABELS.getValueAsBoolean());
        this.showTranslations.setSelected(HmxGeneralOption.SHOW_PROPOSITION_TRANSLATIONS.getValueAsBoolean());
        this.showSettings.setSelected(HmxGeneralOption.SHOW_SETTINGS.getValueAsBoolean());
        generalOptionsLayout.row().grid(new JLabel(HmxMessage.PREFERENCES_GENERAL_PROPOSITIONS.get())).add(this.showLabels);
        generalOptionsLayout.row().grid().add(this.showTranslations);
        generalOptionsLayout.row().grid(new JLabel(HmxMessage.PREFERENCES_GENERAL_INPUT.get())).add(this.showSettings);
        generalOptionsLayout.row().grid(new JLabel(HmxMessage.PREFERENCES_GENERAL_AUTHOR.get())).add(this.authorField);
        this.indentationSizeSlider.setPreferredSize(new Dimension(this.indentationSizeSlider.getMaximum(), this.indentationSizeSlider
                .getPreferredSize().height));
        generalOptionsLayout.row().grid(new JLabel(HmxMessage.PREFERENCES_GENERAL_INDENTATION.get())).addMulti(this.indentationSizeSlider);
        generalOptionsLayout.row().grid().add(this.initIndentationPanel());

        contentBox.add(generalOptionsGroup);
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
        final int width = HmxGeneralOption.INDENTATION_WIDTH.getValueAsInteger();
        this.indentationSizeSlider.setValue(width);
        // create a panel for showing the current selected indentation width
        final JPanel sampleBar = new JPanel();
        sampleBar.setBackground(Color.WHITE);
        sampleBar.setBorder(BorderFactory.createEtchedBorder());
        final Dimension initialSize = new Dimension(width, 24);
        sampleBar.setPreferredSize(initialSize);
        sampleBar.setSize(initialSize);
        indentationPanel.add(sampleBar, AbstractOptionPanel.DEFAULT_INSETS);
        this.indentationSizeSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent event) {
                // adjust the size of the sample panel
                final Dimension size = new Dimension(HmxGeneralOptionPanel.this.indentationSizeSlider.getValue(), 24);
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
        this.addChosenSetting(HmxGeneralOption.SHOW_PROPOSITION_LABELS, String.valueOf(this.showLabels.isSelected()));
        this.addChosenSetting(HmxGeneralOption.SHOW_PROPOSITION_TRANSLATIONS, String.valueOf(this.showTranslations.isSelected()));
        this.addChosenSetting(HmxGeneralOption.SHOW_SETTINGS, String.valueOf(this.showSettings.isSelected()));
        this.addChosenSetting(HmxGeneralOption.AUTHOR, this.authorField.getText());
        this.addChosenSetting(HmxGeneralOption.INDENTATION_WIDTH, Integer.toString(this.indentationSizeSlider.getValue()));
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
