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

package org.hmx.scitos.hmx.core.option;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.core.option.OptionHandler;
import org.hmx.scitos.hmx.core.ModelParseServiceImpl;
import org.hmx.scitos.hmx.domain.ISemanticalRelationProvider;
import org.hmx.scitos.hmx.domain.model.RelationModel;
import org.hmx.scitos.hmx.domain.model.RelationTemplate;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Application level preferences handler for the HmX module's available semantical {@link RelationTemplate}s.
 */
@Singleton
public final class HmxRelationOption implements ISemanticalRelationProvider {

    /** The root XML tag in the persisted options file. */
    private static final String TAG_ROOT = "Options";

    /** The path (determined by convention), where the options file is persisted/saved to. */
    private final String filePath;
    /** The HmX module's ModelParseService implementation, for conversions between the XML and internal data structure. */
    private final ModelParseServiceImpl modelParseService;
    /** The semantical relation model being applied to new projects. */
    private RelationModel defaultRelationModel;

    /**
     * Main constructor.
     *
     * @param modelParseService
     *            the ModelParseService implementation, being used to convert between the XML and internal data structures
     */
    @Inject
    public HmxRelationOption(final ModelParseServiceImpl modelParseService) {
        this.modelParseService = modelParseService;
        // apply general option file naming convention
        this.filePath = OptionHandler.buildOptionFilePath(HmxRelationOption.class);
        // initialize default detail category model tree
        RelationModel model = null;
        // retrieve settings from persistent storage (i.e. file)
        final File targetFile = new File(this.filePath);
        if (targetFile.exists() && targetFile.canRead()) {
            try {
                final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(targetFile);
                model = this.modelParseService.parseRelationModelFromXml(doc);
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
                // the parsed XML structure did not yield a valid relation model
                ex.printStackTrace();
            }
        }
        if (model == null) {
            try {
                model = this.modelParseService.getSystemRelationModel();
            } catch (final HmxException ex) {
                // internal error when loading default model
                ex.printStackTrace();
            }
            if (model == null) {
                // set empty model to avoid NullPointer - we should never get into this position
                model = new RelationModel();
            }
        }
        // remember parsed or generated relation model to apply it to new projects
        this.setDefaultRelationModel(model);
    }

    /**
     * Set the default {@link RelationModel}, to be applied to new projects.
     *
     * @param model
     *            available semantical relations
     * @see #provideRelationTemplates()
     */
    public void setDefaultRelationModel(final RelationModel model) {
        this.defaultRelationModel = model;
    }

    @Override
    public List<List<RelationTemplate>> provideRelationTemplates() {
        return this.defaultRelationModel.provideRelationTemplates();
    }

    /**
     * Save the available semantical relations (for new projects) in the associated options file, in order to load them at the next application start.
     */
    public void persistChanges() {
        final File targetFile = new File(this.filePath);
        if (this.defaultRelationModel != null && (!targetFile.exists() || targetFile.canWrite())) {
            // create target file output stream
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(targetFile);
                final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                doc.appendChild(doc.createElement(HmxRelationOption.TAG_ROOT));
                // default categories
                doc.getDocumentElement().appendChild(this.modelParseService.parseXmlFromRelationModel(doc, this.defaultRelationModel));
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
