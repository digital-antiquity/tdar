package org.tdar.struts.action.export;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.service.resource.ResourceExportService;
import org.tdar.struts_base.action.TdarActionSupport;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.StringLengthFieldValidator;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/export")
public class ResourceExportDownloadAction extends TdarActionSupport implements Preparable {

    private static final long serialVersionUID = -2267784497236541448L;
    private InputStream inputStream;
    private String filename;

    @Autowired
    private ResourceExportService resourceExportService;

    @Override
    public void prepare() throws Exception {
        try {
            inputStream = new FileInputStream(resourceExportService.retrieveFile(filename));
        } catch (FileNotFoundException e) {
            addActionError(e.getLocalizedMessage());
        }
    }

    @Override
    @Action(value = "download", results = {
            @Result(name = SUCCESS, type = "stream",
                    params = {
                            "contentType", "application/zip",
                            "inputName", "inputStream",
                            "contentDisposition", "Content-Disposition:attachment;filename=\"${filename}\""
                    }),
            @Result(name = INPUT, location="request.ftl")
    })
    public String execute() {
        return SUCCESS;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getFilename(){
        return filename;
    }

    @RequiredStringValidator(message = "no filename provided")
    @StringLengthFieldValidator(message = "filename invalid", maxLength = "100", minLength = "1")
    public void setFilename(String filename) {
        this.filename = filename;
    }

}
