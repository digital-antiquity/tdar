package org.tdar.struts.action.api.search;

import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Namespaces;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.service.AsynchronousProcessManager;
import org.tdar.core.service.AsynchronousStatus;
import org.tdar.core.service.GeoRssMode;
import org.tdar.core.service.SerializationService;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.ProjectionModel;
import org.tdar.struts.action.AbstractAdvancedSearchController;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.utils.PersistableUtils;
import org.tdar.web.service.WebSearchService;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@Namespaces(value = { @Namespace("/api/search") })
@Component
@Scope("prototype")
@ParentPackage("default")
@RequiresTdarUserGroup(TdarGroup.TDAR_USERS)
public class SaveSearchResultAction extends AbstractAdvancedSearchController implements Preparable, Validateable {

    private static final long serialVersionUID = -7606256523280755196L;

    private Long collectionId;
    private String key;

    private ResourceCollection resourceCollection;
    private boolean async = true;
    private boolean addAsManaged = false;
    AsynchronousStatus saveSearchResultsForUserAsync = null;

    @Autowired
    private WebSearchService webSearchService;

    @Override
    public void prepare() throws Exception {
        if (!PersistableUtils.isNullOrTransient(getAuthenticatedUser())) {
            // Construct a Key (proposal)
            key = webSearchService.constructKey(collectionId, getAuthenticatedUser().getId());

            // find whether the activity is in the queue
            AsynchronousStatus status = AsynchronousProcessManager.getInstance().findActivity(key);

            if (status != null) {
                addActionError("SaveSearchResultAction.currently_saving");
            }
        }

    }

    @Override
    public void validate() {
        if (PersistableUtils.isNullOrTransient(getAuthenticatedUser())) {
            addActionError("SaveSearchResultAction.not_logged_in");
        }

        if (PersistableUtils.isNullOrTransient(collectionId)) {
            addActionError("SaveSearchResultAction.collection_missing");
        } else {
            resourceCollection = getGenericService().find(ResourceCollection.class, collectionId);

            if (PersistableUtils.isNullOrTransient(resourceCollection)) {
                addActionError("SaveSearchResultAction.invalid_collection");
            }
        }

    }

    @Action(value = "saveResults", results = {
            @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "jsonInputStream" }) })
    @PostOnly
    public String saveSearchResultsToCollection() throws TdarActionException {
        try {
            if (getSortField() == null) {
                setSecondarySortField(SortOption.TITLE);
            }

            // check resource collection is null;

            setMode("json");
            setProjectionModel(ProjectionModel.HIBERNATE_DEFAULT);

            setLookupSource(LookupSource.RESOURCE);

            // we need this for tests to be able to change the projection model so
            // we get full objects
            if (getProjectionModel() == null) {
                setProjectionModel(ProjectionModel.LUCENE);
            }

            processLegacySearchParameters();
            if (isAsync()) {
                saveSearchResultsForUserAsync = webSearchService.saveSearchResultsForUserAsync(getAsqo(), getAuthenticatedUser().getId(), collectionId, addAsManaged);
            } else {
                saveSearchResultsForUserAsync = webSearchService.saveSearchResultsForUser(getAsqo(), getAuthenticatedUser().getId(), collectionId, addAsManaged);
            }
            // invoke the UI to update/notify that results have been completed. jsonifyResult(JsonLookupFilter.class);
        }

        /*
         * catch (TdarActionException tdae) {
         * return tdae.getResponse();
         * }
         */
        catch (Exception e) {
            getLogger().error("rss error", e);
            addActionErrorWithException(getText("advancedSearchController.could_not_process"), e);
        }
        return SUCCESS;
    }

    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public boolean isAddAsManaged() {
        return addAsManaged;
    }

    public void setAddAsManaged(boolean addAsManaged) {
        this.addAsManaged = addAsManaged;
    }

}
