package org.tdar.struts.action.collection;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.SharedCollection;
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

    private SharedCollection collection;

    @Override
    public void prepare() throws Exception {
        collection = getGenericService().find(SharedCollection.class, getId());
        if (collection == null) {
            addActionError("collectionStatisticsAction.no_collection");
        }
        setStatsForAccount(statisticsService.getStatsForCollection(collection, this, getGranularity()));
        setupJson();
    }

    public SharedCollection getCollection() {
        return collection;
    }

    public void setCollection(SharedCollection collection) {
        this.collection = collection;
    }
}
