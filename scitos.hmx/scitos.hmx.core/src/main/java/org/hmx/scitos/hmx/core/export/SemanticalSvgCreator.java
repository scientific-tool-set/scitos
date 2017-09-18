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

package org.hmx.scitos.hmx.core.export;

import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.core.option.Option;
import org.hmx.scitos.domain.util.CollectionUtil;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.core.option.HmxExportOption;
import org.hmx.scitos.hmx.domain.model.AbstractConnectable;
import org.hmx.scitos.hmx.domain.model.ClauseItem;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.domain.model.Relation;
import org.hmx.scitos.hmx.domain.model.RelationTemplate.AssociateRole;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of the svg export functionality for the semantical analysis contained in a {@link Pericope}.
 */
class SemanticalSvgCreator extends AbstractSvgCreator {

    private static final double ARROW_SCALE = .6;

    /**
     * Width of the lines being drawn to represent a {@link Relation}.
     */
    private final double relationStroke;
    /**
     * Stroke color applied to the lines being drawn to represent a {@link Relation}.
     */
    private final String colorRelation;
    /**
     * Font color applied to the associates' roles in a {@link Relation}.
     */
    private final String colorSemRole;

    /** If comments should be indicated by a numeric identifier on the commented element. */
    private boolean commentsIncluded;
    /**
     * The running counter for the numeric identifier on commented elements, if the {@code commentsIncluded} is set to {@code true}.
     */
    private int commentCounter;
    /**
     * Which {@link Locale} to use when converting the high weight associate roles in a {@link Relation} to upper case.
     */
    private Locale locale;
    /**
     * The overall maximum width of a single {@link Proposition} in the currently generated svg document, in order to determine the position of the
     * {@link Relation}s.
     */
    private double propositionExtentX;
    /**
     * The horizontal space reserved for the {@link Proposition} labels.
     */
    private double labelWidth;
    /**
     * The baseline (y coordinate) of the label and origin text in a {@link Proposition}.
     */
    private double originTextBaseLine;
    /**
     * The vertical space reserved for a {@link Proposition}.
     */
    private double propositionHeight;
    /**
     * The vertical space reserved for the a {@link Proposition}'s translation text.
     */
    private double translationHeight;
    /**
     * The expected height of a single arrow connecting a {@link Proposition}'s parts with enclosed children.
     */
    private double arrowHeight;
    /**
     * The expected width of a single arrow connecting a {@link Proposition}'s parts with enclosed children.
     */
    private double arrowWidth;
    /**
     * The reserved vertical space for an associate's role label in a {@link Relation}.
     */
    private double roleHeight;
    /**
     * The reserved horizontal space for the role labels in {@link Relation}s on the different levels.
     */
    private List<Double> semColumnWidths;

    /**
     * Constructor.
     * 
     * @param model
     *            the model containing the semantical analysis to be exported into a svg document
     */
    protected SemanticalSvgCreator(final Pericope model) {
        super(model);
        this.relationStroke = this.verticalSpacing / 2;
        this.colorRelation = HmxExportOption.RELATION_COLOR.getValue();
        this.colorSemRole = HmxExportOption.FONTCOLOR_SEMROLE.getValue();
    }

    /**
     * Generate an svg document representing the semantical analysis.
     *
     * @param includeComments
     *            if the comments are to be included in the created SVG as slightly smaller numbers and invisible description elements (in some SVG
     *            viewers processed as tool tips)
     * @return generated svg document
     * @throws HmxException
     *             failed to create an empty document to be filled
     */
    protected synchronized Document generateSvg(final boolean includeComments) throws HmxException {
        this.commentsIncluded = includeComments;
        this.locale = Option.TRANSLATION.getValueAsLocale();
        final Document xml = this.createSvgDocument(HmxMessage.EXPORT_CONTENT_SEMANTICAL.get());
        final Element root = xml.getDocumentElement();
        if (includeComments && this.model.getComment() != null && !this.model.getComment().isEmpty()) {
            root.appendChild(xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_DESCRIPTION))
                    .setTextContent(this.model.getComment());
        }
        this.commentCounter = 0;
        final List<Proposition> flatText = this.model.getFlatText();
        final List<Relation> flatRelations = this.model.getFlatRelations();
        // first: calculate all needed size and position constraints
        this.prepareConstraints(flatText, flatRelations);
        // second: insert definitions for arrow, colors and fonts
        final Element defsElement = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_DEFINITIONS);
        for (final Element singleArrowDef : this.createArrowDefinition(xml, this.arrowHeight)) {
            defsElement.appendChild(singleArrowDef);
        }
        root.appendChild(defsElement);
        // third: now it is time for the graphics
        final Element contentGroup = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_GROUP);
        // begin with the propositions
        final int propositionCount = flatText.size();
        final List<Element> propositionElements = new ArrayList<Element>(propositionCount);
        // create the single propositions
        for (int i = 0; i < propositionCount; i++) {
            propositionElements.add(this.createSemanticalProposition(xml, flatText, i));
        }
        // what horizontal extent do we have to reserve for the relation tree
        double relationTreeWidth = 0;
        for (final double singleColumnWidth : this.semColumnWidths) {
            relationTreeWidth += singleColumnWidth;
        }
        // calculate the horizontal indentation for propositions
        final String translateX;
        if (this.model.isLeftToRightOriented()) {
            translateX = this.numberToString(relationTreeWidth + SvgConstants.BORDER_PROPOSITION / 2.);
        } else {
            translateX = this.numberToString(this.propositionExtentX - SvgConstants.BORDER_PROPOSITION / 2.);
        }
        // set the propositions locations
        for (int i = 0; i < propositionElements.size(); i++) {
            final String translateY =
                    this.numberToString(SvgConstants.BORDER_PROPOSITION / 2. + i * (this.propositionHeight + this.verticalSpacing / 2));
            propositionElements.get(i).setAttribute(SvgConstants.ATT_GROUP_TRANSFORM,
                    String.format(SvgConstants.VAL_GROUP_TRANSFORM_TRANSLATE_2, translateX, translateY));
            contentGroup.appendChild(propositionElements.get(i));
        }
        // insert propositions in main graphics element, continue with the relations
        final String translateRelations;
        if (this.model.isLeftToRightOriented()) {
            translateRelations = null;
        } else {
            translateRelations =
                    String.format(SvgConstants.VAL_GROUP_TRANSFORM_TRANSLATE_1, this.numberToString(this.propositionExtentX + relationTreeWidth));
        }

        for (final Relation singleRelation : flatRelations) {
            final Element relationElement = this.createRelation(xml, singleRelation, flatText);
            if (translateRelations != null) {
                relationElement.setAttribute(SvgConstants.ATT_GROUP_TRANSFORM, translateRelations);
            }
            contentGroup.appendChild(relationElement);
        }
        root.appendChild(contentGroup);
        // finally apply the calculated size to the created svg document
        root.setAttribute(SvgConstants.ATT_WIDTH,
                this.numberToString(relationTreeWidth + this.propositionExtentX + SvgConstants.BORDER_PROPOSITION));
        root.setAttribute(
                SvgConstants.ATT_HEIGHT,
                this.numberToString(propositionCount * this.propositionHeight + (propositionCount - 1) * this.verticalSpacing / 2
                        + SvgConstants.BORDER_PROPOSITION));
        return xml;
    }

    /**
     * Calculate horizontal and vertical spacings to be applied to the whole generated svg document.
     * 
     * @param flatPropositions
     *            all {@link Proposition}s to be contained in the generated svg document
     * @param flatRelations
     *            all {@link Relation}s to be contained in the generated svg document
     */
    private void prepareConstraints(final List<Proposition> flatPropositions, final List<Relation> flatRelations) {
        final List<String> texts = this.collectTexts(flatPropositions, flatRelations);
        // height of origin text parts: texts[0] Propositions origin texts separated by spaces
        this.originTextBaseLine = this.getTextBounds(texts.get(0), this.model.getFont()).getHeight();
        this.labelWidth = 0;
        // texts[1] Proposition labels separated by line breaks
        if (!texts.get(1).isEmpty()) {
            for (final String singleLabel : texts.get(1).split("[\n]")) {
                // maximum width of the proposition labels
                this.labelWidth = Math.max(this.labelWidth, this.getTextBounds(singleLabel, this.labelFontPlain).getWidth());
            }
            // regard height of the proposition labels
            this.originTextBaseLine =
                    Math.max(this.originTextBaseLine, this.getTextBounds(texts.get(1).replace('\n', ' '), this.labelFontPlain).getHeight());
        }
        // texts[2] semantical translations separated by spaces
        this.translationHeight = texts.get(2).isEmpty() ? 0 : this.getTextBounds(texts.get(2), this.labelFontPlain).getHeight();
        this.originTextBaseLine += this.verticalSpacing;
        this.propositionHeight = 2 * SvgConstants.BORDER_PROPOSITION + this.originTextBaseLine;
        if (this.translationHeight > 0) {
            this.propositionHeight += this.verticalSpacing + this.translationHeight;
        }
        this.arrowHeight = this.originTextBaseLine * SemanticalSvgCreator.ARROW_SCALE;
        this.arrowWidth = this.calculateArrowWidth(this.arrowHeight);
        // height needed for the relation labels
        this.roleHeight = 0;
        // maximum width of each column created by the relation tree
        this.semColumnWidths = new ArrayList<Double>(texts.size() - 3);
        // texts[3..n] semantical roles separated by line breaks
        for (final String rolesInThisColumn : texts.subList(3, texts.size())) {
            this.roleHeight = Math.max(this.roleHeight, this.getTextBounds(rolesInThisColumn.replace('\n', ' '), this.labelFontPlain).getHeight());
            double columnWidth = 0;
            for (final String singleRole : rolesInThisColumn.split("[\n]")) {
                columnWidth = Math.max(columnWidth, this.getTextBounds(singleRole, this.labelFontPlain).getWidth());
            }
            /*
             * space contained in a single semantical column: spacing + stroke + spacing + role + spacing
             */
            columnWidth += 3 * this.horizontalSpacing + this.relationStroke;
            this.semColumnWidths.add(columnWidth);
        }
        this.propositionHeight = Math.max(this.propositionHeight, this.roleHeight * 2 + this.relationStroke + 2 * this.verticalSpacing);
        this.propositionExtentX = 0;
    }

    /**
     * Collects all texts to display of the {@link Pericope} in an array.
     *
     * @param flatPropositions
     *            {@link Proposition}s in the origin order disregarding indentations and splittings (indicated by arrows)
     * @param flatRelations
     *            {@link Relation}s in the order they are to be regarded by commentary indices
     * @return collected text fragments
     *         <ol start=0>
     *         <li>{@link Proposition}s origin texts separated by spaces</li>
     *         <li>{@link Proposition} labels separated by line breaks {@code [\n]}</li>
     *         <li>{@link Proposition}s semantical translations separated by spaces</li>
     *         <li>{@link Proposition} roles separated by line breaks {@code [\n]}</li>
     *         <li>{@link Relation} roles separated by line breaks {@code [\n]}</li>
     *         ... separate array for each semantical column created by the relation tree
     *         </ol>
     */
    private List<String> collectTexts(final List<Proposition> flatPropositions, final List<Relation> flatRelations) {
        final StringBuilder originTexts = new StringBuilder();
        final StringBuilder labels = new StringBuilder();
        final StringBuilder translations = new StringBuilder();
        int maxTreeDepth = 0;
        for (final Relation singleRelation : flatRelations) {
            maxTreeDepth = Math.max(maxTreeDepth, singleRelation.getTreeDepth());
        }
        // check each proposition in the syntactical model
        for (final Proposition singleProposition : flatPropositions) {
            labels.append(singleProposition.getLabel());
            labels.append('\n');
            // collect all origin text parts
            for (final ClauseItem singleItem : singleProposition) {
                originTexts.append(singleItem.getOriginText());
                originTexts.append(' ');
            }
            translations.append(singleProposition.getSemTranslation());
            translations.append(' ');
        }
        final List<String> cachedTexts = new ArrayList<String>(3 + maxTreeDepth);
        cachedTexts.add(originTexts.toString().trim());
        cachedTexts.add(labels.toString().trim());
        cachedTexts.add(translations.toString().trim());
        final List<StringBuilder> rolesInColumns = new ArrayList<StringBuilder>(maxTreeDepth);
        for (int i = 0; i < maxTreeDepth; i++) {
            rolesInColumns.add(new StringBuilder());
        }
        // don´t forget those relations
        for (final Relation singleRelation : flatRelations) {
            final int columnIndex = singleRelation.getTreeDepth() - 1;
            for (final AbstractConnectable singleAssociate : singleRelation) {
                String role = singleAssociate.getRole().getRole();
                // high weight roles are always presented in capital letters
                if (singleAssociate.getRole().isHighWeight()) {
                    role = role.toUpperCase(this.locale);
                }
                rolesInColumns.get(columnIndex).append(role).append('\n');
            }
        }
        for (final StringBuilder singleRoleColumn : rolesInColumns) {
            cachedTexts.add(singleRoleColumn.toString().trim());
        }
        return cachedTexts;
    }

    /**
     * Create the svg representation of the {@link Proposition} at the given {@code targetIndex}.<br>
     * WARNING: all non-origin-text parts are assumed to be left-to-right oriented. In order to provide a dynamic positioning one must explicitly
     * identify the orientation of the user language.
     *
     * @param xml
     *            the designated svg document this element is created for
     * @param flatPropositions
     *            {@link Proposition}s in the origin order disregarding indentations and splittings (indicated by arrows)
     * @param targetIndex
     *            index of the {@link Proposition} to insert here
     * @return created SVG-Element representing the {@link Proposition} on the given index in the provided list
     */
    protected Element createSemanticalProposition(final Document xml, final List<Proposition> flatPropositions, final int targetIndex) {
        final Proposition target = flatPropositions.get(targetIndex);
        final Element groupElement = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_GROUP);
        // start width calculation including label and spacings left
        double currentExtentX = this.horizontalSpacing + this.labelWidth;
        // calculate the horizontal start position
        if (this.labelWidth > 0) {
            currentExtentX += this.horizontalSpacing;
        }
        final String indentTranslate;
        if (this.model.isLeftToRightOriented()) {
            indentTranslate = this.numberToString(currentExtentX);
        } else {
            indentTranslate = this.numberToString(-currentExtentX);
        }
        final Element indentedContent = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_GROUP);
        indentedContent.setAttribute(SvgConstants.ATT_GROUP_TRANSFORM,
                String.format(SvgConstants.VAL_GROUP_TRANSFORM_TRANSLATE_1, indentTranslate));
        // insert clause items including origin text, functions, comments
        final double originTextExtent = this.insertOriginText(xml, indentedContent, flatPropositions, targetIndex);
        // insert translation
        final String translationText = target.getSemTranslation();
        double translationTextExtent;
        if (translationText == null || translationText.isEmpty()) {
            translationTextExtent = 0;
        } else {
            translationTextExtent = this.getTextBounds(translationText, this.labelFontPlain).getWidth();
        }
        final Element translationElement = this.createTranslationElement(xml, translationText, translationTextExtent, this.propositionHeight);
        if (this.commentsIncluded) {
            for (final ClauseItem singleItem : target) {
                if (singleItem.getComment() != null && !singleItem.getComment().isEmpty()) {
                    this.commentCounter++;
                }
            }
            if (target.getComment() != null && !target.getComment().isEmpty()) {
                // insert proposition comment
                final Element commentElement = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_DESCRIPTION);
                commentElement.setTextContent(target.getComment());
                groupElement.appendChild(commentElement);
                // display numeric identifier
                this.commentCounter++;
                final String counterString = ' ' + this.numberToString(this.commentCounter);
                final RectangularShape counterBounds = this.getTextBounds(counterString, this.commentFont);
                final Element counterElement = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_TSPAN);
                counterElement.setAttribute(SvgConstants.ATT_TEXT_BASELINE_SHIFT,
                        this.numberToString(this.translationHeight - counterBounds.getHeight()));
                counterElement.setAttribute(SvgConstants.ATT_FONT_SIZE, this.numberToString(this.commentFont.getSize2D()));
                counterElement.setTextContent(counterString);
                translationElement.appendChild(counterElement);
                translationTextExtent += counterBounds.getWidth();
            }
        }
        indentedContent.appendChild(translationElement);
        currentExtentX += Math.max(originTextExtent, translationTextExtent) + this.horizontalSpacing;
        this.propositionExtentX = Math.max(this.propositionExtentX, currentExtentX + this.horizontalSpacing + SvgConstants.BORDER_PROPOSITION);
        // first insert border (in background)
        groupElement.appendChild(this.createPropositionBackground(xml, currentExtentX, this.propositionHeight));
        // insert label
        groupElement.appendChild(this.createLabelElement(xml, target.getLabel(), this.labelWidth, this.originTextBaseLine));
        // insert origin text, functions, arrows and translation
        groupElement.appendChild(indentedContent);
        return groupElement;
    }

    /**
     * Insert the svg representation for the {@link Proposition}'s origin text (and potential arrows connecting parts with enclosed children) at the
     * given {@code index}.
     * 
     * @param xml
     *            the designated svg document the {@code parent} element belongs to
     * @param parent
     *            parent element in the svg document to subordinate all clause items
     * @param flatPropositions
     *            {@link Proposition}s in the origin order disregarding indentations and splittings (indicated by arrows)
     * @param index
     *            index of the proposition containing the origin text to insert
     * @return needed horizontal space of the
     */
    private double insertOriginText(final Document xml, final Element parent, final List<Proposition> flatPropositions, final int index) {
        final Proposition target = flatPropositions.get(index);
        // insert upward arrows if needed
        double arrowsExtent = 0;
        if (target.getPartBeforeArrow() != null) {
            for (int i = CollectionUtil.indexOfInstance(flatPropositions, target.getPartBeforeArrow()) + 1; i < index; i++) {
                parent.appendChild(this.createArrowElement(xml, SvgConstants.VAL_ID_ARROW_UPWARD, this.model.isLeftToRightOriented()
                        ? arrowsExtent : (-arrowsExtent - this.arrowWidth), this.arrowHeight, SemanticalSvgCreator.ARROW_SCALE));
                arrowsExtent += this.arrowWidth;
            }
            arrowsExtent += this.horizontalSpacing / 2;
        }
        final StringBuilder originTextCollector = new StringBuilder();
        for (final ClauseItem singleItem : target) {
            if (originTextCollector.length() > 0) {
                originTextCollector.append(' ');
            }
            originTextCollector.append(singleItem.getOriginText());
        }
        final String originText = originTextCollector.toString();
        final RectangularShape originTextSize = this.getTextBounds(originText, this.model.getFont());
        double horizontalExtent = originTextSize.getWidth();
        final Element originTextElement =
                this.createTextElement(xml, originText, this.model.getFont(), this.numberToString(0), this
                        .numberToString((this.verticalSpacing + this.originTextBaseLine) / 2), this.model.isLeftToRightOriented()
                        ? SvgConstants.VAL_TEXT_ANCHOR_START : SvgConstants.VAL_TEXT_ANCHOR_END, this.colorOriginText);
        if (arrowsExtent > 0) {
            originTextElement.setAttribute(
                    SvgConstants.ATT_GROUP_TRANSFORM,
                    String.format(SvgConstants.VAL_GROUP_TRANSFORM_TRANSLATE_1,
                            this.numberToString(this.model.isLeftToRightOriented() ? arrowsExtent : -arrowsExtent)));
            horizontalExtent += arrowsExtent;
        }
        parent.appendChild(originTextElement);
        if (target.getPartAfterArrow() != null) {
            horizontalExtent += this.horizontalSpacing / 2;
            for (int i = CollectionUtil.indexOfInstance(flatPropositions, target.getPartAfterArrow()) - 1; i > index; i--) {
                parent.appendChild(this.createArrowElement(xml, SvgConstants.VAL_ID_ARROW_DOWNWARD, this.model.isLeftToRightOriented()
                        ? horizontalExtent : -(horizontalExtent + this.arrowWidth), this.arrowHeight, SemanticalSvgCreator.ARROW_SCALE));
                horizontalExtent += this.arrowWidth;
            }
        }
        return horizontalExtent;
    }

    /**
     * Create the svg representation for the given {@link Relation}.
     * 
     * @param xml
     *            designated svg document this element is created for
     * @param target
     *            the {@link Relation} to represent as a svg element
     * @param flatPropositions
     *            all {@link Proposition} contained in the semantical analysis, to determine the vertical position of the {@link Relation} in the
     *            document
     * @return the created svg element
     */
    protected Element createRelation(final Document xml, final Relation target, final List<Proposition> flatPropositions) {
        final Element groupElement = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_GROUP);
        final double indentX = this.calculateConnectX(target);
        String roleIndentX = this.numberToString(indentX + this.relationStroke + this.horizontalSpacing / 2);
        String strokeIndentX = this.numberToString(indentX + this.relationStroke / 2);
        if (!this.model.isLeftToRightOriented()) {
            roleIndentX = '-' + roleIndentX;
            strokeIndentX = '-' + strokeIndentX;
        }
        // add a count on the end, if there are equal roles
        final List<AbstractConnectable> associates = target.getAssociates();
        final List<AssociateRole> roles = new ArrayList<AssociateRole>(associates.size());
        for (final AbstractConnectable singleAssociate : associates) {
            roles.add(singleAssociate.getRole());
        }
        final Map<AssociateRole, AtomicInteger> occurrences = CollectionUtil.countOccurrences(roles);
        final Map<AssociateRole, Integer> indices = new HashMap<AssociateRole, Integer>();
        boolean insertComment = this.commentsIncluded && target.getComment() != null && !target.getComment().isEmpty();
        final StringBuffer bufferedPoints = new StringBuffer();
        for (final AbstractConnectable singleAssociate : associates) {
            final double positionY = this.calculateConnectY(singleAssociate, flatPropositions);
            final String connectY = this.numberToString(positionY);
            final String mainPoint = String.format(SvgConstants.VAL_POLYLINE_POINTS_2, strokeIndentX, connectY);
            bufferedPoints.append(mainPoint);
            String connectX = this.numberToString(this.calculateConnectX(singleAssociate));
            if (!this.model.isLeftToRightOriented()) {
                connectX = '-' + connectX;
            }
            bufferedPoints.append(String.format(SvgConstants.VAL_POLYLINE_POINTS_2, connectX, connectY));
            bufferedPoints.append(mainPoint);
            String role = singleAssociate.getRole().getRole();
            if (singleAssociate.getRole().isHighWeight()) {
                role = role.toUpperCase(this.locale);
            }
            if (occurrences.get(singleAssociate.getRole()).intValue() > 1) {
                Integer index = indices.get(singleAssociate.getRole());
                if (index == null) {
                    index = 1;
                } else {
                    index += 1;
                }
                role += index.intValue();
                indices.put(singleAssociate.getRole(), index);
            }
            final Element roleElement =
                    this.createTextElement(xml, role, this.labelFontPlain, roleIndentX, this.numberToString(positionY - this.verticalSpacing),
                            this.model.isLeftToRightOriented() ? SvgConstants.VAL_TEXT_ANCHOR_START : SvgConstants.VAL_TEXT_ANCHOR_END,
                            this.colorSemRole);
            groupElement.appendChild(roleElement);
            if (insertComment) {
                // insert relation comment
                groupElement.appendChild(xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_DESCRIPTION)).setTextContent(
                        target.getComment());
                // display numeric identifier
                this.commentCounter++;
                final String counterString = ' ' + this.numberToString(this.commentCounter);
                final double baselineShift = this.roleHeight - this.getTextBounds(counterString, this.commentFont).getHeight();
                final Element counterElement = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_TSPAN);
                counterElement.setAttribute(SvgConstants.ATT_TEXT_BASELINE_SHIFT, this.numberToString(baselineShift));
                counterElement.setAttribute(SvgConstants.ATT_FONT_SIZE, this.numberToString(this.commentFont.getSize2D()));
                counterElement.setTextContent(counterString);
                roleElement.appendChild(counterElement);
                insertComment = false;
            }
        }
        final Element lineElement = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_POLYLINE);
        lineElement.setAttribute(SvgConstants.ATT_FILL, SvgConstants.VAL_FILL_RELATION);
        lineElement.setAttribute(SvgConstants.ATT_STROKE, this.colorRelation);
        lineElement.setAttribute(SvgConstants.ATT_STROKE_LINECAP, SvgConstants.VAL_STROKE_LINECAP_RELATION);
        lineElement.setAttribute(SvgConstants.ATT_STROKE_WIDTH, this.numberToString(this.relationStroke));
        lineElement.setAttribute(SvgConstants.ATT_POLYLINE_POINTS, bufferedPoints.toString());
        groupElement.appendChild(lineElement);
        return groupElement;
    }

    /**
     * Determine the X coordinate where to connect a super ordinated {@link Relation} to the given {@code target} element.
     * 
     * @param target
     *            the associate in a super ordinated {@link Relation} to connect with
     * @return the X coordinate where to connect a super ordinated {@link Relation}
     */
    private double calculateConnectX(final AbstractConnectable target) {
        double indentX;
        final int columnIndex;
        if (target instanceof Relation) {
            indentX = this.horizontalSpacing;
            columnIndex = ((Relation) target).getTreeDepth();
        } else {
            indentX = 0;
            columnIndex = 0;
        }
        for (final Double singleColumnWidth : this.semColumnWidths.subList(columnIndex, this.semColumnWidths.size())) {
            indentX += singleColumnWidth.doubleValue();
        }
        return indentX;
    }

    /**
     * Determine the Y coordinate where to connect a super ordinated {@link Relation} to the given {@code target} element.
     * 
     * @param target
     *            the associate in a super ordinated {@link Relation} to connect with (either a {@link Relation} or {@link Proposition})
     * @param flatPropositions
     *            all {@link Proposition}s, representing the lowest elements in the {@link Relation} hierarchy
     * @return the Y coordinate where to connect a super ordinated {@link Relation}
     */
    private double calculateConnectY(final AbstractConnectable target, final List<Proposition> flatPropositions) {
        if (target instanceof Proposition) {
            return this.propositionHeight / 2 + CollectionUtil.indexOfInstance(flatPropositions, target)
                    * (this.propositionHeight + this.verticalSpacing / 2);
        }
        final List<AbstractConnectable> associates = ((Relation) target).getAssociates();
        AbstractConnectable highWeightAssociate = null;
        for (final AbstractConnectable singleAssociate : associates) {
            if (singleAssociate.getRole().isHighWeight()) {
                if (highWeightAssociate == null) {
                    highWeightAssociate = singleAssociate;
                } else {
                    highWeightAssociate = null;
                    break;
                }
            }
        }
        if (highWeightAssociate == null) {
            final double firstAssociateY = this.calculateConnectY(associates.get(0), flatPropositions);
            final double lastAssociateY = this.calculateConnectY(associates.get(associates.size() - 1), flatPropositions);
            return (firstAssociateY + lastAssociateY) / 2;
        }
        return this.calculateConnectY(highWeightAssociate, flatPropositions);
    }
}
