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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.core.option.HmxGeneralOption;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.view.swing.HmxSwingProject;
import org.hmx.scitos.hmx.view.swing.option.FontChooser;
import org.hmx.scitos.view.swing.components.ScaledTextField;
import org.hmx.scitos.view.swing.components.ScaledTextPane;
import org.hmx.scitos.view.swing.util.ViewUtil;

/**
 * Dialog offering the opportunity to edit the stored title, author and comment as well as the origin text's {@link Font} of a {@link Pericope}.
 */
public final class ProjectInfoDialog extends JDialog {

    private static final Border BORDER = BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(3, 3, 3, 3));
    /**
     * The project to modify the title, author, comment, and origin text {@link Font} for.
     */
    private final HmxSwingProject project;
    /**
     * If the origin text's {@link Font} should be editable as well.
     */
    private final boolean analysisInProgress;
    /** The container for the settings components. */
    final JPanel contentPane = new JPanel(new GridBagLayout());
    /**
     * The input field for the {@link Pericope}'s title.
     */
    private final JTextField titleField = new ScaledTextField();
    /**
     * The input field for the {@link Pericope}'s author.
     */
    private final JTextField authorField = new ScaledTextField();
    /**
     * The input area for the {@link Pericope}'s comment.
     */
    private final JTextPane commentArea = new ScaledTextPane();
    /**
     * The component for setting the origin text {@link Font}.
     */
    private FontChooser fontChooser = null;

    /**
     * Constructor.
     *
     * @param project
     *            project representing the {@link Pericope} that should be edited in this dialog
     * @param analysisInProgress
     *            if the analysis is already in progress (i.e. if the origin text's {@link Font} should be editable as well)
     */
    public ProjectInfoDialog(final HmxSwingProject project, final boolean analysisInProgress) {
        super(project.getFrame(), HmxMessage.PROJECTINFO_FRAME_TITLE.get(), false);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setAlwaysOnTop(true);
        this.project = project;
        this.analysisInProgress = analysisInProgress;
        this.initContent();
        final Dimension minimumSize;
        if (analysisInProgress) {
            minimumSize = new Dimension(400, 500);
        } else {
            minimumSize = new Dimension(350, 300);
        }
        this.setSize(minimumSize);
        this.setMinimumSize(minimumSize);
        ViewUtil.centerOnParent(this);
    }

    /** Initialize the input text fields, presets them with the currently stored values and add the buttons for OK and CANCEL. */
    private void initContent() {
        this.initInput();
        this.initButtons();
        this.contentPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
        final JScrollPane scrollableContent = new JScrollPane(this.contentPane);
        scrollableContent.setBorder(null);
        scrollableContent.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableContent.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        final JPanel background = new JPanel(new GridBagLayout());
        background.setBorder(null);
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        background.add(scrollableContent, constraints);
        this.setContentPane(background);
        this.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(final ComponentEvent event) {
                ProjectInfoDialog.this.handleResizing();
            }
        });
    }

    /**
     * Adjust the {@link #contentPane}'s preferred size to fit the dialog's changed size.
     */
    void handleResizing() {
        int width = this.getSize().width - 20;
        if (this.fontChooser != null) {
            width = Math.min(width, this.fontChooser.getSize().width + 20);
        }
        this.contentPane.setPreferredSize(new Dimension(width, this.contentPane.getPreferredSize().height));
    }

    /** Initialize the input text fields and presets them with the currently stored values. */
    private void initInput() {
        // initialize the title input field
        final JPanel titlePanel = new JPanel(new GridLayout(0, 1));
        titlePanel.setBorder(BorderFactory.createTitledBorder(HmxMessage.PROJECTINFO_TITLE.get()));
        this.titleField.setText(this.project.getModelObject().getTitle());
        this.titleField.setBorder(ProjectInfoDialog.BORDER);
        titlePanel.add(this.titleField);

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        this.contentPane.add(titlePanel, constraints);
        // initializes the author input field
        final JPanel authorPanel = new JPanel(new GridLayout(0, 1));
        authorPanel.setBorder(BorderFactory.createTitledBorder(HmxMessage.PROJECTINFO_AUTHOR.get()));
        if (this.project.getModelObject().getAuthor() == null) {
            this.authorField.setText(HmxGeneralOption.AUTHOR.getValue());
        } else {
            this.authorField.setText(this.project.getModelObject().getAuthor());
        }
        this.authorField.setBorder(ProjectInfoDialog.BORDER);
        authorPanel.add(this.authorField);

        constraints.gridy = 1;
        this.contentPane.add(authorPanel, constraints);
        // initializes the comment input area
        final JPanel commentPanel = new JPanel(new GridLayout(0, 1));
        commentPanel.setBorder(BorderFactory.createTitledBorder(HmxMessage.PROJECTINFO_COMMENT.get()));
        this.commentArea.setText(this.project.getModelObject().getComment());
        this.commentArea.setBorder(null);
        final JScrollPane scrollableComment = new JScrollPane(this.commentArea);
        scrollableComment.setBorder(ProjectInfoDialog.BORDER);
        scrollableComment.setMinimumSize(this.commentArea.getPreferredSize());
        commentPanel.add(scrollableComment);
        constraints.weighty = 1;
        constraints.gridy = 2;
        this.contentPane.add(commentPanel, constraints);
        if (this.analysisInProgress) {
            constraints.gridy = 3;
            final Font pericopeFont = this.project.getModelObject().getFont();
            this.fontChooser = new FontChooser();
            this.fontChooser.setSelection(pericopeFont.getFamily(), pericopeFont.getSize());
            this.contentPane.add(this.fontChooser, constraints);
        }
    }

    /** Initialize the OK and CANCEL buttons on the bottom of the dialog. */
    private void initButtons() {
        final JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 30, 10, 30));
        // creates the CANCEL button
        final JButton cancelButton = new JButton(Message.CANCEL.get());
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                ProjectInfoDialog.this.dispose();
            }
        });
        buttonPanel.add(cancelButton);
        // creates the spacing between the buttons
        final GridBagConstraints spacing = new GridBagConstraints();
        spacing.fill = GridBagConstraints.HORIZONTAL;
        spacing.weightx = 1;
        spacing.gridx = 1;
        buttonPanel.add(new JPanel(), spacing);
        // creates the OK button
        final JButton okButton = new JButton(Message.OK.get());
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                ProjectInfoDialog.this.saveAndClose();
            }
        });
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 2;
        buttonPanel.add(okButton, constraints);
        // make sure the buttons have the same size
        final Dimension cancelSize = cancelButton.getPreferredSize();
        final Dimension okSize = okButton.getPreferredSize();
        final Dimension uniSize = new Dimension(Math.max(cancelSize.width, okSize.width), Math.max(cancelSize.height, okSize.height));
        cancelButton.setPreferredSize(uniSize);
        okButton.setPreferredSize(uniSize);

        constraints.gridx = 0;
        constraints.gridy = this.analysisInProgress ? 4 : 3;
        this.contentPane.add(buttonPanel, constraints);
    }

    /**
     * Save the current values of the input text fields in the {@link Pericope} and close this dialog afterwards.
     */
    void saveAndClose() {
        this.setVisible(false);
        final Font selectedFont;
        if (this.fontChooser == null) {
            selectedFont = this.project.getModelObject().getFont();
        } else {
            selectedFont = this.fontChooser.getSelection();
        }
        this.project.getModelHandler().setMetaData(this.titleField.getText(), this.authorField.getText(), this.commentArea.getText(),
                selectedFont.getFamily(), selectedFont.getSize());
        this.dispose();
    }
}
