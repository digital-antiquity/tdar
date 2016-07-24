package org.tdar.struts.action.collection;

import java.util.List;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts.action.DataTableResourceDisplay;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/collection")
public class CollectionController extends AbstractCollectionController<SharedCollection> implements DataTableResourceDisplay {

    private static final long serialVersionUID = 1169442990022630650L;

    /**
     * Threshold that defines a "big" collection.
     */
    public static final int BIG_COLLECTION_CHILDREN_COUNT = 3_000;

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
        // FIXME: may need some potential check for recursive loops here to prevent self-referential parent-child loops
        // FIXME: if persistable's parent is different from current parent; then need to reindex all of the children as well

        resourceCollectionService.saveCollectionForController(getPersistable(), getParentId(), getParentCollection(), getAuthenticatedUser(), getAuthorizedUsers(), getToAdd(),
                getToRemove(), shouldSaveResource(), generateFileProxy(getFileFileName(), getFile()), SharedCollection.class);
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
            searchIndexService.indexAllResourcesInCollectionSubTreeAsync(getPersistable());
        } else {
            searchIndexService.indexAllResourcesInCollectionSubTree(getPersistable());
        }
    }

    public SharedCollection getResourceCollection() {
        if (getPersistable() == null) {
            setPersistable(new SharedCollection());
        }
        return getPersistable();
    }

    public void setResourceCollection(SharedCollection rc) {
        setPersistable(rc);
    }

    @Override
    public Class<SharedCollection> getPersistableClass() {
        return SharedCollection.class;
    }

    /**
     * A hint to the view-layer that this resource collection is "big". The view-layer may choose to gracefully degrade the presentation to save on bandwidth
     * and/or
     * client resources.
     * 
     * @return
     */
    public boolean isBigCollection() {
        return (getPersistable().getResources().size() + getAuthorizedUsers().size()) > BIG_COLLECTION_CHILDREN_COUNT;
    }
}
