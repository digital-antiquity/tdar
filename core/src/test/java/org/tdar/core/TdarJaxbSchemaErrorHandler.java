package org.tdar.core;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class TdarJaxbSchemaErrorHandler implements ErrorHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

        private List<Exception> errors;

        public TdarJaxbSchemaErrorHandler(List<Exception> errors) {
            this.errors = errors;
        }
        
        @Override
        public void warning(SAXParseException exception) throws SAXException {
            logger.warn("systemId:{}; publicId:{}; {}",exception.getSystemId(), exception.getPublicId(), exception.getMessage());
            errors.add(exception);
        }
        
        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            logger.error("FATAL: systemId:{}; publicId:{}; {}",exception.getSystemId(), exception.getPublicId(), exception.getMessage());
            errors.add(exception);
            
        }
        
        @Override
        public void error(SAXParseException exception) throws SAXException {
            logger.error("systemId:{}; publicId:{}; {}",exception.getSystemId(), exception.getPublicId(), exception.getMessage());
            errors.add(exception);
            
        }

}
