/*
   Copyright (C) 2017 HermeneutiX.org

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

import org.hmx.scitos.hmx.domain.model.originlanguage.AbstractSyntacticalElement;
import org.hmx.scitos.hmx.domain.model.originlanguage.LanguageModel;
import org.hmx.scitos.hmx.domain.model.originlanguage.SyntacticalFunction;
import org.hmx.scitos.hmx.domain.model.originlanguage.SyntacticalDisplayProfile;
import org.hmx.scitos.hmx.domain.model.originlanguage.SyntacticalFunctionGroup;
import org.hmx.scitos.hmx.domain.model.originlanguage.SyntacticalFunctionReference;

/**
 * Extension of the basic {@link LanguageModel} to allow an easy look-up of syntactical function references.
 */
class CompatibleLanguageModel extends LanguageModel {

    /**
     * The mapping of (potentially old) look-up values to their respective syntactical function references.
     */
    private final Map<String, SyntacticalFunctionReference> functionReferences;

    /**
     * Constructor: initializing an empty model with the given name and text orientation.
     *
     * @param name
     *            the name of this model (usually the represented language)
     * @param leftToRightOriented
     *            if the represented language's text orientation is left-to-right (otherwise right-to-left will be assumed)
     */
    CompatibleLanguageModel(final boolean leftToRightOriented) {
        super(leftToRightOriented);
        this.functionReferences = new HashMap<String, SyntacticalFunctionReference>();
    }

    /**
     * Getter for a single syntactical function's proper reference, that is represented by the given unique identifier.
     *
     * @param reference
     *            reference to look-up the associated syntactical function for
     * @return equivalent syntactical function reference
     */
    SyntacticalFunctionReference getFunctionReference(final String reference) {
        return this.functionReferences.get(reference);
    }

    @Override
    public void addDisplayProfile(final SyntacticalDisplayProfile displayProfile) {
        final SyntacticalDisplayProfile plainDisplayProfile;
        if (displayProfile instanceof CompatibleSyntacticalFunctionDisplayProfile) {
            plainDisplayProfile = ((CompatibleSyntacticalFunctionDisplayProfile) displayProfile).discardBackwardCompatibilityInfo();
        } else {
            plainDisplayProfile = displayProfile;
        }
        super.addDisplayProfile(plainDisplayProfile);
        for (final List<AbstractSyntacticalElement<?>> functionElements : displayProfile.provideFunctions()) {
            this.addToMapping(functionElements);
        }
    }

    /**
     * Create an internal mapping between the given syntactical functions and their code values.
     *
     * @param functionElements
     *            the syntactical functions/function groups to create look up mappings for
     * @see #getFunctionReference(String)
     */
    private void addToMapping(final List<AbstractSyntacticalElement<?>> functionElements) {
        for (final AbstractSyntacticalElement<?> functionElement : functionElements) {
            // only create mapping for function without any sub functions
            if (functionElement instanceof SyntacticalFunctionGroup) {
                this.addToMapping(((SyntacticalFunctionGroup) functionElement).getSubFunctions());
            } else {
                final SyntacticalFunction singleFunction = (SyntacticalFunction) functionElement;
                final String oldKeyOrCode;
                if (singleFunction instanceof CompatibleSyntacticalFunction) {
                    // HermeneutiX up to version 1.12 was using display language independent key for the look-up
                    oldKeyOrCode = ((CompatibleSyntacticalFunction) singleFunction).getOldKey();
                } else {
                    // HermeneutiX versions 2.0 to 2.2 used the configured code
                    oldKeyOrCode = singleFunction.getCode();
                }
                // add backwards compatible look-up (for HermeneutiX up to version 2.2)
                this.functionReferences.put(oldKeyOrCode, singleFunction.getReference());
                // add proper look-up by UUID reference (as of HermeneutiX version 2.3+)
                this.functionReferences.put(singleFunction.getReference().getUuid().toString(), singleFunction.getReference());
            }
        }
    }
}
