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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword.Level;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.cache.HomepageGeographicCache;
import org.tdar.core.cache.HomepageResourceCountCache;
import org.tdar.core.configuration.TdarBaseWebAppConfiguration;
import org.tdar.core.service.processes.daily.RebuildHomepageCache;
import org.tdar.core.service.resource.CodingSheetService;

/**
 * @author Adam Brin
 * 
 */
@ContextConfiguration(classes = TdarBaseWebAppConfiguration.class)
@DirtiesContext(methodMode = MethodMode.BEFORE_METHOD, classMode = ClassMode.AFTER_CLASS)
public class CachingServiceITCase extends AbstractIntegrationTestCase {

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
        count_ = updateCodingSheetCount(resourceService.getResourceCounts());
        assertEquals(count, count_);

        createAndSaveNewInformationResource(CodingSheet.class);
        count_ = updateCodingSheetCount(resourceService.getResourceCounts());
        assertEquals(count, count_);

        cacheRebuilder.execute();
        count_ = updateCodingSheetCount(resourceService.getResourceCounts());
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
        List<HomepageGeographicCache> findAll = resourceService.getISOGeographicCounts();
        final Long count = new Long(findAll.size());
        Long count_ = count;
        CodingSheet cs = createAndSaveNewInformationResource(CodingSheet.class);
        GeographicKeyword mgc = new GeographicKeyword();
        mgc.setLabel("test");
        mgc.setLevel(Level.COUNTRY);
        mgc.setCode("TST");
        genericService.save(mgc);
        cs.getManagedGeographicKeywords().add(mgc);
        genericService.saveOrUpdate(cs);
        evictCache();
        count_ = new Long(findAll.size());
        assertEquals(count, count_);
        for (HomepageGeographicCache c: findAll) {
            logger.trace("{} {} {}", c.getCode(), c.getCount(), c.getResourceType());
        }

        // done setup
        cacheRebuilder.execute();
        findAll = resourceService.getISOGeographicCounts();
        count_ = new Long(findAll.size());
        logger.info("list: {} ", findAll);
        for (HomepageGeographicCache c: findAll) {
            logger.trace("{} {} {}", c.getCode(), c.getCount(), c.getResourceType());
        }
        assertFalse(count.equals(count_));

        // note this will need to be updated when the countries and codes are folded together
        assertEquals(count.longValue(), count_ - 2L);

    }

}
