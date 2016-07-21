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

package org.hmx.scitos.hmx.domain;

import java.util.List;

import org.hmx.scitos.hmx.domain.model.AbstractSyntacticalFunctionElement;
import org.hmx.scitos.hmx.domain.model.SyntacticalFunction;
import org.hmx.scitos.hmx.domain.model.SyntacticalFunctionGroup;

/**
 * Generic interface of a model element providing access to the hierarchical structure of {@link SyntacticalFunctionGroup}s and
 * {@link SyntacticalFunction}s.
 */
public interface ISyntacticalFunctionProvider {

    /**
     * Provide the {@link SyntacticalFunction}s, that are used in combination with elements implementing the {@link ICanHaveSyntacticalFunction}
     * interface.
     *
     * @return all top level syntactical functions (including function groups, that contain one or more functions/groups and cannot be selected)
     */
    List<List<AbstractSyntacticalFunctionElement>> provideFunctions();
}
