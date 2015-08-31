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

import java.awt.Image;

import javax.swing.ImageIcon;

/**
 * Collection of icon resources.
 */
public enum ScitosIcon {
    /** Icon: SciToS logo. */
    APPLICATION("/icons/scitos_application.png"),
    /** Icon: SciToS logo in red for error message dialogs. */
    APPLICATION_ERROR("/icons/scitos_error.png"),
    /** Icon: SciToS logo in blue for info message or input dialogs. */
    APPLICATION_INFO("/icons/scitos_info.png"),
    /** Icon: SciToS logo in yellow/orange for warning message dialogs. */
    APPLICATION_WARN("/icons/scitos_warn.png"),
    /** Icon: open project in the main view's project tree. */
    PROJECT_OPEN("/icons/eclipse/project_open.png"),
    /** Icon: closed project in the main view's project tree. */
    PROJECT_CLOSED("/icons/eclipse/project_closed.png"),
    /** Icon: open model group folder in the main view's project tree. */
    FOLDER_OPEN("/icons/eclipse/folder_open.png"),
    /** Icon: closed model group folder in the main view's project tree. */
    FOLDER_CLOSED("/icons/eclipse/folder_closed.png"),
    /** Icon: model element in the main view's project tree. */
    MODEL_ELEMENT("/icons/fatcow/clipboard_invoice.png"),
    /** Icon: model element in the main view's project tree. */
    MODEL_ELEMENT_ADD("/icons/eclipse/add_task.png"),
    /** Icon: new file entry in menu bar / tool bar. */
    NEW_FILE("/icons/fatcow/page_white_add.png"),
    /** Icon: save file entry in menu bar / tool bar. */
    SAVE_FILE("/icons/fatcow/save.png"),
    /** Icon: save-as file entry in menu bar / tool bar. */
    SAVEAS_FILE("/icons/fatcow/save_as.png"),
    /** Icon: undo entry in menu bar / tool bar. */
    UNDO_EDIT("/icons/eclipse/undo_edit.png"),
    /** Icon: redo entry in menu bar / tool bar. */
    REDO_EDIT("/icons/eclipse/redo_edit.png"),
    /** Icon: vertical arrow pointing upwards for moving something up. */
    ARROW_UP("/icons/eclipse/arrow_up.png"),
    /** Icon: vertical arrow pointing downwards for moving something down. */
    ARROW_DOWN("/icons/eclipse/arrow_down.png"),
    /** Icon: config/preferences entry in menu bar. */
    CONFIG("/icons/fatcow/cog.png"),
    /** Icon: spreadsheet file export. **/
    SPREADSHEET("/icons/misc/ods.png"),
    /** Icon: add entry e.g. in category tree table **/
    ADD("/icons/fatcow/add.png"),
    /** Icon: remove entry e.g. in category tree table **/
    DELETE("/icons/fatcow/cross.png"),
    /** Icon: add entry e.g. in category tree table **/
    CATEGORY("/icons/fatcow/clipboard_invoice.png");

    /** Location of the represented icon in the classpath. */
    private final String resourcePath;

    /**
     * Main constructor.
     *
     * @param resourcePath
     *            the location of the represented icon in the classpath
     */
    private ScitosIcon(final String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /**
     * Load the actual image.
     *
     * @return created icon instance
     */
    public ImageIcon create() {
        return new ImageIcon(ScitosIcon.class.getResource(this.resourcePath));
    }

    /**
     * Load the actual image and scale it to the given size. If either width or height is a negative number, then a value is substituted to maintain
     * the aspect ratio of the original image dimensions.
     *
     * @param width
     *            the width to which to scale the image.
     * @param height
     *            the height to which to scale the image.
     *
     * @return created icon instance
     */
    public ImageIcon createScaled(final int width, final int height) {
        final ImageIcon original = new ImageIcon(ScitosIcon.class.getResource(this.resourcePath));
        final Image scaled = original.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    /**
     * Getter for the location of the represented icon in the classpath.
     *
     * @return the image resource' path
     */
    public String getResourcePath() {
        return this.resourcePath;
    }
}
