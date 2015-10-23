package org.hmx.scitos.hmx.view.swing.option;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.hmx.scitos.hmx.core.i18n.HmxMessage;

/**
 * A three-part panel with a combo box to select the fonts name, a slider to choose its size and an area displaying the current setting on a sample
 * text.
 */
public final class FontChooser extends JPanel {

    /** The drop down component to select the font family from. */
    private final JComboBox fontTypeBox = new JComboBox();
    /** The slider component to adjust the font size with. */
    final JSlider fontSizeSlider = new JSlider();
    /** The (editable) sample text area to show case the current font selection on. */
    private final JTextArea fontSample = new JTextArea(HmxMessage.FONT_SAMPLE_TEXT.get());

    /**
     * Constructor: initializing the component without applying a specific default value.
     */
    public FontChooser() {
        super(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();
        // initializes the font type selection
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        this.add(this.createFontTypeComboBox(), constraints);
        // initializes the font size selection
        constraints.gridy = 1;
        this.add(this.createFontSizeSlider(), constraints);
        // creates a font sample
        this.fontSample.setLineWrap(true);
        this.fontSample.setWrapStyleWord(true);
        this.fontSample.setBorder(null);
        final JScrollPane scrollableSample =
                new JScrollPane(this.fontSample, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollableSample.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 1;
        constraints.gridy = 2;
        constraints.ipadx = -5;
        constraints.insets = new Insets(0, 2, 0, 3);
        this.add(scrollableSample, constraints);
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

    /**
     * Create a panel containing the drop down component for selecting the {@link Font}'s (family) type from.
     *
     * @return created panel
     */
    private JPanel createFontTypeComboBox() {
        final JPanel fontTypePanel = new JPanel();
        fontTypePanel.setBorder(BorderFactory.createTitledBorder(HmxMessage.SETTING_FONT_TYPE.get()));
        this.fontTypeBox.setEditable(false);
        final String[] fontFamilyNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        Arrays.sort(fontFamilyNames);
        for (final String singleFontName : fontFamilyNames) {
            this.fontTypeBox.addItem(singleFontName);
        }
        this.fontTypeBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                FontChooser.this.setSampleFont();
            }
        });
        fontTypePanel.add(this.fontTypeBox);
        return fontTypePanel;
    }

    /**
     * Create a panel containing the slider component for adjusting the {@link Font}'s size with.
     *
     * @return created panel
     */
    private JPanel createFontSizeSlider() {
        final JPanel fontSizePanel = new JPanel(new GridBagLayout());
        final JLabel fontSizeDisplay = new JLabel("  ", SwingConstants.TRAILING);
        fontSizeDisplay.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 5));
        fontSizePanel.setBorder(BorderFactory.createTitledBorder(HmxMessage.SETTING_FONT_SIZE.get()));
        this.fontSizeSlider.setMinimum(8);
        this.fontSizeSlider.setMaximum(64);
        this.fontSizeSlider.setVerifyInputWhenFocusTarget(true);
        this.fontSizeSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent event) {
                final int size = FontChooser.this.fontSizeSlider.getValue();
                fontSizeDisplay.setText(String.valueOf(size));
                FontChooser.this.setSampleFont();
            }
        });
        final GridBagConstraints horizontalSpan = new GridBagConstraints();
        horizontalSpan.fill = GridBagConstraints.HORIZONTAL;
        horizontalSpan.weightx = 1;
        fontSizePanel.add(this.fontSizeSlider, horizontalSpan);
        fontSizePanel.add(fontSizeDisplay);
        return fontSizePanel;
    }

}
