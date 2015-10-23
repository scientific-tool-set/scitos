package org.hmx.scitos.hmx.core.export;

import java.awt.Font;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.domain.util.ComparisonUtil;
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

class SyntacticalSvgCreator extends AbstractSvgCreator {

    private static final double ARROW_SCALE = .6;

    private Font indentFunctionFont;
    private boolean commentsIncluded;
    private int commentCounter;
    private double extentX;
    private double labelWidth;
    private final double indentationWidth;
    private double propositionHeight;
    private double originTextBaseLine;
    private double functionHeight;
    private double translationHeight;
    private double arrowHeight;
    private double arrowWidth;
    private Map<Integer, Double> arrowPartIndentation;

    private final String colorPlainFunctionText;
    private final String colorBoldFunctionText;
    private final String colorBoldItalicFunctionText;
    private final String colorItalicFunctionText;

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
     * Executes the svg generation.
     *
     * @param includeComments
     *            if the comments are to be included in the created SVG as slightly smaller numbers and invisible description elements (in some SVG
     *            viewers processed as tool tips)
     * @return generated svg document
     * @throws HmxException
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
     * @param flatPropositions
     *            {@link Proposition}s in the origin order disregarding indentations and splittings (indicated by arrows)
     */
    private void prepareConstraints(final List<Proposition> flatPropositions) {
        this.commentCounter = 0;
        final String[] texts = this.collectTexts(flatPropositions);
        // texts[0] Proposition labels separated by line breaks
        this.labelWidth = 0;
        if (!texts[0].isEmpty()) {
            for (final String singleLabel : texts[0].split("[\n]")) {
                this.labelWidth = Math.max(this.labelWidth, this.getTextBounds(singleLabel, this.labelFontPlain).getWidth());
            }
        }
        this.originTextBaseLine =
                Math.max(texts[0].isEmpty() ? 0 : this.getTextBounds(texts[0].replaceAll("[\n]", " "), this.labelFontPlain).getHeight(),
                // texts[1] ClauseItems origin text parts
                        this.getTextBounds(texts[1], this.model.getFont()).getHeight());
        // texts[2] Propositions syntactical translations
        this.translationHeight = texts[2].isEmpty() ? 0 : this.getTextBounds(texts[2], this.labelFontPlain).getHeight();
        this.functionHeight = 0;
        // texts[3] ClauseItem functions with the PLAIN style
        if (!texts[3].isEmpty()) {
            this.functionHeight = this.getTextBounds(texts[3], this.labelFontPlain).getHeight();
        }
        // texts[4] ClauseItem functions with the BOLD style
        if (!texts[4].isEmpty()) {
            this.functionHeight = Math.max(this.functionHeight, this.getTextBounds(texts[4], this.labelFontBold).getHeight());
        }
        // texts[5] ClauseItem functions with the ITALIC style
        if (!texts[5].isEmpty()) {
            this.functionHeight = Math.max(this.functionHeight, this.getTextBounds(texts[5], this.labelFontItalic).getHeight());
        }
        // texts[6] ClauseItem functions with the combined BOLD | ITALIC style
        if (!texts[6].isEmpty()) {
            this.functionHeight = Math.max(this.functionHeight, this.getTextBounds(texts[6], this.labelFontBoldItalic).getHeight());
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
        // texts[7] Proposition indentation functions with the PLAIN style
        if (!texts[7].isEmpty()) {
            int maxFunctionLength = 0;
            for (final String singleFunction : texts[7].split("[ ]")) {
                maxFunctionLength = Math.max(maxFunctionLength, singleFunction.length());
            }
            // how much space do we GOT for indentation functions
            final double functionAreaHeight = this.propositionHeight - 2 * SvgConstants.BORDER_PROPOSITION - this.verticalSpacing;
            // how much space to we NEED for indentation functions
            final double neededSpace = this.getTextBounds(texts[7], this.labelFontPlain).getHeight() * maxFunctionLength;
            if (neededSpace > functionAreaHeight) {
                // we need too much...
                // how large can the Font for the indentation functions be
                final double maxSize = this.labelFontPlain.getSize2D() * functionAreaHeight / neededSpace;
                // store scaled font
                this.indentFunctionFont = this.labelFontPlain.deriveFont((float) Math.floor(maxSize));
            } else {
                this.indentFunctionFont = this.labelFontPlain;
            }
        }
        this.extentX = 0;
    }

    /**
     * Collects all texts to display of the {@link Pericope} in an array.
     *
     * @param flatPropositions
     *            {@link Proposition}s in the origin order disregarding indentations and splittings (indicated by arrows)
     * @return collected text fragments
     *         <ol start=0>
     *         <li>{@link Proposition} labels separated by line breaks [{@code \n}]</li>
     *         <li>{@link ClauseItem}s origin text parts separated by single spaces</li>
     *         <li>{@link Proposition}s syntactical translations separated by single spaces</li>
     *         <li>{@link ClauseItem} functions with the {@link Style#PLAIN} style separated by single spaces</li>
     *         <li>{@link ClauseItem} functions with the {@link Style#BOLD} style separated by single spaces</li>
     *         <li>{@link ClauseItem} functions with the {@link Style#ITALIC} style separated by single spaces</li>
     *         <li>{@link ClauseItem} functions with the {@link Style#BOLD_ITALIC} style separated by single spaces</li>
     *         <li>{@link Proposition} indentation functions separated by single spaces</li>
     *         </ol>
     */
    private String[] collectTexts(final List<Proposition> flatPropositions) {
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
        return new String[] { labels.toString().trim(), originTexts.toString().trim(), translations.toString().trim(),
                functionsPlain.toString().trim(), functionsBold.toString().trim(), functionsItalic.toString().trim(),
                functionsBoldItalic.toString().trim(), indentFunctions.toString().trim() };
    }

    /**
     * WARNING: all non-origin-text parts are assumed to be left-to-right oriented.<br>
     * In order to provide a dynamic positioning one must explicitly identify the orientation of the user language.</br>
     *
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
            for (int i = ComparisonUtil.indexOfInstance(flatPropositions, target.getPartBeforeArrow()) + 1; i < targetIndex; i++) {
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
            for (int i = ComparisonUtil.indexOfInstance(flatPropositions, target.getPartAfterArrow()) - 1; i > targetIndex; i--) {
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

    private double calculateSyntacticalPropositionIndentation(final List<Proposition> flatPropositions, final int targetIndex) {
        final Proposition target = flatPropositions.get(targetIndex);
        // start width calculation including label and spacings left
        double indentation;
        // calculate the horizontal start position
        if (target.getPartBeforeArrow() == null) {
            // multiply with number of indentations
            IPropositionParent loopParent = target.getParent();
            indentation = this.horizontalSpacing + this.labelWidth;
            if (this.labelWidth > 0) {
                indentation += this.horizontalSpacing;
            }
            while (loopParent instanceof Proposition) {
                indentation += this.indentationWidth;
                loopParent = ((Proposition) loopParent).getParent();
            }
        } else {
            // remember from part before arrow
            indentation = this.arrowPartIndentation.get(ComparisonUtil.indexOfInstance(flatPropositions, target.getPartBeforeArrow()));
        }
        return indentation;
    }

    /**
     * WARNING: the function label is assumed to be left-to-right oriented.<br>
     * In order to provide a dynamic positioning one must identify the desired text orientation of the function label itself...</br>
     *
     * @param storedFunction
     *            not yet translated syntactical function key
     * @return created SVG-Element representing the top-to-bottom rendered indentation function
     */
    private Element createIndentationFunctionElement(final Document xml, final SyntacticalFunction storedFunction) {
        final String indentFunction = storedFunction.getCode();
        final double functionCharHeight = this.getTextBounds(indentFunction, this.indentFunctionFont).getHeight() * 1.2;
        double currentCharY =
                (this.propositionHeight + this.verticalSpacing - functionCharHeight * (indentFunction.length() - 1)) / 2
                        - SvgConstants.BORDER_PROPOSITION;
        double charX = this.indentationWidth / 2;
        if (this.model.isLeftToRightOriented()) {
            charX *= -1;
        }
        final String staticCharX = this.numberToString(charX);
        final StringBuilder coordsY = new StringBuilder();
        final StringBuilder coordsX = new StringBuilder();
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
     * WARNING: the function label is assumed to be left-to-right oriented.<br>
     * In order to provide a dynamic positioning one must identify the desired text orientation of the function label itself...</br>
     *
     * @param parent
     *            parent element to subordinate all clause items
     * @param targetProposition
     *            proposition containing the clause items to insert
     * @param offsetX
     *            horizontal margin
     * @return needed horizontal space of the
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

            double clauseItemMidX = itemWidth / 2;
            if (!this.model.isLeftToRightOriented()) {
                clauseItemMidX *= -1;
            }
            final Element originTextElement =
                    this.createTextElement(xml, singleItem.getOriginText(), this.model.getFont(), this.numberToString(clauseItemMidX),
                            this.numberToString(this.verticalSpacing + this.originTextBaseLine / 2), SvgConstants.VAL_TEXT_ANCHOR_MIDDLE,
                            this.colorOriginText);
            groupElement.appendChild(originTextElement);
            boolean underline = false;
            if (singleItem.getFunction() != null) {
                underline = singleItem.getFunction().isUnderlined();
                if (underline) {
                    originTextElement.setAttribute(SvgConstants.ATT_TEXT_DECORATION, SvgConstants.VAL_TEXT_DECORATION_UNDERLINE);
                }
                groupElement.appendChild(this.createTextElement(xml, singleItem.getFunction().getCode(), functionFont,
                        this.numberToString(clauseItemMidX),
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
