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

package org.hmx.scitos.ais.view.swing;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.SwingUtilities;

import org.hmx.scitos.ais.core.IModelHandler;
import org.hmx.scitos.ais.domain.model.AisProject;
import org.hmx.scitos.ais.domain.model.Interview;
import org.hmx.scitos.core.ExportOption;
import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.domain.IModel;
import org.hmx.scitos.domain.ModelChangeListener;
import org.hmx.scitos.domain.ModelEvent;
import org.hmx.scitos.view.FileType;
import org.hmx.scitos.view.IViewProject;
import org.hmx.scitos.view.swing.MessageHandler;
import org.hmx.scitos.view.swing.ScitosClient;

/** Swing controller representing one open project containing the model and view elements. */
public final class AisViewProject implements IViewProject<AisProject>, ModelChangeListener {

    /** The main client instance this view project belongs to. */
    final ScitosClient client;
    /** The dedicated model handler for the represented model project instance. */
    private final IModelHandler modelHandler;
    /** The flag indicating whether the current state contains no unsaved changes. */
    boolean saved = true;
    /** The path this project has been loaded from and/or last saved to. It is used as the default path for the next requested save operation. */
    File savePath;
    /** The elements displayed in open tabs when this view project was last loaded/saved. */
    private final List<Object> openTabElements = new LinkedList<Object>();

    /**
     * Creates a new project in the specified {@link ScitosClient}.
     *
     * @param client
     *            designated client to show the project
     * @param modelHandler
     *            the handler of the represented {@link AisProject}
     * @param savePath
     *            the path this file has been loaded from and/or will be saved to by default
     */
    public AisViewProject(final ScitosClient client, final IModelHandler modelHandler, final File savePath) {
        this.client = client;
        this.modelHandler = modelHandler;
        this.setSavePath(savePath);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                modelHandler.addModelChangeListener(AisViewProject.this);
            }
        });
    }

    @Override
    public FileType getFileType() {
        return FileType.AIS;
    }

    /**
     * Getter for the model handler, responsible for all actual model changes and manager of any model change events.
     *
     * @return the associated model handler instance
     */
    public IModelHandler getModelHandler() {
        return this.modelHandler;
    }

    @Override
    public AisProject getModelObject() {
        return this.getModelHandler().getModel();
    }

    @Override
    public String getLabel(final Object element) {
        String label;
        if (element instanceof Interview) {
            final Interview interview = (Interview) element;
            final List<Interview> interviewList = this.getModelObject().getSubModelObjects().get(interview.getParticipantId());
            label = interview.getParticipantId();
            if (interviewList == null || interviewList.size() > 1) {
                // display only the participant id, if there is just one interview for this participant
                label += " (" + interview.getIndex() + ")";
            }
        } else if (element == this || element == this.getModelObject()) {
            label = this.getTitle();
        } else {
            label = element.toString();
        }
        return label;
    }

    @Override
    public boolean isValidElement(final Object element) {
        final boolean valid;
        if (element instanceof Interview) {
            // check if this interview was not deleted
            valid = this.getModelObject().getInterviews().contains(element);
        } else if (element instanceof String) {
            // check if the participant with the given id still has at least two interview assigned
            final List<Interview> interviews = this.getModelObject().getSubModelObjects().get(element);
            valid = interviews != null && interviews.size() > 1;
        } else {
            // check if the given element was actually this project
            valid = this == element || this.getModelObject() == element;
        }
        return valid;
    }

    @Override
    public String getTitle() {
        return this.getModelObject().getLabel();
    }

    @Override
    public void modelChanged(final ModelEvent<?> event) {
        if (!event.isUpdated()) {
            this.client.getMainView().resetTreeStructure();
            if (event.getTarget() instanceof Interview) {
                this.client.getMainView().selectModelTreeNode((IModel<?>) event.getTarget());
            }
            this.client.getMainView().validateTabsForProject(this);
        }
        this.saved = false;
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                AisViewProject.this.client.revalidate();
            }
        });
    }

    @Override
    public List<?> getOpenTabElements() {
        return Collections.unmodifiableList(this.openTabElements);
    }

    @Override
    public void setOpenTabElements(final List<?> tabElements) {
        // mark as unsaved if the open tabs changed
        this.setSaved(this.isSaved() && this.openTabElements.equals(tabElements));
        // reset remembered open tabs
        this.openTabElements.clear();
        if (tabElements != null) {
            this.openTabElements.addAll(tabElements);
        }
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
        final Entry<? extends IModel<?>, List<Object>> reloadedModel;
        try {
            reloadedModel = this.client.getModelParseProvider().open(path);
        } catch (final HmxException validationException) {
            // don't confuse the user by showing an exception about being unable to read a file, when it was just saved
            throw new HmxException(Message.ERROR_SAVE_FAILED);
        }
        if (!(reloadedModel.getKey() instanceof AisProject)) {
            throw new HmxException(Message.ERROR_SAVE_FAILED);
        }
        final String validationError = this.modelHandler.validateEquality((AisProject) reloadedModel.getKey());
        if (validationError != null) {
            throw new HmxException(Message.ERROR_SAVE_FAILED, new IllegalStateException(validationError));
        }
        this.setSavePath(path);
        this.setSaved(true);
        this.client.invokeRepresentationRefresh(this);
    }

    @Override
    public void export(final ExportOption type, final File path) throws HmxException {
        switch (type.getTargetFileType()) {
        case HTML:
            this.client.getModelParseProvider().export(this.getModelObject(), type.getStylesheetPath(), path);
            break;
        case ODS:
            new PatternAnalysisModel(this).exportToSpreadSheet(path);
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
        // if the project is in the analysis mode it is possible to save
        if (!this.isSaved()) {
            final MessageHandler.Choice choice =
                    MessageHandler.showYesNoCancelDialog(Message.PROJECT_CLOSE_QUESTION.get(),
                            Message.PROJECT_CLOSE.get() + " - " + this.getTitle());
            if (choice == MessageHandler.Choice.CANCEL) {
                // user canceled the SaveModifiedQuestion dialog
                return false;
            }
            try {
                if (choice == MessageHandler.Choice.YES && !this.save()) {
                    final File path = this.client.getSaveDestination(this.getFileType().getFileExtension(), Message.MENUBAR_FILE_SAVE.get());
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
                if (AisViewProject.this.saved != flag) {
                    AisViewProject.this.saved = flag;
                    AisViewProject.this.client.refreshTitle();
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
        String modelLabel = savePath.getName();
        if (modelLabel.contains(".")) {
            modelLabel = modelLabel.substring(0, modelLabel.lastIndexOf('.'));
        }
        this.getModelObject().setLabel(modelLabel);
    }

    @Override
    public String toString() {
        return this.savePath.getName();
    }
}
