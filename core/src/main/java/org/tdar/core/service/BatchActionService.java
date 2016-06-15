package org.tdar.core.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.service.batch.BatchAction;

@Service
public class BatchActionService {

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private ResourceCollectionDao resourceCollectionDao;

    @Transactional(readOnly = false)
    public void processBatch(BatchAction action, TdarUser user) {
        action.setup(genericDao);
        List<ResourceCollection> allCollections = new ArrayList<>();
        allCollections.addAll(resourceCollectionDao.getAllChildCollections(action.getCollection()));
        allCollections.add(action.getCollection());

        for (ResourceCollection collection : allCollections) {
            for (Resource resource : collection.getResources()) {
                ResourceRevisionLog log = action.performAction(resource, user);
                genericDao.saveOrUpdate(log);
                resource.getResourceRevisionLog().add(log);
                genericDao.saveOrUpdate(resource);
            }
        }
    }
}
