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

    /** The menu's entries - menu items, separators and/or submenus. */
    private final List<CMenuEntry> entryList = new LinkedList<CMenuEntry>();
    /** The menu's title/caption. */
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

    /**
     * Getter for the title/caption of this menu.
     *
     * @return title/caption of this menu
     */
    public String getCaption() {
        return this.caption;
    }

    /**
     * Append a submenu with the given title/caption.
     *
     * @param subMenuCaption
     *            caption of the sub menu
     * @return created sub menu (capable of holding an own set of {@link CMenuEntry entries})
     */
    public CMenu addMenu(final String subMenuCaption) {
        return this.addEntry(new CMenu(subMenuCaption));
    }

    /**
     * Append an item with the given title/caption and the specified action.
     *
     * @param itemCaption
     *            caption of the single menu item
     * @param action
     *            {@link Runnable executable action} on event of item selection
     * @return created menu item (capable of performing the given action on selection)
     */
    public CMenuItem addItem(final String itemCaption, final CMenuItemAction action) {
        return this.addEntry(new CMenuItem(itemCaption, action));
    }

    /**
     * Append a separator to this menu.
     *
     * @return visual grouping element
     */
    public CMenuSeparator addSeparator() {
        return this.addEntry(new CMenuSeparator());
    }

    /**
     * Add the given entry to this menu.
     *
     * @param <E>
     *            the specific type of menu entry
     * @param entry
     *            the menu entry to add
     * @return the added menu entry
     */
    protected <E extends CMenuEntry> E addEntry(final E entry) {
        this.entryList.add(entry);
        return entry;
    }

    /**
     * Getter for the list of contained menu entries.
     *
     * @return list of all defined {@link CMenuEntry menu entries}
     */
    public List<CMenuEntry> getEntries() {
        return Collections.unmodifiableList(this.entryList);
    }

    @Override
    public Iterator<CMenuEntry> iterator() {
        return this.getEntries().iterator();
    }

    /**
     * Returns {@code true} if this menu builder contains no entries.
     *
     * @return whether no entries have been added yet
     */
    public boolean isEmpty() {
        return this.entryList.isEmpty();
    }

    /**
     * Common interface for all kinds of menu entries.
     */
    public interface CMenuEntry {
        // nothing specific
    }

    /**
     * Basically another form of the Runnable interface, only with the addition of the general {@link HmxException} to be thrown.
     */
    public interface CMenuItemAction {

        /**
         * Execute the actual action following a selection of the designated {@link CMenuItem menu item}.
         *
         * @throws HmxException
         *             occurred error in processing the menu item selection event
         */
        void processSelectEvent() throws HmxException;
    }

    /**
     * Sub menu containing a number of other {@link CMenuEntry entries}.
     */
    public static final class CMenu extends ContextMenuBuilder implements CMenuEntry {

        /** The optional tool tip of this sub menu. */
        private String toolTip = null;

        /**
         * Constructor.
         *
         * @param caption
         *            this sub menu's title/caption
         */
        CMenu(final String caption) {
            super(caption);
        }

        /**
         * Check if this sub menu provides a value for the optional tool tip.
         *
         * @return if the optional tool tip is set
         */
        public boolean hasToolTip() {
            return this.toolTip != null;
        }

        /**
         * Getter for the optional tool tip.
         *
         * @return tool tip (can be {@code null})
         */
        public String getToolTip() {
            return this.toolTip;
        }

        /**
         * Setter for the optional tool tip.
         *
         * @param toolTip
         *            the tool tip text to set
         * @return self reference
         */
        public CMenu setToolTip(final String toolTip) {
            this.toolTip = toolTip;
            return this;
        }
    }

    /**
     * Menu item capable of performing a predefined {@link CMenuItemAction action} on selection.
     */
    public static final class CMenuItem implements CMenuEntry {

        /** This item's title/caption. */
        private final String caption;
        /** The action to be performed when this item is selected by the user. */
        private final CMenuItemAction action;
        /** The optional tool tip. */
        private String toolTip = null;
        /**
         * The optional {@link Font} to be applied to the {@link #caption} - as alternative to the {@link #fontStyle}..
         */
        private Font font = null;
        /**
         * The optional {@link Font} style to be applied to the {@link #caption} - as alternative to the specific {@link #font}.
         */
        private int fontStyle = Font.PLAIN;

        /**
         * Constructor.
         *
         * @param caption
         *            the item's title/caption
         * @param action
         *            the action to be performed when this item is selected by the user
         */
        CMenuItem(final String caption, final CMenuItemAction action) {
            this.caption = caption;
            this.action = action;
        }

        /**
         * Getter for this item's title/caption.
         *
         * @return caption of this menu item
         */
        public String getCaption() {
            return this.caption;
        }

        /**
         * Getter for the action to be executed when this item is selected by the user.
         *
         * @return {@link CMenuItemAction executable action} on event of item selection
         */
        public CMenuItemAction getAction() {
            return this.action;
        }

        /**
         * Check if this menu item provides a value for the optional tool tip.
         *
         * @return if the optional tool tip is set
         */
        public boolean hasToolTip() {
            return this.toolTip != null && !this.toolTip.isEmpty();
        }

        /**
         * Getter for the optional tool tip.
         *
         * @return the optional tool tip (can be {@code null})
         */
        public String getToolTip() {
            return this.toolTip;
        }

        /**
         * Setter for the optional tool tip.
         *
         * @param toolTip
         *            tool tip text to set
         * @return self reference
         */
        public CMenuItem setToolTip(final String toolTip) {
            this.toolTip = toolTip;
            return this;
        }

        /**
         * Check if this menu item has a specific non-default {@link Font}.
         *
         * @return if the optional, specific {@link Font} is set
         */
        public boolean hasFont() {
            return this.font != null || this.fontStyle != Font.PLAIN;
        }

        /**
         * Getter for the specific non-default {@link Font} for this menu item.
         *
         * @param defaultFont
         *            {@link Font} to use (or derive from) if no specific {@link Font} would have been set
         * @return specific {@link Font} to apply
         */
        public Font getFont(final Font defaultFont) {
            return this.font == null ? defaultFont.deriveFont(this.fontStyle) : this.font;
        }

        /**
         * Setter for the specific non-default {@link Font} for this menu item.
         *
         * @param font
         *            specific Font to apply (can be {@code null})
         * @return self reference
         */
        public CMenuItem setFont(final Font font) {
            this.font = font;
            return this;
        }

        /**
         * Setter for the specific non-{@code plain} {@link Font} style.
         *
         * @param fontStyle
         *            differing {@link Font} style ({@link Font#BOLD BOLD}, {@link Font#ITALIC ITALIC}, or a combination of the two)
         * @return self reference
         */
        public CMenuItem setFont(final int fontStyle) {
            this.fontStyle = fontStyle;
            return this;
        }
    }

    /**
     * Entry for separating groups of other {@link CMenuEntry entries} without any additional functionality - usually displayed as a line.
     */
    public static final class CMenuSeparator implements CMenuEntry {

        /**
         * Constructor.
         */
        CMenuSeparator() {
            // no functionality - just a static line
        }
    }
}
