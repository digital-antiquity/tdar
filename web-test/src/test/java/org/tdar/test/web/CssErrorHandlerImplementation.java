package org.tdar.test.web;

import static org.junit.Assert.fail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

public class CssErrorHandlerImplementation implements ErrorHandler {
    private String baseUrl;

    transient Logger logger = LoggerFactory.getLogger(getClass());

    public CssErrorHandlerImplementation(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public void warning(CSSParseException exception) throws CSSException {
        String uri = exception.getURI();
        if (uri.contains(baseUrl) && uri.contains("/css/")) {
            logger.trace("CSS Warning:", exception);
        }
    }

    @Override
    public void fatalError(CSSParseException exception) throws CSSException {
        String uri = exception.getURI();
        if (uri.contains(baseUrl) && uri.contains("/css/")) {
            logger.warn("CSS Fatal Error:", exception);
        }
    }

    @Override
    public void error(CSSParseException exception) throws CSSException {
        String uri = exception.getURI();
        if (uri.contains(baseUrl) && uri.contains("tdar")) {
            String msg = String.format("CSS Error: %s ; message: %s line: %s ", exception.getURI(), exception.getMessage(), exception.getLineNumber());
            logger.error(msg);
            fail(msg);
        }
    }
}
