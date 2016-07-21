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

package org.hmx.scitos.hmx.view.swing;

import javax.inject.Inject;

import org.hmx.scitos.hmx.core.option.HmxGeneralOption;
import org.hmx.scitos.hmx.view.swing.option.HmxGeneralOptionPanel;
import org.hmx.scitos.view.service.IOptionPanelService;
import org.hmx.scitos.view.service.IOptionPanelServiceProvider;
import org.hmx.scitos.view.service.IOptionPanelServiceRegistry;
import org.hmx.scitos.view.swing.ScitosClient;
import org.hmx.scitos.view.swing.option.OptionView;

/**
 * The HermeneutiX module's implementation of the {@link IOptionPanelService}, allowing the modification of any setting in the
 * {@link HmxGeneralOption} class.
 *
 * @see IOptionPanelServiceRegistry
 * @see IOptionPanelServiceProvider
 */
public class GeneralOptionPanelServiceImpl implements IOptionPanelService {

    /**
     * Main constructor.
     */
    @Inject
    public GeneralOptionPanelServiceImpl() {
        // mandatory constructor for dependency injection
    }

    @Override
    public HmxGeneralOptionPanel createOptionPanel(final ScitosClient client, final OptionView parentView) {
        return new HmxGeneralOptionPanel();
    }
}
