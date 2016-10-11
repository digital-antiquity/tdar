package org.tdar.struts.action.collection;

import java.util.List;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts.action.AbstractCollectionRightsController;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/collection/rights")
public class CollectionRightsController extends AbstractCollectionRightsController<ListCollection> {


    private static final long serialVersionUID = 4318434880012567197L;
    @Autowired
    private transient SearchIndexService searchIndexService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;
    
    /**
     * Returns a list of all resource collections that can act as candidate parents for the current resource collection.
     * 
     * @return
     */
    public List<ListCollection> getCandidateParentResourceCollections() {
        List<ListCollection> publicResourceCollections = resourceCollectionService.findPotentialParentCollections(getAuthenticatedUser(),
                getPersistable(), ListCollection.class);
        return publicResourceCollections;
    }


    @Override
    protected String save(ListCollection persistable) {
        // FIXME: may need some potential check for recursive loops here to prevent self-referential parent-child loops
        // FIXME: if persistable's parent is different from current parent; then need to reindex all of the children as well

        resourceCollectionService.saveCollectionForRightsController(getPersistable(), getAuthenticatedUser(), getAuthorizedUsers(), ListCollection.class, getStartTime());
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

    public void setResourceCollection(ListCollection rc) {
        setPersistable(rc);
    }

    @Override
    public Class<ListCollection> getPersistableClass() {
        return ListCollection.class;
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
