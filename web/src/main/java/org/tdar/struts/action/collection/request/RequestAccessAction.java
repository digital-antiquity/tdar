package org.tdar.struts.action.collection.request;

import java.util.HashSet;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.action.AbstractRequestAccessController;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.utils.EmailMessageType;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

/**
 * Backing of the actual request-access page
 * 
 * @author abrin
 *
 */
@ParentPackage("default")
@Namespace("/collection/request")
@Component
@Scope("prototype")
public class RequestAccessAction extends AbstractRequestAccessController<ResourceCollection> implements Preparable, PersistableLoadingAction<ResourceCollection> {

    private Set<EmailMessageType> emailTypes = new HashSet<>(EmailMessageType.valuesWithoutConfidentialFiles());

    private static final String SUCCESS_UNAUTH = "success-unauth";
    private static final long serialVersionUID = -6110216327414755768L;
    private EmailMessageType type = EmailMessageType.CONTACT;

    @Override
    public void prepare() {
        getLogger().trace("id: {}, {}", getId(), getPersistableClass());
        try {
        prepareAndLoad(this, RequestType.VIEW);
        } catch (Throwable t) {
            getLogger().error("{}",t,t);
        }
        if (PersistableUtils.isNullOrTransient(getPersistable())) {
            return;
        }



    }

    @Action(value = "{id}", results = { @Result(name = SUCCESS, location = "request-access.ftl"),
            @Result(name = SUCCESS_UNAUTH, location = "request-access-unauthenticated.ftl"),
            @Result(name = ERROR, type = TdarActionSupport.FREEMARKERHTTP, location = "/WEB-INF/content/errors/error.ftl", params = {
                    "status", "500" }),
            @Result(name = INPUT, type = TdarActionSupport.FREEMARKERHTTP, location = "/WEB-INF/content/errors/error.ftl", params = {
                    "status", "500" }) })
    @HttpsOnly
    @SkipValidation
    @Override
    public String execute() throws TdarActionException {
        // if we're logged out, go to request-access-unathenticated.ftl
        if (PersistableUtils.isNullOrTransient(getAuthenticatedUser())) {
            return SUCCESS_UNAUTH;
        }

        return SUCCESS;
    }

    @Override
    public boolean authorize() throws TdarActionException {
        return true;
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.VIEW_ANYTHING;
    }

    @Override
    public String getTypeNamespace() {
        return "collection";
    }

    public Set<EmailMessageType> getEmailTypes() {
        return emailTypes;
    }

    public void setEmailTypes(Set<EmailMessageType> emailTypes) {
        this.emailTypes = emailTypes;
    }

    public Class<ResourceCollection> getPersistableClass() {
        return ResourceCollection.class;
    }
}
