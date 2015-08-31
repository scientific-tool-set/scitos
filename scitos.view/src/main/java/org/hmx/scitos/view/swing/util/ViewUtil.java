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

package org.hmx.scitos.view.swing.util;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

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
        if (screenBounds.x + screenBounds.width < positionX + 50) {
            destX = screenBounds.x + screenBounds.width - 50;
        } else if (positionX < screenBounds.x) {
            destX = screenBounds.x;
        } else {
            destX = positionX;
        }
        final int positionY = target.getX();
        final int destY;
        if (screenBounds.y + screenBounds.height < positionY + 50) {
            destY = screenBounds.y + screenBounds.height - 50;
        } else if (positionY < screenBounds.y) {
            destY = screenBounds.y;
        } else {
            destY = positionY;
        }
        target.setLocation(destX, destY);
    }
}
