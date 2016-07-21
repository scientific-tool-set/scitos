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

package org.hmx.scitos.view.service;

import org.hmx.scitos.view.swing.ScitosClient;
import org.hmx.scitos.view.swing.option.AbstractOptionPanel;
import org.hmx.scitos.view.swing.option.OptionView;

/**
 * Generic interface of an option panel providing instance. The provided option panel will be part of the application's preferences.
 */
public interface IOptionPanelService {

    /**
     * Create the option panel to be displayed in the application's preferences dialog that is currently being assembled.
     *
     * @param client
     *            the main client for which the preferences dialog has been requested
     * @param parentDialog
     *            the actual preferences dialog the created option panel will be displayed in
     * @return the option panel to be included in the given preferences dialog
     */
    AbstractOptionPanel createOptionPanel(ScitosClient client, OptionView parentDialog);
}
