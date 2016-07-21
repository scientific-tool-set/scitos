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
import java.util.UUID;

import javax.swing.tree.TreePath;

import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.domain.ISyntacticalFunctionProvider;
import org.hmx.scitos.hmx.domain.model.AbstractSyntacticalFunctionElement;
import org.hmx.scitos.hmx.domain.model.SyntacticalFunction;
import org.hmx.scitos.hmx.domain.model.SyntacticalFunctionGroup;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the {@link SynFunctionTreeModel} class.
 */
public class SynFunctionTreeModelTest {

    /**
     * The default syntactical function provider for creating the {@link SynFunctionTreeModel} instance to test.
     */
    private static ISyntacticalFunctionProvider functionProvider;
    /**
     * The syntactical function tree model instance to test.
     */
    private SynFunctionTreeModel model;
    /**
     * The model's root node.
     */
    private Object root;
    /**
     * The model's first top level group's reference.
     */
    private UUID referenceGroupA;
    /**
     * The model's second top level group's reference.
     */
    private UUID referenceGroupB;
    /**
     * The model's third top level group's reference.
     */
    private UUID referenceGroupC;

    /**
     * Initialize the default syntactical function provider for creating the {@link SynFunctionTreeModel} instance to test.
     */
    @BeforeClass
    public static void initFunctionProvider() {
        final List<AbstractSyntacticalFunctionElement> groupA = new ArrayList<AbstractSyntacticalFunctionElement>(3);
        groupA.add(new SyntacticalFunction("A1", "Function A1", false, null));
        groupA.add(new SyntacticalFunction("A2", "Function A2", true, "Some extra information"));
        groupA.add(new SyntacticalFunction("A3", "Function A3", false, "Other description"));
        final List<AbstractSyntacticalFunctionElement> groupB = Collections.emptyList();
        final List<AbstractSyntacticalFunctionElement> groupC = new ArrayList<AbstractSyntacticalFunctionElement>(4);
        final List<AbstractSyntacticalFunctionElement> groupC1 = new ArrayList<AbstractSyntacticalFunctionElement>(2);
        groupC1.add(new SyntacticalFunction("C11", "Function C11", true, "some hint"));
        groupC1.add(new SyntacticalFunction("C12", "Function C12", false, null));
        groupC.add(new SyntacticalFunctionGroup("Group C1", "Group C1's hint", groupC1));
        groupC.add(new SyntacticalFunction("C2", "Function C2", false, "text"));
        groupC.add(new SyntacticalFunctionGroup("Group C3", "", Collections.<AbstractSyntacticalFunctionElement>emptyList()));
        final List<AbstractSyntacticalFunctionElement> groupC41 = new ArrayList<AbstractSyntacticalFunctionElement>(2);
        groupC41.add(new SyntacticalFunction("C411", "Function C411", false, "..."));
        groupC41.add(new SyntacticalFunction("C412", "Function C412", true, ""));
        final List<SyntacticalFunctionGroup> groupC4 = Collections.singletonList(new SyntacticalFunctionGroup("Group C41", "Something", groupC41));
        groupC.add(new SyntacticalFunctionGroup("Group C4", null, groupC4));

        final List<List<AbstractSyntacticalFunctionElement>> functionGroups = Arrays.asList(groupA, groupB, groupC);

        SynFunctionTreeModelTest.functionProvider = new ISyntacticalFunctionProvider() {

            @Override
            public List<List<AbstractSyntacticalFunctionElement>> provideFunctions() {
                return functionGroups;
            }
        };
    }

    /** Clear the no longer needed function provider. */
    @AfterClass
    public static void clearFunctionProvider() {
        SynFunctionTreeModelTest.functionProvider = null;
    }

    /** Initialize the syntactical function tree model instance to test. */
    @Before
    public void initRelationTreeModel() {
        this.model = new SynFunctionTreeModel();
        this.model.reset(SynFunctionTreeModelTest.functionProvider);
        this.root = this.model.getRoot();
        this.referenceGroupA = this.model.getChild(this.root, 0);
        this.referenceGroupB = this.model.getChild(this.root, 1);
        this.referenceGroupC = this.model.getChild(this.root, 2);
    }

    /**
     * Test of the {@link SynFunctionTreeModel#provideFunctions()} method.
     */
    @Test
    public void testProvideFunctions() {
        final List<List<AbstractSyntacticalFunctionElement>> expectedElements = SynFunctionTreeModelTest.functionProvider.provideFunctions();
        final List<List<AbstractSyntacticalFunctionElement>> actualElements = this.model.provideFunctions();
        // ensure the ISyntacticalFunctionProvider's provideFunctions() method returns an equal copy of the prepared syntactical functions
        Assert.assertNotSame(expectedElements, actualElements);
        Assert.assertEquals(expectedElements, actualElements);
    }

    /**
     * Test of the {@link SynFunctionTreeModel#getChildCount(Object)} method.
     */
    @Test
    public void testGetChildCount() {
        Assert.assertEquals(3, this.model.getChildCount(this.root));

        Assert.assertEquals(0, this.model.getChildCount(this.referenceGroupB));
        Assert.assertEquals(4, this.model.getChildCount(this.referenceGroupC));

        final UUID referenceGroupC1 = this.model.getChild(this.referenceGroupC, 0);
        final UUID referenceFunctionC2 = this.model.getChild(this.referenceGroupC, 1);
        final UUID referenceGroupC3 = this.model.getChild(this.referenceGroupC, 2);
        final UUID referenceGroupC4 = this.model.getChild(this.referenceGroupC, 3);
        Assert.assertEquals(2, this.model.getChildCount(referenceGroupC1));
        Assert.assertEquals(0, this.model.getChildCount(referenceFunctionC2));
        Assert.assertEquals(0, this.model.getChildCount(referenceGroupC3));
        Assert.assertEquals(1, this.model.getChildCount(referenceGroupC4));

        final UUID referenceGroupC41 = this.model.getChild(referenceGroupC4, 0);
        Assert.assertEquals(2, this.model.getChildCount(referenceGroupC41));
    }

    /**
     * Test of the {@link SynFunctionTreeModel#getIndexOfChild(Object, Object)} method.
     */
    @Test
    public void testGetIndexOfChild() {
        Assert.assertEquals(0, this.model.getIndexOfChild(this.root, this.referenceGroupA));
        Assert.assertEquals(1, this.model.getIndexOfChild(this.root, this.referenceGroupB));
        Assert.assertEquals(2, this.model.getIndexOfChild(this.root, this.referenceGroupC));

        final UUID referenceGroupC1 = this.model.getChild(this.referenceGroupC, 0);
        final UUID referenceFunctionC2 = this.model.getChild(this.referenceGroupC, 1);
        final UUID referenceGroupC3 = this.model.getChild(this.referenceGroupC, 2);
        final UUID referenceGroupC4 = this.model.getChild(this.referenceGroupC, 3);
        Assert.assertEquals(0, this.model.getIndexOfChild(this.referenceGroupC, referenceGroupC1));
        Assert.assertEquals(1, this.model.getIndexOfChild(this.referenceGroupC, referenceFunctionC2));
        Assert.assertEquals(2, this.model.getIndexOfChild(this.referenceGroupC, referenceGroupC3));
        Assert.assertEquals(3, this.model.getIndexOfChild(this.referenceGroupC, referenceGroupC4));
    }

    /**
     * Test of the {@link SynFunctionTreeModel#isCellEditable(Object, int)} method.
     */
    @Test
    public void testIsCellEditable() {
        Assert.assertFalse(this.model.isCellEditable(this.referenceGroupA, 0));
        Assert.assertFalse(this.model.isCellEditable(this.referenceGroupA, 1));
        Assert.assertFalse(this.model.isCellEditable(this.referenceGroupA, 2));
        Assert.assertTrue(this.model.isCellEditable(this.referenceGroupA, 3));
        Assert.assertTrue(this.model.isCellEditable(this.referenceGroupA, 4));
        Assert.assertFalse(this.model.isCellEditable(this.referenceGroupA, 5));
        Assert.assertTrue(this.model.isCellEditable(this.referenceGroupA, 6));
        Assert.assertTrue(this.model.isCellEditable(this.referenceGroupA, 7));

        Assert.assertFalse(this.model.isCellEditable(this.referenceGroupB, 0));
        Assert.assertFalse(this.model.isCellEditable(this.referenceGroupB, 1));
        Assert.assertFalse(this.model.isCellEditable(this.referenceGroupB, 2));
        Assert.assertTrue(this.model.isCellEditable(this.referenceGroupB, 3));
        Assert.assertTrue(this.model.isCellEditable(this.referenceGroupB, 4));
        Assert.assertTrue(this.model.isCellEditable(this.referenceGroupB, 5));
        Assert.assertTrue(this.model.isCellEditable(this.referenceGroupB, 6));
        Assert.assertTrue(this.model.isCellEditable(this.referenceGroupB, 7));

        Assert.assertFalse(this.model.isCellEditable(this.referenceGroupC, 0));
        Assert.assertFalse(this.model.isCellEditable(this.referenceGroupC, 1));
        Assert.assertFalse(this.model.isCellEditable(this.referenceGroupC, 2));
        Assert.assertTrue(this.model.isCellEditable(this.referenceGroupC, 3));
        Assert.assertTrue(this.model.isCellEditable(this.referenceGroupC, 4));
        Assert.assertTrue(this.model.isCellEditable(this.referenceGroupC, 5));
        Assert.assertFalse(this.model.isCellEditable(this.referenceGroupC, 6));
        Assert.assertTrue(this.model.isCellEditable(this.referenceGroupC, 7));

        final UUID referenceFunctionA1 = this.model.getChild(this.referenceGroupA, 0);
        Assert.assertFalse(this.model.isCellEditable(referenceFunctionA1, 0));
        Assert.assertFalse(this.model.isCellEditable(referenceFunctionA1, 1));
        Assert.assertFalse(this.model.isCellEditable(referenceFunctionA1, 2));
        Assert.assertFalse(this.model.isCellEditable(referenceFunctionA1, 3));
        Assert.assertFalse(this.model.isCellEditable(referenceFunctionA1, 4));
        Assert.assertFalse(this.model.isCellEditable(referenceFunctionA1, 5));
        Assert.assertTrue(this.model.isCellEditable(referenceFunctionA1, 6));
        Assert.assertTrue(this.model.isCellEditable(referenceFunctionA1, 7));

        final UUID referenceFunctionA2 = this.model.getChild(this.referenceGroupA, 1);
        Assert.assertFalse(this.model.isCellEditable(referenceFunctionA2, 0));
        Assert.assertFalse(this.model.isCellEditable(referenceFunctionA2, 1));
        Assert.assertFalse(this.model.isCellEditable(referenceFunctionA2, 2));
        Assert.assertFalse(this.model.isCellEditable(referenceFunctionA2, 3));
        Assert.assertFalse(this.model.isCellEditable(referenceFunctionA2, 4));
        Assert.assertTrue(this.model.isCellEditable(referenceFunctionA2, 5));
        Assert.assertTrue(this.model.isCellEditable(referenceFunctionA2, 6));
        Assert.assertTrue(this.model.isCellEditable(referenceFunctionA2, 7));

        final UUID referenceFunctionA3 = this.model.getChild(this.referenceGroupA, 2);
        Assert.assertFalse(this.model.isCellEditable(referenceFunctionA3, 0));
        Assert.assertFalse(this.model.isCellEditable(referenceFunctionA3, 1));
        Assert.assertFalse(this.model.isCellEditable(referenceFunctionA3, 2));
        Assert.assertFalse(this.model.isCellEditable(referenceFunctionA3, 3));
        Assert.assertFalse(this.model.isCellEditable(referenceFunctionA3, 4));
        Assert.assertTrue(this.model.isCellEditable(referenceFunctionA3, 5));
        Assert.assertFalse(this.model.isCellEditable(referenceFunctionA3, 6));
        Assert.assertTrue(this.model.isCellEditable(referenceFunctionA3, 7));
    }

    /**
     * Test of the {@link SynFunctionTreeModel#getValueAt(Object, int)} method.
     */
    @Test
    public void testGetValueAt_Root() {
        Assert.assertNull(this.model.getValueAt(this.root, 0));
    }

    /**
     * Test of the {@link SynFunctionTreeModel#getValueAt(Object, int)} method.
     */
    @Test
    public void testGetValueAt_Column1() {
        final UUID referenceGroupC1 = this.model.getChild(this.referenceGroupC, 0);
        final UUID referenceFunctionC11 = this.model.getChild(referenceGroupC1, 0);

        Assert.assertEquals("3.", this.model.getValueAt(this.referenceGroupC, 0));
        Assert.assertEquals("Group C1", this.model.getValueAt(referenceGroupC1, 0));
        Assert.assertEquals("Function C11", this.model.getValueAt(referenceFunctionC11, 0));
    }

    /**
     * Test of the {@link SynFunctionTreeModel#getValueAt(Object, int)} method.
     */
    @Test
    public void testGetValueAt_Column2() {
        final UUID referenceGroupC1 = this.model.getChild(this.referenceGroupC, 0);
        final UUID referenceFunctionC11 = this.model.getChild(referenceGroupC1, 0);

        Assert.assertEquals("", this.model.getValueAt(this.referenceGroupC, 1));
        Assert.assertEquals("", this.model.getValueAt(referenceGroupC1, 1));
        Assert.assertEquals("C11", this.model.getValueAt(referenceFunctionC11, 1));
    }

    /**
     * Test of the {@link SynFunctionTreeModel#getValueAt(Object, int)} method.
     */
    @Test
    public void testGetValueAt_Column3() {
        final UUID referenceGroupC1 = this.model.getChild(this.referenceGroupC, 0);
        final UUID referenceFunctionC11 = this.model.getChild(referenceGroupC1, 0);

        Assert.assertEquals("", this.model.getValueAt(this.referenceGroupC, 2));
        Assert.assertEquals("Group C1's hint", this.model.getValueAt(referenceGroupC1, 2));
        Assert.assertEquals("some hint", this.model.getValueAt(referenceFunctionC11, 2));
    }

    /**
     * Test of the {@link SynFunctionTreeModel#getValueAt(Object, int)} method.
     */
    @Test
    public void testGetValueAt_Column4() {
        final UUID referenceGroupC1 = this.model.getChild(this.referenceGroupC, 0);
        final UUID referenceFunctionC11 = this.model.getChild(referenceGroupC1, 0);

        final String expectedAddFunctionLabel = HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_ADD_FUNCTION.get();
        Assert.assertEquals(expectedAddFunctionLabel, this.model.getValueAt(this.referenceGroupC, 3));
        Assert.assertEquals(expectedAddFunctionLabel, this.model.getValueAt(referenceGroupC1, 3));
        Assert.assertNull(this.model.getValueAt(referenceFunctionC11, 3));
    }

    /**
     * Test of the {@link SynFunctionTreeModel#getValueAt(Object, int)} method.
     */
    @Test
    public void testGetValueAt_Column5() {
        final UUID referenceGroupC1 = this.model.getChild(this.referenceGroupC, 0);
        final UUID referenceFunctionC11 = this.model.getChild(referenceGroupC1, 0);

        final String expectedAddGroupLabel = HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_ADD_GROUP.get();
        Assert.assertEquals(expectedAddGroupLabel, this.model.getValueAt(this.referenceGroupC, 4));
        Assert.assertEquals(expectedAddGroupLabel, this.model.getValueAt(referenceGroupC1, 4));
        Assert.assertNull(this.model.getValueAt(referenceFunctionC11, 4));
    }

    /**
     * Test of the {@link SynFunctionTreeModel#getValueAt(Object, int)} method.
     */
    @Test
    public void testGetValueAt_Column6() {
        final UUID referenceGroupC1 = this.model.getChild(this.referenceGroupC, 0);
        final UUID referenceFunctionC11 = this.model.getChild(referenceGroupC1, 0);
        final UUID referenceFunctionC12 = this.model.getChild(referenceGroupC1, 1);
        final UUID referenceGroupC3 = this.model.getChild(this.referenceGroupC, 2);
        final UUID referenceGroupC4 = this.model.getChild(this.referenceGroupC, 3);

        final String expectedMoveUpLabel = HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_MOVE_UP.get();
        Assert.assertNull(this.model.getValueAt(this.referenceGroupA, 5));
        Assert.assertEquals(expectedMoveUpLabel, this.model.getValueAt(this.referenceGroupC, 5));
        Assert.assertNull(this.model.getValueAt(referenceGroupC1, 5));
        Assert.assertNull(this.model.getValueAt(referenceFunctionC11, 5));
        Assert.assertEquals(expectedMoveUpLabel, this.model.getValueAt(referenceFunctionC12, 5));
        Assert.assertEquals(expectedMoveUpLabel, this.model.getValueAt(referenceGroupC3, 5));
        Assert.assertEquals(expectedMoveUpLabel, this.model.getValueAt(referenceGroupC4, 5));
    }

    /**
     * Test of the {@link SynFunctionTreeModel#getValueAt(Object, int)} method.
     */
    @Test
    public void testGetValueAt_Column7() {
        final UUID referenceGroupC1 = this.model.getChild(this.referenceGroupC, 0);
        final UUID referenceFunctionC11 = this.model.getChild(referenceGroupC1, 0);
        final UUID referenceFunctionC12 = this.model.getChild(referenceGroupC1, 1);
        final UUID referenceGroupC3 = this.model.getChild(this.referenceGroupC, 2);
        final UUID referenceGroupC4 = this.model.getChild(this.referenceGroupC, 3);

        final String expectedMoveDownLabel = HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_MOVE_DOWN.get();
        Assert.assertEquals(expectedMoveDownLabel, this.model.getValueAt(this.referenceGroupA, 6));
        Assert.assertNull(this.model.getValueAt(this.referenceGroupC, 6));
        Assert.assertEquals(expectedMoveDownLabel, this.model.getValueAt(referenceGroupC1, 6));
        Assert.assertEquals(expectedMoveDownLabel, this.model.getValueAt(referenceFunctionC11, 6));
        Assert.assertNull(this.model.getValueAt(referenceFunctionC12, 6));
        Assert.assertEquals(expectedMoveDownLabel, this.model.getValueAt(referenceGroupC3, 6));
        Assert.assertNull(this.model.getValueAt(referenceGroupC4, 6));
    }

    /**
     * Test of the {@link SynFunctionTreeModel#getValueAt(Object, int)} method.
     */
    @Test
    public void testGetValueAt_Column8() {
        final UUID referenceGroupC1 = this.model.getChild(this.referenceGroupC, 0);
        final UUID referenceFunctionC11 = this.model.getChild(referenceGroupC1, 0);

        final String expectedRemoveGroupLabel = HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_REMOVE_GROUP.get();
        Assert.assertEquals(expectedRemoveGroupLabel, this.model.getValueAt(this.referenceGroupC, 7));
        Assert.assertEquals(expectedRemoveGroupLabel, this.model.getValueAt(referenceGroupC1, 7));
        final String expectedRemoveFunctionLabel = HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_REMOVE.get();
        Assert.assertEquals(expectedRemoveFunctionLabel, this.model.getValueAt(referenceFunctionC11, 7));
    }

    /**
     * Test of the {@link SynFunctionTreeModel#getValueAt(Object, int)} method.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAt_Column9_TopLevelGroup() {
        Assert.assertEquals(8, this.model.getColumnCount());
        this.model.getValueAt(this.referenceGroupA, 8);
    }

    /**
     * Test of the {@link SynFunctionTreeModel#getValueAt(Object, int)} method.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAt_Column9_Entry() {
        final UUID referenceFunctionA1 = this.model.getChild(this.referenceGroupA, 0);

        Assert.assertEquals(8, this.model.getColumnCount());
        this.model.getValueAt(referenceFunctionA1, 8);
    }

    /**
     * Test of the {@link SynFunctionTreeModel#addTopLevelGroup()} method.
     */
    @Test
    public void testAddTopLevelGroup() {
        Assert.assertEquals(3, this.model.getChildCount(this.root));

        final TreePath newEntry = this.model.addTopLevelGroup();

        Assert.assertEquals(4, this.model.getChildCount(this.root));
        final UUID newGroupNode = this.model.getChild(this.root, 3);
        Assert.assertEquals(new TreePath(new Object[] { this.root, newGroupNode }), newEntry);
    }

    /**
     * Test of the {@link SynFunctionTreeModel#addGroupRow(TreePath)} method.
     */
    @Test
    public void testAddGroupRow_InTopLevelGroup() {
        Assert.assertEquals(3, this.model.getChildCount(this.referenceGroupA));

        final TreePath newEntry = this.model.addGroupRow(new TreePath(new Object[] { this.root, this.referenceGroupA }));

        Assert.assertEquals(4, this.model.getChildCount(this.referenceGroupA));
        final UUID newGroupNode = this.model.getChild(this.referenceGroupA, 3);
        Assert.assertEquals(new TreePath(new Object[] { this.root, this.referenceGroupA, newGroupNode }), newEntry);
        // ensure that this is a group and not a function
        Assert.assertEquals(HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_REMOVE_GROUP.get(), this.model.getValueAt(newGroupNode, 7));
    }

    /**
     * Test of the {@link SynFunctionTreeModel#addGroupRow(TreePath)} method.
     */
    @Test
    public void testAddGroupRow_UnderGroupRow() {
        final UUID referenceGroupC1 = this.model.getChild(this.referenceGroupC, 0);
        Assert.assertEquals(2, this.model.getChildCount(referenceGroupC1));

        final TreePath newEntry = this.model.addGroupRow(new TreePath(new Object[] { this.root, this.referenceGroupC, referenceGroupC1 }));

        Assert.assertEquals(3, this.model.getChildCount(referenceGroupC1));
        final UUID newGroupNode = this.model.getChild(referenceGroupC1, 2);
        Assert.assertEquals(new TreePath(new Object[] { this.root, this.referenceGroupC, referenceGroupC1, newGroupNode }), newEntry);
        // ensure that this is a group and not a function
        Assert.assertEquals(HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_REMOVE_GROUP.get(), this.model.getValueAt(newGroupNode, 7));
    }

    /**
     * Test of the {@link SynFunctionTreeModel#addSynFunctionRow(TreePath)} method.
     */
    @Test
    public void testAddSynFunctionRow_InTopLevelGroup() {
        Assert.assertEquals(3, this.model.getChildCount(this.referenceGroupA));

        final TreePath newEntry = this.model.addSynFunctionRow(new TreePath(new Object[] { this.root, this.referenceGroupA }));

        Assert.assertEquals(4, this.model.getChildCount(this.referenceGroupA));
        final UUID newGroupNode = this.model.getChild(this.referenceGroupA, 3);
        Assert.assertEquals(new TreePath(new Object[] { this.root, this.referenceGroupA, newGroupNode }), newEntry);
        // ensure that this is a function and not a group
        Assert.assertEquals(HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_REMOVE.get(), this.model.getValueAt(newGroupNode, 7));
    }

    /**
     * Test of the {@link SynFunctionTreeModel#addSynFunctionRow(TreePath)} method.
     */
    @Test
    public void testAddSynFunctionRow_UnderGroupRow() {
        final UUID referenceGroupC1 = this.model.getChild(this.referenceGroupC, 0);
        Assert.assertEquals(2, this.model.getChildCount(referenceGroupC1));

        final TreePath newEntry = this.model.addSynFunctionRow(new TreePath(new Object[] { this.root, this.referenceGroupC, referenceGroupC1 }));

        Assert.assertEquals(3, this.model.getChildCount(referenceGroupC1));
        final UUID newGroupNode = this.model.getChild(referenceGroupC1, 2);
        Assert.assertEquals(new TreePath(new Object[] { this.root, this.referenceGroupC, referenceGroupC1, newGroupNode }), newEntry);
        // ensure that this is a function and not a group
        Assert.assertEquals(HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_REMOVE.get(), this.model.getValueAt(newGroupNode, 7));
    }

    /**
     * Test of the {@link SynFunctionTreeModel#moveEntry(TreePath, boolean)} method.
     */
    @Test
    public void testMoveEntry_TopLevelGroupDown() {
        this.model.moveEntry(new TreePath(new Object[] { this.root, this.referenceGroupB }), true);

        Assert.assertEquals(this.referenceGroupA, this.model.getChild(this.root, 0));
        Assert.assertEquals(this.referenceGroupC, this.model.getChild(this.root, 1));
        Assert.assertEquals(this.referenceGroupB, this.model.getChild(this.root, 2));
    }

    /**
     * Test of the {@link SynFunctionTreeModel#moveEntry(TreePath, boolean)} method.
     */
    @Test
    public void testMoveEntry_TopLevelGroupUp() {
        this.model.moveEntry(new TreePath(new Object[] { this.root, this.referenceGroupB }), false);

        Assert.assertEquals(this.referenceGroupB, this.model.getChild(this.root, 0));
        Assert.assertEquals(this.referenceGroupA, this.model.getChild(this.root, 1));
        Assert.assertEquals(this.referenceGroupC, this.model.getChild(this.root, 2));
    }

    /**
     * Test of the {@link SynFunctionTreeModel#moveEntry(TreePath, boolean)} method.
     */
    @Test
    public void testMoveEntry_ElementDown() {
        final UUID referenceFunctionA1 = this.model.getChild(this.referenceGroupA, 0);
        final UUID referenceFunctionA2 = this.model.getChild(this.referenceGroupA, 1);
        final UUID referenceFunctionA3 = this.model.getChild(this.referenceGroupA, 2);

        this.model.moveEntry(new TreePath(new Object[] { this.root, this.referenceGroupA, referenceFunctionA2 }), true);

        Assert.assertEquals(referenceFunctionA1, this.model.getChild(this.referenceGroupA, 0));
        Assert.assertEquals(referenceFunctionA3, this.model.getChild(this.referenceGroupA, 1));
        Assert.assertEquals(referenceFunctionA2, this.model.getChild(this.referenceGroupA, 2));
    }

    /**
     * Test of the {@link SynFunctionTreeModel#moveEntry(TreePath, boolean)} method.
     */
    @Test
    public void testMoveEntry_ElementUp() {
        final UUID referenceFunctionA1 = this.model.getChild(this.referenceGroupA, 0);
        final UUID referenceFunctionA2 = this.model.getChild(this.referenceGroupA, 1);
        final UUID referenceFunctionA3 = this.model.getChild(this.referenceGroupA, 2);

        this.model.moveEntry(new TreePath(new Object[] { this.root, this.referenceGroupA, referenceFunctionA2 }), false);

        Assert.assertEquals(referenceFunctionA2, this.model.getChild(this.referenceGroupA, 0));
        Assert.assertEquals(referenceFunctionA1, this.model.getChild(this.referenceGroupA, 1));
        Assert.assertEquals(referenceFunctionA3, this.model.getChild(this.referenceGroupA, 2));
    }

    /**
     * Test of the {@link SynFunctionTreeModel#removeEntry(TreePath)} method.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRemoveEntry_TopLevelGroup() {
        final UUID referenceGroupC4 = this.model.getChild(this.referenceGroupC, 3);
        final UUID referenceGroupC41 = this.model.getChild(referenceGroupC4, 0);
        final UUID referenceFunctionC411 = this.model.getChild(referenceGroupC41, 0);

        this.model.removeEntry(new TreePath(new Object[] { this.root, this.referenceGroupC }));

        Assert.assertEquals(2, this.model.getChildCount(this.root));
        try {
            this.model.getValueAt(this.referenceGroupC, 0);
            Assert.fail("An IllegalArgumentException should have be thrown for the removed top level group");
        } catch (final IllegalArgumentException expected) {
            // provoke IllegalArgumentException as the referenced row no longer exists in the model (after its parent was removed)
            this.model.getValueAt(referenceFunctionC411, 0);
        }
    }

    /**
     * Test of the {@link SynFunctionTreeModel#removeEntry(TreePath)} method.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRemoveEntry_Group() {
        final UUID referenceGroupC4 = this.model.getChild(this.referenceGroupC, 3);
        final UUID referenceGroupC41 = this.model.getChild(referenceGroupC4, 0);
        final UUID referenceFunctionC411 = this.model.getChild(referenceGroupC41, 0);

        this.model.removeEntry(new TreePath(new Object[] { this.root, this.referenceGroupC, referenceGroupC4 }));

        Assert.assertEquals(3, this.model.getChildCount(this.referenceGroupC));
        try {
            this.model.getValueAt(referenceGroupC4, 0);
            Assert.fail("An IllegalArgumentException should have be thrown for the removed function group");
        } catch (final IllegalArgumentException expected) {
            // provoke IllegalArgumentException as the referenced row no longer exists in the model (after its parent was removed)
            this.model.getValueAt(referenceFunctionC411, 0);
        }
    }

    /**
     * Test of the {@link SynFunctionTreeModel#removeEntry(TreePath)} method.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRemoveEntry_Function() {
        final UUID referenceFunctionA1 = this.model.getChild(this.referenceGroupA, 0);

        this.model.removeEntry(new TreePath(new Object[] { this.root, this.referenceGroupA, referenceFunctionA1 }));

        Assert.assertEquals(2, this.model.getChildCount(this.referenceGroupA));
        this.model.getValueAt(referenceFunctionA1, 0);
    }
}
