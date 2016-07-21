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

package org.hmx.scitos.ais.domain;

import java.util.List;

import org.hmx.scitos.ais.domain.model.DetailCategory;
import org.hmx.scitos.domain.IProvider;

/**
 * Generic interface of a model element containing a detail category model.
 */
public interface IDetailCategoryProvider extends IProvider<List<DetailCategory>> {

    /**
     * Provide only those categories, that can be directly assigned to a token in an interview.
     *
     * @return user selectable detail categories
     */
    List<DetailCategory> provideSelectables();
}
