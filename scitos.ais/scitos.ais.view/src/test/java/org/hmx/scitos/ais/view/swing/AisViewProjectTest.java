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

package org.hmx.scitos.ais.view.swing;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import org.assertj.swing.core.MouseButton;
import org.assertj.swing.core.TypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.fixture.JLabelFixture;
import org.assertj.swing.fixture.JOptionPaneFixture;
import org.assertj.swing.fixture.JPanelFixture;
import org.hmx.scitos.ais.core.i18n.AisMessage;
import org.hmx.scitos.ais.view.swing.components.TextTokenComponent;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.view.FileType;
import org.hmx.scitos.view.swing.AbstractScitosUiTest;
import org.hmx.scitos.view.swing.components.ScaledTable;
import org.hmx.scitos.view.swing.util.OrdinalComponentMatcher;
import org.hmx.scitos.view.swing.util.ToolTipComponentMatcher;
import org.junit.Assert;
import org.junit.Test;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;

/** UI test for a simple AIS project workflow. */
public class AisViewProjectTest extends AbstractScitosUiTest {

    /**
     * The default modifier key mask (on most systems {@link InputEvent#CTRL_DOWN_MASK}, e.g. on Mac OS X {@link InputEvent#META_DOWN_MASK} i.e. the
     * command button).
     */
    private final int menuShortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    /**
     * Test for a simple AIS project workflow with the following steps:
     * <ol>
     * <li>create a new/empty AIS project</li>
     * <li>add a first interview to the project (via button on project overview tab)</li>
     * <li>insert text for the first interview and go into scoring mode</li>
     * <li>assign and unassign detail categories via mouse (i.e. mouse selection and clicking on tool bar buttons)</li>
     * <li>add a second interview to the project for the same participant (via tool bar button)</li>
     * <li>insert text for the second interview and go into scoring mode</li>
     * <li>assign and unassign detail categories via keyboard shortcuts (i.e. selection via arrow keys and assigning details via shortcuts)</li>
     * <li>navigate back to the first interview via the project tree and undo/redo the last action</li>
     * <li>navigate to project overview via its tab and confirm displayed results</li>
     * </ol>
     */
    @Test
    public void testSimpleWorkflow() {
        // #1 create a new/empty AIS project
        this.createNewFile(FileType.AIS);
        this.projectTree.requireSelection(AisMessage.PROJECT_UNSAVED.get());
        // #2 add a first interview to the project (via button on project overview tab)
        this.frame.button(JButtonMatcher.withText(AisMessage.INTERVIEW_NEW.get())).click();
        JOptionPaneFixture participantIdDialog = this.frame.optionPane().requireMessage(AisMessage.INTERVIEW_NEW_PARTICIPANTID.get());
        final Lorem textGenerator = LoremIpsum.getInstance();
        final String participantOne = textGenerator.getNameFemale();
        participantIdDialog.textBox().setText(participantOne);
        participantIdDialog.okButton().click();
        this.projectTree.requireSelection(AisMessage.PROJECT_UNSAVED.get() + '/' + participantOne);
        // nothing should happen, as the input area is still blank
        this.frame.button(JButtonMatcher.withText(AisMessage.INTERVIEW_START_SCORING.get())).click();
        // #3 insert text for the first interview and go into scoring mode
        final String interviewTextOne = textGenerator.getWords(6);
        this.frame.textBox(new OrdinalComponentMatcher<JTextPane>(JTextPane.class, 0, true)).setText(interviewTextOne).enterText("\n");
        this.frame.button(JButtonMatcher.withText(AisMessage.INTERVIEW_START_SCORING.get())).click();
        final List<JPanelFixture> tokenComponents = this.getTextTokenComponents();
        Assert.assertEquals(6, tokenComponents.size());
        final String[] textOneTokens = interviewTextOne.trim().split("[\\s]+");
        for (int i = 0; i < 6; i++) {
            this.assertTextTokenState(tokenComponents.get(i), textOneTokens[i], null);
        }
        // #4 assign and unassign detail categories via mouse (i.e. mouse selection and clicking on tool bar buttons)
        this.assignDetailsToFirstInterview(textOneTokens);
        // #5 add an interview for another participant
        this.frame.toolBar().button(new ToolTipComponentMatcher<JButton>(JButton.class, AisMessage.INTERVIEW_NEW.get(), true)).click();
        participantIdDialog = this.frame.optionPane().requireMessage(AisMessage.INTERVIEW_NEW_PARTICIPANTID.get());
        final String participantTwo = textGenerator.getNameMale();
        participantIdDialog.textBox().setText(participantTwo);
        participantIdDialog.okButton().click();
        // #6 insert text for the second interview and go into scoring mode
        final String interviewTextTwo = textGenerator.getWords(6);
        this.frame.textBox(new OrdinalComponentMatcher<JTextPane>(JTextPane.class, 0, true)).setText(interviewTextTwo);
        this.frame.button(JButtonMatcher.withText(AisMessage.INTERVIEW_START_SCORING.get())).click();
        // #7 assign and unassign detail categories via keyboard shortcuts (i.e. selection via arrow keys and assigning details via shortcuts)
        final String[] textTwoTokens = interviewTextTwo.trim().split("[\\s]+");
        this.assignDetailsToSecondInterview(textTwoTokens);
        // #8 navigate back to the first interview via the project tree and undo the last action
        this.projectTree.clickPath(AisMessage.PROJECT_UNSAVED.get() + '/' + participantOne);
        // detail assignment: I1 - E3 -(I2 - ..I2)- ..E3 - ..E3
        this.frame.toolBar().button(new ToolTipComponentMatcher<JButton>(JButton.class, Message.MENUBAR_EDIT_UNDO.get(), true)).click();
        this.assertTextTokenStates_BeforeLastRemoval(textOneTokens);
        // #9 navigate to project overview via its tab and confirm displayed results
        this.tabbedPane.selectTab(AisMessage.PROJECT_UNSAVED.get());
        final String[] rowOne = new String[] { participantOne, "6", "2", "1", "1", "0", "0", "0", "1", "0", "0", "1", "0", "0" };
        final String[] rowTwo = new String[] { participantTwo, "5", "2", "1", "1", "0", "0", "0", "1", "0", "0", "1", "0", "0" };
        this.frame.table(new OrdinalComponentMatcher<ScaledTable>(ScaledTable.class, 0, true)).requireContents(new String[][] { rowOne, rowTwo });
    }

    /**
     * Assign and unassign detail categories via mouse (i.e. mouse selection and clicking on tool bar buttons). The resulting detail assignments
     * should represent the following structure: {@code Int1 Ext3 (Int2 ..Int2 __) ..Ext3}
     * 
     * @param tokenTexts
     *            texts of the interview's tokens
     */
    private void assignDetailsToFirstInterview(final String[] tokenTexts) {
        // detail assignment: I1 - __ - __ - __ - __ - __
        this.getTextTokenComponentLabel(0).click();
        this.frame.toolBar().button(JButtonMatcher.withText("Int1")).click();

        // detail assignment: I1 - __ - I2 - ..I2 - ..I2 - __
        this.robot().moveMouse(this.getTextTokenComponentLabel(2).target());
        this.robot().pressMouse(MouseButton.LEFT_BUTTON);
        this.robot().moveMouse(this.getTextTokenComponentLabel(4).target());
        this.robot().releaseMouseButtons();
        this.frame.toolBar().button(JButtonMatcher.withText("Int2")).click();

        // detail assignment: I1 - E3 -(I2 - ..I2)- ..E3 - ..E3
        this.robot().click(this.getTextTokenComponentLabel(1).target());
        this.robot().waitForIdle();
        this.robot().pressModifiers(this.menuShortcutMask);
        this.robot().moveMouse(this.getTextTokenComponentLabel(4).target());
        this.robot().pressMouse(MouseButton.LEFT_BUTTON);
        this.robot().moveMouse(this.getTextTokenComponentLabel(5).target());
        this.robot().releaseMouseButtons();
        this.robot().waitForIdle();
        this.robot().releaseModifiers(this.menuShortcutMask);
        this.robot().waitForIdle();
        this.frame.toolBar().button(JButtonMatcher.withText("Ext3")).click();
        this.assertTextTokenStates_BeforeLastRemoval(tokenTexts);

        // detail assignment: I1 - E3 -(I2 - ..I2 - __)- ..E3
        this.getTextTokenComponentLabel(4).click();
        this.frame.toolBar().button(JButtonMatcher.withText(AisMessage.SCORE_REMOVE.get())).click();
        this.assertTextTokenStates_Final(tokenTexts);
    }

    /**
     * Assign and unassign detail categories via keyboard shortcuts (i.e. selection via arrow keys and assigning details via shortcuts). The resulting
     * detail assignments should represent the following structure: {@code Int1 Ext3 (Int2 ..Int2 __) ..Ext3}
     * 
     * @param tokenTexts
     *            texts of the interview's tokens
     */
    private void assignDetailsToSecondInterview(final String[] tokenTexts) {
        // detail assignment: I1 - __ - __ - __ - __ - __
        // initial selection needs to be set via mouse
        this.getTextTokenComponentLabel(0).click();
        this.robot().pressAndReleaseKey(KeyEvent.VK_1);

        // detail assignment: I1 - E3 - ..E3 - ..E3 - ..E3 - ..E3
        this.robot().pressAndReleaseKey(KeyEvent.VK_RIGHT);
        this.robot().pressAndReleaseKey(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK, InputEvent.SHIFT_DOWN_MASK);
        this.robot().pressAndReleaseKey(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK, InputEvent.SHIFT_DOWN_MASK);
        this.robot().pressAndReleaseKey(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK, InputEvent.SHIFT_DOWN_MASK);
        this.robot().pressAndReleaseKey(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK, InputEvent.SHIFT_DOWN_MASK);
        this.robot().pressAndReleaseKey(KeyEvent.VK_E);

        // detail assignment: I1 - E3 -(I2 - ..I2)- ..E3 - ..E3
        this.robot().pressAndReleaseKey(KeyEvent.VK_LEFT);
        this.robot().pressAndReleaseKey(KeyEvent.VK_RIGHT);
        this.robot().pressAndReleaseKey(KeyEvent.VK_RIGHT);
        this.robot().pressAndReleaseKey(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK, InputEvent.SHIFT_DOWN_MASK);
        this.robot().pressAndReleaseKey(KeyEvent.VK_2);
        this.assertTextTokenStates_BeforeLastRemoval(tokenTexts);

        // detail assignment: I1 - E3 -(I2 - ..I2 - __)- ..E3
        this.robot().pressAndReleaseKey(KeyEvent.VK_RIGHT);
        this.robot().pressAndReleaseKey(KeyEvent.VK_DELETE);
        this.assertTextTokenStates_Final(tokenTexts);

        // detail assignment: I1 - E3 -(I2 - ..I2)- ..E3 - ..E3
        this.frame.menuItemWithPath(Message.MENUBAR_EDIT.get(), Message.MENUBAR_EDIT_UNDO.get()).click();
        this.assertTextTokenStates_BeforeLastRemoval(tokenTexts);

        // detail assignment: I1 - E3 -(I2 - ..I2 - __)- ..E3
        this.frame.menuItemWithPath(Message.MENUBAR_EDIT.get(), Message.MENUBAR_EDIT_REDO.get()).click();
        this.assertTextTokenStates_Final(tokenTexts);
    }

    /**
     * Getter for the text label on the component representing the n-th text token.
     * 
     * @param ordinal
     *            index of the designated text token (component)
     * @return the {@code TextTokenComponent}'s text label's fixture
     */
    private JLabelFixture getTextTokenComponentLabel(final int ordinal) {
        return this.frame.panel(new OrdinalComponentMatcher<TextTokenComponent>(TextTokenComponent.class, ordinal, true)).label("Text");
    }

    /**
     * Assert the final states of the currently visible interview's text tokens equals the following detail structure:
     * {@code Int1 Ext3 (Int2 ..Int2) ..Ext3 ..Ext3}
     * 
     * @param tokenTexts
     *            expected texts of the individual tokens
     */
    private void assertTextTokenStates_BeforeLastRemoval(final String[] tokenTexts) {
        final List<JPanelFixture> tokenComponents = this.getTextTokenComponents();
        this.assertTextTokenState(tokenComponents.get(0), tokenTexts[0], "Int1");
        this.assertTextTokenState(tokenComponents.get(1), tokenTexts[1], "Ext3");
        this.assertTextTokenState(tokenComponents.get(2), tokenTexts[2], true, false, "Int2");
        // the subsequent tokens of a multi-token-detail should not display the assigned detail category code (just visible via colored lines)
        this.assertTextTokenState(tokenComponents.get(3), tokenTexts[3], false, true, null);
        this.assertTextTokenState(tokenComponents.get(4), tokenTexts[4], null);
        this.assertTextTokenState(tokenComponents.get(5), tokenTexts[5], null);
    }

    /**
     * Assert the final states of the currently visible interview's text tokens equals the following detail structure:
     * {@code Int1 Ext3 (Int2 ..Int2 __) ..Ext3}
     * 
     * @param tokenTexts
     *            expected texts of the individual tokens
     */
    private void assertTextTokenStates_Final(final String[] tokenTexts) {
        final List<JPanelFixture> tokenComponents = this.getTextTokenComponents();
        this.assertTextTokenState(tokenComponents.get(0), tokenTexts[0], "Int1");
        this.assertTextTokenState(tokenComponents.get(1), tokenTexts[1], "Ext3");
        this.assertTextTokenState(tokenComponents.get(2), tokenTexts[2], true, false, "Int2");
        // the subsequent tokens of a multi-token-detail should not display the assigned detail category code (just visible via colored lines)
        this.assertTextTokenState(tokenComponents.get(3), tokenTexts[3], null);
        this.assertTextTokenState(tokenComponents.get(4), tokenTexts[4], false, true, null);
        this.assertTextTokenState(tokenComponents.get(5), tokenTexts[5], null);
    }

    /**
     * Getter for components representing text tokens (including their potentially assigned detail category).
     * 
     * @return {@code TextTokenComponent} fixtures
     */
    private List<JPanelFixture> getTextTokenComponents() {
        final Collection<Component> tokenComponents = this.robot().finder().findAll(new TypeMatcher(TextTokenComponent.class, true));
        final List<JPanelFixture> fixtures = new ArrayList<JPanelFixture>(tokenComponents.size());
        for (final Component singleComponent : tokenComponents) {
            fixtures.add(new JPanelFixture(this.robot(), (JPanel) singleComponent));
        }
        return fixtures;
    }

    /**
     * Assert a single text token component's properties, without expecting any opening or closing brackets.
     * 
     * @param textTokenComponent
     *            the text token component to assert
     * @param text
     *            token's expected text the component should display
     * @param detailCategory
     *            the assigned detail category (only expected to be shown on the first component in a detail range
     */
    private void assertTextTokenState(final JPanelFixture textTokenComponent, final String text, final String detailCategory) {
        this.assertTextTokenState(textTokenComponent, text, false, false, detailCategory);
    }

    /**
     * Assert a single text token component's properties.
     * 
     * @param textTokenComponent
     *            the text token component to assert
     * @param text
     *            token's expected text the component should display
     * @param isEnclosedStart
     *            whether the component represents the leading text token enclosed by another detail
     * @param isEnclosedEnd
     *            whether the component represents the trailing text token enclosed by another detail
     * @param detailCategory
     *            the assigned detail category (only expected to be shown on the first component in a detail range
     */
    private void assertTextTokenState(final JPanelFixture textTokenComponent, final String text, final boolean isEnclosedStart,
            final boolean isEnclosedEnd, final String detailCategory) {
        textTokenComponent.label("Text").requireText(text).requireVisible();
        final JLabelFixture detailLabel = textTokenComponent.label("Assigned Detail");
        if (detailCategory == null) {
            detailLabel.requireText(" ");
        } else {
            detailLabel.requireText(detailCategory);
        }

        final JLabelFixture openingBracketLabel = textTokenComponent.label(JLabelMatcher.withName("Indicator: First Token of Enclosed Detail"));
        if (isEnclosedStart) {
            openingBracketLabel.requireVisible();
        } else {
            openingBracketLabel.requireNotVisible();
        }
        final JLabelFixture closingBracketLabel = textTokenComponent.label(JLabelMatcher.withName("Indicator: Last Token of Enclosed Detail"));
        if (isEnclosedEnd) {
            closingBracketLabel.requireVisible();
        } else {
            closingBracketLabel.requireNotVisible();
        }
    }
}
