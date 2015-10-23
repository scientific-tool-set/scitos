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

package org.hmx.scitos.view.service;

import java.io.File;

import org.hmx.scitos.domain.IModel;
import org.hmx.scitos.view.ContextMenuBuilder;
import org.hmx.scitos.view.IViewProject;
import org.hmx.scitos.view.swing.AbstractProjectView;

/**
 * Service provider for {@link IProjectViewService} implementations.
 */
public interface IProjectViewServiceProvider {

    /**
     * Create an empty view project for the given model type.
     *
     * @param modelClass
     *            model type to manage in the project
     * @return created project instance
     */
    IViewProject<?> createEmptyProject(Class<? extends IModel<?>> modelClass);

    /**
     * Create a view project for the given model instance.
     *
     * @param model
     *            model instance to manage in the project
     * @param savePath
     *            path from where the given model has been loaded (will be used as default save destination)
     * @return created project instance
     */
    IViewProject<?> createProject(IModel<?> model, File savePath);

    /**
     * Create context menu, offering additional actions associated with the given element. This will be called every time a context menu is triggered
     * for the given element in the specified project.
     *
     * @param project
     *            project containing the given element
     * @param element
     *            element in the specified project a context menu has been requested for
     * @return context menu to display (can be <code>null</code>, if none should be shown)
     */
    ContextMenuBuilder createContextMenu(IViewProject<?> project, Object element);

    /**
     * Create the view for the given project instance.
     *
     * @param project
     *            project instance to represent in the view
     * @return created view instance
     */
    AbstractProjectView<?, ? extends IModel<?>> createProjectView(IViewProject<?> project);

    /**
     * Create the view for the model group with the given label in the given project instance.
     *
     * @param project
     *            project instance containing the model group with the given label
     * @param modelGroup
     *            label of the model to represent in the view
     * @return created view instance (or <code>NULL</code> if no group view is provided)
     */
    AbstractProjectView<?, String> createModelGroupView(IViewProject<?> project, String modelGroup);

    /**
     * Create the view for the given model instance in the given project instance.
     *
     * @param project
     *            project instance containing the given model
     * @param model
     *            model instance to represent in the view
     * @return created view instance
     */
    AbstractProjectView<?, ? extends IModel<?>> createModelView(IViewProject<?> project, IModel<?> model);
}
