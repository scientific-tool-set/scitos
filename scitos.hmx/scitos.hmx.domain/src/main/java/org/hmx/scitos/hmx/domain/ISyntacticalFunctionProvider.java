package org.hmx.scitos.hmx.domain;

import java.util.List;

import org.hmx.scitos.hmx.domain.model.SyntacticalFunction;

/**
 * Generic interface of a model element providing access to the hierarchical structure of {@link SyntacticalFunction}s.
 */
public interface ISyntacticalFunctionProvider {

    /**
     * Provide the {@link SyntacticalFunction}s, that are used in combination with elements implementing the {@link ICanHaveSyntacticalFunction}
     * interface.
     *
     * @return all top level syntactical functions (including function groups, that contain one or more functions/groups and cannot be selected)
     */
    List<List<SyntacticalFunction>> provideFunctions();
}
