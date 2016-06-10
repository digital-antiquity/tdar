package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.ResourceAnnotationType;
import org.tdar.search.index.LookupSource;
import org.tdar.struts.action.AbstractIntegrationControllerTestCase;
import org.tdar.struts.action.lookup.ResourceAnnotationKeyLookupAction;

public class AnnotationKeyLookupControllerITCase extends AbstractIntegrationControllerTestCase {

    private ResourceAnnotationKeyLookupAction controller;

    @Before
    public void initController() {
        controller = generateNewInitializedController(ResourceAnnotationKeyLookupAction.class);
        controller.setRecordsPerPage(99);
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

        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE_ANNOTATION_KEY);
        controller.setTerm("IS");
        controller.lookupAnnotationKey();
        List<ResourceAnnotationKey> resources = controller.getResults();
        assertTrue("at least one document", resources.size() == 2);

        // FIXME: not properly simulating new page request
        controller.setTerm("ZZ");
        controller.lookupAnnotationKey();
        resources = controller.getResults();
        assertEquals("ZZ should return no results", 0, resources.size());
    }
}
