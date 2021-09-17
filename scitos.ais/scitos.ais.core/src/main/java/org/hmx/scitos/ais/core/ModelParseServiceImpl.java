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

package org.hmx.scitos.ais.core;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.swing.KeyStroke;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hmx.scitos.ais.core.i18n.AisMessage;
import org.hmx.scitos.ais.domain.IDetailCategoryProvider;
import org.hmx.scitos.ais.domain.model.AisProject;
import org.hmx.scitos.ais.domain.model.DetailCategory;
import org.hmx.scitos.ais.domain.model.Interview;
import org.hmx.scitos.ais.domain.model.MutableDetailCategoryModel;
import org.hmx.scitos.ais.domain.model.TextToken;
import org.hmx.scitos.core.ExportOption;
import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.core.IModelParseService;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.core.util.DomUtil;
import org.hmx.scitos.domain.IModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Implementation of the {@link IModelParseService} for the AIS module.
 */
public class ModelParseServiceImpl implements IModelParseService<AisProject> {

    /** The embedded/export stylesheet for the conversion to a html page. */
    private static final ExportOption HTML_EXPORT = new ExportOption(AisMessage.PROJECT_EXPORT_HTML, ExportOption.TargetFileType.HTML,
            "/org/hmx/scitos/ais/stylesheet.xsl");
    private static final ExportOption ODS_RESULT_EXPORT = new ExportOption(AisMessage.PROJECT_EXPORT_ODS, ExportOption.TargetFileType.ODS, null);
    /*
     * Collection of the XML tags and attributes for representing an AIS module's project in its persisted form.
     */
    private static final String NAMESPACE = "http://www.hermeneutix.org/schema/ais/1.0";
    private static final String SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String SCHEMA_REF_ATTRIBUTE = "xsi:schemaLocation";
    private static final String SCHEMA_LOCATION =
            "https://raw.githubusercontent.com/scientific-tool-set/scitos/master/scitos.ais/schema/ais-v1.0.xsd";
    private static final String TAG_ROOT = "AisProject";
    private static final String TAG_CATEGORY_ROOT = "Categories";
    private static final String TAG_CATEGORY = "Category";
    private static final String ATTR_CATEGORY_CODE = "code";
    private static final String ATTR_CATEGORY_NAME = "name";
    private static final String ATTR_CATEGORY_COLOR = "color";
    private static final String ATTR_CATEGORY_COLOR_VALUE = "rgb(%d, %d, %d)";
    private static final String ATTR_CATEGORY_SHORTCUT = "shortcut";
    private static final String TAG_INTERVIEW_ROOT = "Interviews";
    private static final String TAG_INTERVIEW = "Interview";
    private static final String ATTR_INTERVIEW_PARTICIPANT = "participant";
    private static final String ATTR_INTERVIEW_INDEX = "index";
    private static final String TAG_INTERVIEW_PARAGRAPH = "Paragraph";
    private static final String TAG_INTERVIEW_DETAIL = "Detail";
    private static final String ATTR_INTERVIEW_DETAIL_CODE = "code";
    private static final String TAG_INTERVIEW_TOKEN = "Token";
    private static final String TAG_VIEWS = "Views";
    private static final String TAG_VIEWS_PROJECT = ModelParseServiceImpl.TAG_ROOT;
    private static final String TAG_VIEWS_GROUP = "Group";
    private static final String ATTR_VIEWS_GROUP_NAME = ModelParseServiceImpl.ATTR_INTERVIEW_PARTICIPANT;
    private static final String TAG_VIEWS_INTERVIEW = ModelParseServiceImpl.TAG_INTERVIEW;
    private static final String ATTR_VIEWS_INTERVIEW_PARTICIPANT = ModelParseServiceImpl.ATTR_INTERVIEW_PARTICIPANT;
    private static final String ATTR_VIEWS_INTERVIEW_INDEX = ModelParseServiceImpl.ATTR_INTERVIEW_INDEX;

    /**
     * Main constructor for the state-less service implementation.
     */
    @Inject
    public ModelParseServiceImpl() {
        // constructor for dependency injection
    }

    @Override
    public List<ExportOption> getSupportedExports() {
        return Arrays.asList(ModelParseServiceImpl.HTML_EXPORT, ModelParseServiceImpl.ODS_RESULT_EXPORT);
    }

    @Override
    public Document parseXmlFromModel(final IModel<?> model, final List<?> openViewElements) throws HmxException {
        final AisProject project = (AisProject) model;
        try {
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            doc.appendChild(doc.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_ROOT));
            // add schema reference
            doc.getDocumentElement().setAttributeNS(ModelParseServiceImpl.SCHEMA_NAMESPACE, ModelParseServiceImpl.SCHEMA_REF_ATTRIBUTE,
                    ModelParseServiceImpl.NAMESPACE + ' ' + ModelParseServiceImpl.SCHEMA_LOCATION);
            // include categories used
            doc.getDocumentElement().appendChild(this.parseXmlFromDetailCategories(doc, project));
            // include scored interviews
            final Element interviewRoot = doc.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_INTERVIEW_ROOT);
            final List<Interview> interviews = new ArrayList<>(project.getInterviews());
            // add interviews in sorted order - just for a user who opens the file in a text editor
            Collections.sort(interviews);
            for (final Interview singleInterview : interviews) {
                interviewRoot.appendChild(this.parseXmlFromInterview(doc, singleInterview));
            }
            doc.getDocumentElement().appendChild(interviewRoot);
            // include open view elements for displaying them when loading the generated document again
            if (openViewElements != null && !openViewElements.isEmpty()) {
                doc.getDocumentElement().appendChild(this.parseXmlFromOpenViewElements(doc, openViewElements));
            }
            // embed stylesheet
            this.embedXsltStylesheet(doc);
            return doc;
        } catch (final ParserConfigurationException pcex) {
            throw new HmxException(Message.ERROR_UNKNOWN, pcex);
        }
    }

    /**
     * Embed the XSLT stylesheet for in-browser rendering in the generated project file. If this fails for any reason, just continue without the
     * stylesheet.
     *
     * @param doc
     *            xml document to embed the XSLT stylesheet in
     */
    private void embedXsltStylesheet(final Document doc) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        InputStream stylesheet = null;
        try {
            stylesheet = ModelParseServiceImpl.class.getResourceAsStream(ModelParseServiceImpl.HTML_EXPORT.getStylesheetPath());
            // parse stylesheet into xml structure
            final Element stylesheetRoot = factory.newDocumentBuilder().parse(stylesheet).getDocumentElement();
            stylesheetRoot.setAttribute("id", "embedded_stylesheet");
            doc.getDocumentElement().appendChild(doc.adoptNode(stylesheetRoot));
            doc.setXmlStandalone(true);
            doc.insertBefore(doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xml\" href=\"#embedded_stylesheet\""),
                    doc.getDocumentElement());
        } catch (final ParserConfigurationException pce) {
            // error while creating a DocumentBuilder instance from factory or while accessing file
            pce.printStackTrace();
        } catch (final IOException ioe) {
            // error while creating a DocumentBuilder instance from factory or while accessing file
            ioe.printStackTrace();
        } catch (final SAXException se) {
            // error while interpreting (invalid) xml structure
            se.printStackTrace();
        } finally {
            try {
                if (stylesheet != null) {
                    stylesheet.close();
                }
            } catch (final IOException ioe) {
                // at least we tried to close the stream afterwards
                ioe.printStackTrace();
            }
        }
    }

    @Override
    public Entry<AisProject, List<?>> parseModelFromXml(final Document doc, final File originPath) throws HmxException {
        // retrieve used detail categories from document
        final MutableDetailCategoryModel categories = this.parseDetailCategoriesFromXml(doc);
        final AisProject project = new AisProject(originPath.getName(), categories.provide());
        // retrieve interviews from document
        final Element interviewRoot = DomUtil.getChildElement(doc.getDocumentElement(), ModelParseServiceImpl.TAG_INTERVIEW_ROOT);
        if (interviewRoot != null) {
            final List<Interview> containedInterviews = new ArrayList<>();
            for (final Element singleInterview : DomUtil.getChildElements(interviewRoot, ModelParseServiceImpl.TAG_INTERVIEW)) {
                containedInterviews.add(this.parseInterviewFromXml(singleInterview, categories));
            }
            project.setInterviews(containedInterviews);
        }
        return new SimpleEntry<>(project, this.parseOpenViewElementsFromXml(doc, project));
    }

    /**
     * Create a single xml node that holds the {@link DetailCategory} tree applied in the given project. In order to reproduce it on opening.
     *
     * @param doc
     *            xml document, API to use for creating xml nodes
     * @param categoryProvider
     *            provider to retrieve the detail categories from
     * @return created {@value #TAG_CATEGORY_ROOT} node
     */
    Element parseXmlFromDetailCategories(final Document doc, final IDetailCategoryProvider categoryProvider) {
        // extract hierarchical representation of category's sub elements
        final MutableDetailCategoryModel categoryFamily = new MutableDetailCategoryModel();
        categoryFamily.addAll(categoryProvider.provide());
        // create single element to hold all categories
        final Element categoryRootElement = doc.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_CATEGORY_ROOT);
        // add only the root categories, which in turn add their own children recursively
        for (final DetailCategory singleCategoryRoot : categoryFamily.getRootCategories()) {
            categoryRootElement.appendChild(this.parseXmlFromDetailCategoryTree(doc, singleCategoryRoot, categoryFamily));
        }
        return categoryRootElement;
    }

    /**
     * Create a single xml node representing the given {@link DetailCategory}. If it is has sub categories, they are added as sub nodes in the xml
     * structure as well.
     *
     * @param doc
     *            xml document, API to use for creating xml nodes
     * @param target
     *            category to represent as a xml node
     * @param categoryFamily
     *            collection of all categories in the currently generated structure, to retrieve the target's sub categories from
     *
     * @return created {@value #TAG_CATEGORY} node
     */
    private Element parseXmlFromDetailCategoryTree(final Document doc, final DetailCategory target, final MutableDetailCategoryModel categoryFamily) {
        // create category element and set its attributes
        final Element categoryElement = doc.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_CATEGORY);
        categoryElement.setAttribute(ModelParseServiceImpl.ATTR_CATEGORY_CODE, target.getCode());
        categoryElement.setAttribute(ModelParseServiceImpl.ATTR_CATEGORY_NAME, target.getName());
        final Color color = target.getColor();
        categoryElement.setAttribute(ModelParseServiceImpl.ATTR_CATEGORY_COLOR, String.format(ModelParseServiceImpl.ATTR_CATEGORY_COLOR_VALUE,
                Integer.valueOf(color.getRed()), Integer.valueOf(color.getGreen()), Integer.valueOf(color.getBlue())));
        final KeyStroke shortCut = target.getShortCut();
        if (shortCut != null) {
            categoryElement.setAttribute(ModelParseServiceImpl.ATTR_CATEGORY_SHORTCUT, shortCut.getKeyCode() + ":" + shortCut.getModifiers());
        }
        // add sub categories recursively
        for (final DetailCategory subCategory : categoryFamily.getChildCategories(target)) {
            categoryElement.appendChild(this.parseXmlFromDetailCategoryTree(doc, subCategory, categoryFamily));
        }
        return categoryElement;
    }

    /**
     * Parse the detail category model from the given document's xml structure.
     *
     * @param doc
     *            document to parse
     * @return successfully parsed detail category model
     * @throws HmxException
     *             given document does not contain a valid detail category model
     */
    MutableDetailCategoryModel parseDetailCategoriesFromXml(final Document doc) throws HmxException {
        final Element categoryWrapper = DomUtil.getChildElement(doc.getDocumentElement(), ModelParseServiceImpl.TAG_CATEGORY_ROOT);
        if (categoryWrapper != null) {
            final List<Element> topLevelCategories = DomUtil.getChildElements(categoryWrapper, ModelParseServiceImpl.TAG_CATEGORY);
            if (!topLevelCategories.isEmpty()) {
                final MutableDetailCategoryModel categories = new MutableDetailCategoryModel();
                categories.addAll(this.parseDetailCategoriesFromXmlRecursively(topLevelCategories, null));
                return categories;
            }
        }
        throw new HmxException(Message.ERROR_FILE_INVALID, new IllegalArgumentException(ModelParseServiceImpl.TAG_CATEGORY_ROOT
                + " contains no children (expected multiple " + ModelParseServiceImpl.TAG_CATEGORY + " entries)"));
    }

    /**
     * Parse the detail categories from the given list of xml nodes.
     *
     * @param categories
     *            list of xml nodes representing detail categories to be parsed
     * @param parentCategory
     *            (already parsed) detail category to assign as parent to the detail categories represented by the list of xml nodes
     * @return successfully parsed detail categories
     * @throws HmxException
     *             at least one of the parsed xml nodes did contain an invalid detail category definition
     */
    private List<DetailCategory> parseDetailCategoriesFromXmlRecursively(final List<Element> categories, final DetailCategory parentCategory)
            throws HmxException {
        final List<DetailCategory> result = new ArrayList<>();
        for (final Element singleCategoryElement : categories) {
            // parse mandatory category attributes
            final String code = singleCategoryElement.getAttribute(ModelParseServiceImpl.ATTR_CATEGORY_CODE);
            final String name = singleCategoryElement.getAttribute(ModelParseServiceImpl.ATTR_CATEGORY_NAME);
            if (code.isEmpty()) {
                throw new HmxException(Message.ERROR_FILE_INVALID, new IllegalArgumentException("invalid " + ModelParseServiceImpl.TAG_CATEGORY
                        + " definition"));
            }
            // parse optional color attribute
            final Color color;
            final String[] colorValues =
                    singleCategoryElement.getAttribute(ModelParseServiceImpl.ATTR_CATEGORY_COLOR).replaceAll("[^0-9]+", " ").trim().split(" ");
            if (colorValues.length == 3) {
                final int red = Integer.valueOf(colorValues[0]).intValue();
                final int green = Integer.valueOf(colorValues[1]).intValue();
                final int blue = Integer.valueOf(colorValues[2]).intValue();
                color = new Color(red, green, blue);
            } else {
                color = Color.BLACK;
            }
            // retrieve short cut from string value (returns NULL if no valid string was found)
            final String[] shortCutParts =
                    singleCategoryElement.getAttribute(ModelParseServiceImpl.ATTR_CATEGORY_SHORTCUT).replaceAll("[^0-9]+", " ").trim().split(" ");
            final KeyStroke shortCut;
            if (shortCutParts.length == 2) {
                shortCut =
                        KeyStroke.getKeyStroke(Integer.valueOf(shortCutParts[0]).intValue(), Integer.valueOf(shortCutParts[1]).intValue(), true);
            } else {
                shortCut = null;
            }
            // check for potential child categories
            final List<Element> subCategories = DomUtil.getChildElements(singleCategoryElement, ModelParseServiceImpl.TAG_CATEGORY);
            // only categories without any children are deemed selectable
            final DetailCategory category = new DetailCategory(parentCategory, code, name, subCategories.isEmpty(), color, shortCut);
            result.add(category);
            if (!subCategories.isEmpty()) {
                result.addAll(this.parseDetailCategoriesFromXmlRecursively(subCategories, category));
            }
        }
        return result;
    }

    /**
     * Create a single xml node representing the given {@link Interview} including the scored text.
     *
     * @param doc
     *            xml document, API to use for creating xml nodes
     * @param target
     *            interview to represent as a xml node
     * @return created {@value #TAG_INTERVIEW} node
     */
    private Element parseXmlFromInterview(final Document doc, final Interview target) {
        // create interview element and set its attributes
        final Element interviewElement = doc.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_INTERVIEW);
        interviewElement.setAttribute(ModelParseServiceImpl.ATTR_INTERVIEW_PARTICIPANT, target.getParticipantId());
        interviewElement.setAttribute(ModelParseServiceImpl.ATTR_INTERVIEW_INDEX, String.valueOf(target.getIndex()));
        // include the scored text
        for (final TextToken singleParagraph : target.getText()) {
            interviewElement.appendChild(this.parseXmlFromInterviewParagraph(doc, singleParagraph));
        }
        return interviewElement;
    }

    /**
     * Create a single xml node representing the {@link Interview} paragraph starting with the given token.
     *
     * @param doc
     *            xml document, API to use for creating xml nodes
     * @param paragraphStart
     *            first token of the paragraph to represent as a xml node
     * @return created {@value #TAG_INTERVIEW_PARAGRAPH} node
     */
    private Element parseXmlFromInterviewParagraph(final Document doc, final TextToken paragraphStart) {
        // create paragraph wrapping element
        final Element paragraphElement = doc.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_INTERVIEW_PARAGRAPH);
        // iterate through tokens
        TextToken currentToken = paragraphStart;
        do {
            if (currentToken.getDetail() == null) {
                // omitting detail wrapper of top level tokens without assigned category
                paragraphElement.appendChild(this.parseXmlFromTokenText(doc, currentToken));
                currentToken = currentToken.getFollowingToken();
            } else {
                // wrap all tokens with the same assigned category in one detail element (to make it easier readable in a text editor)
                final Entry<Element, TextToken> detailRange = this.parseXmlFromTokenRange(doc, currentToken);
                paragraphElement.appendChild(detailRange.getKey());
                currentToken = detailRange.getValue();
            }
        } while (currentToken != null);
        return paragraphElement;
    }

    /**
     * Create a single xml node representing a range of {@link TextToken} with the same assigned {@link DetailCategory}, starting with the given one.
     *
     * @param doc
     *            xml document, API to use for creating xml nodes
     * @param rangeStart
     *            first token of the range with the same assigned category to represent as a xml node
     * @return created {@value #TAG_INTERVIEW_DETAIL} node and the next token after the represented range (token reference is {@code null} if
     *         there is no following token to the last in the represented range)
     */
    private Entry<Element, TextToken> parseXmlFromTokenRange(final Document doc, final TextToken rangeStart) {
        // create range wrapping element and set assigned category by its code
        final Element detailElement = doc.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_INTERVIEW_DETAIL);
        final DetailCategory category = rangeStart.getDetail();
        // ranges inside ranges can have no category assigned
        if (category != null) {
            detailElement.setAttribute(ModelParseServiceImpl.ATTR_INTERVIEW_DETAIL_CODE, category.getCode());
        }
        TextToken currentToken = rangeStart;
        // no need for any iterations if this range consists only of this single token
        if (!rangeStart.isLastTokenOfDetail()) {
            // iterate through tokens of this range (include enclosed ranges by recursively calling this method)
            do {
                if (currentToken.getDetail() == category && !currentToken.isFirstTokenOfDetail() || currentToken == rangeStart) {
                    // token belongs to this range, include it directly
                    detailElement.appendChild(this.parseXmlFromTokenText(doc, currentToken));
                    // continue iteration
                    currentToken = currentToken.getFollowingToken();
                } else {
                    // token is (the start of) an enclosed range, wrap it recursively in its own detail element
                    final Entry<Element, TextToken> enclosedRange = this.parseXmlFromTokenRange(doc, currentToken);
                    detailElement.appendChild(enclosedRange.getKey());
                    // the second part of the return value is already the next token
                    currentToken = enclosedRange.getValue();
                }
                // do not stop at an (other) enclosed token/range, only at the last taken of this range
            } while (currentToken.isFirstTokenOfDetail() || !currentToken.isLastTokenOfDetail());
        }
        // include last token of this range
        detailElement.appendChild(this.parseXmlFromTokenText(doc, currentToken));
        // return create wrapper element (including its tokens) and the token where to continue the parsing
        return new SimpleEntry<>(detailElement, currentToken.getFollowingToken());
    }

    /**
     * Create a single xml node representing the given {@link TextToken}.
     *
     * @param doc
     *            xml document, API to use for creating xml nodes
     * @param target
     *            token to represent as a xml node
     * @return create {@value #TAG_INTERVIEW_TOKEN} node
     */
    private Element parseXmlFromTokenText(final Document doc, final TextToken target) {
        // after converting all the category info into wrapping xml structure, the node for the token contains only its text
        final Element tokenElement = doc.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_INTERVIEW_TOKEN);
        tokenElement.appendChild(doc.createTextNode(target.getText()));
        return tokenElement;
    }

    /**
     * Parse the interview represented by the given xml element.
     *
     * @param interviewElement
     *            xml element representing the interview to parse
     * @param categories
     *            detail category model to apply to parsed tokens (identifying assigned detail categories by their unique codes)
     * @return successfully parsed interview
     * @throws HmxException
     *             given xml element does not represent a valid interview definition
     */
    private Interview parseInterviewFromXml(final Element interviewElement, final MutableDetailCategoryModel categories) throws HmxException {
        final String participantId = interviewElement.getAttribute(ModelParseServiceImpl.ATTR_INTERVIEW_PARTICIPANT);
        final int indexValue = DomUtil.getIntAttribute(interviewElement, ModelParseServiceImpl.ATTR_INTERVIEW_INDEX, -1);
        if (participantId.isEmpty() || indexValue == -1) {
            throw new HmxException(Message.ERROR_FILE_INVALID, new IllegalArgumentException("invalid " + ModelParseServiceImpl.TAG_INTERVIEW
                    + " definition"));
        }
        final List<TextToken> text = new ArrayList<>();
        for (final Element singleParagraph : DomUtil.getChildElements(interviewElement, ModelParseServiceImpl.TAG_INTERVIEW_PARAGRAPH)) {
            text.add(this.parseTextParagraphFromXml(singleParagraph, categories));
        }
        return new Interview(participantId, indexValue).setText(text);
    }

    /**
     * Parse the text paragraph contained in the given xml element.
     *
     * @param paragraphElement
     *            wrapping xml element containing the text paragraph to parse
     * @param categories
     *            detail category model to apply (identifying assigned detail categories by their unique codes)
     * @return leading token of the successfully parsed text paragraph
     * @throws HmxException
     *             encountered unexpected/invalid xml tag while parsing given xml element
     */
    private TextToken parseTextParagraphFromXml(final Element paragraphElement, final MutableDetailCategoryModel categories) throws HmxException {
        final String detailCode = DomUtil.getNullableAttribute(paragraphElement, ModelParseServiceImpl.ATTR_INTERVIEW_DETAIL_CODE);
        final DetailCategory category = categories.getDetailByCode(detailCode);
        TextToken previousToken = null;
        for (final Element singleChild : DomUtil.getChildElements(paragraphElement, ModelParseServiceImpl.TAG_INTERVIEW_TOKEN,
                ModelParseServiceImpl.TAG_INTERVIEW_DETAIL)) {
            final TextToken enclosedToken;
            if (ModelParseServiceImpl.TAG_INTERVIEW_TOKEN.equals(singleChild.getTagName())) {
                enclosedToken = new TextToken(singleChild.getTextContent()).setDetail(category);
                if (category == null && previousToken != null && previousToken.getDetail() != null) {
                    enclosedToken.setFirstTokenOfDetail(true);
                }
            } else {
                enclosedToken = this.parseTextParagraphFromXml(singleChild, categories);
                if (category == null && previousToken != null && previousToken.getDetail() == null) {
                    previousToken.setLastTokenOfDetail(true);
                }
            }
            if (previousToken != null) {
                previousToken.setFollowingToken(enclosedToken);
            }
            previousToken = enclosedToken.setPreviousToken(previousToken);
            // if this was an enclosed range, we need the last token to continue our parsing
            while (previousToken.getFollowingToken() != null) {
                previousToken = previousToken.getFollowingToken();
            }
        }
        if (previousToken == null) {
            throw new HmxException(Message.ERROR_FILE_INVALID, new IllegalArgumentException(paragraphElement.getTagName()
                    + " does not contain any children (expected " + ModelParseServiceImpl.TAG_INTERVIEW_DETAIL + " and/or "
                    + ModelParseServiceImpl.TAG_INTERVIEW_TOKEN + ')'));
        }
        previousToken.setLastTokenOfDetail(true);
        // get the first token from the parsed range
        while (previousToken.getPreviousToken() != null) {
            previousToken = previousToken.getPreviousToken();
        }
        previousToken.setFirstTokenOfDetail(true);
        return previousToken;
    }

    /**
     * Create a single xml node holding identifier for the given list's elements, to be initially displayed when the containing document is loaded
     * again.
     *
     * @param doc
     *            xml document, API to use for creating xml nodes
     * @param openViewElements
     *            list of view elements (tabs) to be remember for restoring them when loading this file again
     * @return created {@value #TAG_VIEWS} node
     */
    private Element parseXmlFromOpenViewElements(final Document doc, final List<?> openViewElements) {
        final Element viewElementWrapper = doc.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_VIEWS);
        for (final Object viewElement : openViewElements) {
            final Element view;
            if (viewElement instanceof Interview) {
                view = doc.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_VIEWS_INTERVIEW);
                view.setAttribute(ModelParseServiceImpl.ATTR_VIEWS_INTERVIEW_PARTICIPANT, ((Interview) viewElement).getParticipantId());
                view.setAttribute(ModelParseServiceImpl.ATTR_VIEWS_INTERVIEW_INDEX, String.valueOf(((Interview) viewElement).getIndex()));
            } else if (viewElement instanceof String) {
                view = doc.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_VIEWS_GROUP);
                view.setAttribute(ModelParseServiceImpl.ATTR_VIEWS_GROUP_NAME, (String) viewElement);
            } else {
                view = doc.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_VIEWS_PROJECT);
            }
            viewElementWrapper.appendChild(view);
        }
        return viewElementWrapper;
    }

    /**
     * Parse the list of open view elements from the given document's xml structure.
     *
     * @param doc
     *            document to parse
     * @param parsedProject
     *            (already parsed) project from the given document, to retrieve the actual model elements from
     * @return successfully parsed list of model elements that were open in the view, when the document was created
     */
    private List<Object> parseOpenViewElementsFromXml(final Document doc, final AisProject parsedProject) {
        final List<Object> openViewElements = new ArrayList<>();
        final Element viewsRoot = DomUtil.getChildElement(doc.getDocumentElement(), ModelParseServiceImpl.TAG_VIEWS);
        if (viewsRoot != null) {
            for (final Element singleView : DomUtil.getChildElements(viewsRoot)) {
                if (ModelParseServiceImpl.TAG_VIEWS_PROJECT.equals(singleView.getTagName())) {
                    openViewElements.add(parsedProject);
                } else if (ModelParseServiceImpl.TAG_VIEWS_GROUP.equals(singleView.getTagName())
                        && !singleView.getAttribute(ModelParseServiceImpl.ATTR_VIEWS_GROUP_NAME).isEmpty()) {
                    openViewElements.add(singleView.getAttribute(ModelParseServiceImpl.ATTR_VIEWS_GROUP_NAME));
                } else if (ModelParseServiceImpl.TAG_VIEWS_INTERVIEW.equals(singleView.getTagName())) {
                    final String participantId = singleView.getAttribute(ModelParseServiceImpl.ATTR_VIEWS_INTERVIEW_PARTICIPANT);
                    final int indexValue = DomUtil.getIntAttribute(singleView, ModelParseServiceImpl.ATTR_VIEWS_INTERVIEW_INDEX, -1);
                    final Map<String, List<Interview>> interviews = parsedProject.getSubModelObjects();
                    if (interviews.containsKey(participantId) && indexValue != -1) {
                        for (final Interview singleInterview : interviews.get(participantId)) {
                            if (singleInterview.getIndex() == indexValue) {
                                openViewElements.add(singleInterview);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return openViewElements;
    }
}