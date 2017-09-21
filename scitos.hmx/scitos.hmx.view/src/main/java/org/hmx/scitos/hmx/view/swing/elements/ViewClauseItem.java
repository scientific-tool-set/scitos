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

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.hmx.scitos.hmx.core.option.HmxGeneralOption;
import org.hmx.scitos.hmx.domain.model.ClauseItem;
import org.hmx.scitos.hmx.domain.model.ClauseItem.Style;
import org.hmx.scitos.hmx.domain.model.SyntacticalFunction;
import org.hmx.scitos.hmx.view.ContextMenuFactory;
import org.hmx.scitos.hmx.view.IPericopeView;
import org.hmx.scitos.view.ContextMenuBuilder;
import org.hmx.scitos.view.swing.ContextMenuPopupBuilder;
import org.hmx.scitos.view.swing.components.ScaledLabel;
import org.hmx.scitos.view.swing.components.ScaledTextPane;

/**
 * view representation of a {@link ClauseItem} in the syntactical analysis view consisting of a non-editable {@link JTextPane} for the origin text on
 * the top and a label displaying the current selected function on the bottom.
 */
public final class ViewClauseItem extends AbstractCommentable<ClauseItem> {

    /** etched border, when not selected. */
    private static final Border DEFAULT_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
            BorderFactory.createEmptyBorder(2, 0, 2, 0));
    /** lowered bevel border, when selected. */
    private static final Border COMMENT_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(2, 0, 2, 0));

    /** etched border with color, when not selected and with comment set. */
    private final Border defaultBorderCommented = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
            BorderFactory.createMatteBorder(2, 0, 2, 0, HmxGeneralOption.COMMENTED_BORDER_COLOR.getValueAsColor()));
    /** The containing view, providing access to higher functions. */
    IPericopeView viewReference;
    /**
     * represented model {@link ClauseItem}.
     */
    private final ClauseItem represented;
    /** text field to display the origin text in. */
    private final JTextPane originTextPane;
    /** label for showing the syntactical function. */
    private final JLabel functionLabel;

    /** mouse listener creating the popup containing all available functions. */
    private MouseAdapter popupListener;

    /**
     * Constructor: for a new {@link SynItem} in the given view representing the specified {@link ClauseItem}.
     *
     * @param viewReference
     *            containing view, enabling further interactions
     * @param represented
     *            model element to represent/display
     */
    protected ViewClauseItem(final IPericopeView viewReference, final ClauseItem represented) {
        super(new GridBagLayout());
        this.viewReference = viewReference;
        this.represented = represented;
        this.originTextPane = new ScaledTextPane();
        this.functionLabel = new ScaledLabel(represented.getFunction() == null ? null : represented.getFunction().getCode());
        this.setDefaultBorder();
        this.initOriginTextPane();
        this.initFunctionLabel();
        this.refreshFontStyle();
        // initialize the comment showing listener for the item
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(final MouseEvent event) {
                viewReference.handleSelectedCommentable(ViewClauseItem.this);
            }
        });
        // initialize the popup menu and its listener for the item
        this.refreshPopup();
        this.refreshComment();
    }

    /** Initialize the origin text pane on the top. */
    private void initOriginTextPane() {
        this.originTextPane.setFont(this.viewReference.getModelHandler().getModel().getFont());
        this.originTextPane.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        this.originTextPane.setEditable(false);
        this.refreshOriginText();
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        this.add(this.originTextPane, constraints);
    }

    /** Initialize the label for displaying the function on the bottom. */
    private void initFunctionLabel() {
        this.functionLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        this.functionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.refreshFunction();
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 1;
        this.add(this.functionLabel, constraints);
    }

    /**
     * Create and enable the {@link JPopupMenu}.
     */
    private void refreshPopup() {
        if (this.popupListener != null) {
            // remove the old popup listener
            this.removeMouseListener(this.popupListener);
        }
        // create new popup menu and its referring listener
        this.popupListener = new MouseAdapter() {

            @Override
            public void mousePressed(final MouseEvent event) {
                if (event.isPopupTrigger()) {
                    final ClauseItem item = ViewClauseItem.this.getRepresented();
                    final ContextMenuBuilder contextMenu;
                    if (item.getParent().getPartBeforeArrow() == null) {
                        contextMenu = ContextMenuFactory.createSynItemPopup(ViewClauseItem.this.viewReference, item);
                    } else {
                        contextMenu = ContextMenuFactory.createSynItemAfterArrowPopup(ViewClauseItem.this.viewReference, item);
                    }
                    ContextMenuPopupBuilder.buildSwingPopupMenu(contextMenu).show(event.getComponent(), event.getX(), event.getY());
                }
            }

            @Override
            public void mouseReleased(final MouseEvent event) {
                this.mousePressed(event);
            }
        };
        // add the popup menu and its listener
        this.addMouseListener(this.popupListener);
    }

    @Override
    public ClauseItem getRepresented() {
        return this.represented;
    }

    public void refresh() {
        this.refreshFontStyle();
        this.refreshOriginText();
        this.refreshFunction();
        this.refreshComment();
    }

    /**
     * Update the displayed {@link SyntacticalFunction} to match the current value in the represented {@link ClauseItem}.
     */
    void refreshFunction() {
        final SyntacticalFunction function = this.represented.getFunction();
        final String functionName;
        final boolean underline;
        if (function == null) {
            functionName = " ";
            underline = false;
        } else {
            functionName = function.getCode();
            underline = function.isUnderlined();
        }
        this.functionLabel.setText(functionName);
        ViewClauseItem.setTextPaneUnderlined(this.originTextPane, underline);
        this.functionLabel.setSize(this.functionLabel.getPreferredSize());
    }

    /**
     * Update the displayed origin text to match the current value in the represented {@link ClauseItem}.
     */
    void refreshOriginText() {
        this.originTextPane.setText(this.represented.getOriginText());
        this.refreshPopup();
    }

    /**
     * Update the displayed {@link Font} style to match the current setting in the represented {@link ClauseItem}.
     */
    void refreshFontStyle() {
        final Style style = this.represented.getFontStyle();
        ViewClauseItem.setTextPaneFontStyle(this.originTextPane, style);
        this.functionLabel.setFont(this.functionLabel.getFont().deriveFont(ContextMenuFactory.getFontStyleValue(style)));
    }

    /**
     * Reset the tool tip info containing the comment text to match the value in the represented {@link ClauseItem}.
     */
    void refreshComment() {
        final String comment = this.getRepresented().getComment();
        if (comment == null || comment.isEmpty()) {
            this.setToolTipText(null);
        } else {
            this.setToolTipText(comment);
        }
    }

    @Override
    public void setDefaultBorder() {
        final boolean containsComment = this.represented.getComment() != null && !this.represented.getComment().trim().isEmpty();
        this.setBorder(containsComment ? this.defaultBorderCommented : ViewClauseItem.DEFAULT_BORDER);
        if (this.getParent() != null) {
            ((JComponent) this.getParent()).revalidate();
        }
    }

    @Override
    public void setCommentBorder() {
        this.setBorder(ViewClauseItem.COMMENT_BORDER);
    }

    @Override
    public void setToolTipText(final String toolTip) {
        this.originTextPane.setToolTipText(toolTip);
        this.functionLabel.setToolTipText(toolTip);
        super.setToolTipText(toolTip);
    }

    @Override
    public void setComponentPopupMenu(final JPopupMenu popup) {
        super.setComponentPopupMenu(popup);
        this.originTextPane.setComponentPopupMenu(popup);
        this.functionLabel.setComponentPopupMenu(popup);
    }

    @Override
    public synchronized void addMouseListener(final MouseListener listener) {
        this.originTextPane.addMouseListener(listener);
        this.functionLabel.addMouseListener(listener);
        super.addMouseListener(listener);
    }

    @Override
    public synchronized void removeMouseListener(final MouseListener listener) {
        this.originTextPane.removeMouseListener(listener);
        this.functionLabel.removeMouseListener(listener);
        super.removeMouseListener(listener);
    }

    /**
     * Set the {@link Font} style of the designated {@link JTextPane} to the specified value.
     *
     * @param target
     *            {@link JTextPane} to set the {@link Font} style
     * @param style
     *            {@link Font} style to set
     */
    private static void setTextPaneFontStyle(final JTextPane target, final Style style) {
        final MutableAttributeSet attributes = target.getInputAttributes();
        StyleConstants.setBold(attributes, style == Style.BOLD || style == Style.BOLD_ITALIC);
        StyleConstants.setItalic(attributes, style == Style.ITALIC || style == Style.BOLD_ITALIC);
        final StyledDocument document = target.getStyledDocument();
        document.setCharacterAttributes(0, document.getLength() + 1, attributes, true);
    }

    /**
     * Underline the text in the designated {@link JTextPane}, if the associated flag is {@code true}, remove it otherwise.
     *
     * @param target
     *            {@link JTextPane} to underline
     * @param underline
     *            flag to enable/disable underlining
     */
    private static void setTextPaneUnderlined(final JTextPane target, final boolean underline) {
        final MutableAttributeSet attributes = target.getInputAttributes();
        StyleConstants.setUnderline(attributes, underline);
        final StyledDocument document = target.getStyledDocument();
        document.setCharacterAttributes(0, document.getLength() + 1, attributes, true);
    }
}
