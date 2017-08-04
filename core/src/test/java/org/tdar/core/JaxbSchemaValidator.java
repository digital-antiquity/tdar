package org.tdar.core;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.jaxp13.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.TestConstants;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.SerializationService;

public class JaxbSchemaValidator {

    public transient Logger logger = LoggerFactory.getLogger(getClass());
    private SerializationService serializationService;
    private Validator v;

    public JaxbSchemaValidator(SerializationService serializationService) throws FileNotFoundException {
        this.serializationService = serializationService;
        setupSchemaMap();
        setupValidator();
        }
    
    public JaxbSchemaValidator()  {
        try {
        setupSchemaMap();
        setupValidator();
        } catch (FileNotFoundException nfn) {
            throw new TdarRecoverableRuntimeException(nfn);
        }
    }

    
    public void setupSchemaMap() throws FileNotFoundException {
        String base = TestConstants.TEST_ROOT_DIR + "schemaCache";
        schemaMap.put("http://www.loc.gov/standards/mods/v3/mods-3-3.xsd", TestConstants.getFile(base, "mods3.3.xsd"));
        schemaMap.put("http://www.openarchives.org/OAI/2.0/oai-identifier.xsd", TestConstants.getFile(base, "oai-identifier.xsd"));
        schemaMap.put("http://www.openarchives.org/OAI/2.0/oai_dc.xsd", TestConstants.getFile(base, "oaidc.xsd"));
        schemaMap.put("http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd", TestConstants.getFile(base, "oaipmh.xsd"));
        schemaMap.put("http://www.loc.gov/standards/xlink/xlink.xsd", TestConstants.getFile(base, "xlink.xsd"));
        schemaMap.put("http://www.w3.org/XML/2008/06/xlink.xsd", TestConstants.getFile(base, "xlink.xsd"));
        schemaMap.put("http://www.w3.org/2001/03/xml.xsd", TestConstants.getFile(base, "xml.xsd"));
        schemaMap.put("http://dublincore.org/schemas/xmls/simpledc20021212.xsd", TestConstants.getFile(base, "simpledc20021212.xsd"));
    }

    

    private Validator setupValidator() throws FileNotFoundException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        if (v != null) {
            return v;
        }
        v = new Validator(factory);
        // v.addSchemaSource(new StreamSource(schemaMap.get("http://www.loc.gov/standards/xlink/xlink.xsd")));
        // v.addSchemaSource(new StreamSource(schemaMap.get("http://www.w3.org/XML/2008/06/xlink.xsd")));
        // v.addSchemaSource(new StreamSource(schemaMap.get("http://www.w3.org/2001/03/xml.xsd")));
//        addSchemaToValidatorWithLocalFallback(v, "http://www.loc.gov/standards/xlink/xlink.xsd", TestConstants.getFile(TestConstants.TEST_SCHEMA_DIR , "xlink.xsd"));
        addSchemaToValidatorWithLocalFallback("http://dublincore.org/schemas/xmls/simpledc20021212.xsd", TestConstants.getFile(TestConstants.TEST_SCHEMA_DIR ,
                "simpledc20021212.xsd"));
        // not the "ideal" way to set these up, but it should work... caching the schema locally and injecting
        addSchemaToValidatorWithLocalFallback("http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd", TestConstants.getFile(TestConstants.TEST_SCHEMA_DIR ,
                "oaipmh.xsd"));
        addSchemaToValidatorWithLocalFallback("http://www.openarchives.org/OAI/2.0/oai_dc.xsd",
                TestConstants.getFile(TestConstants.TEST_SCHEMA_DIR , "oaidc.xsd"));
        addSchemaToValidatorWithLocalFallback("http://www.loc.gov/standards/mods/v3/mods-3-3.xsd", TestConstants.getFile(TestConstants.TEST_SCHEMA_DIR ,
                "mods3.3.xsd"));
        addSchemaToValidatorWithLocalFallback("http://www.openarchives.org/OAI/2.0/oai-identifier.xsd", TestConstants.getFile(TestConstants.TEST_SCHEMA_DIR ,
                "oai-identifier.xsd"));

        try {
            if (serializationService != null) {
            addSchemaToValidatorWithLocalFallback("http://localhost:8180/schema/current", serializationService.generateSchema());
            }
        } catch (Exception e) {
            logger.error("an error occured creating the schema", e);
            assertTrue(false);
        }
        return v;
    }
    

    private static Map<String, File> schemaMap = new HashMap<String, File>();

    public void addSchemaToValidatorWithLocalFallback(String url, File schemaFile) {
        File schema = null;
        if (schemaMap.containsKey(url)) {
            schema = schemaMap.get(url);
            logger.debug("using cache of: {}", url);
        } else {
            logger.debug("attempting to add schema to validation list: " + url);
            try {
                File tmpFile = File.createTempFile(schemaFile.getName(), ".temp.xsd");
                FileUtils.writeStringToFile(tmpFile, IOUtils.toString(new URI(url)));
                schema = tmpFile;
            } catch (Throwable e) {
                logger.debug("could not validate against remote schema, attempting to use cached fallback:" + schemaFile);
            }
            if (schema == null) {
                try {
                    schema = schemaFile;
                } catch (Exception e) {
                    logger.debug("could not validate against local schema");
                }
            } else {
                schemaMap.put(url, schema);
            }
        }
        if (schema != null) {
            v.addSchemaSource(new StreamSource(schema));
            for (Object err : v.getSchemaErrors()) {
                logger.error("*=> schema error: {} ", err.toString());
            }
            assertTrue("Schema is invalid! Error count: " + v.getSchemaErrors().size(), v.isSchemaValid());
        }
    }

    public void addSchemaSource(StreamSource streamSource) {
        v.addSchemaSource(streamSource);
    }

    public Validator getValidator() {
        return v;
    }

    public List<?> getInstanceErrors(StreamSource is) {
        return v.getInstanceErrors(is);
    }
}
