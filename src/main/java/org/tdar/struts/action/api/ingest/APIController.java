package org.tdar.struts.action.api.ingest;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.FileProxies;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.FileAction;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.exception.APIException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.ImportService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.utils.jaxb.JaxbParsingException;
import org.tdar.utils.jaxb.JaxbResultContainer;
import org.tdar.utils.jaxb.JaxbValidationEvent;

@SuppressWarnings("serial")
@Namespace("/api/ingest")
@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_API_USER)
@HttpForbiddenErrorResponseOnly
@HttpsOnly
public class APIController extends AuthenticationAware.Base {

    @Autowired
    private transient AuthorizationService authorizationService;

    private List<File> uploadFile = new ArrayList<>();
    private List<String> uploadFileFileName = new ArrayList<>();
    private String record;
    private String msg;
    private StatusCode status;
    private Long projectId; // note this will override projectId value specified in record
    // on the receiving end
    private List<String> processedFileNames;

    @Autowired
    private transient SerializationService serializationService;
    @Autowired
    private transient ObfuscationService obfuscationService;
    @Autowired
    private transient ResourceService resourceService;
    @Autowired
    private transient ImportService importService;
    @Autowired
    private transient BillingAccountService accountService;

    private Resource importedRecord;
    private String message;
    // private List<String> restrictedFiles = new ArrayList<>();
    // private FileAccessRestriction fileAccessRestriction;
    private Long id;
    private InputStream inputStream;
    private JaxbResultContainer xmlResultObject = new JaxbResultContainer();

    private Long accountId;
    public final static String msg_ = "%s is %s %s (%s): %s";

    private void logMessage(String action_, Class<?> cls, Long id_, String name_) {
        getLogger().info(String.format(msg_, getAuthenticatedUser().getEmail(), action_, cls.getSimpleName().toUpperCase(), id_, name_));
    }

    @Action(value = "upload",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = "xmldocument", params = { "statusCode", "${status.httpStatusCode}" }),
                    @Result(name = ERROR, type = "xmldocument", params = { "statusCode", "${status.httpStatusCode}" })
            })
    @PostOnly
    public String upload() {

        if (StringUtils.isEmpty(getRecord())) {
            getLogger().info("no record defined");
            errorResponse(StatusCode.BAD_REQUEST);
            return ERROR;
        }

        try {
            Resource incoming = (Resource) serializationService.parseXml(new StringReader(getRecord()));
            // I don't know that this is "right"
            xmlResultObject.setRecordId(incoming.getId());
            TdarUser authenticatedUser = getAuthenticatedUser();
            List<FileProxy> fileProxies = new ArrayList<FileProxy>();
            if (incoming instanceof InformationResource) {
                fileProxies.addAll(((InformationResource) incoming).getFileProxies());
            }
            getLogger().debug("File Proxies: {}", fileProxies);
            for (int i = 0; i < uploadFile.size(); i++) {
                boolean seen = false;
                String name = uploadFileFileName.get(i);
                File file = uploadFile.get(i);
                for (FileProxy proxy : fileProxies) {
                    if (Objects.equals(proxy.getFilename(), name)) {
                        getLogger().debug("{} -- {}", proxy, name);
                        proxy.setFile(file);
                        seen = true;
                    }
                }
                if (seen == false) {
                    FileProxy proxy = new FileProxy(name, file, VersionType.UPLOADED);
                    proxy.setAction(FileAction.ADD);
                    fileProxies.add(proxy);
                    getLogger().debug("{} -- {}", proxy, name);
                }

            }

            Resource loadedRecord = importService.bringObjectOntoSession(incoming, authenticatedUser, fileProxies, projectId, true);
            updateQuota(getGenericService().find(BillingAccount.class, getAccountId()), loadedRecord);

            setImportedRecord(loadedRecord);
            setId(loadedRecord.getId());

            message = "updated:" + loadedRecord.getId();
            StatusCode code = StatusCode.UPDATED;
            status = StatusCode.UPDATED;
            int statuscode = StatusCode.UPDATED.getHttpStatusCode();
            if (loadedRecord.isCreated()) {
                status = StatusCode.CREATED;
                message = "created:" + loadedRecord.getId();
                code = StatusCode.CREATED;
                getXmlResultObject().setRecordId(loadedRecord.getId());
                statuscode = StatusCode.CREATED.getHttpStatusCode();
            }

            logMessage(" API " + code.name(), loadedRecord.getClass(), loadedRecord.getId(), loadedRecord.getTitle());

            getXmlResultObject().setStatusCode(statuscode);
            getXmlResultObject().setStatus(code.toString());
            resourceService.logResourceModification(loadedRecord, authenticatedUser, message + " " + loadedRecord.getTitle());
            xmlResultObject.setMessage(message);
            if (getLogger().isTraceEnabled()) {
                getLogger().trace(serializationService.convertToXML(loadedRecord));
            }
            return SUCCESS;
        } catch (Exception e) {
            message = "";
            if (e instanceof JaxbParsingException) {
                getLogger().debug("Could not parse the xml import", e);
                final List<JaxbValidationEvent> events = ((JaxbParsingException) e).getEvents();
                List<String> errors = new ArrayList<>();
                for (JaxbValidationEvent event : events) {
                    errors.add(event.toString());
                }

                errorResponse(StatusCode.BAD_REQUEST);
                getXmlResultObject().setMessage(message);
                getXmlResultObject().setErrors(errors);
                return ERROR;
            }
            getLogger().debug("an exception occured when processing the xml import", e);
            Throwable exp = e;
            List<String> stackTraces = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            do {
                errors.add(((exp.getMessage() == null) ? " ? " : exp.getMessage()));
                exp = exp.getCause();
                stackTraces.add(ExceptionUtils.getFullStackTrace(exp));
            } while (exp != null);
            if (e instanceof APIException) {
                getXmlResultObject().setMessage(e.getMessage());
                getXmlResultObject().setStackTraces(stackTraces);
                getXmlResultObject().setErrors(errors);
                errorResponse(((APIException) e).getCode());
                return ERROR;
            }
        }
        errorResponse(StatusCode.UNKNOWN_ERROR);
        return ERROR;

    }
    
    
    @Action(value = "updateFiles",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = "xmldocument", params = { "statusCode", "${status.httpStatusCode}" }),
                    @Result(name = ERROR, type = "xmldocument", params = { "statusCode", "${status.httpStatusCode}" })
            })
    @PostOnly
    public String updateFiles() {

        if (StringUtils.isEmpty(getRecord())) {
            getLogger().info("no record defined");
            errorResponse(StatusCode.BAD_REQUEST);
            return ERROR;
        }

        try {
            InformationResource incoming = getGenericService().find(InformationResource.class, id); 
            // I don't know that this is "right"
            xmlResultObject.setRecordId(getId());
            TdarUser authenticatedUser = getAuthenticatedUser();
            FileProxies fileProxies = (FileProxies) serializationService.parseXml(FileProxies.class, new StringReader(getRecord()));
            getLogger().debug("File Proxies: {}", fileProxies);
            List<FileProxy> fileProxyList = new ArrayList();
            for (int i = 0; i < uploadFile.size(); i++) {
                boolean seen = false;
                String name = uploadFileFileName.get(i);
                File file = uploadFile.get(i);
                for (FileProxy proxy : fileProxies.getFileProxies()) {
                    if (Objects.equals(proxy.getFilename(), name)) {
                        getLogger().debug("{} -- {}", proxy, name);
                        proxy.setFile(file);
                        seen = true;
                    }
                }
                if (seen == false) {
                    FileProxy proxy = new FileProxy(name, file, VersionType.UPLOADED);
                    proxy.setAction(FileAction.ADD);
                    fileProxyList.add(proxy);
                    getLogger().debug("{} -- {}", proxy, name);
                }

            }

            Resource loadedRecord = importService.processFileProxies(incoming, fileProxyList, getAuthenticatedUser());
            updateQuota(getGenericService().find(BillingAccount.class, getAccountId()), loadedRecord);

            setImportedRecord(loadedRecord);

            message = "updated:" + loadedRecord.getId();
            StatusCode code = StatusCode.UPDATED;
            status = StatusCode.UPDATED;
            int statuscode = StatusCode.UPDATED.getHttpStatusCode();

            logMessage(" API " + code.name(), loadedRecord.getClass(), loadedRecord.getId(), loadedRecord.getTitle());

            getXmlResultObject().setStatusCode(statuscode);
            getXmlResultObject().setStatus(code.toString());
            resourceService.logResourceModification(loadedRecord, authenticatedUser, message + " " + loadedRecord.getTitle());
            xmlResultObject.setMessage(message);
            if (getLogger().isTraceEnabled()) {
                getLogger().trace(serializationService.convertToXML(loadedRecord));
            }
            return SUCCESS;
        } catch (Exception e) {
            message = "";
            if (e instanceof JaxbParsingException) {
                getLogger().debug("Could not parse the xml import", e);
                final List<JaxbValidationEvent> events = ((JaxbParsingException) e).getEvents();
                List<String> errors = new ArrayList<>();
                for (JaxbValidationEvent event : events) {
                    errors.add(event.toString());
                }

                errorResponse(StatusCode.BAD_REQUEST);
                getXmlResultObject().setMessage(message);
                getXmlResultObject().setErrors(errors);
                return ERROR;
            }
            getLogger().debug("an exception occured when processing the xml import", e);
            Throwable exp = e;
            List<String> stackTraces = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            do {
                errors.add(((exp.getMessage() == null) ? " ? " : exp.getMessage()));
                exp = exp.getCause();
                stackTraces.add(ExceptionUtils.getFullStackTrace(exp));
            } while (exp != null);
            if (e instanceof APIException) {
                getXmlResultObject().setMessage(e.getMessage());
                getXmlResultObject().setStackTraces(stackTraces);
                getXmlResultObject().setErrors(errors);
                errorResponse(((APIException) e).getCode());
                return ERROR;
            }
        }
        errorResponse(StatusCode.UNKNOWN_ERROR);
        return ERROR;

    }

    private String errorResponse(StatusCode statusCode) {
        status = statusCode;
        xmlResultObject.setStatus(statusCode.toString());
        xmlResultObject.setStatusCode(statusCode.getHttpStatusCode());
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

    public void setStatus(StatusCode status) {
        this.status = status;
    }

    public StatusCode getStatus() {
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

    public void updateQuota(BillingAccount account, Resource resource) {
        if (getTdarConfiguration().isPayPerIngestEnabled()) {
            accountService.updateQuota(account, resource);
        }
    }

    public JaxbResultContainer getXmlResultObject() {
        return xmlResultObject;
    }

    public void setXmlResultObject(JaxbResultContainer xmlResultContainer) {
        this.xmlResultObject = xmlResultContainer;
    }

}