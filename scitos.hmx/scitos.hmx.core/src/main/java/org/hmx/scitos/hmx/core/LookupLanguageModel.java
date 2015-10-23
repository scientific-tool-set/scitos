package org.hmx.scitos.hmx.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hmx.scitos.hmx.domain.model.LanguageModel;
import org.hmx.scitos.hmx.domain.model.SyntacticalFunction;

/**
 * Extension of the basic {@link LanguageModel} to allow an easy look up of syntactical functions by their code.
 */
public class LookupLanguageModel extends LanguageModel {

    /** The mapping of code values to their respective syntactical functions. */
    private final Map<String, SyntacticalFunction> functionsByCode;

    /**
     * Constructor: initializing an empty model with the given name and text orientation.
     *
     * @param name
     *            the name of this model (usually the represented language)
     * @param leftToRightOriented
     *            if the represented language's text orientation is left-to-right (otherwise right-to-left will be assumed)
     */
    public LookupLanguageModel(final String name, final boolean leftToRightOriented) {
        super(name, leftToRightOriented);
        this.functionsByCode = new HashMap<String, SyntacticalFunction>();
    }

    @Override
    public void reset(final List<? extends List<? extends SyntacticalFunction>> functions) {
        this.functionsByCode.clear();
        super.reset(functions);
    }

    @Override
    public void add(final List<? extends SyntacticalFunction> functions) {
        super.add(functions);
        this.addToMapping(functions);
    }

    /**
     * Create an internal mapping between the given syntactical functions and their code values.
     *
     * @param functions
     *            the syntactical functions to create look up mappings for
     * @see #getFunctionByCode(String)
     */
    private void addToMapping(final List<? extends SyntacticalFunction> functions) {
        for (final SyntacticalFunction singleFunction : functions) {
            final List<SyntacticalFunction> subFunctions = singleFunction.getSubFunctions();
            // only create mapping for function without any sub functions
            if (!subFunctions.isEmpty()) {
                this.addToMapping(subFunctions);
            } else if (singleFunction instanceof BackwardCompatibleFunction) {
                this.functionsByCode.put(((BackwardCompatibleFunction) singleFunction).getOldKey(), singleFunction);
            } else {
                this.functionsByCode.put(singleFunction.getCode(), singleFunction);
            }
        }
    }

    /**
     * Getter for a single syntactical function, that is represented by the given unique code.
     *
     * @param functionCode
     *            code to get the associated syntactical function for
     * @return the function represented by the given code
     */
    public SyntacticalFunction getFunctionByCode(final String functionCode) {
        return this.functionsByCode.get(functionCode);
    }

    /** Extension of a syntactical function by the old function's key from when HermeneutiX was a standalone application. */
    static final class BackwardCompatibleFunction extends SyntacticalFunction {

        /** The old function's message key to allow backwards compatibility. */
        private final String oldKey;

        /**
         * Constructor: wraps the given syntactical function for backwards compatibility.
         *
         * @param oldKey
         *            the old function's message key to allow backwards compatibility
         * @param code
         *            the short identifier used in the syntactical analysis
         * @param name
         *            the full identifier displayed when selecting this function in the syntactical analysis
         * @param underlined
         *            if an element with this syntactical function should be underlined
         * @param description
         *            the description of this function's meaning
         */
        BackwardCompatibleFunction(final String oldKey, final String code, final String name, final boolean underlined, final String description) {
            super(code, name, underlined, description, null);
            this.oldKey = oldKey;
        }

        /**
         * Getter for the old function's message key from when HermeneutiX was a standalone application.
         *
         * @return the old function's message key
         */
        String getOldKey() {
            return this.oldKey;
        }

        @Override
        public String toString() {
            return "oldKey: '" + this.oldKey + "' - " + super.toString();
        }
    }
}
