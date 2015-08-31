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

/**
 * Generic interface to register option panel providers to be included in the application's preferences dialog.
 *
 * @see IOptionPanelServiceProvider
 */
public interface IOptionPanelServiceRegistry {

    /**
     * Register the given option panel provider with its associated settings class.
     *
     * @param settingsClass
     *            class representing the user settings that can be accessed via the provided option panel
     * @param service
     *            providing instance for the actual option panel (to be included in the application's preferences dialog)
     */
    void registerOptionPanelService(Class<?> settingsClass, IOptionPanelService service);
}
