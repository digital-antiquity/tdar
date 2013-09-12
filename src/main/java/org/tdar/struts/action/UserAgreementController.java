package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.AuthNotice;
import org.tdar.core.bean.entity.Person;
import org.tdar.struts.WriteableSession;

import com.opensymphony.xwork2.Preparable;

@Namespace("/")
@ParentPackage("secured")
@Component
@Scope("prototype")
@Results({
        @Result(name=TdarActionSupport.INPUT, type="redirectAction", params = {  "actionName", "show-notices", "namespace", "/"}),
        @Result(name=TdarActionSupport.NONE, type="redirectAction", params = { "actionName", "logout", "namespace", "/" })
})
public class UserAgreementController extends AuthenticationAware.Base implements Preparable {

    private static final long serialVersionUID = 5992094345280080761L;
    public static final String FMT1_DECLINE_MESSAGE = "You have been logged out.  Please contact us if you have any questions regarding %s policies and procedures.";
    List<AuthNotice> authNotices = new ArrayList<>();
    List<AuthNotice> acceptedAuthNotices = new ArrayList<>();
    String userResponse = "";
    Person user;

    @Override
    public void prepare() {
        logger.trace("acceptedAuthNotices: {}", acceptedAuthNotices);
        logger.trace("userResponse:{}", userResponse);
        user = getAuthenticatedUser();
        authNotices.addAll(getAuthenticationAndAuthorizationService().getUserRequirements(user));
    }

    @WriteableSession
    @Action(value = "agreement-response", results = {@Result(name=TdarActionSupport.SUCCESS, type="redirect",location="/dashboard")})
    public String agreementResponse() {
        if(!isAuthenticated()) return LOGIN;

        if("decline".equals(userResponse)) {
            String fmt = FMT1_DECLINE_MESSAGE;
            addActionMessage(String.format(fmt, getSiteAcronym()));
            logger.debug("agreements declined,  redirecting to logout page");
            return NONE;
        }

        if("accept".equals(userResponse))  {
            if(processResponse()) {
                logger.debug("all requirements met,  success!! returning success");
                return SUCCESS;
            } else {
                logger.debug("some requirements remain, returning input");
                addActionError("Please accept or decline the updated agreements");
                return INPUT;
            }
        } else {
            //unexpected response. bail out!
            return BAD_REQUEST;
        }
    }

    boolean processResponse() {
        logger.trace(" pending notices:{}", authNotices);
        logger.trace("accepted notices:{}", acceptedAuthNotices);
        getAuthenticationAndAuthorizationService().satisfyUserPrerequisites(getSessionData(), acceptedAuthNotices);
        boolean allRequirementsMet = !getAuthenticationAndAuthorizationService().userHasPendingRequirements(user);
        return allRequirementsMet;
    }

    @Action(value="show-notices")
    public String showNotices() {
        if(!isAuthenticated()) return LOGIN;
        return SUCCESS;
    }

    public List<AuthNotice> getAuthNotices() {
        return authNotices;
    }

    public List<AuthNotice> getAcceptedAuthNotices() {
        return acceptedAuthNotices;
    }

    public void  setAcceptedAuthNotices(List<AuthNotice> value) {
        acceptedAuthNotices = value;
    }

    public void setSubmit(String value) {
        userResponse = value;
    }

    public boolean isTosAcceptanceRequired() {
        return authNotices.contains(AuthNotice.TOS_AGREEMENT);
    }

    public boolean isContributorAgreementAcceptanceRequired() {
        return authNotices.contains(AuthNotice.CONTRIBUTOR_AGREEMENT);
    }

    public String getTosUrl() {
        return getTdarConfiguration().getTosUrl();
    }

    public String getContributorAgreementUrl() {
        return getTdarConfiguration().getContributorAgreementUrl();
    }


}
