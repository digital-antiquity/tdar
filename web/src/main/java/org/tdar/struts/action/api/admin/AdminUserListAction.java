package org.tdar.struts.action.api.admin;

import static com.opensymphony.xwork2.Action.SUCCESS;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.struts.action.TdarBaseActionSupport;
import org.tdar.struts_base.action.TdarActionSupport;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("secured")
@Namespace("/api/admin")
@Component
@Scope("prototype")
@Results({
        @Result(name = SUCCESS, type = TdarActionSupport.JSONRESULT, params = { "stream", "resultJson" })
})
public class AdminUserListAction extends TdarBaseActionSupport implements Preparable {

    private static final long serialVersionUID = 1951574309670698251L;

    private InputStream resultJson;

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private SerializationService serializationService;

    private List<TdarUser> users;
    private List<String> simple = new ArrayList<>();

    @Override
    public void prepare() throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        String remoteAddr = request.getRemoteAddr();
        getLogger().debug("remote: {}", remoteAddr);
        if (!remoteAddr.equals("127.0.0.1") && !remoteAddr.equals("localhost") &&
                !remoteAddr.equals("::1") && !remoteAddr.equals("0:0:0:0:0:0:0:1")) {
            throw new TdarRecoverableRuntimeException(getText("adminUserListAction.denied"));
        }
    }

    @Override
    @Action("loggedIn")
    public String execute() throws Exception {
        users = authService.getCurrentlyActiveUsers();
        for (TdarUser user : users) {
            simple.add(String.format("%s (%s)", user.getProperName(), user.getId()));
        }
        setResultJson(simple);
        return super.execute();
    }

    public InputStream getResultJson() {
        return resultJson;
    }

    public void setResultJson(Object resultObject) {
        String result = serializationService.convertFilteredJsonForStream(resultObject, null, null);
        getLogger().debug(result);
        setResultJson(new ByteArrayInputStream(result.getBytes()));
    }

    public void setResultJson(InputStream resultJson) {
        this.resultJson = resultJson;
    }

}
