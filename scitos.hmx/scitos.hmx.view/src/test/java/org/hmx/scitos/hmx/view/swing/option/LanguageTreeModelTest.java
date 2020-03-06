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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.swing.tree.TreePath;

import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.core.option.ILanguageOptionHandler;
import org.hmx.scitos.hmx.domain.model.originlanguage.AbstractSyntacticalElement;
import org.hmx.scitos.hmx.domain.model.originlanguage.LanguageModel;
import org.hmx.scitos.hmx.domain.model.originlanguage.SyntacticalFunction;
import org.hmx.scitos.hmx.domain.model.originlanguage.SyntacticalFunctionGroup;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the {@link LanguageTreeModel} class.
 */
public class LanguageTreeModelTest {

    /**
     * The default language handler for creating the {@link LanguageTreeModel} instance to test.
     */
    private static ILanguageOptionHandler languageHandler;
    /**
     * The language tree model instance to test.
     */
    private LanguageTreeModel model;
    /**
     * The model's root node.
     */
    private Object root;

    /**
     * Initialize the default syntactical function provider for creating the {@link LanguageTreeModel} instance to test.
     */
    @BeforeClass
    public static void initFunctionProvider() {
        final List<AbstractSyntacticalElement> functionsB = new ArrayList<AbstractSyntacticalElement>(3);
        functionsB.add(new SyntacticalFunction("B1", "Function B1", false, null));
        functionsB.add(new SyntacticalFunction("B2", "Function B2", true, "Some extra information"));
        final List<AbstractSyntacticalElement> groupB3 = new ArrayList<AbstractSyntacticalElement>(2);
        groupB3.add(new SyntacticalFunction("B31", "Function B31", true, "some hint"));
        groupB3.add(new SyntacticalFunction("B32", "Function B32", false, null));
        functionsB.add(new SyntacticalFunctionGroup("Group B3", "Group B3's hint", groupB3));
        functionsB.add(new SyntacticalFunction("B4", "Function B4", false, "Other description"));

        final List<LanguageModel> systemModels =
                Arrays.asList(new LanguageModel("System Model - A", true), new LanguageModel("System Model - B", false));
        systemModels.get(1).add(functionsB);
        final List<LanguageModel> userModels =
                Arrays.asList(new LanguageModel("User Model - C", false), new LanguageModel("User Model - D", true), new LanguageModel(
                        "User Model - E", true));

        LanguageTreeModelTest.languageHandler = new ILanguageOptionHandler() {

            @Override
            public List<LanguageModel> getSystemModels() {
                return systemModels;
            }

            @Override
            public List<LanguageModel> getUserModels() {
                return userModels;
            }
        };
    }

    /** Clear the no longer needed function provider. */
    @AfterClass
    public static void clearFunctionProvider() {
        LanguageTreeModelTest.languageHandler = null;
    }

    /** Initialize the syntactical function tree model instance to test. */
    @Before
    public void initRelationTreeModel() {
        this.model = new LanguageTreeModel(LanguageTreeModelTest.languageHandler);
        this.root = this.model.getRoot();
    }

    /**
     * Test of the {@link LanguageTreeModel#getChildCount(Object)} method.
     */
    @Test
    public void testGetChildCount() {
        Assert.assertEquals(0, this.model.getChildCount(null));
        Assert.assertEquals(5, this.model.getChildCount(this.root));
        Assert.assertEquals(0, this.model.getChildCount(this.model.getChild(this.root, 0)));
    }

    /**
     * Test of the {@link LanguageTreeModel#getIndexOfChild(Object, Object)} method.
     */
    @Test
    public void testGetIndexOfChild() {
        Assert.assertEquals(-1, this.model.getIndexOfChild(this.root, null));
        Assert.assertEquals(0, this.model.getIndexOfChild(this.root, this.model.getChild(this.root, 0)));
        Assert.assertEquals(1, this.model.getIndexOfChild(this.root, this.model.getChild(this.root, 1)));
        Assert.assertEquals(2, this.model.getIndexOfChild(this.root, this.model.getChild(this.root, 2)));
        Assert.assertEquals(3, this.model.getIndexOfChild(this.root, this.model.getChild(this.root, 3)));
        Assert.assertEquals(4, this.model.getIndexOfChild(this.root, this.model.getChild(this.root, 4)));
    }

    /**
     * Test of the {@link LanguageTreeModel#getIndexOfChild(Object, Object)} method.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetIndexOfChild_ParentNull() {
        this.model.getIndexOfChild(null, null);
    }

    /**
     * Test of the {@link LanguageTreeModel#isCellEditable(Object, int)} method.
     */
    @Test
    public void testIsCellEditable() {
        Assert.assertFalse(this.model.isCellEditable(this.root, 0));

        final UUID referenceSystemModelB = this.model.getChild(this.root, 1);
        Assert.assertFalse(this.model.isCellEditable(referenceSystemModelB, 0));
        Assert.assertFalse(this.model.isCellEditable(referenceSystemModelB, 1));
        Assert.assertTrue(this.model.isCellEditable(referenceSystemModelB, 2));
        Assert.assertFalse(this.model.isCellEditable(referenceSystemModelB, 3));

        final UUID referenceUserModelD = this.model.getChild(this.root, 3);
        Assert.assertFalse(this.model.isCellEditable(referenceUserModelD, 0));
        Assert.assertFalse(this.model.isCellEditable(referenceUserModelD, 1));
        Assert.assertTrue(this.model.isCellEditable(referenceUserModelD, 2));
        Assert.assertTrue(this.model.isCellEditable(referenceUserModelD, 3));
    }

    /**
     * Test of the {@link LanguageTreeModel#getValueAt(Object, int)} method.
     */
    @Test
    public void testGetValueAt() {
        Assert.assertNull(this.model.getValueAt(this.root, 0));
        Assert.assertNull(this.model.getValueAt(this.root, 1));
        Assert.assertNull(this.model.getValueAt(this.root, 2));
        Assert.assertNull(this.model.getValueAt(this.root, 3));

        final UUID referenceSystemModelB = this.model.getChild(this.root, 1);
        Assert.assertEquals(MessageFormat.format(HmxMessage.PREFERENCES_LANGUAGE_SYSTEMMODEL.get(), "System Model - B"),
                this.model.getValueAt(referenceSystemModelB, 0));
        Assert.assertEquals(MessageFormat.format(HmxMessage.PREFERENCES_LANGUAGE_FUNCTION_COUNT.get(), 5),
                this.model.getValueAt(referenceSystemModelB, 1));
        Assert.assertEquals(HmxMessage.PREFERENCES_LANGUAGE_CLONE_LANGUAGE.get(), this.model.getValueAt(referenceSystemModelB, 2));
        Assert.assertNull(this.model.getValueAt(referenceSystemModelB, 3));

        final UUID referenceUserModelD = this.model.getChild(this.root, 3);
        Assert.assertEquals("User Model - D", this.model.getValueAt(referenceUserModelD, 0));
        Assert.assertEquals(MessageFormat.format(HmxMessage.PREFERENCES_LANGUAGE_FUNCTION_COUNT.get(), 0),
                this.model.getValueAt(referenceUserModelD, 1));
        Assert.assertEquals(HmxMessage.PREFERENCES_LANGUAGE_CLONE_LANGUAGE.get(), this.model.getValueAt(referenceUserModelD, 2));
        Assert.assertEquals(HmxMessage.PREFERENCES_LANGUAGE_DELETE_LANGUAGE.get(), this.model.getValueAt(referenceUserModelD, 3));
    }

    /**
     * Test of the {@link LanguageTreeModel#getValueAt(Object, int)} method.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAt_Column5() {
        Assert.assertEquals(4, this.model.getColumnCount());
        this.model.getValueAt(this.model.getChild(this.root, 1), 4);
    }

    /**
     * Test of the {@link LanguageTreeModel#isModelAtPathUserDefined(TreePath)} method.
     */
    @Test
    public void testIsModelAtPathUserDefined() {
        final TreePath rootPath = new TreePath(this.root);
        final UUID referenceSystemModelB = this.model.getChild(this.root, 1);
        final UUID referenceUserModelC = this.model.getChild(this.root, 2);
        final UUID referenceUserModelD = this.model.getChild(this.root, 3);

        Assert.assertFalse(this.model.isModelAtPathUserDefined(null));
        Assert.assertFalse(this.model.isModelAtPathUserDefined(rootPath));
        Assert.assertFalse(this.model.isModelAtPathUserDefined(rootPath.pathByAddingChild(referenceSystemModelB)));
        Assert.assertTrue(this.model.isModelAtPathUserDefined(rootPath.pathByAddingChild(referenceUserModelC)));
        Assert.assertTrue(this.model.isModelAtPathUserDefined(rootPath.pathByAddingChild(referenceUserModelD)));
    }

    /**
     * Test of the {@link LanguageTreeModel#addModelRowClone(TreePath)} method.
     */
    @Test
    public void testAddModelRowClone() {
        final UUID referenceSystemModelB = this.model.getChild(this.root, 1);
        final TreePath originalRowPath = new TreePath(new Object[] { this.root, referenceSystemModelB });
        final TreePath newRowPath = this.model.addModelRowClone(originalRowPath);
        final LanguageModel languageOriginal = this.model.getModelForPath(originalRowPath);
        final LanguageModel languageClone = this.model.getModelForPath(newRowPath);

        Assert.assertNotSame(languageOriginal, languageClone);
        Assert.assertEquals(languageOriginal, languageClone);
        Assert.assertEquals(6, this.model.getChildCount(this.root));
    }

    /**
     * Test of the {@link LanguageTreeModel#deleteModelRow(TreePath)} method.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDeleteModelRow_SystemModel() {
        final UUID referenceSystemModelB = this.model.getChild(this.root, 1);
        final TreePath targetPath = new TreePath(new Object[] { this.root, referenceSystemModelB });
        this.model.deleteModelRow(targetPath);
    }

    /**
     * Test of the {@link LanguageTreeModel#deleteModelRow(TreePath)} method.
     */
    @Test
    public void testDeleteModelRow_UserModel() {
        final UUID referenceUserModelD = this.model.getChild(this.root, 3);
        final UUID referenceUserModelE = this.model.getChild(this.root, 4);
        final TreePath targetPath = new TreePath(new Object[] { this.root, referenceUserModelD });
        this.model.deleteModelRow(targetPath);

        Assert.assertEquals(4, this.model.getChildCount(this.root));
        Assert.assertEquals(referenceUserModelE, this.model.getChild(this.root, 3));
    }

    /**
     * Test of the {@link LanguageTreeModel#fireModelRowUpdated(LanguageModel)} method.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testFireModelRowUpdated_SystemModel() {
        final UUID referenceSystemModelB = this.model.getChild(this.root, 1);
        final TreePath pathSystemModelB = new TreePath(new Object[] { this.root, referenceSystemModelB });
        final LanguageModel systemModelB = this.model.getModelForPath(pathSystemModelB);

        this.model.fireModelRowUpdated(systemModelB);
    }

    /**
     * Test of the {@link LanguageTreeModel#fireModelRowUpdated(LanguageModel)} method.
     */
    @Test
    public void testFireModelRowUpdated_UserModel() {
        final UUID referenceUserModelD = this.model.getChild(this.root, 3);
        final TreePath pathUserModelD = new TreePath(new Object[] { this.root, referenceUserModelD });
        final LanguageModel userModelD = this.model.getModelForPath(pathUserModelD);

        this.model.fireModelRowUpdated(userModelD);
    }

    /**
     * Test of the {@link LanguageTreeModel#getUserLanguageModels()} method.
     */
    @Test
    public void testGetUserLanguageModels() {
        Assert.assertEquals(LanguageTreeModelTest.languageHandler.getUserModels(), this.model.getUserLanguageModels());
    }
}
