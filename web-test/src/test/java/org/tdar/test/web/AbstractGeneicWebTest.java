package org.tdar.test.web;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.jaxp13.Validator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.TestConstants;
import org.tdar.core.service.SerializationService;
import org.tdar.utils.TestConfiguration;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.KeyDataPair;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public abstract class AbstractGeneicWebTest {

    protected final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_45);
    protected boolean skipHtmlValidation = false;
    public static List<String> errorPatterns = Arrays.asList("http error", "server error", "{0}", "{1}", "{2}", "{3}", "{4}", ".exception.", "caused by",
            "problems with this submission", "TDAR:500", "TDAR:404", "TDAR:509");

    private boolean validateViaNu = false;
    protected Page internalPage;
    protected HtmlPage htmlPage;
    static final TestConfiguration CONFIG = TestConfiguration.getInstance();
    public transient Logger logger = LoggerFactory.getLogger(getClass());

    protected Set<String> encodingErrorExclusions = new HashSet<>();
    @SuppressWarnings("serial")
    private Map<String, Pattern> encodingErrorPatterns = new LinkedHashMap<String, Pattern>() {
        {
            // note that braces are java regex meta instructions and must be escaped (oh and don't forget to escape the escape character... man I hate you java)
            put("possible html encoding inside json, open-brace", Pattern.compile("\\{&quot;"));
            put("possible html encoding inside json, close-brace", Pattern.compile("&quot;\\}"));
            put("possible html encoding inside json, quoted-key", Pattern.compile("\\{&quot;:"));
            put("double-encoded html tag", Pattern.compile("&lt;(.+?)&gt;"));
            put("double-encoded html attribute pair", Pattern.compile("\\w+\\s?=\\s?&quot;\\w+&quot;"));
        }
    };

    @Rule
    public TestName testName = new TestName();

    @Rule
    public TestWatcher failWatcher = new TestWatcher() {

        @Override
        protected void failed(Throwable e, Description description) {
            AbstractGeneicWebTest.this.onFail(e, description);
        }

    };

    public void onFail(Throwable e, Description description) {
        // FIXME: need to get this to fire *before* the @After method logs out. otherwise the pageCode will always be the tdar login screen.
        // logger.error("{} failed. server response below:\n\n {}", description.getDisplayName(), getPageCode());
    }

    @Before
    public void announceTestStarting() {
        String fmt = " ***   RUNNING TEST: {}.{}() ***";
        logger.info(fmt, getClass().getSimpleName(), testName.getMethodName());
    }

    @After
    public void announceTestOver() {
        String fmt = " *** COMPLETED TEST: {}.{}() ***";
        logger.info(fmt, getClass().getCanonicalName(), testName.getMethodName());
    }

    /*
     * override to test with different URL can use this to point at another
     * instance of tDAR instead of running "integration" tests.
     */
    public String getBaseUrl() {
        return CONFIG.getBaseUrl();
    }

    @Before
    public void prepare() {
        // FIXME: This is far less than ideal, but there's a problem with how
        // the MAC is handling memory
        // and appears to be 'leaking' with jwebunit and gooogle maps. Hence, we
        // need to disable javascript
        // testing on the mac :(
        // if (System.getProperty("os.name").toLowerCase().contains("mac os x"))
        initializeAndConfigureWebClient();
        // reset encoding error exclusions for each test
        encodingErrorExclusions = new HashSet<String>();
        // <generated> gets emitted by cglib methods in stacktrace, let's not consider it to be a double encoding error.
        encodingErrorExclusions.add("&lt;generated&gt;");
        skipHtmlValidation = false;
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
        webClient.setJavaScriptErrorListener(new JavaScriptErrorListenerImplementation());
        webClient.setCssErrorHandler(new CssErrorHandlerImplementation(getBaseUrl()));
    }

    protected void assertPageValidHtml() {
        if (skipHtmlValidation)
            return;
        if (internalPage.getWebResponse().getContentType().contains("json")) {
            try {
                JSONObject.fromObject(getPageCode());
            } catch (Exception e) {
                Assert.fail(String.format("%s : %s: %s", e.getMessage(), ExceptionUtils.getRootCauseStackTrace(e), getPageCode()));
            }
        }
        if (internalPage.getWebResponse().getContentType().toLowerCase().contains("html")) {
            HtmlValidator validator = new HtmlValidator();
            if (validateViaNu) {
                validator.validateHtmlViaNuValidator(internalPage);
            } else {
                validator.validateViaTidy(internalPage);
            }
        }
    }

    public void assertNoAccessibilityErrors() {
        Pattern p = Pattern.compile("<img([^>]+)");
        Matcher matcher = p.matcher(getPageCode());
        List<String> errors = new ArrayList<>();
        while (matcher.find()) {
            boolean missingAlt = false;
            boolean missingTitle = false;
            String group = matcher.group(1);
            if (!group.contains(" alt=") && !group.contains(" alt =")) {
                missingAlt = true;
            }
            if (!group.contains(" title=") && !group.contains(" title =")) {
                missingTitle = true;
            }
            if (missingAlt || missingTitle) {
                errors.add(String.format("%s -- missing alt(%s) title(%s)", group, missingAlt, missingTitle));
            }
        }
        if (CollectionUtils.isNotEmpty(errors)) {
            fail(StringUtils.join(errors.toArray()));
        }

    }

    public HtmlPage getHtmlPage() {
        assertTrue("page is not a HtmlPage", internalPage instanceof HtmlPage);
        return (HtmlPage) internalPage;
    }

    public HtmlAnchor findPageLink(String text) {
        try {
            return getHtmlPage().getAnchorByText(text);
        } catch (ElementNotFoundException exception) {
            fail(String.format("link with text [%s] not found on page %s", text, getPageCode()));
            return null;
        }

    }

    public String getCurrentUrlPath() {
        return internalPage.getUrl().getPath() + "?" + internalPage.getUrl().getQuery();
    }

    public void clickLinkWithText(String text) {
        clickLinkOnPage(text);
    }

    public void clickLinkByHref(String href) throws IOException {
        changePage(getHtmlPage().getAnchorByHref(href).click());
    }

    public void clickLinkOnPage(String text) {
        try {
            changePage(findPageLink(text).click());
        } catch (IOException e) {
            logger.warn("Couldn't click anchor {}", e);
        }
    }

    // click on element that ostensibly should trigger a request (links, submit inputs, submit guttons)
    public void clickElementWithId(String id) {
        HtmlElement el = (HtmlElement) getHtmlPage().getElementById(id);
        try {
            changePage(el.click());
        } catch (IOException e) {
            Assert.fail("click failed:" + e);
        }
    }

    public String getPageText() {
        if (internalPage instanceof HtmlPage) {
            return getHtmlPage().asText();
        }
        if (internalPage instanceof TextPage) {
            return ((TextPage) internalPage).getContent();
        }

        return internalPage.getWebResponse().getContentAsString();
    }

    public static String getBaseSecureUrl() {
        return CONFIG.getBaseSecureUrl();
    }
    //
    // public Page getPage(String localPath) {
    // try {
    // if (localPath.startsWith("http")) {
    // return webClient.getPage(localPath);
    // } else {
    // String url = pathToUrl(localPath);
    // return webClient.getPage(url);
    // }
    // } catch (Exception e) {
    // logger.error("couldn't find page at {}", localPath, e);
    // }
    // return null;
    // }
    //
    // public String pathToUrl(String localPath_) {
    // if(localPath_.startsWith("http")) {return localPath_;}
    // String localPath = localPath_;
    // String prefix = getBaseUrl();
    // try {
    // URL current = internalPage.getUrl();
    // prefix = String.format("%s://%s:%s", current.getProtocol(), current.getHost(), current.getPort());
    // logger.info("SETTING URL TO {}{}", prefix, localPath);
    // } catch (Exception e) {
    // logger.trace("{}", e);
    // }
    //
    // if (localPath.startsWith("//")) {
    // localPath = localPath.substring(1);
    // }
    //
    // if (prefix.endsWith("/")) {
    // while (localPath.startsWith("/")) {
    // localPath = localPath.substring(1);
    // }
    // }
    //
    // String url = prefix + localPath;
    // return url;
    // }
    //

    // TODO: implemnent this, if viable (not sure yet if it will be easy or crazy hard).
    /**
     * asserts whether current web page contains text that matches regex based on the provided reges. So, if format string is
     * 
     * "Hello %s, how are you doing this fine %s?"
     * 
     * becomes...
     * 
     * /.*Hello (.*?), how are you doing this fine (.*?)\?/i
     * 
     * @param formatString
     */
    public void assertContainsFormat(String formatString) {
        fail("not implemented");
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
     * Request a page similar to gotoPage(), sans html validaton
     * 
     * @param path
     * @return
     */
    public String gotoJson(String path) {
        int statusCode = gotoPageWithoutErrorCheck(path);
        assertThat(statusCode, not(anyOf(is(SC_INTERNAL_SERVER_ERROR), is(SC_BAD_REQUEST))));
        return internalPage.getWebResponse().getContentAsString();
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

    
    public String pathToUrl(final String localPath_) {
        String localPath = localPath_;
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
            logger.trace("found schema, using: {}", schemaFile);
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
            assertTrue("Schema (" + schema + ") is invalid! Error count: " + v.getSchemaErrors().size(), v.isSchemaValid());
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
                new File(TestConstants.TEST_SCHEMA_DIR, "mods3.3.xsd"));
        addSchemaToValidatorWithLocalFallback(v, "http://www.openarchives.org/OAI/2.0/oai-identifier.xsd",
                new File(TestConstants.TEST_SCHEMA_DIR, "oai-identifier.xsd"));

        try {
            final File schema = File.createTempFile(SerializationService.TDAR_SCHEMA, SerializationService.XSD);
            JAXBContext jc = JAXBContext.newInstance(SerializationService.rootClasses);

            // WRITE OUT SCHEMA
            jc.generateSchema(new SchemaOutputResolver() {

                @Override
                public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
                    return new StreamResult(schema);
                }
            });

            logger.debug("{}", schema);
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

    public String getPageCode() {
        String content = internalPage.getWebResponse().getContentAsString();
        return content;
    }

    public void assertTextPresent(String text) {
        assertTrue("looking for [" + text + "] in page:" + internalPage.getUrl() + "\n" + getPageText(), internalPage.getWebResponse().getContentAsString()
                .contains(text));
    }

    public void assertTextPresentIgnoreCase(String text) {
        assertTrue("looking for [" + text + "] in page:" + internalPage.getUrl() + "\n" + getPageText(),
                StringUtils.containsIgnoreCase(internalPage.getWebResponse().getContentAsString(), text));
    }

    public void assertTextNotPresentIgnoreCase(String text) {
        assertFalse("looking for [" + text + "] in page:" + internalPage.getUrl() + "\n" + getPageText(),
                StringUtils.containsIgnoreCase(internalPage.getWebResponse().getContentAsString(), text));
    }

    public void assertPageTitleEquals(String expectedTitle) {
        if (internalPage instanceof HtmlPage) {
            HtmlPage page = (HtmlPage) internalPage;
            assertEquals(expectedTitle.toLowerCase(), page.getTitleText().toLowerCase());
        } else {
            Assert.fail(String.format("was looking for <title>%s</title> but server response was not a valid html page", expectedTitle));
        }
    }

    public void assertPageTitleContains(String expectedTitle) {
        if (internalPage instanceof HtmlPage) {
            HtmlPage page = (HtmlPage) internalPage;
            assertTrue(page.getTitleText().toLowerCase().contains(expectedTitle.toLowerCase()));
        } else {
            Assert.fail(String.format("was looking for <title>%s</title> but server response was not a valid html page", expectedTitle));
        }
    }

    public void assertErrorsPresent() {
        assertTextPresent("the following problems with this submission");
    }

    public void assertTextNotPresent(String text) {
        String contents = "";
        if (internalPage instanceof HtmlPage) {
            HtmlPage page = (HtmlPage) internalPage;
            contents = page.asText();
        }
        if (internalPage instanceof UnexpectedPage) {
            UnexpectedPage page = (UnexpectedPage) internalPage;
            contents = page.getWebResponse().getContentAsString();
        }
        if (contents.contains(text)) {
            logger.trace("text {} found in {}", text, contents);
        }
        assertFalse("text should not be present [" + text + "] in page:" + internalPage.getUrl() + "\r\n" + getPageText(), contents.contains(text));
    }

    /**
     * Assert that the page is not an error page and does or contain any inline stacktraces
     */
    public void assertNoErrorTextPresent() {
        checkForFreemarkerExceptions();
        for (String err : errorPatterns) {
            assertTextNotPresentIgnoreCase(err);
        }
    }

    public void checkForFreemarkerExceptions() {
        assertTextNotPresent("Exception stack trace: " + getCurrentUrlPath() + ":" + getPageText()); // inline stacktrace (ftl compiles but dies partway through
                                                                                                     // rendering)
        assertTextNotPresentIgnoreCase("Exception " + getCurrentUrlPath() + ":" + getPageText()); // inline stacktrace (ftl compiles but dies partway through
                                                                                                  // rendering)
        assertFalse("page shouldn't contain action errors " + getCurrentUrlPath() + ":" + getPageText(), getPageCode().contains("class=\"action-error\""));
    }

    public void assertValidJson(String json) {
        try {
            JSONSerializer.toJSON(json);
        } catch (JSONException jex) {
            fail("Invalid json string: >>" + json + "<<");
        }
    }
    

    public NameValuePair nameValuePair(String name, String value) {
        return new NameValuePair(name, value);
    }

    protected NameValuePair nameValuePair(String name, File file) {
        // FIXME:is it safe to specify text/plain even when we know it isn't?? It happens to 'work' for these tests, not sure of potential side effects...
        return nameValuePair(name, file, "text/plain");
    }

    protected NameValuePair nameValuePair(String name, File file, String contentType) {
        //(name, file, contentType, "utf8");
        KeyDataPair keyDataPair = new KeyDataPair(name, file, file.getName(), contentType, "utf8");
        return keyDataPair;
    }


    /**
     * assert provided string is valid json and return a JSONObject, otherwise call fail()
     * 
     * @param json
     * @return
     */
    protected JSONObject toJson(String json) {
        assertValidJson(json);
        JSONObject jso = (JSONObject) JSONSerializer.toJSON(json);
        return jso;
    }

    public void assertCurrentUrlEquals(String url) {
        String msg = String.format("actual page: %s; assumed page: %s; status: %s", internalPage.getUrl(), url, internalPage.getWebResponse().getStatusCode());
        assertEquals(msg, internalPage.getUrl().toString(), url);
    }

    public void assertCurrentUrlContains(String url) {
        String msg = String.format("actual page: %s; assumed page should have in URL: %s; status: %s", internalPage.getUrl(), url, internalPage
                .getWebResponse().getStatusCode());
        assertTrue(msg, internalPage.getUrl().toString().contains(url));
    }

    public void assertNoEscapeIssues() {
        String html = getPageCode().toLowerCase();
        for (Map.Entry<String, Pattern> entry : encodingErrorPatterns.entrySet()) {
            Matcher matcher = entry.getValue().matcher(html);
            if (matcher.find()) {
                String msg = "encoding issue \"%s\" found at pos[%s,%s] : '%s'";
                int start = matcher.start() - 100;
                int end = matcher.end() + 100;
                int max = getPageCode().length();
                if (start < 0) {
                    start = 0;
                }
                if (end > max) {
                    end = max;
                }
                String matchAndContext = getPageCode().subSequence(start, end).toString();
                String exactMatch = getPageCode().subSequence(matcher.start(), matcher.end()).toString();

                if (!encodingErrorExclusions.contains(exactMatch)) {
                    logger.debug(getPageCode());
                    Assert.fail(String.format(msg, entry.getKey(), matcher.start(), matcher.end(), matchAndContext));
                }
            }
        }
    }

    @After
    public void cleanup() {
        webClient.close();
        ;
        webClient.getCookieManager().clearCookies();
        webClient.getCache().clear();
    }


    public void assertNotEquals(Object o1, Object o2) {
        assertFalse(Objects.equals(o1, o2));
    }

    /**
     * Returns a list of DomElement nodes (ignoring any nodes that are not DomElements) that match the specified CSS selector, using the
     * querySelector of the active window.
     *
     * @param selector a css selector
     * @return A list of matching elements. List is empty if no matches found.
     */
    public List<DomElement> querySelectorAll(String selector) {

        List<DomElement> elements = htmlPage.getDocumentElement().querySelectorAll(selector).stream()
                // only find nodes that are DomElements
                .filter(node -> node instanceof DomElement)

                // safely cast nodes to DomElement
                .map(node -> (DomElement) node)

                // collect results into a List
                .collect(Collectors.toList());
        return elements;
    }


}
