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

package org.hmx.scitos.view.swing;

import org.hmx.scitos.view.service.IModelParseServiceProvider;
import org.hmx.scitos.view.service.IModelParseServiceRegistry;
import org.hmx.scitos.view.service.IOptionPanelServiceProvider;
import org.hmx.scitos.view.service.IOptionPanelServiceRegistry;
import org.hmx.scitos.view.service.IProjectViewServiceProvider;
import org.hmx.scitos.view.service.IProjectViewServiceRegistry;
import org.hmx.scitos.view.swing.service.ModelParseServiceProviderImpl;
import org.hmx.scitos.view.swing.service.OptionPanelServiceProviderImpl;
import org.hmx.scitos.view.swing.service.ProjectViewServiceProviderImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Central dependency injection module, containing providing methods to map specific implementations to requested interface types.
 */
@Module(injects = ScitosClient.class, library = true)
public final class ScitosModule {

    /**
     * Providing method for the singleton IProjectViewServiceProvider implementation.
     *
     * @param impl
     *            actual implementation to return
     * @return IProjectViewServiceProvider instance
     */
    @Provides
    public IProjectViewServiceProvider provideProjectViewServiceProvider(final ProjectViewServiceProviderImpl impl) {
        return impl;
    }

    /**
     * Providing method for the singleton IProjectViewServiceRegistry implementation.
     *
     * @param impl
     *            actual implementation to return
     * @return IProjectViewServiceRegistry instance
     */
    @Provides
    public IProjectViewServiceRegistry provideProjectViewServiceRegistry(final ProjectViewServiceProviderImpl impl) {
        return impl;
    }

    /**
     * Providing method for the singleton IOptionPanelServiceProvider implementation.
     *
     * @param impl
     *            actual implementation to return
     * @return IOptionPanelServiceProvider instance
     */
    @Provides
    public IOptionPanelServiceProvider provideOptionPanelServiceProvider(final OptionPanelServiceProviderImpl impl) {
        return impl;
    }

    /**
     * Providing method for the singleton IOptionPanelServiceRegistry implementation.
     *
     * @param impl
     *            actual implementation to return
     * @return IOptionPanelServiceRegistry instance
     */
    @Provides
    public IOptionPanelServiceRegistry provideOptionPanelServiceRegistry(final OptionPanelServiceProviderImpl impl) {
        return impl;
    }

    /**
     * Providing method for the singleton IModelParseServiceProvider implementation.
     *
     * @param impl
     *            actual implementation to return
     * @return IModelParseServiceProvider instance
     */
    @Provides
    public IModelParseServiceProvider provideModelParseServiceProvider(final ModelParseServiceProviderImpl impl) {
        return impl;
    }

    /**
     * Providing method for the singleton IModelParseServiceRegistry implementation.
     *
     * @param impl
     *            actual implementation to return
     * @return IModelParseServiceRegistry instance
     */
    @Provides
    public IModelParseServiceRegistry provideModelParseServiceRegistry(final ModelParseServiceProviderImpl impl) {
        return impl;
    }
}
