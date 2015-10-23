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
import org.hmx.scitos.hmx.view.swing.HmxSwingProject;
import org.hmx.scitos.hmx.view.swing.option.FontChooser;
import org.hmx.scitos.view.swing.components.ScaledTextField;
import org.hmx.scitos.view.swing.components.ScaledTextPane;
import org.hmx.scitos.view.swing.util.ViewUtil;

/**
 * small dialog offering the opportunity to read and edit the stored title, author and comment of a Pericope
 */
public final class ProjectInfoDialog extends JDialog {

    private static final Border BORDER = BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(3, 3, 3, 3));

    private final HmxSwingProject project;
    private final boolean analysisInProgress;
    final JPanel contentPane = new JPanel(new GridBagLayout());
    private final JTextField titleField = new ScaledTextField();
    private final JTextField authorField = new ScaledTextField();
    private final JTextPane commentArea = new ScaledTextPane();
    private FontChooser fontChooser = null;

    /**
     * creates a ProjectInfoDialog for the designated Pericope in the specified {@link HmxClient} to call a refresh of its title if the title has been
     * changed
     *
     * @param project
     *            {@link HmxSwingProject} containing the Pericope to edit title, author and comment of
     * @param analysisInProgress
     *            if the analysis is already in progress
     */
    public ProjectInfoDialog(final HmxSwingProject project, final boolean analysisInProgress) {
        super(project.getFrame(), HmxMessage.PROJECTINFO_FRAME_TITLE.get(), false);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setAlwaysOnTop(true);
        this.project = project;
        this.analysisInProgress = analysisInProgress;
        this.initContent();
        if (analysisInProgress) {
            this.setSize(400, 600);
            this.setMinimumSize(new Dimension(400, 500));
        } else {
            this.setSize(350, 300);
            this.setMinimumSize(new Dimension(350, 300));
        }
        ViewUtil.centerOnParent(this);
    }

    /**
     * initializes the input text fields, presets them with the currently stored values and adds the buttons for <code>Commit</code> and
     * <code>Cancel</code>
     */
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
     * initializes the input text fields and presets them with the currently stored values
     */
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

    /** initializes the OK and CANCEL buttons on the bottom of the dialog */
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
     * save the current values of the input text fields in the Pericope and close the frame afterwards
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

    void handleResizing() {
        int width = this.getSize().width - 20;
        if (this.fontChooser != null) {
            width = Math.min(width, this.fontChooser.getSize().width + 20);
        }
        this.contentPane.setPreferredSize(new Dimension(width, this.contentPane.getPreferredSize().height));
    }
}
