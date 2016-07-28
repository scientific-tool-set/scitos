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

package org.hmx.scitos.hmx.view.swing.components;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.core.UndoManager;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.domain.IModel;
import org.hmx.scitos.hmx.core.ILanguageModelProvider;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.domain.ISemanticalRelationProvider;
import org.hmx.scitos.hmx.domain.model.ClauseItem;
import org.hmx.scitos.hmx.domain.model.LanguageModel;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.domain.model.RelationTemplate;
import org.hmx.scitos.hmx.view.IPericopeView;
import org.hmx.scitos.hmx.view.swing.HmxSwingProject;
import org.hmx.scitos.hmx.view.swing.elements.ProjectInfoDialog;
import org.hmx.scitos.view.ScitosIcon;
import org.hmx.scitos.view.service.IModelParseServiceProvider;
import org.hmx.scitos.view.swing.AbstractProjectView;
import org.hmx.scitos.view.swing.MessageHandler;
import org.hmx.scitos.view.swing.MessageHandler.MessageType;
import org.hmx.scitos.view.swing.util.ViewUtil;

/**
 * The content of a tab in the main SciToS view representing a single project in the HermeneutiX module.
 */
public class SingleProjectView extends AbstractProjectView<HmxSwingProject, Pericope> {

    /**
     * Provider of available {@link RelationTemplate}s to be created/applied in the semantical analysis.
     */
    private final ISemanticalRelationProvider relationProvider;
    /** Service provider for saving and exporting the represented project/model to files. */
    private final IModelParseServiceProvider modelParseProvider;
    /** The undo manager for the whole model. */
    private UndoManager<Pericope> undoManager;
    /**
     * The currently active view component. This is either the {@link TextInputPanel} or {@link CombinedAnalysesPanel}.
     */
    JPanel activeView;
    /**
     * View specific Edit menu item for adding more {@link Proposition}s to this project which is already in progress (i.e. in analysis mode).
     */
    private JMenuItem addTextItem;
    /**
     * View specific Edit menu item for removing {@link Proposition}s from this project which is already in progress (i.e. in analysis mode).
     */
    private JMenuItem removeTextItem;
    /**
     * View specific Edit menu item for adding the contents of another project to this one which is already in progress (i.e. in analysis mode).
     */
    private JMenuItem mergeProjectItem;

    /**
     * Constructor.
     *
     * @param project
     *            the represented view project
     * @param languageModelProvider
     *            provider of available {@link LanguageModel}s to be selected when the represented project is setup initially
     * @param relationProvider
     *            provider of available {@link RelationTemplate}s to be created/applied in the semantical analysis
     * @param modelParseProvider
     *            service provider for saving and exporting the represented project/model to files
     */
    public SingleProjectView(final HmxSwingProject project, final ILanguageModelProvider languageModelProvider,
            final ISemanticalRelationProvider relationProvider, final IModelParseServiceProvider modelParseProvider) {
        super(project, project.getModelObject(), new BorderLayout());
        this.relationProvider = relationProvider;
        this.modelParseProvider = modelParseProvider;
        if (this.containsAnalysisData()) {
            this.undoManager = new UndoManager<Pericope>(this.getModel());
            this.activeView = new CombinedAnalysesPanel(this.getProject().getModelHandler(), this.relationProvider);
        } else {
            this.activeView = new TextInputPanel(this, true, languageModelProvider);
        }
        this.add(this.activeView);
    }

    /**
     * Check whether the current model contains any analysis related data - i.e. if information would be lost if the current model was displayed in a
     * {@link TextInputPanel} rather than a {@link CombinedAnalysesPanel}.
     * 
     * @return if any information besides the origin text is present
     */
    boolean containsAnalysisData() {
        if (!this.getModel().getFlatRelations().isEmpty()) {
            // the semantical analysis has already been started
            return true;
        }
        // we only have to check the top level propositions, if there is any non-top-level one we return true anyway
        for (final Proposition singleProposition : this.getModel().getText()) {
            if (!singleProposition.getPriorChildren().isEmpty() || !singleProposition.getLaterChildren().isEmpty()
                    || singleProposition.getPartAfterArrow() != null || singleProposition.getLabel() != null
                    || singleProposition.getSemTranslation() != null || singleProposition.getSynTranslation() != null
                    || singleProposition.getComment() != null) {
                return true;
            }
            for (final ClauseItem singleItem : singleProposition) {
                if (singleItem.getFunction() != null || singleItem.getFontStyle() != ClauseItem.Style.PLAIN || singleItem.getComment() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Initialize the analysis by referring to the chosen origin language and {@link Font}, opening the analysis view with the syntactical analysis.
     *
     * @param originText
     *            the user input that should be interpreted as the {@link Proposition}s to be analyzed
     * @param originLanguage
     *            the user selected language model associated with the {@code originText}, determining its text orientation and the applicable
     *            functions in the syntactical analysis
     * @param originTextFont
     *            the user defined font to display the {@code originText} with
     */
    public void startAnalysis(final String originText, final LanguageModel originLanguage, final Font originTextFont) {
        this.getModel().init(originText, originLanguage, originTextFont);
        this.undoManager = new UndoManager<Pericope>(this.getModel());
        this.goToAnalysisView();
        new ProjectInfoDialog(this.getProject(), false).setVisible(true);
    }

    /**
     * Prepend/append the given {@code originText} to the represented {@link Pericope}.
     *
     * @param originText
     *            the user input that should be interpreted as the {@link Proposition}s to be added
     * @param inFront
     *            if the new {@link Proposition}s should be inserted as the first in this {@link Pericope}; otherwise they will be appended as the
     *            last
     */
    public void addNewPropositions(final String originText, final boolean inFront) {
        this.getProject().getModelHandler().addNewPropositions(originText, inFront);
        this.goToAnalysisView();
    }

    /** Switch from the text-input mode to the analysis mode. If this is already in analysis mode, this method does nothing. */
    void goToAnalysisView() {
        if (this.activeView instanceof TextInputPanel) {
            this.remove(this.activeView);
            this.activeView = new CombinedAnalysesPanel(this.getProject().getModelHandler(), this.relationProvider);
            this.add(this.activeView);
            this.revalidate();
            this.manageMenuOptions();
        }
    }

    /** Switch from the analysis mode to the text-input mode. If this is already in text-input mode, this method does nothing. */
    void goToTextInputView() {
        if (this.activeView instanceof CombinedAnalysesPanel) {
            this.submitChangesToModel();
            this.remove(this.activeView);
            this.activeView = new TextInputPanel(this, false, null);
            this.add(this.activeView);
            this.revalidate();
            this.manageMenuOptions();
        }
    }

    /**
     * Parse another HermeneutiX file and prepend or append its contents. This will prompt the user to select the other file, decide if the content
     * should be inserted in front or in the back of the current {@link Proposition}s.
     */
    void mergeWithOtherPericope() {
        final File mergeTarget = ViewUtil.openFile(this.getProject().getFrame(), HmxMessage.MENUBAR_PROJECT_MERGE.get(), false);
        if (mergeTarget == null) {
            return;
        }
        try {
            final Entry<? extends IModel<?>, List<?>> modelToMergeWith = this.modelParseProvider.open(mergeTarget);
            if (!(modelToMergeWith.getKey() instanceof Pericope)) {
                MessageHandler.showMessage(HmxMessage.ERROR_MERGE_NOT_A_PERICOPE.get(), Message.ERROR.get(), MessageType.ERROR);
                return;
            }
            final int optionIndex =
                    MessageHandler.showOptionDialog(HmxMessage.MENUBAR_PROJECT_MERGE_POSITION.get(), HmxMessage.MENUBAR_PROJECT_MERGE.get(),
                            new String[] { HmxMessage.MENUBAR_PROJECT_MERGE_INFRONT.get(), HmxMessage.MENUBAR_PROJECT_MERGE_BEHIND.get(),
                                    Message.CANCEL.get() }, 1);
            if (optionIndex == 0 || optionIndex == 1) {
                // TODO allow user to merge language models instead of rejecting any differing ones
                this.getProject().getModelHandler().mergeWithOtherPericope((Pericope) modelToMergeWith.getKey(), optionIndex == 0);
            }
        } catch (final HmxException ex) {
            MessageHandler.showException(ex);
        }
    }

    /** Ensure the menu options' availability based on the current state of the represented project. */
    private void manageMenuOptions() {
        // handle general menu options
        this.getProject().manageMenuOptions();
        // handle view specific options
        if (this.addTextItem != null) {
            final boolean inAnalysisMode = this.activeView instanceof CombinedAnalysesPanel;
            this.addTextItem.setEnabled(inAnalysisMode);
            this.removeTextItem.setEnabled(inAnalysisMode);
            this.mergeProjectItem.setEnabled(inAnalysisMode);
        }
    }

    @Override
    public void refresh() {
        if (this.activeView instanceof CombinedAnalysesPanel) {
            ((CombinedAnalysesPanel) this.activeView).refresh();
        }
    }

    @Override
    public void submitChangesToModel() {
        if (this.activeView instanceof IPericopeView) {
            ((IPericopeView) this.activeView).submitChangesToModel();
        } else if (this.activeView instanceof TextInputPanel) {
            ((TextInputPanel) this.activeView).submitChangesToModel();
        }
    }

    @Override
    public boolean canUndo() {
        if (this.activeView instanceof TextInputPanel) {
            return ((TextInputPanel) this.activeView).canUndo();
        }
        if (this.undoManager == null) {
            return false;
        }
        return this.undoManager.canUndo();
    }

    @Override
    public boolean canRedo() {
        if (this.activeView instanceof TextInputPanel) {
            return ((TextInputPanel) this.activeView).canRedo();
        }
        return this.undoManager.canRedo();
    }

    @Override
    public void undo() {
        if (this.activeView instanceof TextInputPanel) {
            ((TextInputPanel) this.activeView).undo();
        } else {
            this.undoManager.undo();
        }
    }

    @Override
    public void redo() {
        if (this.activeView instanceof TextInputPanel) {
            ((TextInputPanel) this.activeView).redo();
        } else {
            this.undoManager.redo();
        }
    }

    @Override
    public List<JMenuItem> createEditMenuItems() {
        this.addTextItem = new JMenuItem(HmxMessage.MENUBAR_ORIGINTEXT_ADD.get(), ScitosIcon.ADD.create());
        this.addTextItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                SingleProjectView.this.goToTextInputView();
            }
        });
        this.removeTextItem = new JMenuItem(HmxMessage.MENUBAR_ORIGINTEXT_REMOVE.get(), ScitosIcon.DELETE.create());
        this.removeTextItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                if (SingleProjectView.this.activeView instanceof CombinedAnalysesPanel
                        && MessageHandler.Choice.YES == MessageHandler.showConfirmDialog(HmxMessage.MENUBAR_ORIGINTEXT_REMOVE_CONFIRM.get(),
                                HmxMessage.MENUBAR_ORIGINTEXT_REMOVE.get())) {
                    final List<Proposition> selection = ((CombinedAnalysesPanel) SingleProjectView.this.activeView).getSelectedPropositions(null);
                    try {
                        SingleProjectView.this.getProject().getModelHandler().removePropositions(selection);
                    } catch (final HmxException expected) {
                        // illegal selection of Propositions
                        MessageHandler.showException(expected);
                    }
                }
            }
        });
        this.mergeProjectItem = new JMenuItem(HmxMessage.MENUBAR_PROJECT_MERGE.get(), ScitosIcon.PROJECT_OPEN.create());
        this.mergeProjectItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                SingleProjectView.this.mergeWithOtherPericope();
            }
        });
        final JMenuItem projectInfoItem = new JMenuItem(HmxMessage.MENUBAR_PROJECTINFO.get(), ScitosIcon.CATEGORY.create());
        projectInfoItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                final boolean alreadyInProgress = SingleProjectView.this.activeView instanceof CombinedAnalysesPanel;
                new ProjectInfoDialog(SingleProjectView.this.getProject(), alreadyInProgress).setVisible(true);
            }
        });
        return Arrays.asList(this.addTextItem, this.removeTextItem, this.mergeProjectItem, null, projectInfoItem);
    }

    // @Override
    // public List<JMenuItem> createViewMenuItems() {
    // TODO add menu item to hide/show translation input fields
    // }

    // @Override
    // public List<Component> createToolBarItems() {
    // TODO add tool bar item to hide/show translation input fields
    // }
}
