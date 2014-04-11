package org.tdar.web;

import java.io.IOException;

import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.service.SearchIndexService;
import org.xml.sax.SAXException;

public class SchemaWebITCase extends AbstractWebTestCase {

    @Autowired
    SearchIndexService indexService;

    @Test
    public void testSchemaGeneration() throws ConfigurationException, SAXException, IOException {
        gotoPage("/schema/current");
        assertTextPresentInCode("CHEROKEE");
        testValidXMLSchemaResponse(getPageCode());
    }

}
