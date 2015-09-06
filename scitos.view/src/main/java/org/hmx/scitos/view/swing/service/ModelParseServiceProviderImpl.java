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

package org.hmx.scitos.view.swing.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.hmx.scitos.core.ExportOption;
import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.core.IModelParseService;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.domain.IModel;
import org.hmx.scitos.view.FileType;
import org.hmx.scitos.view.service.IModelParseServiceProvider;
import org.hmx.scitos.view.service.IModelParseServiceRegistry;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Single point of contact for interactions with the outside world. Namely reading and writing files outside of the application's own scope.
 */
@Singleton
public final class ModelParseServiceProviderImpl implements IModelParseServiceRegistry, IModelParseServiceProvider {

    /** The registered model implementations matched to their representing file types. */
    private final Map<Class<? extends IModel<?>>, FileType> modelTypes;
    /** The registered model provider implementations for the supported file types. */
    private final Map<FileType, IModelParseService<?>> modelParseServices;

    /**
     * Main constructor.
     */
    @Inject
    public ModelParseServiceProviderImpl() {
        this.modelTypes = Collections.synchronizedMap(new HashMap<Class<? extends IModel<?>>, FileType>());
        this.modelParseServices = Collections.synchronizedMap(new HashMap<FileType, IModelParseService<?>>());
    }

    @Override
    public <M extends IModel<M>> void registerModelParseService(final FileType type, final Class<M> modelClass, final IModelParseService<M> service) {
        synchronized (this.modelTypes) {
            synchronized (this.modelParseServices) {
                this.modelTypes.put(modelClass, type);
                this.modelParseServices.put(type, service);
            }
        }
    }

    @Override
    public Class<? extends IModel<?>> getModelClassForFileType(final FileType type) {
        synchronized (this.modelTypes) {
            for (final Map.Entry<Class<? extends IModel<?>>, FileType> entry : this.modelTypes.entrySet()) {
                if (entry.getValue() == type) {
                    return entry.getKey();
                }
            }
        }
        throw new IllegalStateException("No model class registered for FileType " + type.name());
    }

    @Override
    public Entry<? extends IModel<?>, List<Object>> open(final File target) throws HmxException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final Document xml;
        try {
            // parse file into xml structure
            xml = factory.newDocumentBuilder().parse(target);
        } catch (final ParserConfigurationException pcex) {
            // error while creating a DocumentBuilder instance from factory or while accessing file
            throw new HmxException(Message.ERROR_UNKNOWN, pcex);
        } catch (final IOException ioex) {
            // error while creating a DocumentBuilder instance from factory or while accessing file
            throw new HmxException(Message.ERROR_UNKNOWN, ioex);
        } catch (final SAXException se) {
            // error while interpreting (invalid) xml structure
            throw new HmxException(Message.ERROR_FILE_INVALID, se);
        }
        // interpret represented file type
        final FileType type = FileType.fromXml(xml);
        synchronized (this.modelParseServices) {
            // check if a model provider is registered for that file type
            if (this.modelParseServices.containsKey(type)) {
                // parse model from file of recognized type
                return this.modelParseServices.get(type).parseModelFromXml(xml, target);
            }
        }
        // no valid file type declaration or no matching model provider registered
        throw new HmxException(Message.ERROR_FILE_TYPE_NOT_RECOGNIZED);
    }

    @Override
    public void save(final IModel<?> model, final List<?> openViewElements, final File target) throws HmxException {
        final Document xml = this.createXmlFromModel(model, openViewElements);
        // create target file output stream
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(target);
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            // make the file in its raw XML format human readable by adding indentations
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
            // write the xml structure to the output stream
            transformer.transform(new DOMSource(xml), new StreamResult(output));
            output.flush();
        } catch (final IOException ioe) {
            // error while initializing the FileOutputStream
            throw new HmxException(Message.ERROR_UNKNOWN, ioe);
        } catch (final TransformerFactoryConfigurationError tfce) {
            // error while instantiating the transformer factory
            throw new HmxException(Message.ERROR_UNKNOWN, tfce);
        } catch (final TransformerConfigurationException tce) {
            // error while initializing the transformer instance
            throw new HmxException(Message.ERROR_UNKNOWN, tce);
        } catch (final TransformerException te) {
            // error while transferring the xml document through the output stream
            throw new HmxException(Message.ERROR_UNKNOWN, te);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (final IOException ioex) {
                    // at least we tried
                }
            }
        }
    }

    @Override
    public void export(final IModel<?> model, final String stylesheetPath, final File target) throws HmxException {
        final Document xml = this.createXmlFromModel(model, Collections.emptyList());
        // create target file output stream
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(target);
            final StreamSource stylesheet = new StreamSource(model.getClass().getResourceAsStream(stylesheetPath));
            final Transformer transformer = TransformerFactory.newInstance().newTransformer(stylesheet);
            // write the xml structure to the output stream
            transformer.transform(new DOMSource(xml), new StreamResult(output));
            output.flush();
        } catch (final IOException ioe) {
            // error while initializing the FileOutputStream
            throw new HmxException(Message.ERROR_UNKNOWN, ioe);
        } catch (final TransformerFactoryConfigurationError tfce) {
            // error while instantiating the transformer factory
            throw new HmxException(Message.ERROR_UNKNOWN, tfce);
        } catch (final TransformerConfigurationException tce) {
            // error while initializing the transformer instance
            throw new HmxException(Message.ERROR_UNKNOWN, tce);
        } catch (final TransformerException te) {
            // error while transferring the xml document through the output stream
            throw new HmxException(Message.ERROR_UNKNOWN, te);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (final IOException ioex) {
                    // at least we tried
                }
            }
        }
    }

    /**
     * Create the xml structure to represent the given model object, including the specified list of open model elements.
     *
     * @param model
     *            the model object to represent as xml
     * @param openViewElements
     *            currently open elements from the given model object
     * @return created xml structure
     * @throws HmxException
     *             error while generating the xml structure
     */
    private Document createXmlFromModel(final IModel<?> model, final List<?> openViewElements) throws HmxException {
        // determine representing file type for model implementation
        final FileType type;
        synchronized (this.modelTypes) {
            type = this.modelTypes.get(model.getClass());
        }
        // get registered model provider for file type
        final IModelParseService<?> provider;
        synchronized (this.modelParseServices) {
            provider = this.modelParseServices.get(type);
        }
        // get xml structure for model instance from provider
        final Document xml = provider.parseXmlFromModel(model, openViewElements);
        // ensure the current file type is recognizably contained in xml structure
        type.applyToXml(xml);
        return xml;
    }

    @Override
    public List<ExportOption> getSupportedExports(final IModel<?> model) {
        // determine representing file type for model implementation
        final FileType type;
        synchronized (this.modelTypes) {
            type = this.modelTypes.get(model.getClass());
        }
        // get registered model provider for file type
        final IModelParseService<?> provider;
        synchronized (this.modelParseServices) {
            provider = this.modelParseServices.get(type);
        }
        return provider.getSupportedExports();
    }
}
