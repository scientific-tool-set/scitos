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
        for (final List<RelationTemplate> singleGroup : this.relationTemplateGroups) {
            hashCode += singleGroup.size();
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
