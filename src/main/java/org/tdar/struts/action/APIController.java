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
import org.tdar.core.service.FilestoreService;
import org.tdar.core.service.ImportService;
import org.tdar.utils.Pair;

@SuppressWarnings("serial")
@Namespace("/api")
@Component
@Scope("prototype")
@ParentPackage("secured")
public class APIController extends AuthenticationAware.Base {

    public static final String NOT_FOUND = "notfound";
    public static final String CREATED = "created";
    public static final String UPDATED = "updated";
    public static final String BAD_REQUEST = "badrequest";
    public static final String NOTALLOWED = "notallowed";
    public static final String UNKNOWN_ERROR = "unknownerror";

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
    private String errorMessage;

    @Action(value = "upload", results = {
            @Result(name = APIController.NOT_FOUND,  location = "/WEB-INF/content/apierror.ftl" , type = "freemarker", params = { "contentType", "text/plain", "status", "404" }),// params = { },type = "httpheader",
            @Result(name = CREATED,  location = "/WEB-INF/content/apierror.ftl" , type = "httpheader", params = { "contentType", "text/plain", "error", "201", "errorMessage", "${errorMessage}" }),// params = { "status", "201" },type = "httpheader",
            @Result(name = UPDATED,  location = "/WEB-INF/content/apierror.ftl" , type = "httpheader", params = { "contentType", "text/plain", "status", "204" }),// params = { },type = "httpheader",
            @Result(name = APIController.BAD_REQUEST,  location = "/WEB-INF/content/apierror.ftl" , type = "httpheader", params = { "contentType", "text/plain","error", "400" , "errorMessage","${errorMessage}"}),// params = { },type = "httpheader",
            @Result(name = APIController.NOTALLOWED,  location = "/WEB-INF/content/apierror.ftl" , type = "httpheader", params = { "contentType", "text/plain","error", "403", "errorMessage","${errorMessage}" }), // params = {  },type = "httpheader",
            @Result(name = APIController.UNKNOWN_ERROR,  location = "/WEB-INF/content/apierror.ftl" , type = "httpheader", params = { "contentType", "text/plain","error", "500", "errorMessage","${errorMessage}" }) // params = {  },type = "httpheader",
            })
    public String upload() throws ClassNotFoundException, IOException {
        if (StringUtils.isEmpty(getRecord())) {
            logger.info("you must define a record");
            status = APIController.BAD_REQUEST;
            return APIController.BAD_REQUEST;
        }

        List<Pair<String, InputStream>> filePairs = new ArrayList<Pair<String, InputStream>>();
        for (int i = 0; i < uploadFileFileName.size(); i++) {
            filePairs.add(new Pair<String, InputStream>(uploadFileFileName.get(i), new FileInputStream(uploadFile.get(i))));
        }

        try {
            Resource loadedRecord = importService.loadXMLFile(new StringInputStream(getRecord()), getAuthenticatedUser(), filePairs, projectId);
            setImportedRecord(loadedRecord);
            
            //FIXME: this appears to not be accurate
            //FIXME2: should we pass back the tDAR ID?
            if (loadedRecord.isCreated()) {
                status = CREATED;
                String fmt = "Created %s with %s attachments";
                errorMessage = String.format(fmt, loadedRecord, uploadFileFileName.size());
                return CREATED;
            }
            status = UPDATED;
            return UPDATED;
        } catch (Exception e) {
            getLogger().debug("an exception occured when processing the xml import",e);
            e.printStackTrace();
            StringBuilder error = new StringBuilder();
            error.append(e.getMessage());
            error.append("\r\n");
            error.append(ExceptionUtils.getStackTrace(e));
            errorMessage = error.toString();
            
            if (e instanceof APIException) {
                status = ((APIException) e).getCode().getResultString();
                return status;
            }
        }
        status = APIController.UNKNOWN_ERROR;
        return APIController.UNKNOWN_ERROR;
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
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

}