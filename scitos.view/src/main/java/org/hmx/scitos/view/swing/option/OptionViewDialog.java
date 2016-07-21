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

package org.hmx.scitos.view.swing.option;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import net.java.dev.designgridlayout.DesignGridLayout;

import org.hmx.scitos.core.i18n.Message;

/**
 * This {@link JDialog} presets a category tree on the left side to be filled with {@link DefaultMutableTreeNode}s containing an
 * {@link AbstractOptionPanel} or {@link String} as user object.<br />
 * In case of {@link AbstractOptionPanel}, it will be displayed in the content area on the right side when the respective node is selected.
 */
@SuppressWarnings("serial")
final class OptionViewDialog extends JDialog {

    /** The (invisible) root node. of the category tree. */
    private final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
    /** The left hand tree to select a preference category/option panel in. */
    private final JTree categoryTree = new JTree(this.rootNode);
    /** Layout handling the different option panels. */
    private final CardLayout contentLayout = new CardLayout();
    /** The panel actually containing the option panels (applying the contentLayout).. */
    private final JPanel contentArea = new JPanel(this.contentLayout);
    /** The confirm/okay button, capable of performing an action before closing this dialog. */
    private final JButton okButton = new JButton(Message.OK.get());
    /** The cancel button for discarding any dialog input and closing it. */
    private final JButton cancelButton = new JButton(Message.CANCEL.get());

    /** The currently active/displayed panel in the contentArea. */
    private JPanel activeContent = null;

    /**
     * Constructor: creates a splitted frame with a {@link JTree} on the left side and a content panel on the right side, also two buttons at the
     * bottom ( {@code OK}, {@code CANCEL}); the {@code OK} button is provided with the specified {@link ActionListener}, the {@code CANCEL}-button
     * just closes the frame.
     *
     * @param parentFrame
     *            main window to associate this dialog
     * @param okListener
     *            {@link ActionListener} for the {@code OK}-button to set
     */
    OptionViewDialog(final JFrame parentFrame, final ActionListener okListener) {
        super(parentFrame, Message.MENUBAR_PREFERENCES.get());
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.init();
        this.okButton.addActionListener(okListener);
    }

    /**
     * Initialize the splitted view with the {@link JTree} and the content {@link JPanel}; also adds the {@code OK} and {@code CANCEL} buttons to the
     * bottom area.
     */
    private void init() {
        // initialize the category tree
        this.categoryTree.setRootVisible(false);
        this.categoryTree.setShowsRootHandles(false);
        // remove graphical node icons
        final DefaultTreeCellRenderer iconRenderer = new DefaultTreeCellRenderer();
        iconRenderer.setOpenIcon(null);
        iconRenderer.setClosedIcon(null);
        iconRenderer.setLeafIcon(null);
        this.categoryTree.setCellRenderer(iconRenderer);
        // enable the activation of the user object in the selected node
        this.categoryTree.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(final TreeSelectionEvent event) {
                OptionViewDialog.this.treeSelectionChanged(event);
            }
        });
        final JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        this.setContentPane(contentPane);
        final JScrollPane scrollableTree = new JScrollPane(this.categoryTree);
        scrollableTree.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollableTree.setBorder(BorderFactory.createLoweredBevelBorder());
        this.categoryTree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        contentPane.add(scrollableTree, BorderLayout.WEST);
        this.contentArea.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, this.contentArea.getForeground()));
        contentPane.add(this.contentArea);
        final JPanel buttonBar = new JPanel();
        buttonBar.setBorder(null);
        final DesignGridLayout buttonLayout = new DesignGridLayout(buttonBar);
        buttonLayout.row().bar().left(this.cancelButton).right(this.okButton);
        contentPane.add(buttonBar, BorderLayout.SOUTH);
        this.cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                OptionViewDialog.this.close();
            }
        });
    }

    /**
     * Add the specified node to the category tree, REQUIREMENT: defined JPanel as UserObject in the leaf nodes (else nothing happens when selected),
     * RECOMMENDED: only String or NamedJPanel as UserObject, because the tree displays the UserObjects using toString().
     *
     * @param node
     *            tree node to add
     */
    void insertTreeNode(final DefaultMutableTreeNode node) {
        this.rootNode.add(node);
        if (node.getUserObject() instanceof AbstractOptionPanel) {
            final AbstractOptionPanel optionPanel = (AbstractOptionPanel) node.getUserObject();
            if (optionPanel.isResizeCapable()) {
                // prepare to display the option panel as-is, letting it handle any resizing/scrolling itself
                this.contentArea.add(optionPanel, optionPanel.toString());
            } else {
                // double-wrap the option panel, ensuring its capability to resize and be scrolled if required
                final JPanel panelWrapper = new JPanel(new BorderLayout());
                panelWrapper.setBorder(BorderFactory.createEmptyBorder(5, 15, 0, 5));
                panelWrapper.add(optionPanel);
                final JScrollPane scrollableWrapper = new JScrollPane(panelWrapper);
                scrollableWrapper.setBorder(null);
                this.contentArea.add(scrollableWrapper, optionPanel.toString());
            }
        }
    }

    /**
     * Set the selected node in the category tree.
     *
     * @param node
     *            tree node to select
     */
    void setSelectedNode(final DefaultMutableTreeNode node) {
        final TreePath path = new TreePath(node.getPath());
        this.categoryTree.setSelectionPath(path);
        this.applyTreeSelection(path);
    }

    /**
     * Swap the displayed content panel, if the selected node contains a {@link JPanel} as user object.
     *
     * @param event
     *            {@link TreeSelectionEvent} to handle
     */
    void treeSelectionChanged(final TreeSelectionEvent event) {
        this.applyTreeSelection(event.getPath());
    }

    /**
     * Swap the displayed content panel, if the selected node contains a {@link JPanel} as user object.
     *
     * @param selection
     *            selected node (in its hierarchical path form)
     */
    private void applyTreeSelection(final TreePath selection) {
        // check for user object
        final Object activated = ((DefaultMutableTreeNode) selection.getLastPathComponent()).getUserObject();
        // if there is no panel defined as user object, it might be just a topic node; current content can stay
        if (activated instanceof JPanel) {
            // show the activated content
            this.activeContent = (JPanel) activated;
            this.contentLayout.show(this.contentArea, this.activeContent.toString());
            this.activeContent.setVisible(true);
            this.contentArea.repaint();
        }
    }

    /**
     * Getter for the currently active/displayed panel in the content area.
     *
     * @return active content panel of the last selected tree node with a {@link JPanel} as user object
     */
    JPanel getActiveContent() {
        return this.activeContent;
    }

    /** Close this dialog without further ado. */
    void close() {
        this.dispose();
    }
}
