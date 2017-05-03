package org.tdar.struts.action.collection;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.struts.action.AbstractStatisticsAction;
import org.tdar.struts.interceptor.annotation.HttpsOnly;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/collection/usage")
@HttpsOnly
public class CollectionStatisticsAction extends AbstractStatisticsAction implements Preparable {

    private static final long serialVersionUID = 1653124517249681107L;

    @Autowired
    private ResourceCollectionService collectionService;
    
    private ResourceCollection collection;

    @Override
    public void prepare() throws Exception {
        collection = collectionService.find(getId());
        if (collection == null) {
            addActionError("collectionStatisticsAction.no_collection");
        }
        setStatsForAccount(statisticsService.getStatsForCollection(collection, this, getGranularity()));
        setupJson();
    }

    public ResourceCollection getCollection() {
        return collection;
    }

    public void setCollection(ResourceCollection collection) {
        this.collection = collection;
    }
}
