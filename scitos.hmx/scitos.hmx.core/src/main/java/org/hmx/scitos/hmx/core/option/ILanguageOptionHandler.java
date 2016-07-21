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

package org.hmx.scitos.hmx.core.option;

import java.util.List;

import org.hmx.scitos.hmx.domain.model.LanguageModel;

/**
 * Interface of an element providing access to the available {@link LanguageModel}s, separated by their modifiability (i.e. either only unmodifiable
 * system defined models or modifiable user defined ones).
 */
public interface ILanguageOptionHandler {

    /**
     * Getter for the system defined {@link LanguageModel}s.
     *
     * @return the unmodifiable system default models
     */
    List<LanguageModel> getSystemModels();

    /**
     * Getter for the user defined {@link LanguageModel}s.
     *
     * @return the modifiable user models
     */
    List<LanguageModel> getUserModels();
}
