package org.tdar.struts.action.search;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.tdar.core.service.GeoRssMode;
import org.tdar.core.service.external.session.SessionData;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts.action.api.search.RSSSearchAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@Transactional
public class RSSSearchControllerITCase extends AbstractSearchControllerITCase {

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
    public void testRssDefaultSortOrder() throws InstantiationException, IllegalAccessException, TdarActionException, SearchIndexException, IOException {
        InformationResource document = generateDocumentWithUser();
        searchIndexService.index(document);
        RSSSearchAction controller = generateNewInitializedController(RSSSearchAction.class);
        controller.setSessionData(new SessionData()); // create unauthenticated session
        // doSearch("");
        controller.viewRss();
        // the record we created should be the absolute first record
        assertEquals(document, controller.getResults().get(0));
    }

    @Test
    @Rollback(true)
    public void testRss404() throws InstantiationException, IllegalAccessException, TdarActionException, SearchIndexException, IOException {
        InformationResource document = generateDocumentWithUser();
        searchIndexService.index(document);
        RSSSearchAction controller = generateNewInitializedController(RSSSearchAction.class);
        controller.setSessionData(new SessionData()); // create unauthenticated session
        // doSearch("");
        controller.setStartRecord(1000);
        String viewRss = controller.viewRss();
        assertNotEquals(TdarActionSupport.SUCCESS, viewRss);
        // the record we created should be the absolute first record
        // assertEquals(document, controller.getResults().get(0));
    }

    @Test
    @Rollback(true)
    public void testGeoRSS() throws InstantiationException, IllegalAccessException, TdarActionException, IOException, SearchIndexException {
        InformationResource document = generateDocumentWithUser();
        LatitudeLongitudeBox box = new LatitudeLongitudeBox();
        box.setEast(-100.78792202517137);
        box.setWest(-107.78792202517137);
        box.setNorth(46.08765565625065);
        box.setSouth(36.08765565625065);
        logger.debug("valid:{}", box.isValid());
        assertTrue(box.isValid());
        document.getLatitudeLongitudeBoxes().add(box);
        genericService.saveOrUpdate(document.getLatitudeLongitudeBoxes());
        genericService.saveOrUpdate(document);
        searchIndexService.index(document);

        String xml = setupGeoRssCall(document, GeoRssMode.POINT);
        logger.debug(xml);
        assertTrue(xml.contains("<georss:point>41.08765565625065 -104.28792202517137</georss:point>"));
        xml = setupGeoRssCall(document, GeoRssMode.NONE);
        logger.debug(xml);
        assertTrue(!xml.contains("<georss"));
        xml = setupGeoRssCall(document, GeoRssMode.ENVELOPE);
        logger.debug(xml);
        assertTrue(xml.contains("<georss:box>36.08765565625065 -107.78792202517137 46.08765565625065 -100.78792202517137</georss:box>"));
    }

    @Test
    @Rollback(true)
    public void testRSSLoggedIn() throws TdarActionException, IOException {
        reindex();
        RSSSearchAction controller = generateNewInitializedController(RSSSearchAction.class, getAdminUser());
        controller.viewRss();
        assertNotEmpty("should have results", controller.getResults());
        String xml = IOUtils.toString(controller.getInputStream());
        logger.debug(xml);
        StringUtils.containsAny(xml, "<link rel=\"enclosure\" type=\"application/pdf\"", "<link rel=\"enclosure\" type=\"application/vnd.ms-excel");
    }

    private String setupGeoRssCall(InformationResource document, GeoRssMode mode) throws TdarActionException, IOException {
        RSSSearchAction controller = generateNewInitializedController(RSSSearchAction.class);
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
    public void testRssInvalidCharacters()
            throws InstantiationException, IllegalAccessException, TdarActionException, SearchIndexException, IOException, ParserConfigurationException {
        InformationResource document = generateDocumentWithUser();
        document.setDescription("a\u0001a\u000B");
        genericService.saveOrUpdate(document);
        searchIndexService.index(document);
        RSSSearchAction controller = generateNewInitializedController(RSSSearchAction.class);
        controller.setSessionData(new SessionData()); // create unauthenticated session
        // doSearch("");
        String viewRss = controller.viewRss();
        logger.debug(viewRss);
        String string = IOUtils.toString(controller.getInputStream());
        logger.debug(string);
        assertTrue(string.contains("a#1;a"));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        boolean exceptions = false;
        try {
            Document doc = builder.parse(new ByteArrayInputStream(string.getBytes()));
        } catch (SAXException e) {
            exceptions = true;
            logger.error("e: {}", e, e);
        }
        assertFalse("Should not have seen any parsing exceptions", exceptions);

        logger.debug("{}", controller.getActionErrors());
        // the record we created should be the absolute first record
        assertEquals(0, controller.getActionErrors().size());
    }

    @Test
    @Rollback(true)
    public void testFindResourceBuildRss() throws XpathException, SAXException, IOException, InterruptedException, TdarActionException, SearchIndexException {
        ActivityManager.getInstance().getActivityQueueClone().clear();
        Resource r = genericService.find(Resource.class, 3074L);
        r.setStatus(Status.ACTIVE);
        genericService.saveOrUpdate(r);
        searchIndexService.index(r);
        evictCache();
        Thread.sleep(1000l);
        RSSSearchAction controller = generateNewInitializedController(RSSSearchAction.class);
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
