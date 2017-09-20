package org.tdar.core.service;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.xml.bind.JAXBException;

import org.tdar.core.bean.FileProxies;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.event.TdarEvent;
import org.tdar.core.service.event.LoggingObjectContainer;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.utils.jaxb.JaxbResultContainer;
import org.w3c.dom.Document;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface SerializationService {

    String RDF_KEYWORD_MEDIAN = "/rdf/keywordMedian";
    String RDF_KEYWORD_MEAN = "/rdf/keywordMean";
    String RDF_XML_ABBREV = "RDF/XML-ABBREV";
    String FOAF_XML = ".foaf.xml";
    String RDF_CREATOR_MEDIAN = "/rdf/creatorMedian";
    String RDF_CREATOR_MEAN = "/rdf/creatorMean";
    String RDF_COUNT = "/rdf/count";
    String INSTITUTION = "Institution";
    String XSD = ".xsd";
    String TDAR_SCHEMA = "tdar-schema";
    String S_BROWSE_CREATORS_S_RDF = "%s/browse/creators/%s/rdf";
    @SuppressWarnings("unchecked")
    Class<Class<?>>[] rootClasses = new Class[] { Resource.class, Creator.class, JaxbResultContainer.class, ResourceCollection.class,
            FileProxies.class, FileProxy.class };

    void handleFilestoreEvent(TdarEvent event);

    /**
     * Serializes the JAXB-XML representation of a @link Record to the tDAR @link Filestore
     * 
     * @param resource
     */
    <T extends Persistable> void logRecordXmlToFilestore(T resource);

    <T extends Persistable> void writeToFilestore(FilestoreObjectType filestoreObjectType, Long id, String xml);

    /**
     * Convert the existing object to an XML representation using JAXB
     * 
     * @param object
     * @return
     * @throws Exception
     */
    String convertToXML(Object object) throws Exception;

    /**
     * Generate the XSD schema for tDAR
     * 
     * @return
     * @throws IOException
     * @throws JAXBException
     */
    File generateSchema() throws IOException, JAXBException;

    /**
     * Convert an Object to XML via JAXB, but use the writer instead of a String (For writing directly to a file or Stream)
     * 
     * @param object
     * @param writer
     * @return
     * @throws Exception
     */
    Writer convertToXML(Object object_, Writer writer) throws Exception;

    /**
     * Convert an object to JSON using JAXB using writer
     * 
     * @param object
     * @param writer
     * @throws JsonProcessingException
     * @throws IOException
     */
    void convertToJson(Object object_, Writer writer, Class<?> view, String callback) throws IOException;

    <C> C readObjectFromJson(String json, Class<C> cls) throws IOException;

    <C> C readObjectFromJsonWithJAXB(String json, Class<C> cls) throws IOException;

    /**
     * Convert an object to JSON using JAXB to string
     * 
     * @param object
     * @return
     * @throws IOException
     */
    String convertToJson(Object object) throws IOException;

    /*
     * Takes an object, a @JsonView class (optional); and callback-name (optional); and constructs a JSON or JSONP object passing it back to the controller.
     * Most commonly used to produce a stream.
     */
    String convertFilteredJsonForStream(Object object, Class<?> view, String callback);

    String convertToFilteredJson(Object object, Class<?> view) throws IOException;

    /**
     * Convert an object to XML using JAXB, but populate a W3C XML Document
     * 
     * @param object
     * @param document
     * @return
     * @throws JAXBException
     */
    Document convertToXML(Object object_, Document document) throws JAXBException;

    /**
     * Parse an XML reader stream to a java bean
     * 
     * @param reader
     * @return
     * @throws Exception
     */
    Object parseXml(Reader reader) throws Exception;

    Object parseXml(Class<?> c, Reader reader) throws Exception;

    <C> void convertToXMLFragment(Class<C> cls, C object, Writer writer) throws JAXBException;

    String createGeoJsonFromResourceList(FeedSearchHelper helper) throws IOException;

    boolean isUseTransactionalEvents();

    void setUseTransactionalEvents(boolean useTransactionalEvents);

    void post(LoggingObjectContainer o) throws Exception;

}