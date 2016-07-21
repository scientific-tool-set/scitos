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

import java.awt.Font;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.domain.util.CollectionUtil;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.core.option.HmxExportOption;
import org.hmx.scitos.hmx.domain.IPropositionParent;
import org.hmx.scitos.hmx.domain.model.ClauseItem;
import org.hmx.scitos.hmx.domain.model.ClauseItem.Style;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.domain.model.SyntacticalFunction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of the svg export functionality for the syntactical analysis contained in a {@link Pericope}.
 */
class SyntacticalSvgCreator extends AbstractSvgCreator {

    private static final double ARROW_SCALE = .6;

    /**
     * The font to be applied to {@link Proposition} indentation functions.
     */
    private Font indentFunctionFont;
    /** If comments should be indicated by a numeric identifier on the commented element. */
    private boolean commentsIncluded;
    /**
     * The running counter for the numeric identifier on commented elements, if the {@code commentsIncluded} is set to {@code true}.
     */
    private int commentCounter;
    /**
     * The overall maximum width of a {@link Proposition}, in order to determine the resulting size of the generated svg document.
     */
    private double extentX;
    /**
     * The horizontal space reserved for {@link Proposition} labels.
     */
    private double labelWidth;
    /**
     * The fixed horizontal spacing inserted for a {@link Proposition}'s single indentation under another one.
     */
    private final double indentationWidth;
    /**
     * The vertical space reserved for a single {@link Proposition}.
     */
    private double propositionHeight;
    /**
     * The baseline (y coordinate) of the label and origin text in a {@link Proposition}.
     */
    private double originTextBaseLine;
    /**
     * The vertical space reserved for the {@link ClauseItem} functions.
     */
    private double functionHeight;
    /**
     * The vertical space reserved for a {@link Proposition}'s translation.
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
     * The horizontal space occupied by a {@code partBeforeArrow} of a {@link Proposition} with an enclosed children, to indent its
     * {@code partAfterArrow} appropriately.
     */
    private Map<Integer, Double> arrowPartIndentation;

    /**
     * Font color to be applied to {@link Proposition} and {@link ClauseItem} functions without specific styling.
     */
    private final String colorPlainFunctionText;
    /**
     * Font color to be applied to {@link ClauseItem} functions with {@code bold} styling.
     */
    private final String colorBoldFunctionText;
    /**
     * Font color to be applied to {@link ClauseItem} functions with combined {@code bold} and {@code italic} styling.
     */
    private final String colorBoldItalicFunctionText;
    /**
     * Font color to be applied to {@link ClauseItem} functions with {@code italic} styling.
     */
    private final String colorItalicFunctionText;

    /**
     * Constructor.
     * 
     * @param model
     *            the model containing the syntactical analysis to be exported into a svg document
     */
    protected SyntacticalSvgCreator(final Pericope model) {
        super(model);
        // ensure minimum indentation space for including function labels
        this.indentationWidth = this.getTextBounds("WWWW", this.labelFontPlain).getWidth();
        this.colorPlainFunctionText = HmxExportOption.FONTCOLOR_SYNFUNCTION_PLAIN.getValue();
        this.colorBoldFunctionText = HmxExportOption.FONTCOLOR_SYNFUNCTION_BOLD.getValue();
        this.colorBoldItalicFunctionText = HmxExportOption.FONTCOLOR_SYNFUNCTION_BOLDTALIC.getValue();
        this.colorItalicFunctionText = HmxExportOption.FONTCOLOR_SYNFUNCTION_ITALIC.getValue();
    }

    /**
     * Generate an svg document representing the syntactical analysis.
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
        final Document xml = this.createSvgDocument(HmxMessage.EXPORT_CONTENT_SYNTACTICAL.get());
        final Element root = xml.getDocumentElement();
        if (includeComments && this.model.getComment() != null && !this.model.getComment().isEmpty()) {
            root.appendChild(xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_DESCRIPTION))
                    .setTextContent(this.model.getComment());
        }
        final List<Proposition> flatText = this.model.getFlatText();
        this.prepareConstraints(flatText);
        final Element defsElement = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_DEFINITIONS);
        for (final Element singleArrowDef : this.createArrowDefinition(xml, this.arrowHeight)) {
            defsElement.appendChild(singleArrowDef);
        }
        root.appendChild(defsElement);
        final Element contentGroup = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_GROUP);
        final int propositionCount = flatText.size();
        this.arrowPartIndentation = new HashMap<Integer, Double>();
        final List<Element> propositionElements = new ArrayList<Element>(propositionCount);
        for (int i = 0; i < propositionCount; i++) {
            propositionElements.add(this.createSyntacticalProposition(xml, flatText, i));
        }
        final String translateX;
        if (this.model.isLeftToRightOriented()) {
            translateX = this.numberToString(SvgConstants.BORDER_PROPOSITION / 2.);
        } else {
            translateX = this.numberToString(this.extentX - SvgConstants.BORDER_PROPOSITION / 2.);
        }
        for (int i = 0; i < propositionElements.size(); i++) {
            final String translateY =
                    this.numberToString(SvgConstants.BORDER_PROPOSITION / 2. + i * (this.propositionHeight + this.verticalSpacing / 2));
            propositionElements.get(i).setAttribute(SvgConstants.ATT_GROUP_TRANSFORM,
                    String.format(SvgConstants.VAL_GROUP_TRANSFORM_TRANSLATE_2, translateX, translateY));
            contentGroup.appendChild(propositionElements.get(i));
        }
        root.appendChild(contentGroup);
        root.setAttribute(SvgConstants.ATT_WIDTH, this.numberToString(this.extentX + SvgConstants.BORDER_PROPOSITION));
        root.setAttribute(
                SvgConstants.ATT_HEIGHT,
                this.numberToString(propositionCount * this.propositionHeight + (propositionCount - 1) * this.verticalSpacing / 2
                        + SvgConstants.BORDER_PROPOSITION));
        // allow garbage collector to clean up
        this.arrowPartIndentation = null;
        return xml;
    }

    /**
     * Calculate horizontal and vertical spacings to be applied to the whole generated svg document.
     * 
     * @param flatPropositions
     *            {@link Proposition}s in the origin order disregarding indentations and splittings (indicated by arrows)
     */
    private void prepareConstraints(final List<Proposition> flatPropositions) {
        this.commentCounter = 0;
        final List<String> texts = this.collectTexts(flatPropositions);
        // texts.get(0) Proposition labels separated by line breaks
        this.labelWidth = 0;
        if (!texts.get(0).isEmpty()) {
            for (final String singleLabel : texts.get(0).split("[\n]")) {
                this.labelWidth = Math.max(this.labelWidth, this.getTextBounds(singleLabel, this.labelFontPlain).getWidth());
            }
        }
        // texts.get(1) ClauseItems origin text parts
        this.originTextBaseLine = this.getTextBounds(texts.get(1), this.model.getFont()).getHeight();
        if (!texts.get(0).isEmpty()) {
            this.originTextBaseLine =
                    Math.max(this.originTextBaseLine, this.getTextBounds(texts.get(0).replaceAll("[\n]", " "), this.labelFontPlain).getHeight());
        }
        // texts.get(2) Propositions syntactical translations
        this.translationHeight = texts.get(2).isEmpty() ? 0 : this.getTextBounds(texts.get(2), this.labelFontPlain).getHeight();
        // texts.get(3) ClauseItem functions with the PLAIN style
        if (texts.get(3).isEmpty()) {
            this.functionHeight = 0;
        } else {
            this.functionHeight = this.getTextBounds(texts.get(3), this.labelFontPlain).getHeight();
        }
        // texts.get(4) ClauseItem functions with the BOLD style
        if (!texts.get(4).isEmpty()) {
            this.functionHeight = Math.max(this.functionHeight, this.getTextBounds(texts.get(4), this.labelFontBold).getHeight());
        }
        // texts.get(5) ClauseItem functions with the ITALIC style
        if (!texts.get(5).isEmpty()) {
            this.functionHeight = Math.max(this.functionHeight, this.getTextBounds(texts.get(5), this.labelFontItalic).getHeight());
        }
        // texts.get(6) ClauseItem functions with the combined BOLD | ITALIC style
        if (!texts.get(6).isEmpty()) {
            this.functionHeight = Math.max(this.functionHeight, this.getTextBounds(texts.get(6), this.labelFontBoldItalic).getHeight());
        }
        this.originTextBaseLine += this.verticalSpacing;
        this.propositionHeight = 2 * SvgConstants.BORDER_PROPOSITION + this.originTextBaseLine;
        if (this.functionHeight > 0) {
            this.propositionHeight += this.verticalSpacing + this.functionHeight;
        }
        if (this.translationHeight > 0) {
            this.propositionHeight += this.verticalSpacing + this.translationHeight;
        }
        this.arrowHeight = (this.originTextBaseLine + this.functionHeight) * SyntacticalSvgCreator.ARROW_SCALE;
        this.arrowWidth = this.calculateArrowWidth(this.arrowHeight);
        // texts.get(7) Proposition indentation functions with the PLAIN style
        if (!texts.get(7).isEmpty()) {
            int maxFunctionLength = 0;
            for (final String singleFunction : texts.get(7).split("[ ]")) {
                maxFunctionLength = Math.max(maxFunctionLength, singleFunction.length());
            }
            // how much vertical space to we NEED for indentation functions
            final double neededSpace = maxFunctionLength * this.getTextBounds(texts.get(7), this.labelFontPlain).getHeight();
            // how much vertical space do we GOT for indentation functions
            final double functionAreaHeight = this.propositionHeight - 2 * SvgConstants.BORDER_PROPOSITION - this.verticalSpacing;
            if (neededSpace > functionAreaHeight) {
                // we need too much; scale the Font size for the vertical text down
                this.indentFunctionFont =
                        this.labelFontPlain.deriveFont((float) Math.floor(this.labelFontPlain.getSize2D() * functionAreaHeight / neededSpace));
            } else {
                this.indentFunctionFont = this.labelFontPlain;
            }
        }
        this.extentX = 0;
    }

    /**
     * Collect all texts to display in the syntactical analysis.
     *
     * @param flatPropositions
     *            {@link Proposition}s in the origin order disregarding indentations and splittings (indicated by arrows)
     * @return collected text fragments
     *         <ol start=0>
     *         <li>{@link Proposition} labels separated by line breaks {@code [\n]}</li>
     *         <li>{@link ClauseItem}s origin text parts separated by single spaces</li>
     *         <li>{@link Proposition}s syntactical translations separated by single spaces</li>
     *         <li>{@link ClauseItem} functions with the {@link Style#PLAIN} style separated by single spaces</li>
     *         <li>{@link ClauseItem} functions with the {@link Style#BOLD} style separated by single spaces</li>
     *         <li>{@link ClauseItem} functions with the {@link Style#ITALIC} style separated by single spaces</li>
     *         <li>{@link ClauseItem} functions with the {@link Style#BOLD_ITALIC} style separated by single spaces</li>
     *         <li>{@link Proposition} indentation functions separated by single spaces</li>
     *         </ol>
     */
    private List<String> collectTexts(final List<Proposition> flatPropositions) {
        final StringBuilder labels = new StringBuilder();
        final StringBuilder originTexts = new StringBuilder();
        final StringBuilder translations = new StringBuilder();
        final StringBuilder functionsPlain = new StringBuilder();
        final StringBuilder functionsBold = new StringBuilder();
        final StringBuilder functionsItalic = new StringBuilder();
        final StringBuilder functionsBoldItalic = new StringBuilder();
        final StringBuilder indentFunctions = new StringBuilder();
        // check each proposition in the syntactical model
        for (final Proposition singleProposition : flatPropositions) {
            labels.append(singleProposition.getLabel());
            labels.append('\n');
            translations.append(singleProposition.getSynTranslation());
            translations.append(' ');
            // don´t forget the syntactical indentation function
            if (singleProposition.getParent() instanceof Proposition && singleProposition.getFunction() != null) {
                indentFunctions.append(singleProposition.getFunction().getCode()).append(' ');
            }
            // and now check each clause item
            for (final ClauseItem singleItem : singleProposition) {
                originTexts.append(singleItem.getOriginText()).append(' ');
                if (singleItem.getFunction() == null) {
                    continue;
                }
                /*
                 * in the syntactical analysis we have to display the functions of the single items (in different styles)
                 */
                final StringBuilder functionCollector;
                switch (singleItem.getFontStyle()) {
                case BOLD:
                    functionCollector = functionsBold;
                    break;
                case ITALIC:
                    functionCollector = functionsItalic;
                    break;
                case BOLD_ITALIC:
                    functionCollector = functionsBoldItalic;
                    break;
                default:
                    functionCollector = functionsPlain;
                }
                functionCollector.append(singleItem.getFunction().getCode()).append(' ');
            }
        }

        return Arrays.asList(labels.toString().trim(), originTexts.toString().trim(), translations.toString().trim(), functionsPlain.toString()
                .trim(), functionsBold.toString().trim(), functionsItalic.toString().trim(), functionsBoldItalic.toString().trim(),
                indentFunctions.toString().trim());
    }

    /**
     * Create the svg element representing the {@link Proposition} at the given {@code targetIndex}.<br>
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
    protected Element createSyntacticalProposition(final Document xml, final List<Proposition> flatPropositions, final int targetIndex) {
        final Element groupElement = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_GROUP);
        double currentExtentX = this.calculateSyntacticalPropositionIndentation(flatPropositions, targetIndex);
        final String indentTranslate;
        if (this.model.isLeftToRightOriented()) {
            indentTranslate = this.numberToString(currentExtentX);
        } else {
            indentTranslate = this.numberToString(-currentExtentX);
        }
        final Proposition target = flatPropositions.get(targetIndex);
        final Element indentedContent = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_GROUP);
        indentedContent.setAttribute(SvgConstants.ATT_GROUP_TRANSFORM,
                String.format(SvgConstants.VAL_GROUP_TRANSFORM_TRANSLATE_1, indentTranslate));
        if (target.getParent() instanceof Proposition && target.getFunction() != null) {
            // insert indentation function (vertically)
            indentedContent.appendChild(this.createIndentationFunctionElement(xml, target.getFunction()));
        }
        // insert upward arrows if needed
        double originTextExtent = 0;
        if (target.getPartBeforeArrow() != null) {
            for (int i = CollectionUtil.indexOfInstance(flatPropositions, target.getPartBeforeArrow()) + 1; i < targetIndex; i++) {
                final double coordX;
                if (this.model.isLeftToRightOriented()) {
                    coordX = originTextExtent;
                    originTextExtent += this.arrowWidth;
                } else {
                    originTextExtent += this.arrowWidth;
                    coordX = -originTextExtent;
                }
                indentedContent.appendChild(this.createArrowElement(xml, SvgConstants.VAL_ID_ARROW_UPWARD, coordX, this.arrowHeight,
                        SyntacticalSvgCreator.ARROW_SCALE));
            }
            originTextExtent += this.horizontalSpacing;
        }
        // insert clause items including origin text, functions, comments
        originTextExtent += this.insertClauseItems(xml, indentedContent, target, originTextExtent);
        if (target.getPartAfterArrow() != null) {
            originTextExtent += this.horizontalSpacing;
            this.arrowPartIndentation.put(targetIndex, currentExtentX + originTextExtent);
            for (int i = CollectionUtil.indexOfInstance(flatPropositions, target.getPartAfterArrow()) - 1; i > targetIndex; i--) {
                final double coordX;
                if (this.model.isLeftToRightOriented()) {
                    coordX = originTextExtent;
                    originTextExtent += this.arrowWidth;
                } else {
                    originTextExtent += this.arrowWidth;
                    coordX = -originTextExtent;
                }
                indentedContent.appendChild(this.createArrowElement(xml, SvgConstants.VAL_ID_ARROW_DOWNWARD, coordX, this.arrowHeight,
                        SyntacticalSvgCreator.ARROW_SCALE));
            }
        }
        // insert translation
        final String translationText = target.getSynTranslation();
        double translationTextExtent;
        if (translationText == null || translationText.isEmpty()) {
            translationTextExtent = 0;
        } else {
            translationTextExtent = this.getTextBounds(translationText, this.labelFontPlain).getWidth();
        }
        final Element translationElement = this.createTranslationElement(xml, translationText, translationTextExtent, this.propositionHeight);
        if (this.commentsIncluded && target.getComment() != null && !target.getComment().isEmpty()) {
            // insert proposition comment
            groupElement.appendChild(xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_DESCRIPTION)).setTextContent(
                    target.getComment());
            // display numeric identifier
            this.commentCounter++;
            final String counterString = ' ' + this.numberToString(this.commentCounter);
            final RectangularShape counterBounds = this.getTextBounds(counterString, this.commentFont);
            final double baselineShift = this.translationHeight - counterBounds.getHeight();
            translationTextExtent += counterBounds.getWidth();
            final Element counterElement = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_TSPAN);
            counterElement.setAttribute(SvgConstants.ATT_TEXT_BASELINE_SHIFT, this.numberToString(baselineShift));
            counterElement.setAttribute(SvgConstants.ATT_FONT_SIZE, this.numberToString(this.commentFont.getSize2D()));
            counterElement.setTextContent(counterString);
            translationElement.appendChild(counterElement);
        }
        indentedContent.appendChild(translationElement);
        currentExtentX += Math.max(originTextExtent, translationTextExtent) + this.horizontalSpacing;
        this.extentX = Math.max(this.extentX, currentExtentX + this.horizontalSpacing + SvgConstants.BORDER_PROPOSITION);
        // first insert border (in background)
        groupElement.appendChild(this.createPropositionBackground(xml, currentExtentX, this.propositionHeight));
        // insert label
        groupElement.appendChild(this.createLabelElement(xml, target.getLabel(), this.labelWidth, this.originTextBaseLine));
        // insert origin text, functions, arrows and translation
        groupElement.appendChild(indentedContent);
        return groupElement;
    }

    /**
     * Determine the horizontal spacing inserted in front of the {@link Proposition}'s contents ({@link ClauseItem}s, translation, potential arrows)
     * at the given {@code targetIndex}.
     * 
     * @param flatPropositions
     *            {@link Proposition}s in the origin order disregarding indentations and splittings (indicated by arrows)
     * @param targetIndex
     *            the index of the {@link Proposition} to calculate the indentation width for
     * @return the horizontal spacing to insert in front of the designated {@link Proposition}'s contents
     */
    private double calculateSyntacticalPropositionIndentation(final List<Proposition> flatPropositions, final int targetIndex) {
        final Proposition target = flatPropositions.get(targetIndex);
        double indentation;
        if (target.getPartBeforeArrow() == null) {
            IPropositionParent loopParent = target.getParent();
            // start width calculation including label and spacings left
            indentation = this.horizontalSpacing + this.labelWidth;
            if (this.labelWidth > 0) {
                indentation += this.horizontalSpacing;
            }
            // multiply with number of indentations
            while (loopParent instanceof Proposition) {
                indentation += this.indentationWidth;
                loopParent = ((Proposition) loopParent).getParent();
            }
        } else {
            // remember from part before arrow
            indentation = this.arrowPartIndentation.get(CollectionUtil.indexOfInstance(flatPropositions, target.getPartBeforeArrow()));
        }
        return indentation;
    }

    /**
     * Create a svg element for the vertical text representing the given indentation function.<br>
     * WARNING: the function label is assumed to be left-to-right oriented. In order to provide a dynamic positioning one must identify the desired
     * text orientation of the function label itself...
     *
     * @param xml
     *            the designated svg document this element is created for
     * @param function
     *            indentation function to be inserted
     * @return created SVG-Element representing the top-to-bottom rendered indentation function
     */
    private Element createIndentationFunctionElement(final Document xml, final SyntacticalFunction function) {
        final String indentFunction = function.getCode();
        final double functionCharHeight = this.getTextBounds(indentFunction, this.indentFunctionFont).getHeight() * 1.2;
        double currentCharY =
                (this.propositionHeight + this.verticalSpacing - functionCharHeight * (indentFunction.length() - 1)) / 2
                        - SvgConstants.BORDER_PROPOSITION;
        double charX = this.indentationWidth / 2;
        if (this.model.isLeftToRightOriented()) {
            charX *= -1;
        }
        final String staticCharX = this.numberToString(charX);
        final StringBuilder coordsX = new StringBuilder();
        final StringBuilder coordsY = new StringBuilder();
        for (int i = 0; i < indentFunction.length(); i++) {
            if (i > 0) {
                coordsX.append(',');
                coordsY.append(',');
            }
            coordsX.append(staticCharX);
            coordsY.append(this.numberToString(currentCharY));
            currentCharY += functionCharHeight;
        }
        return this.createTextElement(xml, indentFunction, this.indentFunctionFont, coordsX.toString(), coordsY.toString(),
                SvgConstants.VAL_TEXT_ANCHOR_MIDDLE, this.colorPlainFunctionText);
    }

    /**
     * Insert the svg representation for the given {@link Proposition}'s {@link ClauseItem}s (origin text and function). WARNING: the function label
     * is assumed to be left-to-right oriented. In order to provide a dynamic positioning one must identify the desired text orientation of the
     * function label itself.
     *
     * @param xml
     *            the designated svg document containing the given {@code parent} element
     * @param parent
     *            parent element to subordinate all {@link ClauseItem}s to
     * @param targetProposition
     *            {@link Proposition} containing the {@link ClauseItem}s to insert
     * @param offsetX
     *            horizontal margin
     * @return needed horizontal space of the inserted {@link ClauseItem}s
     */
    private double insertClauseItems(final Document xml, final Element parent, final Proposition targetProposition, final double offsetX) {
        double widthSum = 0;
        for (final ClauseItem singleItem : targetProposition) {
            final Element groupElement = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_GROUP);
            if (widthSum + offsetX > 0) {
                String translateX = this.numberToString(widthSum + offsetX);
                if (!this.model.isLeftToRightOriented()) {
                    translateX = '-' + translateX;
                }
                groupElement.setAttribute(SvgConstants.ATT_GROUP_TRANSFORM,
                        String.format(SvgConstants.VAL_GROUP_TRANSFORM_TRANSLATE_1, translateX));
            }
            final String functionTextColor;
            final double functionWidth;
            Font functionFont = null;
            if (singleItem.getFunction() == null) {
                functionWidth = 0;
                functionTextColor = null;
            } else {
                switch (singleItem.getFontStyle()) {
                case BOLD:
                    functionTextColor = this.colorBoldFunctionText;
                    functionFont = this.labelFontBold;
                    break;
                case ITALIC:
                    functionTextColor = this.colorItalicFunctionText;
                    functionFont = this.labelFontItalic;
                    break;
                case BOLD_ITALIC:
                    functionTextColor = this.colorBoldItalicFunctionText;
                    functionFont = this.labelFontBoldItalic;
                    break;
                default:
                    functionTextColor = this.colorPlainFunctionText;
                    functionFont = this.labelFontPlain;
                }
                functionWidth = this.getTextBounds(singleItem.getFunction().getCode(), functionFont).getWidth();
            }
            final double originTextWidth = this.getTextBounds(singleItem.getOriginText(), this.model.getFont()).getWidth();
            final double itemWidth = Math.max(originTextWidth, functionWidth);
            String clauseItemMidX = this.numberToString(itemWidth / 2);
            if (!this.model.isLeftToRightOriented()) {
                clauseItemMidX = '-' + clauseItemMidX;
            }
            final Element originTextElement =
                    this.createTextElement(xml, singleItem.getOriginText(), this.model.getFont(), clauseItemMidX,
                            this.numberToString(this.verticalSpacing + this.originTextBaseLine / 2), SvgConstants.VAL_TEXT_ANCHOR_MIDDLE,
                            this.colorOriginText);
            groupElement.appendChild(originTextElement);
            final boolean underline;
            if (singleItem.getFunction() == null) {
                underline = false;
            } else {
                underline = singleItem.getFunction().isUnderlined();
                if (underline) {
                    originTextElement.setAttribute(SvgConstants.ATT_TEXT_DECORATION, SvgConstants.VAL_TEXT_DECORATION_UNDERLINE);
                }
                groupElement.appendChild(this.createTextElement(xml, singleItem.getFunction().getCode(), functionFont, clauseItemMidX,
                        this.numberToString(this.originTextBaseLine + this.verticalSpacing + this.functionHeight / 2),
                        SvgConstants.VAL_TEXT_ANCHOR_MIDDLE,
                        // VAL_DIRECTION_LTR,
                        functionTextColor));
            }
            widthSum += itemWidth;
            if (this.commentsIncluded && singleItem.getComment() != null && !singleItem.getComment().isEmpty()) {
                // insert proposition comment
                final Element commentElement = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_DESCRIPTION);
                commentElement.setTextContent(singleItem.getComment());
                groupElement.appendChild(commentElement);
                // display numeric identifier
                this.commentCounter++;
                final String counterString = ' ' + this.numberToString(this.commentCounter);
                final RectangularShape counterBounds = this.getTextBounds(counterString, this.commentFont);
                final double counterWidth = counterBounds.getWidth() + this.horizontalSpacing * .2;
                final Element counterElement = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_TSPAN);
                counterElement.setAttribute(SvgConstants.ATT_TEXT_BASELINE_SHIFT,
                        this.numberToString(this.originTextBaseLine - this.verticalSpacing - counterBounds.getHeight()));
                counterElement.setAttribute(SvgConstants.ATT_FONT_FAMILY, SvgConstants.VAL_ID_FONT_LABELSPLAIN);
                counterElement.setAttribute(SvgConstants.ATT_FONT_SIZE, this.numberToString(this.commentFont.getSize2D()));
                if (underline) {
                    counterElement.setAttribute(SvgConstants.ATT_TEXT_DECORATION, SvgConstants.VAL_TEXT_DECORATION_NONE);
                }
                counterElement.setTextContent(counterString);
                originTextElement.appendChild(counterElement);
                widthSum += counterWidth;
            }
            widthSum += this.horizontalSpacing;
            parent.appendChild(groupElement);
        }
        return widthSum - this.horizontalSpacing;
    }
}
