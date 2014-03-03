package org.tdar.struts.action.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.AsyncUpdateReceiver;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.BulkUploadService;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.personal.PersonalFilestore;
import org.tdar.filestore.personal.PersonalFilestoreFile;
import org.tdar.struts.data.FileProxy;
import org.tdar.utils.Pair;

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
@Scope("prototype")
@Namespace("/batch")
public class BulkUploadController extends AbstractInformationResourceController<Image> {

    private static final long serialVersionUID = -6419692259588266839L;

    @Autowired
    private BulkUploadService bulkUploadService;

    @Autowired
    private PersonalFilestoreService filestoreService;

    @Autowired
    private FileAnalyzer analyzer;

    private String bulkFileName;
    private long bulkContentLength;
    private FileInputStream templateInputStream;
    private Float percentDone = 0f;
    private String phase;
    private List<Pair<Long, String>> details;
    private String asyncErrors;
    private int maxReferenceRow = 0;
    private File templateFile;
    private String templateFilename;

    /**
     * Save basic metadata of the registering concept.
     * 
     * @param concept
     */
    @Override
    protected String save(Image image) {
        getLogger().info("saving batches...");

        if (Persistable.Base.isNullOrTransient(getTicketId())) {
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
                excelManifest = personalFilestore.store(ticket, getUploadedFiles().get(0), filename);
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
        if (isAsync()) {
            getLogger().info("running asyncronously");
            bulkUploadService.saveAsync(image, getAuthenticatedUser().getId(), getTicketId(), excelManifest, fileProxiesToProcess, getAccountId());
        } else {
            getLogger().info("running inline");
            bulkUploadService.save(image, getAuthenticatedUser().getId(), getTicketId(), excelManifest, fileProxiesToProcess, getAccountId());
        }
        getGenericService().markReadOnly(getPersistable());
        getGenericService().detachFromSession(getPersistable());
        setPersistable(null);
        return SUCCESS_ASYNC;
    }

    @Action(value = "template-prepare")
    @SkipValidation
    public String templateView() {
        return SUCCESS;
    }

    @Action(value = "validate-template", results = {
            @Result(name = INPUT, type = "redirect", location = "template-prepare"),
            @Result(name = SUCCESS, type = "redirect", location = "add?ticketId=${ticketId}&templateFilename=${templateFilename}&projectId=${projectId}") })
    @SkipValidation
    public String templateValidate() {

        getLogger().info("{} and names {}", getUploadedFiles(), getUploadedFilesFileName());
        if (CollectionUtils.isEmpty(getUploadedFiles()) || getUploadedFiles().get(0) == null) {
            addActionError(getText("bulkUploadController.upload_template"));
            return INPUT;
        }
        try {
            Workbook workbook = WorkbookFactory.create(getUploadedFiles().get(0));
            bulkUploadService.validateManifestFile(workbook.getSheetAt(0));

            PersonalFilestoreTicket ticket = filestoreService.createPersonalFilestoreTicket(getAuthenticatedUser());
            setTicketId(ticket.getId());
            PersonalFilestore personalFilestore = filestoreService.getPersonalFilestore(getTicketId());
            try {
                String filename = getUploadedFilesFileName().get(0);
                setTemplateFilename(filename);
                personalFilestore.store(ticket, getUploadedFiles().get(0), filename);
            } catch (Exception e) {
                addActionErrorWithException(getText("bulkUploadController.cannot_store_manifest"), e);
            }

        } catch (Exception e) {
            addActionErrorWithException(getText("bulkUploadController.problem_template"), e);
            return INPUT;
        }
        addActionMessage("");
        return SUCCESS;
    }

    @SkipValidation
    @Action(value = "checkstatus", results = {
            @Result(name = WAIT, type = "freemarker", location = "checkstatus-wait.ftl", params = { "contentType", "application/json" }) })
    public String checkStatus() {
        AsyncUpdateReceiver reciever = bulkUploadService.checkAsyncStatus(getTicketId());
        if (reciever != null) {
            phase = reciever.getStatus();
            percentDone = reciever.getPercentComplete();
            setAsyncErrors(reciever.getHtmlAsyncErrors());
            if (percentDone == 100f) {
                List<Pair<Long, String>> details = reciever.getDetails();
                setDetails(details);
            }
            return WAIT;
        } else {
            return ERROR;
        }
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

        HSSFWorkbook workbook = bulkUploadService.createExcelTemplate();
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
        return analyzer.getExtensionsForTypes(ResourceType.IMAGE, ResourceType.DOCUMENT, ResourceType.DATASET);
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
}
