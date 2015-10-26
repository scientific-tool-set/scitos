package org.hmx.scitos.hmx.core.export;

import java.text.MessageFormat;
import java.util.List;

import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.core.option.HmxExportOption;
import org.hmx.scitos.hmx.domain.model.ClauseItem;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.domain.model.Relation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creator for svg document listing comments contained in a single project's {@link Pericope}.
 */
class CommentSvgCreator extends AbstractSvgCreator {

    /** Counter of comments matching with its counter part in respective svgs created for the analyses. */
    private int commentCounter;
    /** Horizontal indentation of the comment texts, providing for enough space for the counter to be displayed in front. */
    private double indexExtentX;
    /** The overall with of the widest comment text, to set the generated document's size appropriately. */
    private double commentExtentX;
    /** The overall height of all comment texts combined, to set the generated document's appropriately. */
    private double extentY;

    /**
     * Constructor.
     * 
     * @param model
     *            the model to create the comment containing document for
     */
    protected CommentSvgCreator(final Pericope model) {
        super(model);
    }

    /**
     * Create a svg document listing the comments in the associated {@link Pericope}. Comments on {@link Proposition}s are always included. The
     * inclusion of {@link Relation} and {@link ClauseItem} comments is enabled/disabled by the respective flags.
     * 
     * @param considerRelations
     *            if comments on {@link Relation}s should be included in the generated document
     * @param considerClauseItems
     *            if comments on {@link ClauseItem}s should be included in the generated document
     * @return generated svg document
     * @throws HmxException
     *             failed to create an empty document to be filled
     */
    protected synchronized Document generateSvg(final boolean considerRelations, final boolean considerClauseItems) throws HmxException {
        final Document xml = this.createSvgDocument(HmxMessage.EXPORT_CONTENT_COMMENTS.get());
        final Element root = xml.getDocumentElement();
        root.appendChild(xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_DESCRIPTION)).setTextContent(this.model.getComment());
        root.appendChild(xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_DEFINITIONS)).appendChild(
                this.createColorDefinition(xml, SvgConstants.VAL_ID_COLOR_FUNCTIONTEXT_PLAIN, HmxExportOption.FONTCOLOR_ORIGINTEXT));
        this.commentCounter = 0;
        this.indexExtentX = 0;
        this.commentExtentX = 0;
        this.extentY = 0;
        final List<Proposition> propositions = this.model.getFlatText();
        final Element commentGroup = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_GROUP);
        for (final Proposition singleProposition : propositions) {
            for (final ClauseItem singleItem : singleProposition) {
                final String itemComment = singleItem.getComment();
                if (itemComment != null && !itemComment.isEmpty()) {
                    /*
                     * to preserve the displayed comment indexing, we have count the clause item comments even if we don´t care for the actual
                     * comments
                     */
                    this.commentCounter++;
                    if (considerClauseItems) {
                        commentGroup.appendChild(this.createCommentElement(xml, itemComment));
                    }
                }
            }
            final String propComment = singleProposition.getComment();
            if (propComment != null && !propComment.isEmpty()) {
                this.commentCounter++;
                commentGroup.appendChild(this.createCommentElement(xml, propComment));
            }
        }
        /*
         * since we are at the end of the comment order, we don´t need counting relations, if we are not actually interested in their commentaries
         */
        if (considerRelations) {
            for (final Relation singleRelation : this.model.getFlatRelations()) {
                final String relationComment = singleRelation.getComment();
                if (relationComment != null && !relationComment.isEmpty()) {
                    this.commentCounter++;
                    commentGroup.appendChild(this.createCommentElement(xml, relationComment));
                }
            }
        }
        commentGroup.setAttribute(SvgConstants.ATT_GROUP_TRANSFORM, MessageFormat.format(SvgConstants.VAL_GROUP_TRANSFORM_TRANSLATE_1,
                this.numberToString(this.indexExtentX + this.horizontalSpacing)));
        root.appendChild(commentGroup);
        root.setAttribute(SvgConstants.ATT_WIDTH, this.numberToString(this.indexExtentX + 3 * this.horizontalSpacing + this.commentExtentX));
        root.setAttribute(SvgConstants.ATT_HEIGHT, this.numberToString(this.extentY - this.verticalSpacing));
        return xml;
    }

    /**
     * Create a single element reoresenting the given comment text.
     * 
     * @param xml
     *            the designated document this element is created for
     * @param commentText
     *            the comment text to represent
     * @return created comment element
     */
    private Element createCommentElement(final Document xml, final String commentText) {
        final Element groupElement = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_GROUP);
        groupElement.setAttribute(SvgConstants.ATT_GROUP_TRANSFORM,
                MessageFormat.format(SvgConstants.VAL_GROUP_TRANSFORM_TRANSLATE_2, "0", this.numberToString(this.extentY)));
        final double commentLineHeight = this.getTextBounds(commentText, this.labelFontPlain).getHeight() * 1.1;
        final String identifier = this.numberToString(this.commentCounter);
        groupElement.appendChild(this.createTextElement(xml, identifier, this.commentFont, this.numberToString(0),
                this.numberToString(commentLineHeight * .75), SvgConstants.VAL_TEXT_ANCHOR_END, null));
        this.indexExtentX = Math.max(this.indexExtentX, this.getTextBounds(identifier, this.commentFont).getWidth());
        double offsetY = 0;
        for (final String singleLine : commentText.split("[\n]")) {
            offsetY += commentLineHeight;
            if (!singleLine.isEmpty()) {
                groupElement.appendChild(this.createTextElement(xml, singleLine, this.labelFontPlain, this.numberToString(this.horizontalSpacing),
                        this.numberToString(offsetY), SvgConstants.VAL_TEXT_ANCHOR_START, null));
                this.commentExtentX = Math.max(this.commentExtentX, this.getTextBounds(singleLine, this.labelFontPlain).getWidth());
            }
        }
        this.extentY += offsetY + 2 * this.verticalSpacing;
        return groupElement;
    }
}
