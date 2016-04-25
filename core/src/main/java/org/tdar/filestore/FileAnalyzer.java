package org.tdar.filestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.file.FileType;
import org.tdar.core.bean.resource.file.HasExtension;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.workflow.MessageService;
import org.tdar.core.service.workflow.workflows.Workflow;
import org.tdar.utils.MessageHelper;

/**
 * $Id$
 * 
 * 
 * @author Adam Brin
 * @version $Rev$
 */
@Component
public class FileAnalyzer {

    private List<Workflow> workflows;
    private Map<String, Workflow> fileExtensionToWorkflowMap = new HashMap<>();
    private Map<FileType, List<String>> primaryExtensionList = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(getClass());
    public TdarConfiguration CONFIG = TdarConfiguration.getInstance();
    @Autowired
    private MessageService messageService;

    public FileType analyzeFile(HasExtension version) {
        for (Workflow w : workflows) {
            if (w.canProcess(version.getExtension())) {
                return w.getInformationResourceFileType();
            }
        }
        return FileType.OTHER;
    }

    /**
     * Returns a modifiable Set<String> of valid file extensions for the given ResourceType.
     * 
     * @param type
     * @return
     */
    public Set<String> getExtensionsForType(ResourceType type) {
        Set<String> toReturn = new HashSet<>();
        for (Workflow w : workflows) {
            Set<String> exts = w.getValidExtensionsForResourceType(type);
            if (CollectionUtils.isNotEmpty(exts)) {
                toReturn.addAll(exts);
                for (List<String> values : w.getRequiredExtensions().values()) {
                    toReturn.addAll(values);
                }
                for (List<String> values : w.getSuggestedExtensions().values()) {
                    toReturn.addAll(values);
                }
            }
        }
        return toReturn;
    }

    public Set<String> getExtensionsForTypes(ResourceType... resourceTypes) {
        Set<String> extensions = new HashSet<>();
        for (Workflow workflow : workflows) {
            for (ResourceType resourceType : resourceTypes) {
                extensions.addAll(workflow.getValidExtensionsForResourceType(resourceType));
            }
        }
        return extensions;
    }

    public ResourceType suggestTypeForFileExtension(String ext, ResourceType... types) {
        for (ResourceType type : types) {
            for (Workflow w : workflows) {
                if (w.getValidExtensionsForResourceType(type).contains(ext.toLowerCase())) {
                    return type;
                }
            }
        }
        return null;
    }

    public Workflow getWorkflow(HasExtension... irFileVersion) {
        Workflow wf = null;
        for (HasExtension ex : irFileVersion) {
            Workflow w = fileExtensionToWorkflowMap.get(ex.getExtension().toLowerCase());
            if (wf == null) {
                wf = w;
            } else if ((w != null) && (wf.getClass() != w.getClass())) {
                throw new TdarRecoverableRuntimeException("filestore.file_version_null");
            }
        }
        return wf;
    }

    private boolean processFile(InformationResourceFileVersion... informationResourceFileVersions) throws FileNotFoundException, IOException {
        if (informationResourceFileVersions == null) {
            throw new TdarRecoverableRuntimeException("filestore.file_version_null");
        }
        Workflow workflow = getWorkflow(informationResourceFileVersions);
        if (workflow == null) {
            String message = MessageHelper.getMessage("fileAnalyzer.no_workflow_found", Arrays.asList(Arrays.toString(informationResourceFileVersions)));
            throw new TdarRecoverableRuntimeException(message);
        }
        checkFilesExist(informationResourceFileVersions);

        logger.debug("using workflow: {}", workflow);
        // Martin: if this is ever going to run asynchronously, this should this return anything?
        return messageService.sendFileProcessingRequest(workflow, informationResourceFileVersions);
    }

    private void checkFilesExist(InformationResourceFileVersion... informationResourceFileVersions) throws FileNotFoundException, IOException {
        for (InformationResourceFileVersion version : informationResourceFileVersions) {
            File file = version.getTransientFile();

            if (file == null) {
                throw new FileNotFoundException(MessageHelper.getMessage("filestore.file_does_not_exist", Arrays.asList(version)));
            }
            if (!file.exists()) {
                throw new FileNotFoundException(MessageHelper.getMessage("error.file_not_found", Arrays.asList(file.getCanonicalPath())));
            }

        }

    }


    @Autowired
    public void setWorkflows(List<Workflow> workflows) {
        if (CollectionUtils.isEmpty(workflows)) {
            return;
        }
        for (Workflow workflow : workflows) {
            for (String validExtension : workflow.getValidExtensions()) {
                String normalizedExtension = validExtension.toLowerCase();
                if (!normalizedExtension.equals(validExtension)) {
                    logger.warn("extension had uppercase characters, normalizing from {} to {}", validExtension, normalizedExtension);
                }
                Workflow previousWorkflow = fileExtensionToWorkflowMap.put(normalizedExtension, workflow);
                if (previousWorkflow != null) {
                    logger.warn("associated {} with {}, replacing old workflow {}", new Object[] { workflow, normalizedExtension, previousWorkflow });
                }
            }
        }
        this.workflows = workflows;
    }

    public boolean isPrimaryFile(FileProxy proxy, FileType type) {
        List<String> extensions = primaryExtensionList.get(type);
        if (CollectionUtils.isNotEmpty(extensions) && extensions.contains(proxy.getExtension())) {
            return true;
        }
        return false;
    }

    public ResourceType suggestTypeForFileName(String fileName, ResourceType[] resourceTypesSupportingBulkUpload) {
        String extension = FilenameUtils.getExtension((fileName.toLowerCase()));
        return suggestTypeForFileExtension(extension, resourceTypesSupportingBulkUpload);
    }

    /*
     * Process the files based on whether the @link ResourceType is a composite (like a @link Dataset where all of the files are necessary) or not where each
     * file is processed separately
     */
    public void processFiles(List<InformationResourceFileVersion> filesToProcess, boolean compositeFilesEnabled) throws FileNotFoundException, IOException {
            if (CollectionUtils.isEmpty(filesToProcess)) {
                return;
            }

            if (compositeFilesEnabled) {
                processFile(filesToProcess.toArray(new InformationResourceFileVersion[0]));
            } else {
                for (InformationResourceFileVersion version : filesToProcess) {
                    if ((version.getTransientFile() == null) || (!version.getTransientFile().exists())) {
                        // If we are re-processing, the transient file might not exist.
                        version.setTransientFile(CONFIG.getFilestore().retrieveFile(FilestoreObjectType.RESOURCE, version));
                    }
                    processFile(version);
                }
            }
        }

}
