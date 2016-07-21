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

import org.hmx.scitos.domain.IModel;
import org.hmx.scitos.domain.IMultiObjectModel;
import org.hmx.scitos.view.IViewProject;
import org.hmx.scitos.view.swing.AbstractProjectView;

/**
 * Interface of a multi-model project view provider.
 *
 * @param <P>
 *            type of the associated project
 * @param <M>
 *            type of the project's (top level) model element
 * @param <S>
 *            type of the sub models of the multi model project
 */
public interface IMultiModelProjectViewService<P extends IViewProject<M>, M extends IMultiObjectModel<M, S>, S extends IModel<S>> extends
        IProjectViewService<P, M> {

    /**
     * Create the view for the model group with the given label in the given project instance.
     *
     * @param project
     *            project instance containing the model group with the given label (should be of type {@code P})
     * @param modelGroup
     *            label of the model to represent in the view
     * @return created view instance (or {@code NULL} if no group view is provided)
     */
    AbstractProjectView<P, String> createModelGroupView(IViewProject<?> project, String modelGroup);

    /**
     * Create the view for the given model instance in the given project instance.
     *
     * @param project
     *            project instance containing the given model (should be of type {@code P})
     * @param model
     *            model instance to represent in the view (should be of type {@code S})
     * @return created view instance
     */
    AbstractProjectView<P, S> createModelView(IViewProject<?> project, IModel<?> model);

}