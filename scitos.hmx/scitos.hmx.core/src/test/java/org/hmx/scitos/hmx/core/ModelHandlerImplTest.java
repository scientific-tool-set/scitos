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

package org.hmx.scitos.hmx.core;

import java.awt.Font;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.hmx.domain.model.AbstractSyntacticalFunctionElement;
import org.hmx.scitos.hmx.domain.model.ClauseItem;
import org.hmx.scitos.hmx.domain.model.LanguageModel;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.domain.model.Relation;
import org.hmx.scitos.hmx.domain.model.RelationTemplate;
import org.hmx.scitos.hmx.domain.model.RelationTemplate.AssociateRole;
import org.hmx.scitos.hmx.domain.model.SyntacticalFunction;
import org.hmx.scitos.hmx.domain.model.SyntacticalFunctionGroup;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the {@link ModelHandlerImpl} class.
 */
public class ModelHandlerImplTest {

    private static LanguageModel languageModel;
    private static RelationTemplate defaultRelationTemplate;
    private Pericope pericope;
    private ModelHandlerImpl modelHandler;

    /**
     * Initial setup: create a simple {@link LanguageModel} to use in all tests.
     */
    @BeforeClass
    public static void setUp() {
        final List<AbstractSyntacticalFunctionElement> functions = new LinkedList<AbstractSyntacticalFunctionElement>();
        functions.add(new SyntacticalFunction("A", "A Function", false, null));
        functions.add(new SyntacticalFunction("B", "Second Function", true, "some description"));
        functions.add(new SyntacticalFunctionGroup("Group", null, Arrays.asList(new SyntacticalFunction("C", "Nested Function", false, null),
                new SyntacticalFunction("D", "Other Sub Function", false, "The last one"))));
        ModelHandlerImplTest.languageModel = new LanguageModel("Language", true);
        ModelHandlerImplTest.languageModel.add(functions);
        ModelHandlerImplTest.defaultRelationTemplate =
                new RelationTemplate(new AssociateRole("A", true), null, new AssociateRole("B", false), "something");
    }

    /** Final clearing up: discard reference to the used language model. */
    @AfterClass
    public static void tearDown() {
        ModelHandlerImplTest.languageModel = null;
        ModelHandlerImplTest.defaultRelationTemplate = null;
    }

    /**
     * Create a {@link Pericope} model consisting of five {@link Proposition}s and initialize a new {@link ModelHandlerImpl} for it
     */
    @Before
    public void prepareModelHandler() {
        this.pericope = new Pericope();
        this.pericope.init("1  2 \t 3 \n 4 \t\t 5 \n 6 \n 7 \t 8 9 \n 10", ModelHandlerImplTest.languageModel, new Font("Arial", Font.PLAIN, 18));
        this.modelHandler = new ModelHandlerImpl(this.pericope);
    }

    /**
     * Test: for {@code setMetaData(String, String, String, String, int)}.
     */
    @Test
    public void testSetMetaData_1() {
        final String title = "Pericope Title applied via setMetaData()";
        final String author = "Someone who knows how";
        final String comment = "Optional comment,\ndescribing what this is all about.\nFor completeness sake...";
        final Font originTextFont = new Font("Dialog", Font.PLAIN, 16);
        this.modelHandler.setMetaData(title, author, comment, originTextFont.getFamily(), originTextFont.getSize());
        Assert.assertEquals(title, this.pericope.getTitle());
        Assert.assertEquals(author, this.pericope.getAuthor());
        Assert.assertEquals(comment, this.pericope.getComment());
        Assert.assertEquals(originTextFont, this.pericope.getFont());
    }

    /**
     * Test: for {@code setMetaData(String, String, String, String, int)}.
     */
    @Test
    public void testSetMetaData_2() {
        final String title = "Title Text";
        final String author = "";
        final String comment = "";
        final Font originTextFont = new Font("Dialog", Font.PLAIN, 18);
        this.modelHandler.setMetaData(title, author, comment, originTextFont.getFamily(), originTextFont.getSize());
        Assert.assertEquals(title, this.pericope.getTitle());
        Assert.assertEquals(author, this.pericope.getAuthor());
        Assert.assertEquals(comment, this.pericope.getComment());
        Assert.assertEquals(originTextFont, this.pericope.getFont());
    }

    /**
     * Test: for {@code indentPropositionUnderParent(Proposition, Proposition, SyntacticalFunction)}, making the first {@link Proposition} the single
     * prior child of its follower.
     *
     * @throws HmxException
     *             impossible to indent first under second top level {@link Proposition}
     */
    @Test
    public void testIndentPropositionUnderParent_1() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.indentPropositionUnderParent(first, second, function);
        Assert.assertSame(second, first.getParent());
        Assert.assertEquals(1, second.getPriorChildren().size());
        Assert.assertSame(first, second.getPriorChildren().get(0));
        Assert.assertSame(first, this.pericope.getPropositionAt(0));
        Assert.assertEquals(function, first.getFunction());
    }

    /**
     * Test: for {@code indentPropositionUnderParent(Proposition, Proposition, SyntacticalFunction)}, making the second {@link Proposition} the single
     * later child of its prior.
     *
     * @throws HmxException
     *             impossible to indent second under first top level {@link Proposition}
     */
    @Test
    public void testIndentPropositionUnderParent_2() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(1);
        this.modelHandler.indentPropositionUnderParent(second, first, function);
        Assert.assertSame(first, second.getParent());
        Assert.assertEquals(1, first.getLaterChildren().size());
        Assert.assertSame(second, first.getLaterChildren().get(0));
        Assert.assertSame(second, this.pericope.getPropositionAt(1));
        Assert.assertEquals(function, second.getFunction());
    }

    /**
     * Test: for {@code indentPropositionUnderParent(Proposition, Proposition, SyntacticalFunction)}, making the second and third {@link Proposition}s
     * the later children of their prior.
     *
     * @throws HmxException
     *             impossible to indent second or third under first top level {@link Proposition}
     */
    @Test
    public void testIndentPropositionUnderParent_3() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final SyntacticalFunction functionSecond =
                (SyntacticalFunction) ((SyntacticalFunctionGroup) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(2))
                        .getSubFunctions().get(0);
        final SyntacticalFunction functionThird =
                (SyntacticalFunction) ((SyntacticalFunctionGroup) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(2))
                        .getSubFunctions().get(1);
        this.modelHandler.indentPropositionUnderParent(second, first, functionSecond);
        this.modelHandler.indentPropositionUnderParent(third, first, functionThird);
        Assert.assertSame(first, third.getParent());
        Assert.assertEquals(2, first.getLaterChildren().size());
        Assert.assertSame(third, first.getLaterChildren().get(1));
        Assert.assertSame(third, this.pericope.getPropositionAt(2));
        Assert.assertEquals(functionThird, third.getFunction());
    }

    /**
     * Test: for {@code indentPropositionUnderParent(Proposition, Proposition, SyntacticalFunction)}, trying to make the third {@link Proposition} the
     * single later child of the first.
     *
     * @throws HmxException
     *             impossible to indent third under first top level {@link Proposition} (expected)
     */
    @Test(expected = HmxException.class)
    public void testIndentPropositionUnderParent_4() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition third = this.pericope.getPropositionAt(2);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.indentPropositionUnderParent(first, third, function);
    }

    /**
     * Test: for {@code indentPropositionUnderParent(Proposition, Proposition, SyntacticalFunction)}, trying to make the first {@link Proposition} the
     * single prior child of the third.
     *
     * @throws HmxException
     *             impossible to indent first under third top level {@link Proposition} (expected)
     */
    @Test(expected = HmxException.class)
    public void testIndentPropositionUnderParent_5() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition third = this.pericope.getPropositionAt(2);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(1);
        this.modelHandler.indentPropositionUnderParent(third, first, function);
    }

    /**
     * Test: for {@code indentPropositionUnderParent(Proposition, Proposition, SyntacticalFunction)}, trying to indent a top level {@link Proposition}
     * under its child's child.
     *
     * @throws HmxException
     *             impossible to indent {@link Proposition}s for setup, or failed to indent {@link Proposition} under its own child's child (latter is
     *             expected)
     */
    @Test(expected = HmxException.class)
    public void testIndentPropositionUnderParent_6() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.indentPropositionUnderParent(second, first, function);
        this.modelHandler.indentPropositionUnderParent(third, second, function);
        this.modelHandler.indentPropositionUnderParent(first, third, function);
    }

    /**
     * Test: of {@code indentPropositionUnderParent(Proposition, Proposition, SyntacticalFunction)} for neighboring indented {@link Proposition}s of
     * differing parents.
     *
     * @throws HmxException
     *             impossible to indent {@link Proposition}s for setup, or failed to indent neighboring {@link Proposition} that have not the same
     *             parent
     */
    @Test
    public void testIndentPropositionUnderParent_7() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final Proposition fourth = this.pericope.getPropositionAt(3);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.indentPropositionUnderParent(second, first, function);
        this.modelHandler.indentPropositionUnderParent(third, fourth, function);
        this.modelHandler.indentPropositionUnderParent(third, second, function);
        Assert.assertEquals(1, first.getLaterChildren().size());
        Assert.assertSame(second, first.getLaterChildren().get(0));
        Assert.assertEquals(1, second.getLaterChildren().size());
        Assert.assertSame(third, second.getLaterChildren().get(0));
        Assert.assertTrue(fourth.getPriorChildren().isEmpty());
    }

    /**
     * Test: of {@code indentPropositionUnderParent(Proposition, Proposition, SyntacticalFunction)} for neighboring indented {@link Proposition}s of
     * differing parents.
     *
     * @throws HmxException
     *             impossible to indent {@link Proposition}s for setup, or failed to indent neighboring {@link Proposition} that have not the same
     *             parent
     */
    @Test
    public void testIndentPropositionUnderParent_8() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final Proposition fourth = this.pericope.getPropositionAt(3);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.indentPropositionUnderParent(second, first, function);
        this.modelHandler.indentPropositionUnderParent(third, fourth, function);
        this.modelHandler.indentPropositionUnderParent(second, third, function);
        Assert.assertTrue(first.getLaterChildren().isEmpty());
        Assert.assertEquals(1, third.getPriorChildren().size());
        Assert.assertSame(second, third.getPriorChildren().get(0));
        Assert.assertEquals(1, fourth.getPriorChildren().size());
        Assert.assertSame(third, fourth.getPriorChildren().get(0));
    }

    /**
     * Test: of {@code indentPropositionUnderParent(Proposition, Proposition, SyntacticalFunction)} for the last {@link Proposition} in the
     * {@link Pericope}.
     *
     * @throws HmxException
     *             impossible to indent last {@link Proposition} under its prior
     */
    @Test
    public void testIndentPropositionUnderParent_9() throws HmxException {
        final Proposition fourth = this.pericope.getPropositionAt(3);
        final Proposition fifth = this.pericope.getPropositionAt(4);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.indentPropositionUnderParent(fifth, fourth, function);
        Assert.assertEquals(1, fourth.getLaterChildren().size());
        Assert.assertSame(fifth, fourth.getLaterChildren().get(0));
    }

    /**
     * Test: of {@code indentPropositionUnderParent(Proposition, Proposition, SyntacticalFunction)} for an already indented {@link Proposition},
     * changing only the {@link SyntacticalFunction}.
     *
     * @throws HmxException
     *             impossible to indent {@link Proposition}s for setup, or failed to change {@link SyntacticalFunction} on second iteration
     */
    @Test
    public void testIndentPropositionUnderParent_10() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final SyntacticalFunction functionOld = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        final SyntacticalFunction functionNew = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(1);
        this.modelHandler.indentPropositionUnderParent(first, second, functionOld);
        this.modelHandler.indentPropositionUnderParent(first, second, functionNew);
        Assert.assertEquals(1, second.getPriorChildren().size());
        Assert.assertSame(first, second.getPriorChildren().get(0));
        Assert.assertNotEquals(functionOld, first.getFunction());
        Assert.assertEquals(functionNew, first.getFunction());
    }

    /**
     * Test: of {@code indentPropositionUnderParent(Proposition, Proposition, SyntacticalFunction)} for the last {@link Proposition}.
     *
     * @throws HmxException
     *             impossible to indent {@link Proposition}s
     */
    @Test
    public void testIndentPropositionUnderParent_11() throws HmxException {
        final Proposition third = this.pericope.getPropositionAt(2);
        final Proposition fourth = this.pericope.getPropositionAt(3);
        final Proposition fifth = this.pericope.getPropositionAt(4);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.indentPropositionUnderParent(fourth, third, function);
        this.modelHandler.indentPropositionUnderParent(fifth, fourth, function);
        Assert.assertEquals(1, third.getLaterChildren().size());
        Assert.assertSame(fourth, third.getLaterChildren().get(0));
        Assert.assertEquals(1, fourth.getLaterChildren().size());
        Assert.assertSame(fifth, fourth.getLaterChildren().get(0));
    }

    /**
     * Test: of {@code indentPropositionUnderParent(Proposition, Proposition, SyntacticalFunction)} for unconnected {@link Proposition}s with
     * differing parents.
     *
     * @throws HmxException
     *             impossible to indent {@link Proposition}s for setup, or failed to indent unconnected {@link Proposition}s of differing parents
     *             (latter is expected)
     */
    @Test(expected = HmxException.class)
    public void testIndentPropositionUnderParent_12() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final Proposition fourth = this.pericope.getPropositionAt(3);
        final Proposition fifth = this.pericope.getPropositionAt(4);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.indentPropositionUnderParent(second, first, function);
        this.modelHandler.indentPropositionUnderParent(first, third, function);
        this.modelHandler.indentPropositionUnderParent(fourth, fifth, function);
        this.modelHandler.indentPropositionUnderParent(fifth, third, function);
        this.modelHandler.indentPropositionUnderParent(second, fourth, function);
    }

    /**
     * Test: of {@code indentPropositionUnderParent(Proposition, Proposition, SyntacticalFunction)} for the second of two {@link Proposition}s that
     * are enclosed by another two part {@link Proposition}.
     *
     * @throws HmxException
     *             impossible to merge {@link Proposition}s for setup, or failed to indent enclosed {@link Proposition} under {@code partAfterArrow}
     */
    @Test
    public void testIndentPropositionUnderParent_13() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final Proposition fourth = this.pericope.getPropositionAt(3);
        final SyntacticalFunction newFunction = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(1);
        this.modelHandler.mergePropositions(first, fourth);
        this.modelHandler.indentPropositionUnderParent(third, first, newFunction);
        Assert.assertEquals(2, fourth.getPriorChildren().size());
        Assert.assertSame(second, fourth.getPriorChildren().get(0));
        Assert.assertSame(third, fourth.getPriorChildren().get(1));
        Assert.assertEquals(newFunction, third.getFunction());
    }

    /**
     * Test: of {@code indentPropositionUnderParent(Proposition, Proposition, SyntacticalFunction)} for the second of two {@link Proposition}s that
     * are enclosed by another two part {@link Proposition}.
     *
     * @throws HmxException
     *             impossible to merge {@link Proposition}s for setup, or failed to indent enclosed {@link Proposition} under {@code partAfterArrow}
     */
    @Test
    public void testIndentPropositionUnderParent_14() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final Proposition fourth = this.pericope.getPropositionAt(3);
        final SyntacticalFunction initialFunction = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        final SyntacticalFunction newFunction = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(1);
        this.modelHandler.mergePropositions(first, fourth);
        this.modelHandler.indentPropositionUnderParent(third, second, initialFunction);
        this.modelHandler.indentPropositionUnderParent(third, first, newFunction);
        Assert.assertEquals(2, fourth.getPriorChildren().size());
        Assert.assertSame(second, fourth.getPriorChildren().get(0));
        Assert.assertSame(third, fourth.getPriorChildren().get(1));
        Assert.assertEquals(newFunction, third.getFunction());
    }

    /**
     * Test: of {@code removeOneIndentation(Proposition)} and {@code removeOneIndentationAffectsOthers(Proposition)} for a single prior child.
     *
     * @throws HmxException
     *             impossible to indent first under second top level {@link Proposition}, or indentation removal (check) failed
     */
    @Test
    public void testRemoveOneIndentation_1() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.indentPropositionUnderParent(first, second, function);
        Assert.assertFalse(this.modelHandler.removeOneIndentationAffectsOthers(first));
        this.modelHandler.removeOneIndentation(first);
        Assert.assertSame(this.pericope, first.getParent());
        Assert.assertTrue(second.getPriorChildren().isEmpty());
        Assert.assertNull(first.getFunction());
    }

    /**
     * Test: of {@code removeOneIndentation(Proposition)} and {@code removeOneIndentationAffectsOthers(Proposition)} for a single later child.
     *
     * @throws HmxException
     *             impossible to indent first under second top level {@link Proposition}, or indentation removal (check) failed
     */
    @Test
    public void testRemoveOneIndentation_2() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(1);
        this.modelHandler.indentPropositionUnderParent(second, first, function);
        Assert.assertFalse(this.modelHandler.removeOneIndentationAffectsOthers(second));
        this.modelHandler.removeOneIndentation(second);
        Assert.assertSame(this.pericope, second.getParent());
        Assert.assertTrue(first.getLaterChildren().isEmpty());
        Assert.assertNull(second.getFunction());
    }

    /**
     * Test: of {@code removeOneIndentationAffectsOthers(Proposition)} for a top level {@link Proposition}.
     *
     * @throws HmxException
     *             indentation removal check failed (expected)
     */
    @Test(expected = HmxException.class)
    public void testRemoveOneIndentation_3() throws HmxException {
        this.modelHandler.removeOneIndentationAffectsOthers(this.pericope.getPropositionAt(0));
    }

    /**
     * Test: of {@code removeOneIndentation(Proposition)} for a top level {@link Proposition}.
     *
     * @throws HmxException
     *             indentation removal failed (expected)
     */
    @Test(expected = HmxException.class)
    public void testRemoveOneIndentation_4() throws HmxException {
        this.modelHandler.removeOneIndentation(this.pericope.getPropositionAt(0));
    }

    /**
     * Test: of {@code removeOneIndentation(Proposition)} and {@code removeOneIndentationAffectsOthers(Proposition)} for the first of two prior
     * children.
     *
     * @throws HmxException
     *             impossible to indent first or second under third top level {@link Proposition}, or indentation removal (check) failed
     */
    @Test
    public void testRemoveOneIndentation_5() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final SyntacticalFunction functionSecond = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.indentPropositionUnderParent(second, third, functionSecond);
        this.modelHandler.indentPropositionUnderParent(first, third, (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions()
                .get(0).get(1));
        Assert.assertFalse(this.modelHandler.removeOneIndentationAffectsOthers(first));
        this.modelHandler.removeOneIndentation(first);
        Assert.assertSame(this.pericope, first.getParent());
        Assert.assertSame(third, second.getParent());
        Assert.assertEquals(1, third.getPriorChildren().size());
        Assert.assertSame(second, third.getPriorChildren().get(0));
        Assert.assertNull(first.getFunction());
        Assert.assertEquals(functionSecond, second.getFunction());
    }

    /**
     * Test: of {@code removeOneIndentation(Proposition)} and {@code removeOneIndentationAffectsOthers(Proposition)} for the second of two prior
     * children.
     *
     * @throws HmxException
     *             impossible to indent first or second under third top level {@link Proposition}, or indentation removal (check) failed
     */
    @Test
    public void testRemoveOneIndentation_6() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        this.modelHandler.indentPropositionUnderParent(second, third, (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions()
                .get(0).get(0));
        this.modelHandler.indentPropositionUnderParent(first, third, (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions()
                .get(0).get(1));
        Assert.assertTrue(this.modelHandler.removeOneIndentationAffectsOthers(second));
        this.modelHandler.removeOneIndentation(second);
        Assert.assertSame(this.pericope, first.getParent());
        Assert.assertSame(this.pericope, second.getParent());
        Assert.assertTrue(third.getPriorChildren().isEmpty());
        Assert.assertNull(first.getFunction());
        Assert.assertNull(second.getFunction());
    }

    /**
     * Test: of {@code removeOneIndentation(Proposition)} and {@code removeOneIndentationAffectsOthers(Proposition)} for the first of two later
     * children.
     *
     * @throws HmxException
     *             impossible to indent second or third under first top level {@link Proposition}, or indentation removal (check) failed
     */
    @Test
    public void testRemoveOneIndentation_7() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        this.modelHandler.indentPropositionUnderParent(second, first, (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions()
                .get(0).get(0));
        this.modelHandler.indentPropositionUnderParent(third, first, (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions()
                .get(0).get(1));
        Assert.assertTrue(this.modelHandler.removeOneIndentationAffectsOthers(second));
        this.modelHandler.removeOneIndentation(second);
        Assert.assertSame(this.pericope, second.getParent());
        Assert.assertSame(this.pericope, third.getParent());
        Assert.assertTrue(first.getLaterChildren().isEmpty());
        Assert.assertNull(second.getFunction());
        Assert.assertNull(third.getFunction());
    }

    /**
     * Test: of {@code removeOneIndentation(Proposition)} and {@code removeOneIndentationAffectsOthers(Proposition)} for the second of two later
     * children.
     *
     * @throws HmxException
     *             impossible to indent second or third under first top level {@link Proposition}, or indentation removal (check) failed
     */
    @Test
    public void testRemoveOneIndentation_8() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final SyntacticalFunction functionSecond = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.indentPropositionUnderParent(second, first, functionSecond);
        this.modelHandler.indentPropositionUnderParent(third, first, (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions()
                .get(0).get(1));
        Assert.assertFalse(this.modelHandler.removeOneIndentationAffectsOthers(third));
        this.modelHandler.removeOneIndentation(third);
        Assert.assertSame(first, second.getParent());
        Assert.assertSame(this.pericope, third.getParent());
        Assert.assertEquals(1, first.getLaterChildren().size());
        Assert.assertSame(second, first.getLaterChildren().get(0));
        Assert.assertEquals(functionSecond, second.getFunction());
        Assert.assertNull(third.getFunction());
    }

    /**
     * Test: of {@code removeOneIndentationAffectsOthers(Proposition)} for a single enclosed child.
     *
     * @throws HmxException
     *             impossible to merge first and second top level {@link Proposition}, or indentation removal check failed (latter is expected)
     */
    @Test(expected = HmxException.class)
    public void testRemoveOneIndentation_9() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        this.modelHandler.mergePropositions(first, third);
        this.modelHandler.removeOneIndentationAffectsOthers(second);
    }

    /**
     * Test: of {@code removeOneIndentation(Proposition)} for a single enclosed child.
     *
     * @throws HmxException
     *             impossible to merge first and third top level {@link Proposition}, or indentation removal failed (latter is expected)
     */
    @Test(expected = HmxException.class)
    public void testRemoveOneIndentation_10() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        this.modelHandler.mergePropositions(first, third);
        this.modelHandler.removeOneIndentation(second);
    }

    /**
     * Test: of {@code removeOneIndentation(Proposition)} for a twice indented prior child {@link Proposition}.
     *
     * @throws HmxException
     *             impossible to indent {@link Proposition}s for setup, or indentation removal failed
     */
    @Test
    public void testRemoveOneIndentation_11() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.indentPropositionUnderParent(first, second, function);
        this.modelHandler.indentPropositionUnderParent(second, third, (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions()
                .get(0).get(1));
        Assert.assertFalse(this.modelHandler.removeOneIndentationAffectsOthers(first));
        this.modelHandler.removeOneIndentation(first);
        Assert.assertEquals(2, third.getPriorChildren().size());
        Assert.assertSame(first, third.getPriorChildren().get(0));
        Assert.assertSame(second, third.getPriorChildren().get(1));
        Assert.assertEquals(function, first.getFunction());
    }

    /**
     * Test: of {@code removeOneIndentation(Proposition)} for a twice indented later child {@link Proposition}.
     *
     * @throws HmxException
     *             impossible to indent {@link Proposition}s for setup, or indentation removal failed
     */
    @Test
    public void testRemoveOneIndentation_12() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.indentPropositionUnderParent(third, second, (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions()
                .get(0).get(1));
        this.modelHandler.indentPropositionUnderParent(second, first, function);
        Assert.assertFalse(this.modelHandler.removeOneIndentationAffectsOthers(third));
        this.modelHandler.removeOneIndentation(third);
        Assert.assertEquals(2, first.getLaterChildren().size());
        Assert.assertSame(second, first.getLaterChildren().get(0));
        Assert.assertSame(third, first.getLaterChildren().get(1));
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for two neighboring top level {@link Proposition}s.
     *
     * @throws HmxException
     *             impossible to merge first and second top level {@link Proposition}
     */
    @Test
    public void testMergePropositions_1() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        this.modelHandler.createRelation(Arrays.asList(first, second), ModelHandlerImplTest.defaultRelationTemplate);
        final String labelFirst = "A-123";
        this.modelHandler.setLabelText(first, labelFirst);
        this.modelHandler.setLabelText(second, "B");
        this.modelHandler.mergePropositions(first, second);
        Assert.assertEquals(4, this.pericope.getText().size());
        final Proposition merged = this.pericope.getPropositionAt(0);
        Assert.assertEquals(Arrays.asList(new ClauseItem(merged, "1 2"), new ClauseItem(merged, "3"), new ClauseItem(merged, "4"), new ClauseItem(
                merged, "5")), merged.getItems());
        Assert.assertNull(merged.getSuperOrdinatedRelation());
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for first and third top level {@link Proposition}s with a single enclosed child.
     *
     * @throws HmxException
     *             impossible to merge first and third top level {@link Proposition}
     */
    @Test
    public void testMergePropositions_2() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        this.modelHandler.createRelation(Arrays.asList(second, third), ModelHandlerImplTest.defaultRelationTemplate);
        this.modelHandler.mergePropositions(first, third);
        Assert.assertEquals(3, this.pericope.getText().size());
        Assert.assertSame(third, first.getPartAfterArrow());
        Assert.assertSame(first, third.getPartBeforeArrow());
        Assert.assertEquals(1, third.getPriorChildren().size());
        Assert.assertSame(second, third.getPriorChildren().get(0));
        Assert.assertSame(third, this.pericope.getPropositionAt(2));
        Assert.assertNull(second.getSuperOrdinatedRelation());
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for a single prior child with its parent {@link Proposition}.
     *
     * @throws HmxException
     *             impossible to merge first and second {@link Proposition}
     */
    @Test
    public void testMergePropositions_3() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        this.modelHandler.createRelation(Arrays.asList(second, third), ModelHandlerImplTest.defaultRelationTemplate);
        final Relation relation = second.getSuperOrdinatedRelation();
        this.modelHandler.indentPropositionUnderParent(first, second, (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions()
                .get(0).get(0));
        this.modelHandler.mergePropositions(first, second);
        Assert.assertEquals(4, this.pericope.getText().size());
        final Proposition merged = this.pericope.getPropositionAt(0);
        Assert.assertEquals(Arrays.asList(new ClauseItem(merged, "1 2"), new ClauseItem(merged, "3"), new ClauseItem(merged, "4"), new ClauseItem(
                merged, "5")), merged.getItems());
        Assert.assertNull(merged.getFunction());
        Assert.assertSame(relation, merged.getSuperOrdinatedRelation());
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for a single later child with its parent {@link Proposition}.
     *
     * @throws HmxException
     *             impossible to merge first and second {@link Proposition}
     */
    @Test
    public void testMergePropositions_4() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        this.modelHandler.indentPropositionUnderParent(second, first, (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions()
                .get(0).get(0));
        this.modelHandler.mergePropositions(second, first);
        Assert.assertEquals(4, this.pericope.getText().size());
        final Proposition merged = this.pericope.getPropositionAt(0);
        Assert.assertEquals(Arrays.asList(new ClauseItem(merged, "1 2"), new ClauseItem(merged, "3"), new ClauseItem(merged, "4"), new ClauseItem(
                merged, "5")), merged.getItems());
        Assert.assertNull(merged.getFunction());
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for a single later child with the following prior child {@link Proposition}.
     *
     * @throws HmxException
     *             impossible to merge first and second {@link Proposition}
     */
    @Test
    public void testMergePropositions_5() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final Proposition fourth = this.pericope.getPropositionAt(3);
        final String label = "A-123";
        this.modelHandler.setLabelText(second, label);
        this.modelHandler.setLabelText(third, "B");
        final SyntacticalFunction functionSecond = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.indentPropositionUnderParent(second, first, functionSecond);
        this.modelHandler.indentPropositionUnderParent(third, fourth, (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions()
                .get(0).get(1));
        this.modelHandler.mergePropositions(second, third);
        Assert.assertEquals(3, this.pericope.getText().size());
        final Proposition merged = this.pericope.getPropositionAt(1);
        Assert.assertEquals(Arrays.asList(new ClauseItem(merged, "4"), new ClauseItem(merged, "5"), new ClauseItem(merged, "6")),
                merged.getItems());
        Assert.assertEquals(functionSecond, merged.getFunction());
        Assert.assertEquals(label, merged.getLabel());
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for a top level {@link Proposition} with the following single prior child.
     *
     * @throws HmxException
     *             impossible to merge first and second {@link Proposition}
     */
    @Test
    public void testMergePropositions_6() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        this.modelHandler.indentPropositionUnderParent(second, third, (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions()
                .get(0).get(0));
        this.modelHandler.mergePropositions(first, second);
        Assert.assertEquals(4, this.pericope.getText().size());
        final Proposition merged = this.pericope.getPropositionAt(0);
        Assert.assertEquals(Arrays.asList(new ClauseItem(merged, "1 2"), new ClauseItem(merged, "3"), new ClauseItem(merged, "4"), new ClauseItem(
                merged, "5")), merged.getItems());
        Assert.assertNull(merged.getFunction());
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for a single later child with the following top level {@link Proposition}.
     *
     * @throws HmxException
     *             impossible to merge first and second {@link Proposition}
     */
    @Test
    public void testMergePropositions_7() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final SyntacticalFunction functionSecond = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.indentPropositionUnderParent(second, first, functionSecond);
        this.modelHandler.mergePropositions(second, third);
        Assert.assertEquals(3, this.pericope.getText().size());
        final Proposition merged = this.pericope.getPropositionAt(1);
        Assert.assertEquals(Arrays.asList(new ClauseItem(merged, "4"), new ClauseItem(merged, "5"), new ClauseItem(merged, "6")),
                merged.getItems());
        Assert.assertEquals(functionSecond, merged.getFunction());
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for enclosed children with a nested enclosed child.
     *
     * @throws HmxException
     *             impossible to merge
     */
    @Test
    public void testMergePropositions_8() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final Proposition fourth = this.pericope.getPropositionAt(3);
        final Proposition fifth = this.pericope.getPropositionAt(4);
        this.modelHandler.mergePropositions(fourth, second);
        this.modelHandler.mergePropositions(first, fifth);
        this.modelHandler.mergePropositions(fourth, fifth);
        Assert.assertEquals(3, this.pericope.getFlatText().size());
        final Proposition merged = this.pericope.getPropositionAt(0);
        Assert.assertEquals(Arrays.asList(new ClauseItem(merged, "1 2"), new ClauseItem(merged, "3"), new ClauseItem(merged, "4"), new ClauseItem(
                merged, "5")), merged.getItems());
        final Proposition mergedAfterArrow = merged.getPartAfterArrow();
        Assert.assertNotNull(mergedAfterArrow);
        Assert.assertEquals(Arrays.asList(new ClauseItem(mergedAfterArrow, "7"), new ClauseItem(mergedAfterArrow, "8 9"), new ClauseItem(
                mergedAfterArrow, "10")), mergedAfterArrow.getItems());
        Assert.assertEquals(1, mergedAfterArrow.getPriorChildren().size());
        Assert.assertSame(third, mergedAfterArrow.getPriorChildren().get(0));
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for enclosed children with two nested enclosed child.
     *
     * @throws HmxException
     *             impossible to merge
     */
    @Test
    public void testMergePropositions_9() throws HmxException {
        this.modelHandler.addNewPropositions("11", false);
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final Proposition fourth = this.pericope.getPropositionAt(3);
        final Proposition fifth = this.pericope.getPropositionAt(4);
        final Proposition sixth = this.pericope.getPropositionAt(5);
        this.modelHandler.mergePropositions(second, fourth);
        this.modelHandler.mergePropositions(first, sixth);
        this.modelHandler.mergePropositions(first, second);
        Assert.assertEquals(5, this.pericope.getFlatText().size());
        final Proposition merged = this.pericope.getPropositionAt(0);
        Assert.assertEquals(Arrays.asList(new ClauseItem(merged, "1 2"), new ClauseItem(merged, "3"), new ClauseItem(merged, "4"), new ClauseItem(
                merged, "5")), merged.getItems());
        final Proposition mergedAfterArrow = merged.getPartAfterArrow();
        Assert.assertNotNull(mergedAfterArrow);
        Assert.assertEquals(Arrays.asList(new ClauseItem(mergedAfterArrow, "7"), new ClauseItem(mergedAfterArrow, "8 9")),
                mergedAfterArrow.getItems());
        Assert.assertEquals(1, mergedAfterArrow.getPriorChildren().size());
        Assert.assertSame(third, mergedAfterArrow.getPriorChildren().get(0));
        final Proposition mergedThirdPart = mergedAfterArrow.getPartAfterArrow();
        Assert.assertNotNull(mergedThirdPart);
        Assert.assertEquals(1, sixth.getPriorChildren().size());
        Assert.assertSame(fifth, sixth.getPriorChildren().get(0));
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for a {@link Proposition} with two enclosed children, merging with the first.
     *
     * @throws HmxException
     *             impossible to merge
     */
    @Test
    public void testMergePropositions_10() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final Proposition fourth = this.pericope.getPropositionAt(3);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.indentPropositionUnderParent(second, first, function);
        this.modelHandler.indentPropositionUnderParent(third, first, function);
        this.modelHandler.mergePropositions(first, fourth);
        this.modelHandler.mergePropositions(first, second);
        Assert.assertEquals(4, this.pericope.getFlatText().size());
        final Proposition merged = this.pericope.getPropositionAt(0);
        Assert.assertEquals(Arrays.asList(new ClauseItem(merged, "1 2"), new ClauseItem(merged, "3"), new ClauseItem(merged, "4"), new ClauseItem(
                merged, "5")), merged.getItems());
        Assert.assertTrue(merged.getLaterChildren().isEmpty());
        final Proposition mergedAfterArrow = merged.getPartAfterArrow();
        Assert.assertNotNull(mergedAfterArrow);
        Assert.assertEquals(Arrays.asList(new ClauseItem(mergedAfterArrow, "7"), new ClauseItem(mergedAfterArrow, "8 9")),
                mergedAfterArrow.getItems());
        Assert.assertEquals(1, mergedAfterArrow.getPriorChildren().size());
        Assert.assertSame(third, mergedAfterArrow.getPriorChildren().get(0));
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for a {@link Proposition} with two enclosed children, merging with the second.
     *
     * @throws HmxException
     *             impossible to merge
     */
    @Test
    public void testMergePropositions_11() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final Proposition fourth = this.pericope.getPropositionAt(3);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.indentPropositionUnderParent(second, first, function);
        this.modelHandler.indentPropositionUnderParent(third, first, function);
        this.modelHandler.mergePropositions(first, fourth);
        this.modelHandler.mergePropositions(first, third);
        Assert.assertEquals(4, this.pericope.getFlatText().size());
        final Proposition merged = this.pericope.getPropositionAt(0);
        Assert.assertEquals(Arrays.asList(new ClauseItem(merged, "1 2"), new ClauseItem(merged, "3")), merged.getItems());
        Assert.assertTrue(merged.getLaterChildren().isEmpty());
        final Proposition mergedAfterArrow = merged.getPartAfterArrow();
        Assert.assertNotNull(mergedAfterArrow);
        Assert.assertEquals(Arrays.asList(new ClauseItem(mergedAfterArrow, "6"), new ClauseItem(mergedAfterArrow, "7"), new ClauseItem(
                mergedAfterArrow, "8 9")), mergedAfterArrow.getItems());
        Assert.assertEquals(1, mergedAfterArrow.getPriorChildren().size());
        Assert.assertSame(second, mergedAfterArrow.getPriorChildren().get(0));
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for a {@link Proposition} merging with its only enclosed child.
     *
     * @throws HmxException
     *             impossible to merge
     */
    @Test
    public void testMergePropositions_12() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final Proposition fourth = this.pericope.getPropositionAt(3);
        this.modelHandler.mergePropositions(first, third);
        this.modelHandler.mergePropositions(first, second);
        Assert.assertEquals(3, this.pericope.getFlatText().size());
        final Proposition merged = this.pericope.getPropositionAt(0);
        Assert.assertEquals(Arrays.asList(new ClauseItem(merged, "1 2"), new ClauseItem(merged, "3"), new ClauseItem(merged, "4"), new ClauseItem(
                merged, "5"), new ClauseItem(merged, "6")), merged.getItems());
        Assert.assertSame(fourth, this.pericope.getPropositionAt(1));
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for a {@link Proposition} merging with its only enclosed child.
     *
     * @throws HmxException
     *             impossible to merge
     */
    @Test
    public void testMergePropositions_13() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final Proposition fourth = this.pericope.getPropositionAt(3);
        this.modelHandler.indentPropositionUnderParent(second, first, (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions()
                .get(0).get(0));
        this.modelHandler.mergePropositions(first, third);
        this.modelHandler.mergePropositions(first, second);
        Assert.assertEquals(3, this.pericope.getFlatText().size());
        final Proposition merged = this.pericope.getPropositionAt(0);
        Assert.assertEquals(Arrays.asList(new ClauseItem(merged, "1 2"), new ClauseItem(merged, "3"), new ClauseItem(merged, "4"), new ClauseItem(
                merged, "5"), new ClauseItem(merged, "6")), merged.getItems());
        Assert.assertSame(fourth, this.pericope.getPropositionAt(1));
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for a {@link Proposition} merging with the first of its two later children.
     *
     * @throws HmxException
     *             impossible to merge
     */
    @Test
    public void testMergePropositions_14() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.indentPropositionUnderParent(second, first, function);
        this.modelHandler.indentPropositionUnderParent(third, first, function);
        final String semTranslation = "semantical translation...";
        this.modelHandler.setSemTranslation(first, semTranslation);
        final String synTranslation = "Syntactical Translation Text";
        this.modelHandler.setSynTranslation(second, synTranslation);
        this.modelHandler.mergePropositions(first, second);
        Assert.assertEquals(4, this.pericope.getFlatText().size());
        final Proposition merged = this.pericope.getPropositionAt(0);
        Assert.assertEquals(Arrays.asList(new ClauseItem(merged, "1 2"), new ClauseItem(merged, "3"), new ClauseItem(merged, "4"), new ClauseItem(
                merged, "5")), merged.getItems());
        Assert.assertEquals(1, first.getLaterChildren().size());
        Assert.assertSame(third, first.getLaterChildren().get(0));
        Assert.assertEquals(semTranslation, first.getSemTranslation());
        Assert.assertEquals(synTranslation, first.getSynTranslation());
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for a {@link Proposition} in three parts with two separate enclosed children.
     *
     * @throws HmxException
     *             impossible to merge
     */
    @Test
    public void testMergePropositions_15() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final Proposition fourth = this.pericope.getPropositionAt(3);
        final Proposition fifth = this.pericope.getPropositionAt(4);
        this.modelHandler.mergePropositions(first, third);
        this.modelHandler.mergePropositions(fifth, first);
        Assert.assertEquals(5, this.pericope.getFlatText().size());
        Assert.assertEquals(1, this.pericope.getText().size());
        Assert.assertSame(third, first.getPartAfterArrow());
        Assert.assertEquals(1, third.getPriorChildren().size());
        Assert.assertSame(second, third.getPriorChildren().get(0));
        Assert.assertSame(fifth, third.getPartAfterArrow());
        Assert.assertEquals(1, fifth.getPriorChildren().size());
        Assert.assertSame(fourth, fifth.getPriorChildren().get(0));
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for a {@link Proposition} in three parts with two separate enclosed children.
     *
     * @throws HmxException
     *             impossible to merge
     */
    @Test
    public void testMergePropositions_16() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final Proposition fourth = this.pericope.getPropositionAt(3);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.indentPropositionUnderParent(second, first, function);
        this.modelHandler.indentPropositionUnderParent(third, fourth, function);
        this.modelHandler.mergePropositions(first, fourth);
        Assert.assertEquals(5, this.pericope.getFlatText().size());
        Assert.assertEquals(2, this.pericope.getText().size());
        Assert.assertSame(fourth, first.getPartAfterArrow());
        Assert.assertEquals(2, fourth.getPriorChildren().size());
        Assert.assertSame(second, fourth.getPriorChildren().get(0));
        Assert.assertSame(third, fourth.getPriorChildren().get(1));
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for a {@link Proposition} to be merged with itself.
     *
     * @throws HmxException
     *             impossible to merge with itself
     */
    @Test
    public void testMergePropositions_17() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        this.modelHandler.mergePropositions(first, first);
        // nothing is supposed to happen
        Assert.assertEquals(5, this.pericope.getText().size());
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for unconnected {@link Proposition} that are not on the same level.
     *
     * @throws HmxException
     *             impossible to merge with itself
     */
    @Test(expected = HmxException.class)
    public void testMergePropositions_18() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        this.modelHandler.indentPropositionUnderParent(third, second, (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions()
                .get(0).get(0));
        this.modelHandler.mergePropositions(first, third);
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for a {@link Proposition} with two enclosed children, merging the enclosed
     * children.
     *
     * @throws HmxException
     *             impossible to merge
     */
    @Test
    public void testMergePropositions_19() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final Proposition fourth = this.pericope.getPropositionAt(3);
        final Proposition fifth = this.pericope.getPropositionAt(4);
        this.modelHandler.mergePropositions(first, fourth);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.setSyntacticalFunction(third, function);
        this.modelHandler.createRelation(Arrays.asList(third, fifth), ModelHandlerImplTest.defaultRelationTemplate);
        final Relation relation = fifth.getSuperOrdinatedRelation();
        this.modelHandler.mergePropositions(second, third);
        Assert.assertEquals(4, this.pericope.getFlatText().size());
        final Proposition merged = this.pericope.getPropositionAt(1);
        Assert.assertEquals(Arrays.asList(new ClauseItem(merged, "4"), new ClauseItem(merged, "5"), new ClauseItem(merged, "6")),
                merged.getItems());
        Assert.assertTrue(merged.getLaterChildren().isEmpty());
        Assert.assertEquals(function, merged.getFunction());
        Assert.assertSame(relation, merged.getSuperOrdinatedRelation());
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for a prior child with the same parent {@link Proposition}'s later child.
     *
     * @throws HmxException
     *             impossible to indent {@link Proposition}s for setup, or failed to merge prior and later child of the same parent (latter expected)
     */
    @Test(expected = HmxException.class)
    public void testMergePropositions_20() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.indentPropositionUnderParent(first, second, function);
        this.modelHandler.indentPropositionUnderParent(third, second, function);
        this.modelHandler.mergePropositions(first, third);
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for the first of two enclosed child {@link Proposition}s.
     *
     * @throws HmxException
     *             impossible to merge
     */
    @Test
    public void testMergePropositions_21() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final Proposition fourth = this.pericope.getPropositionAt(3);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.indentPropositionUnderParent(second, third, function);
        this.modelHandler.mergePropositions(first, fourth);
        this.modelHandler.indentPropositionUnderParent(second, first, function);
        Assert.assertTrue(first.getLaterChildren().isEmpty());
        Assert.assertEquals(2, fourth.getPriorChildren().size());
        Assert.assertSame(second, fourth.getPriorChildren().get(0));
        Assert.assertSame(third, fourth.getPriorChildren().get(1));
        this.modelHandler.mergePropositions(first, second);
        Assert.assertEquals(4, this.pericope.getFlatText().size());
        final Proposition merged = this.pericope.getPropositionAt(0);
        Assert.assertEquals(Arrays.asList(new ClauseItem(merged, "1 2"), new ClauseItem(merged, "3"), new ClauseItem(merged, "4"), new ClauseItem(
                merged, "5")), merged.getItems());
        Assert.assertSame(fourth, merged.getPartAfterArrow());
        Assert.assertEquals(1, fourth.getPriorChildren().size());
        Assert.assertSame(third, fourth.getPriorChildren().get(0));
    }

    /**
     * Test: of {@code mergePropositions(Proposition, Proposition)} for the three of three enclosed child {@link Proposition}s which is in turn
     * indented under the first enclosed child.
     *
     * @throws HmxException
     *             impossible to indent {@link Proposition}s for setup, or failed to merge
     */
    @Test
    public void testMergePropositions_22() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final Proposition fourth = this.pericope.getPropositionAt(3);
        final Proposition fifth = this.pericope.getPropositionAt(4);
        final SyntacticalFunction function = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.mergePropositions(first, fifth);
        this.modelHandler.indentPropositionUnderParent(third, fourth, function);
        this.modelHandler.indentPropositionUnderParent(fourth, second, function);
        this.modelHandler.mergePropositions(first, fourth);
        Assert.assertEquals(4, this.pericope.getFlatText().size());
        Assert.assertTrue(first.getLaterChildren().isEmpty());
        final Proposition merged = this.pericope.getPropositionAt(3);
        Assert.assertEquals(2, merged.getPriorChildren().size());
        Assert.assertSame(second, merged.getPriorChildren().get(0));
        Assert.assertSame(third, merged.getPriorChildren().get(1));
        Assert.assertSame(merged, first.getPartAfterArrow());
        Assert.assertSame(first, merged.getPartBeforeArrow());
        Assert.assertEquals(function, third.getFunction());
        Assert.assertNull(merged.getFunction());
        Assert.assertEquals(Arrays.asList(new ClauseItem(merged, "7"), new ClauseItem(merged, "8 9"), new ClauseItem(merged, "10")),
                merged.getItems());
    }

    /**
     * Test: of {@code splitProposition(Proposition, ClauseItem)} for a top level {@link Proposition}, after the first of two {@link ClauseItem}s.
     *
     * @throws HmxException
     *             impossible to split {@link Proposition} after first of two items
     */
    @Test
    public void testSplitProposition_1() throws HmxException {
        final Proposition proposition = this.pericope.getPropositionAt(0);
        final ClauseItem itemFirst = proposition.getItems().get(0);
        final ClauseItem itemSecond = proposition.getItems().get(1);
        final String translation = "syntactical Translation Text";
        this.modelHandler.setSynTranslation(proposition, translation);
        this.modelHandler.splitProposition(proposition, itemFirst);
        Assert.assertEquals(6, this.pericope.getText().size());
        Assert.assertEquals(Collections.singletonList(itemFirst), proposition.getItems());
        final Proposition split = this.pericope.getPropositionAt(1);
        Assert.assertEquals(Collections.singletonList(itemSecond), split.getItems());
        Assert.assertEquals(translation, proposition.getSynTranslation());
        Assert.assertNull(split.getSynTranslation());
    }

    /**
     * Test: of {@code splitProposition(Proposition, ClauseItem)} for a top level {@link Proposition}, after the second of two {@link ClauseItem}s.
     *
     * @throws HmxException
     *             impossible to split {@link Proposition} after last item (expected)
     */
    @Test(expected = HmxException.class)
    public void testSplitProposition_2() throws HmxException {
        final Proposition proposition = this.pericope.getPropositionAt(0);
        final ClauseItem second = proposition.getItems().get(1);
        this.modelHandler.splitProposition(proposition, second);
    }

    /**
     * Test: of {@code splitProposition(Proposition, ClauseItem)} after a {@link ClauseItem} from a different {@link Proposition}.
     *
     * @throws HmxException
     *             impossible to split {@link Proposition}
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSplitProposition_3() throws HmxException {
        this.modelHandler.splitProposition(this.pericope.getPropositionAt(0), this.pericope.getPropositionAt(1).getItems().get(0));
    }

    /**
     * Test: of {@code splitProposition(Proposition, ClauseItem)} after the last item of the first part of a {@link Proposition} with an enclosed
     * child.
     *
     * @throws HmxException
     *             impossible to split {@link Proposition} part after first of two items
     */
    @Test
    public void testSplitProposition_4() throws HmxException {
        final Proposition propositionFirst = this.pericope.getPropositionAt(0);
        final Proposition propositionSecond = this.pericope.getPropositionAt(1);
        final Proposition propositionThird = this.pericope.getPropositionAt(2);
        final Proposition propositionFourth = this.pericope.getPropositionAt(3);
        final Proposition propositionFifth = this.pericope.getPropositionAt(4);
        this.modelHandler.mergePropositions(propositionFirst, propositionThird);
        this.modelHandler.createRelation(Arrays.asList(propositionFourth, propositionFifth), ModelHandlerImplTest.defaultRelationTemplate);
        final Relation survivingRelation = propositionFourth.getSuperOrdinatedRelation();
        this.modelHandler.createRelation(Arrays.asList(propositionSecond, survivingRelation), ModelHandlerImplTest.defaultRelationTemplate);
        final ClauseItem itemLast = propositionFirst.getItems().get(1);
        this.modelHandler.splitProposition(propositionFirst, itemLast);
        Assert.assertEquals(5, this.pericope.getFlatText().size());
        Assert.assertNull(propositionFirst.getPartAfterArrow());
        Assert.assertNull(propositionThird.getPartBeforeArrow());
        Assert.assertSame(survivingRelation, propositionFourth.getSuperOrdinatedRelation());
        Assert.assertSame(survivingRelation, propositionFifth.getSuperOrdinatedRelation());
        Assert.assertNull(survivingRelation.getSuperOrdinatedRelation());
        Assert.assertNull(propositionSecond.getSuperOrdinatedRelation());
    }

    /**
     * Test: of {@code splitProposition(Proposition, ClauseItem)} after the single item of the first part of a {@link Proposition} with an enclosed
     * child.
     *
     * @throws HmxException
     *             impossible to split {@link Proposition} part after first of two items
     */
    @Test
    public void testSplitProposition_5() throws HmxException {
        final Proposition propositionThird = this.pericope.getPropositionAt(2);
        final Proposition propositionFifth = this.pericope.getPropositionAt(4);
        this.modelHandler.mergePropositions(propositionThird, propositionFifth);
        this.modelHandler.splitProposition(propositionThird, propositionThird.getItems().get(0));
        Assert.assertEquals(5, this.pericope.getFlatText().size());
        Assert.assertNull(propositionThird.getPartAfterArrow());
        Assert.assertNull(propositionFifth.getPartBeforeArrow());
    }

    /**
     * Test: of {@code splitProposition(Proposition, ClauseItem)} for a top level {@link Proposition}, after the first of two {@link ClauseItem}s,
     * with super ordinated {@link Relation}s.
     *
     * @throws HmxException
     *             impossible to split {@link Proposition} after first of two items
     */
    @Test
    public void testSplitProposition_6() throws HmxException {
        final Proposition propositionFirst = this.pericope.getPropositionAt(0);
        final Proposition propositionSecond = this.pericope.getPropositionAt(1);
        final Proposition propositionThird = this.pericope.getPropositionAt(2);
        this.modelHandler.createRelation(Arrays.asList(propositionFirst, propositionSecond), ModelHandlerImplTest.defaultRelationTemplate);
        final Relation survivingRelation = propositionSecond.getSuperOrdinatedRelation();
        this.modelHandler.createRelation(Arrays.asList(survivingRelation, propositionThird), ModelHandlerImplTest.defaultRelationTemplate);
        final ClauseItem itemFirst = propositionSecond.getItems().get(0);
        final ClauseItem itemSecond = propositionSecond.getItems().get(1);
        this.modelHandler.splitProposition(propositionSecond, itemFirst);
        Assert.assertEquals(6, this.pericope.getText().size());
        Assert.assertEquals(Collections.singletonList(itemFirst), propositionSecond.getItems());
        final Proposition split = this.pericope.getPropositionAt(2);
        Assert.assertEquals(Collections.singletonList(itemSecond), split.getItems());
        Assert.assertSame(survivingRelation, propositionFirst.getSuperOrdinatedRelation());
        Assert.assertSame(survivingRelation, propositionSecond.getSuperOrdinatedRelation());
        Assert.assertNull(survivingRelation.getSuperOrdinatedRelation());
        Assert.assertNull(propositionThird.getSuperOrdinatedRelation());
    }

    /**
     * Test: of {@code splitProposition(Proposition, ClauseItem)} after the first item of the first part of a {@link Proposition} with two enclosed
     * children.
     *
     * @throws HmxException
     *             impossible to split {@link Proposition} part after first of two items
     */
    @Test
    public void testSplitProposition_7() throws HmxException {
        final Proposition propositionFirst = this.pericope.getPropositionAt(0);
        final Proposition propositionSecond = this.pericope.getPropositionAt(1);
        final Proposition propositionThird = this.pericope.getPropositionAt(2);
        final Proposition propositionFourth = this.pericope.getPropositionAt(3);
        final Proposition propositionFifth = this.pericope.getPropositionAt(4);
        this.modelHandler.mergePropositions(propositionFirst, propositionFourth);
        this.modelHandler.createRelation(Arrays.asList(propositionFirst, propositionSecond), ModelHandlerImplTest.defaultRelationTemplate);
        final Relation survivingLeadRelation = propositionSecond.getSuperOrdinatedRelation();
        this.modelHandler.createRelation(Arrays.asList(propositionThird, propositionFifth), ModelHandlerImplTest.defaultRelationTemplate);
        final Relation survivingTrailRelation = propositionThird.getSuperOrdinatedRelation();
        this.modelHandler.createRelation(Arrays.asList(survivingLeadRelation, survivingTrailRelation), ModelHandlerImplTest.defaultRelationTemplate);
        final Relation rootRelation = survivingLeadRelation.getSuperOrdinatedRelation();
        final ClauseItem itemFirst = propositionFirst.getItems().get(0);
        final ClauseItem itemSecond = propositionFirst.getItems().get(1);
        this.modelHandler.splitProposition(propositionFirst, itemFirst);
        final List<Proposition> newPropositions = this.pericope.getFlatText();
        Assert.assertEquals(6, newPropositions.size());
        Assert.assertEquals(Collections.singletonList(itemFirst), newPropositions.get(0).getItems());
        Assert.assertEquals(Collections.singletonList(itemSecond), newPropositions.get(1).getItems());
        Assert.assertSame(propositionSecond, newPropositions.get(2));
        Assert.assertSame(propositionThird, newPropositions.get(3));
        Assert.assertSame(propositionFourth, newPropositions.get(4));
        Assert.assertSame(propositionFifth, newPropositions.get(5));
        Assert.assertNull(newPropositions.get(0).getPartAfterArrow());
        Assert.assertSame(propositionFourth, newPropositions.get(1).getPartAfterArrow());
        Assert.assertSame(newPropositions.get(1), propositionFourth.getPartBeforeArrow());
        Assert.assertNull(newPropositions.get(0).getSuperOrdinatedRelation());
        Assert.assertSame(survivingLeadRelation, newPropositions.get(1).getSuperOrdinatedRelation());
        Assert.assertSame(survivingLeadRelation, propositionSecond.getSuperOrdinatedRelation());
        Assert.assertSame(survivingTrailRelation, propositionThird.getSuperOrdinatedRelation());
        Assert.assertSame(survivingTrailRelation, propositionFifth.getSuperOrdinatedRelation());
        Assert.assertNotNull(rootRelation);
        Assert.assertSame(rootRelation, survivingLeadRelation.getSuperOrdinatedRelation());
        Assert.assertSame(rootRelation, survivingTrailRelation.getSuperOrdinatedRelation());
    }

    /**
     * Test: of {@code resetStandaloneStateOfPartAfterArrow(Proposition)} for two top level {@link Proposition}s with a single enclosed child.
     *
     * @throws HmxException
     *             impossible o merge first and third top level {@link Proposition}, or resetting third's standalone state failed
     */
    @Test
    public void testResetStandaloneStateOfPartAfterArrow() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        this.modelHandler.mergePropositions(first, third);
        this.modelHandler.resetStandaloneStateOfPartAfterArrow(third);
        Assert.assertEquals(4, this.pericope.getText().size());
        Assert.assertNull(first.getPartAfterArrow());
        Assert.assertNull(third.getPartBeforeArrow());
        Assert.assertEquals(1, third.getPriorChildren().size());
        Assert.assertSame(second, third.getPriorChildren().get(0));
        Assert.assertSame(third, this.pericope.getPropositionAt(2));
    }

    /**
     * Test: of {@code mergeClauseItemWithPrior(ClauseItem)} for the second of two {@link ClauseItem}s, both having an assigned
     * {@link SyntacticalFunction}.
     *
     * @throws HmxException
     *             no preceding first item to merge with
     */
    @Test
    public void testMergeClauseItemWithPrior_1() throws HmxException {
        final Proposition proposition = this.pericope.getPropositionAt(0);
        final ClauseItem first = proposition.getItems().get(0);
        final ClauseItem second = proposition.getItems().get(1);
        final SyntacticalFunction functionFirst = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.setSyntacticalFunction(first, functionFirst);
        this.modelHandler
                .setSyntacticalFunction(second, (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(1));
        this.modelHandler.mergeClauseItemWithPrior(second);
        Assert.assertEquals(1, proposition.getItems().size());
        Assert.assertEquals(functionFirst, proposition.getItems().get(0).getFunction());
    }

    /**
     * Test: of {@code mergeClauseItemWithPrior(ClauseItem)} for the second of two {@link ClauseItem}s, the first not having an assigned
     * {@link SyntacticalFunction}.
     *
     * @throws HmxException
     *             no preceding first item to merge with
     */
    @Test
    public void testMergeClauseItemWithPrior_2() throws HmxException {
        final Proposition proposition = this.pericope.getPropositionAt(0);
        final ClauseItem second = proposition.getItems().get(1);
        final SyntacticalFunction functionSecond = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(1);
        this.modelHandler.setSyntacticalFunction(second, functionSecond);
        this.modelHandler.mergeClauseItemWithPrior(second);
        Assert.assertEquals(1, proposition.getItems().size());
        Assert.assertEquals(functionSecond, proposition.getItems().get(0).getFunction());
    }

    /**
     * Test: of {@code mergeClauseItemWithPrior(ClauseItem)} for the first of two {@link ClauseItem}s.
     *
     * @throws HmxException
     *             no preceding first item to merge with (expected)
     */
    @Test(expected = HmxException.class)
    public void testMergeClauseItemWithPrior_3() throws HmxException {
        final Proposition proposition = this.pericope.getPropositionAt(0);
        this.modelHandler.mergeClauseItemWithPrior(proposition.getItems().get(0));
    }

    /**
     * Test: of {@code mergeClauseItemWithFollower(ClauseItem)} for the first of two {@link ClauseItem}s, both having an assigned
     * {@link SyntacticalFunction}.
     *
     * @throws HmxException
     *             no following second item to merge with
     */
    @Test
    public void testMergeClauseItemWithFollower_1() throws HmxException {
        final Proposition proposition = this.pericope.getPropositionAt(0);
        final ClauseItem first = proposition.getItems().get(0);
        final ClauseItem second = proposition.getItems().get(1);
        final SyntacticalFunction functionFirst = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(0);
        this.modelHandler.setSyntacticalFunction(first, functionFirst);
        this.modelHandler
                .setSyntacticalFunction(second, (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(1));
        this.modelHandler.mergeClauseItemWithFollower(first);
        Assert.assertEquals(1, proposition.getItems().size());
        Assert.assertEquals(functionFirst, proposition.getItems().get(0).getFunction());
    }

    /**
     * Test: of {@code mergeClauseItemWithFollower(ClauseItem)} for the first of two {@link ClauseItem}s, the first not having an assigned
     * {@link SyntacticalFunction}.
     *
     * @throws HmxException
     *             no following second item to merge with
     */
    @Test
    public void testMergeClauseItemWithFollower_2() throws HmxException {
        final Proposition proposition = this.pericope.getPropositionAt(0);
        final ClauseItem first = proposition.getItems().get(0);
        final ClauseItem second = proposition.getItems().get(1);
        final SyntacticalFunction functionSecond = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(1);
        this.modelHandler.setSyntacticalFunction(second, functionSecond);
        this.modelHandler.mergeClauseItemWithFollower(first);
        Assert.assertEquals(1, proposition.getItems().size());
        Assert.assertEquals(functionSecond, proposition.getItems().get(0).getFunction());
    }

    /**
     * Test: of {@code mergeClauseItemWithFollower(ClauseItem)} for the second of two {@link ClauseItem}s, the first not having an assigned
     * {@link SyntacticalFunction}.
     *
     * @throws HmxException
     *             no following third item to merge with (expected)
     */
    @Test(expected = HmxException.class)
    public void testMergeClauseItemWithFollower_3() throws HmxException {
        final Proposition proposition = this.pericope.getPropositionAt(0);
        this.modelHandler.mergeClauseItemWithFollower(proposition.getItems().get(1));
    }

    /**
     * Test: of {@code splitClauseItem(ClauseItem, String)} for the first of two {@link ClauseItem}s after the first of two contained tokens.
     */
    @Test
    public void testSplitClauseItem_1() {
        final Proposition proposition = this.pericope.getPropositionAt(0);
        this.modelHandler.splitClauseItem(proposition.getItems().get(0), "1");
        Assert.assertEquals(Arrays.asList(new ClauseItem(proposition, "1"), new ClauseItem(proposition, "2"), new ClauseItem(proposition, "3")),
                proposition.getItems());
    }

    /**
     * Test: of {@code splitClauseItem(ClauseItem, String)} with an empty {@link String} as the {@code firstPart} argument.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSplitClauseItem_2() {
        final Proposition proposition = this.pericope.getPropositionAt(0);
        this.modelHandler.splitClauseItem(proposition.getItems().get(0), "");
    }

    /**
     * Test: of {@code splitClauseItem(ClauseItem, String)} with the full {@code originText} as the {@code firstPart} argument.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSplitClauseItem_3() {
        final Proposition proposition = this.pericope.getPropositionAt(0);
        this.modelHandler.splitClauseItem(proposition.getItems().get(0), "1 2");
    }

    /**
     * Test: of {@code splitClauseItem(ClauseItem, String)} with an uncontained {@link String} as the {@code firstPart} argument.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSplitClauseItem_4() {
        final Proposition proposition = this.pericope.getPropositionAt(0);
        this.modelHandler.splitClauseItem(proposition.getItems().get(0), "X");
    }

    /**
     * Test: of {@code setLabelText(Proposition, String)}.
     */
    @Test
    public void testSetLabelText() {
        final Proposition proposition = this.pericope.getPropositionAt(0);
        Assert.assertNull(proposition.getLabel());
        final String newLabelText = "New-1";
        this.modelHandler.setLabelText(proposition, newLabelText);
        Assert.assertEquals(newLabelText, proposition.getLabel());
        final String modifiedLabelText = "Other";
        this.modelHandler.setLabelText(proposition, modifiedLabelText);
        Assert.assertEquals(modifiedLabelText, proposition.getLabel());
        this.modelHandler.setLabelText(proposition, null);
        Assert.assertNull(proposition.getLabel());
    }

    /**
     * Test: of {@code setSynTranslation(Proposition, String)}.
     */
    @Test
    public void testSetSynTranslation() {
        final Proposition proposition = this.pericope.getPropositionAt(0);
        Assert.assertNull(proposition.getSynTranslation());
        final String newTranslation = "Initial Translation, replacing the default 'null' value.";
        this.modelHandler.setSynTranslation(proposition, newTranslation);
        Assert.assertEquals(newTranslation, proposition.getSynTranslation());
        final String modifiedTranslation = "Modified translation; instead of the pr3v10usly set VaLuE!";
        this.modelHandler.setSynTranslation(proposition, modifiedTranslation);
        Assert.assertEquals(modifiedTranslation, proposition.getSynTranslation());
        this.modelHandler.setSynTranslation(proposition, null);
        Assert.assertNull(proposition.getSynTranslation());
    }

    /**
     * Test: of {@code setSemTranslation(Proposition, String)}.
     */
    @Test
    public void testSetSemTranslation() {
        final Proposition proposition = this.pericope.getPropositionAt(0);
        Assert.assertNull(proposition.getSemTranslation());
        final String newTranslation = "Initial Translation, replacing the default 'null' value.";
        this.modelHandler.setSemTranslation(proposition, newTranslation);
        Assert.assertEquals(newTranslation, proposition.getSemTranslation());
        final String modifiedTranslation = "Modified translation; instead of the pr3v10usly set VaLuE!";
        this.modelHandler.setSemTranslation(proposition, modifiedTranslation);
        Assert.assertEquals(modifiedTranslation, proposition.getSemTranslation());
        this.modelHandler.setSemTranslation(proposition, null);
        Assert.assertNull(proposition.getSemTranslation());
    }

    /**
     * Test: of {@code setComment(ICommentable, String)} for a {@link Proposition}.
     */
    @Test
    public void testSetComment_Proposition() {
        final Proposition proposition = this.pericope.getPropositionAt(0);
        Assert.assertNull(proposition.getComment());
        final String newComment = "Initial c0mm3nt:\nadded the first time...";
        this.modelHandler.setComment(proposition, newComment);
        Assert.assertEquals(newComment, proposition.getComment());
        final String modifiedComment = "Altered comment,\n\n\tbeing inserted instead.";
        this.modelHandler.setComment(proposition, modifiedComment);
        Assert.assertEquals(modifiedComment, proposition.getComment());
        this.modelHandler.setComment(proposition, null);
        Assert.assertNull(proposition.getComment());
    }

    /**
     * Test: of {@code setComment(ICommentable, String)} for a {@link ClauseItem}.
     */
    @Test
    public void testSetComment_ClauseItem() {
        final ClauseItem item = this.pericope.getPropositionAt(0).getItems().get(0);
        Assert.assertNull(item.getComment());
        final String newComment = "No.1 c0mm3nt:\n...";
        this.modelHandler.setComment(item, newComment);
        Assert.assertEquals(newComment, item.getComment());
        final String modifiedComment = "Changed comment,\n\tbeing inserted afterwards.";
        this.modelHandler.setComment(item, modifiedComment);
        Assert.assertEquals(modifiedComment, item.getComment());
        this.modelHandler.setComment(item, null);
        Assert.assertNull(item.getComment());
    }

    /**
     * Test: of {@code setSyntacticalFunction(ICanHaveSyntacticalFunction, SyntacticalFunction)} for a single prior child {@link Proposition}.
     *
     * @throws HmxException
     *             impossible to indent first under second top level {@link Proposition}
     */
    @Test
    public void testSetSyntacticalFunction_Proposition() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        this.modelHandler.indentPropositionUnderParent(first, second, (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions()
                .get(0).get(0));
        final SyntacticalFunction functionModified = (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(1);
        this.modelHandler.setSyntacticalFunction(first, functionModified);
        Assert.assertEquals(functionModified, first.getFunction());
    }

    /**
     * Test: of {@code setSyntacticalFunction(ICanHaveSyntacticalFunction, SyntacticalFunction)} for the first {@link ClauseItem} in the first top
     * level {@link Proposition}.
     */
    @Test
    public void testSetSyntacticalFunction_ClauseItem() {
        final ClauseItem target = this.pericope.getPropositionAt(0).getItems().get(0);
        final SyntacticalFunction function =
                (SyntacticalFunction) ((SyntacticalFunctionGroup) ModelHandlerImplTest.languageModel.provideFunctions().get(0).get(2))
                        .getSubFunctions().get(0);
        this.modelHandler.setSyntacticalFunction(target, function);
        Assert.assertEquals(function, target.getFunction());
    }

    /**
     * Test: of {@code setClauseItemFontStyle(ClauseItem, ClauseItem.Style)} .
     */
    @Test
    public void testSetClauseItemFontStyle() {
        final ClauseItem target = this.pericope.getPropositionAt(0).getItems().get(0);
        final ClauseItem.Style style = ClauseItem.Style.BOLD;
        this.modelHandler.setClauseItemFontStyle(target, style);
        Assert.assertSame(style, target.getFontStyle());
    }

    /**
     * Test: of {@code createRelation(List, RelationTemplate)} over four {@link Proposition}s.
     *
     * @throws HmxException
     *             the first three top level {@link Proposition}s cannot be added to a {@link Relation}
     */
    @Test
    public void testCreateRelation_1() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final Proposition fourth = this.pericope.getPropositionAt(3);
        final AssociateRole leading = new AssociateRole("Lead", true);
        final AssociateRole repetitive = new AssociateRole("Repeat", false);
        final RelationTemplate template = new RelationTemplate(leading, repetitive, repetitive, "ToolTip in Menu");
        this.modelHandler.createRelation(Arrays.asList(first, second, third, fourth), template);
        Assert.assertEquals(leading, first.getRole());
        Assert.assertEquals(repetitive, second.getRole());
        Assert.assertEquals(repetitive, third.getRole());
        Assert.assertEquals(repetitive, fourth.getRole());
    }

    /**
     * Test: of {@code createRelation(List, RelationTemplate)} over two {@link Proposition}s that are not direct neighbors.
     *
     * @throws HmxException
     *             the first and third top level {@link Proposition}s cannot be added to a {@link Relation} (expected)
     */
    @Test(expected = HmxException.class)
    public void testCreateRelation_2() throws HmxException {
        this.modelHandler.createRelation(Arrays.asList(this.pericope.getPropositionAt(0), this.pericope.getPropositionAt(2)),
                ModelHandlerImplTest.defaultRelationTemplate);
    }

    /**
     * Test: of {@code createRelation(List, RelationTemplate)} over a single {@link Proposition}.
     *
     * @throws HmxException
     *             the first top level {@link Proposition} cannot be added to a {@link Relation} on its own
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateRelation_3() throws HmxException {
        this.modelHandler.createRelation(Arrays.asList(this.pericope.getPropositionAt(0)), ModelHandlerImplTest.defaultRelationTemplate);
    }

    /**
     * Test: of {@code createRelation(List, RelationTemplate)} over a {@link Relation} and one of its own associate {@link Proposition}s.
     *
     * @throws HmxException
     *             the first and second top level {@link Proposition}s cannot be added to a {@link Relation}, or the first {@link Proposition} cannot
     *             be combined with its own super ordinated {@link Relation} into a new one (latter is expected)
     */
    @Test(expected = HmxException.class)
    public void testCreateRelation_4() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        this.modelHandler.createRelation(Arrays.asList(first, this.pericope.getPropositionAt(1)), ModelHandlerImplTest.defaultRelationTemplate);
        final Relation relation = first.getSuperOrdinatedRelation();
        Assert.assertNotNull(relation);
        this.modelHandler.createRelation(Arrays.asList(first, relation), ModelHandlerImplTest.defaultRelationTemplate);
    }

    /**
     * Test: of {@code createRelation(List, RelationTemplate)} over two {@link Proposition}s and a {@link Relation}.
     *
     * @throws HmxException
     *             the second and third top level {@link Proposition} could not be combined in a {@link Relation} or the first and fourth top level
     *             {@link Proposition} could not be combined with the previous {@link Relation} into a surrounding one
     */
    @Test
    public void testCreateRelation_5() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final Proposition fourth = this.pericope.getPropositionAt(3);
        final AssociateRole leading = new AssociateRole("Second", false);
        final AssociateRole trailing = new AssociateRole("Third", true);
        final RelationTemplate lowerTemplate = new RelationTemplate(leading, null, trailing, null);
        final AssociateRole singleRole = new AssociateRole("Single", true);
        final RelationTemplate upperTemplate = new RelationTemplate(singleRole, singleRole, singleRole, null);
        this.modelHandler.createRelation(Arrays.asList(second, third), lowerTemplate);
        final Relation lowerRelation = second.getSuperOrdinatedRelation();
        Assert.assertNotNull(lowerRelation);
        this.modelHandler.createRelation(Arrays.asList(first, lowerRelation, fourth), upperTemplate);
        final Relation upperRelation = first.getSuperOrdinatedRelation();
        Assert.assertNotNull(upperRelation);
        Assert.assertSame(upperRelation, lowerRelation.getSuperOrdinatedRelation());
        Assert.assertSame(upperRelation, fourth.getSuperOrdinatedRelation());
        Assert.assertEquals(Arrays.asList(first, lowerRelation, fourth), upperRelation.getAssociates());
        Assert.assertEquals(Arrays.asList(second, third), lowerRelation.getAssociates());
        Assert.assertEquals(singleRole, first.getRole());
        Assert.assertEquals(leading, second.getRole());
        Assert.assertEquals(trailing, third.getRole());
        Assert.assertEquals(singleRole, lowerRelation.getRole());
        Assert.assertEquals(singleRole, fourth.getRole());
    }

    /**
     * Test: of {@code rotateAssociateRoles(Relation)} for a {@link Relation} with two associates.
     *
     * @throws HmxException
     *             could not create {@link Relation} over the first two top level {@link Proposition}s
     */
    @Test
    public void testRotateAssociateRoles_1() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final AssociateRole leading = new AssociateRole("Lead", false);
        final AssociateRole trailing = new AssociateRole("Trail", true);
        final RelationTemplate template = new RelationTemplate(leading, null, trailing, null);
        this.modelHandler.createRelation(Arrays.asList(first, second), template);
        final Relation relation = first.getSuperOrdinatedRelation();
        Assert.assertNotNull(relation);
        this.modelHandler.rotateAssociateRoles(relation);
        Assert.assertEquals(trailing, first.getRole());
        Assert.assertEquals(leading, second.getRole());
        Assert.assertEquals(Arrays.asList(first, second), relation.getAssociates());
        this.modelHandler.rotateAssociateRoles(relation);
        Assert.assertEquals(leading, first.getRole());
        Assert.assertEquals(trailing, second.getRole());
    }

    /**
     * Test: of {@code rotateAssociateRoles(Relation)} for a {@link Relation} with three associates.
     *
     * @throws HmxException
     *             could not create {@link Relation} over the first three top level {@link Proposition}s
     */
    @Test
    public void testRotateAssociateRoles_2() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final AssociateRole leading = new AssociateRole("Lead", true);
        final AssociateRole repetitive = new AssociateRole("Repeat", false);
        final RelationTemplate template = new RelationTemplate(leading, repetitive, repetitive, "ToolTip in Menu");
        this.modelHandler.createRelation(Arrays.asList(first, second, third), template);
        final Relation relation = first.getSuperOrdinatedRelation();
        this.modelHandler.rotateAssociateRoles(relation);
        Assert.assertEquals(repetitive, first.getRole());
        Assert.assertEquals(leading, second.getRole());
        Assert.assertEquals(repetitive, third.getRole());
        Assert.assertEquals(Arrays.asList(first, second, third), relation.getAssociates());
        this.modelHandler.rotateAssociateRoles(relation);
        Assert.assertEquals(repetitive, first.getRole());
        Assert.assertEquals(repetitive, second.getRole());
        Assert.assertEquals(leading, third.getRole());
        this.modelHandler.rotateAssociateRoles(relation);
        Assert.assertEquals(leading, first.getRole());
        Assert.assertEquals(repetitive, second.getRole());
        Assert.assertEquals(repetitive, third.getRole());
    }

    /**
     * Test: of {@code alterRelationType(Relation, RelationTemplate)} for a a {@link Relation} with two associates.
     *
     * @throws HmxException
     *             could not create {@link Relation} over the first two top level {@link Proposition}s
     */
    @Test
    public void testAlterRelationType_1() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final AssociateRole leading = new AssociateRole("Lead", false);
        final AssociateRole trailing = new AssociateRole("Trail", true);
        this.modelHandler.createRelation(Arrays.asList(first, second), new RelationTemplate(leading, null, trailing, null));
        final Relation relation = first.getSuperOrdinatedRelation();
        Assert.assertNotNull(relation);
        final AssociateRole singleRole = new AssociateRole("Single", true);
        final RelationTemplate template = new RelationTemplate(singleRole, singleRole, singleRole, null);
        this.modelHandler.alterRelationType(relation, template);
        Assert.assertEquals(singleRole, first.getRole());
        Assert.assertEquals(singleRole, second.getRole());
    }

    /**
     * Test: of {@code alterRelationType(Relation, RelationTemplate)} for a a {@link Relation} with two associates.
     *
     * @throws HmxException
     *             could not create {@link Relation} over the first two top level {@link Proposition}s
     */
    @Test
    public void testAlterRelationType_2() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final AssociateRole leading = new AssociateRole("Lead", false);
        final AssociateRole trailing = new AssociateRole("Trail", true);
        this.modelHandler.createRelation(Arrays.asList(first, second), new RelationTemplate(leading, null, trailing, null));
        final Relation relation = first.getSuperOrdinatedRelation();
        Assert.assertNotNull(relation);
        final AssociateRole singleRole = new AssociateRole("Single", true);
        this.modelHandler.alterRelationType(relation, new RelationTemplate(singleRole, null, singleRole, null));
        Assert.assertEquals(singleRole, first.getRole());
        Assert.assertEquals(singleRole, second.getRole());
    }

    /**
     * Test: of {@code alterRelationType(Relation, RelationTemplate)} for a a {@link Relation} with three associates.
     *
     * @throws HmxException
     *             could not create {@link Relation} over the first three top level {@link Proposition}s
     */
    @Test
    public void testAlterRelationType_3() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final AssociateRole singleRole = new AssociateRole("Single", true);
        this.modelHandler.createRelation(Arrays.asList(first, second), new RelationTemplate(singleRole, singleRole, singleRole, null));
        final Relation relation = first.getSuperOrdinatedRelation();
        Assert.assertNotNull(relation);
        final AssociateRole leading = new AssociateRole("Lead", false);
        final AssociateRole repetitive = new AssociateRole("Trail", true);
        final RelationTemplate template = new RelationTemplate(leading, repetitive, repetitive, null);
        this.modelHandler.alterRelationType(relation, template);
        Assert.assertEquals(leading, first.getRole());
        Assert.assertEquals(repetitive, second.getRole());
    }

    /**
     * Test: of {@code removeRelation(Relation)} for a single {@link Relation}.
     *
     * @throws HmxException
     *             could not create the {@link Relation}
     */
    @Test
    public void testRemoveRelation_1() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final AssociateRole role = new AssociateRole("Role", true);
        final RelationTemplate template = new RelationTemplate(role, null, role, null);
        this.modelHandler.createRelation(Arrays.asList(first, second), template);
        final Relation relation = second.getSuperOrdinatedRelation();
        Assert.assertNotNull(relation);
        this.modelHandler.removeRelation(relation);
        Assert.assertNull(first.getSuperOrdinatedRelation());
        Assert.assertNull(second.getSuperOrdinatedRelation());
        Assert.assertNull(first.getRole());
        Assert.assertNull(second.getRole());
    }

    /**
     * Test: of {@code removeRelation(Relation)} for the highest {@link Relation} in a tree with depth {@code 2}.
     *
     * @throws HmxException
     *             could not create the {@link Relation}s
     */
    @Test
    public void testRemoveRelation_2() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final AssociateRole role = new AssociateRole("Role", true);
        final RelationTemplate template = new RelationTemplate(role, null, role, null);
        this.modelHandler.createRelation(Arrays.asList(first, second), template);
        final Relation lowerRelation = second.getSuperOrdinatedRelation();
        Assert.assertNotNull(lowerRelation);
        this.modelHandler.createRelation(Arrays.asList(lowerRelation, third), template);
        final Relation upperRelation = lowerRelation.getSuperOrdinatedRelation();
        Assert.assertNotNull(upperRelation);
        this.modelHandler.removeRelation(upperRelation);
        Assert.assertSame(lowerRelation, first.getSuperOrdinatedRelation());
        Assert.assertSame(lowerRelation, second.getSuperOrdinatedRelation());
        Assert.assertNull(lowerRelation.getSuperOrdinatedRelation());
        Assert.assertNull(third.getSuperOrdinatedRelation());
        Assert.assertEquals(role, first.getRole());
        Assert.assertEquals(role, second.getRole());
        Assert.assertNull(lowerRelation.getRole());
        Assert.assertNull(third.getRole());
    }

    /**
     * Test: of {@code removeRelation(Relation)} for the lowest {@link Relation} in a tree with depth {@code 2}.
     *
     * @throws HmxException
     *             could not create the {@link Relation}s
     */
    @Test
    public void testRemoveRelation_3() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        final AssociateRole role = new AssociateRole("Role", true);
        final RelationTemplate template = new RelationTemplate(role, null, role, null);
        this.modelHandler.createRelation(Arrays.asList(first, second), template);
        final Relation lowerRelation = second.getSuperOrdinatedRelation();
        Assert.assertNotNull(lowerRelation);
        this.modelHandler.createRelation(Arrays.asList(lowerRelation, third), template);
        this.modelHandler.removeRelation(lowerRelation);
        Assert.assertNull(first.getSuperOrdinatedRelation());
        Assert.assertNull(second.getSuperOrdinatedRelation());
        Assert.assertNull(third.getSuperOrdinatedRelation());
        Assert.assertNull(first.getRole());
        Assert.assertNull(second.getRole());
        Assert.assertNull(third.getRole());
    }

    /**
     * Test: of {@code addNewPropositions(String, boolean)} for a single {@link ClauseItem} in a single leading {@link Proposition}.
     */
    @Test
    public void testAddNewPropositions_1() {
        this.modelHandler.addNewPropositions("0\n \t \n", true);
        final Proposition firstAdded = this.pericope.getPropositionAt(0);
        Assert.assertEquals(6, this.pericope.getText().size());
        Assert.assertEquals(Arrays.asList(new ClauseItem(firstAdded, "0")), firstAdded.getItems());
    }

    /**
     * Test: of {@code addNewPropositions(String, boolean)} for two trailing {@link Proposition}s.
     */
    @Test
    public void testAddNewPropositions_2() {
        this.modelHandler.addNewPropositions("11 \n  12 \t \t 13   14", false);
        final List<Proposition> propositions = this.pericope.getFlatText();
        Assert.assertEquals(7, propositions.size());
        final Proposition firstAdded = propositions.get(5);
        Assert.assertEquals(Arrays.asList(new ClauseItem(firstAdded, "11")), firstAdded.getItems());
        final Proposition secondAdded = propositions.get(6);
        Assert.assertEquals(Arrays.asList(new ClauseItem(secondAdded, "12"), new ClauseItem(secondAdded, "13 14")), secondAdded.getItems());
    }

    /**
     * Test: of {@code mergeWithOtherPericope(Pericope, boolean)}.
     *
     * @throws HmxException
     *             differing language model in the merged {@link Pericope}
     */
    @Test
    public void testMergeWithOtherPericope_1() throws HmxException {
        final Pericope otherPericope = new Pericope();
        otherPericope.init("0", ModelHandlerImplTest.languageModel, this.pericope.getFont());
        this.modelHandler.mergeWithOtherPericope(otherPericope, true);
        final Proposition firstAdded = this.pericope.getPropositionAt(0);
        Assert.assertEquals(6, this.pericope.getText().size());
        Assert.assertEquals(Arrays.asList(new ClauseItem(firstAdded, "0")), firstAdded.getItems());
    }

    /**
     * Test: of {@code mergeWithOtherPericope(Pericope, boolean)}.
     *
     * @throws HmxException
     *             differing language model in the merged {@link Pericope}
     */
    @Test
    public void testMergeWithOtherPericope_2() throws HmxException {
        final Pericope otherPericope = new Pericope();
        otherPericope.init("11 \n  12 \t \t 13   14", ModelHandlerImplTest.languageModel, this.pericope.getFont());
        this.modelHandler.mergeWithOtherPericope(otherPericope, false);
        final List<Proposition> propositions = this.pericope.getFlatText();
        Assert.assertEquals(7, propositions.size());
        final Proposition firstAdded = propositions.get(5);
        Assert.assertEquals(Arrays.asList(new ClauseItem(firstAdded, "11")), firstAdded.getItems());
        final Proposition secondAdded = propositions.get(6);
        Assert.assertEquals(Arrays.asList(new ClauseItem(secondAdded, "12"), new ClauseItem(secondAdded, "13 14")), secondAdded.getItems());
    }

    /**
     * Test: of {@code mergeWithOtherPericope(Pericope, boolean)} with a differing {@link LanguageModel}.
     *
     * @throws HmxException
     *             differing language model in the merged {@link Pericope} (expected)
     */
    @Test(expected = HmxException.class)
    public void testMergeWithOtherPericope_3() throws HmxException {
        final Pericope otherPericope = new Pericope();
        // only the text orientation is different
        final LanguageModel differingModel = new LanguageModel("otherName", false);
        differingModel.addAll(ModelHandlerImplTest.languageModel.provideFunctions());
        otherPericope.init("11", differingModel, this.pericope.getFont());
        this.modelHandler.mergeWithOtherPericope(otherPericope, false);
    }

    /**
     * Test: of {@code removePropositions(List)} for the first two top level {@link Proposition}s.
     *
     * @throws HmxException
     *             invalid selection of {@link Proposition}s to be removed
     */
    @Test
    public void testRemovePropositions_1() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        final Proposition third = this.pericope.getPropositionAt(2);
        this.modelHandler.createRelation(Arrays.asList(second, third), ModelHandlerImplTest.defaultRelationTemplate);
        this.modelHandler.removePropositions(Arrays.asList(first, second));
        Assert.assertEquals(3, this.pericope.getText().size());
        Assert.assertSame(third, this.pericope.getPropositionAt(0));
        Assert.assertNull(third.getSuperOrdinatedRelation());
    }

    /**
     * Test: of {@code removePropositions(List)} for an empty list of {@link Proposition}.
     *
     * @throws HmxException
     *             invalid selection of {@link Proposition}s to be removed (expected)
     */
    @Test(expected = HmxException.class)
    public void testRemovePropositions_2() throws HmxException {
        this.modelHandler.removePropositions(Collections.<Proposition>emptyList());
    }

    /**
     * Test: of {@code removePropositions(List)} for the all {@link Proposition}s.
     *
     * @throws HmxException
     *             invalid selection of {@link Proposition}s to be removed (expected)
     */
    @Test(expected = HmxException.class)
    public void testRemovePropositions_3() throws HmxException {
        this.modelHandler.removePropositions(this.pericope.getText());
    }

    /**
     * Test: of {@code removePropositions(List)} for an indented {@link Proposition}.
     *
     * @throws HmxException
     *             invalid selection of {@link Proposition}s to be removed (expected)
     */
    @Test(expected = HmxException.class)
    public void testRemovePropositions_4() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        this.modelHandler.indentPropositionUnderParent(first, second, (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions()
                .get(0).get(0));
        this.modelHandler.removePropositions(Collections.singletonList(first));
    }

    /**
     * Test: of {@code removePropositions(List)} for a {@link Proposition} having a prior child.
     *
     * @throws HmxException
     *             invalid selection of {@link Proposition}s to be removed (expected)
     */
    @Test(expected = HmxException.class)
    public void testRemovePropositions_5() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        this.modelHandler.indentPropositionUnderParent(first, second, (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions()
                .get(0).get(0));
        this.modelHandler.removePropositions(Collections.singletonList(second));
    }

    /**
     * Test: of {@code removePropositions(List)} for a {@link Proposition} having a later child.
     *
     * @throws HmxException
     *             invalid selection of {@link Proposition}s to be removed (expected)
     */
    @Test(expected = HmxException.class)
    public void testRemovePropositions_6() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition second = this.pericope.getPropositionAt(1);
        this.modelHandler.indentPropositionUnderParent(second, first, (SyntacticalFunction) ModelHandlerImplTest.languageModel.provideFunctions()
                .get(0).get(0));
        this.modelHandler.removePropositions(Collections.singletonList(first));
    }

    /**
     * Test: of {@code removePropositions(List)} for a {@link Proposition} part before arrow.
     *
     * @throws HmxException
     *             invalid selection of {@link Proposition}s to be removed (expected)
     */
    @Test(expected = HmxException.class)
    public void testRemovePropositions_7() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition third = this.pericope.getPropositionAt(2);
        this.modelHandler.mergePropositions(first, third);
        this.modelHandler.removePropositions(Collections.singletonList(first));
    }

    /**
     * Test: of {@code removePropositions(List)} for a {@link Proposition} part after arrow.
     *
     * @throws HmxException
     *             invalid selection of {@link Proposition}s to be removed (expected)
     */
    @Test(expected = HmxException.class)
    public void testRemovePropositions_8() throws HmxException {
        final Proposition first = this.pericope.getPropositionAt(0);
        final Proposition third = this.pericope.getPropositionAt(2);
        this.modelHandler.mergePropositions(first, third);
        this.modelHandler.removePropositions(Collections.singletonList(third));
    }
}
