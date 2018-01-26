package org.tdar.balk.struts.action.setup;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.balk.struts.action.AbstractAuthenticatedAction;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.utils.dropbox.DropboxConfig;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("secured")
@Namespace("/setup")
@Component
@Scope("prototype")
public class AuthorizeAction extends AbstractAuthenticatedAction implements Preparable {

    private static final long serialVersionUID = -3012261887559902216L;
    private String authorizedUrl;

    public void prepare() {
        DropboxConfig config;
        try {
            config = new DropboxConfig();
            setAuthorizedUrl(config.getAuthorizedUrl());
        } catch (URISyntaxException | IOException e) {
            getLogger().error("{}",e,e);
        }
    }
    
    @Override
    @Action(value = "request", results = {
            @Result(name = TdarActionSupport.SUCCESS, location = "/WEB-INF/content/setup/request.ftl")
    })
    public String execute() throws Exception {
        return SUCCESS;
    }

    public String getAuthorizedUrl() {
        return authorizedUrl;
    }

    public void setAuthorizedUrl(String authorizedUrl) {
        this.authorizedUrl = authorizedUrl;
    }

}
