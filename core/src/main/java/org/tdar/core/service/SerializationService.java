package org.tdar.core.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.filters.StringInputStream;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.FileProxies;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.XmlLoggable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.event.TdarEvent;
import org.tdar.core.exception.FilestoreLoggingException;
import org.tdar.core.service.event.EventBusResourceHolder;
import org.tdar.core.service.event.EventBusUtils;
import org.tdar.core.service.event.LoggingObjectContainer;
import org.tdar.core.service.event.TxMessageBus;
import org.tdar.filestore.Filestore.StorageMethod;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.jaxb.JaxbParsingException;
import org.tdar.utils.jaxb.JaxbResultContainer;
import org.tdar.utils.jaxb.JaxbValidationEvent;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;
import org.tdar.utils.jaxb.converters.JaxbResourceCollectionRefConverter;
import org.tdar.utils.json.LatitudeLongitudeBoxWrapper;
import org.w3c.dom.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.util.JSONPObject;

/*
 * class to help with marshalling and unmarshalling of resources
 */
@Service
public class SerializationService implements TxMessageBus<LoggingObjectContainer> {

    private static final TdarConfiguration CONFIG = TdarConfiguration.getInstance();
    public static final String RDF_KEYWORD_MEDIAN = "/rdf/keywordMedian";
    public static final String RDF_KEYWORD_MEAN = "/rdf/keywordMean";
    public static final String RDF_XML_ABBREV = "RDF/XML-ABBREV";
    public static final String FOAF_XML = ".foaf.xml";
    public static final String RDF_CREATOR_MEDIAN = "/rdf/creatorMedian";
    public static final String RDF_CREATOR_MEAN = "/rdf/creatorMean";
    public static final String RDF_COUNT = "/rdf/count";
    public static final String INSTITUTION = "Institution";
    public static final String XSD = ".xsd";
    public static final String TDAR_SCHEMA = "tdar-schema";
    public static final String S_BROWSE_CREATORS_S_RDF = "%s/browse/creators/%s/rdf";

    private boolean useTransactionalEvents = true;

    @SuppressWarnings("unchecked")
    private static final Class<Class<?>>[] rootClasses = new Class[] { Resource.class, Creator.class, JaxbResultContainer.class, ResourceCollection.class,
            FileProxies.class, FileProxy.class };

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JaxbPersistableConverter persistableConverter;

    @Autowired
    private JaxbResourceCollectionRefConverter collectionRefConverter;
    private Class<?>[] jaxbClasses;

    public SerializationService() throws ClassNotFoundException {
        jaxbClasses = ReflectionService.scanForAnnotation(XmlElement.class, XmlRootElement.class);
    }

    @SuppressWarnings("unchecked")
    @EventListener
    public void handleFilestoreEvent(TdarEvent event) {
        if (!(event.getRecord() instanceof XmlLoggable)) {
            return;
        }

        XmlLoggable record = (XmlLoggable) event.getRecord();
        if (record instanceof Persistable && PersistableUtils.isNullOrTransient((Persistable) record)) {
            return;
        }
        String id = record.getClass().getSimpleName() + "-" + record.getId().toString();

        try {
            File file = writeToTempFile(id, record);
            LoggingObjectContainer container = new LoggingObjectContainer(file, id, event.getType(), FilestoreObjectType.fromClass(record.getClass()),
                    record.getId());
            if (!isUseTransactionalEvents() || !CONFIG.useTransactionalEvents()) {
                post(container);
                return;
            }

            @SuppressWarnings("rawtypes")
            Optional<EventBusResourceHolder> holder = EventBusUtils.getTransactionalResourceHolder(this);
            if (holder.isPresent()) {
                holder.get().addMessage(container);
            } else {
                post(container);
            }
        } catch (Throwable t) {
            logger.error("error processing XML Log: {}", t, t);
        }
    }

    private File writeToTempFile(String recordId, XmlLoggable record) throws InstantiationException, IllegalAccessException, Exception {
        File tempDirectory = CONFIG.getTempDirectory();
        File dir = new File(tempDirectory, "index");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File temp = new File(dir, String.format("%s-%s.xml", recordId, System.nanoTime()));
        temp.deleteOnExit();
        if (record instanceof HasStatus && ((HasStatus) record).getStatus() == Status.DELETED) {
            XmlLoggable newInstance = (XmlLoggable) record.getClass().newInstance();
            newInstance.setId(record.getId());
            ((HasStatus) newInstance).setStatus(Status.DELETED);
            convertToXML(newInstance, new FileWriter(temp));
        } else {
            convertToXML(record, new FileWriter(temp));
        }
        return temp;
    }

    /**
     * Serializes the JAXB-XML representation of a @link Record to the tDAR @link Filestore
     * 
     * @param resource
     */
    @Transactional(readOnly = true)
    public <T extends Persistable> void logRecordXmlToFilestore(T resource) {
        if (!(resource instanceof XmlLoggable)) {
            return;
        }
        logger.trace("serializing record to XML: [{}] {}", resource.getClass().getSimpleName().toUpperCase(), resource.getId());

        String xml;
        try {
            xml = convertToXML(resource);
        } catch (Exception e) {
            logger.error("something happend when converting record to XML:" + resource, e);
            throw new FilestoreLoggingException("serializationService.could_not_save");
        }

        writeToFilestore(FilestoreObjectType.fromClass(resource.getClass()), resource.getId(), xml);
        logger.trace("done saving");
    }

    public <T extends Persistable> void writeToFilestore(FilestoreObjectType filestoreObjectType, Long id, String xml) {
        StringInputStream content = new StringInputStream(xml, "UTF-8");
        writeToFilestore(filestoreObjectType, id, content);
    }

    private void writeToFilestore(FilestoreObjectType filestoreObjectType, Long id, InputStream content) {
        @SuppressWarnings("deprecation")
        InformationResourceFileVersion version = new InformationResourceFileVersion();
        version.setFilename("record.xml");
        version.setExtension("xml");
        version.setFileVersionType(VersionType.RECORD);
        version.setInformationResourceId(id);
        try {
            StorageMethod rotate = StorageMethod.DATE;
            CONFIG.getFilestore().storeAndRotate(filestoreObjectType, content, version, rotate);
        } catch (Exception e) {
            logger.error("something happend when converting record to XML:" + filestoreObjectType, e);
            throw new FilestoreLoggingException("serializationService.could_not_save");
        }
    }

    /**
     * Convert the existing object to an XML representation using JAXB
     * 
     * @param object
     * @return
     * @throws Exception
     */
    @Transactional(readOnly = true)
    public String convertToXML(Object object) throws Exception {
        StringWriter sw = new StringWriter();
        convertToXML(object, sw);
        return sw.toString();
    }

    /**
     * Generate the XSD schema for tDAR
     * 
     * @return
     * @throws IOException
     * @throws JAXBException
     */
    public File generateSchema() throws IOException, JAXBException {
        final File tempFile = File.createTempFile(TDAR_SCHEMA, XSD, CONFIG.getTempDirectory());
        JAXBContext jc = JAXBContext.newInstance(rootClasses);

        // WRITE OUT SCHEMA
        jc.generateSchema(new SchemaOutputResolver() {

            @Override
            public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
                return new StreamResult(tempFile);
            }
        });

        return tempFile;
    }

    /**
     * Convert an Object to XML via JAXB, but use the writer instead of a String (For writing directly to a file or Stream)
     * 
     * @param object
     * @param writer
     * @return
     * @throws Exception
     */
    @Transactional(readOnly = true)
    public Writer convertToXML(Object object_, Writer writer) throws Exception {
        Object object = object_;
        // get rid of proxies
        if (object == null) {
            return writer;
        }
        if (HibernateProxy.class.isAssignableFrom(object.getClass())) {
            object = ((HibernateProxy) object).getHibernateLazyInitializer().getImplementation();
        }

        JAXBContext jc = JAXBContext.newInstance(jaxbClasses);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, UrlService.getPairedSchemaUrl());
        // marshaller.setProperty(Marshaller.JAXB_, urlService.getSchemaUrl());
        logger.trace("converting: {}", object);
        marshaller.marshal(object, writer);
        return writer;
    }

    /**
     * Convert an object to JSON using JAXB using writer
     * 
     * @param object
     * @param writer
     * @throws JsonProcessingException
     * @throws IOException
     */
    @Transactional(readOnly = true)
    public void convertToJson(Object object_, Writer writer, Class<?> view, String callback) throws IOException {
        Object object = object_;
        ObjectMapper mapper = JacksonUtils.initializeObjectMapper();
        ObjectWriter objectWriter = JacksonUtils.initializeObjectWriter(mapper, view);
        object = wrapObjectIfNeeded(object, callback);
        objectWriter.writeValue(writer, object);
    }

    @Transactional(readOnly = true)
    public <C> C readObjectFromJson(String json, Class<C> cls) throws IOException {
        ObjectMapper mapper = JacksonUtils.initializeObjectMapper();
        return mapper.readValue(json, cls);
    }

    /**
     * Convert an object to JSON using JAXB to string
     * 
     * @param object
     * @return
     * @throws IOException
     */
    @Transactional
    public String convertToJson(Object object) throws IOException {
        logger.trace("converting object to json:{}", object);
        StringWriter writer = new StringWriter();
        convertToJson(object, writer, null, null);
        return writer.toString();
    }

    /*
     * Takes an object, a @JsonView class (optional); and callback-name (optional); and constructs a JSON or JSONP object passing it back to the controller.
     * Most commonly used to produce a stream.
     */
    @Transactional(readOnly = true)
    public String convertFilteredJsonForStream(Object object, Class<?> view, String callback) {
        Object wrapper = wrapObjectIfNeeded(object, callback);
        String result = null;
        try {
            result = convertToFilteredJson(wrapper, view);
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            wrapper = wrapObjectIfNeeded(error, callback);
            try {
                result = convertToJson(wrapper);
            } catch (IOException e1) {
            }
        } finally {
            if (result == null) {
                result = "{error:'unknown'}";
            }
        }

        return result;

    }

    private Object wrapObjectIfNeeded(Object object, String callback) {
        Object wrapper = object;
        if (StringUtils.isNotBlank(callback)) {
            wrapper = new JSONPObject(callback, object);
        }
        return wrapper;
    }

    @Transactional(readOnly = true)
    public String convertToFilteredJson(Object object, Class<?> view) throws IOException {
        StringWriter writer = new StringWriter();
        convertToJson(object, writer, view, null);
        return writer.toString();
    }

    /**
     * Convert an object to XML using JAXB, but populate a W3C XML Document
     * 
     * @param object
     * @param document
     * @return
     * @throws JAXBException
     */
    @Transactional(readOnly = true)
    public Document convertToXML(Object object_, Document document) throws JAXBException {
        Object object = object_;
        // http://marlonpatrick.info/blog/2012/07/12/jaxb-plus-hibernate-plus-javassist/
        if (HibernateProxy.class.isAssignableFrom(object.getClass())) {
            object = ((HibernateProxy) object).getHibernateLazyInitializer().getImplementation();
        }
        JAXBContext jc = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jc.createMarshaller();
        marshaller.marshal(object, document);

        return document;
    }

    /**
     * Parse an XML reader stream to a java bean
     * 
     * @param reader
     * @return
     * @throws Exception
     */
    public Object parseXml(Reader reader) throws Exception {
        return parseXml(null, reader);
    }

    public Object parseXml(Class<?> c, Reader reader) throws Exception {
        JAXBContext jc = JAXBContext.newInstance(rootClasses);
        if (c != null) {
            jc = JAXBContext.newInstance(c);
        }
        final List<String> lines = IOUtils.readLines(reader);
        IOUtils.closeQuietly(reader);
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(generateSchema());

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        unmarshaller.setSchema(schema);
        unmarshaller.setAdapter(JaxbResourceCollectionRefConverter.class, collectionRefConverter);
        unmarshaller.setAdapter(JaxbPersistableConverter.class, persistableConverter);

        final List<String> errors = new ArrayList<>();
        unmarshaller.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                // TODO Auto-generated method stub
                JaxbValidationEvent err = new JaxbValidationEvent(event, lines.get(event.getLocator().getLineNumber() - 1));
                errors.add(err.toString());
                logger.warn("an XML parsing exception occurred: {}", err);
                return true;
            }
        });

        // separate out so that we can throw the exception
        Object toReturn = unmarshaller.unmarshal(new StringReader(StringUtils.join(lines, "\r\n")));// , new DefaultHandler());

        if (errors.size() > 0) {
            throw new JaxbParsingException(MessageHelper.getMessage("serializationService.could_not_parse"), errors);
        }

        return toReturn;
    }

    @Transactional(readOnly = true)
    public <C> void convertToXMLFragment(Class<C> cls, C object, Writer writer) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(cls);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        logger.trace("converting: {}", object);
        marshaller.marshal(object, writer);

    }

    @Transactional(readOnly = true)
    public String createGeoJsonFromResourceList(FeedSearchHelper helper) throws IOException {
        List<LatitudeLongitudeBoxWrapper> wrappers = new ArrayList<>();
        for (Object obj : helper.getResults()) {
            if (obj instanceof Resource) {
                wrappers.add(new LatitudeLongitudeBoxWrapper((Resource) obj, helper));
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("type", "FeatureCollection");
        result.put("features", wrappers);
        result.put("properties", helper.getSearchParams());
        StringWriter writer = new StringWriter();
        logger.debug("filter: {}", helper.getJsonFilter());
        convertToJson(result, writer, helper.getJsonFilter(), helper.getJsonCallback());
        return writer.toString();
    }

    public boolean isUseTransactionalEvents() {
        return useTransactionalEvents;
    }

    public void setUseTransactionalEvents(boolean useTransactionalEvents) {
        this.useTransactionalEvents = useTransactionalEvents;
    }

    @Override
    public void post(LoggingObjectContainer o) throws Exception {
        writeToFilestore(o.getFilestoreObjectType(), o.getPersistableId(), new FileInputStream(o.getDoc()));
    }
}
