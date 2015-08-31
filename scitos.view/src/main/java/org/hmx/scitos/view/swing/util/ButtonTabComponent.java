/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hmx.scitos.view.swing.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.plaf.basic.BasicButtonUI;

import org.hmx.scitos.core.i18n.Message;

/**
 * Component to be used as tabComponent; Contains a JLabel to show the text and a JButton to close the tab it belongs to.<br />
 * Modifications from original class provided as example by Oracle: handling of LookAndFeel specific label text color.
 */
public class ButtonTabComponent extends JPanel {

    /** The tab pane component containing the tab this is the label/close-button for. */
    final JTabbedPane pane;
    /** The label displaying the tab content's title. */
    private final JLabel label;
    /** The button to close the associated tab. */
    final TabButton button;
    /** The last applied LookAndFeel color key, in order to avoid setting the label/button foreground color repeatedly. */
    private String lastColorKey;

    /**
     * Main constructor.
     *
     * @param pane
     *            tab stack containing the associated tab
     */
    public ButtonTabComponent(final JTabbedPane pane) {
        // avoid default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.pane = pane;
        this.setOpaque(false);
        // make JLabel read titles from JTabbedPane and use look and feel default colors
        this.label = new JLabel() {

            @Override
            public String getText() {
                final int index = pane.indexOfTabComponent(ButtonTabComponent.this);
                if (index == -1) {
                    return null;
                }
                return pane.getTitleAt(index);
            }
        };
        this.add(this.label);
        // add more space between the label and the button
        this.label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        // tab button
        this.button = new TabButton();
        this.add(this.button);
        // add more space to the top of the component
        this.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        final MouseAdapter mouseListener = new MouseAdapter() {

            @Override
            public void mousePressed(final MouseEvent event) {
                if (event.isPopupTrigger()) {
                    ButtonTabComponent.this.createPopupMenu().show(event.getComponent(), event.getX(), event.getY());
                }
            }

            @Override
            public void mouseReleased(final MouseEvent event) {
                if (event.isPopupTrigger()) {
                    ButtonTabComponent.this.createPopupMenu().show(event.getComponent(), event.getX(), event.getY());
                } else if (event.getButton() == MouseEvent.BUTTON1 && event.getComponent() != ButtonTabComponent.this.button) {
                    final int index = pane.indexOfTabComponent(ButtonTabComponent.this);
                    if (index != -1) {
                        pane.setSelectedIndex(index);
                    }
                }
            }
        };
        this.addMouseListener(mouseListener);
        this.label.addMouseListener(mouseListener);
        this.button.addMouseListener(mouseListener);
    }

    /**
     * Create a popup menu, offering the three close options: close this tab, close all other tabs, close all tabs.
     *
     * @return the created popup menu
     */
    JPopupMenu createPopupMenu() {
        final int tabIndex = this.pane.indexOfTabComponent(ButtonTabComponent.this);
        final JPopupMenu popup = new JPopupMenu(this.pane.getTitleAt(tabIndex));
        if (tabIndex != -1) {
            popup.add(new JMenuItem(Message.TAB_CLOSE.get())).addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent event) {
                    ButtonTabComponent.this.pane.remove(tabIndex);
                }
            });
            if (this.pane.getTabCount() > 1) {
                popup.add(new JMenuItem(Message.TAB_CLOSE_OTHERS.get())).addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent event) {
                        ButtonTabComponent.this.closeAllOtherTabs(tabIndex);
                    }
                });
                popup.add(new JMenuItem(Message.TAB_CLOSE_ALL.get())).addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent event) {
                        ButtonTabComponent.this.closeAllOtherTabs(-1);
                    }
                });
            }
        }
        return popup;
    }

    /**
     * Close all tab in the associated tab pane – with the option to leave the tab at the specified index open.
     *
     * @param tabIndexToLeaveOpen
     *            index of the single tab to leave open; if the index is invalid (e.g. <code>-1</code>), all tabs are being closed
     */
    void closeAllOtherTabs(final int tabIndexToLeaveOpen) {
        for (int tabIndex = this.pane.getTabCount() - 1; tabIndex > -1; tabIndex--) {
            if (tabIndex != tabIndexToLeaveOpen) {
                this.pane.remove(tabIndex);
            }
        }
    }

    @Override
    protected void paintChildren(final Graphics graphics) {
        final int index = this.pane.indexOfTabComponent(this);
        final int selection = this.pane.getSelectedIndex();
        String colorKey;
        if (index == selection) {
            colorKey = "TabbedPane.selectedTabTitleNormalColor";
        } else {
            colorKey = "TabbedPane.nonSelectedTabTitleNormalColor";
        }
        if (!colorKey.equals(this.lastColorKey)) {
            final Color color = UIManager.getLookAndFeelDefaults().getColor(colorKey);
            if (color != null) {
                this.label.setForeground(color);
                this.button.setForeground(color);
            }
            this.lastColorKey = colorKey;
        }
        super.paintChildren(graphics);
    }

    @Override
    public void updateUI() {
        this.lastColorKey = null;
        super.updateUI();
    }

    /** The (mostly) LookAndFeel independent button for closing the associated tab. */
    private class TabButton extends JButton implements ActionListener {

        /** Main constructor. */
        TabButton() {
            final int size = 17;
            this.setPreferredSize(new Dimension(size, size));
            this.setToolTipText(Message.TAB_CLOSE.get());
            // Make the button looks the same for all Laf's
            this.setUI(new BasicButtonUI());
            // Make it transparent
            this.setContentAreaFilled(false);
            // No need to be focusable
            this.setFocusable(false);
            this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            // Making nice rollover effect
            this.setBorderPainted(false);
            // we use the same listener for all buttons
            this.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseEntered(final MouseEvent event) {
                    final Component component = event.getComponent();
                    if (component == TabButton.this) {
                        TabButton.this.setBorderPainted(true);
                    }
                }

                @Override
                public void mouseExited(final MouseEvent event) {
                    final Component component = event.getComponent();
                    if (component == TabButton.this) {
                        TabButton.this.setBorderPainted(false);
                    }
                }
            });
            this.setRolloverEnabled(true);
            // default: Close the proper tab by clicking the button
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(final ActionEvent event) {
            final int index = ButtonTabComponent.this.pane.indexOfTabComponent(ButtonTabComponent.this);
            if (index != -1) {
                ButtonTabComponent.this.pane.remove(index);
            }
        }

        @Override
        public void updateUI() {
            // we don't want to update UI for this button
        }

        // paint the cross
        @Override
        protected void paintComponent(final Graphics graphics) {
            super.paintComponent(graphics);
            final Graphics2D graphics2D = (Graphics2D) graphics.create();
            // shift the image for pressed buttons
            if (this.getModel().isPressed()) {
                graphics2D.translate(1, 1);
            }
            graphics2D.setStroke(new BasicStroke(2));
            if (this.getModel().isRollover()) {
                graphics2D.setColor(Color.RED);
            }
            final int delta = 6;
            graphics2D.drawLine(delta, delta, this.getWidth() - delta - 1, this.getHeight() - delta - 1);
            graphics2D.drawLine(this.getWidth() - delta - 1, delta, delta, this.getHeight() - delta - 1);
            graphics2D.dispose();
        }
    }
}