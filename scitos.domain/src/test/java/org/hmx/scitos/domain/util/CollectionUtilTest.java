package org.hmx.scitos.domain.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test of the {@link CollectionUtil} class.
 */
public class CollectionUtilTest {

    /** Test: for containsInstance method (list with same object, String). */
    @Test
    public void testContainsInstance_1() {
        final String instance = "ab";
        Assert.assertTrue(CollectionUtil.containsInstance(Arrays.asList("a", instance, "b"), instance));
    }

    /** Test: for containsInstance method (list with equal but not same object, String). */
    @Test
    public void testContainsInstance_2() {
        final String instance = "ab";
        Assert.assertFalse(CollectionUtil.containsInstance(Arrays.asList("a", instance, "b"), new String(instance)));
    }

    /** Test: for indexOfInstance method (instance contained once). */
    @Test
    public void testIndexOfInstance_1() {
        final String instance = "ab";
        Assert.assertEquals(1, CollectionUtil.indexOfInstance(Arrays.asList("a", instance, "b"), instance));
    }

    /** Test: for indexOfInstance method (instance contained twice). */
    @Test
    public void testIndexOfInstance_2() {
        final String instance = "ab";
        Assert.assertEquals(1, CollectionUtil.indexOfInstance(Arrays.asList("a", instance, "b", instance), instance));
    }

    /** Test: for indexOfInstance method (instance not contained). */
    @Test
    public void testIndexOfInstance_3() {
        Assert.assertEquals(-1, CollectionUtil.indexOfInstance(Arrays.asList("a", "b"), "ab"));
    }

    /** Test: for indexOfInstance method (instance and equal object contained). */
    @Test
    public void testIndexOfInstance_4() {
        final String instance = "ab";
        Assert.assertEquals(3, CollectionUtil.indexOfInstance(Arrays.asList("a", new String(instance), "b", instance), instance));
    }

    /** Test: for moveEntryInInsertSortedMap method (expect failure: move first entry up) */
    @Test(expected = IllegalArgumentException.class)
    public void testMoveEntryInInsertSortedMap_First_Up() {
        final Map<String, Integer> map = new LinkedHashMap<>(3);
        map.put("1", 1);
        map.put("2", 2);
        CollectionUtil.moveEntryInInsertSortedMap(map, "1", false);
    }

    /** Test: for moveEntryInInsertSortedMap method (move first entry up) */
    @Test
    public void testMoveEntryInInsertSortedMap_First_Down() {
        final Map<String, Integer> map = new LinkedHashMap<>(3);
        map.put("1", 1);
        map.put("2", 2);
        map.put("3", 3);
        CollectionUtil.moveEntryInInsertSortedMap(map, "1", true);
        Assert.assertEquals(Arrays.asList("2", "1", "3"), new ArrayList<>(map.keySet()));
    }

    /** Test: for moveEntryInInsertSortedMap method (move second entry up) */
    @Test
    public void testMoveEntryInInsertSortedMap_Second_Up() {
        final Map<String, Integer> map = new LinkedHashMap<>(3);
        map.put("1", 1);
        map.put("2", 2);
        map.put("3", 3);
        map.put("4", 4);
        CollectionUtil.moveEntryInInsertSortedMap(map, "2", false);
        Assert.assertEquals(Arrays.asList("2", "1", "3", "4"), new ArrayList<>(map.keySet()));
    }

    /** Test: for moveEntryInInsertSortedMap method (move second entry down) */
    @Test
    public void testMoveEntryInInsertSortedMap_Second_Down() {
        final Map<String, Integer> map = new LinkedHashMap<>(3);
        map.put("1", 1);
        map.put("2", 2);
        map.put("3", 3);
        map.put("4", 4);
        CollectionUtil.moveEntryInInsertSortedMap(map, "2", true);
        Assert.assertEquals(Arrays.asList("1", "3", "2", "4"), new ArrayList<>(map.keySet()));
    }

    /** Test: for moveEntryInInsertSortedMap method (move middle entry up) */
    @Test
    public void testMoveEntryInInsertSortedMap_Mid_Up() {
        final Map<String, Integer> map = new LinkedHashMap<>(3);
        map.put("1", 1);
        map.put("2", 2);
        map.put("3", 3);
        map.put("4", 4);
        map.put("5", 5);
        CollectionUtil.moveEntryInInsertSortedMap(map, "3", false);
        Assert.assertEquals(Arrays.asList("1", "3", "2", "4", "5"), new ArrayList<>(map.keySet()));
    }

    /** Test: for moveEntryInInsertSortedMap method (move middle entry down) */
    @Test
    public void testMoveEntryInInsertSortedMap_Mid_Down() {
        final Map<String, Integer> map = new LinkedHashMap<>(3);
        map.put("1", 1);
        map.put("2", 2);
        map.put("3", 3);
        map.put("4", 4);
        map.put("5", 5);
        CollectionUtil.moveEntryInInsertSortedMap(map, "3", true);
        Assert.assertEquals(Arrays.asList("1", "2", "4", "3", "5"), new ArrayList<>(map.keySet()));
    }

    /** Test: for moveEntryInInsertSortedMap method (move second to last entry up) */
    @Test
    public void testMoveEntryInInsertSortedMap_SecondToLast_Up() {
        final Map<String, Integer> map = new LinkedHashMap<>(3);
        map.put("1", 1);
        map.put("2", 2);
        map.put("3", 3);
        map.put("4", 4);
        CollectionUtil.moveEntryInInsertSortedMap(map, "3", false);
        Assert.assertEquals(Arrays.asList("1", "3", "2", "4"), new ArrayList<>(map.keySet()));
    }

    /** Test: for moveEntryInInsertSortedMap method (move second to last entry down) */
    @Test
    public void testMoveEntryInInsertSortedMap_SecondToLast_Down() {
        final Map<String, Integer> map = new LinkedHashMap<>(3);
        map.put("1", 1);
        map.put("2", 2);
        map.put("3", 3);
        map.put("4", 4);
        CollectionUtil.moveEntryInInsertSortedMap(map, "3", true);
        Assert.assertEquals(Arrays.asList("1", "2", "4", "3"), new ArrayList<>(map.keySet()));
    }

    /** Test: for moveEntryInInsertSortedMap method (move first entry up) */
    @Test
    public void testMoveEntryInInsertSortedMap_Last_Up() {
        final Map<String, Integer> map = new LinkedHashMap<>(3);
        map.put("1", 1);
        map.put("2", 2);
        map.put("3", 3);
        CollectionUtil.moveEntryInInsertSortedMap(map, "3", false);
        Assert.assertEquals(Arrays.asList("1", "3", "2"), new ArrayList<>(map.keySet()));
    }

    /** Test: for moveEntryInInsertSortedMap method (expect failure: move last entry down) */
    @Test(expected = IllegalArgumentException.class)
    public void testMoveEntryInInsertSortedMap_Last_Down() {
        final Map<String, Integer> map = new LinkedHashMap<>(3);
        map.put("1", 1);
        map.put("2", 2);
        CollectionUtil.moveEntryInInsertSortedMap(map, "2", true);
    }
    
    /** Test: for toString method (collection == null) */
    @Test
    public void testToString_NullCollection() {
        Assert.assertEquals("", CollectionUtil.toString(null, "."));
    }
    
    /** Test: for toString method (collection.isEmpty() == true) */
    @Test
    public void testToString_EmptyList() {
        Assert.assertEquals("", CollectionUtil.toString(Collections.emptyList(), "."));
    }
    
    /** Test: for toString method (separator == null) */
    @Test
    public void testToString_NullSeparator() {
        final List<String> twoElementList = Arrays.asList("1", "2");
        Assert.assertEquals(twoElementList.toString(), CollectionUtil.toString(twoElementList, null));
    }
    
    /** Test: for toString method (collection.size() == 1) */
    @Test
    public void testToString_SingletonList() {
        final List<String> oneElementList = Collections.singletonList("1");
        Assert.assertEquals("1", CollectionUtil.toString(oneElementList, "."));
    }
    
    /** Test: for toString method (collection.size() == 2) */
    @Test
    public void testToString_List() {
        final List<String> twoElementList = Arrays.asList("1", "2");
        Assert.assertEquals("1#_#2", CollectionUtil.toString(twoElementList, "#_#"));
    }
    
    /** Test: for toString method (collection.size() == 4 && collection[1] == null) */
    @Test
    public void testToString_ListWithNullElement() {
        final List<String> twoElementList = Arrays.asList("1", null, "3", "4");
        Assert.assertEquals("1--3-4", CollectionUtil.toString(twoElementList, "-"));
    }
}
