package org.hmx.scitos.view.swing.util;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;

/**
 * Renders ToolTips:<br>
 * - with multiple lines<br>
 * - autowidth + max. width<br>
 * - linewrap<br>
 * - uses jTextArea
 * <p>
 * For ease of use insert the following lines into you Application:
 * <p>
 * MultiLineToolTipUI.setMaximumWidth(250);<br>
 * MultiLineToolTipUI.initialize();<br>
 * javax.swing.ToolTipManager.sharedInstance().setDismissDelay(20000);
 *
 * @author Dexter 2008 <a href= "http://www.java-forum.org/awt-swing-swt/82817-tooltips-als-hilfefunktion.html#post515996" >Source</a>
 */
public final class MultiLineToolTipUI extends BasicToolTipUI {

    private static final String UIMANAGER_KEY = "ToolTipUI";

    private CellRendererPane rendererPane;
    private JTextArea textArea;
    private static int maximumWidth = 0;

    private static MultiLineToolTipUI singleton = new MultiLineToolTipUI();

    /**
     * registers this ToolTipUI.
     */
    public static void initialize() {
        final Class<? extends MultiLineToolTipUI> cls = MultiLineToolTipUI.singleton.getClass();
        final String name = cls.getName();
        UIManager.put(MultiLineToolTipUI.UIMANAGER_KEY, name);
        UIManager.put(name, cls);
    }

    /**
     * constructor for the singleton instance.
     */
    private MultiLineToolTipUI() {
        super();
    }

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
     * set maximum width 0 = no maximum width
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
        this.textArea.setBorder(new EmptyBorder(2, 2, 2, 2));
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
