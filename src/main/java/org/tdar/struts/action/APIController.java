package org.tdar.struts.action;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.exception.APIException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.ImportService;
import org.tdar.struts.data.FileProxy;
import org.tdar.utils.jaxb.JaxbParsingException;
import org.tdar.utils.jaxb.JaxbValidationEvent;

@SuppressWarnings("serial")
@Namespace("/api")
@Component
@Scope("prototype")
@ParentPackage("secured")
public class APIController extends AuthenticationAware.Base {

    private List<File> uploadFile = new ArrayList<>();
    private List<String> uploadFileFileName = new ArrayList<>();
    private String record;
    private String msg;
    private String status;
    private Long projectId; // note this will override projectId value specified in record

    // on the receiving end
    private List<String> processedFileNames;

    @Autowired
    private ImportService importService;

    private Resource importedRecord;
    private String message;
    private List<String> restrictedFiles = new ArrayList<>();
    private FileAccessRestriction fileAccessRestriction;
    private Long id;
    private InputStream inputStream;

    private Long accountId;
    public final static String msg_ = "%s is %s %s (%s): %s";

    private void logMessage(String action_, Class<?> cls, Long id_, String name_) {
        getLogger().info(String.format(msg_, getAuthenticatedUser().getEmail(), action_, cls.getSimpleName().toUpperCase(), id_, name_));
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
                getObfuscationService().obfuscate(resource, getAuthenticatedUser());
            }
            logMessage("API VIEWING", resource.getClass(), resource.getId(), resource.getTitle());
            String xml = getXmlService().convertToXML(resource);
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
            getLogger().info("file access restrictions not set");
            return errorResponse(StatusCode.BAD_REQUEST);
        } else if (StringUtils.isEmpty(getRecord())) {
            getLogger().info("no record defined");
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
            Resource incoming = (Resource) getXmlService().parseXml(new StringReader(getRecord()));
            // I don't know that this is "right"
            TdarUser authenticatedUser = getAuthenticatedUser();
            // getGenericService().detachFromSession(incoming);
            // getGenericService().detachFromSession(getAuthenticatedUser());
            Resource loadedRecord = importService.bringObjectOntoSession(incoming, authenticatedUser, proxies, projectId);
            updateQuota(getGenericService().find(Account.class, getAccountId()), loadedRecord);

            setImportedRecord(loadedRecord);
            setId(loadedRecord.getId());

            message = "updated:" + loadedRecord.getId();
            StatusCode code = StatusCode.UPDATED;
            status = StatusCode.UPDATED.getResultName();
            int statuscode = StatusCode.UPDATED.getHttpStatusCode();
            if (loadedRecord.isCreated()) {
                status = StatusCode.CREATED.getResultName();
                message = "created:" + loadedRecord.getId();
                code = StatusCode.CREATED;
                statuscode = StatusCode.CREATED.getHttpStatusCode();
            }

            logMessage(" API " + code.name(), loadedRecord.getClass(), loadedRecord.getId(), loadedRecord.getTitle());

            getServletResponse().setStatus(statuscode);
            getResourceService().logResourceModification(loadedRecord, authenticatedUser, message + " " + loadedRecord.getTitle());
            return SUCCESS;
        } catch (Exception e) {
            message = "";
            if (e instanceof JaxbParsingException) {
                getLogger().debug("Could not parse the xml import", e);
                final List<JaxbValidationEvent> events = ((JaxbParsingException) e).getEvents();
                for (JaxbValidationEvent event : events) {
                    message = message + event.toString() + "\r\n";
                }
                return errorResponse(StatusCode.BAD_REQUEST);
            }
            getLogger().debug("an exception occured when processing the xml import", e);
            Throwable exp = e;
            do {
                message = message + ((exp.getMessage() == null) ? " ? " : exp.getMessage());
                exp = exp.getCause();
                message = message + ((exp == null) ? "" : "\r\n");
            } while (exp != null);
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

    public String getMessage() {
        return message;
    }

    // the command line tool passes this property in: but we don't need it.
    public void setUploadedItem(String path) {
        getLogger().debug("Path of uploaded item is: " + path);
    }

    // the command line tool passes this property in: but we don't need it.
    public void setUploadFileContentType(String type) {
        getLogger().debug("Contenty type of uploaded item is: " + type);
    }
}