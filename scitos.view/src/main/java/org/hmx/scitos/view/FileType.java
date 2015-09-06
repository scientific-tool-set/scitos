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

import org.hmx.scitos.core.i18n.Message;
import org.w3c.dom.Document;

/**
 * The valid file types this program is able to open and save.
 */
public enum FileType {
    /**
     * File type: autobiographical interview scoring.
     */
    AIS(Message.MENUBAR_FILE_TYPE_AIS, ".aisp", "AI-Scoring/1.0", "org.hmx.scitos.ais.view.swing.AisModule",
            "org.hmx.scitos.ais.view.swing.AisModuleInitializer");

    /**
     * The name of an xml document's root element attribute, determining the expected project/model type contained.
     */
    private static final String XML_ROOT_TYPE_ATTRIBUTE = "FileType";

    /**
     * The localizable message to be displayed for this type.
     */
    private final Message localizableName;
    /**
     * The associated file extension.
     */
    private final String fileExtension;
    /**
     * The <code>type</code> attribute value in the actual <code>xml</code> root element of the represented files.
     */
    private final String internalTypeAttribute;
    /**
     * The fully qualified name of the associated module class (to construct a dependency injection graph).
     */
    private final String moduleClassName;
    /**
     * The fully qualified name of the associated module's initializing class (to be loaded via dependency injection).
     */
    private final String moduleInitializerClassName;

    /**
     * Returns the file type represented by the given xml document.
     *
     * @param xml
     *            document to interpret the represented file type
     * @return represented file type (i.e. expected project/model type contained)
     */
    public static FileType fromXml(final Document xml) {
        final String attributeValue = xml.getDocumentElement().getAttribute(FileType.XML_ROOT_TYPE_ATTRIBUTE);
        for (final FileType singleType : FileType.values()) {
            if (singleType.internalTypeAttribute.equalsIgnoreCase(attributeValue)) {
                return singleType;
            }
        }
        return null;
    }

    /**
     * Main constructor.
     *
     * @param localizableName
     *            the localizable message to be displayed for this type
     * @param extension
     *            the file extension
     * @param internalTypeAttribute
     *            the internal file type (as expected in the actual XML structure of an opened file)
     * @param moduleClassName
     *            the fully qualified name of the associated module class (to construct a dependency injection graph)
     * @param moduleInitializerClassName
     *            the fully qualified name of the associated module's initializing class (to be loaded via dependency injection)
     */
    private FileType(final Message localizableName, final String extension, final String internalTypeAttribute, final String moduleClassName,
            final String moduleInitializerClassName) {
        this.localizableName = localizableName;
        this.fileExtension = extension;
        this.internalTypeAttribute = internalTypeAttribute;
        this.moduleClassName = moduleClassName;
        this.moduleInitializerClassName = moduleInitializerClassName;
    }

    /**
     * Getter for the localizable message to be displayed for this type.
     *
     * @return the representing localizable message
     */
    public Message getLocalizableName() {
        return this.localizableName;
    }

    /**
     * Getter for the file extension.
     *
     * @return the file extension
     */
    public String getFileExtension() {
        return this.fileExtension;
    }

    /**
     * Getter for the dependency injection module's class name.
     *
     * @return the dependency injection module's class name
     */
    public String getModuleClassName() {
        return this.moduleClassName;
    }

    /**
     * Getter for the dependency injections module initializer's class name.
     *
     * @return the dependency injections module initializer's class name
     */
    public String getModuleInitializerClassName() {
        return this.moduleInitializerClassName;
    }

    /**
     * Set the associated xml attribute, which is used for determining the expected project/model type contained, to represent this file type.
     *
     * @param xml
     *            the xml document to be marked as representing this file type
     */
    public void applyToXml(final Document xml) {
        xml.getDocumentElement().setAttribute(FileType.XML_ROOT_TYPE_ATTRIBUTE, this.internalTypeAttribute);
    }
}
