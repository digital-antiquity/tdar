package org.tdar.core.service.batch.actions;

import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.batch.AbstractBatchAction;
import org.tdar.core.service.batch.BatchActionType;

public class StatusChangeBatchAction extends AbstractBatchAction<Status> {

    private static final long serialVersionUID = -5322353727290244847L;

    @Override
    public Status getCurrentValue(Resource resource) {
        return resource.getStatus();
    }

    @Override
    public void performAction(Resource resource, BatchActionType type) {
        switch (type) {
            case CLEAR:
            case REMOVE:
                resource.setStatus(null);
                break;
            case ADD:
            case REPLACE:
                resource.setStatus(getNewValue());
                break;
        }
    }

    @Override
    public String getFieldName() {
        return "status";
    }

}
