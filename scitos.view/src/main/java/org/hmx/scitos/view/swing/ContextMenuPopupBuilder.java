package org.hmx.scitos.view.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.view.ContextMenuBuilder;
import org.hmx.scitos.view.ContextMenuBuilder.CMenu;
import org.hmx.scitos.view.ContextMenuBuilder.CMenuEntry;
import org.hmx.scitos.view.ContextMenuBuilder.CMenuItem;
import org.hmx.scitos.view.ContextMenuBuilder.CMenuItemAction;
import org.hmx.scitos.view.ContextMenuBuilder.CMenuSeparator;

/**
 * Utility class for converting a view implementation independent {@link ContextMenuBuilder} into a swing specific {@link JPopupMenu}.
 */
public class ContextMenuPopupBuilder {

    /**
     * Convert the given generic {@link ContextMenuBuilder} instance into a swing {@link JPopupMenu}.
     *
     * @param contextMenu
     *            generic context menu to convert
     * @return {@link JPopupMenu} representing the given generic context menu
     */
    public static JPopupMenu buildSwingPopupMenu(final ContextMenuBuilder contextMenu) {
        final JPopupMenu popup = new JPopupMenu(contextMenu.getCaption());
        for (final CMenuEntry singleEntry : contextMenu) {
            if (singleEntry instanceof CMenu) {
                popup.add(ContextMenuPopupBuilder.convertToSubMenu((CMenu) singleEntry));
            } else if (singleEntry instanceof CMenuItem) {
                popup.add(ContextMenuPopupBuilder.convertToMenuItem((CMenuItem) singleEntry));
            } else if (singleEntry instanceof CMenuSeparator) {
                popup.addSeparator();
            } else {
                throw new IllegalArgumentException("unknown context menu entry type " + singleEntry.getClass().getSimpleName());
            }
        }
        return popup;
    }

    /**
     * Convert the given generic {@link CMenu} instance into a swing {@link JMenu}.
     *
     * @param contextMenu
     *            generic sub menu to convert
     * @return {@link JMenu} representing the given generic sub menu
     */
    private static JMenu convertToSubMenu(final CMenu contextMenu) {
        final JMenu menu = new JMenu(contextMenu.getCaption());
        if (contextMenu.hasToolTip()) {
            menu.setToolTipText(contextMenu.getToolTip());
        }
        for (final CMenuEntry singleEntry : contextMenu) {
            if (singleEntry instanceof CMenu) {
                menu.add(ContextMenuPopupBuilder.convertToSubMenu((CMenu) singleEntry));
            } else if (singleEntry instanceof CMenuItem) {
                menu.add(ContextMenuPopupBuilder.convertToMenuItem((CMenuItem) singleEntry));
            } else if (singleEntry instanceof CMenuSeparator) {
                menu.addSeparator();
            } else {
                throw new IllegalArgumentException("unknown context menu entry type " + singleEntry.getClass().getSimpleName());
            }
        }
        return menu;
    }

    /**
     * Convert the given generic {@link CMenuItem} instance into a swing {@link JMenuItem}.
     *
     * @param contextMenuItem
     *            generic menu item to convert
     * @return {@link JMenuItem} representing the given generic menu item
     */
    private static JMenuItem convertToMenuItem(final CMenuItem contextMenuItem) {
        final JMenuItem item = new JMenuItem(contextMenuItem.getCaption());
        if (contextMenuItem.hasToolTip()) {
            item.setToolTipText(contextMenuItem.getToolTip());
        }
        if (contextMenuItem.hasFont()) {
            item.setFont(contextMenuItem.getFont(item.getFont()));
        }
        final CMenuItemAction genericAction = contextMenuItem.getAction();
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                try {
                    genericAction.processSelectEvent();
                } catch (final HmxException ex) {
                    MessageHandler.showException(ex);
                }
            }
        });
        return item;
    }
}
