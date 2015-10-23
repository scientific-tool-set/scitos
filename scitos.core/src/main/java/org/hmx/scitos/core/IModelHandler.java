package org.hmx.scitos.core;

import org.hmx.scitos.domain.IModel;
import org.hmx.scitos.domain.ModelChangeListener;

/**
 * Base interface for the single point of contact for model changes in a SciToS module.
 *
 * @param <M>
 *            type of the managed model object
 */
public interface IModelHandler<M extends IModel<?>> {

    /**
     * Getter for the managed model object.
     *
     * @return managed model object
     */
    M getModel();

    /**
     * Add the given {@link ModelChangeListener} to the collection of listeners, that are notified when a model change occurs.
     *
     * @param listener
     *            listener to add
     */
    void addModelChangeListener(ModelChangeListener listener);

    /**
     * Remove the given {@link ModelChangeListener} from the collection of listeners, that are notified when a model change occurs.
     *
     * @param listener
     *            listener to remove
     */
    void removeModelChangeListener(ModelChangeListener listener);

}
