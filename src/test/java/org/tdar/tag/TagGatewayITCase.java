package org.tdar.tag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractWithIndexIntegrationTestCase;
import org.tdar.core.bean.resource.Resource;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.tag.Query.What;
import org.tdar.tag.Query.When;
import org.tdar.tag.Query.Where;
import org.tdar.tag.SearchResults.Meta;
import org.tdar.utils.TestConfiguration;
import org.w3c.dom.Element;

import edu.emory.mathcs.backport.java.util.Arrays;

public class TagGatewayITCase extends AbstractWithIndexIntegrationTestCase {

    private static final String WSDL_LOCATION = TestConfiguration.getInstance().getBaseUrl() + "services/TagGatewayService?wsdl";
    private static final String SERVICE_NAMESPACE = "http://archaeologydataservice.ac.uk/tag/schema";
    private static final String SERVICE_NAME = "TagGatewayService";
    private TagGatewayPort port;

    @Autowired
    TagGateway gateway;

    @Before
    public void setupServiceClient() throws MalformedURLException {
        getSearchIndexService().indexAll(getAdminUser(), Resource.class);
        // use this to run the TAG Gateway with a direct connection, not a socket connection
        boolean runLocal = false;
        if (!runLocal) {
            TagGatewayService service = new TagGatewayService(
                    new URL(WSDL_LOCATION),
                    new QName(SERVICE_NAMESPACE, SERVICE_NAME));
            port = service.getTagGateway();
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
        assertEquals("stylesheet", xslt.getLocalName());
    }

    @Test
    @Rollback(true)
    public void getTopRecords() {

        SearchResults results;
        Meta meta;

        String sessionId = RandomStringUtils.random(10);
        Query query = new Query();

        query.setFreetext("*:*");

        results = port.getTopRecords(sessionId, query, 5);
        meta = results.getMeta();
        assertEquals(sessionId, meta.getSessionID());
        assertTrue(meta.getTotalRecords() > 0); // we are absolutely 100% positive that there are maybe some records that should come back.
        assertEquals(5, results.getResults().getResult().size());

        What domestic = new What();
        domestic.getSubjectTerm().add(SubjectType.DOMESTIC);
        query.setWhat(domestic);
        results = port.getTopRecords(sessionId, query, 5);
        meta = results.getMeta();
        String[] ids = {"262", "1268", "2420", "3805"};
        boolean ok = false;
        for (ResultType result : results.getResults().getResult()) {
            for (String id : ids) {
                if (id.equals(result.identifier)) {
//                    logger.info("ok: {} ", id);
                    ids = (String[])ArrayUtils.removeElement(ids, id);
                    ok = true;
                }
            }
//            logger.info("saw: {}", result.identifier);
        }
        assertTrue("should see something, missed:" + ArrayUtils.toString(ids),ok);
        query.setWhat(null);

        Where where = new Where(); // look in AZ and NM
        where.setMaxLatitude(BigDecimal.valueOf(37.14));
        where.setMinLatitude(BigDecimal.valueOf(31.86));
        where.setMinLongitude(BigDecimal.valueOf(-114.68));
        where.setMaxLongitude(BigDecimal.valueOf(-103.01));
        query.setWhere(where);
        results = port.getTopRecords(sessionId, query, 5);
        meta = results.getMeta();

        assertTrue(titleInResults(results.getResults().getResult(),
                "The Archaeology of Tuzigoot National Monument and Montezuma Castle National Monument"));
        query.setWhere(null);

        When when = new When();
        when.setMinDate(800);
        when.setMaxDate(1500);
        query.setWhen(when);
        results = port.getTopRecords(sessionId, query, 5);
        assertTrue(titleInResults(results.getResults().getResult(),
                "Heshotauthla Archaeological Research Project (HARP)"));
        query.setWhen(null);

        query.setFreetext("Rudd Creek");
        results = port.getTopRecords(sessionId, query, 5);
        assertTrue(titleInResults(results.getResults().getResult(),
                "Rudd Creek Archaeological Project"));

    }

    private boolean titleInResults(List<ResultType> results, String title) {
        for (ResultType res : results) {
            if (res.getTitle().equalsIgnoreCase(title))
                return true;
        }
        return false;
    }

    @Override
    protected TdarActionSupport getController() {
        return null;
    }

    @Test
    @Rollback(true)
    public void testSubjectType() {
        SubjectType type = SubjectType.valueOf("GARDENS_PARKS_AND_URBAN_SPACES");
        assertEquals(SubjectType.GARDENS_PARKS_AND_URBAN_SPACES, type);
        assertEquals("GARDENS_PARKS_AND_URBAN_SPACES", type.value());
    }

}
