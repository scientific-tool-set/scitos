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

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JPopupMenu;

import org.assertj.swing.core.Robot;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.edt.GuiQuery;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JPopupMenuFixture;
import org.assertj.swing.fixture.JTabbedPaneFixture;
import org.assertj.swing.fixture.JTreeFixture;
import org.assertj.swing.testing.AssertJSwingTestCaseTemplate;
import org.hmx.scitos.core.i18n.ILocalizableMessage;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.core.option.Option;
import org.hmx.scitos.view.FileType;
import org.hmx.scitos.view.swing.util.ToolTipComponentMatcher;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * AssertJ Swing test that initializes a {@link ScitosClient} and stores the underlying [@code JFrame} in a member variable. The clean-up afterwards
 * is also taken care of.
 */
public abstract class AbstractScitosUiTest extends AssertJSwingTestCaseTemplate {

    /**
     * The default modifier key mask (on most systems {@link InputEvent#CTRL_DOWN_MASK}, e.g. on Mac OS X {@link InputEvent#META_DOWN_MASK} i.e. the
     * command button).
     */
    protected final int menuShortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    /** The underlying swing JFrame to test. */
    protected FrameFixture frame;
    /** The main view's project tree component, listing the currently open project – including potential sub entries. */
    protected JTreeFixture projectTree;
    /** The main view's tabbed pane component, displaying the actual module views. */
    protected JTabbedPaneFixture tabbedPane;

    /**
     * Installs a {@link FailOnThreadViolationRepaintManager} to catch violations of Swing threading rules.
     */
    @BeforeClass
    public static final void setUpOnce() {
        Assume.assumeFalse("Automated UI Test cannot be executed in headless environment", GraphicsEnvironment.isHeadless());
        FailOnThreadViolationRepaintManager.install();
    }

    /**
     * Sets up this test's fixture, starting from creation of a new <code>{@link Robot}</code>.
     *
     * @see #setUpRobot()
     * @see #onSetUp()
     */
    @Before
    public final void setUp() {
        setUpRobot();
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
        this.frame.resizeTo(new Dimension(1000, 700));
        this.projectTree = this.frame.tree("Project Tree");
        this.tabbedPane = this.frame.tabbedPane("Project View Tab Stack");
        onSetUp();
    }

    /**
     * Subclasses that need to set up their own test fixture in this method. This method is called as <strong>last action</strong> during
     * {@link #setUp()}.
     */
    protected void onSetUp() {
        // default: everything is already set up
    }

    /**
     * Create a new file via the welcome tab (if currently displayed) or the main tool bar's respective button (and potentially displayed popup menu).
     *
     * @param type
     *            designated file type
     */
    protected void createNewFile(final FileType type) {
        // check if the Welcome tab with its dedicated 'New File' buttons is visible
        final JButton welcomeTabNewButton = this.robot().finder()
                .find(JButtonMatcher.withText(Message.MENUBAR_FILE_NEW.get() + " : " + type.getLocalizableName().get()));
        if (welcomeTabNewButton.isVisible()) {
            // click on the dedicated 'New File' button
            new JButtonFixture(this.robot(), welcomeTabNewButton).click();
        } else {
            // click on the 'create new file' button in the main tool bar
            this.getToolBarButtonByToolTip(Message.MENUBAR_FILE_NEW).click();
            // check whether a popup is being shown
            final JPopupMenu shownPopup = this.robot().findActivePopupMenu();
            // we're done if no show popup has been opened
            if (shownPopup != null) {
                // more than one module offering a 'create new file' feature is active, i.e. select the respective entry in the displayed popup
                new JPopupMenuFixture(this.robot(), shownPopup).menuItem(type.getLocalizableName().get()).click();
            }
        }
    }

    /**
     * Find the button with the given text label anywhere in the window.
     *
     * @param buttonText
     *            text label of the button to find
     * @return the matched button
     */
    protected final JButtonFixture getButtonByText(ILocalizableMessage buttonText) {
        final JButtonMatcher matcher = JButtonMatcher.withText(buttonText.get());
        return this.frame.button(matcher);
    }

    /**
     * Find the button on the main tool bar with the given tool tip (as it's assumed to have no label, but an icon and a tool tip).
     *
     * @param buttonToolTip
     *            tool tip of the button to find
     * @return the matched tool bar button
     */
    protected final JButtonFixture getToolBarButtonByToolTip(ILocalizableMessage buttonToolTip) {
        final ToolTipComponentMatcher<JButton> matcher = new ToolTipComponentMatcher<JButton>(JButton.class, buttonToolTip.get(), true);
        return this.frame.toolBar().button(matcher);
    }

    /**
     * Find the "Undo" button in the main tool bar.
     *
     * @return the "Undo" tool bar button for reverting the latest performed action/change
     */
    protected final JButtonFixture getUndoToolBarButton() {
        return this.getToolBarButtonByToolTip(Message.MENUBAR_EDIT_UNDO);
    }

    /**
     * Find the "Redo" button in the main tool bar.
     *
     * @return the "Redo" tool bar button for restoring the last action/change reverted via "Undo"
     */
    protected final JButtonFixture getRedoToolBarButton() {
        return this.getToolBarButtonByToolTip(Message.MENUBAR_EDIT_REDO);
    }

    /**
     * Cleans up any resources used in this test. After calling <code>{@link #onTearDown()}</code>, this method cleans up resources used by this
     * test's <code>{@link Robot}</code>.
     *
     * @see #cleanUp()
     * @see #onTearDown()
     */
    @After
    public final void tearDown() {
        try {
            onTearDown();
            this.frame = null;
            this.projectTree = null;
            this.tabbedPane = null;
            Option.TRANSLATION.setValue(null);
        } finally {
            cleanUp();
        }
    }

    /**
     * Subclasses that need to clean up resources can do so in this method. This method is called as <strong>first action</strong> during
     * {@link #tearDown()}.
     */
    protected void onTearDown() {
        // default: nothing more to tear down
    }
}
