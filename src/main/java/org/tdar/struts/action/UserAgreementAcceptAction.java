package org.tdar.struts.action;

import com.opensymphony.xwork2.Preparable;
import org.apache.struts2.convention.annotation.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.AuthNotice;
import org.tdar.core.bean.entity.Person;

import java.util.ArrayList;
import java.util.List;

@Namespace("/")
@ParentPackage("default")
@Component
@Scope("prototype")
@Action("agreement-response")
@Results({
        @Result(name=TdarActionSupport.INPUT, type="redirectAction", params = {"namespace", "/", "actionName", "show-notices"}),
        @Result(name=TdarActionSupport.SUCCESS, type="redirectAction",params = {"namespace", "dashboard", "actionName", "dashboard"})
})
public class UserAgreementAcceptAction  extends AuthenticationAware.Base implements Preparable {


    private List<AuthNotice> authNotices = new ArrayList<>();
    private List<AuthNotice> acceptedAuthNotices = new ArrayList<>();
    private String userResponse;

    @Override
    public void prepare() {
        Person user = getAuthenticatedUser();
        authNotices.addAll(getAuthenticationAndAuthorizationService().getUserRequirements(user));
    }

    public List<AuthNotice> getAuthNotices() {
        return authNotices;
    }

    public List<AuthNotice> getAcceptedAuthNotices() {
        return acceptedAuthNotices;
    }

    public void setSubmit(String value) {
        userResponse = value;
    }
}
