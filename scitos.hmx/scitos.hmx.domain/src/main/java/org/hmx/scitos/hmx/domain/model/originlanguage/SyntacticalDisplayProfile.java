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

package org.hmx.scitos.hmx.domain.model.originlanguage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Collection of available syntactical functions in the associated {@link LanguageModel}.
 */
public class SyntacticalDisplayProfile {

    /**
     * Name of the associated {@link LanguageModel} (i.e. the origin language) in this profile's {@code locale}.
     */
    private final String name;
    /**
     * The {@link Locale} (i.e. display language) this profile represents for the associated {@link LanguageModel}.
     */
    private final Locale locale;
    /** Look-up of syntactical functions/function groups by their references. */
    private final Map<AbstractSyntacticalElementReference, AbstractSyntacticalElement<?>> syntacticalElements;

    /**
     * Constructor: storing the given values and populating the look-up of syntactical function {@code code} values.
     *
     * @param name name of its {@link LanguageModel} in the given {@code locale}
     * @param locale represented display language of this profile's {@link LanguageModel}
     * @param elements available syntactical functions/function groups in the {@link LanguageModel}
     */
    public SyntacticalDisplayProfile(final String name, final Locale locale,
            final Map<AbstractSyntacticalElementReference, AbstractSyntacticalElement<?>> elements) {
        this.name = name;
        this.locale = locale;
        this.syntacticalElements = new HashMap<AbstractSyntacticalElementReference, AbstractSyntacticalElement<?>>(elements);
    }

    /**
     * Getter for the name of the associated {@link LanguageModel} in this profile's {@code locale}.
     *
     * @return name of the represented origin language
     */
    public String getName() {
        return this.name;
    }

    /**
     * Getter for the represented display language of the associated {@link LanguageModel}.
     *
     * @return display language
     */
    public Locale getLocale() {
        return this.locale;
    }

    /**
     * Provide the code to display for the given syntactical function reference.
     *
     * @param reference reference to a syntactical function
     * @return the short identifier (i.e. "code" value) to display
     */
    public String getSyntacticalFunctionCodeByReference(final SyntacticalFunctionReference reference) {
        final SyntacticalFunction element = (SyntacticalFunction) this.syntacticalElements.get(reference);
        return element.getCode();
    }

    public List<AbstractSyntacticalElement<?>> lookupFunctions(final List<AbstractSyntacticalElementReference> references) {
        final List<AbstractSyntacticalElement<?>> result = new ArrayList<AbstractSyntacticalElement<?>>(references.size());
        for (final AbstractSyntacticalElementReference singleReference : references) {
            final AbstractSyntacticalElement<?> element = this.syntacticalElements.get(singleReference);
            if (element instanceof SyntacticalFunctionGroupInfo) {
                final SyntacticalFunctionGroupReference groupReference = (SyntacticalFunctionGroupReference) singleReference;
                final List<AbstractSyntacticalElement<?>> subFunctions = this.lookupFunctions(groupReference.getSubFunctions());
                result.add(new SyntacticalFunctionGroup((SyntacticalFunctionGroupInfo) element, subFunctions));
            } else if (element instanceof SyntacticalFunction) {
                result.add(element);
            } else {
                throw new IllegalStateException("No valid match found for given reference: " + singleReference.getUuid());
            }
        }
        return result;
    }

    public boolean representsSameModel(final List<List<AbstractSyntacticalElementReference>> modelStructure) {
        try {
            for (final List<AbstractSyntacticalElementReference> references : modelStructure) {
                this.lookupFunctions(references);
            }
            return true;
        } catch (final IllegalStateException ise) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof SyntacticalDisplayProfile)) {
            return false;
        }
        final SyntacticalDisplayProfile otherProfile = (SyntacticalDisplayProfile) otherObject;
        return this.name.equals(otherProfile.name)
                && this.locale.equals(otherProfile.locale)
                && this.syntacticalElements.equals(otherProfile.syntacticalElements);
    }
}
