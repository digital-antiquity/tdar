package org.tdar.web.tag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.tag.GetXsltTemplate;
import org.tdar.tag.GetXsltTemplateResponse;
import org.tdar.tag.Query;
import org.tdar.tag.Query.What;
import org.tdar.tag.Query.When;
import org.tdar.tag.Query.Where;
import org.tdar.tag.ResultType;
import org.tdar.tag.SearchResults;
import org.tdar.tag.SearchResults.Meta;
import org.tdar.tag.SubjectType;
import org.tdar.tag.TagGateway;
import org.tdar.tag.TagGatewayPort;
import org.tdar.tag.TagGatewayService;
import org.tdar.utils.TestConfiguration;
import org.w3c.dom.Element;

/**
 * NOTE THIS TEST NEEDS TO BE RUN VIA MAVEN and NOT THE JUNIT PLUGIN WITHIN ECLIPSE
 * 
 * @author abrin
 *
 */
public class TagGatewayWebITCase  {

    public transient Logger logger = LoggerFactory.getLogger(getClass());
    
    private static final String WSDL_LOCATION = TestConfiguration.getInstance().getBaseUrl() + "TagGatewayService?wsdl";
    private static final String SERVICE_NAMESPACE = "http://archaeologydataservice.ac.uk/tag/schema";
    private static final String SERVICE_NAME = "TagGatewayService";
    private TagGatewayPort port;

    @Autowired
    TagGateway gateway;    

    @Before
    public void setupServiceClient() throws MalformedURLException {
        // use this to run the TAG Gateway with a direct connection, not a socket connection
        boolean runLocal = false;
        if (!runLocal) {
            TagGatewayService service = new TagGatewayService(
                    new URL(WSDL_LOCATION),
                    new QName(SERVICE_NAMESPACE, SERVICE_NAME));
            port = service.getTagGateway();
            logger.debug("loc:{}", WSDL_LOCATION);
            logger.debug("port:{}", port);

        } else {
            port = gateway;
        }
    }

    @Test
    @Rollback(true)
    public void testGetVersion() {
        assertEquals("0.8", port.getVersion());
    }

    /**
     * Tests that the XML returned looks like an XSLT. This is not used in the portal
     * so it doesn't really matter what the stylesheet does.
     */
    @Test
    @Rollback(true)
    public void testGetXsltTemplate() {
        GetXsltTemplate params = new GetXsltTemplate();
        GetXsltTemplateResponse resp = port.getXsltTemplate(params);
        Element xslt = (Element) resp.getAny();
        logger.debug("XSLT: {}", xslt.getOwnerDocument().getDocumentElement().getTagName());
        assertTrue(xslt.getTagName().contains("stylesheet"));
    }

    public SearchResults getResults(Query query, String sessionId) {
        for (int i = 0; i < 5; i++) {
            try {
                return port.getTopRecords(sessionId, query, 5);
            } catch (SOAPFaultException sfe) {
                logger.error("TagException: ", sfe);
            }
        }
        Assert.fail("Tried 5 times and couldn't get past SOAP exception");
        return null;

    }

    @Test
    public void getTopRecords() {
        Query query = new Query();
        query.setFreetext("*:*");
        String sessionId = RandomStringUtils.random(10);
        SearchResults results = getResults(query, sessionId);
        Meta meta = results.getMeta();
        assertEquals(sessionId, meta.getSessionID());
        logger.debug("total: {}", meta.getTotalRecords());
        logger.debug("records: {}", results.getResults());        
        assertTrue(meta.getTotalRecords() > 0); // we are absolutely 100% positive that there are maybe some records that should come back.
        assertEquals(5, results.getResults().getResult().size());
    }

    @Test
    public void testWhatQuery() {
        What domestic = new What();

        Query query = new Query();
        query.setFreetext("*:*");
        String sessionId = RandomStringUtils.random(10);
        domestic.getSubjectTerm().add(SubjectType.DOMESTIC);
        query.setWhat(domestic);
        SearchResults results = getResults(query, sessionId);
        String[] ids = { "262", "1268", "2420", "3805" };
        boolean ok = false;
        for (ResultType result : results.getResults().getResult()) {
            for (String id : ids) {
                if (id.equals(result.getIdentifier())) {
                    // logger.info("ok: {} ", id);
                    ids = (String[]) ArrayUtils.removeElement(ids, id);
                    ok = true;
                }
            }
            // logger.info("saw: {}", result.identifier);
        }
        assertTrue("should see something, missed:" + ArrayUtils.toString(ids), ok);
    }

    @Test
    public void testWhereQuery() {
        Query query = new Query();
        query.setFreetext("*:*");
        String sessionId = RandomStringUtils.random(10);
        Where where = new Where(); // look in AZ and NM
        where.setMaxLatitude(BigDecimal.valueOf(37.14));
        where.setMinLatitude(BigDecimal.valueOf(31.86));
        where.setMinLongitude(BigDecimal.valueOf(-114.68));
        where.setMaxLongitude(BigDecimal.valueOf(-103.01));
        query.setWhere(where);
        SearchResults results = getResults(query, sessionId);

        assertTrue(titleInResults(results.getResults().getResult(), "Ojo Bonito Archaeological Project (OBAP)"));
        assertTrue(titleInResults(results.getResults().getResult(), "Heshotauthla Archaeological Research Project (HARP)"));
        assertTrue(titleInResults(results.getResults().getResult(), "Cibola Archaeological Research Project (CARP)"));
        query.setWhere(null);
    }

    @Test
    public void testWhenQuery() {
        When when = new When();
        when.setMinDate(800);
        when.setMaxDate(1500);
        Query query = new Query();
        query.setFreetext("*:*");
        String sessionId = RandomStringUtils.random(10);
        query.setWhen(when);
        SearchResults results = getResults(query, sessionId);
        assertTrue(titleInResults(results.getResults().getResult(),
                "Heshotauthla Archaeological Research Project (HARP)"));

    }

    @Test
    public void testFreetextQuery() {
        Query query = new Query();
        String sessionId = RandomStringUtils.random(10);

        query.setFreetext("Rudd Creek");
        SearchResults results = getResults(query, sessionId);
        assertTrue(titleInResults(results.getResults().getResult(),
                "Rudd Creek Archaeological Project"));
    }

    private boolean titleInResults(List<ResultType> results, String title) {
        for (ResultType res : results) {
            logger.debug(res.getIdentifier() + " " + res.getTitle());
            if (res.getTitle().equalsIgnoreCase(title)) {
                return true;
            }
        }
        return false;
    }

    @Test
    @Rollback(true)
    public void testSubjectType() {
        SubjectType type = SubjectType.valueOf("GARDENS_PARKS_AND_URBAN_SPACES");
        assertEquals(SubjectType.GARDENS_PARKS_AND_URBAN_SPACES, type);
        assertEquals("GARDENS_PARKS_AND_URBAN_SPACES", type.value());
    }

}
