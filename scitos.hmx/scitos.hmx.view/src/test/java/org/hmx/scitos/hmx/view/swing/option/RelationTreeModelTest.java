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

package org.hmx.scitos.hmx.view.swing.option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.tree.TreePath;

import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.domain.ISemanticalRelationProvider;
import org.hmx.scitos.hmx.domain.model.RelationTemplate;
import org.hmx.scitos.hmx.domain.model.RelationTemplate.AssociateRole;
import org.hmx.scitos.hmx.view.swing.option.RelationTreeModel;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the {@link RelationTreeModel} class.
 */
public class RelationTreeModelTest {

    /**
     * The default relation group provider for creating the {@link RelationTreeModel} instance to test.
     */
    private static ISemanticalRelationProvider relationProvider;
    /**
     * The relation tree model instance to test.
     */
    private RelationTreeModel model;

    /**
     * Initialize the default relation group provider for creating the {@link RelationTreeModel} instance to test.
     */
    @BeforeClass
    public static void initRelationProvider() {
        // build the relation template groups to test
        final List<List<RelationTemplate>> providedModel = new ArrayList<List<RelationTemplate>>(3);
        /* first group: one relation template with all the same high weight roles */
        final AssociateRole singleRoleA1 = new AssociateRole("a1", true);
        final RelationTemplate templateA1 = new RelationTemplate(singleRoleA1, singleRoleA1, singleRoleA1, "a comment");
        final AssociateRole singleRoleA2 = new AssociateRole("a2", true);
        final RelationTemplate templateA2 = new RelationTemplate(singleRoleA2, singleRoleA2, singleRoleA2, "");
        providedModel.add(Arrays.asList(templateA1, templateA2));
        /* second group: empty */
        providedModel.add(Collections.<RelationTemplate>emptyList());
        /* third group: four relation templates with differing roles (i.e. high and low weight roles) */
        // third group - template one: simple high-low template
        final RelationTemplate templateC1 =
                new RelationTemplate(new AssociateRole("c1 high", true), null, new AssociateRole("c1 low", false), "c1");
        // third group - template two: simple low-high template
        final RelationTemplate templateC2 =
                new RelationTemplate(new AssociateRole("c2 low", false), null, new AssociateRole("c2 high", true), "c2");
        // third group - template three: high-low-low... template
        final AssociateRole repeatedRoleC3 = new AssociateRole("c3 low", false);
        final RelationTemplate templateC3 = new RelationTemplate(new AssociateRole("c3 high", true), repeatedRoleC3, repeatedRoleC3, "c3");
        // third group - template four: low-low...-high template (with null comment)
        final AssociateRole repeatedRoleC4 = new AssociateRole("c4 low", false);
        final RelationTemplate templateC4 = new RelationTemplate(repeatedRoleC4, repeatedRoleC4, new AssociateRole("c4 high", true), null);
        providedModel.add(Arrays.asList(templateC1, templateC2, templateC3, templateC4));

        // build model to test with the prepared relation template groups
        RelationTreeModelTest.relationProvider = new ISemanticalRelationProvider() {

            @Override
            public List<List<RelationTemplate>> provideRelationTemplates() {
                return Collections.unmodifiableList(providedModel);
            }
        };
    }

    /** Clear the no longer needed relation provider. */
    @AfterClass
    public static void clearRelationProvider() {
        RelationTreeModelTest.relationProvider = null;
    }

    /** Initialize the relation tree model instance to test. */
    @Before
    public void initRelationTreeModel() {
        this.model = new RelationTreeModel(RelationTreeModelTest.relationProvider);
    }

    /**
     * Test of the {@link RelationTreeModel#provideRelationTemplates()} method.
     */
    @Test
    public void testProvideRelationTemplates() {
        final List<List<RelationTemplate>> expectedTemplates = RelationTreeModelTest.relationProvider.provideRelationTemplates();
        final List<List<RelationTemplate>> actualTemplates = this.model.provideRelationTemplates();
        // ensure the RelationModel's provideRelationTemplates() method returns an equal copy of the prepared relation template groups
        Assert.assertNotSame(expectedTemplates, actualTemplates);
        Assert.assertEquals(expectedTemplates, actualTemplates);
    }

    /**
     * Test of the {@link RelationTreeModel#getChildCount(Object)} method.
     */
    @Test
    public void testGetChildCount() {
        final Object root = this.model.getRoot();
        Assert.assertSame(3, this.model.getChildCount(root));

        final Object groupNode1 = this.model.getChild(root, 0);
        final Object groupNode2 = this.model.getChild(root, 1);
        final Object groupNode3 = this.model.getChild(root, 2);
        Assert.assertSame(2, this.model.getChildCount(groupNode1));
        Assert.assertSame(0, this.model.getChildCount(groupNode2));
        Assert.assertSame(4, this.model.getChildCount(groupNode3));
    }

    /**
     * Test of the {@link RelationTreeModel#isLeaf(Object)} method.
     */
    @Test
    public void testIsLeaf() {
        final Object root = this.model.getRoot();
        Assert.assertFalse(this.model.isLeaf(root));

        final Object groupNode1 = this.model.getChild(root, 0);
        final Object groupNode2 = this.model.getChild(root, 1);
        final Object groupNode3 = this.model.getChild(root, 2);
        Assert.assertFalse(this.model.isLeaf(groupNode1));
        Assert.assertTrue(this.model.isLeaf(groupNode2));
        Assert.assertFalse(this.model.isLeaf(groupNode3));

        final Object childNodeA1 = this.model.getChild(groupNode1, 0);
        final Object childNodeA2 = this.model.getChild(groupNode1, 1);
        final Object childNodeC1 = this.model.getChild(groupNode3, 0);
        final Object childNodeC2 = this.model.getChild(groupNode3, 1);
        final Object childNodeC3 = this.model.getChild(groupNode3, 2);
        final Object childNodeC4 = this.model.getChild(groupNode3, 3);
        Assert.assertTrue(this.model.isLeaf(childNodeA1));
        Assert.assertTrue(this.model.isLeaf(childNodeA2));
        Assert.assertTrue(this.model.isLeaf(childNodeC1));
        Assert.assertTrue(this.model.isLeaf(childNodeC2));
        Assert.assertTrue(this.model.isLeaf(childNodeC3));
        Assert.assertTrue(this.model.isLeaf(childNodeC4));
    }

    /**
     * Test of the {@link RelationTreeModel#isLeaf(Object)} method for an empty model.
     */
    @Test
    public void testIsLeaf_emptyModel() {
        final RelationTreeModel emptyModel = new RelationTreeModel(new ISemanticalRelationProvider() {

            @Override
            public List<List<RelationTemplate>> provideRelationTemplates() {
                return Collections.emptyList();
            }
        });
        final Object root = emptyModel.getRoot();
        Assert.assertTrue(emptyModel.isLeaf(root));
    }

    /**
     * Test of the {@link RelationTreeModel#getIndexOfChild(Object, Object)} method.
     */
    @Test
    public void testGetIndexOfChild() {
        final Object root = this.model.getRoot();
        final Object groupNode1 = this.model.getChild(root, 0);
        final Object groupNode2 = this.model.getChild(root, 1);
        final Object groupNode3 = this.model.getChild(root, 2);
        Assert.assertSame(0, this.model.getIndexOfChild(root, groupNode1));
        Assert.assertSame(1, this.model.getIndexOfChild(root, groupNode2));
        Assert.assertSame(2, this.model.getIndexOfChild(root, groupNode3));

        final Object childNodeA1 = this.model.getChild(groupNode1, 0);
        final Object childNodeA2 = this.model.getChild(groupNode1, 1);
        final Object childNodeC1 = this.model.getChild(groupNode3, 0);
        final Object childNodeC2 = this.model.getChild(groupNode3, 1);
        final Object childNodeC3 = this.model.getChild(groupNode3, 2);
        final Object childNodeC4 = this.model.getChild(groupNode3, 3);
        Assert.assertSame(0, this.model.getIndexOfChild(groupNode1, childNodeA1));
        Assert.assertSame(1, this.model.getIndexOfChild(groupNode1, childNodeA2));
        Assert.assertSame(0, this.model.getIndexOfChild(groupNode3, childNodeC1));
        Assert.assertSame(1, this.model.getIndexOfChild(groupNode3, childNodeC2));
        Assert.assertSame(2, this.model.getIndexOfChild(groupNode3, childNodeC3));
        Assert.assertSame(3, this.model.getIndexOfChild(groupNode3, childNodeC4));
    }

    /**
     * Test of the {@link RelationTreeModel#isCellEditable(Object, int)} method.
     */
    @Test
    public void testIsCellEditable() {
        final Object root = this.model.getRoot();
        Assert.assertFalse(this.model.isCellEditable(root, 0));
        Assert.assertFalse(this.model.isCellEditable(root, 1));
        Assert.assertFalse(this.model.isCellEditable(root, 2));
        Assert.assertFalse(this.model.isCellEditable(root, 3));
        Assert.assertFalse(this.model.isCellEditable(root, 4));
        Assert.assertFalse(this.model.isCellEditable(root, 5));

        final Object groupNode1 = this.model.getChild(root, 0);
        Assert.assertFalse(this.model.isCellEditable(groupNode1, 0));
        Assert.assertFalse(this.model.isCellEditable(groupNode1, 1));
        Assert.assertTrue(this.model.isCellEditable(groupNode1, 2));
        Assert.assertFalse(this.model.isCellEditable(groupNode1, 3));
        Assert.assertTrue(this.model.isCellEditable(groupNode1, 4));
        Assert.assertTrue(this.model.isCellEditable(groupNode1, 5));

        final Object groupNode2 = this.model.getChild(root, 1);
        Assert.assertFalse(this.model.isCellEditable(groupNode2, 0));
        Assert.assertFalse(this.model.isCellEditable(groupNode2, 1));
        Assert.assertTrue(this.model.isCellEditable(groupNode2, 2));
        Assert.assertTrue(this.model.isCellEditable(groupNode2, 3));
        Assert.assertTrue(this.model.isCellEditable(groupNode2, 4));
        Assert.assertTrue(this.model.isCellEditable(groupNode2, 5));

        final Object groupNode3 = this.model.getChild(root, 2);
        Assert.assertFalse(this.model.isCellEditable(groupNode3, 0));
        Assert.assertFalse(this.model.isCellEditable(groupNode3, 1));
        Assert.assertTrue(this.model.isCellEditable(groupNode3, 2));
        Assert.assertTrue(this.model.isCellEditable(groupNode3, 3));
        Assert.assertFalse(this.model.isCellEditable(groupNode3, 4));
        Assert.assertTrue(this.model.isCellEditable(groupNode3, 5));

        final Object childNodeC1 = this.model.getChild(groupNode3, 0);
        Assert.assertFalse(this.model.isCellEditable(childNodeC1, 0));
        Assert.assertFalse(this.model.isCellEditable(childNodeC1, 1));
        Assert.assertFalse(this.model.isCellEditable(childNodeC1, 2));
        Assert.assertFalse(this.model.isCellEditable(childNodeC1, 3));
        Assert.assertTrue(this.model.isCellEditable(childNodeC1, 4));
        Assert.assertTrue(this.model.isCellEditable(childNodeC1, 5));

        final Object childNodeC2 = this.model.getChild(groupNode3, 1);
        Assert.assertFalse(this.model.isCellEditable(childNodeC2, 0));
        Assert.assertFalse(this.model.isCellEditable(childNodeC2, 1));
        Assert.assertFalse(this.model.isCellEditable(childNodeC2, 2));
        Assert.assertTrue(this.model.isCellEditable(childNodeC2, 3));
        Assert.assertTrue(this.model.isCellEditable(childNodeC2, 4));
        Assert.assertTrue(this.model.isCellEditable(childNodeC2, 5));

        final Object childNodeC4 = this.model.getChild(groupNode3, 3);
        Assert.assertFalse(this.model.isCellEditable(childNodeC4, 0));
        Assert.assertFalse(this.model.isCellEditable(childNodeC4, 1));
        Assert.assertFalse(this.model.isCellEditable(childNodeC4, 2));
        Assert.assertTrue(this.model.isCellEditable(childNodeC4, 3));
        Assert.assertFalse(this.model.isCellEditable(childNodeC4, 4));
        Assert.assertTrue(this.model.isCellEditable(childNodeC4, 5));
    }

    /**
     * Test of the {@link RelationTreeModel#getValueAt(Object, int)} method.
     */
    @Test
    public void testGetValueAt_Column1() {
        final Object root = this.model.getRoot();
        Assert.assertNull(this.model.getValueAt(root, 0));

        final Object groupNode1 = this.model.getChild(root, 0);
        final Object groupNode3 = this.model.getChild(root, 2);
        Assert.assertEquals("1. ", this.model.getValueAt(groupNode1, 0));
        Assert.assertEquals("3. ", this.model.getValueAt(groupNode3, 0));

        final Object childNodeC1 = this.model.getChild(groupNode3, 0);
        final Object childNodeC4 = this.model.getChild(groupNode3, 3);
        Assert.assertEquals("3.1. ", this.model.getValueAt(childNodeC1, 0));
        Assert.assertEquals("3.4. ", this.model.getValueAt(childNodeC4, 0));
    }

    /**
     * Test of the {@link RelationTreeModel#getValueAt(Object, int)} method.
     */
    @Test
    public void testGetValueAt_Column2() {
        final Object root = this.model.getRoot();
        Assert.assertNull(this.model.getValueAt(root, 1));

        final Object groupNode1 = this.model.getChild(root, 0);
        final Object groupNode3 = this.model.getChild(root, 2);
        Assert.assertEquals("", this.model.getValueAt(groupNode1, 1));
        Assert.assertEquals("", this.model.getValueAt(groupNode3, 1));

        final Object childNodeC1 = this.model.getChild(groupNode3, 0);
        final Object childNodeC4 = this.model.getChild(groupNode3, 3);
        Assert.assertEquals("C1 HIGH - c1 low", this.model.getValueAt(childNodeC1, 1));
        Assert.assertEquals("c4 low1 - ... - C4 HIGH", this.model.getValueAt(childNodeC4, 1));
    }

    /**
     * Test of the {@link RelationTreeModel#getValueAt(Object, int)} method.
     */
    @Test
    public void testGetValueAt_Column3() {
        final Object root = this.model.getRoot();
        Assert.assertNull(this.model.getValueAt(root, 2));

        final Object groupNode1 = this.model.getChild(root, 0);
        final Object groupNode3 = this.model.getChild(root, 2);
        Assert.assertEquals(HmxMessage.PREFERENCES_RELATION_ADD.get(), this.model.getValueAt(groupNode1, 2));
        Assert.assertEquals(HmxMessage.PREFERENCES_RELATION_ADD.get(), this.model.getValueAt(groupNode3, 2));

        final Object childNodeC1 = this.model.getChild(groupNode3, 0);
        final Object childNodeC4 = this.model.getChild(groupNode3, 3);
        Assert.assertNull(this.model.getValueAt(childNodeC1, 2));
        Assert.assertNull(this.model.getValueAt(childNodeC4, 2));
    }

    /**
     * Test of the {@link RelationTreeModel#getValueAt(Object, int)} method.
     */
    @Test
    public void testGetValueAt_Column4() {
        final Object root = this.model.getRoot();
        Assert.assertNull(this.model.getValueAt(root, 3));

        final Object groupNode1 = this.model.getChild(root, 0);
        final Object groupNode3 = this.model.getChild(root, 2);
        Assert.assertNull(this.model.getValueAt(groupNode1, 3));
        Assert.assertEquals(HmxMessage.PREFERENCES_RELATION_MOVE_UP.get(), this.model.getValueAt(groupNode3, 3));

        final Object childNodeC1 = this.model.getChild(groupNode3, 0);
        final Object childNodeC4 = this.model.getChild(groupNode3, 3);
        Assert.assertNull(this.model.getValueAt(childNodeC1, 3));
        Assert.assertEquals(HmxMessage.PREFERENCES_RELATION_MOVE_UP.get(), this.model.getValueAt(childNodeC4, 3));
    }

    /**
     * Test of the {@link RelationTreeModel#getValueAt(Object, int)} method.
     */
    @Test
    public void testGetValueAt_Column5() {
        final Object root = this.model.getRoot();
        Assert.assertNull(this.model.getValueAt(root, 4));

        final Object groupNode1 = this.model.getChild(root, 0);
        final Object groupNode3 = this.model.getChild(root, 2);
        Assert.assertEquals(HmxMessage.PREFERENCES_RELATION_MOVE_DOWN.get(), this.model.getValueAt(groupNode1, 4));
        Assert.assertNull(this.model.getValueAt(groupNode3, 4));

        final Object childNodeC1 = this.model.getChild(groupNode3, 0);
        final Object childNodeC4 = this.model.getChild(groupNode3, 3);
        Assert.assertEquals(HmxMessage.PREFERENCES_RELATION_MOVE_DOWN.get(), this.model.getValueAt(childNodeC1, 4));
        Assert.assertNull(this.model.getValueAt(childNodeC4, 4));
    }

    /**
     * Test of the {@link RelationTreeModel#getValueAt(Object, int)} method.
     */
    @Test
    public void testGetValueAt_Column6() {
        final Object root = this.model.getRoot();
        Assert.assertNull(this.model.getValueAt(root, 5));

        final Object groupNode1 = this.model.getChild(root, 0);
        final Object groupNode3 = this.model.getChild(root, 2);
        Assert.assertEquals(HmxMessage.PREFERENCES_RELATION_REMOVE_GROUP.get(), this.model.getValueAt(groupNode1, 5));
        Assert.assertEquals(HmxMessage.PREFERENCES_RELATION_REMOVE_GROUP.get(), this.model.getValueAt(groupNode3, 5));

        final Object childNodeC1 = this.model.getChild(groupNode3, 0);
        final Object childNodeC4 = this.model.getChild(groupNode3, 3);
        Assert.assertEquals(HmxMessage.PREFERENCES_RELATION_REMOVE.get(), this.model.getValueAt(childNodeC1, 5));
        Assert.assertEquals(HmxMessage.PREFERENCES_RELATION_REMOVE.get(), this.model.getValueAt(childNodeC4, 5));
    }

    /**
     * Test of the {@link RelationTreeModel#getValueAt(Object, int)} method.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAt_Column7() {
        final Object root = this.model.getRoot();
        final Object groupNode1 = this.model.getChild(root, 0);

        Assert.assertEquals(6, this.model.getColumnCount());
        this.model.getValueAt(groupNode1, 6);
    }

    /**
     * Test of the {@link RelationTreeModel#addGroupEntry()} method.
     */
    @Test
    public void testAddGroupEntry() {
        final TreePath newEntry = this.model.addGroupEntry();
        final Object root = this.model.getRoot();
        Assert.assertEquals(4, this.model.getChildCount(root));
        final Object newGroupNode = this.model.getChild(root, 3);
        Assert.assertEquals(new TreePath(new Object[] { root, newGroupNode }), newEntry);
    }

    /**
     * Test of the {@link RelationTreeModel#getTemplateAtPath(TreePath)} method.
     */
    @Test
    public void testGetTemplateAtPath() {
        final Object root = this.model.getRoot();
        final Object groupNode3 = this.model.getChild(root, 2);
        final Object templateNodeC2 = this.model.getChild(groupNode3, 1);
        final RelationTemplate expectedTemplateC2 =
                new RelationTemplate(new AssociateRole("c2 low", false), null, new AssociateRole("c2 high", true), "c2");
        final RelationTemplate returnedTemplateC2 = this.model.getTemplateAtPath(new TreePath(new Object[] { root, groupNode3, templateNodeC2 }));
        Assert.assertEquals(expectedTemplateC2, returnedTemplateC2);
    }

    /**
     * Test of the {@link RelationTreeModel#addTemplateEntryToGroup(TreePath)} method.
     */
    @Test
    public void testAddTemplateEntryToGroup() {
        final Object root = this.model.getRoot();
        final Object groupNode1 = this.model.getChild(root, 0);
        final TreePath groupPath = new TreePath(new Object[] { root, groupNode1 });

        final AssociateRole defaultHighWeight = new AssociateRole(HmxMessage.PREFERENCES_RELATION_DEFAULTROLE_HIGHWEIGHT.get(), true);
        final AssociateRole defaultLowWeight = new AssociateRole(HmxMessage.PREFERENCES_RELATION_DEFAULTROLE_LOWWEIGHT.get(), false);
        final RelationTemplate defaultTemplate = new RelationTemplate(defaultHighWeight, null, defaultLowWeight, "");

        final TreePath newEntryPath = this.model.addTemplateEntryToGroup(groupPath);
        Assert.assertEquals(groupPath, newEntryPath.getParentPath());
        Assert.assertEquals(defaultTemplate, this.model.getTemplateAtPath(newEntryPath));
    }

    /**
     * Test of the {@link RelationTreeModel#updateTemplateEntry(TreePath, RelationTemplate)} method.
     */
    @Test
    public void testUpdateTemplateEntry() {
        final Object root = this.model.getRoot();
        final Object groupNode1 = this.model.getChild(root, 0);
        final TreePath targetPath = new TreePath(new Object[] { root, groupNode1 });
        final RelationTemplate newState = new RelationTemplate(new AssociateRole("1", true), null, new AssociateRole("2", false), "x");

        this.model.updateTemplateEntry(targetPath, newState);
        Assert.assertEquals(newState, this.model.getTemplateAtPath(targetPath));
    }

    /**
     * Test of the {@link RelationTreeModel#moveEntry(TreePath, boolean)} method.
     */
    @Test
    public void testMoveEntry_GroupDown() {
        final Object root = this.model.getRoot();
        final Object groupNode2 = this.model.getChild(root, 1);
        final Object groupNode3 = this.model.getChild(root, 2);
        this.model.moveEntry(new TreePath(new Object[] { root, groupNode2 }), true);
        Assert.assertEquals(groupNode3, this.model.getChild(root, 1));
        Assert.assertEquals(groupNode2, this.model.getChild(root, 2));
    }

    /**
     * Test of the {@link RelationTreeModel#moveEntry(TreePath, boolean)} method.
     */
    @Test
    public void testMoveEntry_GroupUp() {
        final Object root = this.model.getRoot();
        final Object groupNode1 = this.model.getChild(root, 0);
        final Object groupNode2 = this.model.getChild(root, 1);
        this.model.moveEntry(new TreePath(new Object[] { root, groupNode2 }), false);
        Assert.assertEquals(groupNode2, this.model.getChild(root, 0));
        Assert.assertEquals(groupNode1, this.model.getChild(root, 1));
    }

    /**
     * Test of the {@link RelationTreeModel#moveEntry(TreePath, boolean)} method.
     */
    @Test
    public void testMoveEntry_TemplateDown() {
        final Object root = this.model.getRoot();
        final Object groupNode1 = this.model.getChild(root, 0);
        final Object templateNode1 = this.model.getChild(groupNode1, 0);
        final Object templateNode2 = this.model.getChild(groupNode1, 1);
        this.model.moveEntry(new TreePath(new Object[] { root, groupNode1, templateNode1 }), true);
        Assert.assertEquals(templateNode2, this.model.getChild(groupNode1, 0));
        Assert.assertEquals(templateNode1, this.model.getChild(groupNode1, 1));
    }

    /**
     * Test of the {@link RelationTreeModel#moveEntry(TreePath, boolean)} method.
     */
    @Test
    public void testMoveEntry_TemplateUp() {
        final Object root = this.model.getRoot();
        final Object groupNode1 = this.model.getChild(root, 0);
        final Object templateNode1 = this.model.getChild(groupNode1, 0);
        final Object templateNode2 = this.model.getChild(groupNode1, 1);
        this.model.moveEntry(new TreePath(new Object[] { root, groupNode1, templateNode2 }), false);
        Assert.assertEquals(templateNode2, this.model.getChild(groupNode1, 0));
        Assert.assertEquals(templateNode1, this.model.getChild(groupNode1, 1));
    }

    /**
     * Test of the {@link RelationTreeModel#removeEntry(TreePath)} method.
     */
    @Test
    public void testRemoveEntry_GroupEmpty() {
        final Object root = this.model.getRoot();
        final Object groupNode2 = this.model.getChild(root, 1);
        final Object groupNode3 = this.model.getChild(root, 2);
        final TreePath groupPath2 = new TreePath(new Object[] { root, groupNode2 });

        this.model.removeEntry(groupPath2);
        Assert.assertEquals(2, this.model.getChildCount(root));
        Assert.assertEquals(groupNode3, this.model.getChild(root, 1));
    }

    /**
     * Test of the {@link RelationTreeModel#removeEntry(TreePath)} method.
     */
    @Test
    public void testRemoveEntry_Group() {
        final Object root = this.model.getRoot();
        final Object groupNode1 = this.model.getChild(root, 0);
        final Object groupNode2 = this.model.getChild(root, 1);
        final Object templateNode1 = this.model.getChild(groupNode1, 0);
        final TreePath groupPath1 = new TreePath(new Object[] { root, groupNode1 });
        final TreePath templatePath1 = groupPath1.pathByAddingChild(templateNode1);

        Assert.assertNotNull(this.model.getTemplateAtPath(templatePath1));
        this.model.removeEntry(groupPath1);
        Assert.assertEquals(2, this.model.getChildCount(root));
        Assert.assertEquals(groupNode2, this.model.getChild(root, 0));
        Assert.assertNull(this.model.getTemplateAtPath(templatePath1));
    }

    /**
     * Test of the {@link RelationTreeModel#removeEntry(TreePath)} method.
     */
    @Test
    public void testRemoveEntry_Template() {
        final Object root = this.model.getRoot();
        final Object groupNode1 = this.model.getChild(root, 0);
        final Object templateNode1 = this.model.getChild(groupNode1, 0);
        final Object templateNode2 = this.model.getChild(groupNode1, 1);
        final TreePath templatePath1 = new TreePath(new Object[] { root, groupNode1, templateNode1 });

        this.model.removeEntry(templatePath1);
        Assert.assertNull(this.model.getTemplateAtPath(templatePath1));
        Assert.assertEquals(1, this.model.getChildCount(groupNode1));
        Assert.assertEquals(templateNode2, this.model.getChild(groupNode1, 0));
    }
}
