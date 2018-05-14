package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Namespaces;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.action.TdarActionSupport;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("secured")
@Component
@Scope("prototype")
@Namespace("/resource")
public class CreateResourceFromFilesAction extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = 6944378996413087823L;

    private List<Long> fileIds = new ArrayList<>();
    
    private List<TdarFile> files = new ArrayList<>();
    
    @Autowired
    PersonalFilestoreService personalFilestooreService;

    private ResourceType type;
    
    @Override

    public void prepare() throws Exception {
        files = getGenericService().findAll(TdarFile.class, fileIds);
        setType(personalFilestooreService.getResourceTypeForFiles(files.get(0)));
    }

    @Override
    @Action(value = "createRecordFromFiles",
    results = {
            @Result(name = SUCCESS, type = TdarActionSupport.REDIRECT, location = "/${type.urlNamespace}/add?fileIds=${fileIds[0]}")
    })
    public String execute() throws Exception {
        getLogger().debug("{} | {} -- {}", fileIds, files, type);
        return SUCCESS;
    }
    
    public List<Long> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<Long> filesIds) {
        this.fileIds = filesIds;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    
}
