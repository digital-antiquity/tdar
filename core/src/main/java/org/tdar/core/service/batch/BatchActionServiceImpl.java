package org.tdar.core.service.batch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.base.GenericDao;

@Service
public class BatchActionServiceImpl implements BatchActionService  {

    @Autowired
    private GenericDao genericDao;

    /* (non-Javadoc)
     * @see org.tdar.core.service.batch.BatchActionService#run(T, org.tdar.core.service.batch.BatchActionType, org.tdar.core.bean.collection.SharedCollection)
     */
    @Override
    @Transactional(readOnly = false)
    public <T extends AbstractBatchAction<?>> void run(T action, BatchActionType type, SharedCollection collection) {
        action.setGenericDao(genericDao);
        action.setup();
        for (Resource resource : collection.getResources()) {
            String message = action.prepareLog(resource);
            if (action.getExistingValue() == null || action.getCurrentValue(resource) == action.getExistingValue()) {
                action.performAction(resource, type);
                action.complete(resource, message);
            }
        }
    }
}
