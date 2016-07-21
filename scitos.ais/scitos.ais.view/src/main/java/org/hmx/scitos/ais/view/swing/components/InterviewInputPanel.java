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

package org.hmx.scitos.ais.view.swing.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.undo.UndoManager;

import org.hmx.scitos.ais.core.i18n.AisMessage;
import org.hmx.scitos.ais.domain.model.TextToken;
import org.hmx.scitos.view.swing.IUndoManagedView;
import org.hmx.scitos.view.swing.components.ScaledLabel;
import org.hmx.scitos.view.swing.components.ScaledTextPane;

/**
 * Part of the {@link InterviewView}, that is allowing to insert/modify the interview's text while no scoring (i.e. detail category assignments) has
 * been applied yet. A button enables the user to start the actual scoring – thereby replacing this panel by the {@link InterviewScoringPanel}.
 */
public final class InterviewInputPanel extends JPanel implements IUndoManagedView {

    /** Standard swing un-do manager for the input text pane, to allow the un-do and re-do of textual changes. */
    private final UndoManager undoManager;
    /** The interview view this is a part of. */
    private final InterviewView parentView;
    /** The actual text pane to insert/modify the interview's text in. */
    final ScaledTextPane inputPane = new ScaledTextPane();

    /**
     * Main constructor.
     *
     * @param parentView
     *            the interview displaying (tab) view this panel belongs to
     */
    public InterviewInputPanel(final InterviewView parentView) {
        super(new GridBagLayout());
        this.parentView = parentView;
        // insert a hint label on the top of the input view
        final ScaledLabel hintLabel = new ScaledLabel(AisMessage.INTERVIEW_TEXTINPUT_HINT.get());
        hintLabel.setToolTipText(AisMessage.INTERVIEW_TEXTINPUT_HINT.get());
        hintLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.BASELINE_LEADING;
        constraints.weightx = 1;
        this.add(hintLabel, constraints);
        // insert the button for starting the actual scoring (i.e. replacing this input panel with the scoring panel)
        final JButton button = new JButton(AisMessage.INTERVIEW_START_SCORING.get());
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                // ignore button clicks, if there is no text defined
                if (!InterviewInputPanel.this.inputPane.getText().trim().isEmpty()) {
                    parentView.initScoringMode();
                }
            }
        });
        constraints.anchor = GridBagConstraints.BASELINE_TRAILING;
        constraints.weightx = 0;
        constraints.gridx = 1;
        this.add(button, constraints);
        // make sure the input text pane is scrollable
        final JScrollPane scrollableInput = new JScrollPane(this.inputPane);
        scrollableInput.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollableInput.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableInput.setBorder(null);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        this.add(scrollableInput, constraints);

        if (!this.parentView.getModel().getText().isEmpty()) {
            final StringBuilder text = new StringBuilder();
            for (final TextToken existingParagraph : this.parentView.getModel().getText()) {
                TextToken currentToken = existingParagraph;
                do {
                    text.append(currentToken.getText());
                    text.append(' ');
                    currentToken = currentToken.getFollowingToken();
                } while (currentToken != null);
                // remove excess whitespace
                text.deleteCharAt(text.length() - 1);
                text.append('\n');
            }
            // remove excess line break
            text.deleteCharAt(text.length() - 1);
            // insert text
            this.inputPane.setText(text.toString());
        }

        // configure the input text pane: to have 5px insets and line spacing of one-and-a-half
        this.inputPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        final MutableAttributeSet attributes = this.inputPane.getInputAttributes();
        StyleConstants.setLineSpacing(attributes, 1.5f);
        final StyledDocument document = this.inputPane.getStyledDocument();
        document.setCharacterAttributes(0, document.getLength() + 1, attributes, true);
        // manage undo / redo with the normal swing UndoManager
        this.undoManager = new UndoManager() {

            @Override
            public void undoableEditHappened(final UndoableEditEvent event) {
                super.undoableEditHappened(event);
                parentView.revalidateClient();
            }
        };
        document.addUndoableEditListener(this.undoManager);
    }

    @Override
    public void submitChangesToModel() {
        this.parentView.getProject().getModelHandler().setInterviewText(this.parentView.getModel(), this.inputPane.getText());
    }

    @Override
    public void refresh() {
        // nothing to refresh here
    }

    @Override
    public boolean canUndo() {
        return this.undoManager.canUndo();
    }

    @Override
    public boolean canRedo() {
        return this.undoManager.canRedo();
    }

    @Override
    public void undo() {
        this.undoManager.undo();
    }

    @Override
    public void redo() {
        this.undoManager.redo();
    }

}
