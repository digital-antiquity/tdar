/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.ExternalIDProvider;
import org.tdar.core.service.processes.DoiProcess;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.utils.Pair;

import static org.junit.Assert.*;

/**
 * @author Adam Brin
 * 
 */
public class DOIServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    ResourceService resourceService;
    @Autowired
    InformationResourceService informationResourceService;
    @Autowired
    private DoiProcess doiProcess;

    public Map<String, List<Pair<Long, String>>> processDois() throws ClientProtocolException, IOException {
        // using mock DAO instead of real service
        List<ExternalIDProvider> providers = new ArrayList<ExternalIDProvider>();
        providers.add(new MockIdentifierDao());
        doiProcess.setAllServices(providers);
        // run it once, make sure all are "create", no deletes or updates
        List<Long> ids = doiProcess.getPersistableIdQueue();
        doiProcess.processBatch(ids);
        return doiProcess.getBatchResults();
    }

    @Test
    @Rollback
    public void testDOIService() throws ClientProtocolException, IOException, InstantiationException, IllegalAccessException {
        Map<String, List<Pair<Long, String>>> createAndUpdateDoiInfo = processDois();
        List<Pair<Long, String>> created = createAndUpdateDoiInfo.get(DoiProcess.CREATED);
        List<Pair<Long, String>> updated = createAndUpdateDoiInfo.get(DoiProcess.UPDATED);
        List<Pair<Long, String>> deleted = createAndUpdateDoiInfo.get(DoiProcess.DELETED);
        logger.debug("created:" + created.size());
        logger.debug("updated:" + updated.size());
        logger.debug("deleted:" + deleted.size());
        assertTrue(created.size() > 0);
        assertTrue(updated.size() == 0);
        assertTrue(deleted.size() == 0);
        doiProcess.cleanup();
        // mark one record as deleted, and mark one as updated
        Resource toBeDeleted = resourceService.find(created.get(0).getFirst());
        Resource toBeUpdated = resourceService.find(created.get(1).getFirst());
        toBeDeleted.setStatus(Status.DELETED);
        toBeDeleted.markUpdated(getAdminUser());
        toBeUpdated.markUpdated(getAdminUser());
        resourceService.saveOrUpdate(toBeUpdated);
        resourceService.saveOrUpdate(toBeDeleted);

        // create new resources (1) without file (1) with file (2) with file, ancient date, and already has DOI
        InformationResource generateInformationResourceWithUser = generateInformationResourceWithUser();
        InformationResource file = generateInformationResourceWithFileAndUser();
        InformationResource file2 = generateInformationResourceWithFileAndUser();
        file2.setDateUpdated(new Date(10000)); // forever ago -- should not register
        file2.setExternalId("1234");
        genericService.saveOrUpdate(file2);
        createAndUpdateDoiInfo = processDois();

        List<Pair<Long, String>> created_ = createAndUpdateDoiInfo.get(DoiProcess.CREATED);
        List<Pair<Long, String>> updated_ = createAndUpdateDoiInfo.get(DoiProcess.UPDATED);
        List<Pair<Long, String>> deleted_ = createAndUpdateDoiInfo.get(DoiProcess.DELETED);
        logger.debug("created:" + created_.size());
        logger.debug("updated:" + updated_.size());
        logger.debug("deleted:" + deleted_.size());
        assertEquals(1, created_.size());
        assertTrue(updated_.size() > 0);
        assertTrue(deleted_.size() > 0);
    }
}
