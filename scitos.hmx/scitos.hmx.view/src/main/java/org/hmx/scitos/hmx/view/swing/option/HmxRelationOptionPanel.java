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

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.BorderFactory;

import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.core.option.HmxRelationOption;
import org.hmx.scitos.hmx.domain.model.Relation;
import org.hmx.scitos.hmx.domain.model.RelationModel;
import org.hmx.scitos.hmx.domain.model.RelationTemplate;
import org.hmx.scitos.view.swing.option.AbstractOptionPanel;

/**
 * Option panel representing the {@link HmxRelationOption} and thereby offering the configuration of available {@link Relation}s (or more precisely:
 * {@link RelationTemplate}s).
 */
public final class HmxRelationOptionPanel extends AbstractOptionPanel {

    /** The global preferences handler being represented by this view component. */
    private final HmxRelationOption options;
    /**
     * The tree table component displaying the default detail category model being applied to a new project. The user can modify this structure via
     * in-line editing.
     */
    private RelationTreeTable treeTable;
    /**
     * The current state of the user defined {@link RelationTemplate}s.
     */
    private RelationModel configuredModel = null;

    /**
     * Constructor.
     * 
     * @param options
     *            the global preferences handler being represented by this view component
     */
    public HmxRelationOptionPanel(final HmxRelationOption options) {
        super(new BorderLayout(), HmxMessage.PREFERENCES_RELATION);
        this.options = options;
        this.setBorder(BorderFactory.createEmptyBorder(5, 15, 0, 0));
        this.treeTable = new RelationTreeTable(options);
        this.treeTable.setBorder(BorderFactory.createTitledBorder(HmxMessage.PREFERENCES_RELATION_LONG.get()));
        this.add(this.treeTable);
    }

    @Override
    protected boolean isResizeCapable() {
        // individually handle the resizing and scrolling
        return true;
    }

    @Override
    protected void validateInput() {
        // build configuredModel from user input
        this.configuredModel = this.treeTable.getRelationModel();
    }

    @Override
    public boolean areChosenSettingsValid() {
        // ensure configuredModel is not empty and does not contain any empty relation group
        if (this.configuredModel == null) {
            return false;
        }
        final List<List<RelationTemplate>> templateGroups = this.configuredModel.provideRelationTemplates();
        boolean containsAtLeastOneTemplate = false;
        for (final List<RelationTemplate> group : templateGroups) {
            if (!group.isEmpty()) {
                containsAtLeastOneTemplate = true;
                break;
            }
            // duplicate entries (i.e. equal relation templates) can be ignored as they are effectively harmless (and might even be intentional)
        }
        return containsAtLeastOneTemplate;
    }

    @Override
    public void submitChosenSettings() {
        if (!this.options.provideRelationTemplates().equals(this.configuredModel.provideRelationTemplates())) {
            this.options.setDefaultRelationModel(this.configuredModel);
            this.options.persistChanges();
        }
    }

}
