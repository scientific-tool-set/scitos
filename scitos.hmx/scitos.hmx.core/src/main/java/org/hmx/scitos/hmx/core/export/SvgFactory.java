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

package org.hmx.scitos.hmx.core.export;

import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.w3c.dom.Document;

/**
 * Helper class responsible for generating SVG representations of {@link Pericope}s.
 */
public final class SvgFactory {

    /**
     * Creates a XML document containing a SVG element representing the semantical analysis in the model.
     *
     * @param model
     *            {@link Pericope} to extract the semantical analysis from and export it into a SVG
     * @param includeComments
     *            if the comments are to be included in the created SVG as slightly smaller numbers and invisible description elements (in some SVG
     *            viewers processed as tool tips)
     * @return created XML document with the SVG root
     * @throws HmxException
     *             failed to create the svg document
     */
    public static Document generateSemanticalSvg(final Pericope model, final boolean includeComments) throws HmxException {
        return new SemanticalSvgCreator(model).generateSvg(includeComments);
    }

    /**
     * Creates a XML document containing a SVG element representing the syntactical analysis in the model.
     *
     * @param model
     *            {@link Pericope} to extract the syntactical analysis from and export it into a SVG
     * @param includeComments
     *            if the comments are to be included in the created SVG as slightly smaller numbers and invisible description elements (in some SVG
     *            viewers processed as tool tips)
     * @return created XML document with the SVG root
     * @throws HmxException
     *             failed to create the svg document
     */
    public static Document generateSyntacticalSvg(final Pericope model, final boolean includeComments) throws HmxException {
        return new SyntacticalSvgCreator(model).generateSvg(includeComments);
    }

    /**
     * Creates a XML document containing a SVG element listing the all comments in the model.
     *
     * @param model
     *            {@link Pericope} to extract the commentaries from and export it into a SVG
     * @param considerRelations
     *            if the comments of semantical relations are to be included in the listing
     * @param considerClauseItems
     *            if the comments of clause items are to be included in the listing
     * @return created XML document with the SVG root
     * @throws HmxException
     *             failed to create the svg document
     */
    public static Document generateCommentsSvg(final Pericope model, final boolean considerRelations, final boolean considerClauseItems)
            throws HmxException {
        return new CommentSvgCreator(model).generateSvg(considerRelations, considerClauseItems);
    }
}
