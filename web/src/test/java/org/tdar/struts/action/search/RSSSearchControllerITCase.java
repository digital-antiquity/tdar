package org.tdar.struts.action.search;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServerException;
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
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.ActivityManager;
import org.tdar.core.service.RssService.GeoRssMode;
import org.tdar.core.service.external.session.SessionData;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;
import org.xml.sax.SAXException;

@Transactional
public class RSSSearchControllerITCase extends AbstractSearchControllerITCase {

    @Autowired
    private RSSSearchAction controller;

    @Autowired
    SearchIndexService searchIndexService;

    /*
     * Here's a good webp-based tool for testing xpath queries: http://www.whitebeam.org/library/guide/TechNotes/xpathtestbed.rhtm
     * You can test against the sample snippet below:
     * 
     * <feed xmlns="http://www.w3.org/2005/Atom" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:sy="http://purl.org/rss/1.0/modules/syndication/"
     * xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/" xmlns:dc="http://purl.org/dc/elements/1.1/"
     * xmlns:taxo="http://purl.org/rss/1.0/modules/taxonomy/">
     * <title>tDAR Search Results: All Records</title>
     * <link rel="alternate" href="" />
     * <subtitle>Searching for resource with tDAR ID: &lt;strong&gt; 3074 &lt;/strong&gt;</subtitle>
     * <opensearch:itemsPerPage>20</opensearch:itemsPerPage>
     * <opensearch:totalResults>1</opensearch:totalResults>
     * <opensearch:startIndex>1</opensearch:startIndex>
     * <opensearch:link rel="search" type="application/opensearchdescription+xml" href="http://core.tdar.org/includes/opensearch.xml" />
     * <entry>
     * <title>Durrington Walls Humerus Dataset</title>
     * <link rel="alternate" href="http://core.tdar.org/dataset/3074" />
     * <author>
     * <name />
     * </author>
     * <updated>2010-03-05T19:59:58Z</updated>
     * <published>2010-03-05T19:59:58Z</published>
     * <summary type="HTML" />
     * <dc:date>2010-03-05T19:59:58Z</dc:date>
     * </entry>
     * </feed>
     */

    @Before
    public void setup() {
        // there are no 'default' namespaces in xmlunit land... you *must* specify namespaces for all elements in an xpath query
        HashMap<String, String> m = new HashMap<String, String>();
        m.put("atom", "http://www.w3.org/2005/Atom");
        m.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        m.put("sy", "http://purl.org/rss/1.0/modules/syndication/");
        m.put("opensearch", "http://a9.com/-/spec/opensearch/1.1/");
        m.put("dc", "http://purl.org/dc/elements/1.1/");
        m.put("taxo", "http://purl.org/rss/1.0/modules/taxonomy");
        NamespaceContext ctx = new SimpleNamespaceContext(m);
        XMLUnit.setXpathNamespaceContext(ctx);

    }

    @Test
    @Rollback(true)
    public void testRssDefaultSortOrder() throws InstantiationException, IllegalAccessException, TdarActionException, SolrServerException, IOException {
        InformationResource document = generateDocumentWithUser();
        searchIndexService.index(document);
        controller.setSessionData(new SessionData()); // create unauthenticated session
        // doSearch("");
        controller.viewRss();
        // the record we created should be the absolute first record
        assertEquals(document, controller.getResults().get(0));
    }


    @Test
    @Rollback(true)
    public void testRss404() throws InstantiationException, IllegalAccessException, TdarActionException, SolrServerException, IOException {
        InformationResource document = generateDocumentWithUser();
        searchIndexService.index(document);
        controller.setSessionData(new SessionData()); // create unauthenticated session
        // doSearch("");
        controller.setStartRecord(1000);
        String viewRss = controller.viewRss();
        assertNotEquals(TdarActionSupport.SUCCESS, viewRss);
        // the record we created should be the absolute first record
//        assertEquals(document, controller.getResults().get(0));
    }

    @Test
    @Rollback(true)
    public void testGeoRSS() throws InstantiationException, IllegalAccessException, TdarActionException, IOException, SolrServerException {
        InformationResource document = generateDocumentWithUser();
        document.getLatitudeLongitudeBoxes().add(new LatitudeLongitudeBox(84.37156598282918, 57.89149735271034, -131.484375, 27.0703125));
        genericService.saveOrUpdate(document);
        searchIndexService.index(document);
        String xml = setupGeoRssCall(document, GeoRssMode.POINT);
        logger.debug(xml);
        assertTrue(xml.contains("<georss:point>42.480904926355166 156.44359549141458</georss:point>"));
        xml = setupGeoRssCall(document, GeoRssMode.NONE);
        logger.debug(xml);
        assertTrue(!xml.contains("<georss"));
        xml = setupGeoRssCall(document, GeoRssMode.ENVELOPE);
        logger.debug(xml);
        assertTrue(xml.contains("<georss:box>57.89149735271034 84.37156598282918 27.0703125 -131.484375</georss:box>"));
    }

    @Test
    @Rollback(true)
    public void testRSSLoggedIn() throws TdarActionException, IOException {
        reindex();
        controller = generateNewInitializedController(RSSSearchAction.class, getAdminUser());
        controller.viewRss();
        assertNotEmpty(controller.getResults());
        String xml = IOUtils.toString(controller.getInputStream());
        logger.debug(xml);
        assertTrue(xml.contains("link rel=\"enclosure\" type=\"application/vnd.ms-excel"));
    }

    private String setupGeoRssCall(InformationResource document, GeoRssMode mode) throws TdarActionException, IOException {
        controller = generateNewInitializedController(RSSSearchAction.class);
        controller.setSessionData(new SessionData()); // create unauthenticated session
        controller.setGeoMode(mode);
        controller.viewRss();
        // the record we created should be the absolute first record
        assertEquals(document, controller.getResults().get(0));
        String xml = IOUtils.toString(controller.getInputStream());
        return xml;
    }

    @Test
    @Rollback(true)
    public void testRssInvalidCharacters() throws InstantiationException, IllegalAccessException, TdarActionException, SolrServerException, IOException {
        InformationResource document = generateDocumentWithUser();
        document.setDescription("a\u0001a");
        genericService.saveOrUpdate(document);
        searchIndexService.index(document);
        controller.setSessionData(new SessionData()); // create unauthenticated session
        // doSearch("");
        String viewRss = controller.viewRss();
        logger.debug(viewRss);
        logger.debug("{}", controller.getActionErrors());
        // the record we created should be the absolute first record
        assertEquals(0, controller.getActionErrors().size());
    }

    @Test
    @Rollback(true)
    public void testFindResourceBuildRss() throws XpathException, SAXException, IOException, InterruptedException, TdarActionException, SolrServerException {
        ActivityManager.getInstance().getActivityQueueClone().clear();
        Resource r = genericService.find(Resource.class, 3074L);
        r.setStatus(Status.ACTIVE);
        genericService.saveOrUpdate(r);
        searchIndexService.index(r);
        evictCache();
        Thread.sleep(1000l);
        controller.setId(r.getId());
        controller.getResourceTypes().addAll(Arrays.asList(ResourceType.DATASET));
        controller.setSessionData(new SessionData()); // create unauthenticated session
        assertFalse(controller.isReindexing());
        controller.viewRss();
        String rssFeed = IOUtils.toString(controller.getInputStream());

        assertTrue(resultsContainId(3074l, controller));
        logger.info(rssFeed);
        assertTrue("feed should contain id " + r.getId() + ": " + rssFeed, rssFeed.contains(r.getId().toString()));
        assertTrue(rssFeed.contains("Durrington Walls Humerus Dataset"));
        assertXpathEvaluatesTo("Durrington Walls Humerus Dataset", "/atom:feed/atom:entry/atom:title", rssFeed);
    }

    protected boolean resultsContainId(Long id, RSSSearchAction controller_) {
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
