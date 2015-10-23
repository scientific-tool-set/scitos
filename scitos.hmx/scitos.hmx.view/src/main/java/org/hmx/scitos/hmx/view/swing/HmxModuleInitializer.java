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

package org.hmx.scitos.hmx.view.swing;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hmx.scitos.hmx.core.ModelParseServiceImpl;
import org.hmx.scitos.hmx.core.option.HmxExportOption;
import org.hmx.scitos.hmx.core.option.HmxGeneralOption;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.view.FileType;
import org.hmx.scitos.view.service.IModelParseServiceRegistry;
import org.hmx.scitos.view.service.IOptionPanelServiceRegistry;
import org.hmx.scitos.view.service.IProjectViewServiceRegistry;

/**
 * Dependency Injection initializer for the HermeneutiX module, responsible for populating the main service registries with the module specific
 * service implementations.
 */
@Singleton
public final class HmxModuleInitializer {

    /**
     * Main constructor: populates each injected service implementation to its respective registry.
     *
     * @param modelServiceRegistry
     *            registry for services handling conversions between xml and java model
     * @param modelService
     *            service implementation handling conversions between xml and java model for AIS module
     * @param optionPanelServiceRegistry
     *            registry for services handling the view panels offering customizing options for the user
     * @param generalOptionPanelService
     *            service implementation handling the view panel offering general customizing options in the HermeneutiX module
     * @param viewServiceRegistry
     *            registry for services handling the generation of view projects and their respective views
     * @param viewService
     *            service implementation handling the generation of the HermeneutiX view project and its views
     */
    @Inject
    public HmxModuleInitializer(final IModelParseServiceRegistry modelServiceRegistry, final ModelParseServiceImpl modelService,
            final IOptionPanelServiceRegistry optionPanelServiceRegistry, final GeneralOptionPanelServiceImpl generalOptionPanelService,
            final ExportOptionPanelServiceImpl exportOptionPanelService, final IProjectViewServiceRegistry viewServiceRegistry,
            final ProjectViewServiceImpl viewService) {
        modelServiceRegistry.registerModelParseService(FileType.HMX_OLD, Pericope.class, modelService);
        modelServiceRegistry.registerModelParseService(FileType.HMX, Pericope.class, modelService);
        optionPanelServiceRegistry.registerOptionPanelService(HmxGeneralOption.class, generalOptionPanelService);
        optionPanelServiceRegistry.registerOptionPanelService(HmxExportOption.class, exportOptionPanelService);
        viewServiceRegistry.registerViewService(HmxSwingProject.class, Pericope.class, viewService);
    }
}
