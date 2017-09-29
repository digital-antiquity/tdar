package org.tdar.core.service.batch;

import org.tdar.core.bean.collection.ResourceCollection;

public interface BatchActionService {

    <T extends AbstractBatchAction<?>> void run(T action, BatchActionType type, ResourceCollection collection);

}