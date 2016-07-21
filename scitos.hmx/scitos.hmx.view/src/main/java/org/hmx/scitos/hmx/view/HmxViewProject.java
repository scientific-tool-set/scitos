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

package org.hmx.scitos.hmx.view;

import org.hmx.scitos.hmx.core.HmxModelHandler;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.view.IViewProject;

/**
 * HermeneutiX module's view project providing access to its {@link HmxModelHandler} instance, in addition to the general {@link IViewProject}
 * interface.
 */
public interface HmxViewProject extends IViewProject<Pericope> {

    /**
     * Getter for the {@link HmxModelHandler}, responsible for all actual model changes and issuer of any model change events.
     *
     * @return the associated {@link HmxModelHandler} instance
     */
    HmxModelHandler getModelHandler();

}
