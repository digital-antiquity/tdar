package org.tdar.core.service.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.dao.resource.InformationResourceFileDao;
import org.tdar.core.dao.resource.ResourceDao;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.core.service.ServiceInterface;
import org.tdar.core.service.workflow.WorkflowResult;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.FileStoreFileProxy;
import org.tdar.filestore.Filestore.ObjectType;
import org.tdar.filestore.WorkflowContext;
import org.tdar.filestore.personal.PersonalFilestore;

/**
 * $Id: AbstractInformationResourceService.java 1466 2011-01-18 20:32:38Z abrin$
 * 
 * Provides basic InformationResource services including file management (via FileProxyS).
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */

public abstract class AbstractInformationResourceService<T extends InformationResource, R extends ResourceDao<T>> extends ServiceInterface.TypedDaoBase<T, R> {

    // FIXME: this should be injected
    private static final TdarConfiguration config = TdarConfiguration.getInstance();

    @Autowired
    private InformationResourceFileDao informationResourceFileDao;
    @Autowired
    private DatasetDao datasetDao;

    @Autowired
    private PersonalFilestoreService personalFilestoreService;

    private FileAnalyzer analyzer;

    /*
     * Given a @link Resource and list of @link FileProxy objects, process the files and report any errors to the @link ActionMessageErrorSupport listener,
     * which is likely a controller.
     */
    @Transactional
    public ErrorTransferObject importFileProxiesAndProcessThroughWorkflow(T resource, TdarUser user, Long ticketId,
            List<FileProxy> fileProxiesToProcess) throws IOException {
        if (CollectionUtils.isEmpty(fileProxiesToProcess)) {
            getLogger().debug("Nothing to process, returning.");
            return null;
        }

        // prepare the metadata
        FileProxyWrapper wrapper = new FileProxyWrapper(resource, analyzer, datasetDao, fileProxiesToProcess);
        
        wrapper.processMetadataForFileProxies();

        analyzer.processFiles(wrapper.getFilesToProcess(),  resource.getResourceType().isCompositeFilesEnabled());

        /*
         * FIXME: When we move to an asynchronous model, this section and below will need to be moved into their own dedicated method
         */
        WorkflowResult workflowResult = new WorkflowResult(fileProxiesToProcess);
        ErrorTransferObject errorsAndMessages = workflowResult.getActionErrorsAndMessages();

        // If successful and no errors:
        // purge the filestore
        // mark the uploaded files as "read only"
        if (workflowResult.isSuccess()) {
            List<FileStoreFileProxy> proxies = new ArrayList<>();
            for (InformationResourceFileVersion file : wrapper.getFilesToProcess()) {
                proxies.add(file);
            }
            config.getFilestore().markReadOnly(ObjectType.RESOURCE, proxies);
        }
        if (ticketId != null) {
            PersonalFilestore personalFilestore = personalFilestoreService.getPersonalFilestore(user);
            personalFilestore.purge(getDao().find(PersonalFilestoreTicket.class, ticketId));
        }
        return errorsAndMessages;
    }


    /*
     * Given an @link InformationResource, find all of the latest versions and reprocess them.
     */
    @Transactional(readOnly = false)
    public ErrorTransferObject reprocessInformationResourceFiles(T ir) throws Exception {
        List<InformationResourceFileVersion> latestVersions = new ArrayList<>();
        for (InformationResourceFile irFile : ir.getInformationResourceFiles()) {
            if (irFile.isDeleted()) {
                continue;
            }
            InformationResourceFileVersion original = irFile.getLatestUploadedVersion();
            original.setTransientFile(config.getFilestore().retrieveFile(ObjectType.RESOURCE, original));
            latestVersions.add(original);
            Iterator<InformationResourceFileVersion> iterator = irFile.getInformationResourceFileVersions().iterator();
            while (iterator.hasNext()) {
                InformationResourceFileVersion version = iterator.next();
                if (!version.equals(original) && !version.isUploaded() && !version.isArchival()) {
                    iterator.remove();
                    informationResourceFileDao.deleteVersionImmediately(version);
                }
            }
        }
        analyzer.processFiles(latestVersions, ir.getResourceType().isCompositeFilesEnabled());
        // this is a known case where we need to purge the session
        // getDao().synchronize();

        ErrorTransferObject eto = null;
        for (InformationResourceFile irFile : ir.getInformationResourceFiles()) {
            final WorkflowContext workflowContext = irFile.getWorkflowContext();
            // may be null for "skipped" or composite file
            if ((workflowContext != null) && !workflowContext.isProcessedSuccessfully()) {
                WorkflowResult workflowResult = new WorkflowResult(workflowContext);
                eto = workflowResult.getActionErrorsAndMessages();
            }
        }
        return eto;
    }

    /**
     * We autowire the setter to help with autowiring issues
     * 
     * @param analyzer
     *            the analyzer to set
     */
    @Autowired
    public void setAnalyzer(FileAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    public FileAnalyzer getAnalyzer() {
        return analyzer;
    }
}
