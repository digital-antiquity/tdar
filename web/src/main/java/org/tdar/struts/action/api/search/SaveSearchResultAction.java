package org.tdar.struts.action.api.search;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Namespaces;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.FeedSearchHelper;
import org.tdar.core.service.GeoRssMode;
import org.tdar.core.service.SerializationService;
import org.tdar.search.query.ProjectionModel;
import org.tdar.struts.action.AbstractAdvancedSearchController;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.json.JsonLookupFilter;

@Namespaces(value = {
        @Namespace("/api/search") })
@Component
@Scope("prototype")
@ParentPackage("default")
public class SaveSearchResultAction extends AbstractAdvancedSearchController {

    private static final long serialVersionUID = -7606256523280755196L;

    @Autowired
    private transient SerializationService serializationService;

    private GeoRssMode geoMode = GeoRssMode.POINT;
    private boolean webObfuscation = false;
    private Long collectionId;

    private Map<String, Object> resultObject;
    
    
    @Action(value = "save", results = {
            @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "jsonInputStream" })}
    		
    		)
    @PostOnly
    public String viewJson() throws TdarActionException {
        try {
            if (getSortField() == null) {
                setSecondarySortField(SortOption.TITLE);
            }
            
            ResourceCollection resourceCollection;
            if(PersistableUtils.isNullOrTransient(collectionId)){
            //	resourceCollection = new ResourceCollection();
            	
            }
            else {
            	resourceCollection = getGenericService().find(ResourceCollection.class, collectionId);
            }
            
           //check resource  collection is null;
            
            setMode("json");
            setProjectionModel(ProjectionModel.HIBERNATE_DEFAULT);
            
            
            performResourceSearch();
            
            
            List<Resource> results = getResults();
            
            //pass results w/ user and collection name (id) to service
            
            
            //handle additional pages
   
            
           //  invoke the UI to update/notify that results have been completed. jsonifyResult(JsonLookupFilter.class);
            
        } 
        catch (TdarActionException tdae) {
            return tdae.getResponse();
        } 
        catch (Exception e) {
            getLogger().error("rss error", e);
            addActionErrorWithException(getText("advancedSearchController.could_not_process"), e);
        }
        return SUCCESS;
    }

    @Override
    public void jsonifyResult(Class<?> filter) {
        prepareResult();
        if (!isReindexing()) {
            try {
                FeedSearchHelper feedSearchHelper = new FeedSearchHelper(getRssUrl(), this, getGeoMode(), getAuthenticatedUser());
                resultObject = serializationService.createGeoJsonFromResourceList(feedSearchHelper);
            } catch (Exception e) {
                getLogger().error("error creating json", e);
            }
        }
    }

    public GeoRssMode getGeoMode() {
        return geoMode;
    }

    public void setGeoMode(GeoRssMode geoMode) {
        this.geoMode = geoMode;
    }

    public boolean isWebObfuscation() {
        return webObfuscation;
    }

    public void setWebObfuscation(boolean webObfuscation) {
        this.webObfuscation = webObfuscation;
    }

	public Long getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(Long collectionId) {
		this.collectionId = collectionId;
	}

}
