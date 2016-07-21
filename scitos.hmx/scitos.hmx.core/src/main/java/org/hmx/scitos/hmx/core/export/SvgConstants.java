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

import java.awt.Color;

/**
 * Constant interface to avoid hard coded generic SVG tags in the generation code.
 */
final class SvgConstants {

    /** hidden constructor due to constants only. */
    private SvgConstants() {
        // never called
    }

    /* universal spacing settings */
    static final Color DEFAULT_COLOR_PROPOSITIONBORDER = null;
    static final int BORDER_PROPOSITION = 1;
    static final int MIN_VERTICALSPACING = SvgConstants.BORDER_PROPOSITION * 5;
    static final String VAL_ID_ARROW_DOWNWARD = "downwardArrow";
    static final String VAL_ID_ARROW_UPWARD = "upwardArrow";
    static final String VAL_ID_COLOR_FUNCTIONTEXT_PLAIN = "functionColorPlain";
    static final String VAL_ID_COLOR_FUNCTIONTEXT_BOLD = "functionColorBold";
    static final String VAL_ID_COLOR_FUNCTIONTEXT_BOLDITALIC = "functionColorBoldItalic";
    static final String VAL_ID_COLOR_FUNCTIONTEXT_ITALIC = "functionColorItalic";
    static final String VAL_ID_COLOR_ROLETEXT = "semanticalRoleColor";
    static final String VAL_ID_COLOR_ORIGINTEXT = "originTextColor";
    static final String VAL_ID_COLOR_TRANSLATIONTEXT = "translationTextColor";
    static final String VAL_ID_COLOR_LABELTEXT = "labelTextColor";
    static final String VAL_ID_COLOR_PROPOSIONBORDER = "propositionBorderColor";
    static final String VAL_ID_COLOR_PROPOSIONBACKGROUND = "propositionBackgroundColor";
    static final String VAL_ID_COLOR_RELATION = "relationColor";
    static final String VAL_ID_FONT_LABELSBOLD = "boldLabelFont";
    static final String VAL_ID_FONT_LABELSBOLDITALIC = "boldItalicLabelFont";
    static final String VAL_ID_FONT_LABELSITALIC = "italicLabelFont";
    static final String VAL_ID_FONT_LABELSPLAIN = "plainLabelFont";
    static final String VAL_ID_FONT_ORIGINTEXT = "originTextFont";
    static final String VAL_FOCUSABLE_TRUE = "true";

    /** SVG-Namespace with the corresponding <i>svg</i>-prefix. */
    static final String NAMESPACE_SVG = "http://www.w3.org/2000/svg";
    private static final String NAMESPACE_SVG_PREFIX = "svg:";
    static final String ATT_NAMESPACE_DECLARATION_SVG = "xmlns:svg";
    /** XLink-Namespace with the corresponding <i>xlink</i>-prefix. */
    static final String NAMESPACE_XLINK = "http://www.w3.org/1999/xlink";
    private static final String NAMESPACE_XLINK_PREFIX = "xlink:";
    static final String ATT_NAMESPACE_DECLARATION_XLINK = "xmlns:xlink";
    // /** XML:ID-Namespace with the corresponding <i>xml</i>-prefix */
    // static final String NAMESPACE_XML = "http://www.w3.org/XML/1998/namespace";
    // private static final String NAMESPACE_XML_PREFIX = "xml:";
    // static final String ATT_NAMESPACE_DECLARATION_XML = "xmlns:xml";

    /* svg doctype parts */
    static final String DOCTYPE_ELEMENTNAME = "svg";
    static final String DOCTYPE_PUBLICID = "-//W3C//DTD SVG 1.1//EN";
    static final String DOCTYPE_SYSTEMID = "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd";

    /* used svg tags */
    static final String TAG_MAIN = SvgConstants.NAMESPACE_SVG_PREFIX + "svg";
    static final String TAG_TITLE = SvgConstants.NAMESPACE_SVG_PREFIX + "title";
    static final String TAG_DESCRIPTION = SvgConstants.NAMESPACE_SVG_PREFIX + "desc";
    static final String TAG_DEFINITIONS = SvgConstants.NAMESPACE_SVG_PREFIX + "defs";
    static final String TAG_COLOR = SvgConstants.NAMESPACE_SVG_PREFIX + "solidColor";
    static final String TAG_POLYGON = SvgConstants.NAMESPACE_SVG_PREFIX + "polygon";
    static final String TAG_POLYLINE = SvgConstants.NAMESPACE_SVG_PREFIX + "polyline";
    static final String TAG_USE = SvgConstants.NAMESPACE_SVG_PREFIX + "use";
    static final String TAG_FONT = SvgConstants.NAMESPACE_SVG_PREFIX + "font";
    static final String TAG_GROUP = SvgConstants.NAMESPACE_SVG_PREFIX + "g";
    static final String TAG_RECTANGLE = SvgConstants.NAMESPACE_SVG_PREFIX + "rect";
    static final String TAG_TEXT = SvgConstants.NAMESPACE_SVG_PREFIX + "text";
    static final String TAG_TSPAN = SvgConstants.NAMESPACE_SVG_PREFIX + "tspan";
    /* attributes available in multiple tags */
    static final String ATT_POSX = "x";
    static final String ATT_POSY = "y";
    static final String ATT_WIDTH = "width";
    static final String ATT_HEIGHT = "height";
    static final String ATT_FILL = "fill";
    static final String ATT_STROKE = "stroke";
    static final String ATT_STROKE_WIDTH = "stroke-width";
    static final String ATT_STROKE_LINECAP = "stroke-linecap";
    static final String ATT_STROKE_LINEJOIN = "stroke-linejoin";
    static final String ATT_FOCUSABLE = "focusable";
    /**
     * HINT: it is recommended to explicitly set the xml namespace (http://www.w3.org/XML/1998/namespace) for this attribute, but since it does not
     * actually work for internal references...
     */
    static final String ATT_ID = "id";
    /**
     * this attribute needs to be used in combination with the {@link #NAMESPACE_XLINK xlink} namespace.
     */
    static final String ATT_XLINKHREF = SvgConstants.NAMESPACE_XLINK_PREFIX + "href";

    /* attributes and corresponding values of the TAG_MAIN */
    static final String ATT_MAIN_VERSION = "version";
    static final String VAL_MAIN_VERSION = "1.2";
    static final String ATT_MAIN_BASEPROFILE = "baseProfile";
    static final String VAL_MAIN_BASEPROFILE = "tiny";

    /* attribute and value in the TAG_COLOR */
    static final String ATT_COLOR_VALUE = "solid-color";

    /* attribute and values in the TAG_POLYGON */
    static final String ATT_POLYGON_POINTS = "points";
    static final String VAL_POLYGON_POINTS_UPWARDARROW = "0,18 10,0 20,18 13,15 13,30 7,30 7,15";
    static final String VAL_POLYGON_POINTS_DOWNWARDARROW = "0,12 10,30 20,12 13,15 13,0 7,0 7,15";
    static final int ARROWS_CENTERX = 10;
    static final int ARROWS_CENTERY = 15;
    static final String VAL_STROKE_WIDTH_ARROWS = "1";
    static final String VAL_STROKE_LINECAP_ARROWS = "round";
    static final String VAL_STROKE_LINEJOIN_ARROWS = "round";

    /* attribute and value in the TAG_GROUP */
    static final String ATT_GROUP_TRANSFORM = "transform";
    /**
     * Format for a transformation: {@code scale()} with a single factor.
     */
    static final String VAL_GROUP_TRANSFORM_SCALE_1 = "scale(%s)";
    /**
     * Format for a transformation: {@code translate()} with a single parameter.
     */
    static final String VAL_GROUP_TRANSFORM_TRANSLATE_1 = "translate(%s)";
    /**
     * Format for a transformation: {@code translate()} with two parameters.
     */
    static final String VAL_GROUP_TRANSFORM_TRANSLATE_2 = "translate(%s,%s)";

    /* attribute values in the TAG_RECTANGLE */
    static final String VAL_STROKE_LINECAP_PROPBORDER = "round";
    static final String VAL_STROKE_LINEJOIN_PROPBORDER = "round";

    /* attribute value in the TAG_POLYLINE */
    static final String ATT_POLYLINE_POINTS = "points";
    /**
     * Format for a single point in a {@code polyline}/{@code polygon} consisting of a coordinate pair {@code X,Y}.
     */
    static final String VAL_POLYLINE_POINTS_2 = "%s,%s ";
    static final String VAL_STROKE_LINECAP_RELATION = "round";
    static final String VAL_COLOR_RELATION = "url(#" + SvgConstants.VAL_ID_COLOR_RELATION + ')';
    static final String VAL_FILL_RELATION = "none";

    /* attributes of the TAG_TEXT */
    static final String ATT_FONT_FAMILY = "font-family";
    static final String ATT_FONT_SIZE = "font-size";
    static final String ATT_FONT_STYLE = "font-style";
    static final String VAL_FONT_STYLE_ITALIC = "italic";
    static final String ATT_FONT_WEIGHT = "font-weight";
    static final String VAL_FONT_WEIGHT_BOLD = "bold";
    static final String ATT_TEXT_ANCHOR = "text-anchor";
    static final String VAL_TEXT_ANCHOR_START = "start";
    static final String VAL_TEXT_ANCHOR_MIDDLE = "middle";
    static final String VAL_TEXT_ANCHOR_END = "end";
    static final String ATT_TEXT_DECORATION = "text-decoration";
    static final String VAL_TEXT_DECORATION_UNDERLINE = "underline";
    static final String VAL_TEXT_DECORATION_NONE = "none";
    static final String ATT_TEXT_BASELINE_SHIFT = "baseline-shift";
    static final String ATT_DIRECTION = "direction";
    static final String VAL_DIRECTION_LTR = "ltr";
    static final String VAL_DIRECTION_RTL = "rtl";
}
