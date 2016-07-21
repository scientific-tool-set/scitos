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

import org.hmx.scitos.hmx.domain.model.AbstractConnectable;
import org.hmx.scitos.hmx.domain.model.Relation;
import org.hmx.scitos.hmx.domain.model.RelationTemplate;

/**
 * Generic interface of a model element providing access to the available {@link RelationTemplate}s.
 */
public interface ISemanticalRelationProvider {

    /**
     * Provide the groups of {@link RelationTemplate}s, that are available to create {@link Relation}s between {@link AbstractConnectable}s.
     *
     * @return available {@link RelationTemplate}s, in defined groups
     */
    List<List<RelationTemplate>> provideRelationTemplates();
}
