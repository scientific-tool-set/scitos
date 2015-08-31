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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.domain.IModel;
import org.hmx.scitos.view.IViewProject;
import org.hmx.scitos.view.service.IMultiModelProjectViewService;
import org.hmx.scitos.view.service.IProjectViewService;
import org.hmx.scitos.view.service.IProjectViewServiceProvider;
import org.hmx.scitos.view.service.IProjectViewServiceRegistry;
import org.hmx.scitos.view.swing.AbstractProjectView;

/**
 * Central provider of view services for {@link IViewProject projects} and their {@link IModel models}.
 */
@Singleton
public class ProjectViewServiceProviderImpl implements IProjectViewServiceRegistry, IProjectViewServiceProvider {

    /** Registered view services by their model types. */
    private final Map<Class<? extends IModel<?>>, IProjectViewService<?>> modelViewServices;
    /** Registered view services by their project types. */
    private final Map<Class<? extends IViewProject<?>>, IProjectViewService<?>> projectViewServices;

    /** Main constructor. */
    @Inject
    public ProjectViewServiceProviderImpl() {
        this.modelViewServices = Collections.synchronizedMap(new HashMap<Class<? extends IModel<?>>, IProjectViewService<?>>());
        this.projectViewServices = Collections.synchronizedMap(new HashMap<Class<? extends IViewProject<?>>, IProjectViewService<?>>());
    }

    @Override
    public final <P extends IViewProject<M>, M extends IModel<M>> void registerViewService(final Class<P> projectClass, final Class<M> modelClass,
            final IProjectViewService<P> viewService) {
        synchronized (this.modelViewServices) {
            this.modelViewServices.put(modelClass, viewService);
        }
        synchronized (this.projectViewServices) {
            this.projectViewServices.put(projectClass, viewService);
        }
    }

    /**
     * Getter for a registered service instance for the given model type.
     *
     * @param modelClass
     *            type of the model to get the registered service for
     * @return registered service
     */
    protected final IProjectViewService<?> getServiceByModel(final Class<? extends IModel<?>> modelClass) {
        final IProjectViewService<?> service;
        synchronized (this.modelViewServices) {
            service = this.modelViewServices.get(modelClass);
        }
        if (service == null) {
            throw new IllegalStateException("No project-view service registered for " + modelClass.getName());
        }
        return service;
    }

    /**
     * Getter for a registered service instance for the given project type.
     *
     * @param project
     *            project instance to get the registered service for
     * @return registered service
     */
    protected final IProjectViewService<?> getServiceByProject(final IViewProject<?> project) {
        final Class<? extends IViewProject<?>> projectClass = (Class<? extends IViewProject<?>>) project.getClass();
        final IProjectViewService<?> service;
        synchronized (this.projectViewServices) {
            service = this.projectViewServices.get(projectClass);
        }
        if (service == null) {
            throw new IllegalStateException("No project-view service registered for " + projectClass.getName());
        }
        return service;
    }

    @Override
    public final IViewProject<?> createEmptyProject(final Class<? extends IModel<?>> modelClass) {
        final IProjectViewService<?> service = this.getServiceByModel(modelClass);
        return service.createEmptyProject();
    }

    @Override
    public final IViewProject<?> createProject(final IModel<?> model, final File savePath) {
        final Class<? extends IModel<?>> modelClass = (Class<? extends IModel<?>>) model.getClass();
        final IProjectViewService<?> service = this.getServiceByModel(modelClass);
        return service.createProject(model, savePath);
    }

    @Override
    public JPopupMenu createContextMenu(final IViewProject<?> project, final Object element) {
        final IProjectViewService<?> service = this.getServiceByProject(project);
        JPopupMenu result = service.createContextMenu(project, element);
        if (project == element || project.getModelObject() == element) {
            final JMenuItem closeProjectItem = new JMenuItem(Message.PROJECT_CLOSE.get());
            closeProjectItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent event) {
                    project.close();
                }
            });
            if (result == null) {
                result = new JPopupMenu(project.getLabel(element));
            } else {
                result.addSeparator();
            }
            result.add(closeProjectItem);
        }
        return result;
    }

    @Override
    public final AbstractProjectView<?, ? extends IViewProject<?>> createProjectView(final IViewProject<?> project) {
        final IProjectViewService<?> service = this.getServiceByProject(project);
        return service.createProjectView(project);
    }

    @Override
    public final AbstractProjectView<?, String> createModelGroupView(final IViewProject<?> project, final String modelGroup) {
        final IProjectViewService<?> service = this.getServiceByProject(project);
        if (service instanceof IMultiModelProjectViewService<?, ?>) {
            return ((IMultiModelProjectViewService<?, ?>) service).createModelGroupView(project, modelGroup);
        }
        throw new IllegalStateException("No multi-model project-view service registered for " + project.getClass().getName());
    }

    @Override
    public final AbstractProjectView<?, ? extends IModel<?>> createModelView(final IViewProject<?> project, final IModel<?> model) {
        final IProjectViewService<?> service = this.getServiceByProject(project);
        if (service instanceof IMultiModelProjectViewService<?, ?>) {
            return ((IMultiModelProjectViewService<?, ?>) service).createModelView(project, model);
        }
        throw new IllegalStateException("No multi-model project-view service registered for " + project.getClass().getName());
    }
}
