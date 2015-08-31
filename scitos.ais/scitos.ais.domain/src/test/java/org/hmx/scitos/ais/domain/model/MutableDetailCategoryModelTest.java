package org.hmx.scitos.ais.domain.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for the {@link MutableDetailCategoryModel} class.
 */
public class MutableDetailCategoryModelTest {

    /** The model instance being tested. */
    private MutableDetailCategoryModel model;

    /** Preparation before each test: initialize a new model instance to test. */
    @Before
    public void initModel() {
        this.model = new MutableDetailCategoryModel();
    }

    /** Test: for provide method (flat model). */
    @Test
    public void testProvide_1() {
        final List<DetailCategory> flatCategories =
                Arrays.asList(new DetailCategory(null, "a", "", true, null, null), new DetailCategory(null, "b", "", true, null, null));
        this.model.addAll(flatCategories);
        Assert.assertEquals(flatCategories, this.model.provide());
    }

    /** Test: for provide method (hierarchical model). */
    @Test
    public void testProvide_2() {
        final DetailCategory parent = new DetailCategory(null, "ab", "", false, null, null);
        final DetailCategory childA = new DetailCategory(parent, "a", "", true, null, null);
        final DetailCategory childB = new DetailCategory(parent, "b", "", true, null, null);
        final DetailCategory standalone = new DetailCategory(null, "c", "", true, null, null);
        final List<DetailCategory> categories = Arrays.asList(parent, childA, childB, standalone);
        this.model.addAll(categories);
        Assert.assertEquals(categories, this.model.provide());
    }

    /** Test: for provideSelectables method (flat model). */
    @Test
    public void testProvideSelectables_1() {
        final List<DetailCategory> flatCategories =
                Arrays.asList(new DetailCategory(null, "a", "", true, null, null), new DetailCategory(null, "b", "", true, null, null));
        this.model.addAll(flatCategories);
        Assert.assertEquals(flatCategories, this.model.provideSelectables());
    }

    /** Test: for provideSelectables method (hierarchical model). */
    @Test
    public void testProvideSelectables_2() {
        final DetailCategory parent = new DetailCategory(null, "ab", "", false, null, null);
        final DetailCategory childA = new DetailCategory(parent, "a", "", true, null, null);
        final DetailCategory childB = new DetailCategory(parent, "b", "", true, null, null);
        final DetailCategory standalone = new DetailCategory(null, "c", "", true, null, null);
        this.model.addAll(Arrays.asList(parent, childA, childB, standalone));
        Assert.assertEquals(Arrays.asList(childA, childB, standalone), this.model.provideSelectables());
    }

    /** Test: for provideSelectables method (multi-level hierarchical model). */
    @Test
    public void testProvideSelectables_3() {
        final DetailCategory parent = new DetailCategory(null, "ab", "", false, null, null);
        final DetailCategory childA = new DetailCategory(parent, "a", "", true, null, null);
        final DetailCategory subParentB = new DetailCategory(parent, "b", "", false, null, null);
        final DetailCategory childB1 = new DetailCategory(subParentB, "b1", "", true, null, null);
        final DetailCategory childB2 = new DetailCategory(subParentB, "b2", "", true, null, null);
        final DetailCategory standalone = new DetailCategory(null, "c", "", true, null, null);
        this.model.addAll(Arrays.asList(parent, childA, subParentB, childB1, childB2, standalone));
        Assert.assertEquals(Arrays.asList(childA, childB1, childB2, standalone), this.model.provideSelectables());
    }

    /** Test: for getRootCategories method (flat model). */
    @Test
    public void testGetRootCategories_1() {
        final List<DetailCategory> flatCategories =
                Arrays.asList(new DetailCategory(null, "a", "", true, null, null), new DetailCategory(null, "b", "", true, null, null));
        this.model.addAll(flatCategories);
        Assert.assertEquals(flatCategories, this.model.getRootCategories());
    }

    /** Test: for getRootCategories method (hierarchical model). */
    @Test
    public void testGetRootCategories_2() {
        final DetailCategory parent = new DetailCategory(null, "ab", "", false, null, null);
        final DetailCategory childA = new DetailCategory(parent, "a", "", true, null, null);
        final DetailCategory childB = new DetailCategory(parent, "b", "", true, null, null);
        final DetailCategory standalone = new DetailCategory(null, "c", "", true, null, null);
        this.model.addAll(Arrays.asList(parent, childA, childB, standalone));
        Assert.assertEquals(Arrays.asList(parent, standalone), this.model.getRootCategories());
    }

    /** Test: for getRootCategories method (complex hierarchical model). */
    @Test
    public void testGetRootCategories_3() {
        final DetailCategory parent = new DetailCategory(null, "ab", "", false, null, null);
        final DetailCategory childA = new DetailCategory(parent, "a", "", true, null, null);
        final DetailCategory subParentB = new DetailCategory(parent, "b", "", false, null, null);
        final DetailCategory childB1 = new DetailCategory(subParentB, "b1", "", true, null, null);
        final DetailCategory childB2 = new DetailCategory(subParentB, "b2", "", true, null, null);
        final DetailCategory standalone = new DetailCategory(null, "c", "", true, null, null);
        this.model.addAll(Arrays.asList(parent, childA, subParentB, childB1, childB2, standalone));
        Assert.assertEquals(Arrays.asList(parent, standalone), this.model.getRootCategories());
    }

    /** Test: for getChildCategories method. */
    @Test
    public void testGetChildCategories() {
        final DetailCategory parent = new DetailCategory(null, "ab", "", false, null, null);
        final DetailCategory childA = new DetailCategory(parent, "a", "", true, null, null);
        final DetailCategory subParentB = new DetailCategory(parent, "b", "", false, null, null);
        final DetailCategory childB1 = new DetailCategory(subParentB, "b1", "", true, null, null);
        final DetailCategory childB2 = new DetailCategory(subParentB, "b2", "", true, null, null);
        final DetailCategory standalone = new DetailCategory(null, "c", "", true, null, null);
        this.model.addAll(Arrays.asList(parent, childA, subParentB, childB1, childB2, standalone));
        Assert.assertEquals(Arrays.asList(parent, standalone), this.model.getChildCategories(null));
        Assert.assertEquals(Arrays.asList(childA, subParentB), this.model.getChildCategories(parent));
        Assert.assertEquals(Collections.emptyList(), this.model.getChildCategories(childA));
        Assert.assertEquals(Arrays.asList(childB1, childB2), this.model.getChildCategories(subParentB));
        Assert.assertEquals(Collections.emptyList(), this.model.getChildCategories(childB1));
        Assert.assertEquals(Collections.emptyList(), this.model.getChildCategories(childB2));
        Assert.assertEquals(Collections.emptyList(), this.model.getChildCategories(standalone));
    }

    /** Test: for getDetailByCode method. */
    @Test
    public void testGetDetailByCode() {
        final DetailCategory parent = new DetailCategory(null, "ab", "", false, null, null);
        final DetailCategory childA = new DetailCategory(parent, "a", "", true, null, null);
        final DetailCategory subParentB = new DetailCategory(parent, "b", "", false, null, null);
        final DetailCategory childB1 = new DetailCategory(subParentB, "b1", "", true, null, null);
        final DetailCategory childB2 = new DetailCategory(subParentB, "b2", "", true, null, null);
        final DetailCategory standalone = new DetailCategory(null, "c", "", true, null, null);
        this.model.addAll(Arrays.asList(parent, childA, subParentB, childB1, childB2, standalone));
        Assert.assertSame(parent, this.model.getDetailByCode("ab"));
        Assert.assertSame(childA, this.model.getDetailByCode("a"));
        Assert.assertSame(subParentB, this.model.getDetailByCode("b"));
        Assert.assertSame(childB1, this.model.getDetailByCode("b1"));
        Assert.assertSame(childB2, this.model.getDetailByCode("b2"));
        Assert.assertSame(standalone, this.model.getDetailByCode("c"));
    }

}
