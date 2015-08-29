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

package org.hmx.scitos.view.swing.service;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hmx.scitos.core.option.Option;
import org.hmx.scitos.view.service.IOptionPanelService;
import org.hmx.scitos.view.service.IOptionPanelServiceProvider;
import org.hmx.scitos.view.service.IOptionPanelServiceRegistry;
import org.hmx.scitos.view.swing.ScitosClient;
import org.hmx.scitos.view.swing.option.GeneralOptionPanel;
import org.hmx.scitos.view.swing.option.OptionView;

/**
 * The singleton instance receiving and providing any {@link IOptionPanelService}, that is supposed to be associated/included in the application's
 * preferences dialog.
 */
@Singleton
public final class OptionPanelServiceProviderImpl implements IOptionPanelServiceRegistry, IOptionPanelServiceProvider {

    /** The registered option panel provider implementations for the registered setting groups. */
    private final Map<Class<?>, IOptionPanelService> optionPanelServices;

    /** Main constructor. */
    @Inject
    public OptionPanelServiceProviderImpl() {
        this.optionPanelServices = new LinkedHashMap<Class<?>, IOptionPanelService>();
        // register main application preferences service
        this.registerOptionPanelService(Option.class, new IOptionPanelService() {

            @Override
            public GeneralOptionPanel createOptionPanel(final ScitosClient client, final OptionView parentView) {
                return new GeneralOptionPanel(parentView.getDialog(), client.getFrame());
            }
        });
    }

    @Override
    public void registerOptionPanelService(final Class<?> settingsClass, final IOptionPanelService service) {
        this.optionPanelServices.put(settingsClass, service);
    }

    @Override
    public Collection<IOptionPanelService> getServices() {
        return Collections.unmodifiableCollection(this.optionPanelServices.values());
    }
}
