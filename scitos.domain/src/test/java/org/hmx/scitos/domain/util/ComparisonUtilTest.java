package org.hmx.scitos.domain.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test of the {@link ComparisonUtil} class.
 */
public class ComparisonUtilTest {

    /** Test: for isNullAwareEqual method (null, null). */
    @Test
    public void testIsNullAwareEqual_1() {
        Assert.assertTrue(ComparisonUtil.isNullAwareEqual(null, null));
    }

    /** Test: for isNullAwareEqual method (null, String). */
    @Test
    public void testIsNullAwareEqual_2() {
        Assert.assertFalse(ComparisonUtil.isNullAwareEqual(null, ""));
    }

    /** Test: for isNullAwareEqual method (String, null). */
    @Test
    public void testIsNullAwareEqual_3() {
        Assert.assertFalse(ComparisonUtil.isNullAwareEqual("", null));
    }

    /** Test: for isNullAwareEqual method with equal objects (String, String). */
    @Test
    public void testIsNullAwareEqual_4() {
        final String instance = "ab";
        Assert.assertTrue(ComparisonUtil.isNullAwareEqual(instance, new String(instance)));
    }

    /** Test: for isNullAwareEqual method with unequal objects (String, String). */
    @Test
    public void testIsNullAwareEqual_5() {
        Assert.assertFalse(ComparisonUtil.isNullAwareEqual("ab", "cd"));
    }

    /** Test: for isNullOrEmptyAwareEqual(List, List) method (null, null). */
    @Test
    public void testIsNullOrEmptyAwareEqual_1() {
        Assert.assertTrue(ComparisonUtil.isNullOrEmptyAwareEqual((List<?>) null, null));
    }

    /** Test: for isNullOrEmptyAwareEqual(List, List) method (null, empty). */
    @Test
    public void testIsNullOrEmptyAwareEqual_2() {
        Assert.assertTrue(ComparisonUtil.isNullOrEmptyAwareEqual(null, Collections.emptyList()));
    }

    /** Test: for isNullOrEmptyAwareEqual(List, List) method (empty, null). */
    @Test
    public void testIsNullOrEmptyAwareEqual_3() {
        Assert.assertTrue(ComparisonUtil.isNullOrEmptyAwareEqual(new ArrayList<String>(1), null));
    }

    /** Test: for isNullOrEmptyAwareEqual(List, List) method (empty, empty). */
    @Test
    public void testIsNullOrEmptyAwareEqual_4() {
        Assert.assertTrue(ComparisonUtil.isNullOrEmptyAwareEqual(Collections.emptyList(), new ArrayList<String>(2)));
    }

    /** Test: for isNullOrEmptyAwareEqual(List, List) method (null, non-empty). */
    @Test
    public void testIsNullOrEmptyAwareEqual_5() {
        Assert.assertFalse(ComparisonUtil.isNullOrEmptyAwareEqual(null, Arrays.asList("")));
    }

    /** Test: for isNullOrEmptyAwareEqual(List, List) method (non-empty, null). */
    @Test
    public void testIsNullOrEmptyAwareEqual_6() {
        Assert.assertFalse(ComparisonUtil.isNullOrEmptyAwareEqual(Arrays.asList(""), null));
    }

    /** Test: for isNullOrEmptyAwareEqual(List, List) method (empty, non-empty). */
    @Test
    public void testIsNullOrEmptyAwareEqual_7() {
        Assert.assertFalse(ComparisonUtil.isNullOrEmptyAwareEqual(Collections.emptyList(), Arrays.asList("")));
    }

    /** Test: for isNullOrEmptyAwareEqual(List, List) method (non-empty, empty). */
    @Test
    public void testIsNullOrEmptyAwareEqual_8() {
        Assert.assertFalse(ComparisonUtil.isNullOrEmptyAwareEqual(Arrays.asList(""), Collections.emptyList()));
    }

    /** Test: for isNullOrEmptyAwareEqual(List, List) method for equal lists (non-empty, non-empty). */
    @Test
    public void testIsNullOrEmptyAwareEqual_9() {
        final String instance = "ab";
        Assert.assertTrue(ComparisonUtil.isNullOrEmptyAwareEqual(Arrays.asList(instance), Arrays.asList(new String(instance))));
    }

    /** Test: for isNullOrEmptyAwareEqual(List, List) method for unequal lists (non-empty, non-empty). */
    @Test
    public void testIsNullOrEmptyAwareEqual_10() {
        Assert.assertFalse(ComparisonUtil.isNullOrEmptyAwareEqual(Arrays.asList("ab"), Arrays.asList("cd")));
    }

    /** Test: for isNullOrEmptyAwareEqual(String, String) method (null, null). */
    @Test
    public void testIsNullOrEmptyAwareEqualString_1() {
        Assert.assertTrue(ComparisonUtil.isNullOrEmptyAwareEqual((String) null, null));
    }

    /** Test: for isNullOrEmptyAwareEqual(String, String) method (null, empty). */
    @Test
    public void testIsNullOrEmptyAwareEqualString_2() {
        Assert.assertTrue(ComparisonUtil.isNullOrEmptyAwareEqual(null, ""));
    }

    /** Test: for isNullOrEmptyAwareEqual(String, String) method (empty, null). */
    @Test
    public void testIsNullOrEmptyAwareEqualString_3() {
        Assert.assertTrue(ComparisonUtil.isNullOrEmptyAwareEqual("", null));
    }

    /** Test: for isNullOrEmptyAwareEqual(String, String) method (empty, empty). */
    @Test
    public void testIsNullOrEmptyAwareEqualString_4() {
        final String emptyString = "";
        Assert.assertTrue(ComparisonUtil.isNullOrEmptyAwareEqual(emptyString, new String(emptyString)));
    }

    /** Test: for isNullOrEmptyAwareEqual(String, String) method (null, non-empty). */
    @Test
    public void testIsNullOrEmptyAwareEqualString_5() {
        Assert.assertFalse(ComparisonUtil.isNullOrEmptyAwareEqual(null, "a"));
    }

    /** Test: for isNullOrEmptyAwareEqual(String, String) method (non-empty, null). */
    @Test
    public void testIsNullOrEmptyAwareEqualString_6() {
        Assert.assertFalse(ComparisonUtil.isNullOrEmptyAwareEqual("b", null));
    }

    /** Test: for isNullOrEmptyAwareEqual(String, String) method (empty, non-empty). */
    @Test
    public void testIsNullOrEmptyAwareEqualString_7() {
        Assert.assertFalse(ComparisonUtil.isNullOrEmptyAwareEqual("", "c"));
    }

    /** Test: for isNullOrEmptyAwareEqual(String, String) method (non-empty, empty). */
    @Test
    public void testIsNullOrEmptyAwareEqualString_8() {
        Assert.assertFalse(ComparisonUtil.isNullOrEmptyAwareEqual("d", ""));
    }

    /** Test: for isNullOrEmptyAwareEqual(String, String) method for equal texts (non-empty, non-empty). */
    @Test
    public void testIsNullOrEmptyAwareEqualString_9() {
        final String instance = "ab";
        Assert.assertTrue(ComparisonUtil.isNullOrEmptyAwareEqual(instance, new String(instance)));
    }

    /** Test: for isNullOrEmptyAwareEqual(String, String) method for unequal texts (non-empty, non-empty). */
    @Test
    public void testIsNullOrEmptyAwareEqualString_10() {
        Assert.assertFalse(ComparisonUtil.isNullOrEmptyAwareEqual("ab", "cd"));
    }

    /** Test: for compareNullAware method (null, null). */
    @Test
    public void testCompareNullAware_1() {
        Assert.assertEquals(0, ComparisonUtil.compareNullAware(null, null));
    }

    /** Test: for compareNullAware method (null, String). */
    @Test
    public void testCompareNullAware_2() {
        Assert.assertNotEquals(0, ComparisonUtil.compareNullAware(null, ""));
    }

    /** Test: for compareNullAware method (String, null). */
    @Test
    public void testCompareNullAware_3() {
        Assert.assertNotEquals(0, ComparisonUtil.compareNullAware("", null));
    }

    /** Test: for compareNullAware method (String, String). */
    @Test
    public void testCompareNullAware_4() {
        final String instance = "ab";
        Assert.assertEquals(0, ComparisonUtil.compareNullAware(instance, new String(instance)));
    }

    /** Test: for containsInstance method (list with same object, String). */
    @Test
    public void testContainsInstance_1() {
        final String instance = "ab";
        Assert.assertTrue(ComparisonUtil.containsInstance(Arrays.asList("a", instance, "b"), instance));
    }

    /** Test: for containsInstance method (list with equal but not same object, String). */
    @Test
    public void testContainsInstance_2() {
        final String instance = "ab";
        Assert.assertFalse(ComparisonUtil.containsInstance(Arrays.asList("a", instance, "b"), new String(instance)));
    }

    /** Test: for indexOfInstance method (instance contained once). */
    @Test
    public void testIndexOfInstance_1() {
        final String instance = "ab";
        Assert.assertEquals(1, ComparisonUtil.indexOfInstance(Arrays.asList("a", instance, "b"), instance));
    }

    /** Test: for indexOfInstance method (instance contained twice). */
    @Test
    public void testIndexOfInstance_2() {
        final String instance = "ab";
        Assert.assertEquals(1, ComparisonUtil.indexOfInstance(Arrays.asList("a", instance, "b", instance), instance));
    }

    /** Test: for indexOfInstance method (instance not contained). */
    @Test
    public void testIndexOfInstance_3() {
        Assert.assertEquals(-1, ComparisonUtil.indexOfInstance(Arrays.asList("a", "b"), "ab"));
    }

    /** Test: for indexOfInstance method (instance and equal object contained). */
    @Test
    public void testIndexOfInstance_4() {
        final String instance = "ab";
        Assert.assertEquals(3, ComparisonUtil.indexOfInstance(Arrays.asList("a", new String(instance), "b", instance), instance));
    }
}
