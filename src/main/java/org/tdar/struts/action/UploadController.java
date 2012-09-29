package org.tdar.struts.action;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.tdar.URLConstants;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.core.service.XmlService;
import org.tdar.filestore.personal.PersonalFilestore;
import org.tdar.filestore.personal.PersonalFilestoreFile;
import org.tdar.struts.action.resource.AbstractResourceController;
import org.tdar.struts.data.FileProxy;

@SuppressWarnings("serial")
@Namespace("/upload")
@Component
@Scope("prototype")
@ParentPackage("secured")

//@Results({@Result(name = "exception", type = "stream",
//    params = {
//            "contentType", "application/json",
//            "inputName", "jsonInputStream"})
//})
public class UploadController extends AuthenticationAware.Base {

    @Autowired
    private PersonalFilestoreService filestoreService;
    @Autowired
    XmlService xmlService;

    private List<File> uploadFile = new ArrayList<File>();
    private List<String> uploadFileContentType = new ArrayList<String>();
    private List<Long> uploadFileSizes = new ArrayList<Long>();
    private List<String> uploadFileFileName = new ArrayList<String>();
    private PersonalFilestoreTicket personalFilestoreTicket;
    private String callback;
    private Long informationResourceId;
    InputStream jsonInputStream;
    private int jsonContentLength;

    // on the receiving end
    private List<String> processedFileNames;

    // this is the groupId that comes back to us from the the various upload requests
    private Long ticketId;

    // private static final long INVALID_TICKET_ID = -1L;

    @Action(value = "index", results = { @Result(name = "success", location = "index.ftl") })
    public String index() {

        // get a claimcheck that all uploads will use
        // personalFilestoreTicket = filestoreService.createPersonalFilestoreTicket(getAuthenticatedUser());
        return SUCCESS;
    }

    @Action(value = "upload", results = {
            @Result(name = SUCCESS, type = "freemarker", location = "results.ftl", params = { "contentType", "text/plain" }),
            @Result(name = ERROR, type = "freemarker", location = "error.ftl", params = { "contentType", "text/plain" })
            // FIXME: the line below does not work like i want it to. I'd like to render error.ftl *and* return a error status code.
            // @Result(name = ERROR, type = "freemarker", location = "error.ftl", params = { "contentType", "text/plain", "status", "400" })
    })
    public String upload() {
        PersonalFilestoreTicket ticket = getGenericService().find(PersonalFilestoreTicket.class, ticketId);
        logger.debug("UPLOAD CONTROLLER: called with " + uploadFile.size() + " tkt:" + ticketId);
        if (ticket == null) {
            addActionError("asynchronous uploads require a valid ticket"); // FIXME: yeah, like a user will understand this.
        }
        if (CollectionUtils.isEmpty(uploadFile)) {
            addActionError("No files in this request");
        }
        Person submitter = getAuthenticatedUser();
        for (int i = 0; i < uploadFile.size(); i++) {
            File file = uploadFile.get(i);
            String fileName = uploadFileFileName.get(i);
            // put upload in holding area to be retrieved later (maybe) by the informationResourceController
            if (file != null && file.exists()) {
                String contentType = "";
                try {
                    contentType = uploadFileContentType.get(i);
                } catch (Exception e) { /* OK, JUST USED FOR DEBUG */
                }
                Object[] out = { fileName, file.length(), contentType, ticketId };
                logger.debug("UPLOAD CONTROLLER: processing file: {} ({}) , contentType: {} , tkt: {}", out);
                PersonalFilestore filestore = filestoreService.getPersonalFilestore(submitter);
                try {
                    filestore.store(ticket, file, fileName);
                } catch (Exception e) {
                    addActionErrorWithException("Could not store file.", e);
                }
            }
        }
        if (CollectionUtils.isEmpty(getActionErrors())) {
            return SUCCESS;
        } else {
            // FIXME: I was hoping for an annotation-based way to return an error code *and* render a freemarker template. @Result annotations seem to only
            // allow one or the other (or I'm doing it wrong).
            logger.error("{}", getActionErrors());
            getServletResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 seems appropriate
            return ERROR;
        }
    }

    @Action(value = "list", results = { @Result(name = "success", type = "freemarker", location = "list.ftl") })
    public String list() {
        PersonalFilestore filestore = filestoreService.getPersonalFilestore(getAuthenticatedUser());
        PersonalFilestoreTicket formGroup = getGenericService().find(PersonalFilestoreTicket.class, ticketId);
        List<PersonalFilestoreFile> processedFiles = filestore.retrieveAll(formGroup);
        processedFileNames = new ArrayList<String>();
        for (PersonalFilestoreFile pf : processedFiles) {
            processedFileNames.add(pf.getFile().getName());
        }
        return "success";
    }

    // FIXME: generate a JsonResult rather than put these in an ftl
    // @PostOnly
    @Action(value = "grab-ticket", results = { @Result(name = "success", type = "freemarker", location = "grab-ticket.ftl",
            params = { "contentType", "text/plain" }) })
    public String grabTicket() {
        personalFilestoreTicket = filestoreService.createPersonalFilestoreTicket(getAuthenticatedUser());
        return SUCCESS;
    }

    @Action
    public long getUploadFileSize() {
        long totalBytes = 0;

        for (File file : uploadFile) {
            uploadFileSizes.add(file.length());
            totalBytes += file.length();
        }
        return totalBytes;
    }

    @Action(value = "list-resource-files", results = {
            @Result(name = SUCCESS, type = "stream",
                    params = {
                            "contentType", "application/json",
                            "inputName", "jsonInputStream"
                    })
    })
    /**
     * return json representation of the file proxies associated with the specified informationResource
     * @return
     * @throws Exception
     */
    // FIXME: don't throw everything; don't always return success
    public String listUploadedFiles() throws Exception {
        InformationResource informationResource = getGenericService().find(InformationResource.class, getInformationResourceId());
        List<FileProxy> fileProxies = new ArrayList<FileProxy>();
        for (InformationResourceFile informationResourceFile : informationResource.getInformationResourceFiles()) {
            if (!informationResourceFile.isDeleted()) {
                fileProxies.add(new FileProxy(informationResourceFile));
            }
        }
        StringWriter sw = new StringWriter();
        xmlService.convertToJson(fileProxies, sw);
        String json = sw.toString();
        logger.debug("file list as json: {}", json);
        byte[] jsonBytes = json.getBytes();
        jsonInputStream = new ByteArrayInputStream(jsonBytes);
        jsonContentLength = jsonBytes.length;

        return SUCCESS;
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

    public PersonalFilestoreTicket getPersonalFilestoreTicket() {
        return personalFilestoreTicket;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public List<String> getProcessedFileNames() {
        return processedFileNames;
    }

    public void setProcessedFileNames(List<String> processedFileNames) {
        this.processedFileNames = processedFileNames;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    /**
     * @param uploadFileSizes
     *            the uploadFileSizes to set
     */
    public void setUploadFileSizes(List<Long> uploadFileSizes) {
        this.uploadFileSizes = uploadFileSizes;
    }

    /**
     * @return the uploadFileSizes
     */
    public List<Long> getUploadFileSizes() {
        return uploadFileSizes;
    }

    public Long getInformationResourceId() {
        return informationResourceId;
    }

    public void setInformationResourceId(Long informationResourceId) {
        this.informationResourceId = informationResourceId;
    }

    public InputStream getJsonInputStream() {
        return jsonInputStream;
    }

    public void setJsonInputStream(InputStream jsonInputStream) {
        this.jsonInputStream = jsonInputStream;
    }

    public int getJsonContentLength() {
        return jsonContentLength;
    }

}