package org.tdar.struts.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.tools.ant.filters.StringInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.exception.APIException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.FilestoreService;
import org.tdar.core.service.ImportService;
import org.tdar.utils.Pair;

@SuppressWarnings("serial")
@Namespace("/api")
@Component
@Scope("prototype")
@ParentPackage("secured")
public class APIController extends AuthenticationAware.Base {

    @Autowired
    FilestoreService filestoreService;

    private List<File> uploadFile = new ArrayList<File>();
    private List<String> uploadFileFileName = new ArrayList<String>();
    private String record;
    private String msg;
    private String status;
    private Long projectId;  //note this will override projectId value specified in record

    @Autowired
    public ImportService importService;

    // on the receiving end
    private List<String> processedFileNames;

    private Resource importedRecord;
    private String message;
    private Long id;

    @Action(value = "upload", results = {
            @Result(name = SUCCESS, type = "freemarker", location = "/WEB-INF/content/api.ftl", params = { "contentType", "text/plain" }),
            @Result(name = ERROR, type = "freemarker", location = "/WEB-INF/content/api.ftl", params = { "contentType", "text/plain" }) })
    public String upload() throws ClassNotFoundException, IOException {
        if (StringUtils.isEmpty(getRecord())) {
            logger.info("you must define a record");
            status = StatusCode.BAD_REQUEST.getResultString();
            getServletResponse().setStatus(StatusCode.BAD_REQUEST.getStatusCode());
            return ERROR;
        }
        List<Pair<String, InputStream>> filePairs = new ArrayList<Pair<String, InputStream>>();
        for (int i = 0; i < uploadFileFileName.size(); i++) {
            filePairs.add(new Pair<String, InputStream>(uploadFileFileName.get(i), new FileInputStream(uploadFile.get(i))));
        }

        try {
            Resource loadedRecord = importService.loadXMLFile(new StringInputStream(getRecord()), getAuthenticatedUser(), filePairs, projectId);
            setImportedRecord(loadedRecord);
            setId(loadedRecord.getId());

            if (loadedRecord.isCreated()) {
                status = StatusCode.CREATED.getResultString();
                message = "created:" + loadedRecord.getId();
                getServletResponse().setStatus(StatusCode.CREATED.getStatusCode());
                return SUCCESS;
            }
            status = StatusCode.UPDATED.getResultString();
            getServletResponse().setStatus(StatusCode.UPDATED.getStatusCode());
            return SUCCESS;
        } catch (Exception e) {
            getLogger().debug("an exception occured when processing the xml import", e);
            e.printStackTrace();
            StringBuilder error = new StringBuilder();
            error.append(e.getMessage());
            error.append("\r\n");
            error.append(ExceptionUtils.getStackTrace(e));
            message = error.toString();

            if (e instanceof APIException) {
                StatusCode code = ((APIException) e).getCode();
                status = code.getResultString();
                getServletResponse().setStatus(code.getStatusCode());
                return ERROR;
            }
        }
        status = StatusCode.UNKNOWN_ERROR.getResultString();
        getServletResponse().setStatus(StatusCode.UNKNOWN_ERROR.getStatusCode());
        return ERROR;
    }

    public List<File> getUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(List<File> uploadFile) {
        this.uploadFile = uploadFile;
    }

    public List<String> getUploadFileFileName() {
        return uploadFileFileName;
    }

    public void setUploadFileFileName(List<String> uploadFileFileName) {
        this.uploadFileFileName = uploadFileFileName;
    }

    public List<String> getProcessedFileNames() {
        return processedFileNames;
    }

    public void setProcessedFileNames(List<String> processedFileNames) {
        this.processedFileNames = processedFileNames;
    }

    /**
     * @param record
     *            the record to set
     */
    public void setRecord(String record) {
        this.record = record;
    }

    /**
     * @return the record
     */
    public String getRecord() {
        return record;
    }

    /**
     * @param importedRecord
     *            the importedRecord to set
     */
    public void setImportedRecord(Resource importedRecord) {
        this.importedRecord = importedRecord;
    }

    /**
     * @return the importedRecord
     */
    public Resource getImportedRecord() {
        return importedRecord;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setErrorMessage(String errorMessage) {
        this.message = errorMessage;
    }

    public String getErrorMessage() {
        return message;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

}