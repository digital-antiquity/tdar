package org.tdar.struts.action.resource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.AsyncUpdateReceiver;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.BulkUploadService;
import org.tdar.core.service.BulkUploadTemplateService;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.bulk.BulkManifestProxy;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.personal.PersonalFilestore;
import org.tdar.filestore.personal.PersonalFilestoreFile;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.utils.Pair;
import org.tdar.utils.PersistableUtils;

/**
 * $Id$
 * 
 * <p>
 * Manages requests to create/delete/edit an CodingSheet and its associated metadata.
 * </p>
 * 
 * 
 * @author <a href='mailto:adam.brin@asu.edu'>Adam Brin</a>
 * @version $Revision$
 */
@ParentPackage("secured")
@Component
@HttpsOnly
@Scope("prototype")
@Namespace("/batch")
public class BulkUploadController extends AbstractInformationResourceController<Image> {

    private static final String TEMPLATE_PREPARE = "template-prepare";
    private static final String VALIDATE_ERROR = "validate-error";
    private static final long serialVersionUID = -6419692259588266839L;

    @Autowired
    private transient BulkUploadService bulkUploadService;

    @Autowired
    private transient PersonalFilestoreService filestoreService;

    @Autowired
    private transient FileAnalyzer analyzer;

    @Autowired
    private transient BulkUploadTemplateService bulkUploadTemplateService;

    @Autowired
    private transient SerializationService serializationService;

    private InputStream resultJson;
    private String bulkFileName;
    private long bulkContentLength;
    private FileInputStream templateInputStream;
    private Float percentDone = 0f;
    private String phase;
    private List<Pair<Long, String>> details;
    private String asyncErrors = "";
    private File templateFile;
    private String templateFilename;

    /**
     * Save basic metadata of the registering concept.
     * 
     */
    @Override
    protected String save(Image image) {
        Status oldStatus = getPersistable().getStatus();
        getPersistable().setStatus(Status.DELETED);
        getGenericService().markReadOnly(getPersistable());
        getLogger().info("saving batches...");
        getPersistable().setStatus(oldStatus);
        if (PersistableUtils.isNullOrTransient(getTicketId())) {
            addActionError(getText("bulkUploadController.no_files"));
            return INPUT;
        }
        getLogger().debug("ticketId: {} ", getTicketId());
        getLogger().debug("proxy:    {}", getFileProxies());
        saveBasicResourceMetadata();
        saveInformationResourceProperties();
        File excelManifest = null;
        getLogger().info("{} and names {}", getUploadedFiles(), getUploadedFilesFileName());
        PersonalFilestoreTicket ticket = filestoreService.findPersonalFilestoreTicket(getTicketId());
        PersonalFilestore personalFilestore = filestoreService.getPersonalFilestore(getTicketId());
        if (!CollectionUtils.isEmpty(getUploadedFilesFileName())) {
            try {
                String filename = getUploadedFilesFileName().get(0);
                excelManifest = personalFilestore.store(ticket, getUploadedFiles().get(0), filename).getFile();
            } catch (Exception e) {
                addActionErrorWithException(getText("bulkUploadController.cannot_store_manifest"), e);
            }
        }

        if (getTemplateFilename() != null) {
            PersonalFilestoreFile filestoreFile = personalFilestore.retrieve(ticket, getTemplateFilename());
            if (filestoreFile != null) {
                excelManifest = filestoreFile.getFile();
            }
        }

        getLogger().debug("excel manifest is: {}", excelManifest);
        handleAsyncUploads();
        Collection<FileProxy> fileProxiesToProcess = getFileProxiesToProcess();
        setupAccountForSaving();
        getCreditProxies().clear();
        getGenericService().detachFromSession(getPersistable());
        setPersistable(null);
        getGenericService().detachFromSession(getAuthenticatedUser());
        // getGenericService().detachFromSession(getPersistable().getResourceCollections());
        for (ResourceCreator rc : image.getResourceCreators()) {
            getLogger().debug("resourceCreators:{} {}", rc, rc.getId());
        }

        getAuthorizedUsers().clear();
        if (isAsync()) {
            getLogger().info("running asyncronously");
            bulkUploadService.saveAsync(image, getAuthenticatedUser().getId(), getTicketId(), excelManifest, fileProxiesToProcess, getAccountId());
        } else {
            getLogger().info("running inline");
            bulkUploadService.save(image, getAuthenticatedUser().getId(), getTicketId(), excelManifest, fileProxiesToProcess, getAccountId());
        }
        // setPersistable(null);
        return SUCCESS_ASYNC;
    }

    @Action(value = TEMPLATE_PREPARE)
    @SkipValidation
    public String templateView() {
        return SUCCESS;
    }

    @Action(value = "validate-template",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = INPUT, type = TDAR_REDIRECT, location = TEMPLATE_PREPARE),
                    @Result(name = VALIDATE_ERROR, type = TDAR_REDIRECT, location = TEMPLATE_PREPARE),
                    @Result(name = SUCCESS, type = TDAR_REDIRECT,
                            location = "add?ticketId=${ticketId}&templateFilename=${templateFilename}&projectId=${projectId}") })
    @SkipValidation
    @PostOnly
    public String templateValidate() {
        getLogger().info("{} and names {}", getUploadedFiles(), getUploadedFilesFileName());
        if (CollectionUtils.isEmpty(getUploadedFiles()) || (getUploadedFiles().get(0) == null)) {
            addActionError(getText("bulkUploadController.upload_template"));
            return VALIDATE_ERROR;
        }
        try {
            Workbook workbook = WorkbookFactory.create(getUploadedFiles().get(0));
            Image image = new Image();
            image.setTitle("template_valid_title");
            image.setDescription("test description");
            image.setProject(Project.NULL);

            BulkManifestProxy manifestProxy = bulkUploadService.validateManifestFile(workbook.getSheetAt(0), image, getAuthenticatedUser(), null, null);

            List<String> htmlAsyncErrors = manifestProxy.getAsyncUpdateReceiver().getHtmlAsyncErrors();
            addAsyncHtmlErrors(htmlAsyncErrors);
            PersonalFilestoreTicket ticket = filestoreService.createPersonalFilestoreTicket(getAuthenticatedUser());
            setTicketId(ticket.getId());
            PersonalFilestore personalFilestore = filestoreService.getPersonalFilestore(getTicketId());
            try {
                String filename = getUploadedFilesFileName().get(0);
                setTemplateFilename(filename);
                personalFilestore.store(ticket, getUploadedFiles().get(0), filename);
            } catch (Throwable e) {
                addActionErrorWithException(getText("bulkUploadController.cannot_store_manifest"), e);
            }

        } catch (Throwable e) {
            addActionErrorWithException(getText("bulkUploadController.problem_template", TdarConfiguration.getInstance().getSiteAcronym()), e);
        }
        if (CollectionUtils.isNotEmpty(getActionErrors())) {
            return VALIDATE_ERROR;
        }
        addActionMessage(getText("bulkUploadController.template_validation_success", Arrays.asList(getTemplateFilename())));
        return SUCCESS;
    }

    private void addAsyncHtmlErrors(List<String> htmlAsyncErrors) {
        if (CollectionUtils.isNotEmpty(htmlAsyncErrors)) {
            for (String error : htmlAsyncErrors) {
                addActionError(error);
            }
        }
    }

    @SkipValidation
    @Action(value = "checkstatus",
            results = { @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "resultJson" }) })
    @PostOnly
    public String checkStatus() {
        AsyncUpdateReceiver reciever = bulkUploadService.checkAsyncStatus(getTicketId());
        if (reciever != null) {
            phase = reciever.getStatus();
            percentDone = reciever.getPercentComplete();
            getLogger().debug("{} {}%", phase, percentDone);
            StringBuffer sb = new StringBuffer();
            if (CollectionUtils.isNotEmpty(reciever.getAsyncErrors())) {
                getLogger().warn("bulkUploadErrors: {}", reciever.getAsyncErrors());
                for (String err : reciever.getHtmlAsyncErrors()) {
                    sb.append("<li>").append(err).append("</li>");
                }
                setAsyncErrors(sb.toString());
            }

            if (percentDone == 100f) {
                List<Pair<Long, String>> details = reciever.getDetails();
                setDetails(details);
                // should create revision log
            }
        } else {
            setAsyncErrors("");
            phase = "starting up...";
            percentDone = 0.0f;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("percentDone", percentDone);
        result.put("phase", phase);
        result.put("errors", asyncErrors);
        setResultJson(new ByteArrayInputStream(serializationService.convertFilteredJsonForStream(result, null, null).getBytes()));
        return SUCCESS;
    }

    @SkipValidation
    @Action(value = "template", results = {
            @Result(name = SUCCESS, type = "stream",
                    params = {
                            "contentType", "application/vnd.ms-excel",
                            "inputName", "templateInputStream",
                            "contentDisposition", "attachment;filename=\"${bulkFileName}\"",
                            "contentLength", "${bulkContentLength}"
                    })
    })
    public String downloadBulkTemplate() {
        // create temporary file

        HSSFWorkbook workbook = bulkUploadTemplateService.createExcelTemplate();
        setBulkFileName("tdar-bulk-upload-template.xls");
        try {
            setTemplateFile(File.createTempFile(getBulkFileName(), ".xls", TdarConfiguration.getInstance().getTempDirectory()));
            getTemplateFile().deleteOnExit();
            workbook.write(new FileOutputStream(getTemplateFile()));
            setBulkContentLength(getTemplateFile().length());
            setTemplateInputStream(new FileInputStream(getTemplateFile()));
        } catch (Exception iox) {
            getLogger().error("an error ocurred creating the template file", iox);
            throw new TdarRecoverableRuntimeException(getText("bulkUploadController.could_not_store_file"));
        }
        return SUCCESS;
    }

    /**
     * Get the current concept.
     * 
     * @return
     */
    public Image getImage() {
        return getPersistable();
    }

    public void setImage(Image image) {
        setPersistable(image);
    }

    @Override
    public Collection<String> getValidFileExtensions() {
        return analyzer.getExtensionsForTypes(ResourceType.getTypesSupportingBulkUpload());
    }

    @Override
    public boolean shouldSaveResource() {
        return false;
    }

    public void setBulkContentLength(long bulkContentLength) {
        this.bulkContentLength = bulkContentLength;
    }

    public long getBulkContentLength() {
        return bulkContentLength;
    }

    public void setBulkFileName(String bulkFileName) {
        this.bulkFileName = bulkFileName;
    }

    public String getBulkFileName() {
        return bulkFileName;
    }

    public void setTemplateInputStream(FileInputStream templateInputStream) {
        this.templateInputStream = templateInputStream;
    }

    public FileInputStream getTemplateInputStream() {
        return templateInputStream;
    }

    public void setPercentDone(Float percentDone) {
        this.percentDone = percentDone;
    }

    public Float getPercentDone() {
        return percentDone;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getPhase() {
        return phase;
    }

    @Override
    public boolean isMultipleFileUploadEnabled() {
        return true;
    }

    public void setDetails(List<Pair<Long, String>> details) {
        this.details = details;
    }

    public List<Pair<Long, String>> getDetails() {
        return details;
    }

    /**
     * @param asyncErrors
     *            the asyncErrors to set
     */
    public void setAsyncErrors(String asyncErrors) {
        this.asyncErrors = asyncErrors;
    }

    /**
     * @return the asyncErrors
     */
    public String getAsyncErrors() {
        return asyncErrors;
    }

    @Override
    public Class<Image> getPersistableClass() {
        return Image.class;
    }

    /**
     * @return the templateFile
     */
    public File getTemplateFile() {
        return templateFile;
    }

    /**
     * @param templateFile
     *            the templateFile to set
     */
    public void setTemplateFile(File templateFile) {
        this.templateFile = templateFile;
    }

    @Override
    public boolean isBulkUpload() {
        return true;
    }

    public String getTemplateFilename() {
        return templateFilename;
    }

    public void setTemplateFilename(String templateFilename) {
        this.templateFilename = templateFilename;
    }

    @Override
    protected void postSaveCleanup(String returnString) {
        // don't clean up personal filestore -- we have called async methods that need access to them and will handle cleanup.
    }

    public InputStream getResultJson() {
        return resultJson;
    }

    public void setResultJson(InputStream resultJson) {
        this.resultJson = resultJson;
    }

    /**
     * For edit page: return true if user has pre-validated a mapping file
     * 
     * @return
     */
    public boolean isTemplateValidated() {
        // TODO: probably better off having validate action simply render the edit form instead of redirecting to /batch/add?obnoxiousQueryString
        return PersistableUtils.isNotNullOrTransient(getTicketId()) && StringUtils.isNotBlank(templateFilename);
    }
}
