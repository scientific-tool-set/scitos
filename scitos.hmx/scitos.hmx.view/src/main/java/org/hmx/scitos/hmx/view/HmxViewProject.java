package org.hmx.scitos.hmx.view;

import org.hmx.scitos.hmx.core.HmxModelHandler;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.view.IViewProject;

/**
 * HermeneutiX module's view project providing access to its {@link HmxModelHandler} instance, in addition to the general {@link IViewProject}
 * interface.
 */
public interface HmxViewProject extends IViewProject<Pericope> {

    /**
     * Getter for the {@link HmxModelHandler}, responsible for all actual model changes and issuer of any model change events.
     *
     * @return the associated {@link HmxModelHandler} instance
     */
    HmxModelHandler getModelHandler();

}
