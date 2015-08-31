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

package org.hmx.scitos.view.swing.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * This class is a document for text fields to limit the maximum count of inserted characters with the additional ability to ignore a subset of them.
 *
 * <p>
 * to use: <code>JTextfield.setDocument(new Validation(int limit))</code> or
 * <code>JTextfield.setDocument(new Validation(int limit, String regEx))</code>
 * </p>
 *
 * @author Alexander Elsholz <a href="http://geocities.ws/uweplonus/faq/swing.html#zeichenBeschraenken">Source</a>
 */
public class Validation extends PlainDocument {

    /** Maximum count of characters in the object using this document. */
    private final int limit;
    /** Regular expression for characters to ignore when inserted in the object using this document. */
    private String regExToIgnore = null;

    /**
     * Constructor for the validation document.
     *
     * @param maximum
     *            maximum count of characters insertable
     */
    public Validation(final int maximum) {
        this(maximum, null);
    }

    /**
     * Constructor for the validation document.
     *
     * @param maximum
     *            maximum count of characters insertable
     * @param regEx
     *            regular expression to ignore in input
     */
    public Validation(final int maximum, final String regEx) {
        super();
        if (maximum < 0) {
            this.limit = 0;
        } else {
            this.limit = maximum;
        }
        if ((regEx != null) && (regEx.length() > 0)) {
            this.regExToIgnore = regEx;
        }
    }

    /**
     * Overrides insertString() method of PlainDocument regarding the maximum limit and ignoring all parts matching the stored regular expression.
     *
     * @param offset
     *            position to start the insertion
     * @param str
     *            string to insert
     * @param attr
     *            AttributSet of the string
     * @throws BadLocationException
     *             if offset got an invalid value
     */
    @Override
    public final void insertString(final int offset, final String str, final AttributeSet attr) throws BadLocationException {
        if (str == null) {
            return;
        }
        final String concat;
        if (this.regExToIgnore == null) {
            concat = str;
        } else {
            concat = str.replaceAll(this.regExToIgnore, "");
        }
        if ((this.getLength() + concat.length()) <= this.limit) {
            super.insertString(offset, concat, attr);
        } else {
            super.insertString(offset, concat.substring(0, this.limit - this.getLength()), attr);
        }
    }
}
