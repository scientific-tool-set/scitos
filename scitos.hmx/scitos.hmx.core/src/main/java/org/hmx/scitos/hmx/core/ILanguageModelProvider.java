package org.hmx.scitos.hmx.core;

import java.util.Map;

import org.hmx.scitos.hmx.domain.model.LanguageModel;

/**
 * Generic interface of an element providing access to the available {@link LanguageModel}s.
 */
public interface ILanguageModelProvider {

    /**
     * Provide the {@link LanguageModel}s that can be assigned to a new project.
     *
     * @return available {@link LanguageModel}s mapped (and sorted) by their names
     */
    Map<String, LanguageModel> provideLanguageModels();
}
