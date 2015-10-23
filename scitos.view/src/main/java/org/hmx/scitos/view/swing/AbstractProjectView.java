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

import java.awt.Component;
import java.awt.LayoutManager;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.hmx.scitos.domain.IProvider;
import org.hmx.scitos.view.IViewProject;

/**
 * Abstract implementation of a view that is supposed to be displayed in a separate tab in the {@link ScitosClient}.
 *
 * @param <P>
 *            type of the project this tab belongs to
 * @param <M>
 *            type of the associated user model in the project (either the project itself, a string representing a multi model group, or a model
 *            instance in a multi model project)
 */
public abstract class AbstractProjectView<P extends IViewProject<?>, M> extends JPanel implements IUndoManagedView, IProvider<M> {

    /** The view project this UI component belongs to. */
    private final P project;
    /**
     * The actual model object this UI component represents/is associated with. This is either the project itself, a string representing a multi model
     * group, or a model instance in a multi model project.
     */
    private final M model;

    /**
     * Main constructor.
     *
     * @param project
     *            associated project instance
     * @param model
     *            user model in the project to represent (either the project itself, a string representing a multi model group, or a model instance in
     *            a multi model project)
     * @param layout
     *            layout manager to apply
     */
    protected AbstractProjectView(final P project, final M model, final LayoutManager layout) {
        super(layout);
        this.project = project;
        this.model = model;
    }

    /**
     * Getter for the {@link IViewProject project} instance this tab belongs to.
     *
     * @return associated project instance
     */
    public final P getProject() {
        return this.project;
    }

    /**
     * Getter for the represented user model (either the project itself, a string representing a multi model group, or a model instance in a multi
     * model project).
     *
     * @return the represented user model
     */
    public M getModel() {
        return this.model;
    }

    @Override
    public M provide() {
        return this.getModel();
    }

    /**
     * Create menu items associated with this view to be added to the menu bar's 'Edit' menu. This will be called repeatedly - every time this view is
     * displayed (i.e. the containing tab is selected).
     *
     * @return 'Edit' menu items (contained {@code null} elements are interpreted as separators)
     */
    public List<JMenuItem> createEditMenuItems() {
        return Collections.emptyList();
    }

    /**
     * Create menu items associated with this view to be added to the menu bar's 'View' menu. This will be called repeatedly - every time this view is
     * displayed (i.e. the containing tab is selected).
     *
     * @return 'View' menu items (contained {@code null} elements are interpreted as separators)
     */
    public List<JMenuItem> createViewMenuItems() {
        return Collections.emptyList();
    }

    /**
     * Create components (e.g. buttons) associated with this view to be added to the main view's tool bar. This will be called repeatedly - every time
     * this view is displayed (i.e. the containing tab is selected).
     *
     * @return tool bar items (contained {@code null} elements are interpreted as separators)
     */
    public List<Component> createToolBarItems() {
        return Collections.emptyList();
    }
}
