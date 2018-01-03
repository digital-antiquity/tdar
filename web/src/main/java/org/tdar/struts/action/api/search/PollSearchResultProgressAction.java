package org.tdar.struts.action.api.search;

import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Namespaces;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.service.AsynchronousProcessManager;
import org.tdar.core.service.AsynchronousStatus;
import org.tdar.struts.action.AbstractAdvancedSearchController;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.activity.IgnoreActivity;
import org.tdar.web.service.WebSearchServiceImpl;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@Namespaces(value = { @Namespace("/api/search") })
@Component
@Scope("prototype")
@ParentPackage("default")
@RequiresTdarUserGroup(TdarGroup.TDAR_USERS)
public class PollSearchResultProgressAction extends AbstractAdvancedSearchController implements Preparable, Validateable {

    private static final long serialVersionUID = -7606256523280755196L;

    private boolean webObfuscation = false;

    private Long collectionId;
    private String key;

    private ResourceCollection resourceCollection;

    private boolean async = true;

    private boolean addAsManaged = false;

    @Autowired
    private WebSearchServiceImpl webSearchService;

    @Override
    public void prepare() throws Exception {
        if (!PersistableUtils.isNullOrTransient(getAuthenticatedUser())) {
            key = webSearchService.constructKey(collectionId, getAuthenticatedUser().getId());
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
        super.validate();
    }

    @IgnoreActivity
    @Action(value = "checkstatus", results = { @Result(name = SUCCESS, type = TdarActionSupport.JSONRESULT) })
    @HttpForbiddenErrorResponseOnly
    public String checkStatusAsync() {
        AsynchronousStatus status = AsynchronousProcessManager.getInstance().findActivity(key);
        Map<String, Object> resultObject = new HashMap<String, Object>();
        if (status != null) {
            resultObject.put("status", status.getStatus());
            resultObject.put("message", status.getMessage());
            resultObject.put("percentComplete", status.getPercentComplete());
        } else {
            resultObject.put("status", "not running");
            resultObject.put("message", "there is no service process running");
            resultObject.put("percentComplete", 0.0f);
        }
        setResult(resultObject);
        return SUCCESS;
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
