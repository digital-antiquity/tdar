package org.tdar.struts.action.resource.request;

import java.util.Arrays;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.struts.action.PersistableLoadingAction;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.utils.EmailMessageType;

import com.opensymphony.xwork2.Preparable;
/**
 * Main page to backing permission processing page 
 * @author abrin
 *
 */
@ParentPackage("secured")
@Namespace("/resource/request")
@Component
@Scope("prototype")
public class AdminPermissonsRequestAction extends AbstractProcessPermissonsAction implements Preparable, PersistableLoadingAction<Resource> {

    private static final long serialVersionUID = -3345680920803370222L;

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
        return SUCCESS;
    }

    @Override
	public List<GeneralPermissions> getAvailablePermissions() {
    	if (getType() != null && getType() == EmailMessageType.SAA) {
    		return Arrays.asList(GeneralPermissions.MODIFY_RECORD);
    	}
		return super.getAvailablePermissions();
	}

}
