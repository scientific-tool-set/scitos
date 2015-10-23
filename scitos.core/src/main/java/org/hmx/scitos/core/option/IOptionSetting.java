/*
   Copyright (C) 2015 HermeneutiX.org

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

package org.hmx.scitos.core.option;

import java.awt.Color;

/**
 * Generic interface of a setting or preference to be persisted in an appropriate file to be available at the next application start.
 */
public interface IOptionSetting {

    /**
     * Getter for the actual settings key.
     *
     * @return settings key
     */
    String getKey();

    /**
     * Setter for this specific setting. The actual persistent storing might happen asynchronously.
     *
     * @param value
     *            value to set
     */
    void setValue(String value);

    /**
     * Getter for the value of this specific setting.
     *
     * @return option entry for the key; returns {@code null} if no entry was found and no default defined
     */
    String getValue();

    /**
     * Getter for the value of this specific setting.
     *
     * @return option entry for the key; returns {@code 0} if no entry was found and no default defined
     */
    int getValueAsInteger();

    /**
     * Getter for the value of this specific setting.
     *
     * @return option entry for the key; returns {@code null} if no entry was found, no default defined, or is set to be transparent
     */
    Color getValueAsColor();
}
