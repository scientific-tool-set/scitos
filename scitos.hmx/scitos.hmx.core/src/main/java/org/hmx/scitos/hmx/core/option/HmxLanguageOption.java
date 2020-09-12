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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import org.hmx.scitos.hmx.core.ILanguageModelProvider;
import org.hmx.scitos.hmx.core.ModelParseServiceImpl;
import org.hmx.scitos.hmx.domain.model.LanguageModel;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Application level preferences handler for the HmX module's {@link LanguageModel}s.
 */
@Singleton
public final class HmxLanguageOption implements ILanguageModelProvider, ILanguageOptionHandler {

    /** The root XML tag in the persisted options file. */
    private static final String TAG_ROOT = "Options";

    /** The path (determined by convention), where the options file is persisted/saved to. */
    private final String filePath;
    /** The HmX module's ModelParseService implementation, for conversions between the XML and internal data structure. */
    private final ModelParseServiceImpl modelParseService;
    /** The system default language models available for new projects. */
    private final List<LanguageModel> systemModels;
    /** The user defined language models available for new projects. */
    private final List<LanguageModel> userModels;

    /**
     * Main constructor.
     *
     * @param modelParseService
     *            the ModelParseService implementation, being used to convert between the XML and internal data structures
     */
    @Inject
    public HmxLanguageOption(final ModelParseServiceImpl modelParseService) {
        this.modelParseService = modelParseService;
        List<LanguageModel> systemDefinedModels = Collections.emptyList();
        try {
            systemDefinedModels = this.modelParseService.getSystemLanguageModels();
        } catch (final HmxException ex) {
            // some internal error when loading the system defined language models
            ex.printStackTrace();
        }
        this.systemModels = systemDefinedModels;
        // apply general option file naming convention
        this.filePath = OptionHandler.buildOptionFilePath(HmxLanguageOption.class);
        // initialize user defined language models
        this.userModels = new LinkedList<>();
        // retrieve settings from persistent storage (i.e. file)
        final File targetFile = new File(this.filePath);
        if (targetFile.exists() && targetFile.canRead()) {
            try {
                final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(targetFile);
                this.userModels.addAll(this.modelParseService.parseLanguageModelsFromXml(doc));
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
    }

    @Override
    public List<LanguageModel> getSystemModels() {
        return Collections.unmodifiableList(this.systemModels);
    }

    @Override
    public List<LanguageModel> getUserModels() {
        return Collections.unmodifiableList(this.userModels);
    }

    /**
     * Setter for the user defined {@link LanguageModel}s.
     *
     * @param userModels
     *            the modified user models to set
     */
    public void setUserModels(final List<LanguageModel> userModels) {
        this.userModels.clear();
        if (userModels != null) {
            this.userModels.addAll(userModels);
        }
    }

    @Override
    public Map<String, LanguageModel> provideLanguageModels() {
        // avoid duplicates (two models with same name) and sort the models lexicographically by their names
        final Map<String, LanguageModel> models = new TreeMap<>();
        for (final LanguageModel singleModel : this.systemModels) {
            models.put(singleModel.getName(), singleModel);
        }
        // if there is an overlap, the user defined models replace the system defaults
        for (final LanguageModel singleModel : this.userModels) {
            models.put(singleModel.getName(), singleModel);
        }
        return models;
    }

    /**
     * Save the user defined {@link LanguageModel}s (available for new projects) in the associated options file, in order to load them at the next
     * application start.
     */
    public void persistChanges() {
        final File targetFile = new File(this.filePath);
        if (!targetFile.exists() || targetFile.canWrite()) {
            // create target file output stream
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(targetFile);
                final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                doc.appendChild(doc.createElement(HmxLanguageOption.TAG_ROOT));
                // user defined language models
                for (final LanguageModel singleModel : this.userModels) {
                    doc.getDocumentElement().appendChild(this.modelParseService.parseXmlFromLanguageModel(doc, singleModel));
                }
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
