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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;

import org.hmx.scitos.core.option.Option;
import org.hmx.scitos.view.service.IOptionPanelService;
import org.hmx.scitos.view.service.IOptionPanelServiceProvider;
import org.hmx.scitos.view.swing.ScitosClient;
import org.hmx.scitos.view.swing.util.ViewUtil;

/**
 * Swing handler representing the {@code Preferences} entry in the JMenuBar; opened JFrame offers the opportunity to set default values for a new
 * analysis and customizing the appearance of the {@link ScitosClient} and its displayed content.
 */
public final class OptionView {

    /** The dialog opened and filled with content by the constructor. */
    final OptionViewDialog dialog;
    /** The selectable nodes in the left hand tree. */
    private final List<DefaultMutableTreeNode> optionNodes;

    /**
     * Main constructor.
     *
     * @param parent
     *            parent {@link ScitosClient} to refresh the chosen LookAndFeel
     * @param optionPanelProvider
     *            provider for the variety of option groups
     */
    private OptionView(final ScitosClient parent, final IOptionPanelServiceProvider optionPanelProvider) {
        this.dialog = new OptionViewDialog(parent.getFrame(), new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                // save action of the OK button on the SplitFrame
                if (OptionView.this.saveOptions()) {
                    OptionView.this.dialog.close();
                }
            }
        });
        this.dialog.setModal(true);
        this.optionNodes = new LinkedList<DefaultMutableTreeNode>();
        DefaultMutableTreeNode firstNode = null;
        for (final IOptionPanelService singleService : optionPanelProvider.getServices()) {
            // adding node to the category tree
            final DefaultMutableTreeNode node = new DefaultMutableTreeNode();
            final AbstractOptionPanel panel = singleService.createOptionPanel(parent, this);
            node.setUserObject(panel);
            this.optionNodes.add(node);
            this.dialog.insertTreeNode(node);
            if (firstNode == null) {
                firstNode = node;
            }
        }
        this.dialog.setSelectedNode(firstNode);
        this.dialog.setMinimumSize(new Dimension(600, 400));
        this.dialog.pack();
        this.dialog.setSize(this.dialog.getWidth(), 600);
        ViewUtil.centerOnParent(this.dialog);
        // define the look and feel resetting action if the frame is closed
        this.dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(final WindowEvent event) {
                try {
                    UIManager.setLookAndFeel(Option.LOOK_AND_FEEL.getValue());
                    SwingUtilities.updateComponentTreeUI(parent.getFrame());
                    parent.revalidate();
                } catch (final Exception e) {
                    // ignore
                }
            }
        });
    }

    /**
     * Open a {@link OptionViewDialog} containing a selection tree on the left side (for the different preference categories) and the actual
     * preference panels on the right side.
     *
     * @param parent
     *            parent {@link ScitosClient} to refresh the chosen LookAndFeel
     * @param optionPanelProvider
     *            the provider of all preference categories (and their option panels)
     */
    public static void showPreferenceDialog(final ScitosClient parent, final IOptionPanelServiceProvider optionPanelProvider) {
        new OptionView(parent, optionPanelProvider).getDialog().setVisible(true);
    }

    /**
     * Getter for the actual dialog window.
     *
     * @return the option selection containing frame
     */
    public JDialog getDialog() {
        return this.dialog;
    }

    /**
     * Store the chosen preferences in the options file(s).
     *
     * @return if all chosen settings are valid and were successfully saved
     */
    boolean saveOptions() {
        for (final DefaultMutableTreeNode singleNode : this.optionNodes) {
            final AbstractOptionPanel singlePanel = (AbstractOptionPanel) singleNode.getUserObject();
            singlePanel.validateInput();
            if (!singlePanel.areChosenSettingsValid()) {
                this.dialog.setSelectedNode(singleNode);
                return false;
            }
        }
        for (final DefaultMutableTreeNode singleNode : this.optionNodes) {
            ((AbstractOptionPanel) singleNode.getUserObject()).submitChosenSettings();
        }
        return true;
    }
}
