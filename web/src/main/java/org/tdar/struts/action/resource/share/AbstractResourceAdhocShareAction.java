package org.tdar.struts.action.resource.share;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.collection.AdhocShare;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.struts.action.AbstractPersistableViewableAction;
import org.tdar.struts_base.action.TdarActionException;

/**
 * Created by jimdevos on 3/6/17.
 */
@ParentPackage("secured")
@Namespace("/resource/share")
@Component
@Scope("prototype")
public class AbstractResourceAdhocShareAction extends AbstractPersistableViewableAction<Resource> {

    private AdhocShare adhocShare = new AdhocShare();

    @Autowired
    private ResourceCollectionService resourceCollectionService;

    protected ResourceCollectionService getResourceCollectionService() {
        return resourceCollectionService;
    }

    @Override
    public Class<Resource> getPersistableClass() {
        return Resource.class;
    }

    @Override
    public String loadViewMetadata() throws TdarActionException {
        return "success";
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.EDIT_ANY_RESOURCE;
    }


    @Override
    public boolean authorize() {
        //This request is authorized so long as the current user has the permission to modify the contents of the resource collection.
        return getAuthorizationService().canEdit(getAuthenticatedUser(), getPersistable());
    }

    public AdhocShare getAdhocShare() {
        return adhocShare;
    }
}
