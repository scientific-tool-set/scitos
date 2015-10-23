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
