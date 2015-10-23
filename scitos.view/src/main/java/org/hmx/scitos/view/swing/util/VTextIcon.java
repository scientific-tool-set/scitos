package org.hmx.scitos.view.swing.util;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;

/**
 * VTextIcon is an Icon implementation which draws a short string vertically. It's useful for JTabbedPanes with LEFT or RIGHT tabs but can be used in
 * any component which supports Icons, such as JLabel or JButton You can provide a hint to indicate whether to rotate the string to the left or right,
 * or not at all, and it checks to make sure that the rotation is legal for the given string (for example, Chinese/Japanese/Korean scripts have
 * special rules when drawn vertically and should never be rotated)
 *
 * @author Lee Ann Rucker (original source: <a href="http://macdevcenter.com/pub/a/mac/2002/03/22/vertical_text.html">O Reilly Media</a>)
 */
public final class VTextIcon implements Icon, PropertyChangeListener {

    /** Available rotation options. */
    public enum Rotate {
        /** Don't rotate the characters at all. */
        NONE,
        /** Rotate text counter-clockwise by 90 degrees (top = left, bottom = right). */
        CLOCKWISE,
        /** Rotate text clockwise by 90 degrees (top = right, bottom = left). */
        COUNTER_CLOCKWISE;
    }

    /** Specific positions for single characters when being rotated. */
    private enum Position {
        /** No specific positioning. */
        NORMAL,
        /** The small kana characters that draw in the top right quadrant. */
        TOP_RIGHT,
        /** The Japanese punctuation in the far top right corner. */
        FAR_TOP_RIGHT;

        /** The small kana characters that draw in the top right quadrant. small a, i, u, e, o, tsu, ya, yu, yo, wa (katakana only) ka ke */
        private static final String DRAW_IN_TOP_RIGHT = "\u3041\u3043\u3045\u3047\u3049\u3063\u3083\u3085\u3087\u308E" + // hiragana
                "\u30A1\u30A3\u30A5\u30A7\u30A9\u30C3\u30E3\u30E5\u30E7\u30EE\u30F5\u30F6"; // katakana
        /** The Japanese punctuation being drawn in the far top right corner. comma, full stop */
        private static final String DRAW_IN_FAR_TOP_RIGHT = "\u3001\u3002";

        /**
         * Determine the required positioning for the given {@code symbol}.
         *
         * @param symbol
         *            the character to determine the position for
         * @return required type of positioning
         */
        static Position forCharacter(final char symbol) {
            if (Position.DRAW_IN_TOP_RIGHT.indexOf(symbol) >= 0) {
                return Position.TOP_RIGHT;
            }
            if (Position.DRAW_IN_FAR_TOP_RIGHT.indexOf(symbol) >= 0) {
                return Position.FAR_TOP_RIGHT;
            }
            return Position.NORMAL;
        }
    }

    /** The rotation angle of 90 degrees as radians, being applied when rotating characters/text. */
    private static final double NINETY_DEGREES = Math.toRadians(90.);
    /** The applied buffer space between characters in pixels. */
    private static final int SPACING = 5;

    /** The text being displayed */
    private String labelText;
    /** For efficiency, break the labelText into one-char strings to be passed to drawString individually. */
    private String[] labelCharacters;
    /** Roman characters should be centered when not rotated (Japanese fonts are monospaced). */
    private int[] labelCharacterWidths;
    /** Japanese half-height characters need to be shifted when drawn vertically. */
    private Position[] labelCharacterPositions;
    /** The overall width of this icon when drawn. */
    private int iconWidth;
    /** The overall height of this icon when drawn. */
    private int iconHeight;
    /** The height of a single character in the applied font. */
    private int singleCharacterHeight;
    /** The normal amount of space below the baseline, that is being reserved for a character in the applied font. */
    private int descent;
    /** The rotation being applied (either NONE, COUNTER_CLOCKWISE, or CLOCKWISE). */
    private Rotate rotation;
    /** The actual user interface component this icon is displayed on (to listen for changes of the applied font). */
    private final Component parentComponent;

    /**
     * Creates a {@code VTextIcon} for the specified {@code component} with the specified {@code label}. It sets the orientation to the default for
     * the string.
     *
     * @param component
     *            designated component displaying this icon
     * @param label
     *            the text to display
     * @see #verifyRotation
     */
    public VTextIcon(final Component component, final String label) {
        this(component, label, null);
    }

    /**
     * Creates a {@code VTextIcon} for the specified {@code component} with the specified {@code label}. It sets the orientation to the provided value
     * if it's legal for the string.
     *
     * @param component
     *            designated component displaying this icon
     * @param label
     *            the text to display
     * @param rotateHint
     *            preferred rotation (if deemed invalid, a more fitting rotation is applied based on the text's characters)
     * @see #verifyRotation
     */
    public VTextIcon(final Component component, final String label, final Rotate rotateHint) {
        this.parentComponent = component;
        this.labelText = label;
        this.rotation = VTextIcon.verifyRotation(label, rotateHint);
        this.calcDimensions();
        this.parentComponent.addPropertyChangeListener(this);
    }

    /**
     * sets the label to the given string, updating the orientation as needed and invalidating the layout if the size changes
     *
     * @param label
     *            the new text to display
     * @see #verifyRotation
     */
    public void setLabel(final String label) {
        this.labelText = label;
        // Make sure the current rotation is still legal
        this.rotation = VTextIcon.verifyRotation(label, this.rotation);
        this.recalcDimensions();
    }

    /** Checks for changes to the font on the {@code parentComponent} so that it can invalidate the layout if the size changes */
    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if ("font".equals(event.getPropertyName())) {
            this.recalcDimensions();
        }
    }

    /** Calculates the dimensions. If they've changed, invalidates the component. */
    void recalcDimensions() {
        final int wOld = this.getIconWidth();
        final int hOld = this.getIconHeight();
        this.calcDimensions();
        if (wOld != this.getIconWidth() || hOld != this.getIconHeight()) {
            this.parentComponent.invalidate();
        }
    }

    /** Calculate the dimensions of the icon, determined by the currently assigned label text and the performed rotation. */
    void calcDimensions() {
        final FontMetrics fm = this.parentComponent.getFontMetrics(this.parentComponent.getFont());
        this.singleCharacterHeight = fm.getAscent() + fm.getDescent();
        this.descent = fm.getDescent();
        if (this.rotation == Rotate.NONE) {
            final int len = this.labelText.length();
            final char[] data = new char[len];
            this.labelText.getChars(0, len, data, 0);
            // if not rotated, width is that of the widest char in the string
            this.iconWidth = 0;
            // we need an array of one-char strings for drawString
            this.labelCharacters = new String[len];
            this.labelCharacterWidths = new int[len];
            this.labelCharacterPositions = new Position[len];
            char ch;
            for (int i = 0; i < len; i++) {
                ch = data[i];
                this.labelCharacters[i] = String.valueOf(ch);
                this.labelCharacterWidths[i] = fm.charWidth(ch);
                if (this.labelCharacterWidths[i] > this.iconWidth) {
                    this.iconWidth = this.labelCharacterWidths[i];
                }
                this.labelCharacterPositions[i] = Position.forCharacter(ch);
            }
            // and height is the font height * the char count, + one extra leading at the bottom
            this.iconHeight = this.singleCharacterHeight * len + this.descent;
        } else {
            // if rotated, width is the height of the string
            this.iconWidth = this.singleCharacterHeight;
            // and height is the width, plus some buffer space
            this.iconHeight = fm.stringWidth(this.labelText) + 2 * VTextIcon.SPACING;
        }
    }

    /**
     * Draw the icon at the specified location. Icon implementations may use the Component argument to get properties useful for painting, e.g. the
     * foreground or background color.
     */
    @Override
    public void paintIcon(final Component component, final Graphics gc, final int posX, final int posY) {
        // We don't insist that it be on the same Component
        gc.setColor(component.getForeground());
        gc.setFont(component.getFont());
        switch (this.rotation) {
        case CLOCKWISE:
            gc.translate(posX + this.iconWidth, posY + this.iconHeight);
            ((Graphics2D) gc).rotate(-VTextIcon.NINETY_DEGREES);
            gc.drawString(this.labelText, VTextIcon.SPACING, -this.descent);
            ((Graphics2D) gc).rotate(VTextIcon.NINETY_DEGREES);
            gc.translate(-(posX + this.iconWidth), -(posY + this.iconHeight));
            break;
        case COUNTER_CLOCKWISE:
            gc.translate(posX, posY);
            ((Graphics2D) gc).rotate(VTextIcon.NINETY_DEGREES);
            gc.drawString(this.labelText, VTextIcon.SPACING, -this.descent);
            ((Graphics2D) gc).rotate(-VTextIcon.NINETY_DEGREES);
            gc.translate(-posX, -posY);
            break;
        default:
            int charY = posY + this.singleCharacterHeight;
            for (int index = 0; index < this.labelCharacters.length; index++) {
                final int tweak;
                // Special rules for Japanese - "half-height" characters (like ya, yu, yo in combinations)
                // should draw in the top-right quadrant when drawn vertically - they draw in the bottom-left normally
                switch (this.labelCharacterPositions[index]) {
                case NORMAL:
                    // Roman fonts should be centered. Japanese fonts are always monospaced.
                    gc.drawString(this.labelCharacters[index], posX + ((this.iconWidth - this.labelCharacterWidths[index]) / 2), charY);
                    break;
                case TOP_RIGHT:
                    // Should be 2, but they aren't actually half-height
                    tweak = this.singleCharacterHeight / 3;
                    gc.drawString(this.labelCharacters[index], posX + (tweak / 2), charY - tweak);
                    break;
                case FAR_TOP_RIGHT:
                    tweak = this.singleCharacterHeight - this.singleCharacterHeight / 3;
                    gc.drawString(this.labelCharacters[index], posX + (tweak / 2), charY - tweak);
                    break;
                default:
                    break;
                }
                charY += this.singleCharacterHeight;
            }
        }
    }

    /**
     * Returns the icon's width.
     *
     * @return an int specifying the fixed width of the icon.
     */
    @Override
    public int getIconWidth() {
        return this.iconWidth;
    }

    /**
     * Returns the icon's height.
     *
     * @return an int specifying the fixed height of the icon.
     */
    @Override
    public int getIconHeight() {
        return this.iconHeight;
    }

    /**
     * Returns the best rotation for the string (Rotate.NONE, Rotate.CLOCKWISE, Rotate.COUNTER_CLOCKWISE) This is public so you can use it to test a
     * string without creating a VTextIcon from http://www.unicode.org/unicode/reports/tr9/tr9-3.html When setting text using the Arabic script in
     * vertical lines, it is more common to employ a horizontal baseline that is rotated by 90¡ counterclockwise so that the characters are ordered
     * from top to bottom. Latin text and numbers may be rotated 90 degrees clockwise so that the characters are also ordered from top to bottom.
     * Rotation rules - Roman can rotate left, right, or none - default right (counterclockwise) - CJK can't rotate - Arabic must rotate - default
     * left (clockwise) from the online edition of _The Unicode Standard, Version 3.0_, file ch10.pdf page 4 Ideographs are found in three blocks of
     * the Unicode Standard... U+4E00-U+9FFF, U+3400-U+4DFF, U+F900-U+FAFF Hiragana is U+3040-U+309F, katakana is U+30A0-U+30FF from
     * http://www.unicode.org/unicode/faq/writingdirections.html East Asian scripts are frequently written in vertical lines which run from
     * top-to-bottom and are arrange columns either from left-to-right (Mongolian) or right-to-left (other scripts). Most characters use the same
     * shape and orientation when displayed horizontally or vertically, but many punctuation characters will change their shape when displayed
     * vertically. Letters and words from other scripts are generally rotated through ninety degree angles so that they, too, will read from top to
     * bottom. That is, letters from left-to-right scripts will be rotated clockwise and letters from right-to-left scripts counterclockwise, both
     * through ninety degree angles. Unlike the bidirectional case, the choice of vertical layout is usually treated as a formatting style; therefore,
     * the Unicode Standard does not define default rendering behavior for vertical text nor provide directionality controls designed to override such
     * behavior
     *
     * @param label
     *            the text to display (and potentially rotate)
     * @param rotateHint
     *            requested rotation
     * @return the {@code rotateHint} if it is legal (i.e. valid) for the gien {@code label}, otherwise best guess based on contained characters
     */
    public static Rotate verifyRotation(final String label, final Rotate rotateHint) {
        boolean hasMustRotate = false; // Arabic, etc
        for (final char ch : label.toCharArray()) {
            if (ch >= '\u0590' && ch <= '\u074F') {
                // e.g. Hebrew and Arabic
                hasMustRotate = true;
            } else if (ch >= '\u3040' && ch <= '\u30FF' || ch >= '\u3400' && ch <= '\u9FFF' || ch >= '\uF900' && ch <= '\uFAFF') {
                // If you mix Arabic with Chinese, you're on your own
                return Rotate.NONE;
            }
        }
        if (rotateHint == null || hasMustRotate && rotateHint == Rotate.NONE) {
            // The hint wasn't legal, or it was null
            return hasMustRotate ? Rotate.CLOCKWISE : Rotate.COUNTER_CLOCKWISE;
        }
        return rotateHint;
    }
}
