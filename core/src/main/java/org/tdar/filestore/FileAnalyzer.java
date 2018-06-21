package org.tdar.filestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.file.HasExtension;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.service.workflow.MessageService;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.utils.MessageHelper;
import org.tdar.workflows.RequiredOptionalPairs;
import org.tdar.workflows.Workflow;

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
    private final Logger logger = LoggerFactory.getLogger(getClass());
    public TdarConfiguration CONFIG = TdarConfiguration.getInstance();

    @Autowired
    private MessageService messageService;

    public List<FileType> getFileTypesForResourceType(ResourceType type) {
        List<FileType> toReturn = new ArrayList<>();
        switch (type) {
            case ARCHIVE:
                toReturn.add(FileType.FILE_ARCHIVE);
                break;
            case AUDIO:
                toReturn.add(FileType.AUDIO);
                break;
            case CODING_SHEET:
            case DATASET:
                toReturn.add(FileType.COLUMNAR_DATA);
                break;
            case DOCUMENT:
                toReturn.add(FileType.DOCUMENT);
                break;
            case GEOSPATIAL:
                toReturn.add(FileType.COLUMNAR_DATA);
            case IMAGE:
                toReturn.add(FileType.IMAGE);
                break;
            case ONTOLOGY:
            case PROJECT:
                toReturn.add(FileType.OTHER);
                break;
            case SENSORY_DATA:
                toReturn.add(FileType.FILE_ARCHIVE);
                toReturn.add(FileType.IMAGE);
                break;
            case VIDEO:
                toReturn.add(FileType.VIDEO);
                break;
            default:
                break;
        }
        return toReturn;

    }

    /**
     * Returns a modifiable Set<String> of valid file extensions for the given ResourceType.
     * 
     * @param type
     * @return
     */
    public Set<RequiredOptionalPairs> getExtensionsForType(ResourceType ... types) {
        Set<RequiredOptionalPairs> pairs = new HashSet<>();
        Set<FileType> ftypes = new HashSet<>();
        for (ResourceType type : types) {
            ftypes.addAll(getFileTypesForResourceType(type));
        }

        for (Workflow w : workflows) {
            for (FileType ft : ftypes) {
                if (w.getInformationResourceFileType() == ft) {
                    pairs.addAll(w.getRequiredOptionalPairs());
                }
            }
        }
        return pairs;
    }


    public Workflow getWorkflow(ResourceType rt, HasExtension... irFileVersion) {
        Workflow wf = null;
        for (HasExtension ex : irFileVersion) {
            String lowerCase = ex.getExtension().toLowerCase();
            Workflow w = getWorkflowForResourceType(ex, rt);
            if (w != null) { // not all extensions map... that's ok
                w.setExtension(lowerCase);
            }
            if (wf == null) {
                wf = w;
            } else if ((w != null) && (wf.getClass() != w.getClass())) {
                throw new TdarRecoverableRuntimeException("filestore.file_version_null");
            }
        }
        return wf;
    }

    private boolean processFile(ResourceType rt, InformationResourceFileVersion... informationResourceFileVersions) throws FileNotFoundException, IOException {
        if (informationResourceFileVersions == null) {
            throw new TdarRecoverableRuntimeException("filestore.file_version_null");
        }
        Workflow workflow = getWorkflow(rt, informationResourceFileVersions);
        if (workflow == null) {
            String message = MessageHelper.getMessage("fileAnalyzer.no_workflow_found", Arrays.asList(Arrays.toString(informationResourceFileVersions)));
            throw new TdarRecoverableRuntimeException(message);
        }
        checkFilesExist(informationResourceFileVersions);
        logger.debug(workflow.getExtension());

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
        this.workflows = workflows;
    }


    /*
     * Process the files based on whether the @link ResourceType is a composite (like a @link Dataset where all of the files are necessary) or not where each
     * file is processed separately
     */
    public void processFiles(ResourceType rt, List<InformationResourceFileVersion> filesToProcess, boolean compositeFilesEnabled)
            throws FileNotFoundException, IOException {
        if (CollectionUtils.isEmpty(filesToProcess)) {
            return;
        }

        if (compositeFilesEnabled) {
            processFile(rt, filesToProcess.toArray(new InformationResourceFileVersion[0]));
        } else {
            for (InformationResourceFileVersion version : filesToProcess) {
                if ((version.getTransientFile() == null) || (!version.getTransientFile().exists())) {
                    // If we are re-processing, the transient file might not exist.
                    version.setTransientFile(CONFIG.getFilestore().retrieveFile(FilestoreObjectType.RESOURCE, version));
                }
                processFile(rt, version);
            }
        }
    }

    public FileType getFileTypeForExtension(HasExtension version, ResourceType resourceType) {
        logger.debug("analyzing file extension: {}: {}", version.getExtension(), resourceType);
        List<FileType> types = getFileTypesForResourceType(resourceType);
        for (Workflow w : workflows) {
            if (types.contains(w.getInformationResourceFileType()) && w.canProcess(version.getExtension())) {
                return w.getInformationResourceFileType();
            }
        }
        return FileType.OTHER;
    }

    public Workflow getWorkflowForResourceType(HasExtension ex, ResourceType resourceType) {
        List<FileType> types = getFileTypesForResourceType(resourceType);
        logger.debug("FileTypes: {} for resourceType:{}", types, resourceType);
        for (FileType type : types) {
            for (Workflow w : workflows) {
                if (w.getInformationResourceFileType() == type && w.getAllValidExtensions().contains(ex.getExtension().toLowerCase())) {
                    return w;
                }
            }
        }
        return null;
    }

    public ResourceType suggestTypeForFileName(String extension, ResourceType ... resourceTypes) {
        for (ResourceType type : resourceTypes) {
            for (RequiredOptionalPairs pair : getExtensionsForType(type)) {
                if (pair.getRequired().contains(extension)) {
                    return type;
                }
            }
        }
        return null;
    }

}
