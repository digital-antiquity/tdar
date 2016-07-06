package org.tdar.struts.action.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.GenericService;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/resource/admin")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
public class AdminResourceXmlDownload extends TdarActionSupport implements Preparable {

    private static final long serialVersionUID = -1603500145262991935L;
    private Long id;
    private String filename;
    private InputStream inputStream;

    @Autowired
    private GenericService genericService;

    private Filestore FILESTORE = TdarConfiguration.getInstance().getFilestore();

    @Action(value="xml", results = {
            @Result(name = TdarActionSupport.SUCCESS, type = "stream",
                    params = {
                            "contentType", "application/xml",
                            "inputName", "inputStream",
                            "contentDisposition", "Content-Disposition:attachment;filename=\"$filename}\""
                    })
    })
    public String execute() throws Exception {
        return SUCCESS;
    };

    @Override
    public void prepare() throws Exception {
        Resource r = genericService.find(Resource.class, getId());
        if (r == null) {
            addActionError("Resource does not exist");
            return;
        }
        File f = FILESTORE.getXmlRecordFile(FilestoreObjectType.RESOURCE, id, filename);
        if (!f.exists()) {
            addActionError("File does not exist");
            return;
        }
        setInputStream(new FileInputStream(f));

    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
    
}
