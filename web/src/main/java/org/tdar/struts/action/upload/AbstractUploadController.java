package org.tdar.struts.action.upload;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.file.TdarDir;
import org.tdar.core.exception.FileUploadException;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.utils.json.JacksonView;
import org.tdar.utils.json.JsonLookupFilter;
import org.tdar.web.service.WebPersonalFilestoreService;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@SuppressWarnings("serial")
@Namespace("/upload")
@Component
@Scope("prototype")
@ParentPackage("secured")
@Results({
        @Result(name = "exception", type = TdarActionSupport.HTTPHEADER, params = { "error", "500" }),
        @Result(name = TdarActionSupport.INPUT, type = TdarActionSupport.HTTPHEADER, params = { "error", "500" })
})
@HttpForbiddenErrorResponseOnly
public abstract class AbstractUploadController extends AbstractAuthenticatableAction implements Preparable, Validateable {

    @Autowired
    private transient WebPersonalFilestoreService filestoreService;

    private PersonalFilestoreTicket ticket;
    private Long parentId;
    private Long accountId;
    private BillingAccount account;
    private TdarDir parent;
    private List<File> uploadFile = new ArrayList<File>();
    private List<String> uploadFileContentType = new ArrayList<String>();
    private List<String> uploadFileFileName = new ArrayList<String>();
    private String callback;
    private Long informationResourceId;
    private int jsonContentLength;

    // this is the groupId that comes back to us from the the various upload requests
    private Long ticketId;

    // indicates that client is not sending a ticket because the server should create a new ticket for this upload
    private boolean ticketRequested = false;

    @Autowired
    private WebPersonalFilestoreService personalFilestoreService;

    // @Action(value = "index", results = { @Result(name = SUCCESS, location = "index.ftl") })
    // public String index() {
    //
    // // get a claimcheck that all uploads will use
    // // personalFilestoreTicket = filestoreService.createPersonalFilestoreTicket(getAuthenticatedUser());
    // return SUCCESS;
    // }

    @Override
    public void validate() {
        if (CollectionUtils.isEmpty(uploadFile)) {
            addActionError(getText("uploadController.no_files"));
        }

//        if (account == null && getTdarConfiguration().isPayPerIngestEnabled()) {
//            addActionError(getText("uploadController.no_valid_account"));
//        }

        if (account != null && !getAuthorizationService().canEditAccount(getAuthenticatedUser(), account)) {
            addActionError(getText("uploadController.no_valid_account"));
        }

        super.validate();
    }

    @Override
    public void prepare() throws Exception {
        if (ticketRequested) {
            setTicket(personalFilestoreService.grabTicket(getAuthenticatedUser()));
            ticketId = getTicket().getId();
            getLogger().debug("UPLOAD CONTROLLER: on-demand ticket requested: {}", getTicket());
        } else {
            setTicket(getGenericService().find(PersonalFilestoreTicket.class, ticketId));
            getLogger().debug("UPLOAD CONTROLLER: upload request with ticket included: {}", getTicket());
            if (getTicket() == null) {
                addActionError(getText("uploadController.require_valid_ticket"));
            }
        }

        if (parentId != null) {
            parent = getGenericService().find(TdarDir.class, parentId);
        }
        if (accountId != null) {
            account = getGenericService().find(BillingAccount.class, accountId);
        }
    }

    public String upload() {
        getLogger().info("UPLOAD CONTROLLER: called with " + uploadFile.size() + " tkt:" + ticketId);

        List<String> hashCodes = new ArrayList<>();
        try {
            hashCodes = filestoreService.store(getAuthenticatedUser(), uploadFile, uploadFileFileName, uploadFileContentType, getTicket(), this,
                    account, parent);
        } catch (FileUploadException fue) {
            addActionErrorWithException("uploadController.could_not_store", fue);
            buildJsonError();
            return ERROR;
        }

        buildResultsOutput(hashCodes);

        return SUCCESS;
    }

    private void buildResultsOutput(List<String> hashCodes) {
        Map<String, Object> result = buildResults(hashCodes);
        result.put("ticket", getTicket());
        this.setResultObject(result);
        this.setJsonView(JsonLookupFilter.class);
    }

    private Map<String, Object> buildResults(List<String> hashCodes) {
        Map<String, Object> result = new HashMap<>();
        ArrayList<HashMap<String, Object>> files = new ArrayList<HashMap<String, Object>>();
        result.put("files", files);
        for (int i = 0; i < uploadFileFileName.size(); i++) {
            HashMap<String, Object> file = new HashMap<>();
            files.add(file);
            file.put("name", uploadFileFileName.get(i));
            if (CollectionUtils.isNotEmpty(hashCodes)) {
                file.put("hashCode", hashCodes.get(i));
            }
            if (CollectionUtils.isNotEmpty(getUploadFileSize())) {
                file.put("size", getUploadFileSize().get(i));
            }
            if (CollectionUtils.isNotEmpty(getUploadFileContentType())) {
                file.put("type", getUploadFileContentType().get(i));
            }
            file.put("delete_type", "DELETE");
        }
        return result;
    }

    private Object resultObject;
    private Class<? extends JacksonView> jsonView;

    public Object getResultObject() {
        return resultObject;
    }

    public Class<? extends JacksonView> getJsonView() {
        return jsonView;
    }

    // construct a json result expected by js client (currently dictated by jquery-blueimp-fileupload)
    protected void buildJsonError() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("ticket", ticketId);
        result.put("errors", getActionErrors());
        getLogger().warn("upload request encountered actionErrors: {}", getActionErrors());
        this.setResultObject(result);
    }

    public List<File> getUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(List<File> uploadFile) {
        this.uploadFile = uploadFile;
    }

    public List<String> getUploadFileContentType() {
        return uploadFileContentType;
    }

    public void setUploadFileContentType(List<String> uploadFileContentType) {
        this.uploadFileContentType = uploadFileContentType;
    }

    public List<String> getUploadFileFileName() {
        return uploadFileFileName;
    }

    public void setUploadFileFileName(List<String> uploadFileFileName) {
        this.uploadFileFileName = uploadFileFileName;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    /**
     * @return list of file sizes. Struts uses this naming convention for contentType and fileName lists, so we do the same here
     */
    public List<Long> getUploadFileSize() {
        List<Long> sizes = new ArrayList<Long>();
        for (File file : uploadFile) {
            sizes.add(file.length());
        }
        return sizes;
    }

    public Long getInformationResourceId() {
        return informationResourceId;
    }

    public void setInformationResourceId(Long informationResourceId) {
        this.informationResourceId = informationResourceId;
    }

    public int getJsonContentLength() {
        return jsonContentLength;
    }

    public boolean isTicketRequested() {
        return ticketRequested;
    }

    public void setTicketRequested(boolean ticketRequested) {
        this.ticketRequested = ticketRequested;
    }

    protected void setResultObject(Object resultObject) {
        this.resultObject = resultObject;
    }

    protected void setJsonView(Class<? extends JacksonView> jsonView) {
        this.jsonView = jsonView;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public PersonalFilestoreTicket getTicket() {
        return ticket;
    }

    public void setTicket(PersonalFilestoreTicket ticket) {
        this.ticket = ticket;
    }

}