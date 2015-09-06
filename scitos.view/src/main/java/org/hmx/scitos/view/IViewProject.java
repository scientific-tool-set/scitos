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

package org.hmx.scitos.view;

import java.io.File;
import java.util.List;

import org.hmx.scitos.core.ExportOption;
import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.domain.IModel;
import org.hmx.scitos.view.swing.MainView;

/**
 * Project interface representing general view functions associated with it.
 *
 * @param <M>
 *            class representing the managed model object
 */
public interface IViewProject<M extends IModel<M>> {

    /**
     * Getter for the (short) textual representation of this project.
     *
     * @return the project title
     */
    String getTitle();

    /**
     * Getter for the actual model object this view project represents.
     *
     * @return represented model object
     */
    M getModelObject();

    /**
     * Create textual representation for the given element in this project.
     *
     * @param element
     *            element in this project to represent as a string
     * @return string representation of the given element
     */
    String getLabel(Object element);

    /**
     * Check if a tab representing the given element is (still) valid and should be left open - otherwise the associated main view's tab will be
     * closed.
     *
     * @param element
     *            model object to check validity for
     * @return if the given element is (still) a valid part of this project
     */
    boolean isValidElement(Object element);

    /**
     * Getter for the represented file type by this kind of project.
     *
     * @return represented file type
     */
    FileType getFileType();

    /**
     * Getter for the currently open elements of this project (displayed in the {@link MainView}'s tabs).
     *
     * @return open elements
     */
    List<?> getOpenTabElements();

    /**
     * Setter for the currently open elements of this project (displayed in the {@link MainView}'s tabs).
     *
     * @param tabElements
     *            open elements
     */
    void setOpenTabElements(List<?> tabElements);

    /**
     * Save the current project to the last stored path.
     *
     * @return store path exists
     * @throws HmxException
     *             saving failed due to missing permissions or unsuccessful validation
     */
    boolean save() throws HmxException;

    /**
     * Save the current project to the specified path and store it for the next <code>saveProject()</code> call.
     *
     * @param path
     *            designated save location
     * @throws HmxException
     *             saving failed due to missing permissions or unsuccessful validation
     */
    void saveAs(File path) throws HmxException;

    /**
     * Check if the current state of this project has been saved.
     *
     * @return if the current state of this project has been saved
     */
    boolean isSaved();

    /**
     * Getter for the path this file has been saved to.
     *
     * @return path to last saved state of this project
     */
    String getSavePath();

    /**
     * Export the current project to the specified path.
     *
     * @param type
     *            the kind of export to perform
     * @param path
     *            designated export location
     * @throws HmxException
     *             export failed due to missing permissions or unsuccessful validation
     */
    void export(ExportOption type, File path) throws HmxException;

    /**
     * Attempt to close this project. Ask the user for saving the final state if it is not already saved.
     *
     * @return user saved successfully or did not want to save
     */
    boolean prepareForClosing();

    /**
     * Close this project. If there are unsaved changes, prompt the user what to do.
     *
     * @return successfully closed
     */
    boolean close();
}
