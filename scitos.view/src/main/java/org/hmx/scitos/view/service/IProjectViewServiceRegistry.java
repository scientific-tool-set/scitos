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

import org.hmx.scitos.domain.IModel;
import org.hmx.scitos.view.IViewProject;

/**
 * Registry part of a {@link IProjectViewServiceProvider} implementation.
 */
public interface IProjectViewServiceRegistry {

    /**
     * Register the given service instance for the specified project type.
     *
     * @param projectClass
     *            type of the project the given service should be registered for
     * @param modelClass
     *            type of the model managed by the associated project
     * @param viewService
     *            service instance to register
     */
    <P extends IViewProject<M>, M extends IModel<M>> void registerViewService(Class<P> projectClass, Class<M> modelClass,
            IProjectViewService<P, M> viewService);

}
