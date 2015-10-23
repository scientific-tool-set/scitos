package org.hmx.scitos.hmx.core;

import org.hmx.scitos.core.i18n.ILocalizableMessage;
import org.hmx.scitos.core.i18n.Translator;
import org.hmx.scitos.hmx.domain.model.Relation;

/**
 * Handler for the (no longer provided) internationalization of the role values of associates in a {@link Relation}. This class is only used for
 * compatibilty reasons when converting an old HermeneutiX file (from when it was a standalone application) into its SciToS equivalent.
 */
class CompatibleRelationRole implements ILocalizableMessage {

    /** The (old) associate role key. */
    private final String key;
    /** The resource bundle wrapper to use. */
    private final Translator<CompatibleRelationRole> translator;

    /**
     * Constructor.
     *
     * @param key
     *            the (old) associate role key
     * @param translator
     *            the resource bundle wrapper to use
     */
    CompatibleRelationRole(final String key, final Translator<CompatibleRelationRole> translator) {
        this.key = key;
        this.translator = translator;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String get() {
        return this.translator.getLocalizedMessage(this);
    }
}
