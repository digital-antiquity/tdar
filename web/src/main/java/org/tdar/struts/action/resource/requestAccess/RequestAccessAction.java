package org.tdar.struts.action.resource.requestAccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.external.RecaptchaService;
import org.tdar.core.service.external.auth.AntiSpamHelper;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.PersistableLoadingAction;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.utils.EmailMessageType;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("default")
@Namespace("/resource/request-access")
@Component
@Scope("prototype")
public class RequestAccessAction extends AuthenticationAware.Base implements Preparable, PersistableLoadingAction<Resource> {

    @Autowired
    private transient RecaptchaService recaptchaService;
    private AntiSpamHelper h = new AntiSpamHelper();
    private List<EmailMessageType> emailTypes = EmailMessageType.valuesWithoutConfidentialFiles();

    @Autowired
    private transient ObfuscationService obfuscationService;
    private static final String SUCCESS_UNAUTH = "success-unauth";
	private static final long serialVersionUID = -6110216327414755768L;
    private Long id;
    private Resource resource;
    
    
    @Override
    public void prepare() throws Exception {
    	getLogger().debug("id: {}, {}", getId(), getPersistableClass());
        prepareAndLoad(this, RequestType.VIEW);
    }

    @Action(value = "{id}",
            results = {
                    @Result(name = SUCCESS, location = "request-access.ftl"),
                    @Result(name = SUCCESS_UNAUTH, location = "request-access-unauthenticated.ftl"),
                    @Result(name = ERROR, type = TdarActionSupport.FREEMARKERHTTP, location = "/WEB-INF/content/errors/error.ftl",
                            params = { "status", "500" }),
                    @Result(name = INPUT, type = TdarActionSupport.FREEMARKERHTTP, location = "/WEB-INF/content/errors/error.ftl",
                            params = { "status", "500" })
            })
    @HttpsOnly
    @SkipValidation
    @Override
    public String execute() throws TdarActionException {
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
    public Resource getPersistable() {
        return getResource();
    }

    @Override
    public void setPersistable(Resource persistable) {
    	getLogger().debug("set persistable: {}", persistable);
        this.resource = persistable;
    }

    @Override
    public Class<Resource> getPersistableClass() {
        return Resource.class;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
    	this.id = id;
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.VIEW_ANYTHING;
    }

	public AntiSpamHelper getH() {
		return h;
	}

	public void setH(AntiSpamHelper h) {
		this.h = h;
	}

    public List<EmailMessageType> getEmailTypes() {
        if (getResource() instanceof InformationResource) {
            InformationResource informationResource = (InformationResource) getResource();
            if (informationResource.hasConfidentialFiles()) {
                emailTypes = Arrays.asList(EmailMessageType.values());
            }
        }
        return emailTypes;
    }

    public ResourceCreatorProxy getContactProxies() {
        // this may be duplicative... check
    	List<ResourceCreatorProxy> proxies = new ArrayList<>();
        for (ResourceCreator rc : resource.getActiveResourceCreators()) {
            if (getTdarConfiguration().obfuscationInterceptorDisabled()) {
                if ((rc.getCreatorType() == CreatorType.PERSON) && !isAuthenticated()) {
                    obfuscationService.obfuscate(rc.getCreator(), getAuthenticatedUser());
                }
            }

            ResourceCreatorProxy proxy = new ResourceCreatorProxy(rc);
            if (proxy.isValidEmailContact()) {
                proxies.add(proxy);
            }
        }
        return proxies;

    }
}
