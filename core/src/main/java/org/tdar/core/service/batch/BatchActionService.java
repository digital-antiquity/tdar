package org.tdar.core.service.batch;

import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.ResourceCollection;

public interface BatchActionService {

    <T extends AbstractBatchAction<?>> void run(T action, BatchActionType type, ResourceCollection collection);

}