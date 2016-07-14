package org.tdar.struts.action.api.collection;

import java.io.ByteArrayInputStream;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

@Namespace("/api/collection")
@Component
@Scope("prototype")
@ParentPackage("secured")
@HttpForbiddenErrorResponseOnly
@HttpsOnly
@Results(value = {
        @Result(name = TdarActionSupport.SUCCESS, type = TdarActionSupport.JSONRESULT, params = { "stream", "jsonInputStream" }),
        @Result(name = TdarActionSupport.INPUT, type = TdarActionSupport.JSONRESULT, params = { "stream", "jsonInputStream", "statusCode", "500" })
})
public class MoveCollectionAction extends AbstractJsonApiAction implements Preparable {

    private Long collectionId;
    private Long fromCollectionId;
    private Long toCollectionId;
    private ResourceCollection collection;
    private ResourceCollection fromCollection;
    private ResourceCollection toCollection;

    @Autowired
    protected transient SerializationService serializationService;

    @Autowired
    protected transient SearchIndexService searchIndexService;

    @Autowired
    protected transient ResourceCollectionService resourceCollectionService;

    @Autowired
    private AuthorizationService authorizationService;
    
    @Override
    public void validate() {
        super.validate();
        if (PersistableUtils.isNullOrTransient(collection) || !authorizationService.canEdit(getAuthenticatedUser(), collection)) {
            addActionError("cannot edit collection");
        }
        if (PersistableUtils.isNullOrTransient(fromCollection) || !authorizationService.canEdit(getAuthenticatedUser(), fromCollection)) {
            addActionError("cannot edit from colection");
        }
        if (PersistableUtils.isNullOrTransient(toCollection) || !authorizationService.canEdit(getAuthenticatedUser(), toCollection)) {
            addActionError("cannot edit to colection");
        }
    }
    
    @Override
    @PostOnly
    @Action(value="moveCollection")
    public String execute() throws Exception {
        resourceCollectionService.updateCollectionParentTo(getAuthenticatedUser(), collection, toCollection);
        searchIndexService.indexAllResourcesInCollectionSubTreeAsync(toCollection);
        setJsonInputStream(new ByteArrayInputStream("SUCCESS".getBytes()));
        return super.execute();
    }

    /**
     * 
     */
    private static final long serialVersionUID = 2137331107886327060L;

    @Override
    public void prepare() throws Exception {
        this.collection = getGenericService().find(ResourceCollection.class, collectionId);
        this.fromCollection = getGenericService().find(ResourceCollection.class, fromCollectionId);
        this.toCollection = getGenericService().find(ResourceCollection.class, toCollectionId);
        
    }

    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long resourceId) {
        this.collectionId = resourceId;
    }

    public Long getFromCollectionId() {
        return fromCollectionId;
    }

    public void setFromCollectionId(Long fromCollectionId) {
        this.fromCollectionId = fromCollectionId;
    }

    public Long getToCollectionId() {
        return toCollectionId;
    }

    public void setToCollectionId(Long toCollectionId) {
        this.toCollectionId = toCollectionId;
    }
    
}
