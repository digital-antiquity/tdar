package org.tdar.oai;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.jaxp13.Validator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdar.TestConstants;
import org.tdar.core.configuration.SimpleAppConfiguration;
import org.tdar.core.service.SerializationService;
import org.tdar.utils.TestConfiguration;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.InteractivePage;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;

@ContextConfiguration(classes = SimpleAppConfiguration.class)
public abstract class AbstractWebTest extends AbstractJUnit4SpringContextTests {

	protected final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_38);
	protected Page internalPage;
	protected HtmlPage htmlPage;
    static final TestConfiguration CONFIG = TestConfiguration.getInstance();
	transient Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	SerializationService serializationService;
	
    /*
     * override to test with different URL can use this to point at another
     * instance of tDAR instead of running "integration" tests.
     */
    public String getBaseUrl() {
        return CONFIG.getBaseUrl();
    }

    @Before
    public void prepare() {
        initializeAndConfigureWebClient();
    }

    private void initializeAndConfigureWebClient() {
        webClient.getOptions().setUseInsecureSSL(true);
        // webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getCurrentWindow().setInnerWidth(1024);
        // if you enable css, then we need to figure out how to deal with CSS Responsive issues (hidden-phone-portrait) on UPLOAD Button 
        webClient.getOptions().setCssEnabled(false);
        CookieManager cookieMan = new CookieManager();
        cookieMan = webClient.getCookieManager();
        cookieMan.setCookiesEnabled(true);
        webClient.getOptions().setTimeout(0);
        // webClient.getOptions().setSSLClientCertificate(certificateUrl, certificatePassword, certificateType)
        webClient.setJavaScriptTimeout(0);
        webClient.setJavaScriptErrorListener(new JavaScriptErrorListener() {

            @Override
            public void scriptException(InteractivePage page, ScriptException scriptException) {
                logger.error("JS load exception: {} {}", page.getUrl(), scriptException);
            }

            @Override
            public void timeoutError(InteractivePage page, long allowedTime, long executionTime) {
                logger.error("timeout exception: {} {}", page.getUrl(), allowedTime);
                
            }

            @Override
            public void malformedScriptURL(InteractivePage page, String url, MalformedURLException malformedURLException) {
                logger.error("malformed script URL exception: {} {}", page.getUrl(), malformedURLException);
                
            }

            @Override
            public void loadScriptError(InteractivePage page, URL scriptUrl, Exception exception) {
                logger.error("load script Error: {} {}", scriptUrl, exception);
                
            }
        });
        webClient.setCssErrorHandler(new ErrorHandler() {
            @Override
            public void warning(CSSParseException exception) throws CSSException {
                String uri = exception.getURI();
                if (uri.contains(getBaseUrl()) && uri.contains("/css/")) {
                    logger.trace("CSS Warning:", exception);
                }
            }

            @Override
            public void fatalError(CSSParseException exception) throws CSSException {
                String uri = exception.getURI();
                if (uri.contains(getBaseUrl()) && uri.contains("/css/")) {
                    logger.warn("CSS Fatal Error:", exception);
                }
            }

            @Override
            public void error(CSSParseException exception) throws CSSException {
                String uri = exception.getURI();
                if (uri.contains(getBaseUrl()) && uri.contains("tdar")) {
                    String msg = String.format("CSS Error: %s ; message: %s line: %s ", exception.getURI(), exception.getMessage(), exception.getLineNumber());
                    logger.error(msg);
                    fail(msg);
                }
            }
        });
    }
    
    /**
     * Go to the specified page, with explicit assertions that the server did not return with a 500 error or contain any inline exception text
     * 
     * @param path
     * @return http return code (if no errors found, otherwise assertions fail and method does not return);
     */
    public int gotoPage(String path) {
        int statusCode = gotoPageWithoutErrorCheck(path);
        assertThat(statusCode, not(anyOf(is(SC_INTERNAL_SERVER_ERROR), is(SC_BAD_REQUEST))));
        return statusCode;
    }
    

    /**
     * Same as gotoPage(), but does not perform any assertions on the server response
     * 
     * @param path
     * @return http return code
     */
    public int gotoPageWithoutErrorCheck(String path) {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        changePage(getPage(path));
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
        return internalPage.getWebResponse().getStatusCode();
    }


    public void changePage(Page page) {
        changePage(page, false);
    }

    public void changePage(Page page, boolean expectErrors) {
        if (page == null) {
            fail("changed to a null page for some reason");
            return;
        }
        internalPage = page;
        logger.info("CHANGING url TO: {}", internalPage.getUrl());
        if (internalPage instanceof HtmlPage) {
            htmlPage = (HtmlPage) internalPage;
        }
    }


    public String pathToUrl(String localPath) {
        String prefix = getBaseUrl();
        try {
            URL current = internalPage.getUrl();
            prefix = String.format("%s://%s:%s", current.getProtocol(), current.getHost(), current.getPort());
            logger.info("SETTING URL TO {}{}", prefix, localPath);
        } catch (Exception e) {
            logger.trace("{}", e);
        }

        if (localPath.startsWith("//")) {
            localPath = localPath.substring(1);
        }

        if (prefix.endsWith("/")) {
            while (localPath.startsWith("/")) {
                localPath = localPath.substring(1);
            }
        }

        String url = prefix + localPath;
        return url;
    }

    protected WebClient getWebClient() {
        return webClient;
    }

    public Page getPage(String localPath) {
        try {
            if (localPath.startsWith("http")) {
                return webClient.getPage(localPath);
            } else {
                String url = pathToUrl(localPath);
                return webClient.getPage(url);
            }
        } catch (Exception e) {
            logger.error("couldn't find page at {}", localPath, e);
        }
        return null;
    }

    public String getPageCode() {
        String content = internalPage.getWebResponse().getContentAsString();
        return content;
    }
    

    public void assertTextPresentInPage(String text) {
        assertTextPresentInPage(text, true);
    }

    public void assertTextPresentInPage(String text, boolean sensitive) {
        HtmlPage page = (HtmlPage) internalPage;
        if (sensitive) {
            assertTrue("looking for [" + text + "] in page:" + internalPage.getUrl() + "\n" + page.asText(), page.asText().contains(text));
        } else {
            assertTrue("looking for [" + text + "] in page:" + internalPage.getUrl() + "\n" + page.asText(),
                    page.asText().toLowerCase().contains(text.toLowerCase()));
        }
    }

    public void assertTextPresentInCode(String text) {
        assertTextPresentInCode(text, true);
    }

    public void assertTextPresentInCode(String text, boolean sensitive) {
        if (sensitive) {
            assertTrue("looking for [" + text + "] in page:" + internalPage.getUrl() + "\n" + getPageCode(), getPageCode().contains(text));
        } else {
            assertTrue("looking for [" + text + "] in page:" + internalPage.getUrl() + "\n" + getPageCode(), getPageCode().toLowerCase().contains(text));
        }
    }

    /**
     * Validate a response against an external schema
     * 
     * @param schemaLocation
     *            the URL of the schema to use to validate the document
     * @throws ConfigurationException
     * @throws SAXException
     */
    public void testValidXMLResponse(InputStream code, String schemaLocation) throws ConfigurationException, SAXException {
        testValidXML(code, schemaLocation, true);
    }

    private void testValidXML(InputStream code, String schema, boolean loadSchemas) {
        Validator v = setupValidator(loadSchemas);

        if (schema != null) {
            v.addSchemaSource(new StreamSource(schema));
        }
        InputStream rereadableStream = null;
        try {
            rereadableStream = new ByteArrayInputStream(IOUtils.toByteArray(code));
        } catch (Exception e) {
            logger.error("", e);
        }
        if (rereadableStream == null) {
            rereadableStream = code;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(rereadableStream));
        StreamSource is = new StreamSource(reader);
        List<?> errorList = v.getInstanceErrors(is);

        if (!errorList.isEmpty()) {
            StringBuffer errors = new StringBuffer();
            for (Object error : errorList) {
                errors.append(error.toString());
                errors.append(System.getProperty("line.separator"));
                logger.error(error.toString());
            }
            String content = "";
            try {
                rereadableStream.reset();
                content = IOUtils.toString(rereadableStream);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Assert.fail("Instance invalid: " + errors.toString() + " in:\n" + content);
        }
    }


    private static Map<String, File> schemaMap = new HashMap<String, File>();

    private void addSchemaToValidatorWithLocalFallback(Validator v, String url, File schemaFile) {
        File schema = null;
        
        if (schemaFile.exists()) {
        	schema = schemaFile;
        	schemaMap.put(url, schemaFile);
        	logger.trace("found schema, using: {}" , schemaFile);
        }
        
        if (schemaMap.containsKey(url)) {
            schema = schemaMap.get(url);
            logger.debug("using cache of: {}", url);
        } else {
            logger.debug("attempting to add schema to validation list: " + url);
            try {
                File tmpFile = File.createTempFile(schemaFile.getName(), ".temp.xsd");
                FileUtils.writeStringToFile(tmpFile, IOUtils.toString(new URI(url)));
                schema = tmpFile;
                schemaMap.put(url, schema);
            } catch (Throwable e) {
                logger.debug("could not validate against remote schema, attempting to use cached fallback:" + schemaFile);
            }
        }

        if (schema != null) {
            v.addSchemaSource(new StreamSource(schema));
            for (Object err : v.getSchemaErrors()) {
                logger.error("*=> schema error: {} ", err.toString());
            }
            assertTrue("Schema ("+schema +") is invalid! Error count: " + v.getSchemaErrors().size(), v.isSchemaValid());
        }
    }
    private static Validator v;

    private Validator setupValidator(boolean extra) {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        if (v != null) {
            return v;
        }
        v = new Validator(factory);
        // v.addSchemaSource(new StreamSource(schemaMap.get("http://www.loc.gov/standards/xlink/xlink.xsd")));
        // v.addSchemaSource(new StreamSource(schemaMap.get("http://www.w3.org/XML/2008/06/xlink.xsd")));
        // v.addSchemaSource(new StreamSource(schemaMap.get("http://www.w3.org/2001/03/xml.xsd")));
        addSchemaToValidatorWithLocalFallback(v, "http://www.loc.gov/standards/xlink/xlink.xsd", 
        		new File(TestConstants.TEST_SCHEMA_DIR, "xlink.xsd"));

        // not the "ideal" way to set these up, but it should work... caching the schema locally and injecting
        addSchemaToValidatorWithLocalFallback(v, "http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd",
        		new File(TestConstants.TEST_SCHEMA_DIR, "oaipmh.xsd"));
        addSchemaToValidatorWithLocalFallback(v, "http://www.openarchives.org/OAI/2.0/oai_dc.xsd",
                new File(TestConstants.TEST_XML_DIR, "oaidc.xsd"));
        addSchemaToValidatorWithLocalFallback(v, "http://www.loc.gov/standards/mods/v3/mods-3-3.xsd", 
        		new File(TestConstants.TEST_SCHEMA_DIR,"mods3.3.xsd"));
        addSchemaToValidatorWithLocalFallback(v, "http://www.openarchives.org/OAI/2.0/oai-identifier.xsd", 
        		new File(TestConstants.TEST_SCHEMA_DIR, "oai-identifier.xsd"));

        try {
        	logger.debug("{}",serializationService);
        	File schema = serializationService.generateSchema();
			logger.debug("{}",schema);
            addSchemaToValidatorWithLocalFallback(v, "http://localhost:8180/schema/current", schema);
        } catch (Exception e) {
            logger.error("an error occured creating the schema", e);
            assertTrue(false);
        }
        return v;
    }

    /**
     * Validate that a response is a valid XML schema
     * 
     * @throws ConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void testValidXMLSchemaResponse(String code) throws ConfigurationException, SAXException, IOException {
        Validator setupValidator = setupValidator(false);
        // cleanup -- this is lazy
        File tempFile = File.createTempFile("test-schema", "xsd");
        FileUtils.writeStringToFile(tempFile, code);
        addSchemaToValidatorWithLocalFallback(setupValidator, null, tempFile);
    }

}
