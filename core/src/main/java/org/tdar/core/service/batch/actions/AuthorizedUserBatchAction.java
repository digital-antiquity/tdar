package org.tdar.core.service.batch.actions;

import java.util.Iterator;
import java.util.Set;

import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.batch.AbstractBatchAction;
import org.tdar.core.service.batch.BatchActionType;

import com.google.common.base.Objects;

public class AuthorizedUserBatchAction extends AbstractBatchAction<AuthorizedUser> {

    private static final long serialVersionUID = 4616698328054194307L;

    @Override
    public AuthorizedUser getCurrentValue(Resource resource) {
        return null;
    }

    @Override
    public void performAction(Resource resource, BatchActionType type) {
        ResourceCollection internalResourceCollection = resource.getInternalResourceCollection();
        if (internalResourceCollection == null) {
            return;
        }

        Set<AuthorizedUser> authorizedUsers = internalResourceCollection.getAuthorizedUsers();
        Iterator<AuthorizedUser> iterator = authorizedUsers.iterator();
        switch (type) {
            case CLEAR:
                authorizedUsers.clear();
            case REMOVE:
                while (iterator.hasNext()) {
                    AuthorizedUser au = iterator.next();
                    if (Objects.equal(au.getGeneralPermission(), getExistingValue().getGeneralPermission()) &&
                            Objects.equal(au.getUser(), getExistingValue().getUser())) {
                        iterator.remove();
                    }
                }
                break;
            case REPLACE:
                boolean matched = false;
                while (iterator.hasNext()) {
                    AuthorizedUser au = iterator.next();
                    if (Objects.equal(au.getGeneralPermission(), getExistingValue().getGeneralPermission()) &&
                            Objects.equal(au.getUser(), getExistingValue().getUser())) {
                        iterator.remove();
                        matched = true;
                    }
                }
                if (!matched) {
                    break;
                }
            case ADD:
                authorizedUsers.add(getNewValue());
                break;
        }
    }

    @Override
    public String getFieldName() {
        return "status";
    }

}
