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

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.hmx.scitos.core.i18n.Message;

/**
 * This {@link JFrame} presets a category tree on the left side to be filled with {@link DefaultMutableTreeNode}s containing NamedJPanels or
 * {@link String}s as user objects.<br />
 * in case of NamedJPanels, they will be displayed in the content area on the right side when their node is selected
 */
@SuppressWarnings("serial")
public final class SplitFrame extends JDialog {

    /** The (invisible ) root node. of the category tree. */
    private final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
    /** The left hand tree to select a preference category/option panel in. */
    private final JTree categoryTree = new JTree(this.rootNode);
    /** Layout handling the different option panels. */
    private final CardLayout contentLayout = new CardLayout();
    /** The panel actually containing the option panels (applying the contentLayout).. */
    private final JPanel contentArea = new JPanel(this.contentLayout);
    /** The underlying two part panel, managing the divider between the categoryTree (left) and the contentArea (right). */
    private final JSplitPane splitArea = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    /** The confirm/okay button, capable of performing an action before closing this dialog. */
    private final JButton okButton = new JButton();
    /** The cancel button for discarding any dialog input and closing it. */
    private final JButton cancelButton = new JButton();

    /** The currently active/displayed panel in the contentArea. */
    private JPanel activeContent = null;

    /**
     * Constructor: creates a splitted frame with a {@link JTree} on the left side and a content panel on the right side, also two buttons at the
     * bottom ( <code>OK</code>, <code>CANCEL</code>); the <code>OK</code> -button is provided with the specified {@link ActionListener}, the
     * <code>CANCEL</code>-button just closes the frame.
     *
     * @param parentFrame
     *            main window to associate this dialog
     * @param title
     *            frame title to set
     * @param okListener
     *            {@link ActionListener} for the <code>OK</code>-button to set
     */
    public SplitFrame(final JFrame parentFrame, final String title, final ActionListener okListener) {
        super(parentFrame, title);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.init();
        if (okListener == null) {
            this.okButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent event) {
                    SplitFrame.this.close();
                }
            });
        } else {
            this.okButton.addActionListener(okListener);
        }
    }

    /**
     * Initialize the splitted view with the {@link JTree} and the content {@link JPanel}; also adds the <code>OK</code> and <code>CANCEL</code>
     * buttons to the bottom area.
     */
    private void init() {
        final JPanel contentPane = new JPanel(new GridBagLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        this.setContentPane(contentPane);
        final JScrollPane scrollableTree = new JScrollPane(this.categoryTree);
        // initialize the category tree
        this.categoryTree.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));
        this.categoryTree.setRootVisible(false);
        // remove graphical node icons
        final DefaultTreeCellRenderer iconRenderer = new DefaultTreeCellRenderer();
        iconRenderer.setOpenIcon(null);
        iconRenderer.setClosedIcon(null);
        iconRenderer.setLeafIcon(null);
        this.categoryTree.setCellRenderer(iconRenderer);
        this.categoryTree.setShowsRootHandles(true);
        // enable the activation of the user object in the selected node
        this.categoryTree.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(final TreeSelectionEvent event) {
                SplitFrame.this.treeSelectionChanged(event);
            }
        });
        scrollableTree.setBorder(BorderFactory.createLoweredBevelBorder());
        // set the initial size (height can be ignored)
        scrollableTree.setPreferredSize(new Dimension(120, 300));
        final GridBagConstraints doubleSpan = new GridBagConstraints();
        doubleSpan.fill = GridBagConstraints.BOTH;
        doubleSpan.weightx = 1;
        doubleSpan.weighty = 1;

        final JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 0, 5));
        // initialize the content area
        this.contentArea.setBorder(null);
        contentPanel.add(this.contentArea, doubleSpan);
        final JScrollPane scrollableContent = new JScrollPane(contentPanel);

        this.splitArea.setLeftComponent(scrollableTree);
        this.splitArea.setRightComponent(scrollableContent);
        this.splitArea.setBorder(null);
        contentPane.add(this.splitArea, doubleSpan);
        // initialize the ok and cancel buttons
        this.initButtons();
    }

    /** Initialize the bottom area of the splitted frame, containing the OK and CANCEL button. */
    private void initButtons() {
        final JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 30, 10, 30));

        // creates the CANCEL button
        this.cancelButton.setText(Message.CANCEL.get());
        this.cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                SplitFrame.this.close();
            }
        });
        final GridBagConstraints cancelConstraints = new GridBagConstraints();
        cancelConstraints.anchor = GridBagConstraints.SOUTHWEST;
        buttonPanel.add(this.cancelButton, cancelConstraints);

        // creates the spacing between the buttons
        final GridBagConstraints spacing = new GridBagConstraints();
        spacing.fill = GridBagConstraints.HORIZONTAL;
        spacing.weightx = 1;
        buttonPanel.add(new JPanel(), spacing);

        // creates the OK button
        this.okButton.setText(Message.OK.get());
        final GridBagConstraints okConstraints = new GridBagConstraints();
        okConstraints.anchor = GridBagConstraints.SOUTHEAST;
        okConstraints.gridx = 2;
        buttonPanel.add(this.okButton, okConstraints);

        // make sure the buttons have the same preferred size
        final Dimension cancelSize = this.cancelButton.getPreferredSize();
        final Dimension okSize = this.okButton.getPreferredSize();
        final Dimension uniSize = new Dimension(Math.max(cancelSize.height, okSize.height), Math.max(cancelSize.width, okSize.width));
        this.cancelButton.setSize(uniSize);
        this.okButton.setSize(uniSize);

        // insert buttons in content pane
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 1;
        this.getContentPane().add(buttonPanel, constraints);
    }

    /**
     * Add the specified node to the category tree, REQUIREMENT: defined JPanel as UserObject in the leaf nodes (else nothing happens when selected),
     * RECOMMENDED: only String or NamedJPanel as UserObject, because the tree displays the UserObjects using toString().
     *
     * @param node
     *            tree node to add
     */
    public void insertTreeNode(final DefaultMutableTreeNode node) {
        this.rootNode.add(node);
        final Object userObject = node.getUserObject();
        if (userObject instanceof JPanel) {
            this.contentArea.add((JPanel) userObject, userObject.toString());
        }
    }

    /**
     * Set the selected node in the category tree.
     *
     * @param node
     *            tree node to select
     */
    public void setSelectedNode(final DefaultMutableTreeNode node) {
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
    public JPanel getActiveContent() {
        return this.activeContent;
    }

    /** Set the visibility of the frame, also resetting the divider location. */
    @Override
    public void setVisible(final boolean visible) {
        if (visible) {
            this.splitArea.setDividerLocation(120);
        }
        super.setVisible(visible);
    }

    /** Close this dialog/splitted frame without further ado. */
    public void close() {
        this.dispose();
    }
}
