package org.tdar.core.service.batch;

import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;

public class SubmitterChangeBatchAction extends BatchAction {

    private static final long serialVersionUID = 7388746143096436629L;
    private TdarUser submitter;

    @Override
    public void setup(org.tdar.core.dao.GenericDao genericDao) {
        super.setup(genericDao);
        setSubmitter(genericDao.loadFromSparseEntity(submitter, TdarUser.class));
    };

    @Override
    public ResourceRevisionLog performAction(Resource resource, TdarUser user) {
        ResourceRevisionLog log = new ResourceRevisionLog(String.format("changed submitter from %s to %s", resource.getSubmitter(), submitter), resource, user);
        resource.setSubmitter(getSubmitter());
        resource.markUpdated(user);
        return log;
    }

    public TdarUser getSubmitter() {
        return submitter;
    }

    public void setSubmitter(TdarUser submitter) {
        this.submitter = submitter;
    }

}
