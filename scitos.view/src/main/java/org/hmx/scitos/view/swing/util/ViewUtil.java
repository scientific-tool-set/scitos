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

package org.hmx.scitos.view.swing.util;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JFrame;

import org.hmx.scitos.core.option.Option;

/** Helper class to handle recurring requirements in the user interface. */
public final class ViewUtil {

    /**
     * Modify the location of the given window to be centered on its parent window. If the window has no parent, this does nothing.
     *
     * @param target
     *            the window to be centered on its parent
     */
    public static void centerOnParent(final Window target) {
        if (target.getParent() != null) {
            final Rectangle parentBounds = target.getParent().getBounds();
            final Dimension targetSize = target.getSize();
            final int posX = parentBounds.x + (parentBounds.width - targetSize.width) / 2;
            final int posY = parentBounds.y + (parentBounds.height - targetSize.height) / 2;
            target.setLocation(posX, posY);
            ViewUtil.moveInsideScreen(target);
        }
    }

    /**
     * Modify the location of the given window to ensure it is inside the screen bounds.
     *
     * @param target
     *            the window to move (if necessary)
     */
    public static void moveInsideScreen(final Window target) {
        final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final Point targetCenter = new Point(target.getX() + target.getWidth() / 2, target.getY() + target.getHeight() / 2);
        for (final GraphicsDevice singleScreen : environment.getScreenDevices()) {
            final Rectangle screenBounds = singleScreen.getDefaultConfiguration().getBounds();
            if (screenBounds.contains(targetCenter)) {
                ViewUtil.moveInsideBounds(target, screenBounds);
                return;
            }
        }
        ViewUtil.moveInsideBounds(target, environment.getDefaultScreenDevice().getDefaultConfiguration().getBounds());
    }

    /**
     * Modify the location of the given window to ensure it is inside the specified bounds.
     *
     * @param target
     *            the window to move (if necessary)
     * @param screenBounds
     *            the bounds to display (at least of) the window inside
     */
    private static void moveInsideBounds(final Window target, final Rectangle screenBounds) {
        final int positionX = target.getX();
        final int destX;
        if (positionX < screenBounds.x) {
            destX = screenBounds.x;
        } else if (screenBounds.x + screenBounds.width < positionX + 50) {
            destX = screenBounds.x + screenBounds.width - 50;
        } else {
            destX = positionX;
        }
        final int positionY = target.getY();
        final int height = target.getHeight();
        final int destY;
        if (positionY < screenBounds.y) {
            destY = screenBounds.y;
        } else if (screenBounds.y + screenBounds.height < positionY + height + 50) {
            destY = screenBounds.y + screenBounds.height - height - 50;
        } else {
            destY = positionY;
        }
        target.setLocation(destX, destY);
    }

    /**
     * Show a file dialog to allow the user to select a single file to be opened.
     *
     * @param parent
     *            the application window the displayed file dialog belongs to
     * @param title
     *            the dialog title
     * @param rememberDirectory
     *            if the containing directory of a successful selection should be remembered as {@link Option#WORKDIR}
     * @return the selected file to be opened (is {@code null} if the user aborted the dialog)
     */
    public static File openFile(final JFrame parent, final String title, final boolean rememberDirectory) {
        // use FileDialog instead of JFileChooser because it looks more native
        final FileDialog dialog = new FileDialog(parent, title, FileDialog.LOAD);
        dialog.setDirectory(Option.WORKDIR.getValue());
        ViewUtil.centerOnParent(dialog);
        dialog.setVisible(true);
        final String selection = dialog.getFile();
        if (selection == null) {
            // user aborted the file choosing dialog
            return null;
        }
        final File result = new File(dialog.getDirectory(), selection);
        if (rememberDirectory) {
            // store the default work directory
            Option.WORKDIR.setValue(result.getParentFile().getAbsolutePath());
        }
        // open the selected file
        return result;
    }

    /**
     * Show a file dialog to allow the user to insert a file destination to save to.
     *
     * @param parent
     *            the application window the displayed file dialog belongs to
     * @param fileExtension
     *            associated file type extension
     * @param title
     *            title of the file dialog
     * @param rememberDirectory
     *            if the containing directory of a successful selection should be remembered as {@link Option#WORKDIR}
     * @return the selected save destination (is {@code null} if the user aborted the dialog)
     */
    public static File getSaveDestination(final JFrame parent, final String fileExtension, final String title, final boolean rememberDirectory) {
        // use FileDialog instead of JFileChooser because it looks more native
        final FileDialog dialog = new FileDialog(parent, title, FileDialog.SAVE);
        dialog.setDirectory(Option.WORKDIR.getValue());
        dialog.setFilenameFilter(new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
                return new File(dir, name).isDirectory() || name.endsWith(fileExtension);
            }
        });
        ViewUtil.centerOnParent(dialog);
        dialog.setVisible(true);
        if (dialog.getFile() != null) {
            if (rememberDirectory) {
                // store the default work directory
                Option.WORKDIR.setValue(dialog.getDirectory());
            }
            // make sure the target has the given extension
            final String path = dialog.getDirectory() + dialog.getFile();
            if (path.matches(".*\\" + fileExtension)) {
                // the FileDialog already asked to replace an existing file with the same name
                return new File(path);
            }
            // we have to append the file extension
            File targetFile = new File(path + fileExtension);
            // avoid replacing an existing file - since the dialog is already closed, we cannot ask the user again
            for (int i = 1; targetFile.exists(); i++) {
                // add a counter to the end of the file name
                final StringBuilder pathBuilder = new StringBuilder(path).append('(').append(i).append(')').append(fileExtension);
                targetFile = new File(pathBuilder.toString());
            }
            return targetFile;
        }
        return null;
    }
}
