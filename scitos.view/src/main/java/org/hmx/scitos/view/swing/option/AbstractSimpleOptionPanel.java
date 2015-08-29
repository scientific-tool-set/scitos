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

import java.awt.LayoutManager;
import java.util.HashMap;
import java.util.Map;

import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.core.option.IOptionSetting;
import org.hmx.scitos.view.swing.util.SplitFrame;

/**
 * Abstract panel in the application's preferences dialog, that handles a number of simple settings, represented in an Enum.
 * 
 * @param <S>
 *            the managed enumerated settings
 */
public abstract class AbstractSimpleOptionPanel<S extends Enum<? extends IOptionSetting> & IOptionSetting> extends AbstractOptionPanel {

    /**
     * All settings chosen in this instance, which will be saved when the {@link SplitFrame} is closed by the <code>OK</code>-button.
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
    protected AbstractSimpleOptionPanel(final LayoutManager layout, final Message title) {
        super(layout, title);
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
