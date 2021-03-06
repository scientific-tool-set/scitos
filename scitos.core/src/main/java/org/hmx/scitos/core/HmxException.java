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

package org.hmx.scitos.core;

import org.hmx.scitos.core.i18n.ILocalizableMessage;
import org.hmx.scitos.core.i18n.Message;

/**
 * This exception types provides the ability to handle possible problems while avoiding to use the default java exception types.
 *
 * <p>
 * Thereby, it ensures that it includes a valid message file key to provide the user with a localized error message.
 * </p>
 */
public class HmxException extends Exception {

    /** The associated error message. */
    private final ILocalizableMessage message;

    /**
     * Constructor: setting its contained error message by referring to an entry in the language file.
     *
     * @param message
     *            message file entry to set
     */
    public HmxException(final ILocalizableMessage message) {
        this.message = message;
    }

    /**
     * Constructor: setting its contained error message by referring to an entry in the language file and storing the individual cause to display for
     * giving a more specific hint what went wrong.
     *
     * @param message
     *            message file entry to set
     * @param cause
     *            wrapped reason for this exception
     */
    public HmxException(final ILocalizableMessage message, final Throwable cause) {
        super(cause);
        this.message = message;
    }

    /**
     * Check if the contained error message is {@link Message#ERROR_UNKNOWN}.
     *
     * @return check result
     */
    public boolean isUnknown() {
        return this.message == Message.ERROR_UNKNOWN;
    }

    /**
     * Getter for the localized error message according to the contained message file entry and appends the (optional) causing error's message. The
     * same as {@link #getLocalizedMessage()}.
     *
     * @return localized error message, including the (optional) causing error's message
     */
    @Override
    public String getMessage() {
        return this.getLocalizedMessage();
    }

    /**
     * Getter for the localized error message according to the contained message file entry, and appends the (optional) causing error's message.
     *
     * @return the translated error message according to the contained message file entry and the (optional) causing error
     */
    @Override
    public String getLocalizedMessage() {
        String localizedMessage = this.message.get();
        if (this.getCause() != null) {
            final String causeMessage = this.getCause().getLocalizedMessage();
            if (causeMessage != null && !causeMessage.isEmpty()) {
                // separate from the causing error
                localizedMessage += "\n\n" + causeMessage;
            }
        }
        return localizedMessage;
    }
}
