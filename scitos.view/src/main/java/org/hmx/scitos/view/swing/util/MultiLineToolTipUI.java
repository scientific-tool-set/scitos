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

package org.hmx.scitos.view.swing.util;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;

/**
 * Renders ToolTips:
 * <ul>
 * <li>with multiple lines</li>
 * <li>auto-width + max. width</li>
 * <li>line wrap</li>
 * <li>uses {@link JTextArea}</li>
 * </ul>
 * For ease of use insert the following lines into your Application:
 * 
 * <p>
 * MultiLineToolTipUI.setMaximumWidth(250);<br>
 * MultiLineToolTipUI.initialize();<br>
 * javax.swing.ToolTipManager.sharedInstance().setDismissDelay(20000);
 * </p>
 *
 * @author Dexter 2008 <a href= "http://www.java-forum.org/awt-swing-swt/82817-tooltips-als-hilfefunktion.html#post515996">Source</a>
 */
public final class MultiLineToolTipUI extends BasicToolTipUI {

    /**
     * The {@link UIManager} key to register under.
     */
    private static final String UIMANAGER_KEY = "ToolTipUI";
    /** The singleton instance of the UI definition. */
    private static MultiLineToolTipUI singleton = new MultiLineToolTipUI();

    /** The component to render tool tips on. */
    private CellRendererPane rendererPane;
    /** The component handling wrapping of the tool tip text in the given size constraints. */
    private JTextArea textArea;
    /** Specifically defined maximum width for a displayed tool tip. */
    private static int maximumWidth = 0;

    /**
     * Registers this ToolTipUI.
     */
    public static void initialize() {
        final Class<? extends MultiLineToolTipUI> cls = MultiLineToolTipUI.singleton.getClass();
        final String name = cls.getName();
        UIManager.put(MultiLineToolTipUI.UIMANAGER_KEY, name);
        UIManager.put(name, cls);
    }

    /**
     * Constructor for the singleton instance.
     */
    private MultiLineToolTipUI() {
        super();
    }

    /**
     * Conventional method for retrieving the UI for the given component's tool tip.
     * 
     * @param component
     *            the component to render a tool tip for
     * @return the singleton instance of this UI definition
     */
    public static ComponentUI createUI(final JComponent component) {
        return MultiLineToolTipUI.singleton;
    }

    @Override
    public void installUI(final JComponent component) {
        super.installUI(component);
        this.rendererPane = new CellRendererPane();
        component.add(this.rendererPane);
    }

    @Override
    public void uninstallUI(final JComponent component) {
        super.uninstallUI(component);
        component.remove(this.rendererPane);
    }

    /**
     * Setter for the maximum width. Setting it to {@code 0} disables the enforcement of a maximum width.
     *
     * @param width
     *            maximum width to set
     */
    public static void setMaximumWidth(final int width) {
        MultiLineToolTipUI.maximumWidth = width;
    }

    @Override
    public void paint(final Graphics graphics, final JComponent component) {
        final Dimension size = component.getSize();
        this.textArea.setBackground(component.getBackground());
        this.rendererPane.paintComponent(graphics, this.textArea, component, 1, 1, size.width - 1, size.height - 1, true);
    }

    @Override
    public Dimension getPreferredSize(final JComponent component) {
        final String tipText = ((JToolTip) component).getTipText();
        if (tipText == null) {
            return new Dimension(0, 0);
        }
        this.textArea = new JTextArea(tipText);
        this.textArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        this.rendererPane.removeAll();
        this.rendererPane.add(this.textArea);
        this.textArea.setWrapStyleWord(true);
        this.textArea.setLineWrap(false);

        if (MultiLineToolTipUI.maximumWidth > 0 && MultiLineToolTipUI.maximumWidth < this.textArea.getPreferredSize().getWidth()) {
            this.textArea.setLineWrap(true);
            final Dimension oneLineDim = this.textArea.getPreferredSize();
            oneLineDim.width = MultiLineToolTipUI.maximumWidth;
            oneLineDim.height++;
            this.textArea.setSize(oneLineDim);
        }
        final Dimension dim = this.textArea.getPreferredSize();
        dim.height += 1;
        dim.width += 1;
        return dim;
    }

    @Override
    public Dimension getMinimumSize(final JComponent component) {
        return this.getPreferredSize(component);
    }

    @Override
    public Dimension getMaximumSize(final JComponent component) {
        return this.getPreferredSize(component);
    }
}
