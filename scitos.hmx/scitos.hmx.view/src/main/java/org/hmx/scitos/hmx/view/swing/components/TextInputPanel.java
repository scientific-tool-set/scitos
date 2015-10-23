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
import org.hmx.scitos.hmx.domain.model.LanguageModel;
import org.hmx.scitos.hmx.view.swing.HmxSwingProject;
import org.hmx.scitos.view.swing.components.ScaledTextPane;
import org.hmx.scitos.view.swing.util.VTextIcon;

/**
 * {@link JPanel} offering the possibility to paste or insert a text to analyse at the beginning of a HermeneutiX project<br>
 * in this view the user chooses the language, alignment, {@link Font} and {@link Font} size of the origin text which will be used and unchangeable
 * for the rest of the analysis.
 */
public final class TextInputPanel extends JPanel {

    private static final String DEFAULT_FONT = "Times New Roman";

    final SingleProjectView parentView;
    final Map<String, LanguageModel> languageModels;

    final ScaledTextPane originTextPane = new ScaledTextPane();
    private final JToggleButton showOrHideSettingsButton = new JToggleButton();
    private final JPanel settingArea = new JPanel(new GridBagLayout());
    final JComboBox languageBox = new JComboBox();
    private final JComboBox fontTypeBox = new JComboBox();
    private List<String> fontFamilyNames;
    final JSlider fontSizeSlider = new JSlider();
    private final JButton[] buttons;

    private VTextIcon showButtonIcon;
    private VTextIcon hideButtonIcon;

    UndoManager undoManager;

    /**
     * creates a new {@link TextInputPanel} by initializing all of its components and setting their default behavior and functionality
     *
     * @param project
     *            {@link HmxSwingProject} invoking this
     */
    public TextInputPanel(final SingleProjectView parentView, final ILanguageModelProvider languageModelProvider) {
        super(new GridBagLayout());
        this.parentView = parentView;
        final boolean newProject = parentView.getModel().getText().isEmpty();
        if (newProject) {
            this.languageModels = languageModelProvider.provideLanguageModels();
            this.buttons = new JButton[] { new JButton(HmxMessage.TEXTINPUT_START_BUTTON.get()) };
            this.buttons[0].addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent event) {
                    TextInputPanel.this.startAnalysis();
                }
            });
        } else {
            this.languageModels = new HashMap<String, LanguageModel>();
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
        this.initTopicLabel(newProject);
        // create the text area for entering the origin text
        this.initOriginTextPane(this.buttons[0].getPreferredSize().height);
        // create the button to switch between visible and invisible settings
        this.initShowOrHideSettingsButton();
        // creating the setting area
        this.initSettingArea(newProject);

        // set default setting visibility
        this.manageSettingVisibility();
        this.originTextPane.requestFocus();
    }

    /**
     * initializes the topic label at the top left position.
     *
     * @param newProject
     *            if this is the initial text input, else it is just adding more text to an existing project
     */
    private void initTopicLabel(final boolean newProject) {
        final HmxMessage topicKey = newProject ? HmxMessage.TEXTINPUT_TOPIC : HmxMessage.TEXTINPUT_TOPIC_ADD_PROPOSITIONS;
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
     * initializes the origin text area on the left side.
     *
     * @param bottomInset
     *            additional bottom inset
     */
    private void initOriginTextPane(final int bottomInset) {
        this.originTextPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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
     * enables the undo- and redo-option for the origin text area.
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
                TextInputPanel.this.parentView.getProject().manageMenuOptions();
            }
        };
        // set maximum of undoable events
        this.undoManager.setLimit(Math.max(100, Option.UNDO_LIMIT.getValueAsInteger()));
        this.originTextPane.getDocument().addUndoableEditListener(this.undoManager);
    }

    /**
     * check if a rollback of the last change in the origin text area is possible.
     *
     * @return undo possible
     */
    public boolean canUndo() {
        return this.undoManager.canUndo();
    }

    /**
     * check if the last action in the origin text area was an undo() call.
     *
     * @return redo possible
     */
    public boolean canRedo() {
        return this.undoManager.canRedo();
    }

    /**
     * execute rollback of the last change in the origin text area.
     */
    public void undo() {
        // avoid exception
        if (this.canUndo()) {
            this.undoManager.undo();
        }
    }

    /**
     * revert last undo() call in the origin text area.
     */
    public void redo() {
        // avoid exception
        if (this.canRedo()) {
            this.undoManager.redo();
        }
    }

    /**
     * initializes the button for changing the setting sidebars visibility on the right side.
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
     * initializes the setting side bar to the right of the button to set its visibility.
     *
     * @param newProject
     *            if this is the initial text input, else it is just adding more text to an existing project
     */
    private void initSettingArea(final boolean newProject) {
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
        this.settingArea.add(this.createFontTypeComboBox(newProject), constraints);
        // initializes the slider for choosing the origin text font size
        constraints.gridy++;
        this.settingArea.add(this.createFontSizeSlider(newProject), constraints);
        // initializes the short hint text
        String hint = HmxMessage.TEXTINPUT_HINT.get();
        if (newProject) {
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
        final String defaultLanguage;
        if (!newProject) {
            defaultLanguage = this.parentView.getModel().getLanguage();
            // } else TODO if (user setting for default language available) {
        } else {
            defaultLanguage = (String) this.languageBox.getItemAt(0);
        }
        // listener on languageBox also presets the font type and size
        this.languageBox.setSelectedItem(defaultLanguage);
        this.setOriginTextPaneOrientation();
    }

    /**
     * sets the setting sidebars visibility regarding to the current selection state of the responsible button.
     */
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
     * creates a {@link JPanel} containing the {@link JComboBox} for choosing the language of the origin text, which influences the available
     * functions of ClauseItems in the syntactical analysis.
     *
     * @return {@link JComboBox} containing {@link JPanel}
     */
    private JPanel createLanguageComboBox() {
        final JPanel languagePanel = new JPanel();
        languagePanel.setBorder(BorderFactory.createTitledBorder(HmxMessage.TEXTINPUT_LANGUAGE.get()));
        this.languageBox.setEditable(false);
        for (final String singleModelName : this.languageModels.keySet()) {
            this.languageBox.addItem(singleModelName);
        }
        if (this.languageModels.size() < 2) {
            this.languageBox.setEnabled(false);
        }
        this.languageBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                TextInputPanel.this.setOriginTextPaneOrientation();
                TextInputPanel.this.setFontSelection();
            }
        });
        languagePanel.add(this.languageBox);
        return languagePanel;
    }

    void setOriginTextPaneOrientation() {
        final ComponentOrientation orientation;
        final Object selectedEntry = this.languageBox.getSelectedItem();
        if (selectedEntry == null) {
            orientation = ComponentOrientation.LEFT_TO_RIGHT;
        } else {
            final LanguageModel selectedModel = this.languageModels.get(selectedEntry);
            if (selectedModel == null || selectedModel.isLeftToRightOriented()) {
                orientation = ComponentOrientation.LEFT_TO_RIGHT;
            } else {
                orientation = ComponentOrientation.RIGHT_TO_LEFT;
            }
        }
        this.originTextPane.setComponentOrientation(orientation);
    }

    /**
     * creates a {@link JPanel} containing the {@link JComboBox} for choosing the {@link Font} type of the origin text.
     *
     * @param newProject
     *            if this is the initial text input, else it is just adding more text to an existing project
     * @return {@link JComboBox} containing {@link JPanel}
     */
    private JPanel createFontTypeComboBox(final boolean newProject) {
        final JPanel fontTypePanel = new JPanel();
        fontTypePanel.setBorder(BorderFactory.createTitledBorder(HmxMessage.SETTING_FONT_TYPE.get()));
        this.fontTypeBox.setEditable(false);
        this.fontTypeBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                TextInputPanel.this.setFontTypeAndStyle();
            }
        });

        this.fontFamilyNames = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        Collections.sort(this.fontFamilyNames);
        for (final String singleFontName : this.fontFamilyNames) {
            this.fontTypeBox.addItem(singleFontName);
        }
        fontTypePanel.add(this.fontTypeBox);
        return fontTypePanel;
    }

    /**
     * set the font type and size selection regarding the currently chosen origin text language.
     */
    void setFontSelection() {
        final Object selectedEntry = this.languageBox.getSelectedItem();
        if (selectedEntry == null) {
            return;
        }
        final LanguageModel selectedModel = this.languageModels.get(selectedEntry);
        if (selectedModel == null) {
            return;
        }
        final Font defaultFont;
        if (this.parentView.getModel() != null) {
            defaultFont = this.parentView.getModel().getFont();
        } else {
            defaultFont = null;
        }
        String fontName;
        if (defaultFont != null) {
            fontName = defaultFont.getFamily();
            // } else TODO if (selectedModel != null && user setting for the currently selected language available) {
        } else {
            fontName = null;
        }
        int fontIndex = this.fontFamilyNames.indexOf(fontName);
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
        final int fontSize;
        if (defaultFont == null) {
            fontSize = 14;
        } else {
            fontSize = defaultFont.getSize();
        }
        this.fontSizeSlider.setValue(fontSize);
    }

    /**
     * creates a {@link JPanel} containing the {@link JSlider} for adjusting the {@link Font} size of the origin text.
     *
     * @param newProject
     *            if this is the initial text input, else it is just adding more text to an existing project
     * @return {@link JSlider} containing {@link JPanel}
     */
    private JPanel createFontSizeSlider(final boolean newProject) {
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
     * sets the {@link Font} of the origin text regarding to the chosen type in the setting sidebar.
     */
    void setFontTypeAndStyle() {
        this.originTextPane.setFont(new Font(this.fontTypeBox.getSelectedItem().toString(), Font.PLAIN, this.fontSizeSlider.getValue()));
    }

    /**
     * initializes a new Pericope by transferring the pasted origin text and tells its {@link ProjectControl} to start the analysis.
     */
    void startAnalysis() {
        if (this.containsText()) {
            final Object selectedEntry = this.languageBox.getSelectedItem();
            if (selectedEntry != null) {
                final LanguageModel selectedModel = this.languageModels.get(selectedEntry);
                this.parentView.startAnalysis(this.getPropositionTexts(), selectedModel, new Font(this.fontTypeBox.getSelectedItem().toString(),
                        Font.PLAIN, this.fontSizeSlider.getValue()));
            }
        }
        // if the originTextArea is empty, do nothing
    }

    void addOriginText(final boolean inFront) {
        if (this.containsText()) {
            this.parentView.addNewPropositions(this.getPropositionTexts(), inFront);
        }
        // if the originTextArea is empty, do nothing
    }

    /**
     * @return if the origin text pane contains valid input text
     */
    public boolean containsText() {
        return !this.originTextPane.getText().trim().isEmpty();
    }

    /**
     * Retrieve text from origin text pane, handle multiple whitespaces and split at line separators.
     * <ul>
     * <li>>=4 whitespaces are replaced by a tabstop</li>
     * <li><4 whitespaces are reduced to a single one</li>
     * </ul>
     *
     * @return proposition texts containing tabstop to separate clause items
     */
    String getPropositionTexts() {
        // get the current input text
        String rawText = this.originTextPane.getText();
        // replace each substring containing at least four adjacent whitespaces
        rawText = rawText.replaceAll("    * ", "\t");
        // reduce each remaining substring with multiple whitespaces to one
        rawText = rawText.replaceAll("  * ", " ");
        // split at line separators to get single strings for each proposition
        return rawText;
    }
}
