/*
   Copyright (C) 2017 HermeneutiX.org

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

package org.hmx.scitos.hmx.view.swing.elements;

import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.hmx.scitos.domain.util.ComparisonUtil;
import org.hmx.scitos.hmx.core.option.HmxGeneralOption;
import org.hmx.scitos.hmx.domain.model.ClauseItem;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.domain.model.SyntacticalFunction;
import org.hmx.scitos.hmx.view.ContextMenuFactory;
import org.hmx.scitos.hmx.view.IPericopeView;
import org.hmx.scitos.hmx.view.swing.components.IAnalysisViewSettings;
import org.hmx.scitos.view.ContextMenuBuilder;
import org.hmx.scitos.view.swing.ContextMenuPopupBuilder;
import org.hmx.scitos.view.swing.components.ScaledLabel;
import org.hmx.scitos.view.swing.components.ScaledTextField;
import org.hmx.scitos.view.swing.util.VTextIcon;
import org.hmx.scitos.view.swing.util.Validation;

/**
 * AView representation of a {@link Proposition} with a check box, an input for a short label text, an indentation area, the ability to display arrows
 * in front and behind the origin text area as well as inputs for individual translation texts.
 */
public final class ViewProposition extends AbstractCommentable<Proposition> implements IConnectable<Proposition> {

    /** raised bevel border, when not selected. */
    private static final Border DEFAULT_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(2, 2, 6, 2));
    /** lowered bevel border, when selected. */
    private static final Border COMMENT_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(2, 2, 6, 2));

    /** Containing view, providing access to higher functions. */
    final IPericopeView viewReference;
    /**
     * The represented model {@link Proposition}.
     */
    private final Proposition represented;
    /**
     * Index of the represented {@link Proposition} in the pericope.
     */
    private final int propositionIndex;
    /** raised bevel border with color, when not selected and with comment set. */
    private final Border defaultBorderCommented;
    /** The actual container of the components this element is comprised of. This abstraction layer allows it being resized as needed. */
    private final JPanel contentPane = new JPanel(new GridBagLayout());
    /** The input field for the (up to five characters) identifier. */
    private final ScaledTextField labelField;
    /**
     * The placeholder realizing the indentation of the {@link Proposition} contents.
     */
    private final JPanel indentationArea;
    /** label displaying the syntactical indentation function. */
    private final JLabel functionLabel;
    /**
     * The placeholder for upward pointing arrows referring to a {@code partBeforeArrow} of the represented {@link Proposition} (part).
     */
    private final ArrowStackLabel leftArrows;
    /** The container for the origin text. */
    private final JPanel itemArea = new JPanel();
    /**
     * The single text field displaying the concatenated origin text of all {@link ClauseItem}s of the represented {@link Proposition}.
     */
    private final JTextField originText;
    /** View representation of the contained clause items. */
    private final List<ViewClauseItem> items;
    /**
     * The placeholder for downward pointing arrows referring to a {@code partAfterArrow} of the represented {@link Proposition} (part).
     */
    private final ArrowStackLabel rightArrows;
    /** The input field for the syntactical translation text. */
    private final ScaledTextField synTranslationField;
    /** The input field for the semantical translation text. */
    private final ScaledTextField semTranslationField;
    /**
     * The view element allowing this {@link Proposition} to be selected for any more complex operations involving multiple model elements.
     */
    private final JCheckBox checkBox = new JCheckBox();

    /**
     * Create a {@link SynProposition} representing the given {@link Proposition} on the specified indentation {@code level}.
     *
     * @param viewReference the containing view, providing access to higher functions
     * @param represented represented {@link Proposition} to set
     * @param propositionIndex index of the represented proposition in the origin text (including parts after arrows)
     * @param level number of indentations to set
     * @return created {@link SynProposition}
     */
    public static ViewProposition createSynPropositionByLevel(final IPericopeView viewReference, final Proposition represented,
            final int propositionIndex, final int level) {
        final ViewProposition created = new ViewProposition(viewReference, represented, propositionIndex);
        if (level > 0 && created.indentationArea != null) {
            created.indentationArea.setPreferredSize(ViewProposition.createIndentation(level));
        }
        return created;
    }

    /**
     * Create a {@link SynProposition} representing the given {@link Proposition}, which is the {@code partAfterArrow} for the other given
     * {@link SynProposition}, determining the created elements indentation.
     *
     * @param viewReference the containing view, providing access to higher functions
     * @param represented represented {@link Proposition} part to set
     * @param propositionIndex index of the represented proposition in the origin text (including parts after arrows)
     * @param partBeforeArrow view representation of the {@code partBeforeArrow}
     * @return created {@link SynProposition}
     */
    public static ViewProposition createSynPropositionByPartBeforeArrow(final IPericopeView viewReference, final Proposition represented,
            final int propositionIndex, final ViewProposition partBeforeArrow) {
        final ViewProposition created = new ViewProposition(viewReference, represented, propositionIndex);
        if (created.indentationArea != null) {
            created.indentationArea.setPreferredSize(ViewProposition.createIndentationAfterArrow(partBeforeArrow));
        }
        return created;
    }

    /**
     * creates a {@link Dimension} for the indentation by setting its width regarding to the specified level.
     *
     * @param level number of indentations to be contained
     * @return {@link Dimension} representing the indentation
     */
    private static Dimension createIndentation(final int level) {
        return new Dimension(HmxGeneralOption.INDENTATION_WIDTH.getValueAsInteger() * level, 1);
    }

    /**
     * Create a {@link Dimension} for the indentation by settings its width regarding to the width of the view representation of the
     * {@code partBeforeArrow}.
     *
     * @param partBeforeArrow view representation of the {@code partBeforeArrow}
     * @return {@link Dimension} representing the indentation
     */
    private static Dimension createIndentationAfterArrow(final ViewProposition partBeforeArrow) {
        final int partBeforeArrowIndentation = partBeforeArrow.indentationArea == null ? 0 : partBeforeArrow.indentationArea.getPreferredSize().width;
        return new Dimension(partBeforeArrowIndentation + partBeforeArrow.leftArrows.getPreferredSize().width
                + partBeforeArrow.itemArea.getPreferredSize().width, 1);
    }

    /**
     * Constructor: creating an {@link ViewProposition} by setting all values derived from the represented {@link Proposition}.
     *
     * @param viewReference the analysis view that will contain this component
     * @param represented represented {@link Proposition} to set
     * @param propositionIndex index of the represented proposition in the origin text (including parts after arrows)
     */
    public ViewProposition(final IPericopeView viewReference, final Proposition represented, final int propositionIndex) {
        super(new GridBagLayout());
        this.viewReference = viewReference;
        this.represented = represented;
        this.propositionIndex = propositionIndex;
        final boolean leftAligned = viewReference.getModelHandler().getModel().isLeftToRightOriented();
        this.defaultBorderCommented = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(2, leftAligned ? 1 : 0, 2, leftAligned ? 0 : 1,
                                HmxGeneralOption.COMMENTED_BORDER_COLOR.getValueAsColor()),
                        BorderFactory.createEmptyBorder(0, leftAligned ? 1 : 2, 4, leftAligned ? 2 : 1)));
        final ComponentOrientation orientation = leftAligned ? ComponentOrientation.LEFT_TO_RIGHT : ComponentOrientation.RIGHT_TO_LEFT;
        this.setComponentOrientation(orientation);
        this.contentPane.setComponentOrientation(orientation);
        this.itemArea.setComponentOrientation(orientation);
        this.leftArrows = new ArrowStackLabel(true, 0);
        this.rightArrows = new ArrowStackLabel(false, 0);

        final IAnalysisViewSettings viewSettings = viewReference.getViewSettings();
        final GridBagConstraints checkBoxConstraints = new GridBagConstraints();
        checkBoxConstraints.gridx = 0;
        checkBoxConstraints.gridy = 1;
        if (this.represented.getPartBeforeArrow() == null && (viewSettings.isShowingPropositionIndentations()
                || viewSettings.isShowingRelations() && this.represented.getSuperOrdinatedRelation() == null)) {
            this.checkBox.setName("Check Box");
            this.contentPane.add(this.checkBox, checkBoxConstraints);
        } else {
            final JPanel checkBoxDummy = new JPanel(null);
            checkBoxDummy.setPreferredSize(this.checkBox.getPreferredSize());
            this.contentPane.add(checkBoxDummy, checkBoxConstraints);
        }

        this.labelField = this.initLabel(viewSettings);
        this.indentationArea = this.initIndentationArea(viewSettings);
        this.functionLabel = this.initFunctionLabel();
        if (viewSettings.isShowingClauseItems()) {
            this.originText = null;
            this.items = new LinkedList<ViewClauseItem>();
        } else {
            this.originText = new ScaledTextField();
            this.items = null;
        }
        this.initOriginTextArea();
        this.synTranslationField = this.initSynTranslationField(viewSettings);
        this.semTranslationField = this.initSemTranslationField(viewSettings);
        this.add(this.contentPane);
        // create expanding panel on the right side of the proposition
        final GridBagConstraints rightSpacingConstraints = new GridBagConstraints();
        rightSpacingConstraints.fill = GridBagConstraints.HORIZONTAL;
        rightSpacingConstraints.weightx = 1;
        rightSpacingConstraints.gridx = 1;
        rightSpacingConstraints.gridy = 0;
        final JPanel rightSpacing = new JPanel(null);
        rightSpacing.setPreferredSize(new Dimension(1, 1));
        this.add(rightSpacing, rightSpacingConstraints);
        this.setDefaultBorder();
        this.refresh();
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(final MouseEvent event) {
                viewReference.handleSelectedCommentable(ViewProposition.this);
                this.mouseReleased(event);
            }

            @Override
            public void mouseReleased(final MouseEvent event) {
                if (event.isPopupTrigger()) {
                    final ContextMenuBuilder contextMenu;
                    if (ViewProposition.this.getRepresented().getPartBeforeArrow() == null) {
                        contextMenu = ContextMenuFactory.createPropositionPopup(viewReference, represented);
                    } else {
                        contextMenu = ContextMenuFactory.createPropositionAfterArrowPopup(viewReference, represented);
                    }
                    if (!contextMenu.isEmpty()) {
                        ContextMenuPopupBuilder.buildSwingPopupMenu(contextMenu).show(event.getComponent(), event.getX(), event.getY());
                    }
                }
            }
        });
    }

    /**
     * Initialize the label {@link ScaledTextField input}.
     *
     * @param viewSettings settings to apply
     * @return created label input (can be {@code null})
     */
    private ScaledTextField initLabel(final IAnalysisViewSettings viewSettings) {
        final ScaledTextField label;
        if (viewSettings.isShowingPropositionLabels()) {
            label = new ScaledTextField();
            label.setName("Label Input");
            label.setColumns(Proposition.MAX_LABEL_LENGTH - 1);
            label.setDocument(new Validation(Proposition.MAX_LABEL_LENGTH));
            label.addFocusListener(new FocusAdapter() {

                @Override
                public void focusLost(final FocusEvent event) {
                    ViewProposition.this.submitLabelChanges();
                }
            });
            final GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 1;
            constraints.gridy = 1;
            this.contentPane.add(label, constraints);
        } else {
            label = null;
        }
        return label;
    }

    /**
     * Initialize an expanding {@link JPanel} for the proposition's indentation.
     *
     * @param viewSettings settings to apply
     * @return created panel (can be {@code null})
     */
    private JPanel initIndentationArea(final IAnalysisViewSettings viewSettings) {
        final JPanel indentationAreaPanel;
        if (viewSettings.isShowingPropositionIndentations()) {
            indentationAreaPanel = new JPanel(new GridBagLayout());
            final GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.VERTICAL;
            constraints.weighty = 1;
            constraints.gridheight = 4;
            constraints.gridx = 2;
            constraints.gridy = 0;
            this.contentPane.add(indentationAreaPanel, constraints);
        } else {
            indentationAreaPanel = null;
        }
        return indentationAreaPanel;
    }

    /**
     * Add a label representing the syntactical indentation function of the represented proposition, if any.
     *
     * @return created label (can be {@code null})
     */
    private ScaledLabel initFunctionLabel() {
        final ScaledLabel label;
        if (this.indentationArea == null || !(this.getRepresented().getParent() instanceof Proposition)) {
            label = null;
        } else {
            final GridBagConstraints spacing = new GridBagConstraints();
            spacing.weightx = 1;
            this.indentationArea.add(new JPanel(null), spacing);

            label = new ScaledLabel(" ");
            final SyntacticalFunction function = this.getRepresented().getFunction();
            label.setIcon(new VTextIcon(label, function == null ? " " : function.getCode(), VTextIcon.Rotate.NONE));
            final int border = (ViewProposition.createIndentation(1).width - label.getPreferredSize().width) / 2;
            final GridBagConstraints verticalSpan = new GridBagConstraints();
            verticalSpan.anchor = GridBagConstraints.BASELINE_TRAILING;
            verticalSpan.weighty = 1;
            verticalSpan.insets = new Insets(0, border, 0, border);
            this.indentationArea.add(label, verticalSpan);
        }
        return label;
    }

    /**
     * Initialize the top right part of the {@link Proposition} containing a panel for the origin text as well as arrows on the left and on the right
     * side of it.
     */
    private void initOriginTextArea() {
        if (this.represented.getPartBeforeArrow() != null) {
            // leftArrows
            final GridBagConstraints arrowConstraints = new GridBagConstraints();
            arrowConstraints.anchor = GridBagConstraints.CENTER;
            arrowConstraints.gridx = 3;
            arrowConstraints.gridy = 0;
            arrowConstraints.gridheight = 2;
            this.contentPane.add(this.leftArrows, arrowConstraints);
        }
        // itemArea
        if (this.originText != null) {
            this.originText.setFont(this.viewReference.getModelHandler().getModel().getFont());
            this.originText.setEditable(false);
            this.originText.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                    BorderFactory.createEmptyBorder(2, 5, 2, 5)));
            this.itemArea.add(this.originText);
        }
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 4;
        constraints.gridy = 0;
        constraints.gridheight = 2;
        this.contentPane.add(this.itemArea, constraints);
        if (this.represented.getPartAfterArrow() != null) {
            // rightArrows
            final GridBagConstraints arrowConstraints = new GridBagConstraints();
            arrowConstraints.anchor = GridBagConstraints.CENTER;
            arrowConstraints.gridx = 5;
            arrowConstraints.gridy = 0;
            arrowConstraints.gridheight = 2;
            this.contentPane.add(this.rightArrows, arrowConstraints);
        }
        // just to make sure rightArrows are behind the itemArea in case of an expanded translation field
        final GridBagConstraints rightSpacingConstraints = new GridBagConstraints();
        rightSpacingConstraints.fill = GridBagConstraints.HORIZONTAL;
        rightSpacingConstraints.weightx = 1;
        rightSpacingConstraints.gridx = 6;
        rightSpacingConstraints.gridy = 0;
        rightSpacingConstraints.gridheight = 2;
        final JPanel rightSpacing = new JPanel(null);
        rightSpacing.setPreferredSize(new Dimension(1, 1));
        this.contentPane.add(rightSpacing, rightSpacingConstraints);
    }

    /**
     * Initialize the syntactic translation {@link ScaledTextField input} on the bottom right of the {@link Proposition}.
     *
     * @param viewSettings settings to apply
     * @return created translation input (can be {@code null})
     */
    private ScaledTextField initSynTranslationField(final IAnalysisViewSettings viewSettings) {
        final ScaledTextField translationField;
        if (viewSettings.isShowingSyntacticTranslations()) {
            translationField = new ScaledTextField();
            translationField.setName("Syn Translation Input");
            translationField.setDocument(new Validation(Proposition.MAX_TRANSLATION_LENGTH));
            translationField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(final FocusEvent event) {
                    ViewProposition.this.submitSynTranslationChanges();
                }
            });
            final GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.gridwidth = 4;
            constraints.gridx = 3;
            constraints.gridy = 2;
            this.contentPane.add(translationField, constraints);
        } else {
            translationField = null;
        }
        return translationField;
    }

    /**
     * Initialize the semantic translation {@link ScaledTextField input} on the bottom right of the {@link Proposition}.
     *
     * @param viewSettings settings to apply
     * @return created translation input (can be {@code null})
     */
    private ScaledTextField initSemTranslationField(final IAnalysisViewSettings viewSettings) {
        final ScaledTextField translationField;
        if (viewSettings.isShowingSemanticTranslations()) {
            translationField = new ScaledTextField();
            translationField.setName("Sem Translation Input");
            translationField.setDocument(new Validation(Proposition.MAX_TRANSLATION_LENGTH));
            translationField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(final FocusEvent event) {
                    ViewProposition.this.submitSemTranslationChanges();
                }
            });
            final GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.gridwidth = 4;
            constraints.gridx = 3;
            constraints.gridy = 3;
            this.contentPane.add(translationField, constraints);
        } else {
            translationField = null;
        }
        return translationField;
    }

    /**
     * Ensure that any pending changes (e.g. in the label or translation fields) are being submitted to the model handler.
     */
    public void submitChangesToModel() {
        if (this.labelField != null) {
            this.submitLabelChanges();
        }
        if (this.synTranslationField != null) {
            this.submitSynTranslationChanges();
        }
        if (this.semTranslationField != null) {
            this.submitSemTranslationChanges();
        }
    }

    /**
     * Ensure that any changes in the label field are submitted to the model.
     */
    final void submitLabelChanges() {
        // only transfer if necessary
        final String labelText = this.labelField.getText();
        if (!ComparisonUtil.isNullOrEmptyAwareEqual(this.represented.getLabel(), labelText)) {
            this.viewReference.getModelHandler().setLabelText(this.represented, labelText);
        }
    }

    /**
     * Ensure that any changes in the syntactic translation field are submitted to the model.
     */
    final void submitSynTranslationChanges() {
        final String translationText = this.synTranslationField.getText();
        // only transfer if necessary
        if (!ComparisonUtil.isNullOrEmptyAwareEqual(translationText, this.getRepresented().getSynTranslation())) {
            this.viewReference.getModelHandler().setSynTranslation(this.getRepresented(), translationText);
        }
    }

    /**
     * Ensure that any changes in the semantic translation field are submitted to the model.
     */
    final void submitSemTranslationChanges() {
        final String translationText = this.semTranslationField.getText();
        // only transfer if necessary
        if (!ComparisonUtil.isNullOrEmptyAwareEqual(translationText, this.getRepresented().getSemTranslation())) {
            this.viewReference.getModelHandler().setSemTranslation(this.getRepresented(), translationText);
        }
    }

    /**
     * Setter for the number of displayed upward arrows on the left of the item area.
     *
     * @param count number of displayed arrows to set
     */
    public void setLeftArrowCount(final int count) {
        this.leftArrows.setArrowCount(count);
    }

    /**
     * Setter for the number of displayed downward arrows on the right of the item area.
     *
     * @param count number of displayed arrows to set
     */
    public void setRightArrowCount(final int count) {
        this.rightArrows.setArrowCount(count);
    }

    /**
     * Getter for the represented {@link Proposition}.
     *
     * @return represented model element
     */
    @Override
    public Proposition getRepresented() {
        return this.represented;
    }

    /**
     * Fully update the contents of the displayed proposition without rebuilding it.
     *
     * @return whether the update was successful, otherwise a proper rebuild is required
     */
    public boolean refresh() {
        this.refreshLabelText();
        this.refreshFunction();
        this.refreshTranslation();
        this.refreshComment();
        if (this.originText == null) {
            this.itemArea.removeAll();
            this.items.clear();
            for (final ClauseItem item : this.represented.getItems()) {
                final ViewClauseItem singleItem = new ViewClauseItem(this.viewReference, item);
                this.items.add(singleItem);
                this.itemArea.add(singleItem);
            }
        } else {
            final StringBuffer text = new StringBuffer(" ");
            for (final ClauseItem singleItem : this.getRepresented()) {
                text.append(singleItem.getOriginText()).append(' ');
            }
            this.originText.setText(text.toString());
            this.originText.setSize(this.originText.getPreferredSize());
        }
        return true;
    }

    /**
     * Update the displayed identifier text to match the value in the represented {@link Proposition}.
     */
    void refreshLabelText() {
        if (this.labelField != null) {
            this.labelField.setText(this.represented.getLabel());
        }
    }

    /**
     * Update the indentation function (in relation to the parent {@link Proposition}) to match the represented {@link Proposition}'s function value.
     */
    void refreshFunction() {
        if (this.functionLabel != null && this.getRepresented().getParent() instanceof Proposition) {
            final SyntacticalFunction function = this.getRepresented().getFunction();
            ((VTextIcon) this.functionLabel.getIcon()).setLabel(function == null ? " " : function.getCode());
        }
    }

    /**
     * sets the semantical translation text regarding the stored translation in the represented {@link Proposition} and fits the {@link Proposition}
     * to the possibly expanded translation field size.
     */
    void refreshTranslation() {
        if (this.synTranslationField == null && this.semTranslationField == null) {
            // nothing to refresh here
            return;
        }
        final Dimension preferredSynFieldSize;
        if (this.synTranslationField == null) {
            preferredSynFieldSize = new Dimension(0, 0);
        } else {
            this.synTranslationField.setText(this.getRepresented().getSynTranslation());
            preferredSynFieldSize = this.synTranslationField.getPreferredSize();
        }
        final Dimension preferredSemFieldSize;
        if (this.semTranslationField == null) {
            preferredSemFieldSize = new Dimension(0, 0);
        } else {
            this.semTranslationField.setText(this.getRepresented().getSemTranslation());
            preferredSemFieldSize = this.semTranslationField.getPreferredSize();
        }
        // if the translation text wants more space, it gets more
        final int translationFieldPreference = Math.max(preferredSynFieldSize.width, preferredSemFieldSize.width);
        final int translationFieldWidth = Math.max(translationFieldPreference, this.itemArea.getPreferredSize().width);
        if (this.synTranslationField != null) {
            this.synTranslationField.setSize(translationFieldWidth, preferredSynFieldSize.height);
        }
        if (this.semTranslationField != null) {
            this.semTranslationField.setSize(translationFieldWidth, preferredSemFieldSize.height);
        }
        // enlarge the containing proposition
        this.contentPane.setSize(this.contentPane.getPreferredSize());
        // make sure it is still displayed
        this.itemArea.validate();
    }

    /**
     * resets the tool tip info containing the comment text regarding its value in the represented {@link Proposition}.
     */
    void refreshComment() {
        final String comment = this.getRepresented().getComment();
        if (comment == null || comment.isEmpty()) {
            this.setToolTipText(null);
        } else {
            this.setToolTipText(comment);
        }
    }

    /**
     * Update the view representation of the given {@link ClauseItem}.
     *
     * @param target model element to update representation of
     */
    public void refreshClauseItem(final ClauseItem target) {
        if (this.items != null) {
            for (final ViewClauseItem singleItem : this.items) {
                if (singleItem.getRepresented() == target) {
                    singleItem.refresh();
                    break;
                }
            }
        }
    }

    @Override
    public boolean isChecked() {
        return this.checkBox.isSelected();
    }

    @Override
    public void setNotChecked() {
        this.checkBox.setSelected(false);
    }

    @Override
    public int getDepth() {
        return 0;
    }

    @Override
    public double getConnectY() {
        return 0.5 + this.propositionIndex;
    }

    @Override
    public void setDefaultBorder() {
        final boolean containsComment = this.represented.getComment() != null && !this.represented.getComment().trim().isEmpty();
        this.setContentBorder(containsComment ? this.defaultBorderCommented : ViewProposition.DEFAULT_BORDER);
    }

    @Override
    public void setCommentBorder() {
        this.setContentBorder(ViewProposition.COMMENT_BORDER);
    }

    /**
     * Apply the given border any ensure that any contained {@link ViewClauseItem}s are still properly shown.
     *
     * @param border border to apply
     */
    private void setContentBorder(final Border border) {
        this.contentPane.setBorder(border);
        // without these visibility changes the item area may disappear
        this.itemArea.setVisible(false);
        // make sure the item area is still visible
        this.itemArea.setVisible(true);
        if (this.items != null) {
            this.itemArea.revalidate();
            for (final ViewClauseItem singleItem : this.items) {
                singleItem.revalidate();
            }
        }
    }

    /**
     * Add the specified {@link MouseListener} to all components included except the single {@link SynItem}s in the syntactical analysis view.
     *
     * @param listener listener to add
     */
    @Override
    public synchronized void addMouseListener(final MouseListener listener) {
        super.addMouseListener(listener);
        if (this.labelField != null) {
            this.labelField.addMouseListener(listener);
        }
        if (this.indentationArea != null) {
            this.indentationArea.addMouseListener(listener);
        }
        this.leftArrows.addMouseListener(listener);
        this.itemArea.addMouseListener(listener);
        if (this.originText != null) {
            this.originText.addMouseListener(listener);
        }
        this.rightArrows.addMouseListener(listener);
        if (this.synTranslationField != null) {
            this.synTranslationField.addMouseListener(listener);
        }
        if (this.semTranslationField != null) {
            this.semTranslationField.addMouseListener(listener);
        }
    }

    /**
     * Ses the specified tool tip text to all of its components except the single {@link SynItem}s in the syntactical analysis view.
     *
     * @param toolTip tool tip text to set
     */
    @Override
    public synchronized void setToolTipText(final String toolTip) {
        super.setToolTipText(toolTip);
        if (this.labelField != null) {
            this.labelField.setToolTipText(toolTip);
        }
        if (this.indentationArea != null) {
            this.indentationArea.setToolTipText(toolTip);
        }
        this.leftArrows.setToolTipText(toolTip);
        this.itemArea.setToolTipText(toolTip);
        if (this.originText != null) {
            this.originText.setToolTipText(toolTip);
        }
        this.rightArrows.setToolTipText(toolTip);
        if (this.synTranslationField != null) {
            this.synTranslationField.setToolTipText(toolTip);
        }
        if (this.semTranslationField != null) {
            this.semTranslationField.setToolTipText(toolTip);
        }
    }
}
