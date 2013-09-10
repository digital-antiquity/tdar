package org.tdar.struts.action;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;
import org.apache.struts2.convention.annotation.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.AuthNotice;
import org.tdar.core.bean.entity.Person;
import org.tdar.struts.WriteableSession;

import java.util.ArrayList;
import java.util.List;

@Namespace("/")
@ParentPackage("secured")
@Component
@Scope("prototype")
@Action(value = "agreement-response")
@Results({
        @Result(name=TdarActionSupport.INPUT, type="redirectAction", params = {  "actionName", "show-notices", "namespace", "/"}),
        @Result(name=TdarActionSupport.SUCCESS, type="redirectAction",params = {"actionName", "dashboard", "namespace", "/dashboard"}),
        @Result(name=TdarActionSupport.NONE, type="redirectAction", params = { "actionName", "logout", "namespace", "/" })
})
public class UserAgreementAcceptAction  extends AuthenticationAware.Base implements Preparable {

    public static final String FMT1_DECLINE_MESSAGE = "You have been logged out.  Please contact us if you have any questions regarding %s policies and procedures.";
    List<AuthNotice> authNotices = new ArrayList<>();
    List<AuthNotice> acceptedAuthNotices = new ArrayList<>();
    String userResponse = "";
    Person user;

    @Override
    public void prepare() {
        logger.debug("prepare phase!");
        logger.debug("acceptedAuthNotices: {}", acceptedAuthNotices);
        logger.debug("userResponse:{}", userResponse);
        user = getAuthenticatedUser();
        authNotices.addAll(getAuthenticationAndAuthorizationService().getUserRequirements(user));
    }

    @Override
    @WriteableSession
    public String execute() {
        logger.debug("execute phase!");
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
        logger.debug(" pending notices:{}", authNotices);
        logger.debug("accepted notices:{}", acceptedAuthNotices);
        getAuthenticationAndAuthorizationService().satisfyUserPrerequisites(getSessionData(), acceptedAuthNotices);

        //FIXME: the update and sync are not needed,  but I'm trying to figure out why the user tos fields aren't updated by the time the SUCCESS redirect happens
        //getGenericService().update(user);
        //getGenericService().synchronize();


        boolean allRequirementsMet = !getAuthenticationAndAuthorizationService().userHasPendingRequirements(user);
        return allRequirementsMet;
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
}
