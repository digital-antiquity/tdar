package org.tdar.core.service.batch.actions;

import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.batch.AbstractBatchAction;
import org.tdar.core.service.batch.BatchActionType;

public class SubmitterChangeBatchAction extends AbstractBatchAction<TdarUser> {

    private static final long serialVersionUID = 7388746143096436629L;

    @Override
    public void setup() {
        setExistingValue(getGenericDao().loadFromSparseEntity(getExistingValue(), TdarUser.class));
    };

    @Override
    public String getFieldName() {
        return "Submitter";
    }

    @Override
    public TdarUser getCurrentValue(Resource resource) {
        return resource.getSubmitter();
    }

    @Override
    public void performAction(Resource resource, BatchActionType type) {
        switch (type) {
            case CLEAR:
            case REMOVE:
                resource.setSubmitter(null);
                break;
            case ADD:
            case REPLACE:
                resource.setSubmitter(getNewValue());
                break;
        }

    }
}
