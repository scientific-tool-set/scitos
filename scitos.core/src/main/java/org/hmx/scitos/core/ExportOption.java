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

package org.hmx.scitos.core;

import org.hmx.scitos.core.i18n.ILocalizableMessage;

/** Descriptor for an available export option. */
public class ExportOption {

    /** File type associated with an export option. */
    public enum TargetFileType {
        /** Export file type: Web Page (*.html). */
        HTML(".html"),
        /** Export file type: Spreadsheet (Open Document Standard - *.ods) */
        ODS(".ods"),
        /** Export file type: Scalable Vector Graphic (*.svg) */
        SVG(".svg");

        /** The file extension of an exported file. */
        private final String extension;

        /**
         * Constructor.
         *
         * @param extension
         *            the actual target file extension
         */
        private TargetFileType(final String extension) {
            this.extension = extension;
        }

        /**
         * Getter for the actual target file extension.
         *
         * @return file extension (e.g. ".html")
         */
        public String getExtension() {
            return this.extension;
        }
    }

    /** The text of the export menu entry. */
    private final ILocalizableMessage menuEntry;
    /** The exported file's type. */
    private final TargetFileType targetFileType;
    /** The location of the XSLT stylesheet on the classpath. */
    private final String stylesheetPath;

    /**
     * Main constructor.
     *
     * @param menuEntry
     *            the associated export menu entry's text
     * @param targetFileType
     *            the type of the exported file
     * @param stylesheetPath
     *            the location of the XSLT stylesheet on the classpath
     */
    public ExportOption(final ILocalizableMessage menuEntry, final TargetFileType targetFileType, final String stylesheetPath) {
        this.menuEntry = menuEntry;
        this.targetFileType = targetFileType;
        this.stylesheetPath = stylesheetPath;
    }

    /**
     * Getter for the associated export menu entry's text.
     *
     * @return the text of the export menu entry
     */
    public ILocalizableMessage getMenuEntry() {
        return this.menuEntry;
    }

    /**
     * Getter for the exported file's type.
     *
     * @return the target file's type
     */
    public TargetFileType getTargetFileType() {
        return this.targetFileType;
    }

    /**
     * Getter for the location of the XSLT stylesheet on the classpath.
     *
     * @return the stylesheet's path
     */
    public String getStylesheetPath() {
        return this.stylesheetPath;
    }
}
