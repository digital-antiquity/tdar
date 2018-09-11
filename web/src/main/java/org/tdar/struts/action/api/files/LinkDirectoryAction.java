package org.tdar.struts.action.api.files;

import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.file.TdarDir;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/api/file")
public class LinkDirectoryAction extends AbstractHasFileAction<TdarDir> {



    private static final long serialVersionUID = -8391498966729712455L;

    @Autowired
    private PersonalFilestoreService personalFilestoreService;

    private Long collectionId;
    private ResourceCollection collection;

    
    @Override
    public void prepare() throws Exception {
        super.prepare();
        if (PersistableUtils.isNotNullOrTransient(getCollectionId())) {
            setCollection(getGenericService().find(ResourceCollection.class, getCollectionId()));
        }
        
    }
    
    @Override
    public void validate() {
        super.validate();
        if (PersistableUtils.isNullOrTransient(getCollectionId())) {
            addActionError("missing.collection_id");
        }

        if (!getAuthorizationService().canAddToCollection(getAuthenticatedUser(), getCollection())) {
            addActionError("bad.collection.permissions");
        }

    }
    
    @Action(value = "linkCollection",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") })
    @PostOnly
    @WriteableSession
    public String execute() throws IOException {
        personalFilestoreService.linkCollection(getFile(), getCollection(), getAuthenticatedUser());
        setResultObject(getFile());
        return SUCCESS;
    }

    public ResourceCollection getCollection() {
        return collection;
    }

    public void setCollection(ResourceCollection collection) {
        this.collection = collection;
    }

    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }


}
