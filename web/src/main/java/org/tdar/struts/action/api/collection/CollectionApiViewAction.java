package org.tdar.struts.action.api.collection;

import java.io.InputStream;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.jaxb.JaxbResultContainer;

import com.opensymphony.xwork2.Preparable;

@Namespace("/api/collection")
@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_API_USER)
@HttpForbiddenErrorResponseOnly
@HttpsOnly
public class CollectionApiViewAction extends AbstractAuthenticatableAction implements Preparable {


    private static final long serialVersionUID = 1344077793459231299L;
    @Autowired
    private transient AuthorizationService authorizationService;

    private Long id;
    private InputStream inputStream;
    private JaxbResultContainer xmlResultObject = new JaxbResultContainer();
    private ResourceCollection resource;

    @Action(value = "view", results = {
            @Result(name = SUCCESS, type = "xmldocument") })
    public String view() throws Exception {
        if (PersistableUtils.isNullOrTransient(getId()) || PersistableUtils.isNullOrTransient(resource)) {
                return INPUT;
        }
        return SUCCESS;
    }

    public final static String msg_ = "%s is %s %s (%s): %s";

    private void logMessage(String action_, Class<?> cls, Long id_, String name_) {
        getLogger().info(String.format(msg_, getAuthenticatedUser().getEmail(), action_, cls.getSimpleName().toUpperCase(), id_, name_));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public JaxbResultContainer getXmlResultObject() {
        return xmlResultObject;
    }

    public void setXmlResultObject(JaxbResultContainer xmlResultObject) {
        this.xmlResultObject = xmlResultObject;
    }
    
    @Override
    public void validate() {
        super.validate();
        if (PersistableUtils.isNullOrTransient(resource) || !authorizationService.canView(getAuthenticatedUser(), resource)) {
            addActionError("cannot edit resource");
        }
    }


    @Override
    public void prepare() throws Exception {
        if (PersistableUtils.isNotNullOrTransient(getId())) {
            resource = getGenericService().find(ResourceCollection.class, getId());
            if (resource == null) {
                getLogger().debug("could not find resource: {}", getId());
            }
            logMessage("API VIEWING", resource.getClass(), resource.getId(), resource.getTitle());
            getXmlResultObject().setCollectionResult(resource);
        }
        
    }

}
