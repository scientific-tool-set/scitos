/*
   Copyright (C) 2015 HermeneutiX.org

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

import java.awt.Font;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.hmx.scitos.core.ExportOption;
import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.domain.IModel;
import org.hmx.scitos.domain.ModelChangeListener;
import org.hmx.scitos.domain.ModelEvent;
import org.hmx.scitos.hmx.core.HmxModelHandler;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.view.HmxViewProject;
import org.hmx.scitos.hmx.view.swing.elements.ProjectInfoDialog;
import org.hmx.scitos.hmx.view.swing.elements.SvgExportDetailsDialog;
import org.hmx.scitos.view.FileType;
import org.hmx.scitos.view.swing.MessageHandler;
import org.hmx.scitos.view.swing.ScitosClient;
import org.hmx.scitos.view.swing.util.ViewUtil;

/** Swing controller representing one open project containing the model and view elements. */
public final class HmxSwingProject implements HmxViewProject, ModelChangeListener {

    /** The main client instance this view project belongs to. */
    final ScitosClient client;
    /** The dedicated model handler for the represented model project instance. */
    private final HmxModelHandler modelHandler;
    /** The flag indicating whether the current state contains no unsaved changes. */
    boolean saved = true;
    /** The path this project has been loaded from and/or last saved to. It is used as the default path for the next requested save operation. */
    File savePath;

    /**
     * Creates a new project in the specified {@link ScitosClient}.
     *
     * @param client
     *            designated client to show the project
     * @param modelHandler
     *            the handler of the represented {@link Pericope}
     * @param savePath
     *            the path this file has been loaded from and/or will be saved to by default
     */
    public HmxSwingProject(final ScitosClient client, final HmxModelHandler modelHandler, final File savePath) {
        this.client = client;
        this.modelHandler = modelHandler;
        this.setSavePath(savePath);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                modelHandler.addModelChangeListener(HmxSwingProject.this);
            }
        });
    }

    @Override
    public FileType getFileType() {
        return FileType.HMX;
    }

    @Override
    public HmxModelHandler getModelHandler() {
        return this.modelHandler;
    }

    @Override
    public Pericope getModelObject() {
        return this.getModelHandler().getModel();
    }

    @Override
    public String getLabel(final Object element) {
        final String fileName = this.savePath.getName();
        if (fileName.contains(".")) {
            return fileName.substring(0, fileName.lastIndexOf('.'));
        }
        return fileName;
    }

    @Override
    public boolean isValidElement(final Object element) {
        // the element can only be the whole Pericope
        return !this.getModelObject().getText().isEmpty();
    }

    @Override
    public String getTitle() {
        final String modelTitle = this.getModelObject().getTitle();
        if (modelTitle != null && !modelTitle.isEmpty()) {
            return this.getLabel(null) + " - " + modelTitle;
        }
        return this.getLabel(null);
    }

    @Override
    public void modelChanged(final ModelEvent<?> event) {
        if (!event.isUpdated() && event.getTarget() == this.getModelObject()) {
            this.client.getMainView().resetTreeStructure();
        }
        this.saved = false;
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                HmxSwingProject.this.client.revalidate();
            }
        });
    }

    /**
     * Notify the {@link ScitosClient} to enable/disable its menu entries according to recent changes in this project.
     *
     * @see ScitosClient#manageMenuOptions()
     */
    public void manageMenuOptions() {
        this.client.manageMenuOptions();
    }

    /**
     * Open a dialog for editing the project information (title, author, comment).
     *
     * @param analysisInProgress
     *            if the analysis is already in progress (i.e. if the origin text's {@link Font} should be editable as well)
     */
    public void editInfo(final boolean analysisInProgress) {
        new ProjectInfoDialog(this, analysisInProgress).setVisible(true);
    }

    @Override
    public List<?> getOpenTabElements() {
        return Collections.singletonList(this.getModelObject());
    }

    @Override
    public void setOpenTabElements(final List<?> tabElements) {
        // ignore this, as this is all handled in a single tab
    }

    @Override
    public boolean save() throws HmxException {
        final File defaultSaveTarget = this.savePath;
        if (defaultSaveTarget == null) {
            return false;
        }
        this.saveAs(defaultSaveTarget);
        return true;
    }

    @Override
    public void saveAs(final File path) throws HmxException {
        this.client.getModelParseProvider().save(this.getModelObject(), this.getOpenTabElements(), path);
        final Entry<? extends IModel<?>, List<?>> reloadedModel;
        try {
            reloadedModel = this.client.getModelParseProvider().open(path);
        } catch (final HmxException validationException) {
            // don't confuse the user by showing an exception about being unable to read a file, when it was just saved
            throw new HmxException(Message.ERROR_SAVE_FAILED);
        }
        if (!this.getModelObject().equals(reloadedModel.getKey())) {
            throw new HmxException(Message.ERROR_SAVE_FAILED);
        }
        this.setSavePath(path);
        this.setSaved(true);
        this.client.invokeRepresentationRefresh(this);
    }

    @Override
    public void export(final ExportOption type) throws HmxException {
        switch (type.getTargetFileType()) {
        // case HTML:
        // TODO implement exportToHtml
        // this.client.getModelParseProvider().export(this.getModelObject(), type.getStylesheetPath(), path);
        // break;
        case SVG:
            new SvgExportDetailsDialog(this, this.client.getModelParseProvider()).setVisible(true);
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean close() {
        return this.client.closeProject(this);
    }

    @Override
    public boolean prepareForClosing() {
        if (this.getModelObject().getText().isEmpty()) {
            return MessageHandler.Choice.YES == MessageHandler.showConfirmDialog(HmxMessage.TEXTINPUT_QUIT_QUESTION.get(),
                    HmxMessage.TEXTINPUT_QUIT_TITLE.get());
        }
        // if the project is in analysis mode it is possible to save
        if (!this.isSaved()) {
            final String dialogTitle = Message.PROJECT_CLOSE.get() + " - " + this.getTitle();
            final MessageHandler.Choice choice = MessageHandler.showYesNoCancelDialog(Message.PROJECT_CLOSE_QUESTION.get(), dialogTitle);
            if (choice == MessageHandler.Choice.CANCEL) {
                // user canceled the SaveModifiedQuestion dialog
                return false;
            }
            try {
                if (choice == MessageHandler.Choice.YES && !this.save()) {
                    final File path =
                            ViewUtil.getSaveDestination(this.client.getFrame(), this.getFileType().getFileExtension(),
                                    Message.MENUBAR_FILE_SAVE.get(), true);
                    if (path == null) {
                        // user canceled file choose dialog
                        return false;
                    }
                    this.saveAs(path);
                }
            } catch (final HmxException ex) {
                MessageHandler.showException(ex);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isSaved() {
        return this.saved;
    }

    /**
     * Mark this project as saved or unsaved and show the current state in the main title.
     *
     * @param flag
     *            if this project should be marked as saved
     */
    public void setSaved(final boolean flag) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (HmxSwingProject.this.saved != flag) {
                    HmxSwingProject.this.saved = flag;
                    HmxSwingProject.this.client.refreshTitle();
                }
            }
        });
    }

    @Override
    public String getSavePath() {
        if (this.savePath == null) {
            return null;
        }
        return this.savePath.getAbsolutePath();
    }

    /**
     * Setter for the path this file has been loaded from or last saved to.
     *
     * @param savePath
     *            the default path to save this file to
     */
    public void setSavePath(final File savePath) {
        this.savePath = savePath;
    }

    /**
     * Getter for the main window this project is displayed in. This is intended to be used as parent component for created dialogs.
     *
     * @return the window this project is displayed in
     */
    public JFrame getFrame() {
        return this.client.getFrame();
    }

    @Override
    public String toString() {
        return this.savePath.getName();
    }
}
