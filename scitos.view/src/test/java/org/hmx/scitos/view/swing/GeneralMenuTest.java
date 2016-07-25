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

package org.hmx.scitos.view.swing;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;

import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.fixture.JButtonFixture;
import org.hmx.scitos.core.i18n.Message;
import org.junit.Assert;
import org.junit.Test;

/** Tests for the menu bar, main tool bar, and their general options available. */
public class GeneralMenuTest extends AbstractScitosUiTest {

    /**
     * Test to ensure the existence of the menu bar and main tool bar.
     */
    @Test
    public void testMenuBarAndToolBarExistence() {
        // ensure the existence of the menu bar
        this.frame.menuItemWithPath(Message.MENUBAR_FILE.get(), Message.MENUBAR_FILE_OPEN.get()).requireEnabled();
        this.frame.menuItemWithPath(Message.MENUBAR_FILE.get(), Message.MENUBAR_FILE_SAVE.get()).requireDisabled();
        this.frame.menuItemWithPath(Message.MENUBAR_FILE.get(), Message.MENUBAR_FILE_SAVEAS.get()).requireDisabled();

        this.frame.menuItemWithPath(Message.MENUBAR_EDIT.get(), Message.MENUBAR_EDIT_UNDO.get()).requireDisabled();
        this.frame.menuItemWithPath(Message.MENUBAR_EDIT.get(), Message.MENUBAR_EDIT_REDO.get()).requireDisabled();

        this.frame.menuItemWithPath(Message.MENUBAR_VIEW.get(), Message.MENUBAR_VIEW_FONT_SCALE_UP.get()).requireEnabled();
        this.frame.menuItemWithPath(Message.MENUBAR_VIEW.get(), Message.MENUBAR_VIEW_FONT_SCALE_DOWN.get()).requireEnabled();
        this.frame.menuItemWithPath(Message.MENUBAR_VIEW.get(), Message.MENUBAR_VIEW_TOGGLE_PROJECT_TREE.get()).requireEnabled();

        // ensure the existence of the main tool bar
        final Collection<JButton> toolBarButtons = this.robot().finder().findAll(this.frame.toolBar().target(), JButtonMatcher.any());
        Assert.assertEquals(4, toolBarButtons.size());
        final Iterator<JButton> toolBarButtonIterator = toolBarButtons.iterator();
        new JButtonFixture(this.robot(), toolBarButtonIterator.next()).requireToolTip(Message.MENUBAR_FILE_NEW.get()).requireEnabled();
        new JButtonFixture(this.robot(), toolBarButtonIterator.next()).requireToolTip(Message.MENUBAR_FILE_SAVE.get()).requireDisabled();
        new JButtonFixture(this.robot(), toolBarButtonIterator.next()).requireToolTip(Message.MENUBAR_EDIT_UNDO.get()).requireDisabled();
        new JButtonFixture(this.robot(), toolBarButtonIterator.next()).requireToolTip(Message.MENUBAR_EDIT_REDO.get()).requireDisabled();
    }
}
