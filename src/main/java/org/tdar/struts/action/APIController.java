package org.tdar.struts.action;

import java.io.File;
import java.io.IOException;
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
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.exception.APIException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.ImportService;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.struts.data.FileProxy;

@SuppressWarnings("serial")
@Namespace("/api")
@Component
@Scope("prototype")
@ParentPackage("secured")
public class APIController extends AuthenticationAware.Base {

    @Autowired
    PersonalFilestoreService filestoreService;

    private List<File> uploadFile = new ArrayList<File>();
    private List<String> uploadFileFileName = new ArrayList<String>();
    private String record;
    private String msg;
    private String status;
    private Long projectId; // note this will override projectId value specified in record

    @Autowired
    public ImportService importService;

    // on the receiving end
    private List<String> processedFileNames;

    private Resource importedRecord;
    private String message;
    private List<String> confidentialFiles = new ArrayList<String>();
    private Long id;
    public final static String msg_ = "%s is %s %s (%s): %s";

    private void logMessage(String action_, Class<?> cls, Long id_, String name_) {
        logger.info(String.format(msg_, getAuthenticatedUser().getEmail(), action_, cls.getSimpleName().toUpperCase(), id_, name_));
    }

    @Action(value = "upload", results = {
            @Result(name = SUCCESS, type = "freemarker", location = "/WEB-INF/content/api.ftl", params = { "contentType", "text/plain" }),
            @Result(name = ERROR, type = "freemarker", location = "/WEB-INF/content/api.ftl", params = { "contentType", "text/plain" }) })
    public String upload() throws ClassNotFoundException, IOException {
        if (StringUtils.isEmpty(getRecord())) {
            logger.info("you must define a record");
            status = StatusCode.BAD_REQUEST.getResultName();
            getServletResponse().setStatus(StatusCode.BAD_REQUEST.getHttpStatusCode());
            return ERROR;
        }
        List<FileProxy> proxies = new ArrayList<FileProxy>();
        for (int i = 0; i < uploadFileFileName.size(); i++) {
            FileProxy proxy = new FileProxy(uploadFileFileName.get(i), uploadFile.get(i), VersionType.UPLOADED, FileAction.ADD);
            if (confidentialFiles.contains(uploadFileFileName.get(i))) {
                proxy.setConfidential(true);
            }
            proxies.add(proxy);
        }

        try {
            Resource loadedRecord = importService.loadXMLFile(new StringInputStream(getRecord()), getAuthenticatedUser(), proxies, projectId);
            setImportedRecord(loadedRecord);
            setId(loadedRecord.getId());

            logMessage("SAVING", loadedRecord.getClass(), loadedRecord.getId(), loadedRecord.getTitle());
            if (loadedRecord.isCreated()) {
                status = StatusCode.CREATED.getResultName();
                message = "created:" + loadedRecord.getId();
                getServletResponse().setStatus(StatusCode.CREATED.getHttpStatusCode());
                return SUCCESS;
            }
            status = StatusCode.UPDATED.getResultName();
            getServletResponse().setStatus(StatusCode.UPDATED.getHttpStatusCode());
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
                status = code.getResultName();
                getServletResponse().setStatus(code.getHttpStatusCode());
                return ERROR;
            }
        }
        status = StatusCode.UNKNOWN_ERROR.getResultName();
        getServletResponse().setStatus(StatusCode.UNKNOWN_ERROR.getHttpStatusCode());
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

    public List<String> getConfidentialFiles() {
        return confidentialFiles;
    }

    public void setConfidentialFiles(List<String> confidentialFiles) {
        this.confidentialFiles = confidentialFiles;
    }

}