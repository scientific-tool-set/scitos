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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.hmx.scitos.hmx.domain.model.originlanguage.AbstractSyntacticalElement;
import org.hmx.scitos.hmx.domain.model.originlanguage.SyntacticalFunction;
import org.hmx.scitos.hmx.domain.model.originlanguage.SyntacticalDisplayProfile;
import org.hmx.scitos.hmx.domain.model.originlanguage.SyntacticalFunctionGroup;

/**
 * Extension of the {@link SyntacticalDisplayProfile} that may contain additional information for the sake of backwards compatibility.
 */
class CompatibleSyntacticalFunctionDisplayProfile extends SyntacticalDisplayProfile {

    /**
     * Constructor: just passing the given values to the super constructor.
     *
     * @param name name of its {@link LanguageModel} in the given {@code locale}
     * @param locale represented display language of this profile's {@link LanguageModel}
     * @param groups grouped list of available syntactical functions in the {@link LanguageModel}
     * @see SyntacticalDisplayProfile#SyntacticalFunctionDisplayProfile(boolean, String, Locale, List)
     */
    CompatibleSyntacticalFunctionDisplayProfile(final String name, final Locale locale,
            final List<? extends List<? extends AbstractSyntacticalElement>> groups) {
        super(name, locale, groups);
    }

    /**
     * Create a copy of this display profile while discarding any additional backwards compatibility information.
     *
     * @return plain {@link SyntacticalDisplayProfile}
     */
    public SyntacticalDisplayProfile discardBackwardCompatibilityInfo() {
        final List<List<AbstractSyntacticalElement>> thisFunctionGroups = this.provideFunctions();
        final List<List<AbstractSyntacticalElement>> cleanedFunctionGroups;
        cleanedFunctionGroups = new ArrayList<List<AbstractSyntacticalElement>>(thisFunctionGroups.size());
        for (final List<AbstractSyntacticalElement> singleGroup : thisFunctionGroups) {
            final List<AbstractSyntacticalElement> clonedGroup;
            clonedGroup = new ArrayList<AbstractSyntacticalElement>(singleGroup.size());
            for (final AbstractSyntacticalElement singleElement : singleGroup) {
                clonedGroup.add(this.getElementWithoutBackwardCompatibleInfo(singleElement));
            }
            cleanedFunctionGroups.add(clonedGroup);
        }
        return new SyntacticalDisplayProfile(this.getName(), this.getLocale(), cleanedFunctionGroups);
    }

    /**
     * Copy the given syntactical function or group while discarding any additional backwards compatibility information.
     *
     * @param element element to be copied
     * @return plain {@link SyntacticalFunction}/{@link SyntacticalFunctionGroup}
     */
    private AbstractSyntacticalElement getElementWithoutBackwardCompatibleInfo(final AbstractSyntacticalElement element) {
        if (element instanceof SyntacticalFunction) {
            final SyntacticalFunction function = (SyntacticalFunction) element;
            return new SyntacticalFunction(function.getReference(), function.getCode(), function.getName(), function.isUnderlined(),
                    function.getDescription());
        }
        final SyntacticalFunctionGroup group = (SyntacticalFunctionGroup) element;
        final List<AbstractSyntacticalElement> groupSubFunctions = group.getSubFunctions();
        final List<AbstractSyntacticalElement> cleanedSubFunctions;
        cleanedSubFunctions = new ArrayList<AbstractSyntacticalElement>(groupSubFunctions.size());
        for (final AbstractSyntacticalElement singleSubFunction : groupSubFunctions) {
            cleanedSubFunctions.add(this.getElementWithoutBackwardCompatibleInfo(singleSubFunction));
        }
        return new SyntacticalFunctionGroup(group.getName(), group.getDescription(), cleanedSubFunctions);
    }
}
