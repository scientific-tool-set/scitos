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

package org.hmx.scitos.hmx.view.swing.elements;

import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.hmx.scitos.domain.util.ComparisonUtil;
import org.hmx.scitos.hmx.core.HmxModelHandler;
import org.hmx.scitos.hmx.core.option.HmxGeneralOption;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.view.swing.components.ScaledLabel;
import org.hmx.scitos.view.swing.components.ScaledTextField;
import org.hmx.scitos.view.swing.util.Validation;

/**
 * Abstract mask of the view representation of a {@link Proposition} not specifying how to display the contained origin text<br>
 * offering a uniformed representation of the {@link Proposition}s in the syntactical and semantical analysis views with a {@link JCheckBox}, a
 * {@link JTextField} for a short label text, an indentation area, the ability to display arrows in front and behind the origin text area as well as a
 * {@link JTextField} for an individual translation text.
 */
abstract class AbstractProposition extends AbstractCommentable<Proposition> implements ICheckable {

    /** raised bevel border, when not selected. */
    private static final Border DEFAULT_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(2, 2, 6, 2));
    /** lowered bevel border, when selected. */
    private static final Border COMMENT_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(2, 2, 6, 2));

    /** The responsible model handler implementation realizing any model changes. */
    private final HmxModelHandler modelHandler;
    /**
     * The represented model {@link Proposition}.
     */
    private final Proposition represented;
    /** raised bevel border with color, when not selected and with comment set. */
    private final Border defaultBorderCommented;
    /** The actual container of the components this element is comprised of. This abstraction layer allows it being resized as needed. */
    private final JPanel contentPane = new JPanel(new GridBagLayout());
    /** The input field for the (up to five characters) identifier. */
    private final JTextField labelField;
    /**
     * The placeholder realizing the indentation of the {@link Proposition} contents.
     */
    private final JPanel indentationArea = new JPanel(new GridBagLayout());
    /**
     * The placeholder for upward pointing arrows referring to a {@code partBeforeArrow} of the represented {@link Proposition} (part).
     */
    private final ArrowStack leftArrows;
    /** The container for the origin text, which is displayed differently in the syntactical and semantical analysis. */
    private final JPanel itemArea = new JPanel();
    /**
     * The placeholder for downward pointing arrows referring to a {@code partAfterArrow} of the represented {@link Proposition} (part).
     */
    private final ArrowStack rightArrows;
    /** The input field for the translation text related to the respective analysis (either syntactical or semantical). */
    private final JTextField translationField;
    /**
     * The view element allowing this {@link Proposition} to be selected for any more complex operations involving multiple model elements.
     */
    private final JCheckBox checkBox = new JCheckBox();
    /**
     * The view element replacing the {@link #checkBox} if the represented {@link Proposition} is not eligible for being part of any more complex
     * operations.
     */
    private final JPanel checkBoxDummy = new JPanel();

    /**
     * Constructor: creating an {@link AbstractProposition} by setting all values derived from the represented {@link Proposition}.
     *
     * @param modelHandler
     *            the responsible model handler implementation realizing any model changes
     * @param represented
     *            represented {@link Proposition} to set
     * @param showLabel
     *            whether the label field should be shown
     * @param showTranslation
     *            whether the translation field should be shown
     */
    protected AbstractProposition(final HmxModelHandler modelHandler, final Proposition represented, final boolean showLabel,
            final boolean showTranslation) {
        super(new GridBagLayout());
        this.modelHandler = modelHandler;
        this.represented = represented;
        final boolean leftAligned = modelHandler.getModel().isLeftToRightOriented();
        this.defaultBorderCommented = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(2, leftAligned ? 1 : 0, 2, leftAligned ? 0 : 1,
                                HmxGeneralOption.COMMENTED_BORDER_COLOR.getValueAsColor()),
                        BorderFactory.createEmptyBorder(0, leftAligned ? 1 : 2, 4, leftAligned ? 2 : 1)));
        final ComponentOrientation orientation = leftAligned ? ComponentOrientation.LEFT_TO_RIGHT : ComponentOrientation.RIGHT_TO_LEFT;
        this.setComponentOrientation(orientation);
        this.contentPane.setComponentOrientation(orientation);
        this.itemArea.setComponentOrientation(orientation);
        this.leftArrows = new ArrowStack(true, 0);
        this.rightArrows = new ArrowStack(false, 0);
        if (showLabel) {
            this.labelField = new ScaledTextField();
        } else {
            this.labelField = null;
        }
        this.initCheckboxAndLabel(showLabel);
        this.initOriginTextArea();
        if (showTranslation) {
            this.translationField = new ScaledTextField();
            this.initTranslationArea();
        } else {
            this.translationField = null;
        }
        this.add(this.contentPane);
        this.setDefaultBorder();
    }

    /**
     * Initialize the left part of the {@link AbstractProposition} containing the {@link JCheckBox}, the label {@link JTextField} and an expanding
     * {@link JPanel} for the indentations in the syntactical analysis view.
     *
     * @param showLabel
     *            whether the label field should be shown
     */
    private void initCheckboxAndLabel(final boolean showLabel) {
        // checkBox
        this.checkBoxDummy.setPreferredSize(this.checkBox.getPreferredSize());
        this.setCheckBoxVisible(this.represented.getPartBeforeArrow() == null);
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        this.checkBox.setName("Check Box");
        this.contentPane.add(this.checkBox, constraints);
        this.contentPane.add(this.checkBoxDummy, constraints);
        // labelField
        if (showLabel) {
            this.labelField.setName("Label Input");
            this.labelField.setColumns(Proposition.MAX_LABEL_LENGTH - 1);
            this.labelField.setDocument(new Validation(Proposition.MAX_LABEL_LENGTH));
            this.labelField.addFocusListener(new FocusAdapter() {

                @Override
                public void focusLost(final FocusEvent event) {
                    AbstractProposition.this.submitLabelChanges();
                }
            });
            this.refreshLabelText();
            constraints.gridx = 1;
            this.contentPane.add(this.labelField, constraints);
        }
        // indentationArea
        constraints.fill = GridBagConstraints.VERTICAL;
        constraints.weighty = 1;
        constraints.gridheight = 3;
        constraints.gridx = 2;
        constraints.gridy = 0;
        this.contentPane.add(this.indentationArea, constraints);
    }

    /**
     * Ensure that any pending changes (e.g. in the label or translation field) are being submitted to the model handler.
     */
    public void submitChangesToModel() {
        if (this.labelField != null) {
            this.submitLabelChanges();
        }
        if (this.translationField != null) {
            this.submitTranslationChanges();
        }
    }

    /**
     * Ensure that any changes in the label field are submitted to the model.
     */
    final void submitLabelChanges() {
        // only transfer if necessary
        final String labelText = this.labelField.getText();
        if (!ComparisonUtil.isNullOrEmptyAwareEqual(this.represented.getLabel(), labelText)) {
            this.getModelHandler().setLabelText(this.represented, labelText);
        }
    }

    /**
     * Ensure that any changes in the translation field are submitted to the model.
     */
    protected abstract void submitTranslationChanges();

    /**
     * Getter for the associated {@link HmxModelHandler} implementation.
     *
     * @return the responsible model handler implementation realizing any model changes
     */
    protected final HmxModelHandler getModelHandler() {
        return this.modelHandler;
    }

    /**
     * Initialize the top right part of the {@link Proposition} containing a panel for the origin text as well as arrows on the left and on the right
     * side of it.
     */
    private void initOriginTextArea() {
        // leftArrows
        final GridBagConstraints arrowConstraints = new GridBagConstraints();
        arrowConstraints.anchor = GridBagConstraints.CENTER;
        arrowConstraints.gridx = 3;
        arrowConstraints.gridy = 0;
        arrowConstraints.gridheight = 2;
        this.contentPane.add(this.leftArrows, arrowConstraints);
        // itemArea
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 4;
        constraints.gridy = 0;
        constraints.gridheight = 2;
        this.contentPane.add(this.itemArea, constraints);
        // rightArrows
        arrowConstraints.gridx = 5;
        this.contentPane.add(this.rightArrows, arrowConstraints);
        // just to make sure rightArrows are behind the itemArea in case of an
        // expanded translation field
        final GridBagConstraints rightSpacing = new GridBagConstraints();
        rightSpacing.fill = GridBagConstraints.HORIZONTAL;
        rightSpacing.weightx = 1;
        rightSpacing.gridx = 6;
        rightSpacing.gridy = 0;
        rightSpacing.gridheight = 2;
        this.contentPane.add(new JPanel(), rightSpacing);
    }

    /**
     * Initialize the bottom right part of the {@link Proposition} containing the text field for the translation.
     */
    private void initTranslationArea() {
        this.translationField.setName("Translation Input");
        this.translationField.setDocument(new Validation(Proposition.MAX_TRANSLATION_LENGTH));
        this.refreshTranslation();
        this.translationField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(final FocusEvent event) {
                AbstractProposition.this.submitTranslationChanges();
            }
        });
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weighty = 1;
        constraints.gridwidth = 4;
        constraints.gridx = 3;
        constraints.gridy = 2;
        this.contentPane.add(this.translationField, constraints);
    }

    /**
     * Getter for the identifier's input field.
     *
     * @return label field (can be {@code null})
     */
    protected final JTextField getLabelField() {
        return this.labelField;
    }

    /**
     * Getter for the placeholder realizing the indentation of the {@link Proposition} contents.
     *
     * @return indentation area
     */
    protected final JPanel getIndentationArea() {
        return this.indentationArea;
    }

    /**
     * Getter for the placeholder for upward pointing arrows referring to a {@code partBeforeArrow} of the represented {@link Proposition} (part).
     *
     * @return left arrow stack
     */
    protected final ArrowStack getLeftArrows() {
        return this.leftArrows;
    }

    /**
     * Setter for the number of displayed upward arrows on the left of the item area.
     *
     * @param count
     *            number of displayed arrows to set
     */
    public final void setLeftArrowCount(final int count) {
        this.leftArrows.setArrowCount(count);
    }

    /**
     * Getter for the origin text container, which is displayed differently in the syntactical and semantical analysis.
     *
     * @return item area containing the origin text
     */
    protected final JPanel getItemArea() {
        return this.itemArea;
    }

    /**
     * Getter for the placeholder for downward pointing arrows referring to a {@code partAfterArrow} of the represented {@link Proposition} (part).
     *
     * @return right arrow stack
     */
    protected final ArrowStack getRightArrows() {
        return this.rightArrows;
    }

    /**
     * Setter for the number of displayed downward arrows on the right of the item area.
     *
     * @param count
     *            number of displayed arrows to set
     */
    public final void setRightArrowCount(final int count) {
        this.rightArrows.setArrowCount(count);
    }

    /**
     * Getter for the translation input field.
     *
     * @return translation field (can be {@code null})
     */
    protected final JTextField getTranslationField() {
        return this.translationField;
    }

    /**
     * Getter for the represented {@link Proposition}.
     *
     * @return represented model element
     */
    @Override
    public final Proposition getRepresented() {
        return this.represented;
    }

    /**
     * Update the displayed identifier text to match the value in the represented {@link Proposition}.
     */
    public final void refreshLabelText() {
        if (this.labelField != null) {
            this.labelField.setText(this.represented.getLabel());
        }
    }

    /**
     * This does NOT refresh the translation TEXT, just fits the {@link Proposition} to the possible expanded translation field size. RECOMMENDED:
     * Override and use this method in extending class to refresh and fit the changed translation text.
     */
    protected void refreshTranslation() {
        if (this.translationField != null) {
            // if the translation text wants more space, it gets more
            final Dimension preferred = this.translationField.getPreferredSize();
            final int itemAreaWidth = this.itemArea.getSize().width;
            if (preferred.width > itemAreaWidth) {
                // enlarge the translation field
                this.translationField.setSize(preferred);
            } else {
                this.translationField.setSize(new Dimension(itemAreaWidth, preferred.height));
            }
            // enlarge the containing proposition
            this.contentPane.setSize(this.contentPane.getPreferredSize());
            // make sure it is still displayed
            this.itemArea.validate();
        }
    }

    /**
     * resets the tool tip info containing the comment text regarding its value in the represented {@link Proposition}.
     */
    public void refreshComment() {
        final String comment = this.getRepresented().getComment();
        if (comment == null || comment.isEmpty()) {
            this.setToolTipText(null);
        } else {
            this.setToolTipText(comment);
        }
    }

    @Override
    public final boolean isChecked() {
        return this.checkBox.isSelected();
    }

    @Override
    public final void setNotChecked() {
        this.checkBox.setSelected(false);
    }

    /**
     * Setter for the contained checkbox's visibility.
     *
     * @param visible
     *            visibility to set
     */
    public final void setCheckBoxVisible(final boolean visible) {
        this.checkBox.setVisible(visible);
        this.checkBoxDummy.setVisible(!visible);
    }

    /**
     * creates a {@link Dimension} for the indentation by setting its width regarding to the specified level.
     *
     * @param level
     *            number of indentations to be contained
     * @return {@link Dimension} representing the indentation
     */
    protected static final Dimension createIndentation(final int level) {
        return new Dimension(HmxGeneralOption.INDENTATION_WIDTH.getValueAsInteger() * level, 1);
    }

    @Override
    public void setDefaultBorder() {
        final boolean containsComment = this.represented.getComment() != null && !this.represented.getComment().trim().isEmpty();
        this.contentPane.setBorder(containsComment ? this.defaultBorderCommented : AbstractProposition.DEFAULT_BORDER);
        // without these visibility changes the item area may disappear
        this.itemArea.setVisible(false);
        // make sure the item area is still visible
        this.itemArea.setVisible(true);
    }

    @Override
    public final void setCommentBorder() {
        this.contentPane.setBorder(AbstractProposition.COMMENT_BORDER);
        // without these visibility changes the item area may disappear
        this.itemArea.setVisible(false);
        // make sure the item area is still visible
        this.itemArea.setVisible(true);
    }

    /**
     * Add the specified {@link MouseListener} to all components included except the single {@link SynItem}s in the syntactical analysis view.
     */
    @Override
    public synchronized void addMouseListener(final MouseListener listener) {
        super.addMouseListener(listener);
        if (this.labelField != null) {
            this.labelField.addMouseListener(listener);
        }
        this.indentationArea.addMouseListener(listener);
        this.leftArrows.addMouseListener(listener);
        this.itemArea.addMouseListener(listener);
        this.rightArrows.addMouseListener(listener);
        if (this.translationField != null) {
            this.translationField.addMouseListener(listener);
        }
    }

    /**
     * Ses the specified tool tip text to all of its components except the single {@link SynItem}s in the syntactical analysis view.
     */
    @Override
    public synchronized void setToolTipText(final String toolTip) {
        super.setToolTipText(toolTip);
        if (this.labelField != null) {
            this.labelField.setToolTipText(toolTip);
        }
        this.indentationArea.setToolTipText(toolTip);
        this.leftArrows.setToolTipText(toolTip);
        this.itemArea.setToolTipText(toolTip);
        this.rightArrows.setToolTipText(toolTip);
        if (this.translationField != null) {
            this.translationField.setToolTipText(toolTip);
        }
    }

    /**
     * A stack of arrows pointing up or down to indicate the connection of two {@link Proposition} parts with enclosed children.
     */
    protected static final class ArrowStack extends ScaledLabel {

        /** The base font size to apply – will be scaled with global setting. */
        private static final int BASE_ARROW_FONT_SIZE = 20;

        /** If the displayed arrows are pointing from bottom to top; else top to bottom. */
        private final boolean upward;
        /** The current number of arrows being displayed. */
        private int arrowCount = 0;

        /**
         * Constructor.
         *
         * @param pointsUp
         *            flag if the arrows should point upwards
         * @param count
         *            number of arrows to display
         */
        ArrowStack(final boolean pointsUp, final int count) {
            super(ArrowStack.buildArrowString(pointsUp, count), SwingConstants.CENTER);
            this.upward = pointsUp;
            this.setFont(new Font("Times New Roman", Font.BOLD, ArrowStack.BASE_ARROW_FONT_SIZE));
            this.setForeground(HmxGeneralOption.ARROW_COLOR.getValueAsColor());
            this.setArrowCount(count);
        }

        /**
         * Setter for the number of displayed arrows.
         *
         * @param count
         *            number of displayed arrows to set
         */
        void setArrowCount(final int count) {
            if (this.arrowCount != count) {
                this.arrowCount = count;
                if (count == 0) {
                    this.setVisible(false);
                    this.setText("");
                } else {
                    this.setText(ArrowStack.buildArrowString(this.upward, count));
                    this.setSize(this.getPreferredSize());
                    this.setVisible(true);
                }
            }
        }

        /**
         * Construct a string with the given number of arrows pointing in the indicating direction.
         *
         * @param pointsUp
         *            if the arrows should point upwards, otherwise downwards
         * @param arrowCount
         *            number arrows to include
         * @return string of unicode arrows
         */
        private static String buildArrowString(final boolean pointsUp, final int arrowCount) {
            if (arrowCount < 1) {
                return "";
            }
            final char singleArrow = pointsUp ? '\u2191' : '\u2193';
            final StringBuffer arrows = new StringBuffer(arrowCount);
            for (int i = 0; i < arrowCount; i++) {
                arrows.append(singleArrow);
            }
            return arrows.toString();
        }
    }
}
