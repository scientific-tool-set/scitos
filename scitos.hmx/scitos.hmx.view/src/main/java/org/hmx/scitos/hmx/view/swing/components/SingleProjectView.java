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
import org.hmx.scitos.hmx.domain.model.LanguageModel;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.view.swing.HmxSwingProject;
import org.hmx.scitos.hmx.view.swing.elements.ProjectInfoDialog;
import org.hmx.scitos.view.ScitosIcon;
import org.hmx.scitos.view.service.IModelParseServiceProvider;
import org.hmx.scitos.view.swing.AbstractProjectView;
import org.hmx.scitos.view.swing.MessageHandler;
import org.hmx.scitos.view.swing.MessageHandler.MessageType;
import org.hmx.scitos.view.swing.util.ViewUtil;

/**
 *
 */
public class SingleProjectView extends AbstractProjectView<HmxSwingProject, Pericope> {

    private final ISemanticalRelationProvider relationProvider;
    private final IModelParseServiceProvider modelParseProvider;
    /** The undo manager for the whole model. */
    private UndoManager<Pericope> undoManager;
    JPanel activeView;

    private JMenuItem addTextItem;
    private JMenuItem removeTextItem;
    private JMenuItem mergeProjectItem;

    /**
     * Constructor.
     *
     * @param project
     *            the represented view project
     * @param modelParseProvider
     */
    public SingleProjectView(final HmxSwingProject project, final ILanguageModelProvider languageModelProvider,
            final ISemanticalRelationProvider relationProvider, final IModelParseServiceProvider modelParseProvider) {
        super(project, project.getModelObject(), new BorderLayout());
        this.relationProvider = relationProvider;
        this.modelParseProvider = modelParseProvider;
        if (this.getModel().getText().isEmpty()) {
            this.activeView = new TextInputPanel(this, languageModelProvider);
        } else {
            this.undoManager = new UndoManager<Pericope>(this.getModel());
            this.activeView = new CombinedAnalysesPanel(this.getProject().getModelHandler(), this.relationProvider);
        }
        this.add(this.activeView);
    }

    /**
     * initializes the analysis by referring to the chosen origin language and {@link Font}, opens the default (syntactical) analysis view
     *
     * @param originText
     * @param languageModel
     * @param relationProvider
     * @param originTextFont
     */
    public void startAnalysis(final String originText, final LanguageModel languageModel, final Font originTextFont) {
        this.getModel().init(originText, languageModel, originTextFont);
        this.undoManager = new UndoManager<Pericope>(this.getModel());
        this.goToAnalysisView();
        new ProjectInfoDialog(this.getProject(), false).setVisible(true);
    }

    /**
     *
     * @param originText
     * @param inFront
     */
    public void addNewPropositions(final String originText, final boolean inFront) {
        this.getProject().getModelHandler().addNewPropositions(originText, inFront);
        this.goToAnalysisView();
    }

    void goToAnalysisView() {
        if (this.activeView instanceof TextInputPanel) {
            this.remove(this.activeView);
            this.activeView = new CombinedAnalysesPanel(this.getProject().getModelHandler(), this.relationProvider);
            this.add(this.activeView);
            this.revalidate();
            this.manageMenuOptions();
        }
    }

    void goToTextInputView() {
        if (this.activeView instanceof CombinedAnalysesPanel) {
            this.submitChangesToModel();
            this.remove(this.activeView);
            this.activeView = new TextInputPanel(this, null);
            this.add(this.activeView);
            this.revalidate();
            this.manageMenuOptions();
        }
    }

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
                this.getProject().getModelHandler().mergeWithOtherPericope((Pericope) modelToMergeWith.getKey(), optionIndex == 0);
            }
        } catch (final HmxException ex) {
            MessageHandler.showException(ex);
        }
    }

    private void manageMenuOptions() {
        // handle general menu options
        this.getProject().manageMenuOptions();
        // handle view specific options
        this.manageViewSpecificMenuOptions();
    }

    private void manageViewSpecificMenuOptions() {
        if (this.addTextItem == null) {
            return;
        }
        final boolean inAnalysisMode = this.activeView instanceof CombinedAnalysesPanel;
        this.addTextItem.setEnabled(inAnalysisMode);
        this.removeTextItem.setEnabled(inAnalysisMode);
        this.mergeProjectItem.setEnabled(inAnalysisMode);
    }

    @Override
    public void refresh() {
        if (this.activeView instanceof CombinedAnalysesPanel) {
            ((CombinedAnalysesPanel) this.activeView).refresh();
        }
    }

    @Override
    public void submitChangesToModel() {
        if (this.activeView instanceof CombinedAnalysesPanel) {
            ((CombinedAnalysesPanel) this.activeView).submitChangesToModel();
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
