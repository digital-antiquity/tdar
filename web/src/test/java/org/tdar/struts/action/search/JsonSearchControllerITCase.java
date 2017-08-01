package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.ActivityManager;
import org.tdar.core.service.RssService.GeoRssMode;
import org.tdar.core.service.external.session.SessionData;
import org.tdar.search.bean.ObjectType;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts.action.api.search.JsonSearchAction;
import org.tdar.struts_base.action.TdarActionException;
import org.xml.sax.SAXException;

@Transactional
public class JsonSearchControllerITCase extends AbstractSearchControllerITCase {


    @Autowired
    SearchIndexService searchIndexService;

    @Before
    public void setup() {
        // there are no 'default' namespaces in xmlunit land... you *must* specify namespaces for all elements in an xpath query
        HashMap<String, String> m = new HashMap<String, String>();
        m.put("atom", "http://www.w3.org/2005/Atom");
        m.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        m.put("sy", "http://purl.org/Json/1.0/modules/syndication/");
        m.put("opensearch", "http://a9.com/-/spec/opensearch/1.1/");
        m.put("dc", "http://purl.org/dc/elements/1.1/");
        m.put("taxo", "http://purl.org/Json/1.0/modules/taxonomy");
        NamespaceContext ctx = new SimpleNamespaceContext(m);
        XMLUnit.setXpathNamespaceContext(ctx);

    }

    @Test
    @Rollback(true)
    public void testJsonDefaultSortOrder() throws InstantiationException, IllegalAccessException, TdarActionException, SearchIndexException, IOException {
        InformationResource document = generateDocumentWithUser();
        searchIndexService.index(document);
        JsonSearchAction controller = generateNewInitializedController(JsonSearchAction.class);
        controller.setSessionData(new SessionData()); // create unauthenticated session
        // doSearch("");
        controller.viewJson();
        // the record we created should be the absolute first record
        assertEquals(document, controller.getResults().get(0));
    }

    @Test
    @Rollback(true)
    public void testGeoJson() throws InstantiationException, IllegalAccessException, TdarActionException, IOException, SearchIndexException {
        InformationResource document = generateDocumentWithUser();
        document.getLatitudeLongitudeBoxes().add(new LatitudeLongitudeBox(84.37156598282918, 57.89149735271034, -131.484375, 27.0703125));
        genericService.saveOrUpdate(document);
        searchIndexService.index(document);
        String xml = setupGeoJsonCall(document, GeoRssMode.ENVELOPE);
        logger.debug(xml);
        assertTrue(xml.contains("\"coordinates\" : [ [ [ 84.37156598282918, 57.89149735271034 ], [ 84.37156598282918, 27.0703125 ], [ -131.484375, 27.0703125 ], [ -131.484375, 57.89149735271034 ], [ 84.37156598282918, 57.89149735271034 ] ] ]"));
    }

    @Test
    @Rollback(true)
    public void testJsonLoggedIn() throws TdarActionException, IOException {
        reindex();
        JsonSearchAction controller = generateNewInitializedController(JsonSearchAction.class, getAdminUser());
        controller.viewJson();
        assertNotEmpty("should have results", controller.getResults());
        String xml = IOUtils.toString(controller.getJsonInputStream());
        logger.debug(xml);
    }

    private String setupGeoJsonCall(InformationResource document, GeoRssMode mode) throws TdarActionException, IOException {
        JsonSearchAction controller = generateNewInitializedController(JsonSearchAction.class);
        controller.setSessionData(new SessionData()); // create unauthenticated session
        controller.setGeoMode(mode);
        controller.viewJson();
        // the record we created should be the absolute first record
        assertEquals(document, controller.getResults().get(0));
        String xml = IOUtils.toString(controller.getJsonInputStream());
        return xml;
    }

    @Test
    @Rollback(true)
    public void testJsonInvalidCharacters() throws InstantiationException, IllegalAccessException, TdarActionException, SearchIndexException, IOException {
        InformationResource document = generateDocumentWithUser();
        document.setDescription("a\u0001a");
        genericService.saveOrUpdate(document);
        searchIndexService.index(document);
        JsonSearchAction controller = generateNewInitializedController(JsonSearchAction.class);
        controller.setSessionData(new SessionData()); // create unauthenticated session
        // doSearch("");
        String viewJson = controller.viewJson();
        logger.debug(viewJson);
        logger.debug("{}", controller.getActionErrors());
        String string = IOUtils.toString(controller.getJsonInputStream());
        logger.debug(string);
        // the record we created should be the absolute first record
        assertEquals(0, controller.getActionErrors().size());
    }

    @Test
    @Rollback(true)
    public void testFindResourceBuildJson() throws XpathException, SAXException, IOException, InterruptedException, TdarActionException, SearchIndexException {
        ActivityManager.getInstance().getActivityQueueClone().clear();
        Resource r = genericService.find(Resource.class, 3074L);
        r.setStatus(Status.ACTIVE);
        genericService.saveOrUpdate(r);
        searchIndexService.index(r);
        evictCache();
        Thread.sleep(1000l);
        JsonSearchAction controller = generateNewInitializedController(JsonSearchAction.class);
        controller.setId(r.getId());
        controller.getObjectTypes().addAll(Arrays.asList(ObjectType.DATASET));
        controller.setSessionData(new SessionData()); // create unauthenticated session
        assertFalse(controller.isReindexing());
        controller.viewJson();
        String JsonFeed = IOUtils.toString(controller.getJsonInputStream());

        assertTrue(resultsContainId(3074l, controller));
        logger.info(JsonFeed);
        assertTrue("feed should contain id " + r.getId() + ": " + JsonFeed, JsonFeed.contains(r.getId().toString()));
        assertTrue(JsonFeed.contains("Durrington Walls Humerus Dataset"));
        assertTrue(JsonFeed.contains("title\" : \"Durrington Walls Humerus Dataset\","));
    }

    protected boolean resultsContainId(Long id, JsonSearchAction controller_) {
        boolean found = false;
        for (Resource r : controller_.getResults()) {
            logger.trace(r.getId() + " " + r.getResourceType());
            if (id.equals(r.getId())) {
                found = true;
                break;
            }
        }
        return found;
    }
}
