/*
   Copyright (C) 2016 HermeneutiX.org

   This file is part of SciToS.

   SciToS is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   SciToS is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with SciToS. If not, see <http://www.gnu.org/licenses/>.
 */

package org.hmx.scitos.hmx.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hmx.scitos.hmx.domain.model.AbstractSyntacticalFunctionElement;
import org.hmx.scitos.hmx.domain.model.LanguageModel;
import org.hmx.scitos.hmx.domain.model.SyntacticalFunction;
import org.hmx.scitos.hmx.domain.model.SyntacticalFunctionGroup;

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
    public void reset(final List<? extends List<? extends AbstractSyntacticalFunctionElement>> functions) {
        this.functionsByCode.clear();
        super.reset(functions);
    }

    @Override
    public void add(final List<? extends AbstractSyntacticalFunctionElement> functions) {
        super.add(functions);
        this.addToMapping(functions);
    }

    /**
     * Create an internal mapping between the given syntactical functions and their code values.
     *
     * @param functionElements
     *            the syntactical functions/function groups to create look up mappings for
     * @see #getFunctionByCode(String)
     */
    private void addToMapping(final List<? extends AbstractSyntacticalFunctionElement> functionElements) {
        for (final AbstractSyntacticalFunctionElement functionElement : functionElements) {
            // only create mapping for function without any sub functions
            if (functionElement instanceof SyntacticalFunctionGroup) {
                this.addToMapping(((SyntacticalFunctionGroup) functionElement).getSubFunctions());
            } else {
                final SyntacticalFunction singleFunction = (SyntacticalFunction) functionElement;
                final String mappingKey;
                if (singleFunction instanceof BackwardCompatibleFunction) {
                    mappingKey = ((BackwardCompatibleFunction) singleFunction).getOldKey();
                } else {
                    mappingKey = singleFunction.getCode();
                }
                this.functionsByCode.put(mappingKey, singleFunction);
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
            super(code, name, underlined, description);
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
            return new StringBuilder("oldKey: '").append(this.oldKey).append("' - ").append(super.toString()).toString();
        }
    }
}
