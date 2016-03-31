package org.tdar.search.service.query;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.AbstractWithIndexIntegrationTestCase;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResult;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.SpatialQueryPart;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.search.service.query.SearchService;
import org.tdar.utils.MessageHelper;

public class SpatialSearchITCase extends AbstractWithIndexIntegrationTestCase {

    protected static List<ResourceType> allResourceTypes = Arrays.asList(ResourceType.values());

    @Autowired
    SearchIndexService searchIndexService;

    @Autowired
    SearchService<Resource> searchService;

    @Autowired
    GenericKeywordService genericKeywordService;

    private LatitudeLongitudeBox searchBox;

    @Override
    public void reindex() {
        searchIndexService.purgeAll(LookupSource.RESOURCE);
    }
    
    private Document createGeoDoc(String title, double minLatY, double minLongX, double maxLatY, double maxLongX) throws SolrServerException, IOException {
        Document doc = null;
        try {
            doc = createAndSaveNewInformationResource(Document.class);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        doc.setTitle(title);
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(minLongX, minLatY, maxLongX, maxLatY);
        assertTrue("latlongbox is not valid", llb.isValid());
        doc.setStatus(Status.ACTIVE);
        logger.debug("creating document w/latLong: {}", llb);
        doc.setLatitudeLongitudeBox(llb);
        genericService.save(doc);
        searchIndexService.index(doc);
        return doc;
    }

    private SearchResult<Resource> performGeoSearch(double minLatY, double minLongX, double maxLatY, double maxLongX)
            throws ParseException, SolrServerException, IOException {
        searchBox = new LatitudeLongitudeBox(minLongX, minLatY, maxLongX, maxLatY);
        ResourceQueryBuilder rqb = new ResourceQueryBuilder();
        rqb.setOperator(Operator.AND);
        SpatialQueryPart sqp = new SpatialQueryPart(searchBox);
        rqb.append(sqp);
        rqb.append(new FieldQueryPart<>(QueryFieldNames.STATUS, Status.ACTIVE));
        SearchResult<Resource> result = new SearchResult<>();
        searchService.handleSearch(rqb, result, MessageHelper.getInstance());
        return result;
    }

    @Test
    @Rollback
    public void testGeoDocNotFoundTexasAZ() throws SolrServerException, IOException, ParseException {
        Document doc = createGeoDoc("dyess", 32.388, -99.885, 32.474, -99.796);
        SearchResult<Resource> result = performGeoSearch(31.50362930577303, -114.78515625, 33.284619968887675, -112.67578125);
        assertFalse(result.getResults().contains(doc));
    }

    /**
     * Setup matches how data is being passed in from searchbox, so we must flip it
     * @throws SolrServerException
     * @throws IOException
     * @throws ParseException
     */
    @Test
    @Rollback
    public void testSearchPetra() throws SolrServerException, IOException, ParseException {
                                                        //double minLatY, double minLongX, double maxLatY, double maxLongX
        SearchResult<Resource> result = performGeoSearch(
                30.329055131879333,
                35.450043082237244,
                30.325934299334392,
                35.44148147106171
                );
    }

    @Test
    @Rollback
    public void testGeoDocScale() throws SolrServerException, IOException, ParseException {
        Document doc = createGeoDoc("dyess1", 1.388, -120, 85.474, -99.796); // should not be found because scale is too big
        Document doc2 = createGeoDoc("dyess2", 31, -115, 34, -112); // should be found b/c on similar scale
        SearchResult<Resource> result = performGeoSearch(31.50362930577303, -114.78515625, 33.284619968887675, -112.67578125);
        logger.info("searchScale:{}", searchBox.getScale());
        logger.info("bb1Scale:{}", doc.getFirstActiveLatitudeLongitudeBox().getScale());
        logger.info("bb2Scale:{}", doc2.getFirstActiveLatitudeLongitudeBox().getScale());
        assertFalse(result.getResults().contains(doc));
        assertTrue(result.getResults().contains(doc2));
        assertTrue((searchBox.getScale() + SpatialQueryPart.SCALE_RANGE) < doc.getFirstActiveLatitudeLongitudeBox().getScale());

    }

    @Test
    @Rollback
    // a document within a search's geobound should be found when geobounds span the prime meridian
    public void testGeoSearchFoundWhenSpanningPrimeMeridian() throws SolrServerException, IOException, ParseException {
        // document w/ coords that abut the prime meridian
        Document doc = createGeoDoc("foo", 1d, 0d, 1.1d, 5d);
        SearchResult<Resource> result = performGeoSearch(-10d, -10d, 10d, 10d);
        assertTrue(result.getResults().contains(doc));
    }

    @Test
    @Rollback
    // a document within a search's geobound should be found when geobounds span the prime meridian
    public void testGeoSearchFoundWhenSpanningPrimeMeridian2() throws SolrServerException, IOException, ParseException {
        // document w/ coords that are around equator (Mali)
        Document doc = createGeoDoc("foo", 12.726084296948184, -7.646484375, 21.37124437061831, 3.427734375);
        // box around Micronesia
        SearchResult<Resource> result = performGeoSearch(-14.602, -30d, 20.617, 146.154);
        assertTrue(result.getResults().contains(doc));
    }

    @Test
    @Rollback
    // a document to the west of a search's geobounds should not be found when geobounds span the prime meridian
    public void testGeoSearchNotFoundWhenSpanningPrimeMeridian() throws SolrServerException, IOException, ParseException {
        // document w/ coords that are outside of our search (within lat coords but east of long coords)
        Document doc = createGeoDoc("foo", 1d, 11d, 2d, 12d);
        SearchResult<Resource> result = performGeoSearch(-10d, -10d, 10d, 10d); // block around equator and PM (South Atlantic)
        assertFalse(result.getResults().contains(doc));
        // should be TRUE????
        result = performGeoSearch(-10d, -10d, 10d, 10d);
        assertFalse(result.getResults().contains(doc));

    }

    @Test
    @Rollback
    // a document to the west of a search's geobounds should not be found when geobounds span the prime meridian
    public void testGeoSearchNotFoundWhenSpanningPrimeMeridianLondon() throws SolrServerException, IOException, ParseException {
        // document w/ coords that are outside of our search (within lat coords but east of long coords)
        Document doc = createGeoDoc("foo", 55d, 11d, 58d, 12d);
        SearchResult<Resource> result = performGeoSearch(50d, -10d, 60d, 10d); // block around UK
        assertFalse(result.getResults().contains(doc));
        // should be TRUE????
        result = performGeoSearch(-10d, -10d, 10d, 10d);
        assertFalse(result.getResults().contains(doc));

    }

    @Test
    @Rollback
    public void testGeoSearchFoundWhenSpanningAntiMeridian() throws SolrServerException, IOException, ParseException {
        // document outside our search bounds.
        Document doc = createGeoDoc("foo", 1d, 168d, 2d, 169d);
        SearchResult<Resource> result = performGeoSearch(-10d, -170d, 10d, 170d);
        assertTrue(result.getResults().contains(doc));
    }

    @Test
    @Rollback
    // a document within a search's geobound should be found when geobounds span the equator
    public void testGeoSearchFoundWhenSpanningEquator() throws SolrServerException, IOException, ParseException {
        Document doc = createGeoDoc("foo", 0d, 5d, 1d, 6d);
        SearchResult<Resource> result = performGeoSearch(-10d, -10d, 10d, 10d);
        assertTrue(result.getResults().contains(doc));
    }

    @Test
    @Rollback
    // a document to the west of a search's geobounds should not be found when geobounds span the equator
    public void testGeoSearchNotFoundWhenSpanningEquator() throws SolrServerException, IOException, ParseException {
        Document doc = createGeoDoc("foo", 11d, 5d, 12d, 6d);
        SearchResult<Resource> result = performGeoSearch(-10d, -10d, 10d, 10d);
        assertFalse(result.getResults().contains(doc));
    }

    @Rollback
    public void testGeoSearchWithDocumentThatSpansAntiMeridian() throws SolrServerException, IOException, ParseException {
        Document doc = createGeoDoc("antimeridian doc", 1d, -170d, 2d, 170d);
        SearchResult<Resource> result = performGeoSearch(-10d, -160d, 10d, 160d);
        assertTrue(result.getResults().contains(doc));
    }

    @Test
    @Rollback
    public void testGeoSearchThatSpansChile() throws SolrServerException, IOException, ParseException {
        Document doc1 = createGeoDoc("Camina", -19.3525d, -69.4720458984375d, -19.26447980049709d, -69.378662109375d);
        Document doc2 = createGeoDoc("Monte Patria", -30.8692253480408, -70.77944444444444d, -30.741835717889778d, -70.6146240234375d);
        Document doc3 = createGeoDoc("Isla Gordon", -54.987070078948776d, -69.67527777777778d, -54.91451400766525d, -69.510498046875d);
        SearchResult<Resource> result = performGeoSearch(-56.36527777777778d, -76.2890625d, -16.97274101999902d, -66.97265625d);
        assertTrue(result.getResults().contains(doc1));
        assertTrue(result.getResults().contains(doc2));
        assertTrue(result.getResults().contains(doc3));
    }

    @Test
    @Rollback
    public void testGeoSearchThatSpansGreenland() throws SolrServerException, IOException, ParseException {
        Document doc = createGeoDoc("Nuuk", 64.17472222222223d, -51.73888888888889d, 64.17500000000001d, -51.7385d);
        Document doc2 = createGeoDoc("Nord", 81.69784444971418d, -17.226666666666667d, 81.82379431564337d, -15.46875d);
        SearchResult<Resource> result = performGeoSearch(57.70414723434193d, -75.234375, 83.71554430601263d, -12.65625d);
        assertTrue(result.getResults().contains(doc));
        assertTrue(result.getResults().contains(doc2));
    }

    @Test
    @Rollback
    public void testGeoSearchThatSpansIceland() throws SolrServerException, IOException, ParseException {
        Document doc = createGeoDoc("Stafnsvötn", 65.18518697818304d, -18.776578903198242d, 65.18614154408306d, -18.773725032806396d);
        SearchResult<Resource> result = performGeoSearch(63.35212928507874d, -24.345703125d, 66.8265202749748d, -13.271484375d);
        assertTrue(result.getResults().contains(doc));
    }

    @Test
    @Rollback
    public void testLatLongSearchOfBoxCoveringEntireItem()
            throws InstantiationException, IllegalAccessException, SolrServerException, IOException, ParseException {
        LatitudeLongitudeBox latitudeLongitudeBoxOfItem = new LatitudeLongitudeBox(0.0, 0.0, 10.0, 10.0);
        LatitudeLongitudeBox latitudLongitudeBoxOfQuery = new LatitudeLongitudeBox(1.0, 1.0, 9.0, 9.0);
        testItemIsFoundByBox(latitudeLongitudeBoxOfItem, latitudLongitudeBoxOfQuery);
    }

    @Test
    @Rollback
    public void testLatLongSearchOfBoxCoveringEntireItem2()
            throws InstantiationException, IllegalAccessException, SolrServerException, IOException, ParseException {
        LatitudeLongitudeBox latitudeLongitudeBoxOfItem = new LatitudeLongitudeBox(-173.237, 54.632, -129.98, 71.441);
        LatitudeLongitudeBox latitudLongitudeBoxOfQuery = new LatitudeLongitudeBox(-173.14453125, 55.92458580482951, -139.833984375, 71.35706654962706);
        testItemIsFoundByBox(latitudeLongitudeBoxOfItem, latitudLongitudeBoxOfQuery);
    }

    @Test
    @Rollback
    public void testLatLongSearchOfBoxCoveringPartOfItem()
            throws InstantiationException, IllegalAccessException, SolrServerException, IOException, ParseException {
        LatitudeLongitudeBox latitudeLongitudeBoxOfItem = new LatitudeLongitudeBox(0.0, 0.0, 10.0, 10.0);
        LatitudeLongitudeBox latitudLongitudeBoxOfQuery = new LatitudeLongitudeBox(1.0, 1.0, 12.0, 12.0);
        testItemIsFoundByBox(latitudeLongitudeBoxOfItem, latitudLongitudeBoxOfQuery);
    }

    @Test
    @Rollback
    public void testLatLongSearchOfBoxCoveringPartOfItem2()
            throws InstantiationException, IllegalAccessException, SolrServerException, IOException, ParseException {
        LatitudeLongitudeBox latitudeLongitudeBoxOfItem = new LatitudeLongitudeBox(2.0, 2.0, 10.0, 10.0);
        LatitudeLongitudeBox latitudLongitudeBoxOfQuery = new LatitudeLongitudeBox(1.0, 1.0, 9.0, 9.0);
        testItemIsFoundByBox(latitudeLongitudeBoxOfItem, latitudLongitudeBoxOfQuery);
    }

    private void testItemIsFoundByBox(LatitudeLongitudeBox latitudeLongitudeBoxOfItem, LatitudeLongitudeBox latitudLongitudeBoxOfQuery)
            throws InstantiationException,
            IllegalAccessException, SolrServerException, IOException, ParseException {
        Document file = createAndSaveNewInformationResource(Document.class);
        file.setLatitudeLongitudeBox(latitudeLongitudeBoxOfItem);
        genericService.saveOrUpdate(file);
        genericService.saveOrUpdate(latitudeLongitudeBoxOfItem);
        searchIndexService.index(file);

        SpatialQueryPart spatialQueryPart = new SpatialQueryPart(latitudeLongitudeBoxOfItem);
        SpatialQueryPart searchPart = new SpatialQueryPart(latitudLongitudeBoxOfQuery);

        ResourceQueryBuilder rqb = new ResourceQueryBuilder();
        rqb.append(searchPart);
        SearchResult<Resource> result = new SearchResult<>();
        searchService.handleSearch(rqb, result, MessageHelper.getInstance());
        assertTrue(result.getResults().contains(file));
    }

}
