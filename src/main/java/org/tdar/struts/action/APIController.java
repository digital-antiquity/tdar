package org.tdar.struts.action;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.dao.external.auth.TdarGroup;
import org.tdar.core.exception.APIException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.ImportService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.core.service.XmlService;
import org.tdar.struts.RequiresTdarUserGroup;
import org.tdar.struts.data.FileProxy;

@SuppressWarnings("serial")
@Namespace("/api")
@Component
@Scope("prototype")
@ParentPackage("secured")
public class APIController extends AuthenticationAware.Base {

    @Autowired
    PersonalFilestoreService filestoreService;

    private List<File> uploadFile = new ArrayList<>();
    private List<String> uploadFileFileName = new ArrayList<>();
    private String record;
    private String msg;
    private String status;
    private Long projectId; // note this will override projectId value specified in record

    @Autowired
    public ImportService importService;
    @Autowired
    public XmlService xmlService;

    // on the receiving end
    private List<String> processedFileNames;

    private ObfuscationService obfuscationService;

    private Resource importedRecord;
    private String message;
    private List<String> restrictedFiles = new ArrayList<>();
    private FileAccessRestriction fileAccessRestriction;
    private Long id;
    private InputStream inputStream;

    private Long accountId;
    public final static String msg_ = "%s is %s %s (%s): %s";

    private void logMessage(String action_, Class<?> cls, Long id_, String name_) {
        logger.info(String.format(msg_, getAuthenticatedUser().getEmail(), action_, cls.getSimpleName().toUpperCase(), id_, name_));
    }

    @Action(value = "view", results = {
            @Result(name = SUCCESS, type = "stream", params = {
                    "contentType", "text/xml", "inputName",
                    "inputStream" })
    })
    public String view() throws Exception {
        if (Persistable.Base.isNotNullOrTransient(getId())) {
            Resource resource = getResourceService().find(getId());
            if (!isAdministrator() && !getAuthenticationAndAuthorizationService().canEdit(getAuthenticatedUser(), resource)) {
                obfuscationService.obfuscate(resource);
            }
            String xml = xmlService.convertToXML(resource);
            setInputStream(new ByteArrayInputStream(xml.getBytes()));
            return SUCCESS;
        }
        return INPUT;
    }

    @Action(value = "upload", results = {
            @Result(name = SUCCESS, type = "freemarker", location = "/WEB-INF/content/api.ftl", params = { "contentType", "text/plain" }),
            @Result(name = ERROR, type = "freemarker", location = "/WEB-INF/content/api.ftl", params = { "contentType", "text/plain" }) })
    public String upload() {
        if (fileAccessRestriction == null) {
            // If there is an error setting this field in the OGNL layer this method is still called...
            // This check means that if there was such an error, then we are not going to default to a weaker access restriction. 
            logger.info("file access restrictions not set");
            return errorResponse(StatusCode.BAD_REQUEST);
        } else if (StringUtils.isEmpty(getRecord())) {
            logger.info("no record defined");
            return errorResponse(StatusCode.BAD_REQUEST);
        }
        List<FileProxy> proxies = new ArrayList<>();
        for (int i = 0; i < uploadFileFileName.size(); i++) {
            FileProxy proxy = new FileProxy(uploadFileFileName.get(i), uploadFile.get(i), VersionType.UPLOADED, FileAction.ADD);
            if (restrictedFiles.contains(uploadFileFileName.get(i))) {
                proxy.setRestriction(fileAccessRestriction);
            }
            proxies.add(proxy);
        }

        try {
            Resource incoming = (Resource) xmlService.parseXml(new StringReader(getRecord()));
            // I don't know that this is "right"
            final Person authenticatedUser = getAuthenticatedUser();
            Resource loadedRecord = importService.bringObjectOntoSession(incoming, authenticatedUser, proxies, projectId);
            updateQuota(getGenericService().find(Account.class, getAccountId()), loadedRecord);

            setImportedRecord(loadedRecord);
            setId(loadedRecord.getId());

            logMessage("SAVING", loadedRecord.getClass(), loadedRecord.getId(), loadedRecord.getTitle());
            message = "updated:" + loadedRecord.getId();
            status = StatusCode.UPDATED.getResultName();
            int statuscode = StatusCode.UPDATED.getHttpStatusCode();
            if (loadedRecord.isCreated()) {
                status = StatusCode.CREATED.getResultName();
                message = "created:" + loadedRecord.getId();
                statuscode = StatusCode.CREATED.getHttpStatusCode();
            }

            getServletResponse().setStatus(statuscode);
            getResourceService().logResourceModification(loadedRecord, authenticatedUser, message + " " + loadedRecord.getTitle());
            return SUCCESS;
        } catch (Exception e) {
            getLogger().debug("an exception occured when processing the xml import", e);
            StringBuilder error = new StringBuilder();
            error.append(e.getMessage());
            error.append("\r\n");
            error.append(ExceptionUtils.getStackTrace(e));
            message = error.toString();

            if (e instanceof APIException) {
                return errorResponse(((APIException) e).getCode());
            }
        }
        return errorResponse(StatusCode.UNKNOWN_ERROR);
    }

    private String errorResponse(StatusCode statusCode) {
        status = statusCode.getResultName();
        getServletResponse().setStatus(statusCode.getHttpStatusCode());
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

    public List<String> getRestrictedFiles() {
        return restrictedFiles;
    }

    public void setRestrictedFiles(List<String> confidentialFiles) {
        this.restrictedFiles = confidentialFiles;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public FileAccessRestriction getFileAccessRestriction() {
        return fileAccessRestriction;
    }

    public void setFileAccessRestriction(FileAccessRestriction fileAccessRestriction) {
        this.fileAccessRestriction = fileAccessRestriction;
    }

}