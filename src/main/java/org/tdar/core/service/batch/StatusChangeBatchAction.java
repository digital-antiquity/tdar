package org.tdar.core.service.batch;

import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.Status;

public class StatusChangeBatchAction extends BatchAction {

    private static final long serialVersionUID = -5322353727290244847L;

    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public ResourceRevisionLog performAction(Resource resource, TdarUser user) {
        ResourceRevisionLog log = new ResourceRevisionLog(String.format("changed status from %s to %s", resource.getStatus(), status), resource, user);
        resource.setStatus(status);
        resource.markUpdated(user);
        return log;
    }

}
