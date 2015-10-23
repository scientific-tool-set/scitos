package org.hmx.scitos.hmx.view.swing.elements;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;

import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.hmx.core.export.SvgFactory;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.view.swing.HmxSwingProject;
import org.hmx.scitos.view.service.IModelParseServiceProvider;
import org.hmx.scitos.view.swing.MessageHandler;
import org.hmx.scitos.view.swing.MessageHandler.MessageType;
import org.hmx.scitos.view.swing.util.ViewUtil;
import org.w3c.dom.Document;

/**
 *
 */
public class SvgExportDetailsDialog extends JDialog {

    private final Pericope model;
    private final IModelParseServiceProvider modelParseProvider;

    private final JCheckBox includeCommentsCheckBox;
    private final JRadioButton syntacticalButton;
    private final JRadioButton semanticalButton;
    private final JRadioButton commentsButton;
    private final JRadioButton commentsAllButton;
    private final JRadioButton commentsSynButton;
    private final JRadioButton commentsSemButton;

    /**
     * Constructor.
     * 
     * @param project
     *            the project that is being exported
     * @param modelParseProvider
     *            the service provider for saving the generated SVG document as a file
     */
    public SvgExportDetailsDialog(final HmxSwingProject project, final IModelParseServiceProvider modelParseProvider) {
        super(project.getFrame(), HmxMessage.EXPORT_TYPE_SVG.get());
        this.model = project.getModelObject().clone();
        this.modelParseProvider = modelParseProvider;
        this.setModal(true);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        final JPanel contentPane = new JPanel(new GridBagLayout());

        final JPanel contentSelectionGroup = new JPanel(new GridLayout(3, 1));
        contentSelectionGroup.setBorder(BorderFactory.createTitledBorder(HmxMessage.EXPORT_CONTENT.get()));
        this.syntacticalButton = new JRadioButton(HmxMessage.EXPORT_CONTENT_SYNTACTICAL.get(), true);
        this.semanticalButton = new JRadioButton(HmxMessage.EXPORT_CONTENT_SEMANTICAL.get());
        this.commentsButton = new JRadioButton(HmxMessage.EXPORT_CONTENT_COMMENTS.get());
        contentSelectionGroup.add(this.syntacticalButton);
        contentSelectionGroup.add(this.semanticalButton);
        contentSelectionGroup.add(this.commentsButton);
        final ButtonGroup contentSelectionButtons = new ButtonGroup();
        contentSelectionButtons.add(this.syntacticalButton);
        contentSelectionButtons.add(this.semanticalButton);
        contentSelectionButtons.add(this.commentsButton);
        contentSelectionButtons.setSelected(this.syntacticalButton.getModel(), true);
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        contentPane.add(contentSelectionGroup, constraints);

        final JPanel commentTogglerGroup = new JPanel(new BorderLayout());
        commentTogglerGroup.setBorder(BorderFactory.createTitledBorder(HmxMessage.EXPORT_SETTINGS.get()));
        constraints.gridy++;
        this.includeCommentsCheckBox = new JCheckBox(HmxMessage.EXPORT_SETTINGS_INCLUDE_COMMENTS.get(), true);
        commentTogglerGroup.add(this.includeCommentsCheckBox);
        contentPane.add(commentTogglerGroup, constraints);

        final JPanel commentInclusionGroup = new JPanel(new GridLayout(3, 1));
        commentInclusionGroup.setBorder(BorderFactory.createTitledBorder(HmxMessage.EXPORT_SETTINGS.get()));
        this.commentsAllButton = new JRadioButton(HmxMessage.EXPORT_COMMENTS_WHICH_ALL.get(), true);
        this.commentsSynButton = new JRadioButton(HmxMessage.EXPORT_COMMENTS_WHICH_SYNTACTICAL.get());
        this.commentsSemButton = new JRadioButton(HmxMessage.EXPORT_COMMENTS_WHICH_SEMANTICAL.get());
        commentInclusionGroup.add(this.commentsAllButton);
        commentInclusionGroup.add(this.commentsSynButton);
        commentInclusionGroup.add(this.commentsSemButton);
        final ButtonGroup whichCommentButtons = new ButtonGroup();
        whichCommentButtons.add(this.commentsAllButton);
        whichCommentButtons.add(this.commentsSynButton);
        whichCommentButtons.add(this.commentsSemButton);
        whichCommentButtons.setSelected(this.commentsAllButton.getModel(), true);
        contentPane.add(commentInclusionGroup, constraints);
        constraints.gridy++;
        constraints.weighty = 1;
        contentPane.add(new JLabel(), constraints);

        final JButton cancelButton = new JButton(Message.CANCEL.get());
        constraints.gridwidth = 1;
        constraints.gridy++;
        constraints.weighty = 0;
        contentPane.add(cancelButton, constraints);
        final JButton exportButton = new JButton(HmxMessage.EXPORT_START.get());
        constraints.gridx = 2;
        contentPane.add(exportButton, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        contentPane.add(new JLabel(), constraints);

        final ActionListener showCommentTogglerListener = new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                commentTogglerGroup.setVisible(true);
                commentInclusionGroup.setVisible(false);
            }
        };
        this.syntacticalButton.addActionListener(showCommentTogglerListener);
        this.semanticalButton.addActionListener(showCommentTogglerListener);
        this.commentsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                commentTogglerGroup.setVisible(false);
                commentInclusionGroup.setVisible(true);
            }
        });
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                SvgExportDetailsDialog.this.dispose();
            }
        });
        exportButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                try {
                    if (SvgExportDetailsDialog.this.executeExport()) {
                        SvgExportDetailsDialog.this.dispose();
                        MessageHandler.showMessage(HmxMessage.EXPORT_SUCCESS.get(), HmxMessage.EXPORT_TYPE_SVG.get(), MessageType.INFO);
                    }
                } catch (final HmxException ex) {
                    MessageHandler.showException(ex);
                }
            }
        });
        this.setContentPane(contentPane);
        this.pack();
        commentInclusionGroup.setVisible(false);
        this.setResizable(false);
        ViewUtil.centerOnParent(this);
    }

    boolean executeExport() throws HmxException {
        final Document svg;
        if (this.syntacticalButton.isSelected()) {
            svg = SvgFactory.generateSyntacticalSvg(this.model, this.includeCommentsCheckBox.isSelected());
        } else if (this.semanticalButton.isSelected()) {
            svg = SvgFactory.generateSemanticalSvg(this.model, this.includeCommentsCheckBox.isSelected());
        } else {
            svg = SvgFactory.generateCommentsSvg(this.model, !this.commentsSynButton.isSelected(), !this.commentsSemButton.isSelected());
        }
        final File target = ViewUtil.getSaveDestination((JFrame) this.getParent(), ".svg", Message.MENUBAR_FILE_EXPORT.get(), false);
        if (target == null) {
            return false;
        }
        this.modelParseProvider.save(svg, target);
        return true;
    }
}
