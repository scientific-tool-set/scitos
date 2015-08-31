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

package org.hmx.scitos.view.service;

import java.util.Collection;

/**
 * Interface of the collected option panel providers, allowing them to be invoked whenever the application's preferences dialog is created/displayed.
 */
public interface IOptionPanelServiceProvider {

    /**
     * Getter for the actual option panel providers.
     *
     * @return option panel provides that should be included in the application's preferences dialog
     */
    Collection<IOptionPanelService> getServices();
}
