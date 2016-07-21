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

package org.hmx.scitos.hmx.core.option;

import java.awt.Color;

import org.hmx.scitos.core.option.IOptionSetting;
import org.hmx.scitos.core.option.OptionHandler;
import org.hmx.scitos.core.util.ConversionUtil;

/**
 * Collection of general user preferences for the HermeneutiX module.
 */
public enum HmxExportOption implements IOptionSetting {
    /** user setting: color of arrows (between splitted propositions) in exports. */
    ARROW_COLOR("Export.Arrow.Color", ConversionUtil.toString(Color.BLUE)),
    /** user setting: border color of propositions in exports. */
    PROPOSITION_COLOR_BORDER("Export.Proposition.BorderColor", ConversionUtil.toString(null)),
    /** user setting: background color of propositions in exports. */
    PROPOSITION_COLOR_BACKGROUND("Export.Proposition.BackgroundColor", ConversionUtil.toString(new Color(240, 240, 240))),
    /** user setting: color of semantical relations in exports. */
    RELATION_COLOR("Export.Relation.Color", ConversionUtil.toString(Color.RED)),
    /** user setting: color of origin texts in exports. */
    FONTCOLOR_ORIGINTEXT("Export.OriginText.FontColor", ConversionUtil.toString(Color.BLACK)),
    /** user setting: color of translation texts in exports. */
    FONTCOLOR_TRANSLATION("Export.Translation.FontColor", ConversionUtil.toString(Color.BLACK)),
    /** user setting: color of label texts in exports. */
    FONTCOLOR_LABEL("Export.Label.FontColor", ConversionUtil.toString(Color.BLACK)),
    /** user setting: color of semantical relation roles in exports. */
    FONTCOLOR_SEMROLE("Export.SemRole.FontColor", ConversionUtil.toString(Color.BLACK)),
    /** user setting: color of plain syntactical functions in exports. */
    FONTCOLOR_SYNFUNCTION_PLAIN("Export.SynFunction.Plain.FontColor", ConversionUtil.toString(Color.BLACK)),
    /** user setting: color of bold syntactical functions in exports. */
    FONTCOLOR_SYNFUNCTION_BOLD("Export.SynFunction.Bold.FontColor", ConversionUtil.toString(Color.BLACK)),
    /** user setting: color of bold-italic syntactical functions in exports. */
    FONTCOLOR_SYNFUNCTION_BOLDTALIC("Export.SynFunction.BoldItalic.FontColor", ConversionUtil.toString(Color.BLACK)),
    /** user setting: color of italic syntactical functions in exports. */
    FONTCOLOR_SYNFUNCTION_ITALIC("Export.SynFunction.Italic.FontColor", ConversionUtil.toString(Color.BLACK)),
    /** user setting: family name of the font to use for non-origin texts in exports. */
    NONORIGINTEXT_FONT_TYPE("Export.FontType", "Times New Roman"),
    /** user setting: size of the font to use for non-origin texts in exports. */
    NONORIGINTEXT_FONT_SIZE("Export.FontSize", "14");

    /** The handler for this specific settings collection. */
    private static final OptionHandler<HmxExportOption> HANDLER = OptionHandler.getInstance(HmxExportOption.class);

    /** The actual settings key. */
    private final String key;
    /** The default value to return if none was specified before. */
    private final String defaultValue;

    /**
     * Constructor.
     *
     * @param attributeKey
     *            actual settings key
     * @param defaultValue
     *            default value to return if no value was specified before
     */
    private HmxExportOption(final String attributeKey, final String defaultValue) {
        this.key = attributeKey;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public void setValue(final String value) {
        HmxExportOption.HANDLER.setProperty(this, value);
    }

    @Override
    public String getValue() {
        String value = HmxExportOption.HANDLER.getProperty(this);
        if (value == null || value.isEmpty()) {
            value = this.defaultValue;
        }
        return value;
    }

    /**
     * Getter for the value of this specific setting.
     *
     * @return option entry for the key; returns {@code null} if this was explicitly set to be transparent or this does not represent a color
     */
    @Override
    public Color getValueAsColor() {
        return ConversionUtil.toColor(HmxExportOption.HANDLER.getProperty(this), ConversionUtil.toColor(this.defaultValue, null));
    }

    @Override
    public int getValueAsInteger() {
        int value = ConversionUtil.toInt(HmxExportOption.HANDLER.getProperty(this), Integer.MIN_VALUE);
        if (value == Integer.MIN_VALUE) {
            value = ConversionUtil.toInt(this.defaultValue, 0);
        }
        return value;
    }
}
