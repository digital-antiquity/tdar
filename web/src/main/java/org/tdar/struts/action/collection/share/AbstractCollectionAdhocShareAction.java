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
    public String loadViewMetadata() throws TdarActionException {
        // by default, simply  load the resourceCollection associated with the request (which is done by APC.prepare())
        return "success";
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
        //This request is authorized so long as the current user has the permission to modify the contents of the resource collection.
        return getAuthorizationService().canAddToCollection(getPersistable(), getAuthenticatedUser());
    }

}
