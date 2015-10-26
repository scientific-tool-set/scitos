package org.hmx.scitos.hmx.view;

import java.util.List;

import org.hmx.scitos.hmx.core.HmxModelHandler;
import org.hmx.scitos.hmx.domain.ISemanticalRelationProvider;
import org.hmx.scitos.hmx.domain.model.AbstractConnectable;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.view.swing.elements.AbstractCommentable;

/** Generic interface of a user view regardless of its actual implementation. */
public interface IPericopeView extends ISemanticalRelationProvider {

    /**
     * Getter for the model handler, responsible for all actual model changes and manager of any model change events.
     *
     * @return the associated model handler instance
     */
    HmxModelHandler getModelHandler();

    /** Ensure all pending changes are completed before any new model changes are being applied. */
    void submitChangesToModel();

    /**
     * Collect the list of selected {@link Proposition}s in the syntactical analysis, if it is currently active (i.e. displayed).
     *
     * @param selectedProposition
     *            single element to be included in the result list regardless of its actual selection state (may be {@code null})
     * @return the currently selected {@link Proposition}s
     */
    List<Proposition> getSelectedPropositions(Proposition selectedProposition);

    /**
     * Collect the list of selected {@link Proposition}s and {@link org.hmx.scitos.hmx.domain.model.Relation Relation}s in the semantical analysis, if
     * it is currently active (i.e. displayed).
     *
     * @param selectedConnectable
     *            single element to be included in the result list regardless of its actual selection state (may be {@code null})
     * @return the currently selected {@link AbstractConnectable}s
     */
    List<? extends AbstractConnectable> getSelectedConnectables(AbstractConnectable selectedConnectable);

    /**
     * Store the most recent modifications in the displayed comment area to the previously selected {@link AbstractCommentable commentable element}
     * and mark the given {@code newSelected} view element as the currently selected one.
     *
     * @param newSelected
     *            the newly selected commentable view element
     */
    void handleSelectedCommentable(AbstractCommentable<?> newSelected);
}
