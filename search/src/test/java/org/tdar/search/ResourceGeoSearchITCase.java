package org.tdar.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.exception.SearchException;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.geosearch.GeoSearchService;
import org.tdar.search.query.LuceneSearchResultHandler;
import org.tdar.search.query.SearchResult;
import org.tdar.search.service.query.SearchService;
import org.tdar.utils.MessageHelper;

public class ResourceGeoSearchITCase extends AbstractResourceSearchITCase {

    @Autowired
    GenericService genericService;
    @Autowired
    SearchService<Resource> searchService;

    @Autowired
    GeoSearchService geoSearchService;

    @Autowired
    ResourceService resourceService;

    @Test
    @Rollback(true)
    // FIXME: This test tests too many pointless things, but it's the only thing that covers processManagedKeywords. Remove this
    // once we have a proper test for processManagedKeywords.
    public void testPersistedManagedKeyword() throws IOException,SearchException, SearchIndexException {
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

        Project project2 = genericService.find(Project.class, 3738L);
        logger.info("{}", project2.getManagedGeographicKeywords());
        assertEquals("managed keywords (expected " + extractedGeoInfo.size() + ")", extractedGeoInfo.size(), project2.getManagedGeographicKeywords().size());

        AdvancedSearchQueryObject asqo2 = new AdvancedSearchQueryObject();
        asqo.setAllGeneralQueryFields(Arrays.asList("Virginia"));
        LuceneSearchResultHandler<Resource> result = new SearchResult<>();
        result.setRecordsPerPage(1000);
        resourceSearchService.buildAdvancedSearch(asqo2, getAdminUser(), result, MessageHelper.getInstance());
        List<Resource> projects = result.getResults();
        logger.info("found: " + result.getTotalRecords());
        boolean found = false;
        for (Resource p : projects) {
            logger.info("{}", p);
            if (p.getId().equals(project2.getId())) {
                found = true;
            }
        }
        assertTrue("managed geo keywords found in index", found);

    }

}
