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

package org.hmx.scitos.hmx.domain.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.hmx.scitos.hmx.domain.ISemanticalRelationProvider;

/** Implementation of a mutable relation model - enabling its visual representation and modification. */
public class RelationModel implements ISemanticalRelationProvider, Serializable, Cloneable {

    /** The grouped relation templates. */
    private final List<List<RelationTemplate>> relationTemplateGroups;

    /**
     * Constructor: initializing an empty set of template groups.
     */
    public RelationModel() {
        this.relationTemplateGroups = new LinkedList<List<RelationTemplate>>();
    }

    /**
     * Reset the contained relation templates to the given ones.
     *
     * @param groups
     *            grouped relation templates to set for this model (instead of any currently contained ones)
     * @see #addAll(List)
     */
    public void reset(final List<List<RelationTemplate>> groups) {
        this.relationTemplateGroups.clear();
        if (groups != null) {
            this.addAll(groups);
        }
    }

    /**
     * Add the given grouped relation templates to this model.
     *
     * @param groups
     *            grouped relation templates to add to this model
     */
    public void addAll(final List<List<RelationTemplate>> groups) {
        for (final List<RelationTemplate> singleGroup : groups) {
            this.add(singleGroup);
        }
    }

    /**
     * Add the given relation template group to this model.
     *
     * @param group
     *            single group of relation templates to add to this model
     */
    public void add(final List<RelationTemplate> group) {
        this.relationTemplateGroups.add(Collections.unmodifiableList(new ArrayList<RelationTemplate>(group)));
    }

    @Override
    public List<List<RelationTemplate>> provideRelationTemplates() {
        return Collections.unmodifiableList(this.relationTemplateGroups);
    }

    @Override
    public RelationModel clone() {
        final RelationModel clone = new RelationModel();
        clone.addAll(this.relationTemplateGroups);
        return clone;
    }

    @Override
    public int hashCode() {
        int hashCode = this.relationTemplateGroups.size() * 13;
        int factor = 3;
        for (final List<RelationTemplate> singleGroup : this.relationTemplateGroups) {
            hashCode += singleGroup.size() * factor;
            factor += 2;
        }
        return hashCode;
    }

    @Override
    public boolean equals(final Object otherObj) {
        if (this == otherObj) {
            return true;
        }
        if (!(otherObj instanceof RelationModel)) {
            return false;
        }
        return this.relationTemplateGroups.equals(((RelationModel) otherObj).relationTemplateGroups);
    }
}
