package org.hmx.scitos.ais.core.i18n;

import java.util.Collections;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.hmx.scitos.core.XmlResourceBundleControl;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test of the AIS module's {@link AisMessage} class.
 */
public class AisMessageTest {

    /**
     * Test: check all defined entries, for potential {@link MissingResourceException}s (i.e. ensure they are all included in the xml file).
     * Additionally, confirm that all entries in the xml file have a corresponding message enum entry.
     */
    @Test
    public void testMessageAvailability() {
        final ResourceBundle bundle = ResourceBundle.getBundle(AisMessage.class.getName(), Locale.ENGLISH, new XmlResourceBundleControl());
        Assert.assertNotNull(bundle);
        final List<String> unusedMessages = new ArrayList<>();
        final List<String> unavailableMessages = new ArrayList<>();
        final Enumeration<String> availableKeys = bundle.getKeys();
        while (availableKeys.hasMoreElements()) {
            unusedMessages.add(availableKeys.nextElement());
        }
        for (final AisMessage singleMessage : AisMessage.values()) {
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
