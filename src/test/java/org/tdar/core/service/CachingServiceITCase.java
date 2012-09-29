/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.utils.Pair;

/**
 * @author Adam Brin
 * 
 */
public class CachingServiceITCase extends AbstractControllerITCase {

    @Autowired
    SimpleCachingService simpleCachingService;

    @Autowired
    CodingSheetService codingSheetService;

    public Long countActive() {
        int count = 0;
        for (CodingSheet sheet : codingSheetService.findAll()) {
            if (sheet.getStatus().equals(Status.ACTIVE))
                count++;
        }
        return new Long(count);
    }

    @Test
    @Rollback
    public void testCachingService() throws InstantiationException, IllegalAccessException {
        Long count = countActive();
        Map<ResourceType, Pair<Long, Double>> resourceCount = simpleCachingService.getHomepageCache().getResourceCount();
        assertEquals(count, resourceCount.get(ResourceType.CODING_SHEET).getFirst());

        createAndSaveNewInformationResource(CodingSheet.class);
        assertEquals(count, resourceCount.get(ResourceType.CODING_SHEET).getFirst());

        simpleCachingService.taintAll();
        resourceCount = simpleCachingService.getHomepageCache().getResourceCount();
        assertFalse(count.equals(resourceCount.get(ResourceType.CODING_SHEET).getFirst()));

        assertEquals(count.longValue() + 1l, resourceCount.get(ResourceType.CODING_SHEET).getFirst().longValue());

    }

    @Test
    @Rollback
    @SuppressWarnings("static-access")
    public void testCachingService2() throws InstantiationException, IllegalAccessException {
        simpleCachingService.taintAll();
        Number count = countActive();
        Map<ResourceType, Pair<Long, Double>> resourceCount = simpleCachingService.getHomepageCache().getResourceCount();
        assertEquals(count, resourceCount.get(ResourceType.CODING_SHEET).getFirst());

        createAndSaveNewInformationResource(CodingSheet.class);
        assertEquals(count, resourceCount.get(ResourceType.CODING_SHEET).getFirst());

        simpleCachingService.taintAllAndRebuild();
        resourceCount = simpleCachingService.getHomepageCache().getResourceCount();
        assertFalse(count.equals(resourceCount.get(ResourceType.CODING_SHEET).getFirst()));

        assertEquals(count.longValue() + 1l, resourceCount.get(ResourceType.CODING_SHEET).getFirst().longValue());

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.struts.action.AbstractControllerITCase#getController()
     */
    @Override
    protected TdarActionSupport getController() {
        return null;
    }
}
