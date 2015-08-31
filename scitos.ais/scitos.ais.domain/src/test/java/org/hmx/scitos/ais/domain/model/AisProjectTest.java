package org.hmx.scitos.ais.domain.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for the {@link AisProject} class.
 */
public class AisProjectTest {

    /** Test: for getGroupKey method. */
    @Test
    public void testGetGroupKey() {
        final AisProject project = new AisProject("", Arrays.asList(new DetailCategory(null, "x", "", true, null, null)));
        Assert.assertEquals("a", project.getGroupKey(new Interview("a", 1)));
    }

    /** Test: for getSubModelObjects method. */
    @Test
    public void testGetSubModelObjects() {
        final AisProject project = new AisProject("", Arrays.asList(new DetailCategory(null, "x", "", true, null, null)));
        final Interview modelA1 = new Interview("a", 1);
        final Interview modelA2 = new Interview("a", 2);
        final Interview modelB = new Interview("b", 1);
        final Interview modelC = new Interview("c", 1);
        project.setInterviews(Arrays.asList(modelB, modelA2, modelC, modelA1));
        final Map<String, List<Interview>> actual = project.getSubModelObjects();
        Assert.assertEquals(3, actual.size());
        Assert.assertEquals(Arrays.asList(modelA1, modelA2), actual.get("a"));
        Assert.assertEquals(Arrays.asList(modelB), actual.get("b"));
        Assert.assertEquals(Arrays.asList(modelC), actual.get("c"));
    }

    /** Test: for provide method (flat model). */
    @Test
    public void testProvide_1() {
        final List<DetailCategory> flatCategories =
                Arrays.asList(new DetailCategory(null, "a", "", true, null, null), new DetailCategory(null, "b", "", true, null, null));
        final AisProject project = new AisProject("", flatCategories);
        Assert.assertEquals(flatCategories, project.provide());
    }

    /** Test: for provide method (hierarchical model). */
    @Test
    public void testProvide_2() {
        final DetailCategory parent = new DetailCategory(null, "ab", "", false, null, null);
        final DetailCategory childA = new DetailCategory(parent, "a", "", true, null, null);
        final DetailCategory childB = new DetailCategory(parent, "b", "", true, null, null);
        final DetailCategory standalone = new DetailCategory(null, "c", "", true, null, null);
        final List<DetailCategory> categories = Arrays.asList(parent, childA, childB, standalone);
        final AisProject project = new AisProject("", categories);
        Assert.assertEquals(categories, project.provide());
    }

    /** Test: for provideSelectables method (flat model). */
    @Test
    public void testProvideSelectables_1() {
        final List<DetailCategory> flatCategories =
                Arrays.asList(new DetailCategory(null, "a", "", true, null, null), new DetailCategory(null, "b", "", true, null, null));
        final AisProject project = new AisProject("", flatCategories);
        Assert.assertEquals(flatCategories, project.provideSelectables());
    }

    /** Test: for provideSelectables method (hierarchical model). */
    @Test
    public void testProvideSelectables_2() {
        final DetailCategory parent = new DetailCategory(null, "ab", "", false, null, null);
        final DetailCategory childA = new DetailCategory(parent, "a", "", true, null, null);
        final DetailCategory childB = new DetailCategory(parent, "b", "", true, null, null);
        final DetailCategory standalone = new DetailCategory(null, "c", "", true, null, null);
        final AisProject project = new AisProject("", Arrays.asList(parent, childA, childB, standalone));
        Assert.assertEquals(Arrays.asList(childA, childB, standalone), project.provideSelectables());
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
        final AisProject project = new AisProject("", Arrays.asList(parent, childA, subParentB, childB1, childB2, standalone));
        Assert.assertEquals(Arrays.asList(childA, childB1, childB2, standalone), project.provideSelectables());
    }
}
