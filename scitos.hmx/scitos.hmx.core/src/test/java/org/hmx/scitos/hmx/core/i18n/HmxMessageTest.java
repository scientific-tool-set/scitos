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

package org.hmx.scitos.hmx.core.i18n;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.hmx.scitos.core.XmlResourceBundleControl;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test of the HermeneutiX module's {@link HmxMessage} class.
 */
public class HmxMessageTest {

    /**
     * Test: check all defined entries, for potential {@link MissingResourceException}s (i.e. ensure they are all included in the xml file).
     * Additionally, confirm that all entries in the xml file have a corresponding message enum entry.
     */
    @Test
    public void testMessageAvailability() {
        final ResourceBundle bundle = ResourceBundle.getBundle(HmxMessage.class.getName(), Locale.ENGLISH, new XmlResourceBundleControl());
        Assert.assertNotNull(bundle);
        final List<String> unusedMessages = new LinkedList<>();
        final List<String> unavailableMessages = new LinkedList<>();
        final Enumeration<String> availableKeys = bundle.getKeys();
        while (availableKeys.hasMoreElements()) {
            unusedMessages.add(availableKeys.nextElement());
        }
        for (final HmxMessage singleMessage : HmxMessage.values()) {
            try {
                singleMessage.get();
                unusedMessages.remove(singleMessage.getKey());
            } catch (final MissingResourceException expected) {
                unavailableMessages.add(singleMessage.getKey());
            }
        }
        Assert.assertEquals("There are declared messages not contained in the basic xml file.", Collections.emptyList(), unavailableMessages);
        Assert.assertEquals("There are unused messages in the basic xml file.", Collections.emptyList(), unusedMessages);
    }
}
