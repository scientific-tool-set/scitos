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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.hmx.scitos.core.option.Option;
import org.hmx.scitos.hmx.core.option.HmxGeneralOption;
import org.hmx.scitos.hmx.domain.model.AbstractConnectable;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.domain.model.Relation;
import org.hmx.scitos.hmx.view.ContextMenuFactory;
import org.hmx.scitos.hmx.view.IPericopeView;
import org.hmx.scitos.hmx.view.swing.components.SemAnalysisPanel;
import org.hmx.scitos.hmx.view.swing.components.SemControl;
import org.hmx.scitos.view.ContextMenuBuilder;
import org.hmx.scitos.view.swing.ContextMenuPopupBuilder;
import org.hmx.scitos.view.swing.components.ScaledTextField;

/**
 * View representation of a {@link Relation} drawing colored lines to show the relations between its subordinated {@link IConnectable}s and
 * {@link JTextField}s above displaying their roles.
 */
public final class ViewRelation extends AbstractCommentable<Relation> implements IConnectable<Relation> {

    /** half thickness of the displayed lines. */
    protected static final int HALF_LINE_THICKNESS = 2;
    /** lowered bevel border, when selected. */
    protected static final Border COMMENT_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(2, 0, 2, 0));
    /** Raised bevel border, when not selected and no comment assigned. */
    private final Border defaultBorder;
    /** Colored, raised bevel border, when not selected and with assigned comment. */
    private final Border defaultCommentedBorder;
    /** The semantical analysis this is displayed in. */
    final SemAnalysisPanel semArea;
    /** The view representations of the sub ordinated associates. */
    private final List<IConnectable<?>> viewAssociates;
    /** The text fields displaying the respective roles of the sub ordinated associates. */
    private final List<JTextField> roleFields;
    /** The color of the relation lines. */
    private final Color color = HmxGeneralOption.RELATION_COLOR.getValueAsColor();
    /**
     * The represented model {@link Relation}.
     */
    private final Relation represented;
    /** The origin language of the current analysis is aligned from left to right. */
    private final boolean leftAligned;
    /** Whether the role labels should be shown above the relation lines. Otherwise, they will be shown on top of the lines (i.e. hiding them). */
    private final boolean showRoleAboveLine;
    /**
     * check box to select this {@link ViewRelation}.
     */
    private final JCheckBox checkBox = new JCheckBox();
    /** The depth in the relation tree of the current analysis. */
    private final int depth;
    /**
     * index of the first contained {@link Proposition} (sub) associate.
     */
    private final double firstGridY;
    /**
     * index of the last contained {@link Proposition} (sub) associate.
     */
    private final double lastGridY;
    /**
     * index where to connect to the super ordinated {@link Relation}.
     */
    private double connectY;

    /**
     * Constructor.
     *
     * @param viewReference
     *            the view providing access to the project's model handler and handling the comments on model elements
     * @param semArea
     *            semantical analysis view to be contained in
     * @param represented
     *            model {@link Relation} to display
     * @param foldedLevels
     *            levels to suppress display of semantic roles on
     */
    public ViewRelation(final IPericopeView viewReference, final SemAnalysisPanel semArea, final Relation represented,
            final Collection<Integer> foldedLevels) {
        super(null);
        this.semArea = semArea;
        this.represented = represented;
        this.leftAligned = viewReference.getModelHandler().getModel().isLeftToRightOriented();
        final List<AbstractConnectable> modelAssociates = represented.getAssociates();
        this.showRoleAboveLine = viewReference.getViewSettings().isShowingSemanticTranslations()
                || viewReference.getViewSettings().isShowingSyntacticTranslations();
        this.viewAssociates = new ArrayList<IConnectable<?>>(modelAssociates.size());
        for (final AbstractConnectable singleAssociate : modelAssociates) {
            this.viewAssociates.add(SemControl.getRepresentative(semArea, singleAssociate));
        }
        this.depth = represented.getTreeDepth();
        if (represented.getSuperOrdinatedRelation() == null) {
            this.add(this.checkBox);
        }
        if (foldedLevels.contains(this.depth)) {
            this.roleFields = null;
        } else {
            final int associateCount = modelAssociates.size();
            this.roleFields = new ArrayList<JTextField>(associateCount);
            final Border outsideBorder;
            if (this.showRoleAboveLine) {
                outsideBorder = BorderFactory.createLoweredBevelBorder();
            } else {
                outsideBorder = BorderFactory.createLineBorder(this.color, 1);
            }
            final Border fieldBorder = BorderFactory.createCompoundBorder(outsideBorder, BorderFactory.createEmptyBorder(2, 2, 2, 2));
            for (int i = 0; i < associateCount; i++) {
                final JTextField roleField = new ScaledTextField();
                roleField.setEditable(false);
                roleField.setBorder(fieldBorder);
                this.roleFields.add(roleField);
                this.add(roleField);
            }
        }
        this.firstGridY = SemControl.getRepresentative(semArea, represented.getFirstPropositionContained()).getConnectY();
        this.lastGridY = SemControl.getRepresentative(semArea, represented.getLastPropositionContained()).getConnectY();
        this.refreshRoles();
        this.setToolTipText(represented.getComment());
        this.defaultBorder =
                BorderFactory.createEmptyBorder(ViewRelation.COMMENT_BORDER.getBorderInsets(this).top, 0,
                        ViewRelation.COMMENT_BORDER.getBorderInsets(this).bottom, 0);
        final Insets borderInsets = ViewRelation.COMMENT_BORDER.getBorderInsets(this);
        this.defaultCommentedBorder =
                BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(borderInsets.top - 2, this.leftAligned
                        ? (borderInsets.left - 1) : 0, borderInsets.bottom - 2, this.leftAligned ? 0 : (borderInsets.right - 1)), BorderFactory
                        .createMatteBorder(2, this.leftAligned ? 1 : 0, 2, this.leftAligned ? 0 : 1,
                                HmxGeneralOption.COMMENTED_BORDER_COLOR.getValueAsColor()));
        this.setDefaultBorder();
        // add functionality
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(final MouseEvent event) {
                viewReference.handleSelectedCommentable(ViewRelation.this);
                this.mouseReleased(event);
            }

            @Override
            public void mouseReleased(final MouseEvent event) {
                if (event.isPopupTrigger()) {
                    final ContextMenuBuilder contextMenu = ContextMenuFactory.createSemRelationPopup(viewReference, represented);
                    ContextMenuPopupBuilder.buildSwingPopupMenu(contextMenu).show(event.getComponent(), event.getX(), event.getY());
                }
            }
        });
        this.setPreferredSize(new Dimension(this.calculateWidth(), this.getPreferredSize().height));
    }

    /**
     * draw the colored lines to show the relations between its subordinated {@link IConnectable}s and calculates the bounds of the role
     * {@link JTextField}s.
     *
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    @Override
    protected void paintComponent(final Graphics graphics) {
        super.paintComponent(graphics);
        final int endX = this.calculateWidth();
        if (endX > this.getSize().width) {
            final Dimension size = new Dimension(endX, this.getSize().height);
            this.setPreferredSize(size);
            this.setSize(size);
            return;
        }
        final Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setColor(this.color);
        // prepare for calculations
        final int gridHeight = (int) (this.lastGridY - this.firstGridY) + 1;
        final int height = this.getSize().height;
        final double partY = height / (double) gridHeight;
        int startX;
        if (this.represented.getSuperOrdinatedRelation() == null) {
            final Dimension boxSize = this.checkBox.getPreferredSize();
            int posX;
            if (this.leftAligned) {
                posX = ViewRelation.HALF_LINE_THICKNESS;
                startX = (boxSize.width + (2 * ViewRelation.HALF_LINE_THICKNESS));
            } else {
                posX = this.getSize().width - (boxSize.width + ViewRelation.HALF_LINE_THICKNESS);
                startX = this.getSize().width - (boxSize.width + (2 * ViewRelation.HALF_LINE_THICKNESS));
            }
            // insert check box
            this.checkBox.setBounds(posX, (int) (((this.connectY - (this.firstGridY - 0.5)) * partY) - (boxSize.height / 2.)), boxSize.width,
                    boxSize.height);
        } else {
            if (this.leftAligned) {
                startX = 0;
            } else {
                startX = this.getSize().width;
            }
        }
        final int topBorder = ViewRelation.COMMENT_BORDER.getBorderInsets(this).top;
        final int lineLeftEnd;
        final int lineWidth;
        final List<Integer> horizontalLines = new ArrayList<Integer>(this.viewAssociates.size());
        if (this.leftAligned) {
            lineLeftEnd = startX;
            lineWidth = this.getSize().width - startX;
        } else {
            lineLeftEnd = 0;
            lineWidth = startX;
        }
        // draw horizontal lines
        for (final IConnectable<?> singleAssociate : this.viewAssociates) {
            final int lineY = (int) ((singleAssociate.getConnectY() - this.firstGridY + 0.5) * partY - (topBorder / 2.));
            final Rectangle singleHorizontal = new Rectangle(lineLeftEnd, lineY, lineWidth, (2 * ViewRelation.HALF_LINE_THICKNESS));
            graphics2D.draw(singleHorizontal);
            graphics2D.fill(singleHorizontal);
            horizontalLines.add(lineY);
        }
        // draw vertical line
        final int verticalPos;
        if (this.leftAligned) {
            verticalPos = lineLeftEnd;
        } else {
            verticalPos = startX - (2 * ViewRelation.HALF_LINE_THICKNESS);
        }
        final Rectangle verticalLine =
                new Rectangle(verticalPos, horizontalLines.get(0), (2 * ViewRelation.HALF_LINE_THICKNESS), (horizontalLines.get(horizontalLines
                        .size() - 1) - horizontalLines.get(0)) + (2 * ViewRelation.HALF_LINE_THICKNESS));
        graphics2D.draw(verticalLine);
        graphics2D.fill(verticalLine);
        if (this.roleFields != null) {
            int fieldX = startX + 2 + (3 * ViewRelation.HALF_LINE_THICKNESS);
            // insert role text fields
            for (int i = 0; i < this.roleFields.size(); i++) {
                final JTextField roleField = this.roleFields.get(i);
                final int fieldHeight = roleField.getPreferredSize().height;
                final int fieldWidth = roleField.getPreferredSize().width + 5;
                if (!this.leftAligned) {
                    fieldX = startX - (2 + (3 * ViewRelation.HALF_LINE_THICKNESS) + fieldWidth);
                }
                int fieldY = horizontalLines.get(i);
                if (this.showRoleAboveLine) {
                    // ensure the field is shown above the line with spacing of 2px
                    fieldY -= fieldHeight + ViewRelation.HALF_LINE_THICKNESS + 2;
                } else {
                    // display the field directly over the line (half above/half below)
                    fieldY -= fieldHeight / 2;
                }
                roleField.setBounds(fieldX, fieldY, fieldWidth, fieldHeight);
            }
        }
    }

    /**
     * Calculate the minimum preferred width of the {@link ViewRelation} regarding the {@link JTextField}s containing the roles and the additional
     * border spacings.
     *
     * @return minimum preferred width
     */
    private int calculateWidth() {
        int result = 0;
        if (this.roleFields != null) {
            // get the maximum width of the role fields
            for (final JTextField singleRoleField : this.roleFields) {
                result = Math.max(result, singleRoleField.getPreferredSize().width);
            }
        }
        // regard the additional checkbox if the relation is checkable
        if (this.represented.getSuperOrdinatedRelation() == null) {
            result += this.checkBox.getPreferredSize().width + (2 * ViewRelation.HALF_LINE_THICKNESS);
        }
        // add the default left and right spacing
        return result + 11 + (4 * ViewRelation.HALF_LINE_THICKNESS);
    }

    @Override
    public Relation getRepresented() {
        return this.represented;
    }

    @Override
    public void setCheckBoxVisible(final boolean val) {
        this.checkBox.setVisible(val);
    }

    @Override
    public boolean isChecked() {
        return this.checkBox.isSelected();
    }

    @Override
    public void setNotChecked() {
        this.checkBox.setSelected(false);
    }

    /**
     * Check if the semantical roles are currently hidden/invisible.
     *
     * @return if the semantical roles are hidden
     */
    public boolean isFolded() {
        return this.roleFields == null;
    }

    @Override
    public int getDepth() {
        return this.depth;
    }

    @Override
    public void setDefaultBorder() {
        final boolean containsComment = this.represented.getComment() != null && !this.represented.getComment().trim().isEmpty();
        this.setBorder(containsComment ? this.defaultCommentedBorder : this.defaultBorder);
    }

    @Override
    public void setCommentBorder() {
        this.setBorder(ViewRelation.COMMENT_BORDER);
    }

    @Override
    public double getConnectY() {
        return this.connectY;
    }

    /**
     * Getter for top most vertical position contained in the represented {@link Relation} subtree. This is the index of the first contained
     * {@link Proposition} in the {@link Pericope} {@code + 0.5} (e.g. {@code 0.5}, {@code 1.5}, {@code 2.5}, ...).
     *
     * @return point to connect of the first contained {@link SemProposition}
     */
    public double getFirstGridY() {
        return this.firstGridY;
    }

    /**
     * Getter for bottom most vertical position contained in the represented {@link Relation} subtree. This is the index of the last contained
     * {@link Proposition} in the {@link Pericope} {@code + 0.5} (e.g. {@code 1.5}, {@code 2.5}, {@code 3.5}, ...).
     *
     * @return point to connect of the last contained {@link SemProposition}
     */
    public double getLastGridY() {
        return this.lastGridY;
    }

    @Override
    public void setToolTipText(final String text) {
        super.setToolTipText(text);
        if (this.roleFields != null) {
            for (final JTextField singleRoleField : this.roleFields) {
                singleRoleField.setToolTipText(text);
            }
        }
    }

    /**
     * Update the displayed associate roles to match the values in the represented {@link Relation}.
     */
    private void refreshRoles() {
        if (this.roleFields != null) {
            final Iterator<JTextField> roleFieldIterator = this.roleFields.iterator();
            for (final IConnectable<?> singleAssociate : this.viewAssociates) {
                String role = singleAssociate.getRepresented().getRole().getRole();
                if (singleAssociate.getRepresented().getRole().isHighWeight()) {
                    role = role.toUpperCase(Option.TRANSLATION.getValueAsLocale());
                }
                final JTextField roleField = roleFieldIterator.next();
                roleField.setText(role);
                roleField.setSize(roleField.getPreferredSize());
            }
            // add a count on the end, if there are equal roles
            int equalRoleCount = 1;
            for (int i = 1; i < this.roleFields.size(); i++) {
                final JTextField previousRoleField = this.roleFields.get(i - 1);
                final JTextField thisRoleField = this.roleFields.get(i);
                if (previousRoleField.getText().equals(thisRoleField.getText())) {
                    // priors role equals currents role
                    previousRoleField.setText(previousRoleField.getText() + equalRoleCount);
                    equalRoleCount++;
                    if (i + 1 == this.roleFields.size() || !thisRoleField.getText().equals(this.roleFields.get(i + 1).getText())) {
                        // no follower or followers role not equals currents role
                        thisRoleField.setText(thisRoleField.getText() + equalRoleCount);
                        equalRoleCount++;
                    }
                } else if (i + 1 < this.roleFields.size() && previousRoleField.getText().equals(this.roleFields.get(i + 1).getText())) {
                    // priors role equals followers role
                    previousRoleField.setText(previousRoleField.getText() + equalRoleCount);
                    equalRoleCount++;
                } else if (i - 1 > 0 && this.roleFields.get(i - 2).getText().startsWith(thisRoleField.getText())) {
                    // followers role equals priors role
                    thisRoleField.setText(thisRoleField.getText() + equalRoleCount);
                    equalRoleCount++;
                }
            }
        }
        final Dimension preferred = this.getPreferredSize();
        this.setSize(new Dimension(preferred.width + (2 * ViewRelation.HALF_LINE_THICKNESS), preferred.height));

        // recalculate connectY
        final boolean firstAssociateHighWeight = this.viewAssociates.get(0).getRepresented().getRole().isHighWeight();
        for (final IConnectable<?> singleAssociate : this.viewAssociates) {
            if (firstAssociateHighWeight != singleAssociate.getRepresented().getRole().isHighWeight()) {
                // connectY equals the connectY of the heavy weight associate
                if (firstAssociateHighWeight) {
                    this.connectY = this.viewAssociates.get(0).getConnectY();
                } else {
                    this.connectY = singleAssociate.getConnectY();
                }
                return;
            }
        }
        // connectY is the mid of the drawn relation
        this.connectY =
                Math.round(this.viewAssociates.get(0).getConnectY() + this.viewAssociates.get(this.viewAssociates.size() - 1).getConnectY()) * 0.5;
    }

    /**
     * Add the specified {@link MouseListener} to itself and all of its {@link Component}s.
     */
    @Override
    public synchronized void addMouseListener(final MouseListener listener) {
        super.addMouseListener(listener);
        for (final Component singleComponent : this.getComponents()) {
            singleComponent.addMouseListener(listener);
        }
    }
}
