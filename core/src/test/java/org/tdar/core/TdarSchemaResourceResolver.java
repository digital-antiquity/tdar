package org.tdar.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.TestConstants;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public class TdarSchemaResourceResolver implements LSResourceResolver {
    public transient Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        String base = TestConstants.TEST_ROOT_DIR + "schemaCache";
        String part = StringUtils.substringAfterLast(systemId, "/");
        logger.trace("{}/{}", publicId, systemId);
        logger.trace("{}/{}", part, namespaceURI);
        if (StringUtils.isBlank(part)) {
            logger.trace("returning null");
            return null;
        }
        try {
            return new Input(publicId, systemId, new FileInputStream(TestConstants.getFile(base, part)));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}