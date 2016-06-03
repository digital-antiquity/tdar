package org.tdar.core;

import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.FeedSearchHelper;
import org.tdar.core.service.ImportService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.ReflectionService;
import org.tdar.core.service.SerializationService;
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
    ImportService importService;

    @Autowired
    ObfuscationService obfuscationService;

    @Test
    @Rollback
    public void testJsonSearchExport() throws Exception {
        FeedSearchHelper feedHelper = setupTest();
        String result = serializationService.createGeoJsonFromResourceList(feedHelper);
        logger.info(result);
        
        assertFalse(result.contains("\"activeMaterialKeywords\":null"));
        assertFalse(result.contains("minLatitude"));
        assertFalse(result.contains("maxLatitude"));
    }

    private FeedSearchHelper setupTest(Resource... resources) throws InstantiationException, IllegalAccessException {
        Document document = generateDocumentWithFileAndUseDefaultUser();
        document.getProject().getCultureKeywords().add(new CultureKeyword(NABATAEAN));
        document.setInheritingCulturalInformation(true);
        Project project = genericService.find(Project.class, 3805l);
        project.getCultureKeywords().add(new CultureKeyword(BEDOUIN));
        List<Resource> list = new ArrayList<>();
        list.add(document);
        list.add(project);
        if (ArrayUtils.isNotEmpty(resources)) {
            list.addAll(Arrays.asList(resources));
        }
        // map, null, false, JsonLookupFilter.class, null
        BaseSearchResult<Resource> handler = new BaseSearchResult<>();
        handler.setResults(list);
        FeedSearchHelper feedHelper = new FeedSearchHelper("http://www.test.com", handler, null, null);
        return feedHelper;
    }

}
