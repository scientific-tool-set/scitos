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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.core.option.Option;
import org.hmx.scitos.hmx.domain.ISyntacticalFunctionProvider;
import org.hmx.scitos.hmx.domain.model.AbstractSyntacticalFunctionElement;
import org.hmx.scitos.hmx.domain.model.ClauseItem;
import org.hmx.scitos.hmx.domain.model.LanguageModel;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.domain.model.Relation;
import org.hmx.scitos.hmx.domain.model.RelationModel;
import org.hmx.scitos.hmx.domain.model.RelationTemplate;
import org.hmx.scitos.hmx.domain.model.RelationTemplate.AssociateRole;
import org.hmx.scitos.hmx.domain.model.SyntacticalFunction;
import org.hmx.scitos.hmx.domain.model.SyntacticalFunctionGroup;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Test for the {@link ModelParseServiceImpl} class.
 */
public class ModelParseServiceImplTest {

    /** The model parse service implementation being tested. */
    private static ModelParseServiceImpl parseService;

    /** Initialize the service implementation being tested. */
    @BeforeClass
    public static void initServiceImplementation() {
        ModelParseServiceImplTest.parseService = new ModelParseServiceImpl();
        // enforce the English Locale to make this test independent of the executing system's Locale
        Option.TRANSLATION.setValue(Locale.ENGLISH.toString());
    }

    /** Discard reference to tested service implementation. */
    @AfterClass
    public static void tearDown() {
        ModelParseServiceImplTest.parseService = null;
        Option.TRANSLATION.setValue(null);
    }

    /**
     * Test: of the {@code parseXmlFromModel()} and {@code parseModelFromXml()} methods.
     *
     * @throws HmxException
     *             failed to retrieve the system {@link LanguageModel}s, or parsing to/from xml structure failed
     */
    @Test
    public void testParseModelToAndFromXml_1() throws HmxException {
        final LanguageModel language = ModelParseServiceImplTest.parseService.getSystemLanguageModels().get(0);
        final Pericope model = new Pericope();
        model.init("1A\t1B\t1C\t1D\n2\n3\n4\n5\n6\n7\n8\n9\n10", language, new Font("Times New Roman", Font.PLAIN, 23));
        final HmxModelHandler modelHandler = new ModelHandlerImpl(model);
        final List<Proposition> propositions = model.getFlatText();
        modelHandler.setClauseItemFontStyle(propositions.get(0).getItems().get(0), ClauseItem.Style.PLAIN);
        modelHandler.setClauseItemFontStyle(propositions.get(0).getItems().get(1), ClauseItem.Style.BOLD);
        modelHandler.setClauseItemFontStyle(propositions.get(0).getItems().get(2), ClauseItem.Style.ITALIC);
        modelHandler.setClauseItemFontStyle(propositions.get(0).getItems().get(3), ClauseItem.Style.BOLD_ITALIC);
        final List<List<AbstractSyntacticalFunctionElement>> functions = model.provideFunctions();
        modelHandler.indentPropositionUnderParent(propositions.get(1), propositions.get(2), (SyntacticalFunction) functions.get(0).get(1));
        modelHandler.indentPropositionUnderParent(propositions.get(0), propositions.get(2), (SyntacticalFunction) functions.get(0).get(2));
        modelHandler.mergePropositions(propositions.get(5), propositions.get(2));
        modelHandler.indentPropositionUnderParent(propositions.get(4), propositions.get(3), (SyntacticalFunction) functions.get(4).get(0));
        modelHandler.mergePropositions(propositions.get(2), propositions.get(7));
        modelHandler.setSyntacticalFunction(propositions.get(6), (SyntacticalFunction) functions.get(4).get(1));
        modelHandler.indentPropositionUnderParent(propositions.get(8), propositions.get(9), (SyntacticalFunction) functions.get(4).get(2));
        modelHandler.indentPropositionUnderParent(propositions.get(9), propositions.get(2), (SyntacticalFunction) functions.get(4).get(3));
        final Document xml = ModelParseServiceImplTest.parseService.parseXmlFromModel(model, Arrays.asList(model));
        final Entry<Pericope, List<?>> parsed = ModelParseServiceImplTest.parseService.parseModelFromXml(xml, new File("test.hmx"));
        this.assertPericopeEquals(model, parsed.getKey());
        Assert.assertEquals(1, parsed.getValue().size());
        Assert.assertSame(parsed.getKey(), parsed.getValue().get(0));
    }

    /**
     * Test: of the {@code parseXmlFromModel()} and {@code parseModelFromXml()} methods.
     *
     * @throws HmxException
     *             failed to retrieve the system {@link LanguageModel}s, could not create {@link Relation}s for setup, or parsing to/from xml
     *             structure failed
     */
    @Test
    public void testParseModelToAndFromXml_2() throws HmxException {
        final LanguageModel language = ModelParseServiceImplTest.parseService.getSystemLanguageModels().get(1);
        final Pericope model = new Pericope();
        model.init("1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11\n12", language, new Font("Arial", Font.PLAIN, 17));
        final HmxModelHandler modelHandler = new ModelHandlerImpl(model);
        final List<Proposition> propositions = model.getFlatText();
        final List<List<AbstractSyntacticalFunctionElement>> functions = model.provideFunctions();
        modelHandler.setSyntacticalFunction(propositions.get(0).getItems().get(0), (SyntacticalFunction) functions.get(0).get(4));
        modelHandler.setSyntacticalFunction(propositions.get(1).getItems().get(0), (SyntacticalFunction) functions.get(1).get(0));
        modelHandler.setSyntacticalFunction(propositions.get(2).getItems().get(0), (SyntacticalFunction) functions.get(3).get(0));
        modelHandler.setSyntacticalFunction(propositions.get(4).getItems().get(0), (SyntacticalFunction) functions.get(4).get(2));
        modelHandler.setSyntacticalFunction(propositions.get(5).getItems().get(0), (SyntacticalFunction) ((SyntacticalFunctionGroup) functions
                .get(5).get(0)).getSubFunctions().get(3));
        modelHandler.setSyntacticalFunction(propositions.get(6).getItems().get(0),
                (SyntacticalFunction) ((SyntacticalFunctionGroup) ((SyntacticalFunctionGroup) functions.get(5).get(0)).getSubFunctions().get(7))
                        .getSubFunctions().get(2));
        modelHandler.setSyntacticalFunction(propositions.get(8).getItems().get(0), (SyntacticalFunction) ((SyntacticalFunctionGroup) functions
                .get(5).get(1)).getSubFunctions().get(4));
        modelHandler.setSyntacticalFunction(propositions.get(9).getItems().get(0),
                (SyntacticalFunction) ((SyntacticalFunctionGroup) ((SyntacticalFunctionGroup) functions.get(5).get(1)).getSubFunctions().get(7))
                        .getSubFunctions().get(5));
        modelHandler.createRelation(Arrays.asList(propositions.get(0), propositions.get(1)), new RelationTemplate(new AssociateRole("A", true),
                null, new AssociateRole("B", false), null));
        modelHandler.createRelation(Arrays.asList(propositions.get(5), propositions.get(6)), new RelationTemplate(new AssociateRole("C", true),
                null, new AssociateRole("C", true), null));
        modelHandler.createRelation(Arrays.asList(propositions.get(7), propositions.get(8), propositions.get(9)), new RelationTemplate(
                new AssociateRole("D", false), new AssociateRole("D", false), new AssociateRole("E", true), null));
        final List<Relation> firstLevelRelations = model.getFlatRelations();
        modelHandler.createRelation(Arrays.asList(firstLevelRelations.get(0), propositions.get(2), propositions.get(3)), new RelationTemplate(
                new AssociateRole("F", true), new AssociateRole("F", true), new AssociateRole("F", true), null));
        modelHandler.createRelation(Arrays.asList(firstLevelRelations.get(2), propositions.get(10), propositions.get(11)), new RelationTemplate(
                new AssociateRole("G", true), new AssociateRole("H", false), new AssociateRole("H", false), null));
        final Document xml = ModelParseServiceImplTest.parseService.parseXmlFromModel(model, Arrays.asList(model));
        final Entry<Pericope, List<?>> parsed =
                ModelParseServiceImplTest.parseService.parseModelFromXml(xml, new File("some/unexistent/path.hmx"));
        this.assertPericopeEquals(model, parsed.getKey());
        Assert.assertEquals(1, parsed.getValue().size());
        Assert.assertSame(parsed.getKey(), parsed.getValue().get(0));
    }

    /**
     * Check the equality of the given {@link Pericope}s in detail, in order to produce a helpful error message to narrow down the source of a
     * potential error.
     *
     * @param expected
     *            expected model
     * @param actual
     *            parsed model
     */
    private void assertPericopeEquals(final Pericope expected, final Pericope actual) {
        Assert.assertEquals("Origin Text Font Family differs", expected.getFont().getFamily(Locale.ENGLISH),
                actual.getFont().getFamily(Locale.ENGLISH));
        Assert.assertEquals("Origin Text Font Size differs", expected.getFont().getSize(), actual.getFont().getSize());
        Assert.assertEquals("Text Orientation differs", expected.isLeftToRightOriented(), actual.isLeftToRightOriented());
        Assert.assertEquals("Language Name differs", expected.getLanguage(), actual.getLanguage());
        this.assertLanguageModelEquals(expected, actual);
        this.assertStringEqualsNullOrEmptyAware("Author differs", expected.getAuthor(), actual.getAuthor());
        this.assertStringEqualsNullOrEmptyAware("Title differs", expected.getTitle(), actual.getTitle());
        this.assertStringEqualsNullOrEmptyAware("Comment differs", expected.getComment(), actual.getComment());
        // iterate over the contained Propositions and their super ordinated Relations, thereby producing helpful output in case of an error
        this.assertPropositionListEquals(expected.getText(), actual.getText(), new HashSet<Relation>());
        // this covers the whole model but does not provide a helpful error message
        Assert.assertEquals(expected, actual);
    }

    private void assertPropositionListEquals(final List<Proposition> expected, final List<Proposition> actual,
            final Set<Relation> checkedExpectedRelations) {
        final int propositionCount = expected.size();
        Assert.assertEquals("Number of Propositions are not equal. (" + expected + " != " + actual + ')', propositionCount, actual.size());
        for (int index = 0; index < propositionCount; index++) {
            this.assertPropositionEquals(expected.get(index), actual.get(index), checkedExpectedRelations);
        }
    }

    private void assertPropositionEquals(final Proposition expected, final Proposition actual, final Set<Relation> checkedExpectedRelations) {
        this.assertPropositionListEquals(expected.getPriorChildren(), actual.getPriorChildren(), checkedExpectedRelations);
        this.assertClauseItemListEquals(expected.getItems(), actual.getItems());
        this.assertStringEqualsNullOrEmptyAware("Label differs", expected.getLabel(), actual.getLabel());
        this.assertStringEqualsNullOrEmptyAware("Syntactical Translation differs", expected.getSynTranslation(), actual.getSynTranslation());
        this.assertStringEqualsNullOrEmptyAware("Semantical Translation differs", expected.getSemTranslation(), actual.getSemTranslation());
        this.assertStringEqualsNullOrEmptyAware("Proposition Comment differs", expected.getComment(), actual.getComment());
        Assert.assertEquals("Proposition Function differs", expected.getFunction(), actual.getFunction());
        Assert.assertEquals("Proposition Role differs", expected.getRole(), actual.getRole());
        this.assertRelationEquals(expected.getSuperOrdinatedRelation(), actual.getSuperOrdinatedRelation(), checkedExpectedRelations);
        this.assertPropositionListEquals(expected.getLaterChildren(), actual.getLaterChildren(), checkedExpectedRelations);
        if (expected.getPartAfterArrow() == null) {
            Assert.assertNull("PartAfterArrow should not be present", actual.getPartAfterArrow());
        } else {
            this.assertPropositionEquals(expected.getPartAfterArrow(), actual.getPartAfterArrow(), checkedExpectedRelations);
        }
    }

    private void assertClauseItemListEquals(final List<ClauseItem> expected, final List<ClauseItem> actual) {
        final int itemCount = expected.size();
        Assert.assertEquals("Number of ClauseItems are not equal (" + expected + " != " + actual + ')', itemCount, actual.size());
        for (int index = 0; index < itemCount; index++) {
            final ClauseItem expectedItem = expected.get(index);
            final ClauseItem actualItem = actual.get(index);
            Assert.assertEquals("Origin Text differs", expectedItem.getOriginText(), actualItem.getOriginText());
            Assert.assertEquals("Item Function differs", expectedItem.getFunction(), actualItem.getFunction());
            Assert.assertSame("Item Font Style differs", expectedItem.getFontStyle(), actualItem.getFontStyle());
            this.assertStringEqualsNullOrEmptyAware("ClauseItem Comment differs", expectedItem.getComment(), actualItem.getComment());
        }
    }

    private void assertRelationEquals(final Relation expected, final Relation actual, final Set<Relation> checkedExpectedRelations) {
        if (expected == null) {
            Assert.assertNull("Relation should not be present", actual);
        } else if (!checkedExpectedRelations.contains(expected)) {
            Assert.assertNotNull("Relation should be present", actual);
            Assert.assertEquals("Relation Role differs", expected.getRole(), actual.getRole());
            this.assertStringEqualsNullOrEmptyAware("Relation Comment differs", expected.getComment(), actual.getComment());
            checkedExpectedRelations.add(expected);
            this.assertRelationEquals(expected.getSuperOrdinatedRelation(), actual.getSuperOrdinatedRelation(), checkedExpectedRelations);
        }
    }

    private void assertStringEqualsNullOrEmptyAware(final String errorMessage, final String expected, final String actual) {
        final String expectedText;
        if (expected == null) {
            expectedText = "";
        } else {
            expectedText = expected;
        }
        final String actualText;
        if (actual == null) {
            actualText = "";
        } else {
            actualText = actual;
        }
        Assert.assertEquals(errorMessage, expectedText, actualText);
    }

    /**
     * Test: of the system {@link LanguageModel} retrieval.
     *
     * @throws HmxException
     *             internal error while looking up or parsing the system defined model
     */
    @Test
    public void testGetSystemLanguageModels() throws HmxException {
        // Build the default English language model for Greek
        final LookupLanguageModel model = new LookupLanguageModel("Greek", true);
        model.add(Arrays.asList(new SyntacticalFunction("Subj", "Subject", false, null), new SyntacticalFunction("S/P", "Subject-Predicate",
                false, null), new SyntacticalFunction("Voc", "Vocative", false, null)));
        model.add(Arrays.asList(new SyntacticalFunction("Pred", "Predicate", false, null)));
        model.add(Arrays.asList(new SyntacticalFunction("C", "Connector", true, null)));
        model.add(Arrays.asList(new SyntacticalFunction("Attr", "Attribute", false, null)));
        model.add(Arrays.asList(new SyntacticalFunction("GenO", "Genitive Object", false, null), new SyntacticalFunction("DatO", "Dative Object",
                false, null), new SyntacticalFunction("AccO", "Accusative Object", false, null), new SyntacticalFunction("PrepO",
                "Prepositional Object", false, null)));
        final List<AbstractSyntacticalFunctionElement> groups = new ArrayList<AbstractSyntacticalFunctionElement>(2);
        for (final String[] groupedType : new String[][] { new String[] { "Complement", "necessary" }, new String[] { "Adjunct", "erasable" } }) {
            final String type = groupedType[0];
            final char firstChar = type.charAt(0);
            final List<AbstractSyntacticalFunctionElement> subTypes = new ArrayList<AbstractSyntacticalFunctionElement>(8);
            subTypes.add(new SyntacticalFunction("SId" + firstChar, "Subject-Indentification-" + type, false, null));
            subTypes.add(new SyntacticalFunction("SMan" + firstChar, "Subject-Manner-" + type, false, null));
            subTypes.add(new SyntacticalFunction("OId" + firstChar, "Object-Indentification-" + type, false, null));
            subTypes.add(new SyntacticalFunction("OMan" + firstChar, "Object-Manner-" + type, false, null));
            subTypes.add(new SyntacticalFunction("Loc" + firstChar, "Locale-" + type, false, null));
            subTypes.add(new SyntacticalFunction("Time" + firstChar, "Time-" + type, false, null));
            subTypes.add(new SyntacticalFunction("Mod" + firstChar, "Modal-" + type, false, null));
            final List<SyntacticalFunction> subSubTypes = new ArrayList<SyntacticalFunction>(7);
            subSubTypes.add(new SyntacticalFunction("Caus" + firstChar, "causal", false, null));
            subSubTypes.add(new SyntacticalFunction("Cond" + firstChar, "conditional", false, null));
            subSubTypes.add(new SyntacticalFunction("Cons" + firstChar, "consecutiv", false, null));
            subSubTypes.add(new SyntacticalFunction("Fin" + firstChar, "final", false, null));
            subSubTypes.add(new SyntacticalFunction("Conc" + firstChar, "concessiv", false, null));
            subSubTypes.add(new SyntacticalFunction("Inst" + firstChar, "instrumental", false, null));
            subSubTypes.add(new SyntacticalFunction("Int" + firstChar, "of interest", false, null));
            subTypes.add(new SyntacticalFunctionGroup("Causal " + type, null, subSubTypes));
            groups.add(new SyntacticalFunctionGroup(type, groupedType[1] + ", means\nnot requested by verb", subTypes));
        }
        model.add(groups);
        // parse this model from the internal xml file
        final List<LanguageModel> systemModels = ModelParseServiceImplTest.parseService.getSystemLanguageModels();
        // the test xml file contains only this one model
        Assert.assertEquals(2, systemModels.size());
        // confirm equality of the expected and retrieved model (step by step to produce helpful test output in case of an error)
        this.assertLanguageModelEquals(model, systemModels.get(0));
        // this covers the whole model (including name and text orientation), but does not produce a helpful message to narrow down a potential error
        Assert.assertEquals(model, systemModels.get(0));
    }

    /**
     * Check the equality of the provided {@link SyntacticalFunction}s.
     *
     * @param expectedLanguageModel
     *            provider of {@link SyntacticalFunction}s, as they are expected
     * @param actualLanguageModel
     *            provider of {@link SyntacticalFunction}s, as they have been parsed/loaded
     */
    private void assertLanguageModelEquals(final ISyntacticalFunctionProvider expectedLanguageModel,
            final ISyntacticalFunctionProvider actualLanguageModel) {
        final List<List<AbstractSyntacticalFunctionElement>> expectedGroups = expectedLanguageModel.provideFunctions();
        final List<List<AbstractSyntacticalFunctionElement>> actualGroups = actualLanguageModel.provideFunctions();
        final int expectedGroupCount = expectedGroups.size();
        Assert.assertEquals("Number of defined groups differ", expectedGroupCount, actualGroups.size());
        for (int groupIndex = 0; groupIndex < expectedGroupCount; groupIndex++) {
            this.assertFunctionGroupEquals(expectedGroups.get(groupIndex), actualGroups.get(groupIndex));
        }
    }

    /**
     * Recursively check the equality of the given groups of {@link SyntacticalFunction}s.
     *
     * @param expected
     *            list of {@link SyntacticalFunction}s, as they are expected
     * @param actual
     *            list of {@link SyntacticalFunction}s, as they have been parsed/loaded
     */
    private void assertFunctionGroupEquals(final List<AbstractSyntacticalFunctionElement> expected,
            final List<AbstractSyntacticalFunctionElement> actual) {
        final int expectedCount = expected.size();
        Assert.assertEquals("Number of functions differ. (" + expected + " != " + actual + ')', expectedCount, actual.size());
        for (int functionIndex = 0; functionIndex < expectedCount; functionIndex++) {
            final AbstractSyntacticalFunctionElement expectedElement = expected.get(functionIndex);
            final AbstractSyntacticalFunctionElement actualElement = actual.get(functionIndex);
            if (expectedElement instanceof SyntacticalFunctionGroup && actualElement instanceof SyntacticalFunctionGroup) {
                this.assertFunctionGroupEquals(((SyntacticalFunctionGroup) expectedElement).getSubFunctions(),
                        ((SyntacticalFunctionGroup) actualElement).getSubFunctions());
            }
            Assert.assertEquals(expectedElement, actualElement);
        }
        Assert.assertEquals("Lists of functions are not equal.", expected, actual);
    }

    /**
     * Test: of the system {@link RelationModel} retrieval.
     *
     * @throws HmxException
     *             internal error while looking of the system relation model
     */
    @Test
    public void testGetSystemRelationModel() throws HmxException {
        final RelationModel model = new RelationModel();
        final AssociateRole sequentialNucleus = new AssociateRole("Sequential-Nucleus", true);
        final AssociateRole simultaneousNucleus = new AssociateRole("Simultaneous-Nucleus", true);
        final AssociateRole nucleus = new AssociateRole("Nucleus", true);
        model.add(Arrays.asList(new RelationTemplate(sequentialNucleus, sequentialNucleus, sequentialNucleus, null), new RelationTemplate(
                simultaneousNucleus, simultaneousNucleus, simultaneousNucleus, null), new RelationTemplate(nucleus, nucleus, nucleus, null)));
        final List<RelationTemplate> secondGroup = new LinkedList<RelationTemplate>();
        secondGroup.add(new RelationTemplate(new AssociateRole("Orienter", false), null, new AssociateRole("Content", true), null));
        secondGroup.add(new RelationTemplate(new AssociateRole("Circumstance", false), null, nucleus, null));
        secondGroup.add(new RelationTemplate(new AssociateRole("Move", false), null, new AssociateRole("Goal", true), null));
        secondGroup.add(new RelationTemplate(new AssociateRole("Occasion", false), null, new AssociateRole("Outcome", true), null));
        secondGroup.add(new RelationTemplate(new AssociateRole("Time", false), null, nucleus, null));
        secondGroup.add(new RelationTemplate(nucleus, null, new AssociateRole("Equivalent", false), null));
        secondGroup.add(new RelationTemplate(new AssociateRole("Generic", true), new AssociateRole("Specificum", false), new AssociateRole(
                "Specificum", false), null));
        secondGroup.add(new RelationTemplate(new AssociateRole("Generic", false), null, new AssociateRole("Specificum", true), null));
        secondGroup.add(new RelationTemplate(new AssociateRole("Contraction", true), null, new AssociateRole("Amplification", false), null));
        secondGroup.add(new RelationTemplate(new AssociateRole("Positivum", true), null, new AssociateRole("Negativum", false), null));
        secondGroup.add(new RelationTemplate(nucleus, null, new AssociateRole("Comparison", false), null));
        secondGroup.add(new RelationTemplate(nucleus, null, new AssociateRole("Illustration", false), null));
        secondGroup.add(new RelationTemplate(new AssociateRole("Congruence", true), null, new AssociateRole("Standard", false), null));
        secondGroup.add(new RelationTemplate(nucleus, null, new AssociateRole("Manner", false), null));
        secondGroup.add(new RelationTemplate(nucleus, null, new AssociateRole("Contrast", false), null));
        secondGroup.add(new RelationTemplate(new AssociateRole("Reason", false), null, new AssociateRole("Consequence", true), null));
        secondGroup.add(new RelationTemplate(new AssociateRole("Means", false), null, new AssociateRole("Result", true), null));
        secondGroup.add(new RelationTemplate(new AssociateRole("Purpose", false), null, new AssociateRole("Means", true), null));
        secondGroup.add(new RelationTemplate(new AssociateRole("Condition", false), null, new AssociateRole("Consequence", true), null));
        secondGroup.add(new RelationTemplate(new AssociateRole("Concession", false), null, new AssociateRole("Contraexpectation", true), null));
        secondGroup.add(new RelationTemplate(new AssociateRole("Grounds", false), null, new AssociateRole("Conclusion", true), null));
        secondGroup.add(new RelationTemplate(new AssociateRole("Grounds", false), null, new AssociateRole("Exhortation", true), null));
        secondGroup.add(new RelationTemplate(nucleus, null, new AssociateRole("Comment", false), null));
        secondGroup.add(new RelationTemplate(nucleus, null, new AssociateRole("Parenthesis", false), null));
        model.add(secondGroup);
        model.add(Arrays.asList(new RelationTemplate(new AssociateRole("Object", true), null, new AssociateRole("Identification", false), null),
                new RelationTemplate(new AssociateRole("Object", true), null, new AssociateRole("Description", false), null)));
        // parse this model from the internal xml file
        final RelationModel systemModel = ModelParseServiceImplTest.parseService.getSystemRelationModel();
        Assert.assertEquals(model, systemModel);
    }

    /**
     * Test: of the {@code parseXmlFromRelationModel()} and {@code parseRelationModelFromXml()} methods.
     *
     * @throws HmxException
     *             failed to retrieve the system {@link RelationModel}, or parsing to/from xml structure failed
     * @throws ParserConfigurationException
     *             failed to initialize empty {@link Document} for parsing
     */
    @Test
    public void testParseRelationModelToAndFromXml() throws HmxException, ParserConfigurationException {
        final RelationModel model = ModelParseServiceImplTest.parseService.getSystemRelationModel();
        final Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        xml.appendChild(xml.createElement("root"));
        Assert.assertNull(ModelParseServiceImplTest.parseService.parseRelationModelFromXml(xml));
        xml.getDocumentElement().appendChild(ModelParseServiceImplTest.parseService.parseXmlFromRelationModel(xml, model));
        final RelationModel parsed = ModelParseServiceImplTest.parseService.parseRelationModelFromXml(xml);
        Assert.assertEquals(model, parsed);
    }
}
