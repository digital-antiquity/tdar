package org.tdar.struts.action.api.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.jena.ext.com.google.common.base.Objects;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.struts.action.AbstractAuthenticatableAction;

import com.opensymphony.xwork2.Preparable;

/**
 * $Id$
 * 
 * Data integration activities in the workspace.
 * 
 * @author Allen Lee, Adam Brin
 * @version $Rev$
 */
@ParentPackage("secured")
@Namespace("/file")
@Component
@Scope("prototype")
public class DownloadFileAction extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = -8260354072890503475L;

    @Autowired
    private transient PersonalFilestoreService filestoreService;

    private Long id;

    private String filename;
    private String contentType;
    private long contentLength;
    private transient InputStream inputStream;

    @Action(value = "download/{id}", results = {
            @Result(name = SUCCESS, type = "stream",
                    params = {
                            // "contentType", "${contentType}",
                            "inputName", "inputStream",
                            "contentDisposition", "attachment;filename=\"${filename}\"",
                            "contentLength", "${contentLength}"
                    }),
            @Result(name = INPUT, type = TDAR_REDIRECT, location = "select-tables")
    })
    public String downloadIntegrationDataResults() {

        return SUCCESS;
    }

    @Override
    public void prepare() throws Exception {
        TdarFile tFile = getGenericService().find(TdarFile.class, id);

        if (!isEditor() && !getAuthorizationService().canEditAccount(getAuthenticatedUser(), tFile.getAccount()) &&
                !Objects.equal(tFile.getUploader(), getActionErrors())) {
            addActionError("downloadFileAction.cannot_downlood");
        }

        try {
            File file = new File(tFile.getLocalPath());
            inputStream = new FileInputStream(file);
            contentLength = file.length();
            filename = tFile.getName();
            // contentType = tFile.con

        } catch (Exception exception) {
            addActionErrorWithException("Unable to access file.", exception);
        }
    }

    public PersonalFilestoreService getFilestoreService() {
        return filestoreService;
    }

    public void setFilestoreService(PersonalFilestoreService filestoreService) {
        this.filestoreService = filestoreService;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

}
