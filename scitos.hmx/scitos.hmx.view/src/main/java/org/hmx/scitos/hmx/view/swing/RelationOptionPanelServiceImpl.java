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

package org.hmx.scitos.hmx.view.swing;

import javax.inject.Inject;

import org.hmx.scitos.hmx.core.option.HmxExportOption;
import org.hmx.scitos.hmx.core.option.HmxRelationOption;
import org.hmx.scitos.hmx.view.swing.option.HmxRelationOptionPanel;
import org.hmx.scitos.view.service.IOptionPanelService;
import org.hmx.scitos.view.service.IOptionPanelServiceProvider;
import org.hmx.scitos.view.service.IOptionPanelServiceRegistry;
import org.hmx.scitos.view.swing.ScitosClient;
import org.hmx.scitos.view.swing.option.OptionView;

/**
 * The HermeneutiX module's implementation of the {@link IOptionPanelService}, allowing the modification of the export related settings in the
 * {@link HmxExportOption} class.
 *
 * @see IOptionPanelServiceRegistry
 * @see IOptionPanelServiceProvider
 */
public class RelationOptionPanelServiceImpl implements IOptionPanelService {

    /** The handler instance for the available semantical relation templates. */
    private final HmxRelationOption options;

    /**
     * Constructor.
     * 
     * @param relationModelHandler
     *            the handler instance for the available semantical relation templates
     */
    @Inject
    public RelationOptionPanelServiceImpl(final HmxRelationOption relationModelHandler) {
        // mandatory constructor for dependency injection
        this.options = relationModelHandler;
    }

    @Override
    public HmxRelationOptionPanel createOptionPanel(final ScitosClient client, final OptionView parentView) {
        return new HmxRelationOptionPanel(this.options);
    }
}
