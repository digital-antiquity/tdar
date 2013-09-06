package org.tdar.struts.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
public class UserAgreementAction extends AuthenticationAware.Base {
//public class UserAgreementAction extends ActionSupport {

    //FIXME: though we should exclude authenticationInterceptor, we should require user to be logged in
    @Override
    public String execute() {
        return SUCCESS;
    }
}
