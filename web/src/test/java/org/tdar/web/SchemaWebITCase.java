package org.tdar.web;

import java.io.IOException;

import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.junit.Test;
import org.xml.sax.SAXException;

public class SchemaWebITCase extends AbstractWebTestCase {

    @Test
    public void testSchemaGeneration() throws ConfigurationException, SAXException, IOException {
        gotoPage("/schema/current");
        assertTextPresentInCode("CHEROKEE");
        testValidXMLSchemaResponse(getPageCode());
    }

}
