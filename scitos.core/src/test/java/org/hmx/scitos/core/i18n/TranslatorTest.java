package org.hmx.scitos.core.i18n;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for the {@link Translator} class.
 */
public class TranslatorTest {

    /**
     * Test: of getAvailableLocales method for the {@link Message} class, expecting three Locales: for the German language and the English language
     * (countries: New Zealand and US).
     */
    @Test
    public void testGetAvailableLocales() {
        final List<Locale> result = Translator.getAvailableLocales(Message.class);
        Assert.assertEquals(Arrays.asList(new Locale("de"), new Locale("en", "NZ"), new Locale("en", "US")), result);
    }
}
