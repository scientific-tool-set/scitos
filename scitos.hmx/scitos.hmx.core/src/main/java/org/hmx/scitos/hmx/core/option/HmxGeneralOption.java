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

package org.hmx.scitos.hmx.core.option;

import java.awt.Color;

import org.hmx.scitos.core.option.IOptionSetting;
import org.hmx.scitos.core.option.OptionHandler;
import org.hmx.scitos.core.util.ConversionUtil;

/**
 * Collection of general user preferences for the HermeneutiX module.
 */
public enum HmxGeneralOption implements IOptionSetting {
    /** user setting: color of arrows (between splitted propositions). */
    ARROW_COLOR("Arrow.Color", ConversionUtil.toString(Color.BLUE)),
    /** user setting: color of semantical relations. */
    RELATION_COLOR("Relation.Color", ConversionUtil.toString(Color.RED)),
    /** user setting: color of semantical relations. */
    COMMENTED_BORDER_COLOR("Commented.BorderColor", ConversionUtil.toString(Color.GREEN)),
    /** user setting: width of syntactical indentations. */
    INDENTATION_WIDTH("SynAnalysis.IndentationWidth", "50"),
    /** user setting: if the setting area in the new-project-setup should be visible by default. */
    SHOW_SETTINGS("TextInput.ShowInputSettings", String.valueOf(true)),
    /** user setting: default author name when setting up new projects. */
    AUTHOR("ProjectInfo.DefaultAuthor", null);

    /** The handler for this specific settings collection. */
    private static final OptionHandler<HmxGeneralOption> HANDLER = OptionHandler.getInstance(HmxGeneralOption.class);

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
    private HmxGeneralOption(final String attributeKey, final String defaultValue) {
        this.key = attributeKey;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public void setValue(final String value) {
        HmxGeneralOption.HANDLER.setProperty(this, value);
    }

    @Override
    public String getValue() {
        String value = HmxGeneralOption.HANDLER.getProperty(this);
        if (value == null || value.isEmpty()) {
            value = this.defaultValue;
        }
        return value;
    }

    /**
     * Getter for the value of this specific setting.
     *
     * @return option entry for the key
     */
    public boolean getValueAsBoolean() {
        return Boolean.valueOf(this.getValue());
    }

    @Override
    public int getValueAsInteger() {
        int value = ConversionUtil.toInt(HmxGeneralOption.HANDLER.getProperty(this), Integer.MIN_VALUE);
        if (value == Integer.MIN_VALUE) {
            value = ConversionUtil.toInt(this.defaultValue, 0);
        }
        return value;
    }

    @Override
    public Color getValueAsColor() {
        return ConversionUtil.toColor(HmxGeneralOption.HANDLER.getProperty(this), ConversionUtil.toColor(this.defaultValue, null));
    }
}
