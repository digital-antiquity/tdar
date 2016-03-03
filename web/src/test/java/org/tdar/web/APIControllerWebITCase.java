package org.tdar.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.struts.action.APIControllerITCase;
import org.tdar.utils.APIClient;
import org.tdar.utils.ApiClientResponse;
import org.tdar.utils.SimpleHttpUtils;
import org.tdar.utils.TestConfiguration;
import org.tdar.utils.jaxb.JaxbResultContainer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.media.rtsp.protocol.StatusCode;

@RunWith(MultipleTdarConfigurationRunner.class)
public class APIControllerWebITCase extends AbstractWebTestCase {

    @Autowired
    SerializationService serializationService;

    @Autowired
    BillingAccountService billingAccountService;

    private static final TestConfiguration CONFIG = TestConfiguration.getInstance();
    private static Logger logger = LoggerFactory.getLogger(SimpleHttpUtils.class);
    private APIClient apiClient;
    
    @Before
    public void setupAPIClient() {
        apiClient = new APIClient(CONFIG.getBaseSecureUrl());
    }
    
    @Test
    public void testValidLogin() throws IllegalStateException, Exception {
        setupValidLogin();
        ApiClientResponse response = apiClient.apiLogout();
        Assert.assertTrue(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_MOVED_TEMPORARILY).contains(response.getStatusLine().getStatusCode()));

    }

    private JaxbResultContainer setupValidLogin() {
        try {
             ApiClientResponse response = apiClient.apiLogin(CONFIG.getAdminUsername(), CONFIG.getAdminPassword());
            logger.debug("status {}", response.getStatusLine());
            assertEquals(StatusCode.OK, response.getStatusCode());
            JaxbResultContainer result = (JaxbResultContainer) serializationService.parseXml(new StringReader(response.getBody()));
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
        get = new HttpGet(String.format("%s/api/view?id=4231&%s=%s", CONFIG.getBaseSecureUrl(), login.getSessionKeyName(), login.getApiToken()));
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
        JaxbResultContainer login = setupValidLogin();
        Document doc = genericService.findAll(Document.class, 1).get(0);
        genericService.markReadOnly(doc);
        doc.setId(null);
        doc.getInformationResourceFiles().clear();
        doc.setMappedDataKeyColumn(null);
        APIControllerITCase.removeInvalidFields(doc);
        String docXml = serializationService.convertToXML(doc);
        logger.info(docXml);
        ApiClientResponse response = apiClient.uploadRecord(docXml, null, null);
        logger.debug("status:{} ", response.getStatusLine());
        logger.debug("response: {}", response.getBody());
        assertEquals(StatusCode.CREATED, response.getStatusLine().getStatusCode());
    }

    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.CREDIT_CARD })
    public void testConfidential() throws Exception {
        JaxbResultContainer login = setupValidLogin();
        String filesUed = getFilesUsed(true);
        logger.debug("used: {}", filesUed);

        String text = FileUtils.readFileToString(new File(TestConstants.TEST_ROOT_DIR + "/xml/confidentialImage.xml"));
        ApiClientResponse response = apiClient.uploadRecord(text, null, 1L, new File(TestConstants.TEST_IMAGE),new File(TestConstants.TEST_IMAGE2));

        logger.debug("status:{} ", response.getStatusLine());
        logger.debug("response: {}", response.getBody());
        assertEquals(StatusCode.CREATED, response.getStatusLine().getStatusCode());

        String filesUed_ = getFilesUsed(false);
        logger.debug("used: {} vs. {}", filesUed, filesUed_);
    }

    @Test
    public void testReplaceFile() throws Exception {
        JaxbResultContainer login = setupValidLogin();

        String text = FileUtils.readFileToString(new File(TestConstants.TEST_ROOT_DIR + "/xml/confidentialImage.xml"));
        ApiClientResponse response = apiClient.uploadRecord(text, null, null, new File(TestConstants.TEST_IMAGE));
        logger.debug("status:{} ", response.getStatusLine());
        String resp = response.getBody();
        logger.debug("response: {}", resp);
        org.w3c.dom.Document document = getXmlDocument(new InputSource(new StringReader(resp)));
        String id_ = document.getElementsByTagName("tdar:id").item(0).getTextContent();
        logger.debug("ID:: {}", id_);
        Long id = Long.parseLong(id_);
        assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());

        CloseableHttpClient client2 = SimpleHttpUtils.createClient();
        HttpGet get = new HttpGet(String.format("%s/api/view?id=%s&%s=%s", CONFIG.getBaseSecureUrl(), id, login.getSessionKeyName(), login.getApiToken()));
        CloseableHttpResponse execute = client2.execute(get);
        String xmlRecord = IOUtils.toString(execute.getEntity().getContent());
        logger.debug(xmlRecord);
        document = getXmlDocument(new InputSource(new StringReader(xmlRecord)));
        String fileId = document.getElementsByTagName("tdar:informationResourceFile").item(0).getAttributes().getNamedItem("id").getNodeValue();
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

    private org.w3c.dom.Document getXmlDocument(InputSource is) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory
                .newDocumentBuilder();
        org.w3c.dom.Document document = documentBuilder.parse(is);
        return document;
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

    @Test
    @Rollback
    public void testProjectWithCollection() throws Exception {
        JaxbResultContainer login = setupValidLogin();
        String text = FileUtils.readFileToString(new File(TestConstants.TEST_ROOT_DIR + "/xml/record-with-collections.xml"));
        ApiClientResponse response = apiClient.uploadRecord(text, null, null);
        logger.debug("status:{} ", response.getStatusLine());
        logger.debug("response: {}", response.getBody());
        assertEquals(StatusCode.CREATED, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testInvalidLogin() throws IllegalStateException, Exception {
        ApiClientResponse response = apiClient.apiLogin(CONFIG.getUsername(), CONFIG.getPassword());
        assertEquals(StatusCode.BAD_REQUEST, response.getStatusCode());
        JaxbResultContainer result = (JaxbResultContainer)serializationService.parseXml(new StringReader(response.getBody()));
        assertNull(result.getApiToken());
        assertNull(result.getUsername());
        assertEquals(getText("apiAuthenticationController.invalid_user"), result.getErrors().get(0));
    }

}
