package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.ResourceAnnotationType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.query.SortOption;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.resource.ProjectController;

public class KeywordAnnotationLookupControllerITCase extends AbstractIntegrationTestCase {

    private LookupController controller;

    @Before
    public void initController() {
        controller = generateNewInitializedController(LookupController.class);
        controller.setRecordsPerPage(99);
    }

    @Test
    public void testKeywordLookup() {
        searchIndexService.indexAll(getAdminUser(), GeographicKeyword.class, CultureKeyword.class);
        controller.setKeywordType("culturekeyword");
        controller.setTerm("Folsom");
        controller.lookupKeyword();
        List<Indexable> resources = controller.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

    @Test
    public void testAnnotationLookup() {
        ResourceAnnotationKey key = new ResourceAnnotationKey();
        key.setKey("ISSN");
        key.setResourceAnnotationType(ResourceAnnotationType.IDENTIFIER);
        genericService.save(key);
        ResourceAnnotationKey key2 = new ResourceAnnotationKey();
        key2.setKey("ISBN");
        key2.setResourceAnnotationType(ResourceAnnotationType.IDENTIFIER);
        genericService.save(key2);

        searchIndexService.indexAll(getAdminUser(), ResourceAnnotationKey.class);
        controller.setTerm("IS");
        controller.lookupAnnotationKey();
        List<Indexable> resources = controller.getResults();
        assertTrue("at least one document", resources.size() == 2);

        // FIXME: not properly simulating new page request
        controller.setTerm("ZZ");
        controller.lookupAnnotationKey();
        resources = controller.getResults();
        assertEquals("ZZ should return no results", 0, resources.size());
    }
}
