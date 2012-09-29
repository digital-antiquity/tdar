package org.tdar.struts.action.search;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.lucene.queryParser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.SearchIndexService;
import org.tdar.struts.action.TdarActionSupport;

@Transactional
public class LuceneSearchControllerSpatialITCase extends AbstractSearchControllerITCase {

    @Autowired
    public TdarActionSupport getController() {
        return controller;
    }

    @Autowired
    SearchIndexService searchIndexService;

    @Before
    public void reset() {
        controller = generateNewInitializedController(LuceneSearchController.class);
        controller.setRecordsPerPage(50);
    }

    private Document createGeoDoc(String title, double minLatY, double minLongX, double maxLatY, double maxLongX) {
        Document doc = null;
        try {
            doc = createAndSaveNewInformationResource(Document.class);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail(e.getMessage());
        }
        doc.setTitle(title);
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox( minLongX,minLatY, maxLongX,maxLatY);
        assertTrue("latlongbox is not valid", llb.isValid());
        doc.setStatus(Status.ACTIVE);
        logger.debug("creating document w/latLong: {}", llb);
        doc.setLatitudeLongitudeBox(llb);
        llb.setResource(doc);
        genericService.save(doc);
        searchIndexService.index(doc);
        return doc;
    }

    private void performGeoSearch(double minLatY, double minLongX, double maxLatY, double maxLongX) {
        // reindex();
        controller.setMinx(minLongX);
        controller.setMiny(minLatY);
        controller.setMaxx(maxLongX);
        controller.setMaxy(maxLatY);
        try {
            controller.performSearch();
        } catch (ParseException e) {
            fail("exception during parse:" + e.getMessage());
        }
    }

    @Test
    @Rollback
    // a document within a search's geobound should be found when geobounds span the prime meridian
    public void testGeoSearchFoundWhenSpanningPrimeMeridian() {
        // document w/ coords that abut the prime meridian
        Document doc = createGeoDoc("foo", 1d, 0d, 1.1d, 5d);
        performGeoSearch(-10d, -10d, 10d, 10d);
        assertTrue(controller.getResults().contains(doc));
    }

    @Test
    @Rollback
    // a document within a search's geobound should be found when geobounds span the prime meridian
    public void testGeoSearchFoundWhenSpanningPrimeMeridian2() {
        // document w/ coords that are around equator (Mali)
        Document doc = createGeoDoc("foo", 12.726084296948184, -7.646484375, 21.37124437061831, 3.427734375);
        // box around Micronesia
        performGeoSearch(-14.602, 146.154, 20.617, -171.142);
        assertFalse(controller.getResults().contains(doc));
    }

    @Test
    @Rollback
    // a document to the west of a search's geobounds should not be found when geobounds span the prime meridian
    public void testGeoSearchNotFoundWhenSpanningPrimeMeridian() {
        // document w/ coords that are outside of our search (within lat coords but east of long coords)
        Document doc = createGeoDoc("foo", 1d, 11d, 2d, 12d);
        performGeoSearch(-10d, -10d, 10d, 10d); // block around equator and PM (South Atlantic)
        assertFalse(controller.getResults().contains(doc));
        // should be TRUE????
        // performGeoSearch(-10d, -10d, 10d, 10d);
        // assertFalse(controller.getResults().contains(doc));

    }

    @Test
    @Rollback
    // a document to the west of a search's geobounds should not be found when geobounds span the prime meridian
    public void testGeoSearchNotFoundWhenSpanningPrimeMeridianLondon() {
        // document w/ coords that are outside of our search (within lat coords but east of long coords)
        Document doc = createGeoDoc("foo", 55d, 11d, 58d, 12d);
        performGeoSearch(50d, -10d, 60d, 10d); // block around UK
        assertFalse(controller.getResults().contains(doc));
        // should be TRUE????
        // performGeoSearch(-10d, -10d, 10d, 10d);
        // assertFalse(controller.getResults().contains(doc));

    }

    @Test
    @Rollback
    public void testGeoSearchFoundWhenSpanningAntiMeridian() {
        // document w/ bounds that abut the antimeridian
        Document doc = createGeoDoc("foo", 1d, -179d, 2d, -178d);
        logger.debug("{}", doc.getFirstLatitudeLongitudeBox().toString());
        logger.debug("{}", doc);
        performGeoSearch(-10d, 170d, 10d, -170d);
        assertTrue(controller.getResults().contains(doc));
    }

    @Test
    @Rollback
    public void testGeoSearchNotFoundWhenSpanningAntiMeridian() {
        // document outside our search bounds.
        Document doc = createGeoDoc("foo", 1d, 168d, 2d, 169d);
        performGeoSearch(-10d, 170d, 10d, -170d);
        assertFalse(controller.getResults().contains(doc));
    }

    @Test
    @Rollback
    // a document within a search's geobound should be found when geobounds span the equator
    public void testGeoSearchFoundWhenSpanningEquator() {
        Document doc = createGeoDoc("foo", 0d, 5d, 1d, 6d);
        performGeoSearch(-10d, -10d, 10d, 10d);
        assertTrue(controller.getResults().contains(doc));
    }

    @Test
    @Rollback
    // a document to the west of a search's geobounds should not be found when geobounds span the equator
    public void testGeoSearchNotFoundWhenSpanningEquator() {
        Document doc = createGeoDoc("foo", 11d, 5d, 12d, 6d);
        performGeoSearch(-10d, -10d, 10d, 10d);
        assertFalse(controller.getResults().contains(doc));
    }

    @Test
    @Rollback
    public void testGeoSearchWithDocumentThatSpansAntiMeridian() {
        Document doc = createGeoDoc("antimeridian doc", 1d, 170d, 2d, -170d);
        performGeoSearch(-10d, 160d, 10d, -160d);
        assertTrue(controller.getResults().contains(doc));
    }

    @Test
    @Rollback
    public void testGeoSearchThatSpansChile() {
        Document doc1 = createGeoDoc("Camina", -19.3525d, -69.4720458984375d, -19.26447980049709d, -69.378662109375d);
        Document doc2 = createGeoDoc("Monte Patria", -30.8692253480408, -70.77944444444444d, -30.741835717889778d, -70.6146240234375d);
        Document doc3 = createGeoDoc("Isla Gordon", -54.987070078948776d, -69.67527777777778d, -54.91451400766525d, -69.510498046875d);
        performGeoSearch(-56.36527777777778d, -76.2890625d, -16.97274101999902d, -66.97265625d);
        assertTrue(controller.getResults().contains(doc1));
        assertTrue(controller.getResults().contains(doc2));
        assertTrue(controller.getResults().contains(doc3));
    }

    @Test
    @Rollback
    public void testGeoSearchThatSpansGreenland() {
        Document doc = createGeoDoc("Nuuk", 64.17472222222223d, -51.73888888888889d, 64.17500000000001d, -51.7385d);
        Document doc2 = createGeoDoc("Nord", 81.69784444971418d, -17.226666666666667d, 81.82379431564337d, -15.46875d);
        performGeoSearch(57.70414723434193d, -75.234375, 83.71554430601263d, -12.65625d);
        assertTrue(controller.getResults().contains(doc));
        assertTrue(controller.getResults().contains(doc2));
    }

    @Test
    @Rollback
    public void testGeoSearchThatSpansIceland() {
        Document doc = createGeoDoc("Stafnsvötn", 65.18518697818304d, -18.776578903198242d, 65.18614154408306d, -18.773725032806396d);
        performGeoSearch(63.35212928507874d, -24.345703125d, 66.8265202749748d, -13.271484375d);
        assertTrue(controller.getResults().contains(doc));
    }

}
