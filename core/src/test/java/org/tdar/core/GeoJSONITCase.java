package org.tdar.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.FeedSearchHelper;
import org.tdar.core.service.ImportService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.ReflectionService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.search.query.BaseSearchResult;

import edu.emory.mathcs.backport.java.util.Arrays;

public class GeoJSONITCase extends AbstractIntegrationTestCase {

    private static final String BEDOUIN = "bedouin";
    private static final String NABATAEAN = "Nabataean";

    @Autowired
    SerializationService serializationService;

    @Autowired
    ReflectionService reflectionService;
    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    ImportService importService;

    @Autowired
    ObfuscationService obfuscationService;

    @Test
    @Rollback
    public void testJsonSearchExport() throws Exception {
        Resource[] setupDocs = setupDocs();
        FeedSearchHelper feedHelper = setupTest(null, setupDocs);
        String result = serializationService.createGeoJsonFromResourceList(feedHelper);
        logger.info(result);
        feedHelper.setOverrideAndObfuscate(true);
        String result2 = serializationService.createGeoJsonFromResourceList(feedHelper);
        logger.info(result2);
        String r1 = getCoordinatesBlock(result);
        String r2 = getCoordinatesBlock(result2);
        logger.debug(r1);
        logger.debug(r2);
        assertFalse(result.contains("\"activeMaterialKeywords\":null"));
        // test basic obfuscation

        assertObfuscated(setupDocs[1], result, r1, true);
        assertObfuscated(setupDocs[1], result, r2, true);
        // test override obfuscation
        assertEquals("should be same (enforcing boolean default)", r1, r2);
    }

    @SuppressWarnings("deprecation")
    private void assertObfuscated(Resource res, String result, String r1, boolean obfuscated) {
        Double minLatitude = res.getFirstActiveLatitudeLongitudeBox().getSouth();
        Double maxLatitude = res.getFirstActiveLatitudeLongitudeBox().getNorth();
        logger.debug("minLat: {}, maxLat:{}", minLatitude, maxLatitude);
        logger.debug(r1);
        if (obfuscated) {
            assertFalse(result.contains(minLatitude.toString()));
            assertFalse(result.contains(maxLatitude.toString()));
        } else {
            assertTrue("Should see unobfuscated lat", result.contains(minLatitude.toString()));
            assertTrue("Should see unobfuscated lat", result.contains(maxLatitude.toString()));
        }
    }

    @SuppressWarnings("deprecation")
    private Resource[] setupDocs() throws InstantiationException, IllegalAccessException {
        Resource[] resources = new Resource[2];
        Document document = generateDocumentWithFileAndUseDefaultUser();
        document.getProject().getCultureKeywords().add(new CultureKeyword(NABATAEAN));
        document.setInheritingCulturalInformation(true);
        // PROJECT LAT/LONG should be obfuscated
        Project project = genericService.find(Project.class, 3805l);
        LatitudeLongitudeBox llb = project.getFirstActiveLatitudeLongitudeBox();
        llb.setNorth(llb.getSouth());
        llb.setEast(llb.getWest());
        project.getCultureKeywords().add(new CultureKeyword(BEDOUIN));
        resources[1] = project;
        resources[0] = document;
        return resources;
    }

    @Test
    @Rollback
    public void testObfuscationOff() throws InstantiationException, IllegalAccessException, IOException {
        Resource[] setupDocs = setupDocs();
        FeedSearchHelper feedHelper = setupTest(getAdminUser(), setupDocs);
        feedHelper.setOverrideAndObfuscate(false);
        String result = serializationService.createGeoJsonFromResourceList(feedHelper);
        logger.info(result);
        String r1 = getCoordinatesBlock(result);

        assertObfuscated(setupDocs[1], result, r1, false);
    }

    private String getCoordinatesBlock(String result) {
        return StringUtils.substringBetween(result, "coordinates", "}");
    }

    @SuppressWarnings("unchecked")
    private FeedSearchHelper setupTest(TdarUser user, Resource... resources) throws InstantiationException, IllegalAccessException {
        List<Resource> list = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(resources)) {
            list.addAll(Arrays.asList(resources));
        }
        list.forEach(item -> authorizationService.applyTransientViewableFlag(item, user));

        BaseSearchResult<Resource> handler = new BaseSearchResult<>();
        handler.setResults(list);
        FeedSearchHelper feedHelper = new FeedSearchHelper("http://www.test.com", handler, null, user);
        return feedHelper;
    }

}
