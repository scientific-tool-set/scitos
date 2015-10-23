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
import org.w3c.dom.Element;

/**
 * The valid file types this program is able to open and save.
 */
public enum FileType {
    /**
     * File type: autobiographical interview scoring.
     */
    AIS(Message.MENUBAR_FILE_TYPE_AIS, ".aisp", "FileType", "AI-Scoring/1.0", "org.hmx.scitos.ais.view.swing.AisModule",
            "org.hmx.scitos.ais.view.swing.AisModuleInitializer"),
    /**
     * File type: HermeneutiX (syntactical and semantical structure analysis of complex texts - potentially in foreign language).
     */
    HMX(Message.MENUBAR_FILE_TYPE_HMX, ".hmx", "FileType", "HermeneutiX/2.0", "org.hmx.scitos.hmx.view.swing.HmxModule",
            "org.hmx.scitos.hmx.view.swing.HmxModuleInitializer"),
    /**
     * File type: HermeneutiX (old type for backwards compatibility).
     */
    HMX_OLD(null, ".hmx", "Type", "HermeneutiX", "org.hmx.scitos.hmx.view.swing.HmxModule", "org.hmx.scitos.hmx.view.swing.HmxModuleInitializer");

    /**
     * The localizable message to be displayed for this type.
     */
    private final Message localizableName;
    /**
     * The associated file extension.
     */
    private final String fileExtension;
    /**
     * The {@code type} attribute on a {@code xml}'s root element of an associated file.
     */
    private final String typeAttribute;
    /**
     * The {@code type} attribute's value in the actual {@code xml} root element of an associated file.
     */
    private final String typeAttributeValue;
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
        final Element root = xml.getDocumentElement();
        for (final FileType singleType : FileType.values()) {
            if (root.getAttribute(singleType.typeAttribute).equals(singleType.typeAttributeValue)) {
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
     * @param typeAttribute
     *            the internal file type attribute (on the XML structure's document element)
     * @param typeAttributeValue
     *            the internal file type value (as expected in the actual XML structure of an opened file)
     * @param moduleClassName
     *            the fully qualified name of the associated module class (to construct a dependency injection graph)
     * @param moduleInitializerClassName
     *            the fully qualified name of the associated module's initializing class (to be loaded via dependency injection)
     */
    private FileType(final Message localizableName, final String extension, final String typeAttribute, final String typeAttributeValue,
            final String moduleClassName, final String moduleInitializerClassName) {
        this.localizableName = localizableName;
        this.fileExtension = extension;
        this.typeAttribute = typeAttribute;
        this.typeAttributeValue = typeAttributeValue;
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
     * @return the file extension (is {@code null} if this type only exists for backward compatibility)
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
        xml.getDocumentElement().setAttribute(this.typeAttribute, this.typeAttributeValue);
    }
}
