package org.tdar.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.MultipleWebTdarConfigurationRunner;
import org.tdar.configuration.TdarConfiguration;
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
public class JsonCollectionAPIControllerWebITCase extends AbstractWebTestCase {
    // FIXME: too much duplicate code with APIControllerWebITCase (subclass and share)

    private static final TestConfiguration CONFIG = TestConfiguration.getInstance();
    private static Logger logger = LoggerFactory.getLogger(SimpleHttpUtils.class);
    private APIClient apiClient;

    @Before
    public void setupAPIClient() {
        apiClient = new APIClient(CONFIG.getBaseSecureUrl(), 500);
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

    @Test
    public void testValidRequestWithoutCookie() throws IllegalStateException, Exception {
        JaxbResultContainer login = setupValidLogin();
        CloseableHttpClient client2 = SimpleHttpUtils.createClient();
        HttpPost post = new HttpPost(CONFIG.getBaseSecureUrl() + "/api/collection/upload");

        // String docXml = FileUtils.readFileToString(TestConstants.getFile(TestConstants.TEST_ROOT_DIR , "/xml/newDocument.xml"));

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        // FIXME: put in test-resources
        String docXml = "{\"status\" : \"ACTIVE\",\"sortBy\" : \"TITLE\",\"orientation\" : \"LIST\",\"name\" : \"Viking\",\"description\" : \"Viking test\"}";

        builder.addTextBody("record", docXml, ContentType.create("application/json", Consts.UTF_8));
        // add sessionToken
        builder.addTextBody(login.getSessionKeyName(), login.getApiToken(), ContentType.create("application/text", Consts.UTF_8));
        post.setEntity(builder.build());

        CloseableHttpResponse execute = client2.execute(post);
        int statusCode = execute.getStatusLine().getStatusCode();
        logger.debug("status:{}", statusCode);
        logger.debug(IOUtils.toString(execute.getEntity().getContent()));
        assertNotEquals(HttpStatus.SC_FORBIDDEN, statusCode);
        apiClient.apiLogout();
    }
}
