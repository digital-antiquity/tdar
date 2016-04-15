package org.tdar.struts.action.resource.request;

import java.util.List;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.UserAffiliation;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.RecaptchaService;
import org.tdar.core.service.external.auth.AntiSpamHelper;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

@Namespace("/resource/request")
@Component
@Scope("prototype")
@Results({
        @Result(name = TdarActionSupport.SUCCESS, type = AbstractRequestAccessController.REDIRECT, location = AbstractRequestAccessController.SUCCESS_REDIRECT_REQUEST_ACCESS),
        @Result(name = TdarActionSupport.ERROR, type = TdarActionSupport.HTTPHEADER, params = { "error", "404" }),
        @Result(name = TdarActionSupport.INPUT, type = TdarActionSupport.HTTPHEADER, params = { "error", "404" }),
        @Result(name = TdarActionSupport.FORBIDDEN, type = TdarActionSupport.HTTPHEADER, params = { "error", "403" })
})
public class AbstractRequestAccessController extends AuthenticationAware.Base implements Preparable {

    private static final long serialVersionUID = -1831798412944149018L;
    @Autowired
    private transient AuthorizationService authorizationService;

    private List<UserAffiliation> affiliations = UserAffiliation.getUserSubmittableAffiliations();

    public static final String LOGIN_REGISTER_PROMPT = "/resource/request/request-access-unauthenticated.ftl";
    public static final String SUCCESS_REDIRECT_REQUEST_ACCESS = "/resource/request/${id}";
    public static final String FORBIDDEN = "forbidden";
    private Long id;


    // the resource being downloaded (or the resource that the file is being downloade from)
    private InformationResource informationResource;

    @Autowired
    private transient RecaptchaService recaptchaService;
    private AntiSpamHelper h = new AntiSpamHelper();

    public InformationResource getInformationResource() {
        return informationResource;
    }

    public void setInformationResource(InformationResource informationResource) {
        this.informationResource = informationResource;
    }


    @Override
    public void prepare() {
        if (PersistableUtils.isNotNullOrTransient(getId())) {
            setInformationResource(getGenericService().find(InformationResource.class, getId()));
            // bad, but force onto session until better way found
            authorizationService.applyTransientViewableFlag(informationResource, getAuthenticatedUser());
        }
    }

    @Override
    public void validate() {
        if (PersistableUtils.isNullOrTransient(getId())) {
            addActionError(getText("requestAccessController.specify_what_to_download"));
        }
    }

    public RecaptchaService getRecaptchaService() {
        return recaptchaService;
    }


	public List<UserAffiliation> getAffiliations() {
		return affiliations;
	}

	public void setAffiliations(List<UserAffiliation> affiliations) {
		this.affiliations = affiliations;
	}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	public AntiSpamHelper getH() {
		return h;
	}

	public void setH(AntiSpamHelper h) {
		this.h = h;
	}
}
