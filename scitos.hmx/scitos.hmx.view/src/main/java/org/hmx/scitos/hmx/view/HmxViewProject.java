package org.hmx.scitos.hmx.view;

import org.hmx.scitos.hmx.core.HmxModelHandler;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.view.IViewProject;

/**
 *
 */
public interface HmxViewProject extends IViewProject<Pericope> {

    /**
     * Getter for the model handler, responsible for all actual model changes and manager of any model change events.
     *
     * @return the associated model handler instance
     */
    HmxModelHandler getModelHandler();

}
