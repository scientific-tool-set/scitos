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

package org.hmx.scitos.ais.view.swing;

import javax.inject.Inject;

import org.hmx.scitos.ais.core.AisOption;
import org.hmx.scitos.ais.view.swing.components.OptionPanel;
import org.hmx.scitos.view.service.IOptionPanelService;
import org.hmx.scitos.view.service.IOptionPanelServiceProvider;
import org.hmx.scitos.view.service.IOptionPanelServiceRegistry;
import org.hmx.scitos.view.swing.ScitosClient;
import org.hmx.scitos.view.swing.option.OptionView;

/**
 * The AIS module's implementation of the {@link IOptionPanelService}, allowing the modification of any setting in the {@link AisOption} class.
 *
 * @see IOptionPanelServiceRegistry
 * @see IOptionPanelServiceProvider
 */
public class OptionPanelServiceImpl implements IOptionPanelService {

    /** Actual option handler instance to refer to when instantiating an option panel. */
    private final AisOption options;

    /**
     * Main constructor.
     *
     * @param options
     *            AIS module's preferences handler to represent in an option panel
     */
    @Inject
    public OptionPanelServiceImpl(final AisOption options) {
        this.options = options;
    }

    @Override
    public OptionPanel createOptionPanel(final ScitosClient client, final OptionView parentView) {
        return new OptionPanel(this.options);
    }
}
