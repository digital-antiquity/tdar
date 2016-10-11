package org.tdar.struts.action.share;

import java.util.List;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts.action.AbstractCollectionRightsController;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/collection/rights")
public class ShareRightsController extends AbstractCollectionRightsController<SharedCollection> {


    private static final long serialVersionUID = 5522048517742464825L;
    @Autowired
    private transient SearchIndexService searchIndexService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;
    
    /**
     * Returns a list of all resource collections that can act as candidate parents for the current resource collection.
     * 
     * @return
     */
    public List<SharedCollection> getCandidateParentResourceCollections() {
        List<SharedCollection> publicResourceCollections = resourceCollectionService.findPotentialParentCollections(getAuthenticatedUser(),
                getPersistable(), SharedCollection.class);
        return publicResourceCollections;
    }


    @Override
    protected String save(SharedCollection persistable) {
        resourceCollectionService.saveCollectionForRightsController(getPersistable(), getAuthenticatedUser(), getAuthorizedUsers(), SharedCollection.class, getStartTime());
        setSaveSuccessPath(getPersistable().getUrlNamespace());
        return SUCCESS;
    }

    @Override
    public void indexPersistable() {
        /*
         * if we want to be really "aggressive" we only need to do this if
         * (a) permissions change
         * (b) visibility changes
         */
        if (isAsync()) {
            searchIndexService.partialIndexAllResourcesInCollectionSubTreeAsync(getPersistable());
        } else {
            searchIndexService.partialIndexAllResourcesInCollectionSubTree(getPersistable());
        }
    }

    public void setResourceCollection(SharedCollection rc) {
        setPersistable(rc);
    }

    @Override
    public Class<SharedCollection> getPersistableClass() {
        return SharedCollection.class;
    }


    @Override
    public <C> C getResourceCollection() {
        return (C)getPersistable();
    }


    @Override
    public boolean isBigCollection() {
        return false;
    }

}
