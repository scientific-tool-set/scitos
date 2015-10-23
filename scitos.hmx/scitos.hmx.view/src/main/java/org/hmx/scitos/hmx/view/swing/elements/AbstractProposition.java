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
    /** represented model {@link Proposition}. */
    private final Proposition represented;
    /** raised bevel border with color, when not selected and with comment set */
    private final Border defaultBorderCommented;

    private final JPanel contentPane = new JPanel(new GridBagLayout());

    private final JTextField labelField = new ScaledTextField();
    private final JPanel indentationArea = new JPanel(new GridBagLayout());
    private final ArrowStack leftArrows;
    private final JPanel itemArea = new JPanel();
    private final ArrowStack rightArrows;
    private final JTextField translationField = new ScaledTextField();
    private final JCheckBox checkBox = new JCheckBox();
    private final JPanel checkBoxDummy = new JPanel();

    /**
     * creates an {@link AbstractProposition} by setting all values regarding to the represented {@link Proposition}.
     *
     * @param modelHandler
     *            the responsible model handler implementation realizing any model changes
     * @param represented
     *            represented {@link Proposition} to set
     */
    protected AbstractProposition(final HmxModelHandler modelHandler, final Proposition represented) {
        super(new GridBagLayout());
        this.modelHandler = modelHandler;
        this.represented = represented;
        final boolean leftAligned = modelHandler.getModel().isLeftToRightOriented();
        this.defaultBorderCommented =
                BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(2, leftAligned ? 1 : 0, 2, leftAligned ? 0 : 1,
                                HmxGeneralOption.COMMENTED_BORDER_COLOR.getValueAsColor()),
                        BorderFactory.createEmptyBorder(0, leftAligned ? 1 : 2, 4, leftAligned ? 2 : 1)));
        final ComponentOrientation orientation = leftAligned ? ComponentOrientation.LEFT_TO_RIGHT : ComponentOrientation.RIGHT_TO_LEFT;
        this.setComponentOrientation(orientation);
        this.contentPane.setComponentOrientation(orientation);
        this.itemArea.setComponentOrientation(orientation);
        this.leftArrows = new ArrowStack(true, 0);
        this.rightArrows = new ArrowStack(false, 0);
        this.initCheckboxAndLabel();
        this.initOriginTextArea();
        this.initTranslationArea();
        this.add(this.contentPane);
        this.setDefaultBorder();
    }

    /**
     * initializes the left part of the {@link AbstractProposition} containing the {@link JCheckBox}, the label {@link JTextField} and an expanding
     * {@link JPanel} for the indentations in the syntactical analysis view.
     */
    private void initCheckboxAndLabel() {
        // checkBox
        this.checkBoxDummy.setPreferredSize(this.checkBox.getPreferredSize());
        this.setCheckBoxVisible(this.represented.getPartBeforeArrow() == null);
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        this.contentPane.add(this.checkBox, constraints);
        this.contentPane.add(this.checkBoxDummy, constraints);
        // labelField
        this.labelField.setColumns(Proposition.MAX_LABEL_LENGTH - 1);
        this.labelField.setDocument(new Validation(Proposition.MAX_LABEL_LENGTH));
        this.labelField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(final FocusEvent event) {
                AbstractProposition.this.lostFocusOnLabel();
            }
        });
        this.refreshLabelText();
        constraints.gridx = 1;
        this.contentPane.add(this.labelField, constraints);
        // indentationArea
        constraints.fill = GridBagConstraints.VERTICAL;
        constraints.weighty = 1;
        constraints.gridheight = 3;
        constraints.gridx = 2;
        constraints.gridy = 0;
        this.contentPane.add(this.indentationArea, constraints);
    }

    /**
     * deal with a label text which lost the focus and might have been changed
     */
    final void lostFocusOnLabel() {
        // only transfer if necessary
        final String labelText = this.labelField.getText();
        if (!ComparisonUtil.isNullOrEmptyAwareEqual(this.represented.getLabel(), labelText)) {
            this.getModelHandler().setLabelText(this.represented, labelText);
        }
    }

    /**
     * Getter for the associated {@link HmxModelHandler} implementation.
     *
     * @return the responsible model handler implementation realizing any model changes
     */
    protected final HmxModelHandler getModelHandler() {
        return this.modelHandler;
    }

    /**
     * initializes the top right part of the proposition containing a panel for the origin text as well as arrows on the left and on the right side of
     * it.
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
     * initializes the bottom right part of the proposition containing the text field for the translation.
     */
    private final void initTranslationArea() {
        this.translationField.setDocument(new Validation(Proposition.MAX_TRANSLATION_LENGTH));
        this.refreshTranslation();
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weighty = 1;
        constraints.gridwidth = 4;
        constraints.gridx = 3;
        constraints.gridy = 2;
        this.contentPane.add(this.translationField, constraints);
    }

    /**
     * @return label field
     */
    protected final JTextField getLabelField() {
        return this.labelField;
    }

    /**
     * @return indentation area
     */
    protected final JPanel getIndentationArea() {
        return this.indentationArea;
    }

    /**
     * @return left arrow stack
     */
    protected final ArrowStack getLeftArrows() {
        return this.leftArrows;
    }

    /**
     * sets the number of displayed upward arrows on the left of the item area.
     *
     * @param count
     *            number of displayed arrows to set
     */
    public final void setLeftArrowCount(final int count) {
        this.leftArrows.setArrowCount(count);
    }

    /**
     * @return item area containing the origin text
     */
    protected final JPanel getItemArea() {
        return this.itemArea;
    }

    /**
     * @return right arrow stack
     */
    protected final ArrowStack getRightArrows() {
        return this.rightArrows;
    }

    /**
     * sets the number of displayed downward arrows on the right of the item area.
     *
     * @param count
     *            number of displayed arrows to set
     */
    public final void setRightArrowCount(final int count) {
        this.rightArrows.setArrowCount(count);
    }

    /**
     * @return translation field
     */
    protected final JTextField getTranslationField() {
        return this.translationField;
    }

    /**
     * @return represented {@link Proposition}
     */
    @Override
    public final Proposition getRepresented() {
        return this.represented;
    }

    /**
     * sets the label text regarding the represented {@link Proposition}s label.
     */
    public final void refreshLabelText() {
        this.labelField.setText(this.represented.getLabel());
    }

    /**
     * does NOT refresh the translation TEXT, just fits the proposition on possible expanded translation field size<br>
     * RECOMMENDED: Override and use this method in extending class to refresh and fit the changed translation text.
     */
    protected void refreshTranslation() {
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

    @Override
    public final boolean isChecked() {
        return this.checkBox.isSelected();
    }

    @Override
    public final void setNotChecked() {
        this.checkBox.setSelected(false);
    }

    /**
     * sets the visibility of the {@link JCheckBox} contained.
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
     * adds the specified {@link MouseListener} to all components included except the single {@link SynItem}s in the syntactical analysis view.
     */
    @Override
    public synchronized void addMouseListener(final MouseListener listener) {
        super.addMouseListener(listener);
        this.labelField.addMouseListener(listener);
        this.indentationArea.addMouseListener(listener);
        this.leftArrows.addMouseListener(listener);
        this.itemArea.addMouseListener(listener);
        this.rightArrows.addMouseListener(listener);
        this.translationField.addMouseListener(listener);
    }

    /**
     * sets the specified tool tip text to all of its components except the single {@link SynItem}s in the syntactical analysis view.
     */
    @Override
    public synchronized void setToolTipText(final String toolTip) {
        super.setToolTipText(toolTip);
        this.labelField.setToolTipText(toolTip);
        this.indentationArea.setToolTipText(toolTip);
        this.leftArrows.setToolTipText(toolTip);
        this.itemArea.setToolTipText(toolTip);
        this.rightArrows.setToolTipText(toolTip);
        this.translationField.setToolTipText(toolTip);
    }

    /**
     * A stack of arrows pointing up or down to indicate the connection of two {@link Proposition} parts with enclosed children.
     */
    protected static final class ArrowStack extends ScaledLabel {

        /** The base font size to apply – will be scaled with global setting. */
        private static final int BASE_ARROW_FONT_SIZE = 20;

        /** If the displayed arrows are pointing from bottom to top; else top to bottom */
        private final boolean upward;
        /** The current number of arrows being displayed. */
        private int arrowCount = 0;

        /**
         * creates a new {@link ArrowStack} with the specified direction, number of arrows and size
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
