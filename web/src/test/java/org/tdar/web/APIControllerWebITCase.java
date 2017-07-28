package org.tdar.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.Rollback;
import org.tdar.MultipleWebTdarConfigurationRunner;
import org.tdar.TestConstants;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.utils.APIClient;
import org.tdar.utils.ApiClientResponse;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.SimpleHttpUtils;
import org.tdar.utils.TestConfiguration;
import org.tdar.utils.jaxb.JaxbParsingException;
import org.tdar.utils.jaxb.JaxbResultContainer;
import org.tdar.utils.jaxb.JaxbValidationEvent;

import com.sun.media.rtsp.protocol.StatusCode;

@RunWith(MultipleWebTdarConfigurationRunner.class)
// @ContextConfiguration(classes = TdarAppConfiguration.class)
public class APIControllerWebITCase extends AbstractWebTestCase {

    private static final TestConfiguration CONFIG = TestConfiguration.getInstance();
    private static Logger logger = LoggerFactory.getLogger(SimpleHttpUtils.class);
    private APIClient apiClient;

    @Before
    public void setupAPIClient() {
        apiClient = new APIClient(CONFIG.getBaseSecureUrl(), 500);
    }

    @Test
    public void testValidLogin() throws IllegalStateException, Exception {
        setupValidLogin();
        ApiClientResponse response = apiClient.apiLogout();
        Assert.assertTrue(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_MOVED_TEMPORARILY)
                .contains(response.getStatusLine().getStatusCode()));

    }

    private JaxbResultContainer setupValidLogin() {
        try {
            ApiClientResponse response = apiClient.apiLogin(CONFIG.getAdminUsername(), CONFIG.getAdminPassword());
            logger.debug("status {}", response.getStatusLine());
            assertEquals(StatusCode.OK, response.getStatusCode());
            JaxbResultContainer result = (JaxbResultContainer) parseResult(response.getBody());
            assertNotNull(result.getApiToken());
            assertEquals(TdarConfiguration.getInstance().getRequestTokenName(), result.getSessionKeyName());
            assertNotNull(result.getUsername());
            return result;
        } catch (Exception e) {
            logger.debug("exception", e);
        }
        return null;
    }

    @Test
    public void testValidRequestWithoutCookie() throws IllegalStateException, Exception {
        JaxbResultContainer login = setupValidLogin();
        CloseableHttpClient client2 = SimpleHttpUtils.createClient();
        HttpGet get = new HttpGet(CONFIG.getBaseSecureUrl() + "/api/view?id=4231");
        CloseableHttpResponse execute = client2.execute(get);
        int statusCode = execute.getStatusLine().getStatusCode();
        logger.debug("status:{}", statusCode);
        assertEquals(HttpStatus.SC_FORBIDDEN, statusCode);
        logger.debug(IOUtils.toString(execute.getEntity().getContent()));
        get = new HttpGet(String.format("%s/api/view?id=4231&%s=%s", CONFIG.getBaseSecureUrl(),
                login.getSessionKeyName(), login.getApiToken()));
        execute = client2.execute(get);
        logger.debug("status:{}", statusCode);
        statusCode = execute.getStatusLine().getStatusCode();
        assertEquals(HttpStatus.SC_OK, statusCode);
        logger.debug(IOUtils.toString(execute.getEntity().getContent()));
        apiClient.apiLogout();
    }

    @Test
    @Rollback
    public void testCreate() throws Exception {
        setupValidLogin();
        String docXml = FileUtils.readFileToString(new File(TestConstants.TEST_ROOT_DIR + "/xml/newDocument.xml"));
        ApiClientResponse response = apiClient.uploadRecord(docXml, null, null);
        logger.debug("status:{} ", response.getStatusLine());
        logger.debug("response: {}", response.getBody());
        assertEquals(StatusCode.CREATED, response.getStatusLine().getStatusCode());
    }

    @Test
    @Rollback
    public void testHiddenCollection() throws Exception {
        setupValidLogin();
        String docXml = FileUtils.readFileToString(new File(TestConstants.TEST_ROOT_DIR + "/xml/hidden-collection.xml"));
        ApiClientResponse response = apiClient.uploadRecord(docXml, null, null);
        logger.debug("status:{} ", response.getStatusLine());
        logger.debug("response: {}", response.getBody());
        assertEquals(StatusCode.CREATED, response.getStatusLine().getStatusCode());
        Long id = response.getTdarId();
        ApiClientResponse viewRecord = apiClient.viewRecord(id);
        int statusCode = viewRecord.getStatusLine().getStatusCode();
        logger.debug("status:{}", statusCode);
        logger.debug(viewRecord.getBody());
        String rcid = StringUtils.substringAfter(viewRecord.getBody(), "tdar:jaxbPersistableRef id=\"");
        logger.debug(rcid);
        rcid = StringUtils.substringBefore(rcid, "\"");
        apiClient = null;
        setupAPIClient();
        setupValidLogin();
        ApiClientResponse viewCollection = apiClient.viewCollection(Long.parseLong(rcid));
        statusCode = viewCollection.getStatusLine().getStatusCode();
        logger.debug("status:{}", statusCode);
        logger.debug(viewCollection.getBody());
        String decl = viewCollection.getBody();
        decl = StringUtils.substringBetween(decl, "<tdar:collectionResult", ">");
        logger.debug(decl);
        assertTrue(StringUtils.contains(decl, " hidden=\"true\""));

    }

    @Test
    @Rollback
    public void testInvalid() throws Exception {
        setupValidLogin();
        String docXml = FileUtils.readFileToString(new File(TestConstants.TEST_ROOT_DIR + "/xml/invalid.xml"));
        ApiClientResponse response = apiClient.uploadRecord(docXml, null, null);
        logger.debug("status:{} ", response.getStatusLine());
        logger.debug("response: {}", response.getBody());
        assertNotEquals(StatusCode.CREATED, response.getStatusLine().getStatusCode());
        assertTrue(response.getBody().contains("using unsupported controlled keyword"));
    }

    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.CREDIT_CARD })
    public void testConfidential() throws Exception {
        setupValidLogin();
        String filesUed = getFilesUsed(true);
        logger.debug("used: {}", filesUed);

        String text = FileUtils.readFileToString(new File(TestConstants.TEST_ROOT_DIR + "/xml/confidentialImage.xml"));
        ApiClientResponse response = apiClient.uploadRecord(text, null, 1L, new File(TestConstants.TEST_IMAGE),
                new File(TestConstants.TEST_IMAGE2));

        logger.debug("status:{} ", response.getStatusLine());
        logger.debug("response: {}", response.getBody());
        assertEquals(StatusCode.CREATED, response.getStatusLine().getStatusCode());

        String filesUed_ = getFilesUsed(false);
        logger.debug("used: {} vs. {}", filesUed, filesUed_);
    }

    @Test
    public void testReplaceFile() throws Exception {
        setupValidLogin();

        String text = FileUtils.readFileToString(new File(TestConstants.TEST_ROOT_DIR + "/xml/confidentialImage.xml"));
        ApiClientResponse response = apiClient.uploadRecord(text, null, null, new File(TestConstants.TEST_IMAGE));
        logger.debug("status:{} ", response.getStatusLine());
        String resp = response.getBody();
        logger.debug("response: {}", resp);
        Long id = response.getTdarId();
        assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());

        ApiClientResponse viewRecord = apiClient.viewRecord(id);
        String fileId = viewRecord.getXmlDocument().getElementsByTagName("tdar:informationResourceFile").item(0)
                .getAttributes().getNamedItem("id").getNodeValue();
        logger.debug("fileId::{}", fileId);
        text = FileUtils.readFileToString(new File(TestConstants.TEST_ROOT_DIR + "/xml/replaceFileProxy.xml"));
        text = text.replace("{{FILE_ID}}", fileId);
        text = text.replace("{{FILENAME}}", TestConstants.TEST_IMAGE_NAME2);
        logger.debug(text);
        response = apiClient.updateFiles(text, id, null, new File(TestConstants.TEST_IMAGE2));

        logger.debug("status:{} ", response.getStatusLine());
        assertEquals(HttpStatus.SC_ACCEPTED, response.getStatusLine().getStatusCode());
        logger.debug("response: {}", response.getBody());
    }

    private String getFilesUsed(boolean login) {
        if (login) {
            login(CONFIG.getAdminUsername(), CONFIG.getAdminPassword());
        }
        gotoPage("/billing/1");
        String code = getPageCode();
        String str = "class=\"filesused\">";
        String filesUed = code.substring(code.indexOf(str) + str.length());
        filesUed = filesUed.substring(0, filesUed.indexOf("</"));
        return filesUed;
    }

    /**
     *  at java.lang.Thread.run(Thread.java:745) [?:1.8.0_25]
DEBUG 2017-07-24 09:42:55,522 106019 [qtp90647349-57 []] (ActivityLoggingInterceptor.java:81) org.tdar.struts_base.interceptor.ActivityLoggingInterceptor - e» 948 ms | a: 807 ms; r: 141 ms
DEBUG 2017-07-24 09:42:55,523 5812 [main []] (APIControllerWebITCase.java:226) org.tdar.utils.SimpleHttpUtils - status:HTTP/1.1 400 Bad Request 
DEBUG 2017-07-24 09:42:55,523 5812 [main []] (APIControllerWebITCase.java:227) org.tdar.utils.SimpleHttpUtils - response: <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<tdar:resultContainer xsi:schemaLocation="https://localhost:8143/schema/current schema.xsd" xmlns:tdar="http://www.tdar.org/namespace" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <tdar:errors>[FATAL ERROR] cvc-complex-type.2.4.a: Invalid content was found starting with element 'tdar:name'. One of '{"http://www.tdar.org/namespace":secondarySortBy, "http://www.tdar.org/namespace":sortBy, "http://www.tdar.org/namespace":status, "http://www.tdar.org/namespace":type, "http://www.tdar.org/namespace":alternateParentIds, "http://www.tdar.org/namespace":alternateParent}' is expected. line: 28 column: 24  {             &lt;tdar:name&gt;1234&lt;/tdar:name&gt; } cvc-complex-type.2.4.a: Invalid content was found starting with element 'tdar:name'. One of '{"http://www.tdar.org/namespace":secondarySortBy, "http://www.tdar.org/namespace":sortBy, "http://www.tdar.org/namespace":status, "http://www.tdar.org/namespace":type, "http://www.tdar.org/namespace":alternateParentIds, "http://www.tdar.org/namespace":alternateParent}' is expected. </tdar:errors>
    <tdar:message></tdar:message>
    <tdar:statusCode>400</tdar:statusCode>
    <tdar:status>HTTP 400 BAD REQUEST</tdar:status>
</tdar:resultContainer>
     * @throws Exception
     */
    @Test
    @Rollback
    public void testProjectWithCollection() throws Exception {
        setupValidLogin();
        String text = FileUtils
                .readFileToString(new File(TestConstants.TEST_ROOT_DIR + "/xml/record-with-collections.xml"));
        ApiClientResponse response = apiClient.uploadRecord(text, null, null);
        logger.debug("status:{} ", response.getStatusLine());
        logger.debug("response: {}", response.getBody());
        assertEquals(StatusCode.CREATED, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testInvalidLogin() throws IllegalStateException, Exception {
        ApiClientResponse response = apiClient.apiLogin(CONFIG.getUsername(), CONFIG.getPassword());
        assertEquals(StatusCode.BAD_REQUEST, response.getStatusCode());
        JaxbResultContainer result = (JaxbResultContainer) parseResult(response.getBody());
        assertNull(result.getApiToken());
        assertNull(result.getUsername());
        assertEquals(MessageHelper.getInstance().getText("apiAuthenticationController.invalid_user"), result.getErrors().get(0));
    }

    public JaxbResultContainer parseResult(String xml) throws JAXBException, JaxbParsingException {
        JAXBContext jc = JAXBContext.newInstance(JaxbResultContainer.class);
        // SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        // Schema schema = sf.newSchema(generateSchema());

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        // unmarshaller.setSchema(schema);

        final List<String> errors = new ArrayList<>();
        List<String> lines = Arrays.asList(StringUtils.split(xml, '\n'));
        unmarshaller.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                // TODO Auto-generated method stub
                JaxbValidationEvent err = new JaxbValidationEvent(event,
                        lines.get(event.getLocator().getLineNumber() - 1));
                errors.add(err.toString());
                logger.warn("an XML parsing exception occurred: {}", err);
                return true;
            }
        });

        // separate out so that we can throw the exception
        JaxbResultContainer toReturn = (JaxbResultContainer) unmarshaller.unmarshal(new StringReader(xml));

        if (errors.size() > 0) {
            throw new JaxbParsingException(MessageHelper.getMessage("serializationService.could_not_parse"), errors);
        }

        return toReturn;
    }

}
