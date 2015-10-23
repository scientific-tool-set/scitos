package org.hmx.scitos.hmx.core;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hmx.scitos.core.ExportOption;
import org.hmx.scitos.core.ExportOption.TargetFileType;
import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.core.IModelParseService;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.core.i18n.Translator;
import org.hmx.scitos.core.option.Option;
import org.hmx.scitos.core.util.DomUtil;
import org.hmx.scitos.domain.IModel;
import org.hmx.scitos.hmx.core.LookupLanguageModel.BackwardCompatibleFunction;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.domain.ISemanticalRelationProvider;
import org.hmx.scitos.hmx.domain.ISyntacticalFunctionProvider;
import org.hmx.scitos.hmx.domain.model.AbstractConnectable;
import org.hmx.scitos.hmx.domain.model.ClauseItem;
import org.hmx.scitos.hmx.domain.model.ClauseItem.Style;
import org.hmx.scitos.hmx.domain.model.LanguageModel;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.domain.model.Relation;
import org.hmx.scitos.hmx.domain.model.RelationModel;
import org.hmx.scitos.hmx.domain.model.RelationTemplate;
import org.hmx.scitos.hmx.domain.model.RelationTemplate.AssociateRole;
import org.hmx.scitos.hmx.domain.model.SyntacticalFunction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 */
@Singleton
public class ModelParseServiceImpl implements IModelParseService<Pericope> {

    private static final String NAMESPACE = "http://www.hermeneutix.org/schema/hmx/2.0";
    private static final String SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String SCHEMA_REF_ATTRIBUTE = "xsi:schemaLocation";
    private static final String SCHEMA_LOCATION =
            "https://raw.githubusercontent.com/scientific-tool-set/scitos/master/scitos.hmx/schema/hmx-v2.0.xsd";
    private static final String TAG_ROOT = "Pericope";
    private static final String ATT_ROOT_FONT = "Font";
    private static final String ATT_ROOT_FONTSIZE = "FontSize";
    private static final String ATT_ROOT_LANGUAGE = "Language";
    private static final String ATT_ROOT_AUTHOR = "Author";
    private static final String ATT_ROOT_TITLE = "Title";
    private static final String ATT_ROOT_COMMENT = "Comment";
    private static final String ATT_ROOT_ORIENTATION = "text-orientation";
    private static final String VAL_ROOT_ORIENTATION_LTR = "left-to-right";
    private static final String VAL_ROOT_ORIENTATION_RTL = "right-to-left";

    private static final String TAG_LANGMODEL = "LanguageModel";
    /* Additional attributes directly on a model element, if it is one of the system defaults. */
    private static final String ATT_LANGMODEL_NAME = "name";
    private static final String ATT_LANGMODEL_LOCALE = "locale";
    private static final String ATT_LANGMODEL_NAME_COMPATIBLE = "oldName";
    private static final String ATT_LANGMODEL_ORIENTATION = ModelParseServiceImpl.ATT_ROOT_ORIENTATION;
    private static final String VAL_LANGMODEL_ORIENTATION_LTR = ModelParseServiceImpl.VAL_ROOT_ORIENTATION_LTR;
    private static final String VAL_LANGMODEL_ORIENTATION_RTL = ModelParseServiceImpl.VAL_ROOT_ORIENTATION_RTL;
    /* The actual language model in its grouped, as they are being displayed on the frontend. */
    private static final String TAG_LANGMODEL_GROUP = "Group";
    private static final String TAG_LANGMODEL_FUNCTION = "Function";
    private static final String ATT_LANGMODEL_FUNCTION_CODE = "code";
    private static final String ATT_LANGMODEL_FUNCTION_DESCRIPTION = "description";
    private static final String ATT_LANGMODEL_FUNCTION_NAME = "name";
    private static final String ATT_LANGMODEL_FUNCTION_NAME_COMPATIBLE = "oldKey";
    private static final String ATT_LANGMODEL_FUNCTION_STYLE = "style";
    private static final String VAL_LANGMODEL_FUNCTION_STYLE_UNDERLINE = "underline";
    private static final String TAG_LANGMODEL_FUNCTIONGROUP = "FunctionGroup";
    private static final String ATT_LANGMODEL_FUNCTIONGROUP_NAME = ModelParseServiceImpl.ATT_LANGMODEL_FUNCTION_NAME;
    private static final String ATT_LANGMODEL_FUNCTIONGROUP_DESCRIPTION = ModelParseServiceImpl.ATT_LANGMODEL_FUNCTION_DESCRIPTION;

    private static final String TAG_RELMODEL = "RelationModel";
    private static final String TAG_RELMODEL_GROUP = "Group";
    private static final String TAG_RELMODEL_RELATION = "Relation";
    private static final String ATT_RELMODEL_RELATION_DESCRIPTION = "description";
    private static final String TAG_RELMODEL_ASSOCIATE = "Associate";
    private static final String ATT_RELMODEL_ASSOCIATE_ROLE = "role";
    private static final String ATT_RELMODEL_ASSOCIATE_WEIGHT = "weight";
    private static final String VAL_RELMODEL_ASSOCIATE_WEIGHT_HIGH = "high";
    private static final String VAL_RELMODEL_ASSOCIATE_WEIGHT_LOW = "low";

    private static final String TAG_PROPOSITION = "Proposition";
    private static final String ATT_PROP_LABEL = "Label";
    private static final String ATT_PROP_FUNCTION = "Function";
    private static final String ATT_PROP_SEM_TRANSLATION = "SemTranslation";
    private static final String ATT_PROP_SYN_TRANSLATION = "SynTranslation";
    private static final String ATT_PROP_COMMENT = "Comment";

    private static final String TAG_PRIOR_PROP_SUB_TREE = "PriorPropositions";
    private static final String TAG_LATER_PROP_SUB_TREE = "LaterPropositions";
    private static final String TAG_PART_PROP_SUB_TREE = "PartAfterArrowProposition";
    private static final String TAG_CLAUSE_ITEM_SUB_TREE = "ClauseItems";

    private static final String TAG_CLAUSE_ITEM = "Item";
    private static final String ATT_ITEM_FUNCTION = "Function";
    private static final String ATT_ITEM_TEXT = "OriginText";
    private static final String ATT_ITEM_STYLE = "FontStyle";
    private static final String VAL_ITEM_STYLE_BOLD = "Bold";
    private static final String VAL_ITEM_STYLE_ITALIC = "Italic";
    private static final String VAL_ITEM_STYLE_BOLD_ITALIC = "BoldAndItalic";
    private static final String ATT_ITEM_COMMENT = "Comment";

    private static final String TAG_RELATION_SUB_TREE = "Connectables";
    private static final String TAG_CONNECTABLE = "Connectable";
    private static final String ATT_CONN_ROLE = "Role";
    private static final String ATT_CONN_WEIGHT = "Weight";
    private static final String VAL_CONN_WEIGHT_HIGH = "high";
    private static final String VAL_CONN_WEIGHT_LOW = "low";
    private static final String ATT_RELATION_COMMENT = "Comment";

    /**
     * Main constructor for the state-less service implementation.
     */
    @Inject
    public ModelParseServiceImpl() {
        // constructor for dependency injection
    }

    @Override
    public List<ExportOption> getSupportedExports() {
        // TODO enable exportToHtml new ExportOption(HmxMessage.EXPORT_TYPE_HTML, TargetFileType.HTML, null),
        return Arrays.asList(new ExportOption(HmxMessage.EXPORT_TYPE_SVG, TargetFileType.SVG, null));
    }

    @Override
    public Document parseXmlFromModel(final IModel<?> model, final List<?> openViewElements) throws HmxException {
        final Document xml;
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            xml = factory.newDocumentBuilder().newDocument();
        } catch (final ParserConfigurationException pcex) {
            throw new HmxException(Message.ERROR_UNKNOWN, pcex);
        }
        final Element root = (Element) xml.appendChild(xml.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_ROOT));
        // add schema reference
        root.setAttributeNS(ModelParseServiceImpl.SCHEMA_NAMESPACE, ModelParseServiceImpl.SCHEMA_REF_ATTRIBUTE, ModelParseServiceImpl.NAMESPACE
                + ' ' + ModelParseServiceImpl.SCHEMA_LOCATION);
        final Pericope pericope = (Pericope) model;
        // add attribute for the origin text font
        root.setAttribute(ModelParseServiceImpl.ATT_ROOT_FONT, pericope.getFont().getFontName());
        root.setAttribute(ModelParseServiceImpl.ATT_ROOT_FONTSIZE, Integer.toString(pericope.getFont().getSize()));
        // add project meta data
        DomUtil.setNullableAttribute(root, ModelParseServiceImpl.ATT_ROOT_AUTHOR, pericope.getAuthor());
        DomUtil.setNullableAttribute(root, ModelParseServiceImpl.ATT_ROOT_TITLE, pericope.getTitle());
        DomUtil.setNullableAttribute(root, ModelParseServiceImpl.ATT_ROOT_COMMENT, pericope.getComment());
        // include language model
        root.setAttribute(ModelParseServiceImpl.ATT_ROOT_LANGUAGE, pericope.getLanguage());
        root.setAttribute(ModelParseServiceImpl.ATT_ROOT_ORIENTATION, pericope.isLeftToRightOriented()
                ? ModelParseServiceImpl.VAL_ROOT_ORIENTATION_LTR : ModelParseServiceImpl.VAL_ROOT_ORIENTATION_RTL);
        root.appendChild(this.parseXmlFromLanguageModel(xml, pericope));
        // add each root Proposition (a Proposition that is not subordinated to another Proposition) of the Pericope
        for (final Proposition singleRootProposition : pericope.getText()) {
            root.appendChild(this.parseXmlFromProposition(xml, singleRootProposition));
        }
        // add the semantic structure
        final Element connectablesNode = xml.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_RELATION_SUB_TREE);
        Proposition currentProposition = pericope.getPropositionAt(0);
        while (currentProposition != null) {
            AbstractConnectable topMostConnectable = currentProposition;
            while (topMostConnectable.getSuperOrdinatedRelation() != null) {
                topMostConnectable = topMostConnectable.getSuperOrdinatedRelation();
            }
            connectablesNode.appendChild(this.parseXmlFromConnectable(xml, topMostConnectable));
            currentProposition = topMostConnectable.getFollowingConnectableProposition();
        }
        root.appendChild(connectablesNode);
        return xml;
    }

    /**
     * Create the xml structure for the language model represented by the given {@code model}.
     *
     * @param xml
     *            the designated xml document the element is created for
     * @param model
     *            the provider of {@link SyntacticalFunction}s to represent
     * @return the created xml element
     */
    public Element parseXmlFromLanguageModel(final Document xml, final ISyntacticalFunctionProvider model) {
        final Element syntacticalModelNode = xml.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_LANGMODEL);
        if (model instanceof LanguageModel) {
            syntacticalModelNode.setAttribute(ModelParseServiceImpl.ATT_LANGMODEL_NAME, ((LanguageModel) model).getName());
            final String textOrientation;
            if (((LanguageModel) model).isLeftToRightOriented()) {
                textOrientation = ModelParseServiceImpl.VAL_LANGMODEL_ORIENTATION_LTR;
            } else {
                textOrientation = ModelParseServiceImpl.VAL_LANGMODEL_ORIENTATION_RTL;
            }
            syntacticalModelNode.setAttribute(ModelParseServiceImpl.ATT_LANGMODEL_ORIENTATION, textOrientation);
        }
        for (final List<SyntacticalFunction> singleGroup : model.provideFunctions()) {
            final Element groupNode = xml.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_LANGMODEL_GROUP);
            for (final SyntacticalFunction singleFunction : singleGroup) {
                groupNode.appendChild(this.parseXmlFromSyntacticalFunction(xml, singleFunction));
            }
            syntacticalModelNode.appendChild(groupNode);
        }
        return syntacticalModelNode;
    }

    /**
     * Create the xml structure representing the given {@link SyntacticalFunction}. This includes potentially contained sub functions.
     *
     * @param xml
     *            the designated xml document the element is created for
     * @param function
     *            the {@link SyntacticalFunction} to represent
     * @return the created xml element
     */
    private Element parseXmlFromSyntacticalFunction(final Document xml, final SyntacticalFunction function) {
        final Element functionNode;
        if (function.getSubFunctions().isEmpty()) {
            functionNode = xml.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_LANGMODEL_FUNCTION);
            functionNode.setAttribute(ModelParseServiceImpl.ATT_LANGMODEL_FUNCTION_CODE, function.getCode());
            functionNode.setAttribute(ModelParseServiceImpl.ATT_LANGMODEL_FUNCTION_NAME, function.getName());
            if (function.isUnderlined()) {
                functionNode.setAttribute(ModelParseServiceImpl.ATT_LANGMODEL_FUNCTION_STYLE,
                        ModelParseServiceImpl.VAL_LANGMODEL_FUNCTION_STYLE_UNDERLINE);
            }
            DomUtil.setNullableAttribute(functionNode, ModelParseServiceImpl.ATT_LANGMODEL_FUNCTION_DESCRIPTION, function.getDescription());
        } else {
            functionNode = xml.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_LANGMODEL_FUNCTIONGROUP);
            functionNode.setAttribute(ModelParseServiceImpl.ATT_LANGMODEL_FUNCTIONGROUP_NAME, function.getName());
            DomUtil.setNullableAttribute(functionNode, ModelParseServiceImpl.ATT_LANGMODEL_FUNCTIONGROUP_DESCRIPTION, function.getDescription());
            // recursively build contained sub functions
            for (final SyntacticalFunction singleFunction : function.getSubFunctions()) {
                functionNode.appendChild(this.parseXmlFromSyntacticalFunction(xml, singleFunction));
            }
        }
        return functionNode;
    }

    /**
     * Generate a xml element representing the given {@link RelationModel}.
     *
     * @param xml
     *            the designated xml document the generated element will be a part of
     * @param model
     *            the model containing available semantical relations to be parsed
     * @return the generated xml element representing the given relation model
     */
    public Element parseXmlFromRelationModel(final Document xml, final ISemanticalRelationProvider model) {
        final Element semanticalModelNode = xml.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_RELMODEL);
        for (final List<RelationTemplate> singleGroup : model.provideRelationTemplates()) {
            final Element groupNode = xml.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_RELMODEL_GROUP);
            for (final RelationTemplate singleTemplate : singleGroup) {
                final Element relationNode = xml.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_RELMODEL_RELATION);
                final int associateCount;
                if (singleTemplate.canHaveMoreThanTwoAssociates()) {
                    associateCount = 3;
                } else {
                    associateCount = 2;
                }
                for (final AssociateRole singleRole : singleTemplate.getAssociateRoles(associateCount)) {
                    final Element roleNode = xml.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_RELMODEL_ASSOCIATE);
                    roleNode.setAttribute(ModelParseServiceImpl.ATT_RELMODEL_ASSOCIATE_ROLE, singleRole.getRole());
                    final String weight;
                    if (singleRole.isHighWeight()) {
                        weight = ModelParseServiceImpl.VAL_RELMODEL_ASSOCIATE_WEIGHT_HIGH;
                    } else {
                        weight = ModelParseServiceImpl.VAL_RELMODEL_ASSOCIATE_WEIGHT_LOW;
                    }
                    roleNode.setAttribute(ModelParseServiceImpl.ATT_RELMODEL_ASSOCIATE_WEIGHT, weight);
                    relationNode.appendChild(roleNode);
                }
                groupNode.appendChild(relationNode);
            }
            semanticalModelNode.appendChild(groupNode);
        }
        return semanticalModelNode;
    }

    /**
     * Create the xml structure representing the given {@link Proposition}. This includes all its subordinated {@link Proposition}s and potential
     * {@code partAfterArrow}s.
     *
     * @param xml
     *            the designated xml document the element is created for
     * @param model
     *            the {@link Proposition} to represent
     * @return the created xml element
     */
    private Element parseXmlFromProposition(final Document xml, final Proposition model) {
        final Element propositionNode = xml.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_PROPOSITION);
        DomUtil.setNullableAttribute(propositionNode, ModelParseServiceImpl.ATT_PROP_LABEL, model.getLabel());
        if (model.getFunction() != null) {
            propositionNode.setAttribute(ModelParseServiceImpl.ATT_PROP_FUNCTION, model.getFunction().getCode());
        }
        DomUtil.setNullableAttribute(propositionNode, ModelParseServiceImpl.ATT_PROP_SYN_TRANSLATION, model.getSynTranslation());
        DomUtil.setNullableAttribute(propositionNode, ModelParseServiceImpl.ATT_PROP_SEM_TRANSLATION, model.getSemTranslation());
        DomUtil.setNullableAttribute(propositionNode, ModelParseServiceImpl.ATT_PROP_COMMENT, model.getComment());
        // add all preceding subordinated Propositions to a dedicated group node
        final List<Proposition> priorChildren = model.getPriorChildren();
        if (!priorChildren.isEmpty()) {
            final Element childGroupNode = xml.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_PRIOR_PROP_SUB_TREE);
            for (final Proposition singleChild : priorChildren) {
                childGroupNode.appendChild(this.parseXmlFromProposition(xml, singleChild));
            }
            propositionNode.appendChild(childGroupNode);
        }
        // add all ClauseItems to dedicated group node
        final Element clauseItemGroupNode = xml.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_CLAUSE_ITEM_SUB_TREE);
        for (final ClauseItem singleItem : model.getItems()) {
            final Element itemNode = xml.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_CLAUSE_ITEM);
            itemNode.setAttribute(ModelParseServiceImpl.ATT_ITEM_TEXT, singleItem.getOriginText());
            if (singleItem.getFunction() != null) {
                itemNode.setAttribute(ModelParseServiceImpl.ATT_ITEM_FUNCTION, singleItem.getFunction().getCode());
            }
            final String styleValue;
            switch (singleItem.getFontStyle()) {
            case BOLD:
                styleValue = ModelParseServiceImpl.VAL_ITEM_STYLE_BOLD;
                break;
            case ITALIC:
                styleValue = ModelParseServiceImpl.VAL_ITEM_STYLE_ITALIC;
                break;
            case BOLD_ITALIC:
                styleValue = ModelParseServiceImpl.VAL_ITEM_STYLE_BOLD_ITALIC;
                break;
            default:
                styleValue = null;
            }
            DomUtil.setNullableAttribute(itemNode, ModelParseServiceImpl.ATT_ITEM_STYLE, styleValue);
            DomUtil.setNullableAttribute(itemNode, ModelParseServiceImpl.ATT_ITEM_COMMENT, singleItem.getComment());
            clauseItemGroupNode.appendChild(itemNode);
        }
        propositionNode.appendChild(clauseItemGroupNode);
        // add all following subordinated Propositions to a dedicated group node
        final List<Proposition> laterChildren = model.getLaterChildren();
        if (!laterChildren.isEmpty()) {
            final Element childGroupNode = xml.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_LATER_PROP_SUB_TREE);
            for (final Proposition singleChild : laterChildren) {
                childGroupNode.appendChild(this.parseXmlFromProposition(xml, singleChild));
            }
            propositionNode.appendChild(childGroupNode);
        }
        // add the Proposition's partAfterArrow
        if (model.getPartAfterArrow() != null) {
            final Element partAfterArrowNode = xml.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_PART_PROP_SUB_TREE);
            partAfterArrowNode.appendChild(this.parseXmlFromProposition(xml, model.getPartAfterArrow()));
            propositionNode.appendChild(partAfterArrowNode);
        }
        return propositionNode;
    }

    /**
     * Create the xml structure representing the given {@link AbstractConnectable}'s role and weight in its {@link Pericope}'s semantical analysis. In
     * case of a {@link Relation} this includes recursively all associated (i.e. contained) {@link AbstractConnectable}s.
     *
     * @param xml
     *            the designated xml document the element is created for
     * @param model
     *            the {@link AbstractConnectable} (i.e. {@link Proposition} or {@link Relation}) to represent
     * @return the created xml element
     */
    private Element parseXmlFromConnectable(final Document xml, final AbstractConnectable model) {
        final Element connectableNode = xml.createElementNS(ModelParseServiceImpl.NAMESPACE, ModelParseServiceImpl.TAG_CONNECTABLE);
        // adds the role attribute to a connectable element
        final AssociateRole connectableRole = model.getRole();
        if (connectableRole != null) {
            connectableNode.setAttribute(ModelParseServiceImpl.ATT_CONN_ROLE, connectableRole.getRole());
            connectableNode.setAttribute(ModelParseServiceImpl.ATT_CONN_WEIGHT, connectableRole.isHighWeight()
                    ? ModelParseServiceImpl.VAL_CONN_WEIGHT_HIGH : ModelParseServiceImpl.VAL_CONN_WEIGHT_LOW);
        }
        // add the comment only for Relations (as Propositions already get their comments in their separate representation)
        if (model instanceof Relation) {
            DomUtil.setNullableAttribute(connectableNode, ModelParseServiceImpl.ATT_RELATION_COMMENT, model.getComment());
            // insert all sub ordinated Connectables
            for (final AbstractConnectable singleAssociate : (Relation) model) {
                connectableNode.appendChild(this.parseXmlFromConnectable(xml, singleAssociate));
            }
        }
        return connectableNode;
    }

    @Override
    public Entry<Pericope, List<?>> parseModelFromXml(final Document xml, final File originPath) throws HmxException {
        // Reads the root Element (Pericope tag) of the xml file
        final Element pericopeNode = xml.getDocumentElement();
        // retrieve language model and relation model
        final String language = pericopeNode.getAttribute(ModelParseServiceImpl.ATT_ROOT_LANGUAGE);
        final LookupLanguageModel languageModel;
        final Translator<CompatibleRelationRole> compatibleRoleTranslator;
        final Element syntacticalModelNode = DomUtil.getChildElement(pericopeNode, ModelParseServiceImpl.TAG_LANGMODEL);
        if (syntacticalModelNode == null) {
            /*
             * Load the backwards compatible language model from the internal default models, matching the given language key. This is to allow
             * opening of HermeneutiX files, that have been created with the (old) HermeneutiX standalone application.
             */
            final Element systemModelNode = this.getSystemLanguageModelNodes().get(language);
            if (systemModelNode == null) {
                throw new HmxException(Message.ERROR_FILE_INVALID);
            }
            final String textOrientation = systemModelNode.getAttribute(ModelParseServiceImpl.ATT_LANGMODEL_ORIENTATION);
            languageModel =
                    this.parseLanguageModelFromXml(systemModelNode, systemModelNode.getAttribute(ModelParseServiceImpl.ATT_LANGMODEL_NAME),
                            textOrientation);
            compatibleRoleTranslator = new Translator<CompatibleRelationRole>(CompatibleRelationRole.class);
        } else {
            final String textOrientation = pericopeNode.getAttribute(ModelParseServiceImpl.ATT_ROOT_ORIENTATION);
            languageModel = this.parseLanguageModelFromXml(syntacticalModelNode, language, textOrientation);
            // no translation needed, as the new file format contains the full role names without i18n in favor of configurability
            compatibleRoleTranslator = null;
        }
        // Reads the font name for the origin text font
        final String fontName = DomUtil.getNullableAttribute(pericopeNode, ModelParseServiceImpl.ATT_ROOT_FONT);
        // Reads the font size for the origin text font
        final int fontSize = DomUtil.getIntAttribute(pericopeNode, ModelParseServiceImpl.ATT_ROOT_FONTSIZE, 14);
        // Creates a new Font with the read information for the origin text
        final Font font = new Font(fontName, Font.PLAIN, fontSize);
        // create the resulting instance
        final Pericope newPericope = new Pericope();
        newPericope.init(null, languageModel, font);
        newPericope.setTitle(DomUtil.getNullableAttribute(pericopeNode, ModelParseServiceImpl.ATT_ROOT_TITLE));
        newPericope.setAuthor(DomUtil.getNullableAttribute(pericopeNode, ModelParseServiceImpl.ATT_ROOT_AUTHOR));
        newPericope.setComment(DomUtil.getNullableAttribute(pericopeNode, ModelParseServiceImpl.ATT_ROOT_COMMENT));

        // Creates an List of Propositions for the rootPropositions
        final List<Proposition> text = new LinkedList<Proposition>();
        // all top level children, including the root Propositions
        for (final Element topLevelProposition : DomUtil.getChildElements(pericopeNode, ModelParseServiceImpl.TAG_PROPOSITION)) {
            text.add(this.parsePropositionFromXml(topLevelProposition, languageModel));
        }
        // sets the retrieved rootPropositions as the text of the new Pericope
        newPericope.addNewPropositions(text, false);

        final Element connectablesTree = DomUtil.getChildElement(pericopeNode, ModelParseServiceImpl.TAG_RELATION_SUB_TREE);
        if (connectablesTree != null) {
            // gets a list of all Propositions contained in the pericope in order of appearence
            final Deque<Proposition> propositionsInOrder = new LinkedList<Proposition>();
            Proposition nextProposition = newPericope.getPropositionAt(0);
            do {
                propositionsInOrder.addLast(nextProposition);
                nextProposition = nextProposition.getFollowingConnectableProposition();
            } while (nextProposition != null);
            for (final Element topLevelConnectable : DomUtil.getChildElements(connectablesTree, ModelParseServiceImpl.TAG_CONNECTABLE)) {
                this.parseConnectableFromXml(topLevelConnectable, propositionsInOrder, compatibleRoleTranslator);
            }
        }
        return new SimpleEntry<Pericope, List<?>>(newPericope, Collections.singletonList(newPericope));
    }

    /**
     * Retrieve system defined {@link LanguageModel}s for the currently active system {@link Locale}.
     *
     * @return all system defined {@link LanguageModel}s
     * @throws HmxException
     *             internal error while looking up or parsing a system defined model
     */
    public List<LanguageModel> getSystemLanguageModels() throws HmxException {
        final Map<String, Element> systemModelNodes = this.getSystemLanguageModelNodes();
        final List<LanguageModel> result = new ArrayList<LanguageModel>(systemModelNodes.size());
        for (final Element modelNode : systemModelNodes.values()) {
            final LookupLanguageModel model =
                    this.parseLanguageModelFromXml(modelNode, modelNode.getAttribute(ModelParseServiceImpl.ATT_LANGMODEL_NAME),
                            modelNode.getAttribute(ModelParseServiceImpl.ATT_LANGMODEL_ORIENTATION));
            // remove backward compatibility information
            final LanguageModel incompatibleModel = new LanguageModel(model.getName(), model.isLeftToRightOriented());
            for (final List<SyntacticalFunction> singleFunctionGroup : model.provideFunctions()) {
                incompatibleModel.add(this.removeBackwardCompatibilityInfo(singleFunctionGroup));
            }
            result.add(incompatibleModel);
        }
        return result;
    }

    private List<SyntacticalFunction> removeBackwardCompatibilityInfo(final List<SyntacticalFunction> possibleCompatibleFunctions) {
        final List<SyntacticalFunction> result = new ArrayList<SyntacticalFunction>(possibleCompatibleFunctions.size());
        for (final SyntacticalFunction singleFunction : possibleCompatibleFunctions) {
            result.add(new SyntacticalFunction(singleFunction.getCode(), singleFunction.getName(), singleFunction.isUnderlined(), singleFunction
                    .getDescription(), this.removeBackwardCompatibilityInfo(singleFunction.getSubFunctions())));
        }
        return result;
    }

    /**
     * Collect all system language models for the currently active user locale from the internal xml file.
     *
     * @return the DOM elements representing a system language model, associated with the user language independent key for the origin text language
     * @throws HmxException
     *             internal error while parsing system language model file
     * @see #parseLanguageModelFromXml(Element, String, String)
     */
    private Map<String, Element> getSystemLanguageModelNodes() throws HmxException {
        InputStream systemModelFileInput = null;
        final List<Element> systemModelNodes;
        try {
            systemModelFileInput = ModelParseServiceImpl.class.getResourceAsStream("/org/hmx/scitos/hmx/core/language-models.xml");
            // parse file into xml structure
            final Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(systemModelFileInput).getDocumentElement();
            systemModelNodes = DomUtil.getChildElements(root, ModelParseServiceImpl.TAG_LANGMODEL);
        } catch (final ParserConfigurationException pcex) {
            // error while creating a DocumentBuilder instance from factory or while accessing file
            throw new HmxException(Message.ERROR_UNKNOWN, pcex);
        } catch (final IOException ioex) {
            // error while creating a DocumentBuilder instance from factory or while accessing file
            throw new HmxException(Message.ERROR_UNKNOWN, ioex);
        } catch (final SAXException se) {
            // error while interpreting (invalid) xml structure
            throw new HmxException(Message.ERROR_UNKNOWN, se);
        } finally {
            if (systemModelFileInput != null) {
                try {
                    systemModelFileInput.close();
                } catch (final IOException expected) {
                    // ignore
                }
            }
        }
        final String userLanguage = Option.TRANSLATION.getValueAsLocale().getLanguage();
        final Map<String, Element> availableModels = new HashMap<String, Element>();
        for (final Element modelNode : systemModelNodes) {
            // get the user language independent name of this model
            final String compatibleName = modelNode.getAttribute(ModelParseServiceImpl.ATT_LANGMODEL_NAME_COMPATIBLE);
            // for each model name, get the version for the specific user locale (falling back on the version without localization)
            if (!availableModels.containsKey(compatibleName) && !modelNode.hasAttribute(ModelParseServiceImpl.ATT_LANGMODEL_LOCALE)
                    || modelNode.getAttribute(ModelParseServiceImpl.ATT_LANGMODEL_LOCALE).equals(userLanguage)) {
                availableModels.put(compatibleName, modelNode);
            }
        }
        return availableModels;
    }

    /**
     * Retrieve the language model represented by the given xml element.
     *
     * @param syntacticalModelNode
     *            node representing the full language model contained in a currently parsed file
     * @param language
     *            the name of the language model to load (usually the name of the origin text's language)
     * @param textOrientation
     *            the value of the attribute representing the origin text's orientation (should be either {@value #VAL_ROOT_ORIENTATION_LTR} or
     *            {@value #VAL_ROOT_ORIENTATION_RTL})
     * @return successfully parsed language model
     * @throws HmxException
     *             could not build/retrieve a valid language model
     */
    private LookupLanguageModel parseLanguageModelFromXml(final Element syntacticalModelNode, final String language, final String textOrientation)
            throws HmxException {
        final LookupLanguageModel model =
                new LookupLanguageModel(language, !ModelParseServiceImpl.VAL_ROOT_ORIENTATION_RTL.equals(textOrientation));
        // read model contents from given node
        for (final Element mainGroup : DomUtil.getChildElements(syntacticalModelNode, ModelParseServiceImpl.TAG_LANGMODEL_GROUP)) {
            model.add(this.parseSyntacticalFunctionsFromXml(mainGroup));
        }
        return model;
    }

    /**
     * Retrieve the {@link SyntacticalFunction} contained in the given {@code parentNode}.
     *
     * @param parentNode
     *            element containing the {@value #TAG_LANGMODEL_FUNCTION} and {@value #TAG_LANGMODEL_FUNCTIONGROUP} children being parsed as
     *            {@link SyntacticalFunction}s
     * @return successfully parsed functions
     */
    private List<SyntacticalFunction> parseSyntacticalFunctionsFromXml(final Element parentNode) {
        final LinkedList<SyntacticalFunction> result = new LinkedList<SyntacticalFunction>();
        for (final Element singleFunctionNode : DomUtil.getChildElements(parentNode, ModelParseServiceImpl.TAG_LANGMODEL_FUNCTION,
                ModelParseServiceImpl.TAG_LANGMODEL_FUNCTIONGROUP)) {
            if (ModelParseServiceImpl.TAG_LANGMODEL_FUNCTION.equals(singleFunctionNode.getTagName())) {
                final String code = singleFunctionNode.getAttribute(ModelParseServiceImpl.ATT_LANGMODEL_FUNCTION_CODE);
                final String name = singleFunctionNode.getAttribute(ModelParseServiceImpl.ATT_LANGMODEL_FUNCTION_NAME);
                final boolean underlined =
                        ModelParseServiceImpl.VAL_LANGMODEL_FUNCTION_STYLE_UNDERLINE.equals(singleFunctionNode
                                .getAttribute(ModelParseServiceImpl.ATT_LANGMODEL_FUNCTION_STYLE));
                final String description =
                        DomUtil.getNullableAttribute(singleFunctionNode, ModelParseServiceImpl.ATT_LANGMODEL_FUNCTION_DESCRIPTION);
                final String oldKey = singleFunctionNode.getAttribute(ModelParseServiceImpl.ATT_LANGMODEL_FUNCTION_NAME_COMPATIBLE);
                if (oldKey.isEmpty()) {
                    result.add(new SyntacticalFunction(code, name, underlined, description, null));
                } else {
                    result.add(new BackwardCompatibleFunction(oldKey, code, name, underlined, description));
                }
            } else {
                final String name = singleFunctionNode.getAttribute(ModelParseServiceImpl.ATT_LANGMODEL_FUNCTIONGROUP_NAME);
                final String description =
                        DomUtil.getNullableAttribute(singleFunctionNode, ModelParseServiceImpl.ATT_LANGMODEL_FUNCTIONGROUP_DESCRIPTION);
                // recursively collect subordinated functions in this group
                final List<SyntacticalFunction> subFunctions = this.parseSyntacticalFunctionsFromXml(singleFunctionNode);
                result.add(new SyntacticalFunction("", name, false, description, subFunctions));
            }
        }
        return result;
    }

    /**
     * Generate the system default {@link RelationModel} (depending on the current setting of {@link Option#TRANSLATION} this is either in English or
     * German).
     *
     * @return successfully generated system default {@link RelationModel}
     * @throws HmxException
     *             internal error when loading system default model
     */
    public RelationModel getSystemRelationModel() throws HmxException {
        InputStream systemModelFileInput = null;
        final List<Element> systemModelNodes;
        try {
            systemModelFileInput = ModelParseServiceImpl.class.getResourceAsStream("/org/hmx/scitos/hmx/core/relation-models.xml");
            // parse file into xml structure
            final Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(systemModelFileInput).getDocumentElement();
            systemModelNodes = DomUtil.getChildElements(root, ModelParseServiceImpl.TAG_RELMODEL);
        } catch (final ParserConfigurationException pcex) {
            // error while creating a DocumentBuilder instance from factory or while accessing file
            throw new HmxException(Message.ERROR_UNKNOWN, pcex);
        } catch (final IOException ioex) {
            // error while creating a DocumentBuilder instance from factory or while accessing file
            throw new HmxException(Message.ERROR_UNKNOWN, ioex);
        } catch (final SAXException se) {
            // error while interpreting (invalid) xml structure
            throw new HmxException(Message.ERROR_UNKNOWN, se);
        } finally {
            if (systemModelFileInput != null) {
                try {
                    systemModelFileInput.close();
                } catch (final IOException expected) {
                    // ignore
                }
            }
        }
        final String userLanguage = Option.TRANSLATION.getValueAsLocale().getLanguage();
        Element semanticalModelNode = null;
        for (final Element modelNode : systemModelNodes) {
            // get the user language independent name of this model
            // for each model name, get the version for the specific user locale (falling back on the version without localization)
            if (!modelNode.hasAttribute(ModelParseServiceImpl.ATT_LANGMODEL_LOCALE)) {
                semanticalModelNode = modelNode;
            } else if (modelNode.getAttribute(ModelParseServiceImpl.ATT_LANGMODEL_LOCALE).equals(userLanguage)) {
                semanticalModelNode = modelNode;
                break;
            }
        }
        if (semanticalModelNode == null) {
            return null;
        }
        return this.parseRelationModelFromXml(semanticalModelNode);
    }

    /**
     * Retrieve the single (i.e. first) contained {@link RelationModel} from the given {@code xml} document.
     *
     * @param xml
     *            the document that is expected to contain a {@value #TAG_RELMODEL} element directly under its root, which is being parsed
     * @return successfully retrieved {@link RelationModel}
     * @throws HmxException
     *             invalid model
     */
    public RelationModel parseRelationModelFromXml(final Document xml) throws HmxException {
        final Element semanticalModelNode = DomUtil.getChildElement(xml.getDocumentElement(), ModelParseServiceImpl.TAG_RELMODEL);
        if (semanticalModelNode == null) {
            return null;
        }
        return this.parseRelationModelFromXml(semanticalModelNode);
    }

    /**
     * Retrieve the represented {@link RelationModel} from the given xml node.
     *
     * @param semanticalModelNode
     *            the {@value #TAG_RELMODEL} element being parsed
     * @return successfully parsed available semantical relations
     * @throws HmxException
     *             invalid contained definition or no model contents at all
     */
    private RelationModel parseRelationModelFromXml(final Element semanticalModelNode) throws HmxException {
        final RelationModel model = new RelationModel();
        for (final Element singleGroupNode : DomUtil.getChildElements(semanticalModelNode, ModelParseServiceImpl.TAG_RELMODEL_GROUP)) {
            final List<RelationTemplate> group = new LinkedList<RelationTemplate>();
            for (final Element singleTemplateNode : DomUtil.getChildElements(singleGroupNode, ModelParseServiceImpl.TAG_RELMODEL_RELATION)) {
                final List<AssociateRole> roles = new ArrayList<AssociateRole>(3);
                for (final Element singleRoleNode : DomUtil.getChildElements(singleTemplateNode, ModelParseServiceImpl.TAG_RELMODEL_ASSOCIATE)) {
                    final String roleName = singleRoleNode.getAttribute(ModelParseServiceImpl.ATT_RELMODEL_ASSOCIATE_ROLE);
                    final boolean isHighWeight =
                            ModelParseServiceImpl.VAL_RELMODEL_ASSOCIATE_WEIGHT_HIGH.equals(singleRoleNode
                                    .getAttribute(ModelParseServiceImpl.ATT_RELMODEL_ASSOCIATE_WEIGHT));
                    if (roleName.isEmpty()) {
                        throw new HmxException(Message.ERROR_FILE_INVALID);
                    }
                    roles.add(new AssociateRole(roleName, isHighWeight));
                }
                if (roles.size() < 2 || 3 < roles.size()) {
                    throw new HmxException(Message.ERROR_FILE_INVALID);
                }
                final AssociateRole repetitiveAssociate;
                if (roles.size() == 2) {
                    repetitiveAssociate = null;
                } else {
                    repetitiveAssociate = roles.get(1);
                }
                final String description =
                        DomUtil.getNullableAttribute(singleTemplateNode, ModelParseServiceImpl.ATT_RELMODEL_RELATION_DESCRIPTION);
                group.add(new RelationTemplate(roles.get(0), repetitiveAssociate, roles.get(roles.size() - 1), description));
            }
            if (!group.isEmpty()) {
                model.add(group);
            }
        }
        if (model.provideRelationTemplates().isEmpty()) {
            throw new HmxException(Message.ERROR_FILE_INVALID);
        }
        return model;
    }

    /**
     * Retrieve the {@link Proposition} represented by the given element.
     *
     * @param propositionNode
     *            the element being parsed (representing a {@link Proposition} including its subordinated elements and a potential
     *            {@code partAfterArrow})
     * @param languageModel
     *            the language model to retrieve {@link SyntacticalFunction}s from
     * @return retrieved {@link Proposition}
     * @throws HmxException
     *             invalid file structure
     */
    private Proposition parsePropositionFromXml(final Element propositionNode, final LookupLanguageModel languageModel) throws HmxException {
        final Element tempItemElement = DomUtil.getChildElement(propositionNode, ModelParseServiceImpl.TAG_CLAUSE_ITEM_SUB_TREE);
        if (tempItemElement == null) {
            // a Proposition must contain ClauseItems
            throw new HmxException(Message.ERROR_FILE_INVALID);
        }
        // collect the contained ClauseItems
        final List<ClauseItem> itemList = new LinkedList<ClauseItem>();
        for (final Element singleClauseItem : DomUtil.getChildElements(tempItemElement, ModelParseServiceImpl.TAG_CLAUSE_ITEM)) {
            itemList.add(this.parseClauseItemFromXml(singleClauseItem, languageModel));
        }
        if (itemList.isEmpty()) {
            // a Proposition must contain ClauseItems
            throw new HmxException(Message.ERROR_FILE_INVALID);
        }
        final Proposition result = new Proposition(null, itemList);
        // retrieves attributes of a proposition from the xml file
        result.setLabel(DomUtil.getNullableAttribute(propositionNode, ModelParseServiceImpl.ATT_PROP_LABEL));
        result.setFunction(languageModel.getFunctionByCode(DomUtil.getNullableAttribute(propositionNode, ModelParseServiceImpl.ATT_PROP_FUNCTION)));
        result.setSemTranslation(DomUtil.getNullableAttribute(propositionNode, ModelParseServiceImpl.ATT_PROP_SEM_TRANSLATION));
        result.setSynTranslation(DomUtil.getNullableAttribute(propositionNode, ModelParseServiceImpl.ATT_PROP_SYN_TRANSLATION));
        result.setComment(DomUtil.getNullableAttribute(propositionNode, ModelParseServiceImpl.ATT_PROP_COMMENT));

        // adds prior propositions to the Proposition according to the xml code
        final Element priorPropositionsElement = DomUtil.getChildElement(propositionNode, ModelParseServiceImpl.TAG_PRIOR_PROP_SUB_TREE);
        if (priorPropositionsElement != null) {
            final List<Proposition> priorChildren = new LinkedList<Proposition>();
            // calls itself to add every prior Proposition contained in the Proposition
            for (final Element singlePriorChild : DomUtil.getChildElements(priorPropositionsElement, ModelParseServiceImpl.TAG_PROPOSITION)) {
                priorChildren.add(this.parsePropositionFromXml(singlePriorChild, languageModel));
            }
            result.setPriorChildren(priorChildren);
        }
        // adds later propositions to the proposition according to the xml code
        final Element laterPropositionsElement = DomUtil.getChildElement(propositionNode, ModelParseServiceImpl.TAG_LATER_PROP_SUB_TREE);
        if (laterPropositionsElement != null) {
            final List<Proposition> laterChildren = new LinkedList<Proposition>();
            // calls itself to add every later Proposition contained in the Proposition
            for (final Element singleLaterChild : DomUtil.getChildElements(laterPropositionsElement, ModelParseServiceImpl.TAG_PROPOSITION)) {
                laterChildren.add(this.parsePropositionFromXml(singleLaterChild, languageModel));
            }
            result.setLaterChildren(laterChildren);
        }
        // add part after arrow, if one exists
        final Element tempAfterArrowElement = DomUtil.getChildElement(propositionNode, ModelParseServiceImpl.TAG_PART_PROP_SUB_TREE);
        if (tempAfterArrowElement != null) {
            final Element propositionPart = DomUtil.getChildElement(tempAfterArrowElement, ModelParseServiceImpl.TAG_PROPOSITION);
            if (propositionPart != null) {
                result.setPartAfterArrow(this.parsePropositionFromXml(propositionPart, languageModel));
            }
        }
        // returns the created Proposition
        return result;
    }

    /**
     * Retrieve the {@link ClauseItem} represented by the given element.
     *
     * @param itemNode
     *            the element being parsed (representing a single clause item)
     * @param languageModel
     *            the language model to look up any assigned {@link SyntacticalFunction} from
     * @return successfully parsed {@link ClauseItem}
     */
    private ClauseItem parseClauseItemFromXml(final Element itemNode, final LookupLanguageModel languageModel) {
        final ClauseItem result = new ClauseItem(null, itemNode.getAttribute(ModelParseServiceImpl.ATT_ITEM_TEXT));
        result.setFunction(languageModel.getFunctionByCode(DomUtil.getNullableAttribute(itemNode, ModelParseServiceImpl.ATT_ITEM_FUNCTION)));
        result.setComment(DomUtil.getNullableAttribute(itemNode, ModelParseServiceImpl.ATT_ITEM_COMMENT));
        final String xmlValue = DomUtil.getNullableAttribute(itemNode, ModelParseServiceImpl.ATT_ITEM_STYLE);
        if (ModelParseServiceImpl.VAL_ITEM_STYLE_BOLD.equals(xmlValue)) {
            result.setFontStyle(Style.BOLD);
        } else if (ModelParseServiceImpl.VAL_ITEM_STYLE_ITALIC.equals(xmlValue)) {
            result.setFontStyle(Style.ITALIC);
        } else if (ModelParseServiceImpl.VAL_ITEM_STYLE_BOLD_ITALIC.equals(xmlValue)) {
            result.setFontStyle(Style.BOLD_ITALIC);
        } else {
            result.setFontStyle(Style.PLAIN);
        }
        return result;
    }

    /**
     * Retrieve the attributes associated with the semantical analysis from the given element. If the targeted element represents a {@link Relation},
     * it will be created and returned. Otherwise the first {@link Proposition} from the given stack will be polled (removed and returned).
     *
     * @param connectableElement
     *            the element being parsed (represents either a {@link Relation} or is the reference to a {@link Proposition}
     * @param propositionsInOrder
     *            the stack containing all {@link Proposition}s of the currently parsed {@link Pericope} in their text order
     * @param compatibleRoleTranslator
     *            the translator to use to map old associate role keys to their full (translated) counterparts (only applied in a compatibility
     *            scenario, i.e. if not {@code null})
     * @return successfully parsed {@link Relation} or the references {@link Proposition} from the stack
     */
    private AbstractConnectable parseConnectableFromXml(final Element connectableElement, final Deque<Proposition> propositionsInOrder,
            final Translator<CompatibleRelationRole> compatibleRoleTranslator) {
        final List<Element> associateNodes = DomUtil.getChildElements(connectableElement, ModelParseServiceImpl.TAG_CONNECTABLE);
        if (associateNodes.isEmpty()) {
            // Connectable to retrieve is a Proposition
            // Proposition fully initialized in the Pericope - to be deleted from the List of Propositions without superordinated Relations
            return propositionsInOrder.removeFirst();
        }
        // Connectable to retrieve is a Relation - iterate through all subordinated Connectables
        final List<AbstractConnectable> associates = new ArrayList<AbstractConnectable>(associateNodes.size());
        final List<AssociateRole> rolesAndWeights = new ArrayList<AssociateRole>(associateNodes.size());
        for (final Element singleAssociateNode : associateNodes) {
            // recursively handle contained nodes representing a connectable model element
            associates.add(this.parseConnectableFromXml(singleAssociateNode, propositionsInOrder, compatibleRoleTranslator));
            String roleName = singleAssociateNode.getAttribute(ModelParseServiceImpl.ATT_CONN_ROLE);
            if (compatibleRoleTranslator != null) {
                // translate the role key, as the parsed xml structure is in the old format from when HermeneutiX was a standalone application
                try {
                    roleName = new CompatibleRelationRole(roleName, compatibleRoleTranslator).get();
                } catch (final MissingResourceException mrex) {
                    // could not retrieve a proper translation for the assigned role, fall back on the key value
                    mrex.printStackTrace();
                }
            }
            rolesAndWeights.add(new AssociateRole(roleName, ModelParseServiceImpl.VAL_CONN_WEIGHT_HIGH.equals(singleAssociateNode
                    .getAttribute(ModelParseServiceImpl.ATT_CONN_WEIGHT))));
        }
        final Relation relation = new Relation(associates, rolesAndWeights);
        relation.setComment(DomUtil.getNullableAttribute(connectableElement, ModelParseServiceImpl.ATT_RELATION_COMMENT));
        return relation;
    }
}
