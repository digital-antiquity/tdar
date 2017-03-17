package org.tdar.struts.action.collection.share;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.collection.AdhocShare;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.struts.action.AbstractPersistableViewableAction;
import org.tdar.struts_base.action.TdarActionException;

/**
 * Created by jimdevos on 2/27/17.
 */
@ParentPackage("secured")
@Namespace("/collection/share")
@Component
@Scope("prototype")
public abstract class AbstractCollectionAdhocShareAction extends AbstractPersistableViewableAction<SharedCollection> {

    @Override
    //fixme: APVA requires loadViewMetadata() but only calls it when action is "view".  Pull out APVA into another class that has what we use minus view()
    public String loadViewMetadata() throws TdarActionException {
        return "error";
    }

    @Autowired
    private ResourceCollectionService resourceCollectionService;

    private AdhocShare adhocShare = new AdhocShare();

    @Override public Class<SharedCollection> getPersistableClass() {
        return SharedCollection.class;
    }

    public ResourceCollectionService getResourceCollectionService() {
        return this.resourceCollectionService;
    }

    public AdhocShare getAdhocShare() {
        return adhocShare;
    }

    public SharedCollection getResourceCollection() {
        return getPersistable();
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.EDIT_RESOURCE_COLLECTIONS;
    }

    @Override
    public boolean authorize() {
        return getAuthorizationService().canAdministerCollection(getAuthenticatedUser(), getPersistable());
    }

}
