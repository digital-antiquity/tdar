package org.tdar.struts.action.api.ingest;

import java.io.File;
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
import org.apache.tomcat.logging.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.FileProxies;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.Coupon;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.APIException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.ImportService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.action.api.AbstractApiController;
import org.tdar.struts.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.struts.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.jaxb.JaxbParsingException;

@SuppressWarnings("serial")
@Namespace("/api/ingest")
@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_API_USER)
@HttpForbiddenErrorResponseOnly
@HttpsOnly
public class APIController extends AbstractApiController {

    @Autowired
    private transient AuthorizationService authorizationService;

    private List<File> uploadFile = new ArrayList<>();
    private List<String> uploadFileFileName = new ArrayList<>();
    private Long projectId; // note this will override projectId value specified in record

    // on the receiving end
    private List<String> processedFileNames;

    @Autowired
    private transient SerializationService serializationService;
    @Autowired
    private transient ResourceService resourceService;
    @Autowired
    private transient ImportService importService;
    @Autowired
    private transient BillingAccountService accountService;

    private Resource importedRecord;

    private Long accountId;
    private Long couponNumberOfFiles = -1L;

    @Action(value = "upload",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = "xmldocument", params = { "statusCode", "${status.httpStatusCode}" }),
                    @Result(name = ERROR, type = "xmldocument", params = { "statusCode", "${status.httpStatusCode}" })
            })
    @PostOnly
    // @WriteableSession
    public String upload() {
        List<String> stackTraces = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        if (StringUtils.isEmpty(getRecord())) {
            getLogger().info("no record defined");
            errorResponse(StatusCode.BAD_REQUEST, null, null, null);
            return ERROR;
        }

        try {
            Resource incoming = (Resource) serializationService.parseXml(new StringReader(getRecord()));
            // I don't know that this is "right"

            getXmlResultObject().setRecordId(incoming.getId());
            TdarUser authenticatedUser = getAuthenticatedUser();
            List<FileProxy> fileProxies = new ArrayList<FileProxy>();
            if (incoming instanceof InformationResource) {
                fileProxies.addAll(((InformationResource) incoming).getFileProxies());
            }
            getLogger().debug("File Proxies: {}", fileProxies);
            processIncomingFileProxies(fileProxies);

            Resource loadedRecord = importService.bringObjectOntoSession(incoming, authenticatedUser, fileProxies, projectId, true);
            BillingAccount billingAccount = getGenericService().find(BillingAccount.class, getAccountId());
            updateQuota(billingAccount, loadedRecord);

            setImportedRecord(loadedRecord);
            setId(loadedRecord.getId());

            RevisionLogType type = RevisionLogType.EDIT;
            setStatusMessage(StatusCode.UPDATED, "updated:" + loadedRecord.getId());
            int statuscode = StatusCode.UPDATED.getHttpStatusCode();
            if (loadedRecord.isCreated()) {
                setStatusMessage(StatusCode.CREATED, "created:" + loadedRecord.getId());
                type = RevisionLogType.CREATE;
                getXmlResultObject().setRecordId(loadedRecord.getId());
                getXmlResultObject().setId(loadedRecord.getId());
                statuscode = StatusCode.CREATED.getHttpStatusCode();
            } else {
                reconcileAccountId(loadedRecord);
            }

            logMessage(" API " + getStatus().name(), loadedRecord.getClass(), loadedRecord.getId(), loadedRecord.getTitle());

            if (getCouponNumberOfFiles() > 0 && billingAccount != null) {
                Coupon coupon = accountService.generateCouponCode(billingAccount, getCouponNumberOfFiles(), null, DateTime.now().plusYears(1).toDate());
                coupon.getResourceIds().add(loadedRecord.getId());
                getGenericService().saveOrUpdate(coupon);
            }
            getXmlResultObject().setStatusCode(statuscode);
            getXmlResultObject().setStatus(getStatus().toString());
            resourceService.logResourceModification(loadedRecord, authenticatedUser, getErrorMessage() + " " + loadedRecord.getTitle(), type);
            getXmlResultObject().setMessage(getErrorMessage());
            if (getLogger().isTraceEnabled()) {
                getLogger().trace(serializationService.convertToXML(loadedRecord));
            }

            return SUCCESS;
        } catch (Throwable e) {
            setErrorMessage("");
            if (e instanceof JaxbParsingException) {
                getLogger().debug("Could not parse the xml import", e);
                final List<String> events = ((JaxbParsingException) e).getEvents();
                errors = new ArrayList<>(events);

                errorResponse(StatusCode.BAD_REQUEST, errors, getErrorMessage(), null);
                return ERROR;
            }
            getLogger().debug("an exception occured when processing the xml import", e);
            Throwable cause = ExceptionUtils.getRootCause(e);
            if (cause == null) {
                cause = e;
            }
            stackTraces.add(ExceptionUtils.getFullStackTrace(cause));
            if (cause.getLocalizedMessage() != null) {
                try {
                    errors.add(cause.getLocalizedMessage());
                } catch (Exception ex) {
                    errors.add(cause.getMessage());
                }
            }

            if (e instanceof APIException) {
                errorResponse(((APIException) e).getCode(), errors, e.getMessage(), stackTraces);
                return ERROR;
            }
        }
        errorResponse(StatusCode.UNKNOWN_ERROR, errors, getErrorMessage(), stackTraces);
        return ERROR;

    }

    private void processIncomingFileProxies(List<FileProxy> fileProxies) {
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
    }

    @Action(value = "updateFiles",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = "xmldocument", params = { "statusCode", "${status.httpStatusCode}" }),
                    @Result(name = ERROR, type = "xmldocument", params = { "statusCode", "${status.httpStatusCode}" })
            })
    @PostOnly
    @WriteableSession
    public String updateFiles() {

        if (StringUtils.isEmpty(getRecord())) {
            getLogger().info("no record defined");
            errorResponse(StatusCode.BAD_REQUEST, null, null, null);
            return ERROR;
        }

        try {
            InformationResource incoming = getGenericService().find(InformationResource.class, getId());
            if (!authorizationService.canUploadFiles(getAuthenticatedUser(), incoming)) {
                errorResponse(StatusCode.FORBIDDEN, null, null, null);
                return ERROR;
            }
            // I don't know that this is "right"
            incoming = getGenericService().markWritableOnExistingSession(incoming);
            getXmlResultObject().setRecordId(getId());
            getXmlResultObject().setId(getId());
            TdarUser authenticatedUser = getAuthenticatedUser();
            FileProxies fileProxies = (FileProxies) serializationService.parseXml(FileProxies.class, new StringReader(getRecord()));
            List<FileProxy> incomingList = fileProxies.getFileProxies();
            getLogger().debug("File Proxies: {}", incomingList);
            processIncomingFileProxies(incomingList);

            Resource loadedRecord = importService.processFileProxies(incoming, incomingList, getAuthenticatedUser());
            reconcileAccountId(loadedRecord);

            updateQuota(getGenericService().find(BillingAccount.class, getAccountId()), loadedRecord);

            setImportedRecord(loadedRecord);

            setStatusMessage(StatusCode.UPDATED, "updated:" + loadedRecord.getId());
            int statuscode = StatusCode.UPDATED.getHttpStatusCode();

            logMessage(" API " + getStatus().name(), loadedRecord.getClass(), loadedRecord.getId(), loadedRecord.getTitle());

            getXmlResultObject().setStatusCode(statuscode);
            getXmlResultObject().setStatus(getStatus().toString());
            resourceService.logResourceModification(loadedRecord, authenticatedUser, getErrorMessage() + " " + loadedRecord.getTitle(), RevisionLogType.EDIT);
            getXmlResultObject().setMessage(getErrorMessage());
            if (getLogger().isTraceEnabled()) {
                getLogger().trace(serializationService.convertToXML(loadedRecord));
            }
            return SUCCESS;
        } catch (Throwable e) {
            setErrorMessage("");
            if (e instanceof JaxbParsingException) {
                getLogger().debug("Could not parse the xml import", e);
                final List<String> events = ((JaxbParsingException) e).getEvents();
                errorResponse(StatusCode.BAD_REQUEST, events, getErrorMessage(), null);
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
                errorResponse(((APIException) e).getCode(), errors, e.getMessage(), stackTraces);
                return ERROR;
            }
        }
        errorResponse(StatusCode.UNKNOWN_ERROR, null, null, null);
        return ERROR;

    }

    private void reconcileAccountId(Resource loadedRecord) {
        if (!getTdarConfiguration().isPayPerIngestEnabled()) {
            return;
        }
        if (PersistableUtils.isNullOrTransient(getAccountId())) {
            accountService.updateTransientAccountInfo(loadedRecord);
            if (PersistableUtils.isNotNullOrTransient(loadedRecord.getAccount())) {
                setAccountId(loadedRecord.getAccount().getId());
            }
        }
        if (PersistableUtils.isNullOrTransient(getAccountId()) && isAdministrator()) {
            setAccountId(TdarConfiguration.getInstance().getAdminBillingAccountId());
        }
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

    // the command line tool passes this property in: but we don't need it.
    public void setUploadedItem(String path) {
        getLogger().debug("Path of uploaded item is: " + path);
    }

    // the command line tool passes this property in: but we don't need it.
    public void setUploadFileContentType(String type) {
        getLogger().debug("Contenty type of uploaded item is: " + type);
    }

    public void updateQuota(BillingAccount account_, Resource resource) {
        if (getTdarConfiguration().isPayPerIngestEnabled()) {
            BillingAccount account = getGenericService().markWritableOnExistingSession(account_);
            accountService.updateQuota(account, getAuthenticatedUser(), resource);
        }
    }

    public Long getCouponNumberOfFiles() {
        return couponNumberOfFiles;
    }

    public void setCouponNumberOfFiles(Long couponNumberOfFiles) {
        this.couponNumberOfFiles = couponNumberOfFiles;
    }

}