/*
   Copyright (C) 2015 HermeneutiX.org

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
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.KeyStroke;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.hmx.scitos.ais.domain.IDetailCategoryProvider;
import org.hmx.scitos.ais.domain.model.DetailCategory;
import org.hmx.scitos.ais.domain.model.MutableDetailCategoryModel;
import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.core.option.OptionHandler;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Application level preferences handler for the AIS module. Currently this encompasses only the default detail categories applied to new projects.
 */
@Singleton
public final class AisOption implements IDetailCategoryProvider {

    /** The root XML tag in the persisted options file. */
    private static final String TAG_ROOT = "Options";

    /** The path (determined by convention), where the options file is persisted/saved to. */
    private final String filePath;
    /**
     * The AIS module's ModelParseService implementation, for conversions between XML and the {@link MutableDetailCategoryModel}.
     */
    private final ModelParseServiceImpl modelParseService;
    /** The default detail categories for new projects. */
    private MutableDetailCategoryModel defaultDetailCategoryModel;

    /**
     * Main constructor.
     *
     * @param modelParseService
     *            the ModelParseService implementation, being used to convert between the XML structure and {@link MutableDetailCategoryModel}.
     */
    @Inject
    public AisOption(final ModelParseServiceImpl modelParseService) {
        this.modelParseService = modelParseService;
        // apply general option file naming convention
        this.filePath = OptionHandler.buildOptionFilePath(AisOption.class);
        // initialize default detail category model tree
        MutableDetailCategoryModel model = null;
        // retrieve settings from persistent storage (i.e. file)
        final File targetFile = new File(this.filePath);
        if (targetFile.exists() && targetFile.canRead()) {
            try {
                final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(targetFile);
                model = this.modelParseService.parseDetailCategoriesFromXml(doc);
            } catch (final ParserConfigurationException pcex) {
                // could not create a newDocumentBuilder()
                pcex.printStackTrace();
            } catch (final IOException ioex) {
                // could not read the contents of the given file
                ioex.printStackTrace();
            } catch (final SAXException saxe) {
                // could not parse the XML structure from the given file
                saxe.printStackTrace();
            } catch (final HmxException ex) {
                // the parsed XML structure did not yield a valid detail category model
                ex.printStackTrace();
            }
        }
        if (model == null) {
            // could not retrieve valid model from file, use default values instead
            model = AisOption.createDefaultCategoryModel();
        }
        // remember parsed or generated default detail category model to apply it to new project
        this.setDefaultDetailCategoryModel(model);
    }

    /**
     * Create the application's default detail category model, in case no model can be loaded from an options file.
     *
     * @return default detail categories
     */
    static MutableDetailCategoryModel createDefaultCategoryModel() {
        final MutableDetailCategoryModel model = new MutableDetailCategoryModel();
        // five internal categories, with red color and assigned numeric keys 1 to 5 without modifiers
        final Color internalColor = new Color(255, 51, 0);
        final DetailCategory internal = new DetailCategory(null, "Int", "Internal", false, internalColor, null);
        model.add(internal);
        model.add(AisOption.createSelectableCategory(internal, "Int1", "Internal: Event details", internalColor, KeyEvent.VK_1));
        model.add(AisOption.createSelectableCategory(internal, "Int2", "Internal: Place details", internalColor, KeyEvent.VK_2));
        model.add(AisOption.createSelectableCategory(internal, "Int3", "Internal: Time details", internalColor, KeyEvent.VK_3));
        model.add(AisOption.createSelectableCategory(internal, "Int4", "Internal: Perceptual details", internalColor, KeyEvent.VK_4));
        model.add(AisOption.createSelectableCategory(internal, "Int5", "Internal: Emotion/Thought details", internalColor, KeyEvent.VK_5));
        // five external categories, with blue color and assigned alphabetic keys Q, W, E, R, T without modifiers
        final Color externalColor = Color.BLUE;
        final DetailCategory external = new DetailCategory(null, "Ext", "External", false, externalColor, null);
        model.add(external);
        model.add(AisOption.createSelectableCategory(external, "Ext1", "External: Semantic details", externalColor, KeyEvent.VK_Q));
        model.add(AisOption.createSelectableCategory(external, "Ext2", "External: Repititions", externalColor, KeyEvent.VK_W));
        model.add(AisOption.createSelectableCategory(external, "Ext3", "External: Other details", externalColor, KeyEvent.VK_E));
        model.add(AisOption.createSelectableCategory(external, "Ext4", "External: Episodic details", externalColor, KeyEvent.VK_R));
        model.add(AisOption.createSelectableCategory(external, "Ext5", "External: Generic events/routines", externalColor, KeyEvent.VK_T));
        return model;
    }

    /**
     * Create a selectable detail category with the given properties, converting the virtualKeyCode value into an actual KeyStroke.
     *
     * @param parent
     *            not-selectable parent detail category (can be <code>null</code> if the create category should have now parent)
     * @param code
     *            short detail name (max. five characters/symbols)
     * @param description
     *            optional description/title of the detail category
     * @param color
     *            the color to show over TextTokens with this assigned detail category
     * @param virtualKeyCode
     *            the virtual key (as found as KeyEvent constant <code>VK_xxx</code>) to use as short cut
     * @return the created detail category
     * @see #createDefaultCategoryModel()
     */
    private static DetailCategory createSelectableCategory(final DetailCategory parent, final String code, final String description,
            final Color color, final int virtualKeyCode) {
        return new DetailCategory(parent, code, description, true, color, KeyStroke.getKeyStroke(virtualKeyCode, 0, true));
    }

    /**
     * Set the default detail categories, to be applied to new projects.
     *
     * @param model
     *            detail categories to set
     * @see #provide()
     * @see #provideSelectables()
     */
    public void setDefaultDetailCategoryModel(final MutableDetailCategoryModel model) {
        this.defaultDetailCategoryModel = model;
    }

    /** Get the default detail categories (for new projects). */
    @Override
    public List<DetailCategory> provide() {
        return this.defaultDetailCategoryModel.provide();
    }

    /** Get the selectable (i.e. assignable to tokens as part of an interview scoring) default detail categories (for new projects). */
    @Override
    public List<DetailCategory> provideSelectables() {
        return this.defaultDetailCategoryModel.provideSelectables();
    }

    /** Save the default detail categories (for new projects) in the associated options file, in order to load them at the next application start. */
    public void persistChanges() {
        final File targetFile = new File(this.filePath);
        if (!targetFile.exists() || targetFile.canWrite()) {
            // create target file output stream
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(targetFile);
                final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                doc.appendChild(doc.createElement(AisOption.TAG_ROOT));
                // default categories
                doc.getDocumentElement().appendChild(this.modelParseService.parseXmlFromDetailCategories(doc, this.defaultDetailCategoryModel));
                // write the xml structure to the output stream
                TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(output));
            } catch (final IOException ioex) {
                // could not create/open the FileOutPutStream
                ioex.printStackTrace();
            } catch (final ParserConfigurationException pcex) {
                // could not create a newDocumentBuilder() for the xml structure
                pcex.printStackTrace();
            } catch (final TransformerFactoryConfigurationError tfcer) {
                // could not get a newInstance() from the TransformerFactory
                tfcer.printStackTrace();
            } catch (final TransformerException tex) {
                // could not create a newTransformer() or execute the actual transformation (i.e. file saving)
                tex.printStackTrace();
            } finally {
                // try to properly close the FileOutputStream anyway
                if (output != null) {
                    try {
                        output.close();
                    } catch (final IOException ioex) {
                        // at least we tried
                        ioex.printStackTrace();
                    }
                }
            }
        }
    }
}
