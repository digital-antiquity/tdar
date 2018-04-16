package org.tdar.struts.action.api.files;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.file.TdarDir;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/api/file")
public class CreateDirectoryAction extends AbstractJsonApiAction {

    private static final long serialVersionUID = 5045590663147590632L;

    private Long parentId;
    private TdarDir parent;
    private String name;

    @Autowired
    private PersonalFilestoreService personalFilestoreService;

    @Override
    public void prepare() throws Exception {
        super.prepare();
        getLogger().debug("name: {} parentId: {} ", name, parentId);
        if (PersistableUtils.isNotNullOrTransient(parentId)) {
            parent = getGenericService().find(TdarDir.class, parentId);
        }
    }

    @Override
    public void validate() {
        super.validate();
        if (StringUtils.isBlank(name)) {
            addActionError("createDirectoryAction.no_dir");
        }
    }

    @Action(value = "mkdir",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = { @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "jsonInputStream" }),
                    @Result(name = ERROR, type = JSONRESULT, params = { "stream", "jsonInputStream", "statusCode", "400" })
            })
    @PostOnly
    @WriteableSession
    public String execute() throws IOException {
        TdarDir directory = personalFilestoreService.createDirectory(parent, name, getAuthenticatedUser());
        setResultObject(directory);
        return SUCCESS;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public TdarDir getParent() {
        return parent;
    }

    public void setParent(TdarDir parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
