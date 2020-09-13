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
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;

import org.hmx.scitos.core.HmxException;
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
import org.hmx.scitos.view.swing.IUndoManagedView;
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
    /**
     * Service provider for saving and exporting the represented project/model to files.
     */
    private final IModelParseServiceProvider modelParseProvider;
    /**
     * User settings determining what parts of the model should be displayed.
     */
    private final AnalysisViewSettings viewSettings = new AnalysisViewSettings().applyViewPreset(IAnalysisViewSettings.SYNTACTICAL_ANALYSIS);
    /**
     * The currently active view component. This is either the {@link TextInputPanel} or {@link AnalysisPanel}.
     */
    IUndoManagedView activeView;
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
    /** View specific View menu item for toggling the visibility of proposition labels. */
    private JCheckBoxMenuItem viewPropositionLabelMenuItem;
    /** View specific View menu item for toggling the visibility of proposition indentations. */
    private JCheckBoxMenuItem viewPropositionIndentationMenuItem;
    /** View specific View menu item for toggling the visibility of relations. */
    private JCheckBoxMenuItem viewRelationMenuItem;
    /** View specific View menu item for toggling the visibility of individual clause items. */
    private JCheckBoxMenuItem viewClauseItemMenuItem;
    /** View specific View menu item for toggling the visibility of syntactic proposition translations. */
    private JCheckBoxMenuItem viewSyntacticTranslationMenuItem;
    /** View specific View menu item for toggling the visibility of semantic proposition translations. */
    private JCheckBoxMenuItem viewSemanticTranslationMenuItem;
    /** View specific tool bar toggle button for view preset "Show All Details". */
    private JToggleButton allViewDetailsToolBarItem;
    /** View specific tool bar toggle button for view preset "Syntactical Analysis". */
    private JToggleButton syntacticalAnalysisToolBarItem;
    /** View specific tool bar toggle button for view preset "Semantical Analysis". */
    private JToggleButton semanticalAnalysisToolBarItem;

    /**
     * Constructor.
     *
     * @param project the represented view project
     * @param languageModelProvider provider of available {@link LanguageModel}s to be selected when the represented project is setup initially
     * @param relationProvider provider of available {@link RelationTemplate}s to be created/applied in the semantical analysis
     * @param modelParseProvider service provider for saving and exporting the represented project/model to files
     */
    public SingleProjectView(final HmxSwingProject project, final ILanguageModelProvider languageModelProvider,
            final ISemanticalRelationProvider relationProvider, final IModelParseServiceProvider modelParseProvider) {
        super(project, project.getModelObject(), new BorderLayout());
        this.relationProvider = relationProvider;
        this.modelParseProvider = modelParseProvider;
        if (this.containsAnalysisData()) {
            this.activeView = new AnalysisPanel(this.getProject().getModelHandler(), this.relationProvider, this.viewSettings);
        } else {
            this.activeView = new TextInputPanel(this, true, languageModelProvider);
        }
        this.add((Component) this.activeView);
    }

    /**
     * Check whether the current model contains any analysis related data - i.e. if information would be lost if the current model was displayed in a
     * {@link TextInputPanel} rather than a {@link AnalysisPanel}.
     *
     * @return if any information besides the origin text is present
     */
    final boolean containsAnalysisData() {
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
     * @param originText the user input that should be interpreted as the {@link Proposition}s to be analyzed
     * @param originLanguage the user selected language model associated with the {@code originText}, determining orientation and available functions
     * @param originTextFont the user defined font to display the {@code originText} with
     */
    public void startAnalysis(final String originText, final LanguageModel originLanguage, final Font originTextFont) {
        this.getModel().init(originText, originLanguage, originTextFont);
        this.goToAnalysisView();
        new ProjectInfoDialog(this.getProject(), false).setVisible(true);
    }

    /**
     * Prepend/append the given {@code originText} to the represented {@link Pericope}.
     *
     * @param originText the user input that should be interpreted as the {@link Proposition}s to be added
     * @param inFront if the new {@link Proposition}s should be inserted as the first in this {@link Pericope}; otherwise they will be appended
     */
    public void addNewPropositions(final String originText, final boolean inFront) {
        this.getProject().getModelHandler().addNewPropositions(originText, inFront);
        this.goToAnalysisView();
    }

    /** Switch from the text-input mode to the analysis mode. If this is already in analysis mode, this method does nothing. */
    void goToAnalysisView() {
        if (this.activeView instanceof TextInputPanel) {
            this.remove((Component) this.activeView);
            this.activeView = new AnalysisPanel(this.getProject().getModelHandler(), this.relationProvider, this.viewSettings);
            this.add((Component) this.activeView);
            this.revalidate();
            this.manageMenuOptions();
        }
    }

    /** Switch from the analysis mode to the text-input mode. If this is already in text-input mode, this method does nothing. */
    void goToTextInputView() {
        if (this.activeView instanceof AnalysisPanel) {
            this.submitChangesToModel();
            this.remove((Component) this.activeView);
            this.activeView = new TextInputPanel(this, false, null);
            this.add((Component) this.activeView);
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
            final int optionIndex = MessageHandler.showOptionDialog(HmxMessage.MENUBAR_PROJECT_MERGE_POSITION.get(),
                    HmxMessage.MENUBAR_PROJECT_MERGE.get(),
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

    /**
     * Ensure the menu options' availability based on the current state of the represented project.
     */
    private void manageMenuOptions() {
        // handle general menu options
        this.getProject().manageMenuOptions();
        // handle view specific menu options
        this.manageEditMenuOptions();
        this.manageViewMenuOptions();
        this.manageToolBarItems();
    }

    /**
     * Ensure the availability of the view specific "Edit" menu options depending on the currently active view.
     */
    private void manageEditMenuOptions() {
        // avoid NullPointer if Edit menu items have not been created yet
        if (this.addTextItem != null) {
            final boolean inAnalysisMode = this.activeView instanceof AnalysisPanel;
            this.addTextItem.setEnabled(inAnalysisMode);
            this.removeTextItem.setEnabled(inAnalysisMode);
            this.mergeProjectItem.setEnabled(inAnalysisMode);
        }
    }

    /**
     * Ensure the availability of the view specific "View" menu options depending on the currently active view.
     */
    private void manageViewMenuOptions() {
        // avoid NullPointer if View menu items have not been created yet
        if (this.viewPropositionLabelMenuItem != null) {
            final boolean inAnalysisMode = this.activeView instanceof AnalysisPanel;
            this.viewPropositionLabelMenuItem.setEnabled(inAnalysisMode);
            this.viewPropositionLabelMenuItem.setSelected(inAnalysisMode && this.viewSettings.isShowingPropositionLabels());
            this.viewPropositionIndentationMenuItem.setEnabled(inAnalysisMode);
            this.viewPropositionIndentationMenuItem.setSelected(inAnalysisMode && this.viewSettings.isShowingPropositionIndentations());
            this.viewRelationMenuItem.setEnabled(inAnalysisMode);
            this.viewRelationMenuItem.setSelected(inAnalysisMode && this.viewSettings.isShowingRelations());
            this.viewClauseItemMenuItem.setEnabled(inAnalysisMode);
            this.viewClauseItemMenuItem.setSelected(inAnalysisMode && this.viewSettings.isShowingClauseItems());
            this.viewSyntacticTranslationMenuItem.setEnabled(inAnalysisMode);
            this.viewSyntacticTranslationMenuItem.setSelected(inAnalysisMode && this.viewSettings.isShowingSyntacticTranslations());
            this.viewSemanticTranslationMenuItem.setEnabled(inAnalysisMode);
            this.viewSemanticTranslationMenuItem.setSelected(inAnalysisMode && this.viewSettings.isShowingSemanticTranslations());
        }
    }

    /**
     * Ensure the availability of the view specific tool bar items depending on the currently active view.
     */
    private void manageToolBarItems() {
        // avoid NullPointer if Edit menu items have not been created yet
        if (this.allViewDetailsToolBarItem != null) {
            final boolean inAnalysisMode = this.activeView instanceof AnalysisPanel;
            this.allViewDetailsToolBarItem.setEnabled(inAnalysisMode);
            this.allViewDetailsToolBarItem.setSelected(inAnalysisMode
                    && this.viewSettings.matchesPreset(IAnalysisViewSettings.SHOW_ALL));
            this.syntacticalAnalysisToolBarItem.setEnabled(inAnalysisMode);
            this.syntacticalAnalysisToolBarItem.setSelected(inAnalysisMode
                    && this.viewSettings.matchesPreset(IAnalysisViewSettings.SYNTACTICAL_ANALYSIS));
            this.semanticalAnalysisToolBarItem.setEnabled(inAnalysisMode);
            this.semanticalAnalysisToolBarItem.setSelected(inAnalysisMode
                    && this.viewSettings.matchesPreset(IAnalysisViewSettings.SEMANTICAL_ANALYSIS));
        }
    }

    @Override
    public void refresh() {
        this.activeView.refresh();
    }

    @Override
    public void submitChangesToModel() {
        this.activeView.submitChangesToModel();
    }

    @Override
    public boolean canUndo() {
        return this.activeView.canUndo();
    }

    @Override
    public boolean canRedo() {
        return this.activeView.canRedo();
    }

    @Override
    public void undo() {
        this.activeView.undo();
    }

    @Override
    public void redo() {
        this.activeView.redo();
    }

    @Override
    public List<JMenuItem> createEditMenuItems() {
        this.addTextItem = new JMenuItem(HmxMessage.MENUBAR_ORIGINTEXT_ADD.get(), ScitosIcon.ADD.create());
        this.addTextItem.addActionListener(event -> this.goToTextInputView());
        this.removeTextItem = new JMenuItem(HmxMessage.MENUBAR_ORIGINTEXT_REMOVE.get(), ScitosIcon.DELETE.create());
        this.removeTextItem.addActionListener(event -> {
            if (this.activeView instanceof IPericopeView
                    && MessageHandler.Choice.YES == MessageHandler.showConfirmDialog(HmxMessage.MENUBAR_ORIGINTEXT_REMOVE_CONFIRM.get(),
                            HmxMessage.MENUBAR_ORIGINTEXT_REMOVE.get())) {
                final List<Proposition> selection = ((IPericopeView) this.activeView).getSelectedPropositions(null);
                try {
                    this.getProject().getModelHandler().removePropositions(selection);
                } catch (final HmxException expected) {
                    // illegal selection of Propositions
                    MessageHandler.showException(expected);
                }
            }
        });
        this.mergeProjectItem = new JMenuItem(HmxMessage.MENUBAR_PROJECT_MERGE.get(), ScitosIcon.PROJECT_OPEN.create());
        this.mergeProjectItem.addActionListener(event -> this.mergeWithOtherPericope());
        final JMenuItem projectInfoItem = new JMenuItem(HmxMessage.MENUBAR_PROJECTINFO.get(), ScitosIcon.CLIPBOARD.create());
        projectInfoItem.addActionListener(event -> new ProjectInfoDialog(this.getProject(),
                this.activeView instanceof AnalysisPanel).setVisible(true));
        this.manageEditMenuOptions();
        return Arrays.asList(this.addTextItem, this.removeTextItem, this.mergeProjectItem, null, projectInfoItem);
    }

    @Override
    public List<JMenuItem> createViewMenuItems() {
        this.viewPropositionLabelMenuItem = new JCheckBoxMenuItem(HmxMessage.MENUBAR_TOGGLE_PROPOSITION_LABELS.get(),
                ScitosIcon.ATTRIBUTES_DISPLAY.create());
        this.viewPropositionLabelMenuItem.addActionListener(event -> this.togglePropositionLabelVisibility());
        this.viewPropositionIndentationMenuItem = new JCheckBoxMenuItem(HmxMessage.MENUBAR_TOGGLE_PROPOSITION_INDENTATIONS.get(),
                ScitosIcon.TREE.create());
        this.viewPropositionIndentationMenuItem.addActionListener(event -> this.togglePropositionIndentationVisibility());
        this.viewRelationMenuItem = new JCheckBoxMenuItem(HmxMessage.MENUBAR_TOGGLE_RELATIONS.get(),
                ScitosIcon.RELATIONS.create());
        this.viewRelationMenuItem.addActionListener(event -> this.toggleRelationVisibility());
        this.viewClauseItemMenuItem = new JCheckBoxMenuItem(HmxMessage.MENUBAR_TOGGLE_CLAUSE_ITEMS.get(),
                ScitosIcon.GRID.create());
        this.viewClauseItemMenuItem.addActionListener(event -> this.toggleClauseItemVisibility());
        this.viewSyntacticTranslationMenuItem = new JCheckBoxMenuItem(HmxMessage.MENUBAR_TOGGLE_SYNTACTIC_TRANSLATIONS.get(),
                ScitosIcon.HORIZONTAL_RULE.create());
        this.viewSyntacticTranslationMenuItem.addActionListener(event -> this.toggleSyntacticTranslationVisibility());
        this.viewSemanticTranslationMenuItem = new JCheckBoxMenuItem(HmxMessage.MENUBAR_TOGGLE_SEMANTIC_TRANSLATIONS.get(),
                ScitosIcon.HORIZONTAL_RULE.create());
        this.viewSemanticTranslationMenuItem.addActionListener(event -> this.toggleSemanticTranslationVisibility());
        this.manageViewMenuOptions();
        return Arrays.<JMenuItem>asList(this.viewPropositionLabelMenuItem, this.viewPropositionIndentationMenuItem, this.viewRelationMenuItem,
                this.viewClauseItemMenuItem, this.viewSyntacticTranslationMenuItem, this.viewSemanticTranslationMenuItem);
    }

    @Override
    public List<Component> createToolBarItems() {
        this.allViewDetailsToolBarItem = new JToggleButton(HmxMessage.ANALYSIS_PRESET_SHOW_ALL.get(), ScitosIcon.ATTRIBUTES_DISPLAY.create());
        this.allViewDetailsToolBarItem.setFocusable(false);
        this.allViewDetailsToolBarItem.setName("Preset: All");
        this.allViewDetailsToolBarItem.addActionListener(event -> this.applyViewPreset(IAnalysisViewSettings.SHOW_ALL));
        this.syntacticalAnalysisToolBarItem = new JToggleButton(HmxMessage.ANALYSIS_PRESET_SYNTACTICAL.get(), ScitosIcon.GRID.create());
        this.syntacticalAnalysisToolBarItem.setFocusable(false);
        this.syntacticalAnalysisToolBarItem.setName("Preset: Syntactical");
        this.syntacticalAnalysisToolBarItem.addActionListener(event -> this.applyViewPreset(IAnalysisViewSettings.SYNTACTICAL_ANALYSIS));
        this.semanticalAnalysisToolBarItem = new JToggleButton(HmxMessage.ANALYSIS_PRESET_SEMANTICAL.get(), ScitosIcon.RELATIONS.create());
        this.semanticalAnalysisToolBarItem.setFocusable(false);
        this.semanticalAnalysisToolBarItem.setName("Preset: Semantical");
        this.semanticalAnalysisToolBarItem.addActionListener(event -> this.applyViewPreset(IAnalysisViewSettings.SEMANTICAL_ANALYSIS));
        this.manageToolBarItems();
        return Arrays.<Component>asList(this.allViewDetailsToolBarItem, this.syntacticalAnalysisToolBarItem, this.semanticalAnalysisToolBarItem);
    }

    /**
     * Toggle the visibility of the label fields for all propositions. Causing a full rebuild of the Pericope.
     *
     * @see #refresh()
     */
    void togglePropositionLabelVisibility() {
        final boolean toggleValue = this.viewSettings.setShowingPropositionLabels(!this.viewSettings.isShowingPropositionLabels());
        this.viewPropositionLabelMenuItem.setSelected(toggleValue);
        this.manageToolBarItems();
        this.refresh();
    }

    /**
     * Toggle the visibility of the indentation (and associated functions) of all propositions. Causing a full rebuild of the Pericope.
     *
     * @see #refresh()
     */
    void togglePropositionIndentationVisibility() {
        final boolean toggleValue = this.viewSettings.setShowingPropositionIndentations(!this.viewSettings.isShowingPropositionIndentations());
        this.viewPropositionIndentationMenuItem.setSelected(toggleValue);
        this.manageToolBarItems();
        this.refresh();
    }

    /**
     * Toggle the visibility of the relations over all propositions. Causing a full rebuild of the Pericope.
     *
     * @see #refresh()
     */
    void toggleRelationVisibility() {
        final boolean toggleValue = this.viewSettings.setShowingRelations(!this.viewSettings.isShowingRelations());
        this.viewRelationMenuItem.setSelected(toggleValue);
        this.manageToolBarItems();
        this.refresh();
    }

    /**
     * Toggle between individual clause items and a single origin text field being shown for all propositions. Causing a full rebuild of the Pericope.
     *
     * @see #refresh()
     */
    void toggleClauseItemVisibility() {
        final boolean toggleValue = this.viewSettings.setShowingClauseItems(!this.viewSettings.isShowingClauseItems());
        this.viewClauseItemMenuItem.setSelected(toggleValue);
        this.manageToolBarItems();
        this.refresh();
    }

    /**
     * Toggle the visibility of the syntactic translation fields for all propositions. Causing a full rebuild of the Pericope.
     *
     * @see #refresh()
     */
    void toggleSyntacticTranslationVisibility() {
        final boolean toggleValue = this.viewSettings.setShowingSyntacticTranslations(!this.viewSettings.isShowingSyntacticTranslations());
        this.viewSyntacticTranslationMenuItem.setSelected(toggleValue);
        this.manageToolBarItems();
        this.refresh();
    }

    /**
     * Toggle the visibility of the semantic translation fields for all propositions. Causing a full rebuild of the Pericope.
     *
     * @see #refresh()
     */
    void toggleSemanticTranslationVisibility() {
        final boolean toggleValue = this.viewSettings.setShowingSemanticTranslations(!this.viewSettings.isShowingSemanticTranslations());
        this.viewSemanticTranslationMenuItem.setSelected(toggleValue);
        this.manageToolBarItems();
        this.refresh();
    }

    /**
     * Apply the given view preset (potentially altering multiple individual toggles). Causing a full rebuild of the Pericope.
     *
     * @param preset collection of view toggles to set
     * @see #refresh()
     */
    void applyViewPreset(final IAnalysisViewSettings preset) {
        if (this.viewSettings.matchesPreset(preset)) {
            this.viewSettings.applyViewPreset(IAnalysisViewSettings.HIDE_ALL);
        } else {
            this.viewSettings.applyViewPreset(preset);
        }
        this.manageViewMenuOptions();
        this.manageToolBarItems();
        this.refresh();
    }
}
