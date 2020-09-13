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

package org.hmx.scitos.hmx.view.swing.components;

import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.undo.UndoManager;

import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.core.option.Option;
import org.hmx.scitos.hmx.core.ILanguageModelProvider;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.core.option.HmxGeneralOption;
import org.hmx.scitos.hmx.domain.model.ClauseItem;
import org.hmx.scitos.hmx.domain.model.LanguageModel;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.view.swing.IUndoManagedView;
import org.hmx.scitos.view.swing.components.ScaledTextPane;
import org.hmx.scitos.view.swing.util.VTextIcon;

/**
 * View offering the possibility to insert a text to analyze, which is referred to as the {@code origin text} in the HermeneutiX project. The user
 * chooses the language model (text orientation and applicable syntactical functions) and {@link Font} for the origin text.
 */
public final class TextInputPanel extends JPanel implements IUndoManagedView {

    /** The default font type to select if none is elsewhere configured/preset. */
    private static final String DEFAULT_FONT = "Times New Roman";

    /**
     * The super ordinated main view representing the associated HermeneutiX project, in which this view is referred to a {@code text-input mode}.
     */
    final SingleProjectView parentView;
    /**
     * All selectable {@link LanguageModel}s, mapped by their names.
     */
    final Map<String, LanguageModel> languageModels;
    /** The text area to insert the origin text in. */
    final JTextPane originTextPane = new ScaledTextPane();
    /** The button for toggling the visibility of the settings area. */
    private final JToggleButton showOrHideSettingsButton = new JToggleButton();
    /**
     * The area on the right side containing the selection of the {@link LanguageModel} and {@link Font}.
     */
    private final JPanel settingArea = new JPanel(new GridBagLayout());
    /**
     * The selection component for the {@link #languageModels}.
     */
    private JComboBox<String> languageBox;
    /** The selection component for the origin text's font type. */
    private JComboBox<String> fontTypeBox;
    /** Available font types. */
    private List<String> fontFamilyNames;
    /** The selection component for setting the origin text's font size. */
    final JSlider fontSizeSlider = new JSlider();
    /** The button(s) for finishing the text input and switching to the analysis mode. */
    private final JButton[] buttons;
    /**
     * The vertical button label for the {@link #showOrHideSettingsButton} to make the {@link #settingArea} visible.
     */
    private VTextIcon showButtonIcon;
    /**
     * The vertical button label for the {@link #showOrHideSettingsButton} to hide the {@link #settingArea}.
     */
    private VTextIcon hideButtonIcon;
    /**
     * The {@link #originTextPane}'s undo manager.
     */
    UndoManager undoManager;
    /**
     * Flag indicating whether this panel is used for initializing a {@link Pericope} - i.e. not just for prepending/appending additional text.
     */
    private final boolean replaceCurrentText;

    /**
     * Constructor.
     *
     * @param parentView
     *            the super ordinated main view representing the associated HermeneutiX project, in which this view is referred to a
     *            {@code text-input mode}
     * @param replaceCurrentText
     *            whether this panel is used for initializing a {@link Pericope} - i.e. not just for prepending/appending additional text.
     * @param languageModelProvider
     *            provider of all selectable {@link LanguageModel}s
     */
    public TextInputPanel(final SingleProjectView parentView, final boolean replaceCurrentText, final ILanguageModelProvider languageModelProvider) {
        super(new GridBagLayout());
        this.parentView = parentView;
        this.replaceCurrentText = replaceCurrentText;
        if (this.replaceCurrentText) {
            this.languageModels = languageModelProvider.provideLanguageModels();
            this.buttons = new JButton[] { new JButton(HmxMessage.TEXTINPUT_START_BUTTON.get()) };
            this.buttons[0].addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent event) {
                    TextInputPanel.this.startAnalysis();
                }
            });
        } else {
            this.languageModels = new HashMap<>();
            final String language = parentView.getModel().getLanguage();
            final LanguageModel currentModel = new LanguageModel(language, parentView.getModel().isLeftToRightOriented());
            currentModel.addAll(parentView.getModel().provideFunctions());
            this.languageModels.put(language, currentModel);
            this.buttons = new JButton[3];
            this.buttons[0] = new JButton(HmxMessage.TEXTINPUT_BEFORE_BUTTON.get());
            this.buttons[0].addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent event) {
                    if (TextInputPanel.this.containsText()) {
                        parentView.addNewPropositions(TextInputPanel.this.getPropositionTexts(), true);
                    }
                }
            });
            this.buttons[1] = new JButton(HmxMessage.TEXTINPUT_BEHIND_BUTTON.get());
            this.buttons[1].addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent event) {
                    if (TextInputPanel.this.containsText()) {
                        parentView.addNewPropositions(TextInputPanel.this.getPropositionTexts(), false);
                    }
                }
            });
            this.buttons[2] = new JButton(Message.CANCEL.get());
            this.buttons[2].addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent event) {
                    // restart analysis with unaltered state
                    parentView.goToAnalysisView();
                }
            });
        }

        this.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        // create the topic label at the top left
        this.initTopicLabel();
        // create the text area for entering the origin text
        this.initOriginTextPane(this.buttons[0].getPreferredSize().height);
        // create the button to switch between visible and invisible settings
        this.initShowOrHideSettingsButton();
        // creating the setting area
        this.initSettingArea();
        // set default setting visibility
        this.manageSettingVisibility();
        if (this.replaceCurrentText) {
            final StringBuilder textBuilder = new StringBuilder(512);
            for (final Proposition singleProposition : this.parentView.getModel().getFlatText()) {
                final Iterator<ClauseItem> clauseItems = singleProposition.getItems().iterator();
                textBuilder.append(clauseItems.next().getOriginText());
                while (clauseItems.hasNext()) {
                    textBuilder.append("    ").append(clauseItems.next().getOriginText());
                }
                textBuilder.append('\n');
            }
            this.originTextPane.setText(textBuilder.toString().trim());
        }
        this.originTextPane.requestFocus();
    }

    /**
     * Ensure all currently displayed changes are represented as such in the underlying model objects - in order to be able to save the current state.
     */
    @Override
    public void submitChangesToModel() {
        if (this.replaceCurrentText) {
            // apply the default selected language model and font selection (to actually allow something to be saved this early)
            this.parentView.getModel().init(this.getPropositionTexts(), this.getLanguageModelSelection(), this.getFontSelection());
        }
        // if we are currently adding text to an existing model, the unchanged model will be saved
    }

    @Override
    public void refresh() {
        // nothing to refresh here
    }

    /**
     * Initialize the topic label at the top left position.
     */
    private void initTopicLabel() {
        final HmxMessage topicKey = this.replaceCurrentText ? HmxMessage.TEXTINPUT_TOPIC : HmxMessage.TEXTINPUT_TOPIC_ADD_PROPOSITIONS;
        final JLabel topicLabel = new JLabel(topicKey.get());
        topicLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 10));
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        this.add(topicLabel, constraints);
    }

    /**
     * Initialize the origin text area on the left side.
     *
     * @param bottomInset
     *            additional bottom inset
     */
    private void initOriginTextPane(final int bottomInset) {
        this.originTextPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.originTextPane.setName("Origin Text Input");
        final MutableAttributeSet attributes = this.originTextPane.getInputAttributes();
        StyleConstants.setLineSpacing(attributes, 1.5f);
        final StyledDocument document = this.originTextPane.getStyledDocument();
        document.setCharacterAttributes(0, document.getLength() + 1, attributes, true);
        // make sure the origin text pane is scrollable
        final JScrollPane scrollPane = new JScrollPane(this.originTextPane);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 0, 10 + bottomInset, 10);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridx = 0;
        constraints.gridy = 1;
        this.add(scrollPane, constraints);
        // provide addition functionality
        this.initUndoRedoOption();
    }

    /**
     * Enable the undo-/redo-option for the origin text area.
     */
    private void initUndoRedoOption() {
        // manage undo / redo
        this.undoManager = new UndoManager() {

            @Override
            public synchronized void undo() {
                super.undo();
                this.manageMenuStatus();
            }

            @Override
            public synchronized void redo() {
                super.redo();
                this.manageMenuStatus();
            }

            @Override
            public void undoableEditHappened(final UndoableEditEvent event) {
                super.undoableEditHappened(event);
                this.manageMenuStatus();
            }

            private void manageMenuStatus() {
                TextInputPanel.this.parentView.getProject().setSaved(false);
                TextInputPanel.this.parentView.getProject().manageMenuOptions();
            }
        };
        // set maximum of undoable events
        this.undoManager.setLimit(Math.max(100, Option.UNDO_LIMIT.getValueAsInteger()));
        this.originTextPane.getDocument().addUndoableEditListener(this.undoManager);
    }

    /**
     * Check if a rollback of the last change in the origin text area is possible.
     *
     * @return undo possible
     */
    @Override
    public boolean canUndo() {
        return this.undoManager.canUndo();
    }

    /**
     * Check if the last action in the origin text area was an {@link #undo()} call.
     *
     * @return redo possible
     */
    @Override
    public boolean canRedo() {
        return this.undoManager.canRedo();
    }

    /**
     * Execute rollback of the last change in the origin text area.
     */
    @Override
    public void undo() {
        // avoid exception
        if (this.canUndo()) {
            this.undoManager.undo();
        }
    }

    /**
     * Revert the last {@link #undo()} call in the origin text area.
     */
    @Override
    public void redo() {
        // avoid exception
        if (this.canRedo()) {
            this.undoManager.redo();
        }
    }

    /**
     * Initialize the button for toggling the setting sidebar's visibility on the right side.
     */
    private void initShowOrHideSettingsButton() {
        // increase the font size for the showOrHideSettingsButton by 2
        final Font defaultFont = this.showOrHideSettingsButton.getFont();
        this.showOrHideSettingsButton.setFont(new Font(defaultFont.getFontName(), defaultFont.getStyle(), defaultFont.getSize() + 2));
        /*
         * prepare the possible button texts and make sure the characters are in an vertical order
         */
        this.showButtonIcon = new VTextIcon(this.showOrHideSettingsButton, HmxMessage.TEXTINPUT_SHOW_BUTTON.get(), VTextIcon.Rotate.NONE);
        this.hideButtonIcon = new VTextIcon(this.showOrHideSettingsButton, HmxMessage.TEXTINPUT_HIDE_BUTTON.get(), VTextIcon.Rotate.NONE);

        this.showOrHideSettingsButton.setSelected(HmxGeneralOption.SHOW_SETTINGS.getValueAsBoolean());
        this.showOrHideSettingsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                TextInputPanel.this.manageSettingVisibility();
            }
        });

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.VERTICAL;
        constraints.weighty = 1;
        constraints.gridheight = 3;
        constraints.gridx = 1;
        constraints.gridy = 0;
        this.add(this.showOrHideSettingsButton, constraints);
    }

    /**
     * Initialize the setting side bar to the right of the button to set its visibility.
     */
    private void initSettingArea() {
        this.settingArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 8, 0));
        // initializes the combo box for choosing the origin language
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        this.settingArea.add(this.createLanguageComboBox(), constraints);
        // initializes the combo box for choosing the origin text font type
        constraints.gridy = 1;
        this.settingArea.add(this.createFontTypeComboBox(), constraints);
        // initializes the slider for choosing the origin text font size
        constraints.gridy++;
        this.settingArea.add(this.createFontSizeSlider(), constraints);
        // initializes the short hint text
        String hint = HmxMessage.TEXTINPUT_HINT.get();
        if (this.replaceCurrentText) {
            hint += "\n\n\n" + HmxMessage.TEXTINPUT_WARNING.get();
        }
        final JTextArea hintArea = new JTextArea(hint);
        hintArea.setLineWrap(true);
        hintArea.setWrapStyleWord(true);
        hintArea.setEditable(false);
        hintArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        final JScrollPane hintPanel = new JScrollPane(hintArea);
        hintPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 1;
        constraints.gridy++;
        this.settingArea.add(hintPanel, constraints);

        // initializes the button to switch in the analysis view
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weighty = 0;
        constraints.gridy++;
        for (final JButton singleButton : this.buttons) {
            constraints.gridy++;
            this.settingArea.add(singleButton, constraints);
        }
        // finally add the settingArea to this TextInputPanel
        final GridBagConstraints rightConstraints = new GridBagConstraints();
        rightConstraints.fill = GridBagConstraints.VERTICAL;
        rightConstraints.weighty = 1;
        rightConstraints.gridheight = 3;
        rightConstraints.gridx = 2;
        rightConstraints.gridy = 0;
        this.add(this.settingArea, rightConstraints);

        // set default language
        String defaultLanguage = this.parentView.getModel().getLanguage();
        if (defaultLanguage == null) {
            defaultLanguage = this.languageBox.getItemAt(0);
        }
        // listener on languageBox also presets the font type and size
        this.languageBox.setSelectedItem(defaultLanguage);
        this.setFontSelection();
        this.setOriginTextPaneOrientation();
    }

    /** Set the setting sidebar's visibility regarding to the current selection state of the associated button. */
    void manageSettingVisibility() {
        if (this.showOrHideSettingsButton.isSelected()) {
            this.settingArea.setVisible(true);
            this.showOrHideSettingsButton.setIcon(this.hideButtonIcon);
        } else {
            this.settingArea.setVisible(false);
            this.showOrHideSettingsButton.setIcon(this.showButtonIcon);
        }
    }

    /**
     * Create a {@link JPanel} containing the {@link JComboBox} for choosing the language of the origin text, which influences the available functions
     * of {@link ClauseItem} in the syntactical analysis and the overall text orientation.
     *
     * @return created {@link JPanel}
     */
    private JPanel createLanguageComboBox() {
        this.languageBox = new JComboBox<>(this.languageModels.keySet().toArray(new String[this.languageModels.size()]));
        this.languageBox.setName("Language Selection");
        this.languageBox.setEditable(false);
        if (this.languageModels.size() < 2) {
            this.languageBox.setEnabled(false);
        }
        this.languageBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                TextInputPanel.this.setOriginTextPaneOrientation();
                TextInputPanel.this.setFontSelection();
                TextInputPanel.this.parentView.getProject().setSaved(false);
            }
        });
        final JPanel languagePanel = new JPanel();
        languagePanel.setBorder(BorderFactory.createTitledBorder(HmxMessage.TEXTINPUT_LANGUAGE.get()));
        languagePanel.add(this.languageBox);
        return languagePanel;
    }

    /**
     * Apply the appropriate text orientation, based on the currently selected {@link LanguageModel} in the {@link #languageBox}.
     */
    void setOriginTextPaneOrientation() {
        final ComponentOrientation orientation;
        final LanguageModel selectedModel = this.getLanguageModelSelection();
        if (selectedModel == null || selectedModel.isLeftToRightOriented()) {
            orientation = ComponentOrientation.LEFT_TO_RIGHT;
        } else {
            orientation = ComponentOrientation.RIGHT_TO_LEFT;
        }
        this.originTextPane.setComponentOrientation(orientation);
    }

    /**
     * Create a {@link JPanel} containing the {@link JComboBox} for choosing the {@link Font} type of the origin text.
     *
     * @return created {@link JPanel}
     */
    private JPanel createFontTypeComboBox() {
        this.fontFamilyNames = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        Collections.sort(this.fontFamilyNames);
        this.fontTypeBox = new JComboBox<>(this.fontFamilyNames.toArray(new String[this.fontFamilyNames.size()]));
        this.fontTypeBox.setName("Font Type Selection");
        this.fontTypeBox.setEditable(false);
        this.fontTypeBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                TextInputPanel.this.setFontTypeAndStyle();
                TextInputPanel.this.parentView.getProject().setSaved(false);
            }
        });
        final JPanel fontTypePanel = new JPanel();
        fontTypePanel.setBorder(BorderFactory.createTitledBorder(HmxMessage.SETTING_FONT_TYPE.get()));
        fontTypePanel.add(this.fontTypeBox);
        return fontTypePanel;
    }

    /**
     * Set the font type and size selection based on the currently chosen {@link LanguageModel}.
     */
    void setFontSelection() {
        final LanguageModel selectedModel = this.getLanguageModelSelection();
        if (selectedModel == null) {
            return;
        }
        final Font currentFont = this.parentView.getModel().getFont();
        int fontIndex;
        if (currentFont == null) {
            fontIndex = -1;
        } else {
            fontIndex = this.fontFamilyNames.indexOf(currentFont.getFamily());
        }
        if (fontIndex == -1) {
            for (final String singleFont : selectedModel.getRecommendedFonts()) {
                if (this.fontFamilyNames.contains(singleFont)) {
                    fontIndex = this.fontFamilyNames.indexOf(singleFont);
                    break;
                }
            }
            if (fontIndex == -1) {
                fontIndex = Math.max(0, this.fontFamilyNames.indexOf(TextInputPanel.DEFAULT_FONT));
            }
        }
        this.fontTypeBox.setSelectedIndex(fontIndex);
        this.fontSizeSlider.setValue(currentFont == null ? 14 : currentFont.getSize());
    }

    /**
     * Create a {@link JPanel} containing the {@link JSlider} for adjusting the {@link Font} size of the origin text.
     *
     * @return created {@link JPanel}
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
                fontSizeDisplay.setText(String.valueOf(TextInputPanel.this.fontSizeSlider.getValue()));
                TextInputPanel.this.setFontTypeAndStyle();
                TextInputPanel.this.parentView.getProject().setSaved(false);
            }
        });
        final GridBagConstraints horizontalSpan = new GridBagConstraints();
        horizontalSpan.fill = GridBagConstraints.HORIZONTAL;
        horizontalSpan.weightx = 1;
        fontSizePanel.add(this.fontSizeSlider, horizontalSpan);
        fontSizePanel.add(fontSizeDisplay);
        return fontSizePanel;
    }

    /**
     * Set the {@link Font} of the origin text, as it configured by the {@link #fontTypeBox} and {@link #fontSizeSlider} in the setting sidebar.
     */
    void setFontTypeAndStyle() {
        this.originTextPane.setFont(new Font(this.fontTypeBox.getSelectedItem().toString(), Font.PLAIN, this.fontSizeSlider.getValue()));
    }

    /**
     * Initialize a new {@link Pericope} by transferring the inserted origin text and display the appropriate analysis view.
     */
    void startAnalysis() {
        if (this.containsText()) {
            final LanguageModel selectedModel = this.getLanguageModelSelection();
            // the value could be null if no language model could be loaded (neither system defined nor user defined ones) on initialization
            if (selectedModel != null) {
                this.parentView.startAnalysis(this.getPropositionTexts(), selectedModel, this.getFontSelection());
            }
        }
        // if the originTextArea is empty, do nothing
    }

    /**
     * Get the currently selected {@link LanguageModel} from the associated selection component.
     *
     * @return selected language model
     */
    private LanguageModel getLanguageModelSelection() {
        final Object languageName = this.languageBox.getSelectedItem();
        final LanguageModel selectedModel = languageName == null ? null : this.languageModels.get(languageName.toString());
        return selectedModel;
    }

    /**
     * Get the currently selected origin text {@link Font} – as specified by the chosen font type and the size slider's current value.
     *
     * @return the currently defined
     */
    private Font getFontSelection() {
        return new Font(this.fontTypeBox.getSelectedItem().toString(), Font.PLAIN, this.fontSizeSlider.getValue());
    }

    /**
     * Add the inserted origin text to the associated {@link Pericope}.
     *
     * @param inFront
     *            if the additional {@link Proposition}s should be added as the leading contents in the {@link Pericope}; otherwise they will be
     *            appended as trailing addition
     */
    void addOriginText(final boolean inFront) {
        if (this.containsText()) {
            this.parentView.addNewPropositions(this.getPropositionTexts(), inFront);
        }
        // if the originTextArea is empty, do nothing
    }

    /**
     * Check if the {@link #originTextPane} contains any text to be interpreted as {@link Proposition}s.
     *
     * @return if the origin text pane contains valid input text
     */
    public boolean containsText() {
        return !this.originTextPane.getText().trim().isEmpty();
    }

    /**
     * Retrieve the text from the {@link #originTextPane}, cleaning up multiple whitespaces. Occurrences of four or more spaces will be replaced by
     * tabstops, which in turn are later used to split a single {@link Proposition} into multiple {@link ClauseItem}s.
     *
     * @return inserted origin text
     */
    String getPropositionTexts() {
        // get the current input text
        String rawText = this.originTextPane.getText();
        // replace each substring containing at least four adjacent whitespaces
        rawText = rawText.replaceAll("    +", "\t");
        // reduce each remaining substring with multiple whitespaces to one
        rawText = rawText.replaceAll("  +", " ");
        return rawText;
    }
}
