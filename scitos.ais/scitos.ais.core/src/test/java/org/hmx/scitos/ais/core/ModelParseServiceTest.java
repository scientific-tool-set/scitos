package org.hmx.scitos.ais.core;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.KeyStroke;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hmx.scitos.ais.domain.model.AisProject;
import org.hmx.scitos.ais.domain.model.DetailCategory;
import org.hmx.scitos.ais.domain.model.Interview;
import org.hmx.scitos.ais.domain.model.MutableDetailCategoryModel;
import org.hmx.scitos.ais.domain.model.TextToken;
import org.hmx.scitos.core.HmxException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Test for the {@link ModelParseServiceImpl} class.
 */
public class ModelParseServiceTest {

    /** The default detail category model to apply. */
    private static MutableDetailCategoryModel DEFAULT_CATEGORY_MODEL;
    /** The service implementation to test. */
    private ModelParseServiceImpl service;

    /**
     * Initial setup: get the default category model from {@link AisOption#createDefaultCategoryModel()}.
     */
    @BeforeClass
    public static void setUp() {
        ModelParseServiceTest.DEFAULT_CATEGORY_MODEL = AisOption.createDefaultCategoryModel();
    }

    /** Preparation before each test: get a new service implementation instance, create a project and the associated model handler. */
    @Before
    public void prepareForModelParsing() {
        this.service = new ModelParseServiceImpl();
    }

    /**
     * Test: for parseXmlFromModel and parseModelFromXml methods – with multiple interviews.
     *
     * @throws HmxException
     *             error when parsing to/from xml
     */
    @Test
    public void testParseModelToAndFromXml_1() throws HmxException {
        final AisProject model = new AisProject("test.aisp", ModelParseServiceTest.DEFAULT_CATEGORY_MODEL.provide());
        final AisModelHandler modelHandler = new ModelHandlerImpl(model);
        final Interview firstInterview = modelHandler.createInterview("a");
        modelHandler.setInterviewText(firstInterview, "1 2 3");
        modelHandler.assignDetailCategory(firstInterview, firstInterview.getText(), ModelParseServiceTest.DEFAULT_CATEGORY_MODEL
                .provideSelectables().get(0));
        modelHandler.setInterviewText(modelHandler.createInterview("a"), "1 2 3\n4 5 6 7 8 9\n10");
        modelHandler.createInterview("b");
        // all interview views open, and a sub model group
        final List<Object> openViewElements = new LinkedList<>(model.getInterviews());
        openViewElements.add("a");
        final Document xml = this.service.parseXmlFromModel(model, openViewElements);
        final Entry<AisProject, List<?>> parsed = this.service.parseModelFromXml(xml, new File("test.aisp"));
        Assert.assertEquals(model, parsed.getKey());
        Assert.assertEquals(openViewElements, parsed.getValue());
    }

    /**
     * Test: for parseXmlFromModel and parseModelFromXml methods – with assigned detail categories.<br/>
     * Paragraph 1: aba-<br/>
     * Paragraph 2: abcbadda
     *
     * @throws HmxException
     *             error when parsing to/from xml
     */
    @Test
    public void testParseModelToAndFromXml_2() throws HmxException {
        final AisProject model = new AisProject("test.aisp", ModelParseServiceTest.DEFAULT_CATEGORY_MODEL.provide());
        final AisModelHandler modelHandler = new ModelHandlerImpl(model);
        final Interview interview = modelHandler.createInterview("123");
        modelHandler.setInterviewText(interview, "1a 2b 3a 4-\n1a 2b 3c 4b 5a 6d 7d 8a");
        // first paragraph, simple enclosed case with differing categories
        final TextToken leadParagraph = interview.getText().get(0);
        final List<DetailCategory> categories = ModelParseServiceTest.DEFAULT_CATEGORY_MODEL.provideSelectables();
        modelHandler.assignDetailCategory(interview, Arrays.asList(leadParagraph.getFollowingToken()), categories.get(0));
        modelHandler.assignDetailCategory(interview, Arrays.asList(leadParagraph, leadParagraph.getFollowingToken().getFollowingToken()),
                categories.get(1));
        // second paragraph, all with the same detail category, but nested multiple times
        final TextToken firstToken = interview.getText().get(1);
        final TextToken thirdToken = firstToken.getFollowingToken().getFollowingToken();
        final TextToken fifthToken = thirdToken.getFollowingToken().getFollowingToken();
        final TextToken eightToken = fifthToken.getFollowingToken().getFollowingToken().getFollowingToken();
        final DetailCategory category = categories.get(3);
        modelHandler.assignDetailCategory(interview, Arrays.asList(firstToken, fifthToken, eightToken), category);
        modelHandler.assignDetailCategory(interview, Arrays.asList(firstToken.getFollowingToken(), thirdToken.getFollowingToken()), category);
        modelHandler.assignDetailCategory(interview, Arrays.asList(thirdToken), category);
        modelHandler.assignDetailCategory(interview, Arrays.asList(fifthToken.getFollowingToken(), eightToken.getPreviousToken()), category);
        // no open views
        final List<?> openViewElements = Collections.emptyList();
        final Document xml = this.service.parseXmlFromModel(model, openViewElements);
        final Entry<AisProject, List<?>> parsed = this.service.parseModelFromXml(xml, new File("test.aisp"));
        Assert.assertEquals(model, parsed.getKey());
        Assert.assertEquals(openViewElements, parsed.getValue());
    }

    /**
     * Test: for parseXmlFromModel and parseModelFromXml methods – empty project.
     *
     * @throws HmxException
     *             error when parsing to/from xml
     */
    @Test
    public void testParseModelToAndFromXml_3() throws HmxException {
        final AisProject model = new AisProject("test.aisp", ModelParseServiceTest.DEFAULT_CATEGORY_MODEL.provide());
        // all interview views open, and a sub model group
        final Document xml = this.service.parseXmlFromModel(model, Arrays.asList(model));
        final Entry<AisProject, List<?>> parsed = this.service.parseModelFromXml(xml, new File("test.aisp"));
        Assert.assertEquals(model, parsed.getKey());
        Assert.assertSame(parsed.getKey(), parsed.getValue().get(0));
    }

    /**
     * Test: for parseDetailCategoriesToXml and parseDetailCategoriesFromXml methods (default detail category model).
     *
     * @throws ParserConfigurationException
     *             error creating a new xml {@link Document}
     * @throws HmxException
     *             error reading detail categories from xml
     */
    @Test
    public void testParseDetailCategoriesFromXml_1() throws ParserConfigurationException, HmxException {
        final Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        xml.appendChild(xml.createElement("Test")).appendChild(
                this.service.parseXmlFromDetailCategories(xml, ModelParseServiceTest.DEFAULT_CATEGORY_MODEL));
        Assert.assertEquals(ModelParseServiceTest.DEFAULT_CATEGORY_MODEL, this.service.parseDetailCategoriesFromXml(xml));
    }

    /**
     * Test: for parseDetailCategoriesToXml and parseDetailCategoriesFromXml methods (multi-level detail category model).
     *
     * @throws ParserConfigurationException
     *             error creating a new xml {@link Document}
     * @throws HmxException
     *             error reading detail categories from xml
     */
    @Test
    public void testParseDetailCategoriesFromXml_2() throws ParserConfigurationException, HmxException {
        final DetailCategory parent = new DetailCategory(null, "ab", "", false, Color.GREEN, null);
        final DetailCategory childA = new DetailCategory(parent, "a", "", true, Color.BLUE, KeyStroke.getKeyStroke(KeyEvent.VK_3, 0, true));
        final DetailCategory subParentB = new DetailCategory(parent, "b", "", false, Color.YELLOW, null);
        final DetailCategory childB1 =
                new DetailCategory(subParentB, "b1", "", true, Color.ORANGE, KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true));
        final DetailCategory childB2 = new DetailCategory(subParentB, "b2", "", true, Color.RED, null);
        final DetailCategory standalone =
                new DetailCategory(null, "c", "", true, Color.BLACK, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.SHIFT_DOWN_MASK, true));
        final MutableDetailCategoryModel categoryModel = new MutableDetailCategoryModel();
        categoryModel.addAll(Arrays.asList(parent, childA, subParentB, childB1, childB2, standalone));
        final Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        xml.appendChild(xml.createElement("Test")).appendChild(this.service.parseXmlFromDetailCategories(xml, categoryModel));
        Assert.assertEquals(categoryModel, this.service.parseDetailCategoriesFromXml(xml));
    }
}
