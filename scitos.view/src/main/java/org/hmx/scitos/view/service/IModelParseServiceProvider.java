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
import java.util.List;
import java.util.Map.Entry;

import org.hmx.scitos.core.ExportOption;
import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.domain.IModel;
import org.hmx.scitos.view.FileType;
import org.w3c.dom.Document;

/**
 * Service provider handling conversions between xml and java structures for the registered file types.
 */
public interface IModelParseServiceProvider {

    /**
     * Get the associated model implementation class for the given file type.
     *
     * @param type
     *            file type to get associated model implementation class for
     * @return associated model implementation class
     */
    Class<? extends IModel<?>> getModelClassForFileType(FileType type);

    /**
     * Open the given file and parse the contained model object.
     *
     * @param target
     *            file to open
     * @return successfully parsed model object from file and the list of elements to be initially displayed
     * @throws HmxException
     *             the targeted file did not contain a valid/recognized model
     */
    Entry<? extends IModel<?>, List<?>> open(File target) throws HmxException;

    /**
     * Save the given model object to the targeted {@link File}.
     *
     * @param model
     *            model object to save
     * @param openViewElements
     *            the list of element to be initially displayed when the generated file is opened
     * @param target
     *            file to save the model object into (old version is replaced, if one already exists)
     * @throws HmxException
     *             the targeted file could not be created/replaced
     */
    void save(IModel<?> model, List<?> openViewElements, File target) throws HmxException;

    /**
     * Save the given {@code xml} {@link Document} to the targeted {@link File}.
     *
     * @param xml
     *            xml document to save
     * @param target
     *            file to save the document into (old version is replaced, if one already exists)
     * @throws HmxException
     *             the targeted file could not be created/replaced
     */
    void save(Document xml, File target) throws HmxException;

    /**
     * Export the given model object to the targeted file.
     *
     * @param model
     *            model object to export
     * @param stylesheetPath
     *            path to the XSLT stylesheet on the classpath to apply
     * @param target
     *            file export the model object to
     * @throws HmxException
     *             the targeted file could not be created/replaced
     */
    void export(IModel<?> model, String stylesheetPath, File target) throws HmxException;

    /**
     * Getter for all export options generally available for the given model object.
     *
     * @param model
     *            model object to (potentially) export
     * @return supported export options
     */
    List<ExportOption> getSupportedExports(IModel<?> model);
}
