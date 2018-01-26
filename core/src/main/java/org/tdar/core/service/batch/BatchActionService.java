package org.tdar.core.service.batch;

import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.SharedCollection;

public interface BatchActionService {

    <T extends AbstractBatchAction<?>> void run(T action, BatchActionType type, SharedCollection collection);

}