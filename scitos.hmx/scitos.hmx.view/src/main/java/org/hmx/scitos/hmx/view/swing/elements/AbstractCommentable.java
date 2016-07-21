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

package org.hmx.scitos.hmx.view.swing.elements;

import java.awt.LayoutManager;

import javax.swing.JPanel;

import org.hmx.scitos.hmx.domain.ICommentable;

/**
 * {@link JPanel} with two different borders to show whether it is the currently selected commentable element or not.
 *
 * @param <T>
 *            represented commentable model class
 */
public abstract class AbstractCommentable<T extends ICommentable> extends JPanel {

    /**
     * Constructor: applying the specified layout.
     *
     * @param layout
     *            {@link LayoutManager} to use
     */
    protected AbstractCommentable(final LayoutManager layout) {
        super(layout);
    }

    /**
     * Getter for the represented commentable model element.
     * 
     * @return represented model element
     */
    public abstract T getRepresented();

    /** Set the default border to show that it is not the currently selected element. */
    public abstract void setDefaultBorder();

    /** Set the lowered comment border to show that it is the currently selected element. */
    public abstract void setCommentBorder();
}
