package org.tdar.balk.struts.action.setup;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.balk.service.UserService;
import org.tdar.balk.struts.action.AbstractAuthenticatedAction;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.utils.dropbox.DropboxConfig;

import com.dropbox.core.DbxException;
import com.opensymphony.xwork2.Preparable;

@ParentPackage("secured")
@Namespace("/setup")
@Component
@Scope("prototype")
@PostOnly
@WriteableSession
public class ProcessAuthorization extends AbstractAuthenticatedAction implements Preparable {

    private static final long serialVersionUID = 3312980413167535971L;

    @Autowired
    private UserService userService;

    private String code;
    private String token;

    public void prepare() {
        try {
            DropboxConfig config = new DropboxConfig();
            token = config.finish(getCode());
        } catch (URISyntaxException | IOException | DbxException e) {
            getLogger().error("{}", e, e);
        }
    }

    @Override
    @Action(value = "response", results = {
            @Result(name = TdarActionSupport.SUCCESS, type = TdarActionSupport.REDIRECT, location = "/"),
            @Result(name = TdarActionSupport.INPUT, type = TdarActionSupport.REDIRECT, location = "/setup/request")
    })
    public String execute() throws Exception {
        userService.saveTokenFor(getAuthenticatedUser(), token);
        return SUCCESS;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
