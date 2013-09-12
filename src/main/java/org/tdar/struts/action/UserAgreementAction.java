package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.AuthNotice;
import org.tdar.core.bean.entity.Person;

import com.opensymphony.xwork2.Preparable;

/**
 * contorller for presenting user notices
 * User: Jim
 * Date: 9/5/13
 * Time: 10:42 PM
 * To change this template use File | Settings | File Templates.
 */

@Namespace("/")
@ParentPackage("secured")
@Component
@Scope("prototype")
@Action(value="show-notices")
public class UserAgreementAction extends AuthenticationAware.Base implements Preparable {
//public class UserAgreementAction extends ActionSupport {

    private static final long serialVersionUID = 581473361181921943L;
    List<AuthNotice> authNotices = new ArrayList<>();

    @Override
    public void prepare() {
        Person user = getAuthenticatedUser();
        authNotices.addAll(getAuthenticationAndAuthorizationService().getUserRequirements(user));
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
        return getTdarConfiguration().getTosUrl();
    }

    public String getContributorAgreementUrl() {
        return getTdarConfiguration().getContributorAgreementUrl();
    }

}
