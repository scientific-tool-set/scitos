/*
   Copyright (C) 2017 HermeneutiX.org

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

package org.hmx.scitos.hmx.view.swing;

import org.assertj.swing.fixture.JPanelFixture;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.view.FileType;
import org.hmx.scitos.view.swing.AbstractScitosUiTest;
import org.hmx.scitos.view.swing.util.OrdinalComponentMatcher;
import org.junit.Test;

import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.view.swing.elements.SynProposition;

/** UI test for a simple HermeneutiX project workflow. */
public class HmxViewProjectTest extends AbstractScitosUiTest {

    /**
     * Test of the input view of a newly created HermeneutiX project with the following steps:
     * <ol>
     * <li>create a new/empty HermeneutiX project,</li>
     * <li>enter a single character as origin text,</li>
     * <li>trigger "Undo",</li>
     * <li>trigger "Redo".</li>
     * </ol>
     */
    @Test
    public void testUndoRedoOnInputView() {
        // #1 create a new/empty HermeneutiX project
        this.createNewFile(FileType.HMX);
        this.projectTree.requireSelection(HmxMessage.PROJECT_UNSAVED.get());
        // #2 enter a single character as origin text
        final JTextComponentFixture originTextPane = this.frame.textBox("Origin Text Input");
        originTextPane.enterText("X");
        // #3 trigger "Undo"
        final JButtonFixture undoButton = this.getUndoToolBarButton();
        final JButtonFixture redoButton = this.getRedoToolBarButton();
        redoButton.requireDisabled();
        undoButton.requireEnabled().click().requireDisabled();
        originTextPane.requireText("");
        // #4 trigger "Redo"
        redoButton.requireEnabled().click().requireDisabled();
        originTextPane.requireText("X");
        undoButton.requireEnabled();
    }

    /**
     * Test of the analysis view of a newly created HermeneutiX project with the following steps:
     * <ol>
     * <li>create a new/empty HermeneutiX project,</li>
     * <li>enter a single character as origin text,</li>
     * <li>start analysis,</li>
     * <li>ignore project info input dialog,</li>
     * <li>enter single character as translation for first Proposition,</li>
     * <li>enter single character as label for first Proposition,</li>
     * <li>trigger "Undo" once,</li>
     * <li>trigger "Undo" a second time,</li>
     * <li>trigger "Redo" once,</li>
     * <li>trigger "Redo" a second time.</li>
     * </ol>
     */
    @Test
    public void testUndoRedoOnAnalysisView() {
        // #1 create a new/empty HermeneutiX project
        this.createNewFile(FileType.HMX);
        this.projectTree.requireSelection(HmxMessage.PROJECT_UNSAVED.get());
        // #2 enter a single character as origin text
        final JTextComponentFixture originTextPane = this.frame.textBox("Origin Text Input").enterText("X");
        // #3 start analysis
        this.getButtonByText(HmxMessage.TEXTINPUT_START_BUTTON).click();
        // #4 ignore project info input dialog
        this.getButtonByText(Message.CANCEL).click();
        // #5 enter single character as translation for first Proposition
        this.getSynPropositionTranslationInput(0).enterText("T");
        final JButtonFixture undoButton = this.getUndoToolBarButton().requireDisabled();
        final JButtonFixture redoButton = this.getRedoToolBarButton().requireDisabled();
        // #6 enter single character as label for first Proposition
        this.getSynPropositionLabelInput(0).enterText("L");
        // #7 trigger "Undo" once
        redoButton.requireDisabled();
        undoButton.requireEnabled().click().requireEnabled();
        this.getSynPropositionTranslationInput(0).requireText("T");
        this.getSynPropositionLabelInput(0).requireEmpty();
        // #8 trigger "Undo" a second time
        redoButton.requireEnabled();
        undoButton.requireEnabled().click().requireDisabled();
        this.getSynPropositionTranslationInput(0).requireEmpty();
        this.getSynPropositionLabelInput(0).requireEmpty();
        // #9 trigger "Redo" once
        redoButton.requireEnabled().click().requireEnabled();
        undoButton.requireEnabled();
        getSynPropositionTranslationInput(0).requireText("T");
        // #10 trigger "Redo" a second time
        redoButton.requireEnabled().click().requireDisabled();
        undoButton.requireEnabled();
        getSynPropositionTranslationInput(0).requireText("T");
        this.getSynPropositionLabelInput(0).requireText("L");
    }

    private JPanelFixture getSynProposition(final int index) {
        return this.frame.panel(new OrdinalComponentMatcher<SynProposition>(SynProposition.class, index, true));
    }

    private JTextComponentFixture getSynPropositionTranslationInput(final int Index) {
        return this.getSynProposition(Index).textBox("Translation Input");
    }

    private JTextComponentFixture getSynPropositionLabelInput(final int Index) {
        return this.getSynProposition(Index).textBox("Label Input");
    }
}
