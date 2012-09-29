package org.tdar.struts.action;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * $Id$
 * 
 * <p>
 * Action for the root namespace.
 * 
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Namespace("/")
@ParentPackage("default")
@Component
@Scope("prototype")
@Results({
    @Result(name="authenticated", type="redirect", location="/")
})
public class IndexAction extends AuthenticationAware.Base {
    private static final long serialVersionUID = -9216882130992021384L;
    
    @Override
    @Action(results={
        @Result(name="success", location="about.ftl")
    })
    public String execute() {
        return SUCCESS;
    }

    @Actions({
        @Action("about"),
        @Action("terms"),
        @Action("contact"),
        @Action("page-not-found"),
        @Action("access-denied")
    })
    public String passThrough() {
        return SUCCESS;
    }
    
    @Action("login")
    public String login() {
        if (isAuthenticated()) {
            return AUTHENTICATED;
        }
        return SUCCESS;
        
    }
    
    @Action(value="logout",
            results={
            @Result(name="success", type="redirect", location="/")
    })
    public String logout() {
        if (getSessionData().isAuthenticated()) {
            clearAuthenticationToken();
            getCrowdService().logout(getServletRequest(), getServletResponse());
            return SUCCESS;
        }
        else {
            return LOGIN;
        }
    }
    
}
