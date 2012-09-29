/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword.Level;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.util.HomepageGeographicKeywordCache;
import org.tdar.core.bean.util.HomepageResourceCountCache;
import org.tdar.core.service.processes.RebuildHomepageCache;
import org.tdar.core.service.resource.CodingSheetService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.TdarActionSupport;

/**
 * @author Adam Brin
 * 
 */
public class CachingServiceITCase extends AbstractControllerITCase {

    @Autowired
    ResourceService resourceService;

    @Autowired
    RebuildHomepageCache cache;

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
    public void testCachingService() throws Exception {
        Long count = countActive();
        cache.processBatch(Arrays.asList(-1L));
        Long count_ = -1l;
        count_ = updateCodingSheetCount(genericService.findAll(HomepageResourceCountCache.class));
        assertEquals(count, count_);

        createAndSaveNewInformationResource(CodingSheet.class, false);
        count_ = updateCodingSheetCount(genericService.findAll(HomepageResourceCountCache.class));
        assertEquals(count, count_);

        cache.processBatch(Arrays.asList(-1L));
        count_ = updateCodingSheetCount(genericService.findAll(HomepageResourceCountCache.class));
        assertFalse(count.equals(count_));

        assertEquals(count.longValue(), count_ - 1l);

    }

    private Long updateCodingSheetCount(List<HomepageResourceCountCache> resourceCounts) {
        Long count_ = -1L;
        for (HomepageResourceCountCache cache_ : resourceCounts) {
            if (cache_.getResourceType() == ResourceType.CODING_SHEET) {
                count_ = cache_.getCount();
            }
        }
        return count_;
    }

    @Test
    @Rollback
    public void testCachingServiceGeo() throws Exception {
        cache.processBatch(Arrays.asList(-1L));
        List<HomepageGeographicKeywordCache> findAll = genericService.findAll(HomepageGeographicKeywordCache.class);
        final Long count = new Long(findAll.size());
        Long count_ = count;
        CodingSheet cs = createAndSaveNewInformationResource(CodingSheet.class, false);
        GeographicKeyword mgc = new GeographicKeyword();
        mgc.setLabel("test");
        mgc.setLevel(Level.ISO_COUNTRY);
        genericService.save(mgc);
        cs.getManagedGeographicKeywords().add(mgc);
        genericService.saveOrUpdate(cs);
        genericService.synchronize();
        count_ = new Long(findAll.size());
        assertEquals(count, count_);
        logger.info("list: {} ", findAll);
        cache.processBatch(Arrays.asList(-1L));
        findAll = genericService.findAll(HomepageGeographicKeywordCache.class);
        count_ = new Long(findAll.size());
        logger.info("list: {} ", findAll);
        assertFalse(count.equals(count_));

        assertEquals(count.longValue(), count_ - 1L);

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
