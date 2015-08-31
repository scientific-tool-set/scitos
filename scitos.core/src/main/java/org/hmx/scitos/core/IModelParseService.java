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

package org.hmx.scitos.core;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;

import org.hmx.scitos.domain.IModel;
import org.w3c.dom.Document;

/**
 * Generic interface for the model specific instantiation of an empty main model (for a new project) and the parsing from and to the individual xml
 * structure.
 *
 * @param <M>
 *            type of the managed model
 */
public interface IModelParseService<M extends IModel<M>> {

    /**
     * Parse the model contained in the given document's xml structure.
     *
     * @param xml
     *            document to parse
     * @param originPath
     *            path where the given document has been loaded from
     * @return parsed model instance from xml document and list of view elements to be displayed
     * @throws HmxException
     *             wrapped error that occured while parsing the document
     */
    Entry<M, List<Object>> parseModelFromXml(Document xml, File originPath) throws HmxException;

    /**
     * Create the xml structure for the given model instance.
     *
     * @param model
     *            model instance to parse (should be of type <code>M</code>)
     * @param openViewElements
     *            list of currently displayed view elements, to be restored on opening
     * @return created xml structure
     * @throws HmxException
     *             wrapped error that occured while generating the document
     */
    Document parseXmlFromModel(IModel<?> model, List<?> openViewElements) throws HmxException;
}