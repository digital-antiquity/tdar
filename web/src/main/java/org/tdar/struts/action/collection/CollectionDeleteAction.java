package org.tdar.struts.action.collection;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.DeleteIssue;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AbstractDeleteAction;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/collection")
public class CollectionDeleteAction extends AbstractDeleteAction<SharedCollection> implements Preparable {

    private static final long serialVersionUID = 8210288974799774479L;

    
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;
    @Autowired
    private transient AuthorizationService authorizationService;

    @Override
    protected SharedCollection loadPersistable() {
        SharedCollection collection = getGenericService().find(SharedCollection.class, getId());
        if (collection == null) {
            return null;
        }
        return collection;
    }

    @Override
    protected void delete(SharedCollection collection) {
        resourceCollectionService.deleteForController(collection, getDeletionReason(), getAuthenticatedUser());
    }

    @Override
    protected DeleteIssue getDeletionIssues() {
        return resourceCollectionService.getDeletionIssues(this, getPersistable());
    }

    @Override
    protected boolean canDelete() {
        if (authorizationService.can(InternalTdarRights.DELETE_COLLECTIONS, getAuthenticatedUser())) {
            return true;
        }
        return authorizationService.canEditCollection(getAuthenticatedUser(), getPersistable());
    }

}
