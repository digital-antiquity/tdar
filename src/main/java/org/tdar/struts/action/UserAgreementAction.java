package org.tdar.struts.action;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.Preparable;
import org.apache.struts2.convention.annotation.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.AuthNotice;
import org.tdar.core.bean.entity.Person;

import java.util.ArrayList;
import java.util.List;

/**
 * contorller for presenting user notices
 * User: Jim
 * Date: 9/5/13
 * Time: 10:42 PM
 * To change this template use File | Settings | File Templates.
 */

@Namespace("/")
@ParentPackage("default")
@Component
@Scope("prototype")
@Action(value="show-notices")
public class UserAgreementAction extends AuthenticationAware.Base implements Preparable {
//public class UserAgreementAction extends ActionSupport {


    List<AuthNotice> authNotices = new ArrayList<>();
    String tosUrl;


    String contributorAgreementUrl;

    @Override
    public void prepare() {
        Person user = getAuthenticatedUser();
        authNotices.addAll(getAuthenticationAndAuthorizationService().getUserRequirements(user));
        tosUrl = getTdarConfiguration().getTosUrl();
        contributorAgreementUrl = getTdarConfiguration().getContributorAgreementUrl();
    }

    @Override
    public String execute() {
        if(!isAuthenticated()) return LOGIN;
        return SUCCESS;
    }

    public List<AuthNotice> getAuthNotices() {
        return authNotices;
    }

    public boolean isTosAcceptanceRequired() {
        return authNotices.contains(AuthNotice.TOS_AGREEMENT);
    }

    public boolean isContributorAgreementAcceptanceRequired() {
        return authNotices.contains(AuthNotice.CONTRIBUTOR_AGREEMENT);
    }

    public String getTosUrl() {
        return tosUrl;
    }

    public String getContributorAgreementUrl() {
        return contributorAgreementUrl;
    }

}
