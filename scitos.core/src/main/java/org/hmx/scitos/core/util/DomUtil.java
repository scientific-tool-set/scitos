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

package org.hmx.scitos.core.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Collection of convenience functions for handling {@link Document} (i.e. parsed XML files).
 */
public final class DomUtil {

    /**
     * Set the attribute value under the specified name, if the given value is neither {@code null} nor an empty {@code String}.
     *
     * @param node
     *            element to set the attribute on
     * @param attributeName
     *            name of the targeted attribute
     * @param attributeValue
     *            value to set (method does nothing if this is {@code null} or an empty {@code String}
     * @return if the attribute has been set
     */
    public static boolean setNullableAttribute(final Element node, final String attributeName, final Object attributeValue) {
        if (attributeValue != null) {
            final String textValue = attributeValue.toString();
            if (!textValue.isEmpty()) {
                node.setAttribute(attributeName, textValue);
                return true;
            }
        }
        return false;
    }

    /**
     * Get the attribute value under the specified name from the given element.
     *
     * @param node
     *            element to retrieve the attribute value from
     * @param attributeName
     *            name of the targeted attribute
     * @return the attribute's value (or {@code null} if no such attribute exists)
     */
    public static String getNullableAttribute(final Element node, final String attributeName) {
        if (node.hasAttribute(attributeName)) {
            return node.getAttribute(attributeName);
        }
        return null;
    }

    /**
     * Get the attribute value under the specified name from the given element. Parsing it as an Integer.
     *
     * @param node
     *            element to retrieve the attribute value from
     * @param attributeName
     *            name of the targeted attribute
     * @param defaultValue
     *            value to return if attribute doesn't exist or cannot be parsed to an Integer
     * @return either retrieved or given default value
     */
    public static int getIntAttribute(final Element node, final String attributeName, final int defaultValue) {
        final String stringValue = DomUtil.getNullableAttribute(node, attributeName);
        if (stringValue != null) {
            try {
                return Integer.parseInt(stringValue);
            } catch (final NumberFormatException expected) {
                // fall back on default value
            }
        }
        return defaultValue;
    }

    /**
     * Get the first child element with the specified name from the given parent node. Returns {@code null} if no child element with the specified
     * name exists.
     *
     * @param parentNode
     *            node to retrieve a single child element from
     * @param childNodeName
     *            name of the targeted child element
     * @return the first child element with the given name
     */
    public static Element getChildElement(final Node parentNode, final String childNodeName) {
        final NodeList candidates = parentNode.getChildNodes();
        final int childCount = candidates.getLength();
        for (int childIndex = 0; childIndex < childCount; childIndex++) {
            final Node singleChild = candidates.item(childIndex);
            if (singleChild instanceof Element && childNodeName.equals(((Element) singleChild).getTagName())) {
                // found a matching child element, ignore potentially following matches
                return (Element) singleChild;
            }
        }
        return null;
    }

    /**
     * Get all child elements with the specified name(s) from the given parent node. Returns an empty list if no child element with the specified
     * name(s) exist.
     *
     * @param parentNode
     *            node to retrieve the child elements from
     * @param childNodeNames
     *            names of the targeted child elements
     * @return all child elements with the given name
     */
    public static List<Element> getChildElements(final Node parentNode, final String... childNodeNames) {
        final List<Element> children = new LinkedList<Element>();
        final NodeList candidates = parentNode.getChildNodes();
        final int childCount = candidates.getLength();
        final List<String> targetNodeNames = Arrays.asList(childNodeNames);
        for (int childIndex = 0; childIndex < childCount; childIndex++) {
            final Node singleChild = candidates.item(childIndex);
            if (singleChild instanceof Element && (targetNodeNames.isEmpty() || targetNodeNames.contains(((Element) singleChild).getTagName()))) {
                children.add((Element) singleChild);
            }
        }
        return children;
    }
}
