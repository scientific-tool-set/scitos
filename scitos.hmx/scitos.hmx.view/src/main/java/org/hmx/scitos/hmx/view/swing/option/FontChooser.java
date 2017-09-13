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

package org.hmx.scitos.hmx.view.swing.option;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.java.dev.designgridlayout.DesignGridLayout;

import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

/**
 * A three-part panel with a combo box to select the fonts name, a slider to choose its size and an area displaying the current setting on a sample
 * text.
 */
public final class FontChooser extends JPanel {

    /** The drop down component to select the font family from. */
    private final JComboBox<String> fontTypeBox;
    /** The slider component to adjust the font size with. */
    final JSlider fontSizeSlider = new JSlider(8, 64);
    /** The (editable) sample text area to show case the current font selection on. */
    final JTextArea fontSample = new JTextArea(HmxMessage.FONT_SAMPLE_TEXT.get());

    /**
     * Constructor: initializing the component without applying a specific default value.
     */
    public FontChooser() {
        final String[] fontFamilyNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        Arrays.sort(fontFamilyNames);
        this.fontTypeBox = new JComboBox<String>(fontFamilyNames);
        AutoCompleteDecorator.decorate(this.fontTypeBox);
        this.fontTypeBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                FontChooser.this.setSampleFont();
            }
        });
        final JLabel fontSizeDisplay = new JLabel("  ", SwingConstants.TRAILING);
        fontSizeDisplay.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 5));
        this.fontSizeSlider.setVerifyInputWhenFocusTarget(true);
        this.fontSizeSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent event) {
                final int size = FontChooser.this.fontSizeSlider.getValue();
                fontSizeDisplay.setText((size < 10 ? " " : "") + String.valueOf(size));
                FontChooser.this.setSampleFont();
            }
        });
        this.fontSample.setLineWrap(true);
        this.fontSample.setWrapStyleWord(true);
        this.fontSample.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        this.fontSample.setRows(3);
        this.fontSample.setPreferredSize(new Dimension(1, 1));

        final DesignGridLayout layout = new DesignGridLayout(this);
        layout.row().grid(new JLabel(HmxMessage.SETTING_FONT_TYPE.get())).addMulti(this.fontTypeBox);
        layout.row().grid(new JLabel(HmxMessage.SETTING_FONT_SIZE.get())).addMulti(this.fontSizeSlider, fontSizeDisplay);
        layout.row().left().add(this.fontSample).fill();
    }

    /**
     * Setter for the font family and size.
     *
     * @param fontName
     *            the (family) name of the font to select
     * @param fontSize
     *            the size of the font to set
     */
    public void setSelection(final String fontName, final int fontSize) {
        this.fontTypeBox.setSelectedItem(fontName);
        this.fontSizeSlider.setValue(fontSize);
        this.setSampleFont();
    }

    /**
     * Getter for the currently selected {@link Font}.
     *
     * @return currently selected value
     */
    public Font getSelection() {
        return new Font(this.fontTypeBox.getSelectedItem().toString(), Font.PLAIN, this.fontSizeSlider.getValue());
    }

    /**
     * Apply the currently selected {@link Font} to the sample text area.
     */
    void setSampleFont() {
        this.fontSample.setFont(this.getSelection());
    }
}
