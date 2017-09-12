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

package org.hmx.scitos.core.option;

import java.awt.Color;
import java.util.Locale;

import javax.swing.UIManager;

import org.hmx.scitos.core.util.ConversionUtil;

/**
 * Collection of user preferences.
 */
public enum Option implements IOptionSetting {
    /** User setting: the display language. */
    TRANSLATION("Translation", null),
    /** User setting: the gui (swing) look-and-feel. */
    LOOK_AND_FEEL("SwingLookAndFeel", UIManager.getSystemLookAndFeelClassName()),
    /** User setting: the number of undo-able steps per open project. */
    UNDO_LIMIT("Undo.Limit", "10"),
    /** For usability: last directory used for saving. */
    WORKDIR("WorkDir", null),
    /** For usability: window width in the last session. */
    WINDOW_WIDTH("Window.Width", "1000"),
    /** For usability: window height in the last session. */
    WINDOW_HEIGHT("Window.Height", "700"),
    /** For usability: left border of the window in the last session. */
    WINDOW_X_LOCATION("Window.PosX", "0"),
    /** For usability: top border of the window in the last session. */
    WINDOW_Y_LOCATION("Window.PosY", "0");

    /** The handler for this specific settings collection. */
    private static final OptionHandler<Option> HANDLER = OptionHandler.getInstance(Option.class);

    /** The actual settings key. */
    private final String key;
    /** The default value to return if none was specified before. */
    private final String defaultValue;

    /**
     * Main constructor.
     *
     * @param attributeKey
     *            actual settings key
     * @param defaultValue
     *            default value to return if no value was specified before
     */
    private Option(final String attributeKey, final String defaultValue) {
        this.key = attributeKey;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public void setValue(final String value) {
        Option.HANDLER.setProperty(this, value);
    }

    @Override
    public String getValue() {
        String value = Option.HANDLER.getProperty(this);
        if (value == null || value.isEmpty()) {
            value = this.defaultValue;
        }
        return value;
    }

    @Override
    public int getValueAsInteger() {
        int value = ConversionUtil.toInt(Option.HANDLER.getProperty(this), Integer.MIN_VALUE);
        if (value == Integer.MIN_VALUE) {
            value = ConversionUtil.toInt(this.defaultValue, 0);
        }
        return value;
    }

    @Override
    public Color getValueAsColor() {
        return ConversionUtil.toColor(Option.HANDLER.getProperty(this), ConversionUtil.toColor(this.defaultValue, null));
    }

    /**
     * Getter for the value of this specific setting, interpreted as a {@link Locale}.
     *
     * @return the currently set value (or the default if none is specified)
     */
    public Locale getValueAsLocale() {
        return ConversionUtil.toLocale(this.getValue(), Locale.getDefault());
    }
}
