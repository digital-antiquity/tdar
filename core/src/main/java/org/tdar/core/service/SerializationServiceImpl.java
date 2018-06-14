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

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/*
 * class to help with marshalling and unmarshalling of resources
 */
@Service
public class SerializationServiceImpl implements TxMessageBus<LoggingObjectContainer>, SerializationService {

    private static final TdarConfiguration CONFIG = TdarConfiguration.getInstance();
    private boolean useTransactionalEvents = true;
    private Class<Class<?>>[] rootClasses = new Class[] { Resource.class, Creator.class, JaxbResultContainer.class, ResourceCollection.class, FileProxies.class, FileProxy.class };

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JaxbPersistableConverter persistableConverter;

    @Autowired
    private JaxbResourceCollectionRefConverter collectionRefConverter;
    private Class<?>[] jaxbClasses;

    public SerializationServiceImpl() throws ClassNotFoundException {
        jaxbClasses = ReflectionHelper.scanForAnnotation(XmlElement.class, XmlRootElement.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.SerializationService#handleFilestoreEvent(org.tdar.core.event.TdarEvent)
     */
    @Override
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
        File dir = new File(tempDirectory, "logging");
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.SerializationService#logRecordXmlToFilestore(T)
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.SerializationService#writeToFilestore(org.tdar.filestore.FilestoreObjectType, java.lang.Long, java.lang.String)
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.SerializationService#convertToXML(java.lang.Object)
     */
    @Override
    @Transactional(readOnly = true)
    public String convertToXML(Object object) throws Exception {
        StringWriter sw = new StringWriter();
        convertToXML(object, sw);
        return sw.toString();
    }

    private File schemaFile = null;

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.SerializationService#generateSchema()
     */
    @Override
    public File generateSchema() throws IOException, JAXBException {
        if (schemaFile != null) {
            return schemaFile;
        }
        final File tempFile = File.createTempFile(TDAR_SCHEMA, XSD, CONFIG.getTempDirectory());
        JAXBContext jc = JAXBContext.newInstance(rootClasses);

        // WRITE OUT SCHEMA
        jc.generateSchema(new SchemaOutputResolver() {

            @Override
            public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
                return new StreamResult(tempFile);
            }
        });
        schemaFile = tempFile;
        return schemaFile;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.SerializationService#convertToXML(java.lang.Object, java.io.Writer)
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.SerializationService#convertToJson(java.lang.Object, java.io.Writer, java.lang.Class, java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public void convertToJson(Object object_, Writer writer, Class<?> view, String callback) throws IOException {
        Object object = object_;
        ObjectMapper mapper = JacksonUtils.initializeObjectMapper();
        ObjectWriter objectWriter = JacksonUtils.initializeObjectWriter(mapper, view);
        object = wrapObjectIfNeeded(object, callback);
        objectWriter.writeValue(writer, object);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.SerializationService#readObjectFromJson(java.lang.String, java.lang.Class)
     */
    @Override
    @Transactional(readOnly = true)
    public <C> C readObjectFromJson(String json, Class<C> cls) throws IOException {
        ObjectMapper mapper = JacksonUtils.initializeObjectMapper();
        return mapper.readValue(json, cls);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.SerializationService#readObjectFromJsonWithJAXB(java.lang.String, java.lang.Class)
     */
    @Override
    @Transactional(readOnly = true)
    public <C> C readObjectFromJsonWithJAXB(String json, Class<C> cls) throws IOException {
        final AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        ObjectMapper mapper = JacksonUtils.initializeObjectMapper().setAnnotationIntrospector(jaxbIntrospector);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper.readValue(json, cls);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.SerializationService#convertToJson(java.lang.Object)
     */
    @Override
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
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.SerializationService#convertFilteredJsonForStream(java.lang.Object, java.lang.Class, java.lang.String)
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.SerializationService#convertToFilteredJson(java.lang.Object, java.lang.Class)
     */
    @Override
    @Transactional(readOnly = true)
    public String convertToFilteredJson(Object object, Class<?> view) throws IOException {
        StringWriter writer = new StringWriter();
        convertToJson(object, writer, view, null);
        return writer.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.SerializationService#convertToXML(java.lang.Object, org.w3c.dom.Document)
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.SerializationService#parseXml(java.io.Reader)
     */
    @Override
    public Object parseXml(Reader reader) throws Exception {
        return parseXml(null, reader);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.SerializationService#parseXml(java.lang.Class, java.io.Reader)
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.SerializationService#convertToXMLFragment(java.lang.Class, C, java.io.Writer)
     */
    @Override
    @Transactional(readOnly = true)
    public <C> void convertToXMLFragment(Class<C> cls, C object, Writer writer) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(cls);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        logger.trace("converting: {}", object);
        marshaller.marshal(object, writer);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.SerializationService#createGeoJsonFromResourceList(org.tdar.core.service.FeedSearchHelper)
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> createGeoJsonFromResourceList(FeedSearchHelper helper) throws IOException {
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
        // convertToJson(result, writer, helper.getJsonFilter(), helper.getJsonCallback());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.SerializationService#isUseTransactionalEvents()
     */
    @Override
    public boolean isUseTransactionalEvents() {
        return useTransactionalEvents;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.SerializationService#setUseTransactionalEvents(boolean)
     */
    @Override
    public void setUseTransactionalEvents(boolean useTransactionalEvents) {
        this.useTransactionalEvents = useTransactionalEvents;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.SerializationService#post(org.tdar.core.service.event.LoggingObjectContainer)
     */
    @Override
    public void post(LoggingObjectContainer o) throws Exception {
        logger.trace("event write to filestore: {}", o);
        writeToFilestore(o.getFilestoreObjectType(), o.getPersistableId(), new FileInputStream(o.getDoc()));
    }
}
