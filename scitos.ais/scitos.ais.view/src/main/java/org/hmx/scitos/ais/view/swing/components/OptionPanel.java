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

package org.hmx.scitos.ais.view.swing.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.hmx.scitos.ais.core.AisOption;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.view.service.IOptionPanelService;
import org.hmx.scitos.view.swing.option.AbstractOptionPanel;

/**
 * The single panel provided via an {@link IOptionPanelService} to the application's preferences dialog, for the AIS module.
 */
public final class OptionPanel extends AbstractOptionPanel {

    /** The actual AIS module's option handler, being represented by this panel. */
    private final AisOption options;
    /**
     * The tree table component displaying the default detail category model being applied to new project. The user can modify this structure via
     * in-line editing.
     */
    DetailCategoryTreeTable treeTable;

    /**
     * Main constructor.
     * 
     * @param options
     *            the actual AIS module's option handler, being represented by this panel
     */
    public OptionPanel(final AisOption options) {
        super(new GridBagLayout(), Message.AIS_PREFERENCES);
        this.options = options;
        final Box contentBox = new Box(BoxLayout.PAGE_AXIS);
        // since the purpose of this component is to set the default for new projects, we don't need the associated check box
        this.treeTable = new DetailCategoryTreeTable(this.options, false);
        this.treeTable.setBorder(BorderFactory.createTitledBorder("Default for new projects"));
        contentBox.add(this.treeTable);
        contentBox.revalidate();
        this.add(contentBox, AbstractOptionPanel.HORIZONTAL_SPAN);
        final GridBagConstraints spacing = new GridBagConstraints();
        spacing.weighty = 1;
        spacing.gridy = 1;
        this.add(new JPanel(), spacing);
        this.setMinimumSize(this.getPreferredSize());
    }

    @Override
    protected void validateInput() {
        // the tree model contains all possible changes, nothing more to remember here
    }

    @Override
    public boolean areChosenSettingsValid() {
        return this.treeTable.containsValidModel();
    }

    @Override
    public void submitChosenSettings() {
        if (this.treeTable != null) {
            this.options.setDefaultDetailCategoryModel(this.treeTable.toModel());
            this.options.persistChanges();
        }
    }
}
