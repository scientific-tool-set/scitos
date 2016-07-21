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

package org.hmx.scitos.hmx.view.swing.option;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.tree.TreePath;

import org.hmx.scitos.domain.util.CollectionUtil;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.core.option.ILanguageOptionHandler;
import org.hmx.scitos.hmx.domain.model.AbstractSyntacticalFunctionElement;
import org.hmx.scitos.hmx.domain.model.LanguageModel;
import org.hmx.scitos.hmx.domain.model.SyntacticalFunction;
import org.hmx.scitos.hmx.domain.model.SyntacticalFunctionGroup;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

/**
 * TreeTable data model of a language model provider. It allows the adding/updating/removing of {@link LanguageModel}s.
 * <ol start=0>
 * Manages data and buttons in a number of columns:
 * <li>Name</li>
 * <li>Number of contained syntactical functions</li>
 * <li>Button: clone model</li>
 * <li>Button: remove user defined model</li>
 * </ol>
 */
final class LanguageTreeModel extends AbstractTreeTableModel {

    /** The read-only system-defined language models. */
    private final Map<UUID, LanguageModel> languageModels = new LinkedHashMap<UUID, LanguageModel>();
    /** The number of unchangeable system defined language models (at the top of the table). */
    private final int systemModelCount;

    /**
     * Main constructor.
     * 
     * @param options
     *            the global handler managing the represented language models
     */
    LanguageTreeModel(final ILanguageOptionHandler options) {
        super(new Object());
        // system-defined language models cannot be changed, i.e. no need to clone them
        final List<LanguageModel> systemModels = options.getSystemModels();
        this.systemModelCount = systemModels.size();
        for (final LanguageModel singleSystemModel : systemModels) {
            this.languageModels.put(UUID.randomUUID(), singleSystemModel);
        }
        // user-defined language models can be changed, i.e. clone them to insulate unsaved changes from the active model instances
        for (final LanguageModel singleUserModel : options.getUserModels()) {
            this.languageModels.put(UUID.randomUUID(), singleUserModel.clone());
        }
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public UUID getChild(final Object parentNode, final int index) {
        final Iterator<UUID> languageModelNodes = this.languageModels.keySet().iterator();
        UUID result = languageModelNodes.next();
        for (int i = 0; i < index; i++) {
            result = languageModelNodes.next();
        }
        return result;
    }

    @Override
    public int getChildCount(final Object parentNode) {
        if (parentNode == this.getRoot()) {
            return this.languageModels.size();
        }
        return 0;
    }

    @Override
    public int getIndexOfChild(final Object parentNode, final Object childNode) {
        if (parentNode == this.getRoot()) {
            return CollectionUtil.indexOfInstance(this.languageModels.keySet(), childNode);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public boolean isCellEditable(final Object node, final int columnIndex) {
        if (this.languageModels.containsKey(node)) {
            switch (columnIndex) {
            case 2:
                return true;
            case 3:
                return this.getRowIndexOfNode(node) >= this.systemModelCount;
            default:
                return false;
            }
        }
        return false;
    }

    @Override
    public Object getValueAt(final Object node, final int columnIndex) {
        if (node == this.getRoot()) {
            return null;
        }
        switch (columnIndex) {
        case 0:
            // return the language model's name
            final String modelName = this.languageModels.get(node).getName();
            if (this.isModelAtPathUserDefined(new TreePath(new Object[] { this.getRoot(), node }))) {
                return modelName;
            }
            return MessageFormat.format(HmxMessage.PREFERENCES_LANGUAGE_SYSTEMMODEL.get(), modelName);
        case 1:
            // count the number of syntactical functions contained in the respective language model (ignore the number of groups)
            final AtomicInteger functionCounter = new AtomicInteger(0);
            for (final List<AbstractSyntacticalFunctionElement> singleGroup : this.languageModels.get(node).provideFunctions()) {
                for (final AbstractSyntacticalFunctionElement topLevelElement : singleGroup) {
                    this.addFunctionCount(topLevelElement, functionCounter);
                }
            }
            return MessageFormat.format(HmxMessage.PREFERENCES_LANGUAGE_FUNCTION_COUNT.get(), functionCounter.intValue());
        case 2:
            // all language models can be cloned as new user-defined languages
            return HmxMessage.PREFERENCES_LANGUAGE_CLONE_LANGUAGE.get();
        case 3:
            // system-defined language models cannot be deleted; user-defined language models can
            return this.isCellEditable(node, columnIndex) ? HmxMessage.PREFERENCES_LANGUAGE_DELETE_LANGUAGE.get() : null;
        default:
            // we are only planning for four columns here
            throw new IllegalArgumentException();
        }
    }

    /**
     * Add the number of syntactical functions in the given element to the specified counter.
     * 
     * @param element
     *            syntactical function or function group to be included in counter
     * @param counter
     *            the counter to increment by the number of contained syntactical functions
     */
    private void addFunctionCount(final AbstractSyntacticalFunctionElement element, final AtomicInteger counter) {
        if (element instanceof SyntacticalFunction) {
            counter.incrementAndGet();
        } else if (element instanceof SyntacticalFunctionGroup) {
            for (final AbstractSyntacticalFunctionElement subElement : ((SyntacticalFunctionGroup) element).getSubFunctions()) {
                this.addFunctionCount(subElement, counter);
            }
        }
    }

    /**
     * Getter for the underlying language model at the given path.
     * 
     * @param path
     *            the path to return the associated language model for
     * @return language model
     */
    LanguageModel getModelForPath(final TreePath path) {
        return this.languageModels.get(path.getLastPathComponent());
    }

    /**
     * Check whether the model at the given path is user defined, i.e. can be changed.
     * 
     * @param path
     *            the path to check
     * @return if the path refers to a user defined model; otherwise its a system defined one
     */
    boolean isModelAtPathUserDefined(final TreePath path) {
        if (path == null || path.getPathCount() < 2) {
            return false;
        }
        return this.getRowIndexOfNode(path.getLastPathComponent()) >= this.systemModelCount;
    }

    /**
     * Determine the index of the row with the given reference.
     * 
     * @param node
     *            reference to the table row
     * @return index of the referenced table row
     */
    private int getRowIndexOfNode(final Object node) {
        return CollectionUtil.indexOfInstance(this.languageModels.keySet(), node);
    }

    /**
     * Add a new table row representing a copy of the language model at the given path.
     * 
     * @param path
     *            path of the language model to clone and insert as new row
     * @return path to the added row
     */
    TreePath addModelRowClone(final TreePath path) {
        final LanguageModel originalModel = this.languageModels.get(path.getLastPathComponent());
        final UUID cloneReference = UUID.randomUUID();
        this.languageModels.put(cloneReference, originalModel.clone());
        final TreePath rootPath = new TreePath(this.getRoot());
        this.modelSupport.fireChildAdded(rootPath, this.languageModels.size() - 1, cloneReference);
        return rootPath.pathByAddingChild(cloneReference);
    }

    /**
     * Remove the table row at the given index.
     * 
     * @param path
     *            path of the table row to delete
     */
    void deleteModelRow(final TreePath path) {
        final Object node = path.getLastPathComponent();
        final int index = CollectionUtil.indexOfInstance(this.languageModels.keySet(), node);
        if (index < this.systemModelCount) {
            throw new IllegalArgumentException();
        }
        this.languageModels.remove(node);
        this.modelSupport.fireChildRemoved(new TreePath(this.getRoot()), index, node);
    }

    /**
     * Trigger the update of the table row associated with the given language model.
     * 
     * @param model
     *            the language model that has been updated
     */
    void fireModelRowUpdated(final LanguageModel model) {
        final int index = CollectionUtil.indexOfInstance(this.languageModels.values(), model);
        if (index < this.systemModelCount) {
            throw new IllegalArgumentException();
        }
        final UUID node = new ArrayList<UUID>(this.languageModels.keySet()).get(index);
        this.modelSupport.fireChildChanged(new TreePath(this.getRoot()), index, node);
    }

    /**
     * Getter for the user-defined language models.
     * 
     * @return the user-defined language-models
     */
    List<LanguageModel> getUserLanguageModels() {
        return new ArrayList<LanguageModel>(this.languageModels.values()).subList(this.systemModelCount, this.languageModels.size());
    }
}
