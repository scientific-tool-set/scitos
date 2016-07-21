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

package org.hmx.scitos.ais.view.swing;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hmx.scitos.ais.core.AisOption;
import org.hmx.scitos.ais.core.ModelParseServiceImpl;
import org.hmx.scitos.ais.domain.model.AisProject;
import org.hmx.scitos.view.FileType;
import org.hmx.scitos.view.service.IModelParseServiceRegistry;
import org.hmx.scitos.view.service.IOptionPanelServiceRegistry;
import org.hmx.scitos.view.service.IProjectViewServiceRegistry;

/**
 * Dependency Injection initializer for the AIS module, responsible for populating the main service registries with the module specific service
 * implementations.
 */
@Singleton
public final class AisModuleInitializer {

    /**
     * Main constructor: populates each injected service implementation to its respective registry.
     *
     * @param modelServiceRegistry
     *            registry for services handling conversions between xml and java model
     * @param modelService
     *            service implementation handling conversions between xml and java model for AIS module
     * @param optionPanelServiceRegistry
     *            registry for services handling the view panels offering customizing options for the user
     * @param optionPanelService
     *            service implementation handling the view panel offering customizing options in the AIS module
     * @param viewServiceRegistry
     *            registry for services handling the generation of view projects and their respective views
     * @param viewService
     *            service implementation handling the generation of the AIS view project and its views
     */
    @Inject
    public AisModuleInitializer(final IModelParseServiceRegistry modelServiceRegistry, final ModelParseServiceImpl modelService,
            final IOptionPanelServiceRegistry optionPanelServiceRegistry, final OptionPanelServiceImpl optionPanelService,
            final IProjectViewServiceRegistry viewServiceRegistry, final ProjectViewServiceImpl viewService) {
        modelServiceRegistry.registerModelParseService(FileType.AIS, AisProject.class, modelService);
        optionPanelServiceRegistry.registerOptionPanelService(AisOption.class, optionPanelService);
        viewServiceRegistry.registerViewService(AisViewProject.class, AisProject.class, viewService);
    }
}
