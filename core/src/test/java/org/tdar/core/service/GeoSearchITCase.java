package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import javax.sql.DataSource;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword.Level;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.geosearch.GeoSearchDao;
import org.tdar.search.geosearch.GeoSearchService;

public class GeoSearchITCase extends AbstractIntegrationTestCase {

    @Autowired
    private GeoSearchService geoSearchService;

    @Autowired
    private GeoSearchDao geoSearchDao;

    @Autowired
    private ResourceService resourceService;
    @Autowired
    @Qualifier("tdarGeoDataSource")
    private DataSource dataSource;

    /*
     * useful for visualizing WKTs
     * http://dev.openlayers.org/releases/OpenLayers-2.4/examples/wkt.html
     */

    @Test
    @Rollback(true)
    public void testInvalidLatLong() {
        LatitudeLongitudeBox latLong = new LatitudeLongitudeBox(1d, 1d, -1d, -1d);
        assertFalse(latLong.isValid());
    }

    @Test
    @Rollback(true)
    public void testQuietRun() {
        assertNotNull(geoSearchService);
        geoSearchDao.setDataSource(new SingleConnectionDataSource("jdbc:postgresql://localhost/postgis_broken", true));
        LatitudeLongitudeBox latLongBox = constructLatLongBox();
        Set<GeographicKeyword> countryInfo = geoSearchService.extractCountryInfo(latLongBox);
        assertNotNull(countryInfo);
        assertTrue(countryInfo.size() == 0);
        assertFalse(geoSearchDao.isEnabled());
        // reset after breaking the connection
        geoSearchDao.setDataSource(dataSource);
    }

    @Test
    @Rollback
    public void testNoCounties() {
        LatitudeLongitudeBox latLong = new LatitudeLongitudeBox();
        latLong.setMinimumLatitude(37.222d);
        latLong.setMaximumLatitude(38.865d);
        latLong.setMaximumLongitude(-121.31d);
        latLong.setMinimumLongitude(-124.43d);
        Set<GeographicKeyword> countryInfo = geoSearchService.extractAllGeographicInfo(latLong);
        for (GeographicKeyword kwd : countryInfo) {
            logger.info("{}", kwd);
            assertFalse(kwd.getLabel().contains("County"));
        }
    }

    @Test
    @Rollback(true)
    public void testCountrySearch() {
        LatitudeLongitudeBox latLongBox = constructLatLongBox();
        Set<GeographicKeyword> countryInfo = geoSearchService.extractCountryInfo(latLongBox);
        boolean found = false;
        logger.info("{}", countryInfo);
        for (GeographicKeyword kwd : countryInfo) {
            logger.debug("{}", kwd);
            if (kwd.getLabel().contains("United States")) {
                found = true;
            }
        }
        if (!geoSearchService.isEnabled()) {
            return;
        }
        assertTrue("country search found item not in US", found);
    }

    @Test
    @Rollback(true)
    @Ignore("FIXME: indexing")
    // FIXME: This test tests too many pointless things, but it's the only thing that covers processManagedKeywords. Remove this
    // once we have a proper test for processManagedKeywords.
    public void testPersistedManagedKeyword() {
        Project project = genericService.find(Project.class, 3738L);
        Set<LatitudeLongitudeBox> latitudeLongitudeBoxes = project.getLatitudeLongitudeBoxes();
        project.getManagedGeographicKeywords().clear();
        genericService.save(project);
        assertNotNull(latitudeLongitudeBoxes);
        assertEquals("managed keywords (expected 0)", 0, project.getManagedGeographicKeywords().size());
        resourceService.processManagedKeywords(project, latitudeLongitudeBoxes);
        LatitudeLongitudeBox latLong = latitudeLongitudeBoxes.iterator().next();
        assertNotNull(latLong);
        Set<GeographicKeyword> extractedGeoInfo = project.getManagedGeographicKeywords();
        if (!geoSearchService.isEnabled()) {
            assertTrue(0 == extractedGeoInfo.size());
            return;
        }
        assertFalse(0 == extractedGeoInfo.size());
        int fnd = 0;
        logger.info("{}", extractedGeoInfo);
        for (GeographicKeyword kwd : extractedGeoInfo) {
            assertFalse(kwd.getLabel().contains("Asia"));

            if (kwd.getLabel().contains("Virginia") || kwd.getLabel().contains("Alexandria")) {
                fnd++;
            }
        }
        assertEquals("expected 3 (1 for US, Virginia, Alexandria) " + fnd, 2, fnd);
        genericService.saveOrUpdate(project);
        project = null;

//        Project project2 = genericService.find(Project.class, 3738L);
//        logger.info("{}", project2.getManagedGeographicKeywords());
//        assertEquals("managed keywords (expected " + extractedGeoInfo.size() + ")", extractedGeoInfo.size(), project2.getManagedGeographicKeywords().size());
//        searchIndexService.flushToIndexes();
//        QueryBuilder q = new ResourceQueryBuilder();
//
//        FieldQueryPart<ResourceType> qp = new FieldQueryPart<ResourceType>("resourceType", ResourceType.PROJECT);
//        q.append(qp);
//
//        GeneralSearchResourceQueryPart ft = new GeneralSearchResourceQueryPart("Virginia");
//        q.append(ft);
//
//        FullTextQuery ftq = searchService.search(q);
//        int totalRecords = ftq.getResultSize();
//        ftq.setFirstResult(0);
//        ftq.setMaxResults(100);
//        List<Project> projects = ftq.list();
//        logger.info("found: " + totalRecords);
//        boolean found = false;
//        for (Project p : projects) {
//            logger.info("{}", p);
//            if (p.getId().equals(project2.getId())) {
//                found = true;
//            }
//        }
//        assertTrue("managed geo keywords found in index", found);

    }

    @Test
    @Rollback
    public void testFipsSearch() {
        String fips = "02090";
        LatitudeLongitudeBox latLong = geoSearchDao.extractLatLongFromFipsCode(fips);
        if (!geoSearchService.isEnabled()) {
            return;
        }
        assertNotNull(latLong);
        Set<GeographicKeyword> extractAllGeographicInfo = geoSearchService.extractAllGeographicInfo(latLong);
        extractAllGeographicInfo.addAll(geoSearchService.extractCountyInfo(latLong));
        boolean found = false;
        for (GeographicKeyword kwd : extractAllGeographicInfo) {
            logger.debug("{}", kwd);
            if (kwd.getLabel().contains("Fairbanks")) {
                found = true;
            }
        }
        assertTrue("should have found fairbanks in the fips lookup", found);
        logger.info("{}", extractAllGeographicInfo);
    }

    @Test
    @Rollback
    public void testFipsSearch2() {
        String[] fips = { "66999" };
        LatitudeLongitudeBox latLong = geoSearchDao.extractLatLongFromFipsCode(fips);
        if (!geoSearchService.isEnabled()) {
            return;
        }
        Set<GeographicKeyword> extractAllGeographicInfo = geoSearchService.extractAllGeographicInfo(latLong);
        boolean found = false;
        logger.info("{}", latLong);
        for (GeographicKeyword kwd : extractAllGeographicInfo) {
            if (kwd.getLabel().contains("Guam")) {
                found = true;
            }
            logger.info("{}", kwd.getLabel());
        }
        assertTrue("Should have found Guam", found);
    }

    // @Test
    /*
     * This test fails because the FIPS CODE looks across the dateline and produces the following bounding box:
     * POLYGON((-171.14192385899992 -14.601806034999925,-171.14192385899992 20.616560613000047,146.15441286700002 20.616560613000047,146.15441286700002
     * -14.601806034999925,-171.14192385899992 -14.601806034999925))
     * see: http://dev.openlayers.org/releases/OpenLayers-2.4/examples/wkt.html
     * 
     * The issue is 'less' of an issue because any lat/long box that tDAR would produce would split this box into two bounding boxes that cross
     * the Dateline/AntiMeridian instead of what this is doing.
     */
    @Rollback
    public void testFipsSearchAcrossDateline() {
        String[] fips = { "66999", "69999", "60999" };
        LatitudeLongitudeBox latLong = geoSearchDao.extractLatLongFromFipsCode(fips);
        logger.info("{}", latLong);
        if (!geoSearchService.isEnabled()) {
            return;
        }
        assertNotNull(latLong);
        Set<GeographicKeyword> extractAllGeographicInfo = geoSearchService.extractAllGeographicInfo(latLong);
        boolean found = false;
        boolean found2 = false;
        // logger.info("{}",extractAllGeographicInfo);
        for (GeographicKeyword kwd : extractAllGeographicInfo) {
            if (kwd.getLevel() == Level.COUNTRY) {
                logger.info("{}", kwd);
            }
            // if (kwd.getLabel().contains("Guam"))
            // found3 = true;

            if (kwd.getLabel().contains("Samoa")) {
                found = true;
            }
            if (kwd.getLabel().contains("Belize")) {
                found2 = true;
            }
        }
        assertTrue("should have found Milne in the fips", found);
        // assertTrue("should have found Guam in the fips", found3);
        assertFalse("should not have found belize in the fips", found2);
    }

    @Test
    @Rollback
    public void testFipsExpandedSearch() {
        String fips = "02999";
        LatitudeLongitudeBox latLong = geoSearchDao.extractLatLongFromFipsCode(fips);
        if (!geoSearchService.isEnabled()) {
            return;
        }
        assertNotNull(latLong);
        Set<GeographicKeyword> extractAllGeographicInfo = geoSearchService.extractAllGeographicInfo(latLong);
        int found = 0;
        for (GeographicKeyword kwd : extractAllGeographicInfo) {
            if (kwd.getLabel().contains("Russia") || kwd.getLabel().contains("Canada") || kwd.getLabel().contains("United States")) {
                found++;
            }
        }
        // assertTrue(latLong.getMinimumLatitude().equals(62.73741335487087));
        // assertTrue(latLong.getMaximumLatitude().equals(64.37669436945947));
        // assertTrue(latLong.getMinimumLongitude().equals(-152.9894742378389));
        // assertTrue(latLong.getMaximumLongitude().equals(-146.95735023930874));
        logger.info("{}", extractAllGeographicInfo);
        assertEquals("should have found US only", 1, found);
    }

    @Test
    @Rollback
    public void testAdminSearch() {
        if (geoSearchService.isEnabled()) {
            LatitudeLongitudeBox latLongBox = constructLatLongBox();
            Set<GeographicKeyword> adminInfo = geoSearchService.extractAdminInfo(latLongBox);
            boolean found = false;
            for (GeographicKeyword kwd : adminInfo) {
                logger.debug("{}", kwd);
                if (kwd.getLabel().contains("California")) {
                    found = true;
                }
            }
            assertTrue("country search found item not in California", found);
        }
    }

    private LatitudeLongitudeBox constructLatLongBox() {
        LatitudeLongitudeBox latLongBox = new LatitudeLongitudeBox();
        latLongBox.setMinimumLatitude(37.7129);
        latLongBox.setMinimumLongitude(-122.5240);
        latLongBox.setMaximumLatitude(37.8968);
        latLongBox.setMaximumLongitude(-122.0416);
        return latLongBox;
    }

    @Test
    @Rollback
    @Ignore
    /* THIS TEST IS NOT VALID BECAUSE OF THE VALIDATION RULES ON LATLONGBOX, but it is, nonetheless, a good test to document */
    public void testMicronesia() {
        LatitudeLongitudeBox latLong = new LatitudeLongitudeBox(-171.142, -14.602, 146.154, 20.617);

        Set<GeographicKeyword> extractAllGeographicInfo = geoSearchService.extractAllGeographicInfo(latLong);
        int found = 0;
        logger.debug(extractAllGeographicInfo.size() + " geographic terms being returned {}", extractAllGeographicInfo);
        for (GeographicKeyword kwd : extractAllGeographicInfo) {
            if (kwd.getLabel().contains("ML (") || kwd.getLabel().contains("GY (") || kwd.getLabel().contains("TZ (")) {
                found++;
            }
        }
        assertEquals("Should not find africa for search for Micronesia", 0, found);
    }
}
