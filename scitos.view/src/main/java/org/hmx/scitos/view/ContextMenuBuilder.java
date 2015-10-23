package org.hmx.scitos.view;

import java.awt.Font;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.hmx.scitos.core.HmxException;

/**
 * The {@code ContextMenuBuilder} allows the definition of a context menu regardless of the specific user interface implementation.
 */
public class ContextMenuBuilder implements Iterable<ContextMenuBuilder.CMenuEntry> {

    private final List<CMenuEntry> entryList = new LinkedList<CMenuEntry>();
    private final String caption;

    /**
     * Constructor: for a menu parent element without an optional title.
     */
    public ContextMenuBuilder() {
        this.caption = null;
    }

    /**
     * Constructor: for a menu parent with the given title/caption.
     *
     * @param caption
     *            the menu's title
     */
    public ContextMenuBuilder(final String caption) {
        this.caption = caption;
    }

    /** @return caption of the sub menu */
    public String getCaption() {
        return this.caption;
    }

    /**
     * @param subMenuCaption
     *            caption of the sub menu
     * @return created sub menu (capable of holding an own set of {@link CMenuEntry entries})
     */
    public CMenu addMenu(final String subMenuCaption) {
        return this.addEntry(new CMenu(subMenuCaption));
    }

    /**
     * @param itemCaption
     *            caption of the single menu item
     * @param action
     *            {@link Runnable executable action} on event of item selection
     * @return created menu item (capable of performing the given action on selection)
     */
    public CMenuItem addItem(final String itemCaption, final CMenuItemAction action) {
        return this.addEntry(new CMenuItem(itemCaption, action));
    }

    /** @return visual grouping element */
    public CMenuSeparator addSeparator() {
        return this.addEntry(new CMenuSeparator());
    }

    /**
     * Add the given menu entry to this container.
     *
     * @param entry
     *            the menu entry to add
     * @return the added menu entry
     */
    protected <E extends CMenuEntry> E addEntry(final E entry) {
        this.entryList.add(entry);
        return entry;
    }

    /** @return list of all defined {@link CMenuEntry context menu entries} */
    public List<CMenuEntry> getEntries() {
        return Collections.unmodifiableList(this.entryList);
    }

    @Override
    public Iterator<CMenuEntry> iterator() {
        return this.getEntries().iterator();
    }

    /**
     * common interface for all kinds of menu entries ({@link CMenu}, {@link CMenuItem}, {@link CMenuSeparator})
     */
    public interface CMenuEntry {
        // nothing specific
    }

    /**
     * basically another form of the Runnable interface, only with the addition of the general {@link HmxException}.
     */
    public interface CMenuItemAction {

        /**
         * execute the actual action following a selection of the designated {@link CMenuItem context menu item}
         *
         * @throws HmxException
         *             occurred error in processing the menu item selection event
         */
        void processSelectEvent() throws HmxException;
    }

    /** sub menu containing a number of other {@link CMenuEntry entries} */
    public static final class CMenu extends ContextMenuBuilder implements CMenuEntry {

        private String toolTip = null;

        CMenu(final String caption) {
            super(caption);
        }

        /**
         * @return if the optional help text - possibly displayed as tool tip - is set
         */
        public boolean hasToolTip() {
            return this.toolTip != null;
        }

        /**
         * @return help text - possibly displayed as tool tip (can be {@code null})
         */
        public String getToolTip() {
            return this.toolTip;
        }

        /**
         * @param toolTip
         *            help text - possibly displayed as tool tip
         * @return self reference
         */
        public CMenu setToolTip(final String toolTip) {
            this.toolTip = toolTip;
            return this;
        }
    }

    /**
     * menu item capable of performing a predefined {@link CMenuItemAction action} on selection
     */
    public static final class CMenuItem implements CMenuEntry {

        private final String caption;
        private final CMenuItemAction action;
        private String toolTip = null;
        private Font font = null;
        private int fontStyle = Font.PLAIN;

        CMenuItem(final String caption, final CMenuItemAction action) {
            this.caption = caption;
            this.action = action;
        }

        /** @return caption of the single menu item */
        public String getCaption() {
            return this.caption;
        }

        /**
         * @return {@link CMenuItemAction executable action} on event of item selection
         */
        public CMenuItemAction getAction() {
            return this.action;
        }

        /**
         * @return if the optional help text - possibly displayed as tool tip - is set
         */
        public boolean hasToolTip() {
            return this.toolTip != null && !this.toolTip.isEmpty();
        }

        /**
         * @return help text - possibly displayed as tool tip (can be {@code null})
         */
        public String getToolTip() {
            return this.toolTip;
        }

        /**
         * @param toolTip
         *            help text - possibly displayed as tool tip
         * @return self reference
         */
        public CMenuItem setToolTip(final String toolTip) {
            this.toolTip = toolTip;
            return this;
        }

        /** @return if the optional, specific {@link Font} is set */
        public boolean hasFont() {
            return this.font != null || this.fontStyle != Font.PLAIN;
        }

        /**
         * @param defaultFont
         *            Font to use (or derive from) if no specific Font would have been set
         * @return specific Font to apply (can be {@code null})
         */
        public Font getFont(final Font defaultFont) {
            return this.font == null ? defaultFont.deriveFont(this.fontStyle) : this.font;
        }

        /**
         * @param font
         *            specific Font to apply (can be {@code null})
         * @return self reference
         */
        public CMenuItem setFont(final Font font) {
            this.font = font;
            return this;
        }

        /**
         * @param fontStyle
         *            differing {@link Font} style ({@link Font#PLAIN}, {@link Font#BOLD}, {@link Font#ITALIC}, or a combination of the latter two)
         * @return self reference
         */
        public CMenuItem setFont(final int fontStyle) {
            this.fontStyle = fontStyle;
            return this;
        }
    }

    /**
     * entry for separating groups of other {@link CMenuEntry entries} without any additional functionality - usually displayed as a line
     */
    public static final class CMenuSeparator implements CMenuEntry {

        CMenuSeparator() {
            // no functionality - just a static line
        }
    }
}
