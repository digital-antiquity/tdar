package org.tdar.struts.action.resource;

import java.io.File;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;
import org.tdar.web.service.ArchivalFileSaveService;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/resource/admin")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
public class AdminArchivalFileUploadAction extends AbstractAuthenticatableAction implements Preparable , Validateable{

    private static final long serialVersionUID = -8963751484178492951L;
    private File file;
    private String fileContentType;
    private String fileFileName;
    private Long id;
    private Long fileId;

    @Autowired
    private ArchivalFileSaveService archivalFileSaveService;
    
    private InformationResourceFile informationResourceFile;
    private Resource resource;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFileContentType() {
        return fileContentType;
    }

    public void setFileContentType(String fileContentType) {
        this.fileContentType = fileContentType;
    }

    public String getFileFileName() {
        return fileFileName;
    }

    public void setFileFileName(String fileFileName) {
        this.fileFileName = fileFileName;
    }

    @Override
    @PostOnly
    @WriteableSession
    @HttpsOnly
    @Action(value = "saveArchivalVersion",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
    results = { @Result(name = SUCCESS, type=TdarActionSupport.TDAR_REDIRECT, location="/resource/admin?id=${id}"),
            @Result(name = INPUT, type=TdarActionSupport.TDAR_REDIRECT, location="/resource/admin?id=${id}"),
    })
    public String execute() throws Exception {
        archivalFileSaveService.saveArchivalVersion(resource, informationResourceFile, file, fileFileName, getAuthenticatedUser());
        addActionMessage("file added successfully");
        return super.execute();
    }

    @Override
    public void prepare() throws Exception {
        resource = getGenericService().find(Resource.class, id);
        informationResourceFile = getGenericService().find(InformationResourceFile.class, fileId);
    }

    @Override
    public void validate() {
        getLogger().debug("id: {}, fid: {}, file:{}", id, fileId, file);
        if (file == null) {
            addActionError("missing.file");
        }
        if (PersistableUtils.isNullOrTransient(id)) {
            addActionError("missing.resource");
        }
        if (PersistableUtils.isNullOrTransient(fileId)) {
            addActionError("missing.file");
        }
        super.validate();
    }
    
    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
