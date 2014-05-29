/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.cache.HomepageGeographicKeywordCache;
import org.tdar.core.bean.cache.HomepageResourceCountCache;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword.Level;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.processes.RebuildHomepageCache;
import org.tdar.core.service.resource.CodingSheetService;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.TdarActionSupport;

/**
 * @author Adam Brin
 * 
 */
public class CachingServiceITCase extends AbstractControllerITCase {

    @Autowired
    private RebuildHomepageCache cacheRebuilder;

    @Autowired
    private CodingSheetService codingSheetService;

    public Long countActive() {
        int count = 0;
        for (CodingSheet sheet : codingSheetService.findAll()) {
            if (sheet.getStatus().equals(Status.ACTIVE)) {
                count++;
            }
        }
        return new Long(count);
    }

    @Test
    @Rollback
    public void testCachingService() throws Exception {
        Long count = countActive();
        cacheRebuilder.execute();
        Long count_ = -1l;
        count_ = updateCodingSheetCount(genericService.findAll(HomepageResourceCountCache.class));
        assertEquals(count, count_);

        createAndSaveNewInformationResource(CodingSheet.class);
        count_ = updateCodingSheetCount(genericService.findAll(HomepageResourceCountCache.class));
        assertEquals(count, count_);

        cacheRebuilder.execute();
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
        cacheRebuilder.execute();
        List<HomepageGeographicKeywordCache> findAll = genericService.findAll(HomepageGeographicKeywordCache.class);
        final Long count = new Long(findAll.size());
        Long count_ = count;
        CodingSheet cs = createAndSaveNewInformationResource(CodingSheet.class);
        GeographicKeyword mgc = new GeographicKeyword();
        mgc.setLabel("test");
        mgc.setLevel(Level.ISO_COUNTRY);
        genericService.save(mgc);
        cs.getManagedGeographicKeywords().add(mgc);
        genericService.saveOrUpdate(cs);
        evictCache();
        count_ = new Long(findAll.size());
        assertEquals(count, count_);
        logger.info("list: {} ", findAll);
        cacheRebuilder.execute();
        findAll = genericService.findAll(HomepageGeographicKeywordCache.class);
        count_ = new Long(findAll.size());
        logger.info("list: {} ", findAll);
        assertFalse(count.equals(count_));

        assertEquals(count.longValue(), count_ - 1L);

    }

}
