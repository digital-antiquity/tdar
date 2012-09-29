package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.lucene.queryParser.ParseException;
import org.hibernate.search.FullTextQuery;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword.Level;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.keyword.GeographicKeywordService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.geosearch.GeoSearchService;
import org.tdar.search.query.FieldQueryPart;
import org.tdar.search.query.FreetextQueryPart;
import org.tdar.search.query.QueryBuilder;
import org.tdar.search.query.ResourceQueryBuilder;
import org.tdar.struts.action.AbstractAdminControllerITCase;
import org.tdar.struts.action.TdarActionSupport;

public class GeoSearchITCase extends AbstractAdminControllerITCase {

    @Autowired
    private GeoSearchService geoSearchService;
    @Autowired
    private GeographicKeywordService geographicKeywordService;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    @Qualifier("tdarGeoDataSource")
    private DataSource dataSource;

    @Test
    @Rollback(true)
    public void testQuietRun() {
        assertNotNull(geoSearchService);
        geoSearchService.setDataSource(new SingleConnectionDataSource("jdbc:postgresql://localhost/postgis_broken", true));
        LatitudeLongitudeBox latLongBox = constructLatLongBox();
        Set<GeographicKeyword> countryInfo = geoSearchService.extractCountryInfo(latLongBox);
        assertNotNull(countryInfo);
        assertTrue(countryInfo.size() == 0);
        assertFalse(geoSearchService.isEnabled());
        // reset after breaking the connection
        geoSearchService.setDataSource(dataSource);
    }

    @Test
    @Rollback
    public void testNoCounties() {
        LatitudeLongitudeBox latLong = new LatitudeLongitudeBox();
        latLong.setMinimumLatitude(37.222d);
        latLong.setMinimumLongitude(-121.31d);
        latLong.setMaximumLatitude(38.865d);
        latLong.setMaximumLongitude(-124.43d);
        Set<GeographicKeyword> countryInfo = geoSearchService.extractAllGeographicInfo(latLong);
        for (GeographicKeyword kwd : countryInfo) {
            logger.info("{}", kwd);
            assertFalse(kwd.getLabel().contains("County"));
        }
    }

    @Override
    protected TdarActionSupport getController() {
        return null;
    }

    @Test
    @Rollback(true)
    public void testCountrySearch() {
        LatitudeLongitudeBox latLongBox = constructLatLongBox();
        Set<GeographicKeyword> countryInfo = geoSearchService.extractCountryInfo(latLongBox);
        boolean found = false;
        for (GeographicKeyword kwd : countryInfo) {
            logger.debug("{}", kwd);
            if (kwd.getLabel().contains("US"))
                found = true;
        }
        if (!geoSearchService.isEnabled())
            return;
        assertTrue("country search found item not in US", found);
    }

    @SuppressWarnings("unchecked")
    @Test
    @Rollback(true)
    public void testPersistedManagedKeyword() throws ParseException {
        Project project = genericService.find(Project.class, 3738L);
        Set<LatitudeLongitudeBox> latitudeLongitudeBoxes = project.getLatitudeLongitudeBoxes();
        project.setManagedGeographicKeywords(Collections.EMPTY_SET);
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
            if (kwd.getLabel().contains("US") || kwd.getLabel().contains("Virginia") || kwd.getLabel().contains("Fairfax"))
                fnd++;
        }
        assertEquals("expected 3 (1 for US, Virginia, Maryland) " + fnd, 3, fnd);
        genericService.saveOrUpdate(project);
        project = null;

        Project project2 = genericService.find(Project.class, 3738L);
        logger.info("{}", project2.getManagedGeographicKeywords());
        assertEquals("managed keywords (expected " + extractedGeoInfo.size() + ")", extractedGeoInfo.size(), project2.getManagedGeographicKeywords().size());

        searchIndexService.indexAll(Resource.class);
        QueryBuilder q = new ResourceQueryBuilder();

        FieldQueryPart qp = new FieldQueryPart();
        qp.setFieldName("resourceType");
        qp.setFieldValue("PROJECT");
        q.append(qp);

        FreetextQueryPart ft = new FreetextQueryPart();
        ft.setFieldValue("Virginia");
        q.append(ft);

        FullTextQuery ftq = searchService.search(q);
        int totalRecords = ftq.getResultSize();
        ftq.setFirstResult(0);
        ftq.setMaxResults(100);
        List<Project> projects = (List<Project>) ftq.list();
        logger.info("found: " + totalRecords);
        boolean found = false;
        for (Project p : projects) {
            logger.info("{}", p);
            if (p.getId().equals(project2.getId()))
                found = true;
        }
        assertTrue("managed geo keywords found in index", found);

    }

    @Test
    @Rollback
    public void testFipsSearch() {
        String fips = "02090";
        LatitudeLongitudeBox latLong = geoSearchService.extractLatLongFromFipsCode(fips);
        if (!geoSearchService.isEnabled())
            return;
        assertNotNull(latLong);
        Set<GeographicKeyword> extractAllGeographicInfo = geoSearchService.extractAllGeographicInfo(latLong);
        extractAllGeographicInfo.addAll(geoSearchService.extractCountyInfo(latLong));
        boolean found = false;
        for (GeographicKeyword kwd : extractAllGeographicInfo) {
            logger.debug("{}", kwd);
            if (kwd.getLabel().contains("Fairbanks"))
                found = true;
        }
        assertTrue("should have found fairbanks in the fips lookup", found);
        logger.info("{}", extractAllGeographicInfo);
    }

    @Test
    @Rollback
    public void testFipsSearch2() {
        String[] fips = { "66999" };
        LatitudeLongitudeBox latLong = geoSearchService.extractLatLongFromFipsCode(fips);
        if (!geoSearchService.isEnabled()) {
            return;
        }
        Set<GeographicKeyword> extractAllGeographicInfo = geoSearchService.extractAllGeographicInfo(latLong);
        boolean found = false;
        logger.info("{}", latLong);
        for (GeographicKeyword kwd : extractAllGeographicInfo) {
            if (kwd.getLabel().contains("Guam"))
                found = true;
            logger.info("{}", kwd.getLabel());
        }
        assertTrue("Should have found Guam", found);
    }

    @Test
    @Rollback
    public void testFipsSearchAcrossDateline() {
        String[] fips = { "66999", "69999", "60999" };
        LatitudeLongitudeBox latLong = geoSearchService.extractLatLongFromFipsCode(fips);
        logger.info("{}", latLong);
        if (!geoSearchService.isEnabled())
            return;
        assertNotNull(latLong);
        Set<GeographicKeyword> extractAllGeographicInfo = geoSearchService.extractAllGeographicInfo(latLong);
        boolean found = false;
        boolean found2 = false;
        boolean found3 = false;
        // logger.info("{}",extractAllGeographicInfo);
        for (GeographicKeyword kwd : extractAllGeographicInfo) {
            if (kwd.getLevel() == Level.COUNTRY) {
                logger.info("{}", kwd);
            }
            // if (kwd.getLabel().contains("Guam"))
            // found3 = true;

            if (kwd.getLabel().contains("Samoa"))
                found = true;
            if (kwd.getLabel().contains("Belize"))
                found2 = true;
        }
        assertTrue("should have found Milne in the fips", found);
        // assertTrue("should have found Guam in the fips", found3);
        assertFalse("should not have found belize in the fips", found2);
    }

    @Test
    @Rollback
    public void testFipsExpandedSearch() {
        String fips = "02999";
        LatitudeLongitudeBox latLong = geoSearchService.extractLatLongFromFipsCode(fips);
        if (!geoSearchService.isEnabled())
            return;
        assertNotNull(latLong);
        Set<GeographicKeyword> extractAllGeographicInfo = geoSearchService.extractAllGeographicInfo(latLong);
        int found = 0;
        for (GeographicKeyword kwd : extractAllGeographicInfo) {
            if (kwd.getLabel().contains("RU (") || kwd.getLabel().contains("CA (") || kwd.getLabel().contains("US ("))
                found++;
        }
        // assertTrue(latLong.getMinimumLatitude().equals(62.73741335487087));
        // assertTrue(latLong.getMaximumLatitude().equals(64.37669436945947));
        // assertTrue(latLong.getMinimumLongitude().equals(-152.9894742378389));
        // assertTrue(latLong.getMaximumLongitude().equals(-146.95735023930874));
        assertEquals("should have found US, Russia, and Canada", 3, found);
        logger.info("{}", extractAllGeographicInfo);
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
                if (kwd.getLabel().contains("California"))
                    found = true;
            }
            assertTrue("country search found item not in California", found);
        }
    }

    private LatitudeLongitudeBox constructLatLongBox() {
        LatitudeLongitudeBox latLongBox = new LatitudeLongitudeBox();
        latLongBox.setMaximumLatitude(37.8968);
        latLongBox.setMinimumLatitude(37.7129);
        latLongBox.setMaximumLongitude(-122.0416);
        latLongBox.setMinimumLongitude(-122.5240);
        return latLongBox;
    }

}
