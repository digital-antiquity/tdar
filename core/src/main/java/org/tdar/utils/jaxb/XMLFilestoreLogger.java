package org.tdar.utils.jaxb;

import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.tools.ant.filters.StringInputStream;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.XmlLoggable;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.FilestoreLoggingException;
import org.tdar.core.service.ReflectionService;
import org.tdar.core.service.UrlService;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.Filestore.StorageMethod;
import org.tdar.filestore.FilestoreObjectType;
import org.w3c.dom.Document;

@Component
public class XMLFilestoreLogger implements Serializable {

    private static final Filestore FILESTORE = TdarConfiguration.getInstance().getFilestore();
    private static final long serialVersionUID = -5576504979196350682L;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private static final transient Logger staticLogger = LoggerFactory.getLogger(XMLFilestoreLogger.class);

    private static Class<?>[] jaxbClasses;
    private TdarConfiguration CONFIG = TdarConfiguration.getInstance();

    public XMLFilestoreLogger() throws ClassNotFoundException {
        jaxbClasses = ReflectionService.scanForAnnotation(XmlElement.class, XmlRootElement.class);
    }

    /**
     * Serializes the JAXB-XML representation of a @link Record to the tDAR @link Filestore
     * 
     * @param resource
     */
    @Transactional(readOnly = true)
    public <T extends Persistable> void logRecordXmlToFilestore(T resource) {
        if (!CONFIG.shouldLogToFilestore()) {
            return;
        }
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

    public static <T extends Persistable> void writeToFilestore(FilestoreObjectType filestoreObjectType, Long id, String xml) {
        @SuppressWarnings("deprecation")
        InformationResourceFileVersion version = new InformationResourceFileVersion();
        version.setFilename("record.xml");
        version.setExtension("xml");
        version.setFileVersionType(VersionType.RECORD);
        version.setInformationResourceId(id);
        try {
            StorageMethod rotate = StorageMethod.DATE;
            FILESTORE.storeAndRotate(filestoreObjectType, new StringInputStream(xml, "UTF-8"), version, rotate);
        } catch (Exception e) {
            staticLogger.error("something happend when converting record to XML:" + filestoreObjectType, e);
            throw new FilestoreLoggingException("serializationService.could_not_save");
        }
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
    public Writer convertToXML(Object object, Writer writer) throws Exception {
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
     * Convert an object to XML using JAXB, but populate a W3C XML Document
     * 
     * @param object
     * @param document
     * @return
     * @throws JAXBException
     */
    @Transactional(readOnly = true)
    public Document convertToXML(Object object, Document document) throws JAXBException {
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

}
