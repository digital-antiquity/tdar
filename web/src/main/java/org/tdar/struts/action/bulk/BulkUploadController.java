package org.tdar.struts.action.bulk;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.bulk.BulkUploadService;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.struts.action.resource.AbstractInformationResourceController;
import org.tdar.struts.data.AuthWrapper;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.utils.PersistableUtils;
import org.tdar.web.service.ResourceSaveControllerService;

import com.opensymphony.xwork2.TextProvider;

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
@Namespace("/bulk")
public class BulkUploadController extends AbstractInformationResourceController<Image> {

    private static final long serialVersionUID = -6419692259588266839L;

    @Autowired
    private transient FileAnalyzer analyzer;
    
    @Autowired
    private transient BulkUploadService bulkUploadService;

    @Autowired
    private transient ResourceSaveControllerService resourceSaveControllerService;

    private String bulkFileName;
    private long bulkContentLength;

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
        getLogger().info("{} and names {}", getUploadedFiles(), getUploadedFilesFileName());

        AuthWrapper<InformationResource> auth = new AuthWrapper<InformationResource>(getImage(), isAuthenticated(), getAuthenticatedUser(), isEditor());
        Collection<FileProxy> fileProxiesToProcess = resourceSaveControllerService.getFileProxiesToProcess(auth, this, getTicketId(), isMultipleFileUploadEnabled(), getFileProxies(), null, getUploadedFilesFileName(), getUploadedFiles());
        setupAccountForSaving();
        getCreditProxies().clear();
        getGenericService().detachFromSession(getPersistable());
        setPersistable(null);
        getGenericService().detachFromSession(getAuthenticatedUser());
        // getGenericService().detachFromSession(getPersistable().getResourceCollections());
        for (ResourceCreator rc : image.getResourceCreators()) {
            getLogger().debug("resourceCreators:{} {}", rc, rc.getId());
        }

        if (isAsync()) {
            getLogger().info("running asyncronously");
            bulkUploadService.saveAsync(image, getAuthenticatedUser().getId(), getTicketId(), fileProxiesToProcess, getAccountId());
        } else {
            getLogger().info("running inline");
            bulkUploadService.save(image, getAuthenticatedUser().getId(), getTicketId(), fileProxiesToProcess, getAccountId());
        }
        // setPersistable(null);
        return SUCCESS_ASYNC;
    }

    private void addAsyncHtmlErrors(List<String> htmlAsyncErrors) {
        if (CollectionUtils.isNotEmpty(htmlAsyncErrors)) {
            for (String error : htmlAsyncErrors) {
                addActionError(error);
            }
        }
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


    @Override
    public boolean isMultipleFileUploadEnabled() {
        return true;
    }

    @Override
    public Class<Image> getPersistableClass() {
        return Image.class;
    }

    @Override
    public boolean isBulkUpload() {
        return true;
    }


    @Override
    protected void postSaveCleanup(String returnString) {
        // don't clean up personal filestore -- we have called async methods that need access to them and will handle cleanup.
    }

}
