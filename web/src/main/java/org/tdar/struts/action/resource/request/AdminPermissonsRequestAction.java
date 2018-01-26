package org.tdar.struts.action.resource.request;

import java.util.Arrays;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.RequestCollection;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.utils.EmailMessageType;

import com.opensymphony.xwork2.Preparable;

/**
 * Main page to backing permission processing page
 * 
 * @author abrin
 *
 */
@ParentPackage("secured")
@Namespace("/resource/request")
@Component
@Scope("prototype")
public class AdminPermissonsRequestAction extends AbstractProcessPermissonsAction implements Preparable, PersistableLoadingAction<Resource> {

    private static final long serialVersionUID = -3345680920803370222L;

    @Autowired
    private transient ResourceService resourceService;

    private RequestCollection custom;

    @Action(value = "grant",
            results = {
                    @Result(name = SUCCESS, location = "grant-access.ftl"),
                    @Result(name = ERROR, type = TdarActionSupport.FREEMARKERHTTP, location = "/WEB-INF/content/errors/error.ftl",
                            params = { "status", "500" }),
                    @Result(name = INPUT, type = TdarActionSupport.FREEMARKERHTTP, location = "/WEB-INF/content/errors/error.ftl",
                            params = { "status", "500" })
            })
    @HttpsOnly
    public String requestAccess() throws TdarActionException {
        custom = resourceService.findCustom(getResource());
        return SUCCESS;
    }

    @Override
    public List<Permissions> getAvailablePermissions() {
        if (getType() != null && getType() == EmailMessageType.CUSTOM) {
            if (custom != null) {
                return Arrays.asList(custom.getPermission());
            }
        }
        return super.getAvailablePermissions();
    }

    public RequestCollection getCustom() {
        return custom;
    }
}
