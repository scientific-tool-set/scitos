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

import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JPopupMenu;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.edt.GuiQuery;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JPopupMenuFixture;
import org.assertj.swing.fixture.JTabbedPaneFixture;
import org.assertj.swing.fixture.JTreeFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.core.option.Option;
import org.hmx.scitos.view.FileType;
import org.hmx.scitos.view.swing.util.ToolTipComponentMatcher;

/**
 * AssertJ Swing test that initializes a {@link ScitosClient} and stores the underlying [@code JFrame} in a member variable. The clean-up afterwards
 * is also taken care of.
 */
public abstract class AbstractScitosUiTest extends AssertJSwingJUnitTestCase {

    /** The underlying swing JFrame to test. */
    protected FrameFixture frame;
    /** The main view's project tree component, listing the currently open project – including potential sub entries. */
    protected JTreeFixture projectTree;
    /** The main view's tabbed pane component, displaying the actual module views. */
    protected JTabbedPaneFixture tabbedPane;

    @Override
    protected void onSetUp() {
        // enforce the English Locale to make this test independent of the executing system's Locale
        Option.TRANSLATION.setValue(Locale.ENGLISH.toString());
        // initialize the graphical user interface
        ScitosClient client = GuiActionRunner.execute(new GuiQuery<ScitosClient>() {

            @Override
            protected ScitosClient executeInEDT() throws Exception {
                return ScitosApp.loadModulesAndShowClient();
            }
        });
        this.frame = new FrameFixture(this.robot(), client.getFrame());
        this.frame.show();
        this.projectTree = this.frame.tree("Project Tree");
        this.tabbedPane = this.frame.tabbedPane("Project View Tab Stack");
    }

    /**
     * Create a new file via the main tool bar's respective button (and potentially displayed popup menu).
     * 
     * @param type
     *            designated file type
     */
    protected void createNewFile(final FileType type) {
        // click on the 'create new file' button in the main tool bar
        this.frame.toolBar().button(new ToolTipComponentMatcher<JButton>(JButton.class, Message.MENUBAR_FILE_NEW.get(), true)).click();
        // check whether a popup is being shown
        final JPopupMenu shownPopup = this.robot().findActivePopupMenu();
        // we're done if no show popup has been opened
        if (shownPopup != null) {
            // more than one module offering a 'create new file' feature is active, i.e. select the respective entry in the displayed popup
            new JPopupMenuFixture(this.robot(), shownPopup).menuItem(type.getLocalizableName().get()).click();
        }
    }

    @Override
    protected void onTearDown() {
        super.onTearDown();
        this.frame = null;
        this.projectTree = null;
        this.tabbedPane = null;
        Option.TRANSLATION.setValue(null);
    }
}
