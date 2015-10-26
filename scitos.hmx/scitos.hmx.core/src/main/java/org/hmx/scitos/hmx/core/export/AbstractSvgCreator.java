package org.hmx.scitos.hmx.core.export;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.RectangularShape;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.core.option.HmxExportOption;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Abstract class for creating SVGs from within the java code (instead of using XSLT stylesheets), offering a few wrapping methods for general use
 * cases and providing a base set of {@link Font}s in addition to the targeted {@link Pericope}.
 */
abstract class AbstractSvgCreator {

    /**
     * SVG compatible number format for {@link #numberToString(double)}.
     */
    private final NumberFormat numberFormatter = NumberFormat.getInstance(Locale.ENGLISH);

    {
        // SVG interpreters do not like the grouping
        this.numberFormatter.setGroupingUsed(false);
        // the default (1) is just confusing and useless
        this.numberFormatter.setMinimumFractionDigits(0);
        // the default (3) might not be accurate enough
        this.numberFormatter.setMaximumFractionDigits(5);
    }

    /** Copy of the targeted model at the time of this svg creator instance being initialized. */
    protected final Pericope model;
    /** Font to apply for non-origin texts with plain style. */
    protected final Font labelFontPlain;
    /** Font to apply for non-origin texts with bold style. */
    protected final Font labelFontBold;
    /** Font to apply for non-origin texts with bold and italic style. */
    protected final Font labelFontBoldItalic;
    /** Font to apply for non-origin texts with italic style. */
    protected final Font labelFontItalic;
    /** Smaller sized font to apply for comments and their representing numeric identifiers. */
    protected final Font commentFont;
    /** The spacing to insert between elements on the x-axis. */
    protected final double horizontalSpacing;
    /** The spacing to insert between elements on the y-axis. */
    protected final double verticalSpacing;
    /**
     * Generic {@link FontRenderContext} for executing calculations in {@link #getTextBounds(String, Font)}.
     */
    private final FontRenderContext fontContext = new FontRenderContext(new AffineTransform(), true, true);
    /** Font color to be applied to orgin text parts. */
    protected final String colorOriginText;
    /** Font color to be applied to translations. */
    protected final String colorTranslationText;
    /** Font color to be applied to labels. */
    protected final String colorLabelText;
    /** Border color to be applied to propositions. */
    protected final String colorPropositionBorder;
    /** Background color to be applied to propositions. */
    protected final String colorPropositionBackground;

    /**
     * Constructor: cloning the given model to ensure thread security and calculating basic constraints.
     *
     * @param model
     *            the {@link Pericope} to represent in a SVG
     */
    protected AbstractSvgCreator(final Pericope model) {
        this.model = model.clone();
        this.labelFontPlain =
                new Font(HmxExportOption.NONORIGINTEXT_FONT_TYPE.getValue(), Font.PLAIN,
                        HmxExportOption.NONORIGINTEXT_FONT_SIZE.getValueAsInteger());
        this.labelFontBold = this.labelFontPlain.deriveFont(Font.BOLD);
        this.labelFontBoldItalic = this.labelFontPlain.deriveFont(Font.BOLD | Font.ITALIC);
        this.labelFontItalic = this.labelFontPlain.deriveFont(Font.ITALIC);
        this.commentFont = this.labelFontPlain.deriveFont(this.labelFontPlain.getSize2D() * .66f);
        this.horizontalSpacing =
                Math.max(this.getTextBounds("W", this.model.getFont()).getWidth(), this.getTextBounds("W", this.labelFontPlain).getWidth());
        final LineMetrics originTextMetrics = this.model.getFont().getLineMetrics("", this.fontContext);
        final LineMetrics labelMetrics = this.labelFontBoldItalic.getLineMetrics("", this.fontContext);
        this.verticalSpacing =
                Math.max(originTextMetrics.getDescent() + originTextMetrics.getLeading(), labelMetrics.getDescent() + labelMetrics.getLeading())
                        * 1.1 + SvgConstants.BORDER_PROPOSITION;
        this.colorOriginText = HmxExportOption.FONTCOLOR_ORIGINTEXT.getValue();
        this.colorTranslationText = HmxExportOption.FONTCOLOR_TRANSLATION.getValue();
        this.colorLabelText = HmxExportOption.FONTCOLOR_LABEL.getValue();
        this.colorPropositionBorder = HmxExportOption.PROPOSITION_COLOR_BORDER.getValue();
        this.colorPropositionBackground = HmxExportOption.PROPOSITION_COLOR_BACKGROUND.getValue();
    }

    /**
     * Initially creates the XML document, its SVG root element and the main title element within.
     * <p>
     * HINT: the size needs to be set separately in the root element.
     * </p>
     *
     * @param exportTypeTitle
     *            language file key representing what kind of SVG document this is, too include it in the main title element
     * @return created XML document with the SVG root
     * @throws HmxException
     *             failed to create an empty document to be filled
     */
    protected Document createSvgDocument(final String exportTypeTitle) throws HmxException {
        final Document xml;
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            xml = factory.newDocumentBuilder().newDocument();
        } catch (final ParserConfigurationException pcex) {
            throw new HmxException(Message.ERROR_UNKNOWN, pcex);
        }
        // add the svg doctype declaration
        xml.appendChild(xml.getImplementation().createDocumentType(SvgConstants.DOCTYPE_ELEMENTNAME, SvgConstants.DOCTYPE_PUBLICID,
                SvgConstants.DOCTYPE_SYSTEMID));
        // create the 'svg' root element in its namespace
        final Element root = (Element) xml.appendChild(xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_MAIN));
        // add declarations for all document-wide used namespaces
        root.setAttribute(SvgConstants.ATT_NAMESPACE_DECLARATION_SVG, SvgConstants.NAMESPACE_SVG);
        root.setAttribute(SvgConstants.ATT_NAMESPACE_DECLARATION_XLINK, SvgConstants.NAMESPACE_XLINK);
        // define svg attributes like version, baseProfile and viewBox
        root.setAttribute(SvgConstants.ATT_MAIN_VERSION, SvgConstants.VAL_MAIN_VERSION);
        root.setAttribute(SvgConstants.ATT_MAIN_BASEPROFILE, SvgConstants.VAL_MAIN_BASEPROFILE);
        final StringBuilder title = new StringBuilder(HmxMessage.EXPORT_TITLE.get());
        if (this.model.getAuthor() != null && !this.model.getAuthor().isEmpty()) {
            title.append(' ').append(HmxMessage.EXPORT_TITLE_AUTHOR.get()).append(' ').append(this.model.getAuthor());
        }
        title.append(" [").append(exportTypeTitle).append("]");
        final Element titleElement = (Element) root.appendChild(xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_TITLE));
        if (this.model.getTitle() == null || this.model.getTitle().isEmpty()) {
            titleElement.setTextContent(title.toString());
        } else {
            titleElement.setTextContent(this.model.getTitle() + " (" + title.append(')').toString());
        }
        return xml;
    }

    /**
     * Creates colored polygon definitions for upward and downward arrows (indicating a {@link Proposition} with encapsulated child
     * {@link Proposition}s), which can be called by their IDs.
     *
     * @param xml
     *            the designated document this element is created for
     * @param arrowHeight
     *            desired height of the rendered arrows
     * @return created definitions
     * @see SvgConstants#VAL_ID_ARROW_UPWARD
     * @see SvgConstants#VAL_ID_ARROW_DOWNWARD
     */
    protected List<Element> createArrowDefinition(final Document xml, final double arrowHeight) {
        final List<Element> result = new ArrayList<Element>(2);
        // determine the preferred color
        final String arrowColor = HmxExportOption.ARROW_COLOR.getValue();
        // prepare scaling
        final double scalingFactor = this.calculateArrowScalingFactor(arrowHeight);
        final String transformScale = String.format(SvgConstants.VAL_GROUP_TRANSFORM_SCALE_1, String.valueOf(scalingFactor));
        // prepare a scaled version of a upward pointing arrow
        result.add(this.createArrowTemplate(xml, SvgConstants.VAL_ID_ARROW_UPWARD, arrowColor, SvgConstants.VAL_POLYGON_POINTS_UPWARDARROW,
                transformScale));
        // prepare a scaled version of a downward pointing arrow
        result.add(this.createArrowTemplate(xml, SvgConstants.VAL_ID_ARROW_DOWNWARD, arrowColor, SvgConstants.VAL_POLYGON_POINTS_DOWNWARDARROW,
                transformScale));
        return result;
    }

    /**
     * Calculate the factor required to scale one of the predefined arrow templates to the specified height.
     * 
     * @param arrowHeight
     *            expected height of the arrow element
     * @return scaling factor to apply for reaching the given height for an arrow
     */
    private double calculateArrowScalingFactor(final double arrowHeight) {
        return arrowHeight / (2 * SvgConstants.ARROWS_CENTERY);
    }

    /**
     * Calculate the width for an arrow with the specified height while preserving the predefined aspect ratio.
     * 
     * @param arrowHeight
     *            expected height of the arrow element
     * @return matching width to be expected for an arrow element with the given height
     */
    protected double calculateArrowWidth(final double arrowHeight) {
        return this.calculateArrowScalingFactor(arrowHeight) * 2 * SvgConstants.ARROWS_CENTERX;
    }

    /**
     * Calls one of the predefined arrow templates according to the given ID.
     *
     * @param xml
     *            the designated document this element is created for
     * @param arrowRefID
     *            ID of the arrow template to use
     * @param coordX
     *            most left horizontal coordinate to position the arrow to create
     * @param arrowHeight
     *            height of the arrow to insert
     * @param arrowScale
     *            applied scaling factor
     * @return created SVG-Element representing an arrow
     * @see SvgConstants#VAL_ID_ARROW_DOWNWARD
     * @see SvgConstants#VAL_ID_ARROW_UPWARD
     */
    protected Element createArrowElement(final Document xml, final String arrowRefID, final double coordX, final double arrowHeight,
            final double arrowScale) {
        final Element singleArrow = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_USE);
        singleArrow.setAttributeNS(SvgConstants.NAMESPACE_XLINK, SvgConstants.ATT_XLINKHREF, '#' + arrowRefID);
        singleArrow.setAttribute(SvgConstants.ATT_POSX, this.numberToString(coordX));
        singleArrow.setAttribute(SvgConstants.ATT_POSY, this.numberToString((arrowHeight / arrowScale - arrowHeight) / 2));
        return singleArrow;
    }

    /**
     * Create a template for a polygon with the specified fill color and coordinates.
     * 
     * @param xml
     *            the designated document this element is created for
     * @param id
     *            ID to set for this arrow template, which can be used to insert it in the main document
     * @param color
     *            the fill color to apply for the arrow
     * @param points
     *            pairs of x,y coordinates making up the arrow form
     * @param transform
     *            the transformation to be applied to the defined arrow
     * @return create template definition containing the arrow
     */
    private Element createArrowTemplate(final Document xml, final String id, final String color, final String points, final String transform) {
        final Element template = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_POLYGON);
        template.setAttribute(SvgConstants.ATT_FILL, color);
        template.setAttribute(SvgConstants.ATT_STROKE_LINECAP, SvgConstants.VAL_STROKE_LINECAP_ARROWS);
        template.setAttribute(SvgConstants.ATT_STROKE_LINEJOIN, SvgConstants.VAL_STROKE_LINEJOIN_ARROWS);
        template.setAttribute(SvgConstants.ATT_STROKE_WIDTH, SvgConstants.VAL_STROKE_WIDTH_ARROWS);
        template.setAttribute(SvgConstants.ATT_POLYGON_POINTS, points);
        final Element wrappedTemplate = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_GROUP);
        wrappedTemplate.setAttribute(SvgConstants.ATT_ID, id);
        wrappedTemplate.setAttribute(SvgConstants.ATT_GROUP_TRANSFORM, transform);
        wrappedTemplate.appendChild(template);
        return wrappedTemplate;
    }

    /**
     * Creates a color definition element, which can be called by the given ID.
     *
     * @param xml
     *            the designated document this element is created for
     * @param id
     *            ID to set for this color definition, which can be used to call for its specific value
     * @param option
     *            the setting providing the actual color value being defined
     * @return created definition
     */
    protected Element createColorDefinition(final Document xml, final String id, final HmxExportOption option) {
        final Element colorElement = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_COLOR);
        colorElement.setAttribute(SvgConstants.ATT_ID, id);
        colorElement.setAttribute(SvgConstants.ATT_COLOR_VALUE, option.getValue());
        return colorElement;
    }

    /**
     * Creates a filled rectangle with the given size on the coordinates {@code (0, 0)} according to the user selected color options.
     *
     * @param xml
     *            the designated document this element is created for
     * @param horizontalExtent
     *            width of the background rectangle
     * @param verticalExtent
     *            height of the background rectangle
     * @return created SVG-Element representing the specified rectangle
     */
    protected Element createPropositionBackground(final Document xml, final double horizontalExtent, final double verticalExtent) {
        final Element borderElement = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_RECTANGLE);
        if (this.model.isLeftToRightOriented()) {
            borderElement.setAttribute(SvgConstants.ATT_POSX, this.numberToString(SvgConstants.BORDER_PROPOSITION / 2.));
        } else {
            borderElement.setAttribute(SvgConstants.ATT_POSX, this.numberToString(SvgConstants.BORDER_PROPOSITION / 2. - horizontalExtent));
        }
        borderElement.setAttribute(SvgConstants.ATT_POSY, this.numberToString(SvgConstants.BORDER_PROPOSITION / 2.));
        borderElement.setAttribute(SvgConstants.ATT_WIDTH, this.numberToString(horizontalExtent - SvgConstants.BORDER_PROPOSITION));
        borderElement.setAttribute(SvgConstants.ATT_HEIGHT, this.numberToString(verticalExtent - SvgConstants.BORDER_PROPOSITION));
        borderElement.setAttribute(SvgConstants.ATT_STROKE, this.colorPropositionBorder);
        borderElement.setAttribute(SvgConstants.ATT_FILL, this.colorPropositionBackground);
        borderElement.setAttribute(SvgConstants.ATT_STROKE_WIDTH, this.numberToString(SvgConstants.BORDER_PROPOSITION));
        borderElement.setAttribute(SvgConstants.ATT_STROKE_LINECAP, SvgConstants.VAL_STROKE_LINECAP_PROPBORDER);
        borderElement.setAttribute(SvgConstants.ATT_STROKE_LINEJOIN, SvgConstants.VAL_STROKE_LINEJOIN_PROPBORDER);
        return borderElement;
    }

    /**
     * WARNING: the label is assumed to be left-to-right oriented. In order to provide a dynamic positioning one must identify the desired text
     * orientation of the label text itself...
     *
     * @param xml
     *            the designated document this element is created for
     * @param label
     *            short description label to insert
     * @param labelWidth
     *            maximum horizontal extent of a label
     * @param baseLine
     *            Y-coordinate of the origin texts baseline
     * @return created SVG-Element representing the given label text
     */
    protected Element createLabelElement(final Document xml, final String label, final double labelWidth, final double baseLine) {
        double coordX = this.horizontalSpacing + labelWidth / 2;
        if (!this.model.isLeftToRightOriented()) {
            coordX *= -1;
        }
        return this.createTextElement(xml, label, this.labelFontPlain, this.numberToString(coordX),
                this.numberToString(this.verticalSpacing + (baseLine - this.verticalSpacing) / 2), SvgConstants.VAL_TEXT_ANCHOR_MIDDLE,
                this.colorLabelText);
    }

    /**
     * WARNING: the translation is assumed to be left-to-right oriented. In order to provide a dynamic positioning one must identify the desired text
     * orientation of the translation text itself...
     *
     * @param xml
     *            the designated document this element is created for
     * @param translation
     *            translation text to insert
     * @param width
     *            horizontal space needed for rendering this translation text; <i>currently ignored, cause it would only be necessary for
     *            right-to-left oriented translations</i>
     * @param propositionHeight
     *            height of the proposition containing this translation
     * @return created SVG-Element representing the given translation text
     */
    protected Element createTranslationElement(final Document xml, final String translation, final double width, final double propositionHeight) {
        final String anchor;
        if (this.model.isLeftToRightOriented()) {
            anchor = SvgConstants.VAL_TEXT_ANCHOR_START;
        } else {
            anchor = SvgConstants.VAL_TEXT_ANCHOR_END;
        }
        return this
                .createTextElement(xml, translation, this.labelFontPlain, this.numberToString(0),
                        this.numberToString(propositionHeight - SvgConstants.BORDER_PROPOSITION - this.verticalSpacing), anchor,
                        this.colorTranslationText);
    }

    /**
     * Calculates a rectangle large enough to fit all the given text with the given font in.
     *
     * @param text
     *            text to calculate the needed space for
     * @param font
     *            font to use in needed space calculation
     * @return space needed to render the given text in the given font
     */
    protected RectangularShape getTextBounds(final String text, final Font font) {
        return new TextLayout(text, font, this.fontContext).getBounds();
    }

    /**
     * Creates a &lt;{@link SvgConstants#NAMESPACE_SVG svg}:{@link SvgConstants#TAG_TEXT text}/&gt; element containing the given text, on the
     * specified (parent-relative) position.
     * 
     * @param xml
     *            the designated document this element is created for
     * @param text
     *            content of the create element
     * @param font
     *            font to set (extracting its family name, size, style and weight)
     * @param coordX
     *            horizontal position (relative to its parent element); if this is on the left, mid or right of the text is determined by the
     *            {@code anchor} parameter
     * @param coordY
     *            vertical position (relative to its parent element); if this is on the top, mid or bottom of the text is determined by the
     *            {@code anchor} parameter
     * @param anchor
     *            <ul>
     *            <li>{@link SvgConstants#VAL_TEXT_ANCHOR_START start}: the given point is the bottom left (or bottom right in right-to-left
     *            direction) of the rendered text</li>
     *            <li>{@link SvgConstants#VAL_TEXT_ANCHOR_MIDDLE middle}: the given point is the center of the rendered text</li>
     *            </ul>
     * @param textColor
     *            use this color for the stroke as well as for the filling
     * @return created element
     */
    protected Element createTextElement(final Document xml, final String text, final Font font, final String coordX, final String coordY,
            final String anchor, final String textColor) {
        final Element textElement = xml.createElementNS(SvgConstants.NAMESPACE_SVG, SvgConstants.TAG_TEXT);
        textElement.setAttribute(SvgConstants.ATT_POSX, coordX);
        textElement.setAttribute(SvgConstants.ATT_POSY, coordY);
        textElement.setAttribute(SvgConstants.ATT_FOCUSABLE, SvgConstants.VAL_FOCUSABLE_TRUE);
        textElement.setAttribute(SvgConstants.ATT_FONT_FAMILY, font.getFamily());
        textElement.setAttribute(SvgConstants.ATT_FONT_SIZE, this.numberToString(font.getSize2D()));
        if (font == this.labelFontItalic || font == this.labelFontBoldItalic) {
            textElement.setAttribute(SvgConstants.ATT_FONT_STYLE, SvgConstants.VAL_FONT_STYLE_ITALIC);
        }
        if (font == this.labelFontBold || font == this.labelFontBoldItalic) {
            textElement.setAttribute(SvgConstants.ATT_FONT_WEIGHT, SvgConstants.VAL_FONT_WEIGHT_BOLD);
        }
        if (textColor != null) {
            textElement.setAttribute(SvgConstants.ATT_FILL, textColor);
            textElement.setAttribute(SvgConstants.ATT_STROKE, textColor);
        }
        textElement.setAttribute(SvgConstants.ATT_TEXT_ANCHOR, anchor);
        textElement.setTextContent(text);
        return textElement;
    }

    /**
     * The {@link String#valueOf(double)} method always adds at least one fractional digit. With our own {@link NumberFormat} we can avoid this
     * behavior and provide useful formatting for all numeric values.
     *
     * @param number
     *            value to be converted into svg compatible String
     * @return formatted numeric value
     */
    protected String numberToString(final double number) {
        return this.numberFormatter.format(number);
    }
}
