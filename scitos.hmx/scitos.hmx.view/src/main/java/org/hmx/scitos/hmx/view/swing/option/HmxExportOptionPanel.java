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

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.hmx.scitos.core.option.OptionHandler;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.core.option.HmxExportOption;
import org.hmx.scitos.view.swing.option.AbstractOptionPanel;
import org.hmx.scitos.view.swing.option.AbstractSimpleOptionPanel;
import org.hmx.scitos.view.swing.option.OptionView;

/**
 * Panel to be added in the {@code HmX - General} node of the {@link OptionView}.
 */
public final class HmxExportOptionPanel extends AbstractSimpleOptionPanel<HmxExportOption> {

    final JPanel arrowColorSample = new JPanel();
    final JPanel relationColorSample = new JPanel();
    final JPanel originFontColorSample = new JPanel();
    final JPanel translationFontColorSample = new JPanel();
    final JPanel labelFontColorSample = new JPanel();
    final JPanel roleFontColorSample = new JPanel();
    final JPanel plainFunctionFontColorSample = new JPanel();
    final JPanel boldFunctionFontColorSample = new JPanel();
    final JPanel boldItalicFunctionFontColorSample = new JPanel();
    final JPanel italicFunctionFontColorSample = new JPanel();
    final JPanel propositionBorderSample = new JPanel();
    final JPanel propositionBackgroundSample = new JPanel();
    final FontChooser fontChooser = new FontChooser();

    /**
     * Main constructor: create the general options panel.
     */
    public HmxExportOptionPanel() {
        super(new GridBagLayout(), HmxMessage.PREFERENCES_EXPORT);
        this.init();
    }

    /**
     * Initialize all components and their default values.
     */
    private void init() {
        final Box contentBox = new Box(BoxLayout.PAGE_AXIS);
        // color options
        contentBox.add(this.initElementColorPanel());
        contentBox.add(this.initFontColorPanel());
        // SynAnalysis.IndentationWidth
        // contentBox.add(initIndentationPanel());
        // Font
        contentBox.add(this.initFontPanel());
        this.add(contentBox, AbstractOptionPanel.HORIZONTAL_SPAN);
        final GridBagConstraints spacing = new GridBagConstraints();
        spacing.fill = GridBagConstraints.VERTICAL;
        spacing.weighty = 1;
        spacing.gridy = 1;
        this.add(new JPanel(), spacing);
        this.setMinimumSize(this.getPreferredSize());
    }

    private JPanel initElementColorPanel() {
        final JPanel elementColorGroup = new JPanel(new GridBagLayout());
        elementColorGroup.setBorder(BorderFactory.createTitledBorder(HmxMessage.PREFERENCES_EXPORT_ELEMENTCOLOR.get()));
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        // ArrowColor.RGB
        elementColorGroup.add(
                this.initColorPanel(this.arrowColorSample, HmxMessage.PREFERENCES_GENERAL_ARROW_COLOR, HmxExportOption.ARROW_COLOR, false),
                constraints);
        constraints.gridy++;
        // Proposition Border
        elementColorGroup.add(this.initColorPanel(this.propositionBorderSample, HmxMessage.PREFERENCES_EXPORT_PROPOSITION_BORDER,
                HmxExportOption.PROPOSITION_COLOR_BORDER, true), constraints);
        constraints.gridx = 1;
        constraints.gridy = 0;
        // RelationColor.RGB
        elementColorGroup.add(
                this.initColorPanel(this.relationColorSample, HmxMessage.PREFERENCES_GENERAL_RELATION_COLOR, HmxExportOption.RELATION_COLOR, false),
                constraints);
        constraints.gridy++;
        elementColorGroup.add(this.initColorPanel(this.propositionBackgroundSample, HmxMessage.PREFERENCES_EXPORT_PROPOSITION_BACKGROUND,
                HmxExportOption.PROPOSITION_COLOR_BACKGROUND, true), constraints);
        return elementColorGroup;
    }

    private JPanel initFontColorPanel() {
        final JPanel fontColorGroup = new JPanel(new GridBagLayout());
        fontColorGroup.setBorder(BorderFactory.createTitledBorder(HmxMessage.PREFERENCES_EXPORT_FONTCOLOR.get()));
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        fontColorGroup.add(this.initColorPanel(this.originFontColorSample, HmxMessage.PREFERENCES_EXPORT_ORIGINTEXT_COLOR,
                HmxExportOption.FONTCOLOR_ORIGINTEXT, false), constraints);
        constraints.gridy++;
        fontColorGroup.add(
                this.initColorPanel(this.labelFontColorSample, HmxMessage.PREFERENCES_EXPORT_LABEL_COLOR, HmxExportOption.FONTCOLOR_LABEL, false),
                constraints);
        constraints.gridy++;
        fontColorGroup.add(this.initColorPanel(this.plainFunctionFontColorSample, HmxMessage.PREFERENCES_EXPORT_SYNFUNCTION_PLAIN_COLOR,
                HmxExportOption.FONTCOLOR_SYNFUNCTION_PLAIN, false), constraints);
        constraints.gridy++;
        fontColorGroup.add(this.initColorPanel(this.italicFunctionFontColorSample, HmxMessage.PREFERENCES_EXPORT_SYNFUNCTION_ITALIC_COLOR,
                HmxExportOption.FONTCOLOR_SYNFUNCTION_ITALIC, false), constraints);
        constraints.gridx = 1;
        constraints.gridy = 0;
        fontColorGroup.add(this.initColorPanel(this.translationFontColorSample, HmxMessage.PREFERENCES_EXPORT_TRANSLATION_COLOR,
                HmxExportOption.FONTCOLOR_TRANSLATION, false), constraints);
        constraints.gridy++;
        fontColorGroup.add(this.initColorPanel(this.roleFontColorSample, HmxMessage.PREFERENCES_EXPORT_SEMROLE_COLOR,
                HmxExportOption.FONTCOLOR_SEMROLE, false), constraints);
        constraints.gridy++;
        fontColorGroup.add(this.initColorPanel(this.boldFunctionFontColorSample, HmxMessage.PREFERENCES_EXPORT_SYNFUNCTION_BOLD_COLOR,
                HmxExportOption.FONTCOLOR_SYNFUNCTION_BOLD, false), constraints);
        constraints.gridy++;
        fontColorGroup.add(this.initColorPanel(this.boldItalicFunctionFontColorSample, HmxMessage.PREFERENCES_EXPORT_SYNFUNCTION_BOLDITALIC_COLOR,
                HmxExportOption.FONTCOLOR_SYNFUNCTION_BOLDTALIC, false), constraints);
        return fontColorGroup;
    }

    private JPanel initFontPanel() {
        final JPanel fontPanel = new JPanel(new GridBagLayout());
        fontPanel.setBorder(BorderFactory.createTitledBorder(HmxMessage.PREFERENCES_EXPORT_FONT.get()));
        fontPanel.add(this.fontChooser, AbstractOptionPanel.HORIZONTAL_SPAN);
        return fontPanel;
    }

    @Override
    protected void validateInput() {
        this.addChosenSetting(HmxExportOption.ARROW_COLOR, this.arrowColorSample.getBackground());
        this.addChosenSetting(HmxExportOption.RELATION_COLOR, this.relationColorSample.getBackground());
        this.addChosenSetting(HmxExportOption.PROPOSITION_COLOR_BORDER, this.propositionBorderSample.getBackground());
        this.addChosenSetting(HmxExportOption.PROPOSITION_COLOR_BACKGROUND, this.propositionBackgroundSample.getBackground());
        this.addChosenSetting(HmxExportOption.FONTCOLOR_ORIGINTEXT, this.originFontColorSample.getBackground());
        this.addChosenSetting(HmxExportOption.FONTCOLOR_TRANSLATION, this.translationFontColorSample.getBackground());
        this.addChosenSetting(HmxExportOption.FONTCOLOR_LABEL, this.labelFontColorSample.getBackground());
        this.addChosenSetting(HmxExportOption.FONTCOLOR_SEMROLE, this.roleFontColorSample.getBackground());
        this.addChosenSetting(HmxExportOption.FONTCOLOR_SYNFUNCTION_PLAIN, this.plainFunctionFontColorSample.getBackground());
        this.addChosenSetting(HmxExportOption.FONTCOLOR_SYNFUNCTION_BOLD, this.boldFunctionFontColorSample.getBackground());
        this.addChosenSetting(HmxExportOption.FONTCOLOR_SYNFUNCTION_ITALIC, this.italicFunctionFontColorSample.getBackground());
        this.addChosenSetting(HmxExportOption.FONTCOLOR_SYNFUNCTION_BOLDTALIC, this.boldItalicFunctionFontColorSample.getBackground());
        final Font font = this.fontChooser.getSelection();
        this.addChosenSetting(HmxExportOption.NONORIGINTEXT_FONT_TYPE, font.getFamily());
        this.addChosenSetting(HmxExportOption.NONORIGINTEXT_FONT_SIZE, font.getSize());
    }

    @Override
    public boolean areChosenSettingsValid() {
        // no invalid selection allowed
        return true;
    }

    @Override
    public void persistChanges() {
        OptionHandler.getInstance(HmxExportOption.class).persistChanges();
    }
}
