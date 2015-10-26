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

import java.io.File;

import javax.inject.Inject;

import org.hmx.scitos.domain.IModel;
import org.hmx.scitos.hmx.core.ModelHandlerImpl;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.core.option.HmxLanguageOption;
import org.hmx.scitos.hmx.core.option.HmxRelationOption;
import org.hmx.scitos.hmx.domain.model.LanguageModel;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.domain.model.RelationModel;
import org.hmx.scitos.hmx.view.swing.components.SingleProjectView;
import org.hmx.scitos.view.ContextMenuBuilder;
import org.hmx.scitos.view.FileType;
import org.hmx.scitos.view.IViewProject;
import org.hmx.scitos.view.service.IProjectViewService;
import org.hmx.scitos.view.swing.ScitosClient;
import org.hmx.scitos.view.swing.util.ViewUtil;

/**
 * Implementation of the {@link IProjectViewService} for the AIS module.
 */
public class ProjectViewServiceImpl implements IProjectViewService<HmxSwingProject, Pericope> {

    /** The associated active client instance, to forward to created views. */
    private final ScitosClient client;
    /**
     * The (user defined) provider of {@link LanguageModel}s available for new projects.
     */
    private final HmxLanguageOption languageModelProvider;
    /**
     * The (user defined) provider of the {@link RelationModel} being applied.
     */
    private final HmxRelationOption relationProvider;

    /**
     * Constructor.
     *
     * @param client
     *            associated active client instance
     * @param languageModelProvider
     *            the provider of the available {@link LanguageModel}s, eligible for new projects
     * @param relationProvider
     *            the provider of the current {@link RelationModel}, being applied
     */
    @Inject
    public ProjectViewServiceImpl(final ScitosClient client, final HmxLanguageOption languageModelProvider,
            final HmxRelationOption relationProvider) {
        this.client = client;
        this.languageModelProvider = languageModelProvider;
        this.relationProvider = relationProvider;
    }

    @Override
    public HmxSwingProject createEmptyProject() {
        final File path = ViewUtil.getSaveDestination(this.client.getFrame(), FileType.HMX.getFileExtension(), HmxMessage.PROJECT_NEW.get(), true);
        if (path != null) {
            return this.createProject(new Pericope(), path);
        }
        return null;
    }

    @Override
    public HmxSwingProject createProject(final IModel<?> model, final File savePath) {
        return new HmxSwingProject(this.client, new ModelHandlerImpl((Pericope) model), savePath);
    }

    @Override
    public ContextMenuBuilder createContextMenu(final IViewProject<?> project, final Object element) {
        // no additional entries for the item representing the given project in the main project tree
        return null;
    }

    @Override
    public SingleProjectView createProjectView(final IViewProject<?> project) {
        return new SingleProjectView((HmxSwingProject) project, this.languageModelProvider, this.relationProvider,
                this.client.getModelParseProvider());
    }
}
