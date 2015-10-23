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

package org.hmx.scitos.core.util;

import java.awt.Color;
import java.util.Locale;

/**
 * Collection of utility functions for type conversions.
 */
public final class ConversionUtil {

    /**
     * Conversion: {@link Color} to human readable RGB representation.
     *
     * @param color
     *            the color to convert (can be {@code null})
     * @return textual representation 'rgb(X, X, X)' (is 'none' if {@code color} is {@code null})
     * @see #toColor(String, Color)
     */
    public static String toString(final Color color) {
        if (color == null) {
            return "none";
        }
        return String.format("rgb(%d, %d, %d)", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Re-conversion of the human readable RGB representation back to a {@link Color}:
     * <ol>
     * <li>if the given {@code text} matches the expected pattern 'rgb(X, X, X)', the represented {@link Color} will be returned,</li>
     * <li>if the given {@code text} equals 'none', {@code null} will be returned,</li>
     * <li>otherwise the provided {@code defaultValue} will be returned.</li>
     * </ol>
     *
     * @param text
     *            the text to interpret as color 'rgb(X, X, X)' ('none' will return {@code null})
     * @param defaultValue
     *            value to return if the given {@code text} is {@code null} or otherwise invalid
     * @return interpreted {@link Color} value (can be {@code null})
     * @see #toString(Color)
     */
    public static Color toColor(final String text, final Color defaultValue) {
        if ("none".equals(text)) {
            return null;
        }
        if (text != null) {
            final String[] colorValues = text.replaceAll("[^0-9]+", " ").trim().split(" ");
            if (colorValues.length == 3) {
                final int red = Integer.valueOf(colorValues[0]).intValue();
                final int green = Integer.valueOf(colorValues[1]).intValue();
                final int blue = Integer.valueOf(colorValues[2]).intValue();
                return new Color(red, green, blue);
            }
        }
        return defaultValue;
    }

    /**
     * Re-conversion of the textual representation of an {@link Integer}. The characters in the {@code text} must all be decimal digits, except that
     * the first character may be an ASCII minus sign '-' to indicate a negative value. Otherwise the given {@code defaultValue} will be returned.
     *
     * @param text
     *            the text to parse as integer
     * @param defaultValue
     *            value to return if the given {@code text} does not represent a valid integer value
     * @return interpreted integer value
     */
    public static int toInt(final String text, final int defaultValue) {
        if (text != null) {
            try {
                return Integer.parseInt(text);
            } catch (final NumberFormatException nfe) {
                // use default value instead
            }
        }
        return defaultValue;
    }

    /**
     * Re-conversion of a {@link Locale}'s textual representation into a matching instance.
     *
     * @param text
     *            the output from {@link Locale#toString()} to interpret as a {@link Locale}
     * @param defaultValue
     *            value to return if the given {@code text} is {@code null} or empty
     * @return interpreted {@link Locale} instance
     */
    public static Locale toLocale(final String text, final Locale defaultValue) {
        if (text == null || text.isEmpty()) {
            return defaultValue;
        }
        final int separatorIndex = text.indexOf('_');
        if (separatorIndex == -1) {
            // base scenario: Locale is only defined by the language
            return new Locale(text);
        }
        // extended scenario: Locale is a combination of language and country
        final String language = text.substring(0, separatorIndex);
        final String country;
        final int variantStart = text.indexOf('_', separatorIndex + 1);
        if (variantStart == -1) {
            country = text.substring(separatorIndex + 1);
        } else {
            // the provided text also contains a specific variant, which is ignored in this implementation
            country = text.substring(separatorIndex + 1, variantStart);
        }
        return new Locale(language, country);
    }
}
