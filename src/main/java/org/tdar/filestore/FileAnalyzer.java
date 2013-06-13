package org.tdar.filestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileType;
import org.tdar.core.bean.resource.HasExtension;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.workflow.MessageService;
import org.tdar.core.service.workflow.workflows.Workflow;
import org.tdar.struts.data.FileProxy;

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
    private Map<String, Workflow> fileExtensionToWorkflowMap = new HashMap<String, Workflow>();
    private Map<FileType, List<String>> primaryExtensionList = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(getClass());
   
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
        Set<String> toReturn = new HashSet<String>();
        for (Workflow w : workflows) {
            toReturn.addAll(w.getValidExtensionsForResourceType(type));
            for (List<String> values : w.getRequiredExtensions().values()) {
                toReturn.addAll(values);
            }
            for (List<String> values : w.getSuggestedExtensions().values()) {
                toReturn.addAll(values);
            }
        }
        return toReturn;
    }

    public Set<String> getExtensionsForTypes(ResourceType... resourceTypes) {
        Set<String> extensions = new HashSet<String>();
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
                if (w.getValidExtensionsForResourceType(type).contains(ext.toLowerCase()))
                    return type;
            }
        }
        return null;
    }

    public Workflow getWorkflow(HasExtension... irFileVersion) throws Exception {
        Workflow wf = null;
        for (HasExtension ex :  irFileVersion) {
            Workflow w = fileExtensionToWorkflowMap.get(ex.getExtension());
            if (wf == null) {
                wf = w;
            } else if (w != null && wf.getClass() != w.getClass()) {
                throw new TdarRecoverableRuntimeException("cannot use two separate workflows");
            }
        }
        return wf;
    }

    public boolean processFile(InformationResourceFileVersion... informationResourceFileVersions) throws Exception {
        Workflow workflow = getWorkflow(informationResourceFileVersions);
        if (workflow == null) {
            throw new TdarRecoverableRuntimeException(String.format("no workflow could be found for these files %s", informationResourceFileVersions));
        }
        if (informationResourceFileVersions == null) {
            throw new TdarRecoverableRuntimeException("File version was null, this should not happen");
        }
        checkFilesExist(informationResourceFileVersions);

        logger.debug("using workflow: {}", workflow);
//        return workflow;
        return messageService.sendFileProcessingRequest(workflow, informationResourceFileVersions);
    }

    private void checkFilesExist(InformationResourceFileVersion[] informationResourceFileVersions) throws FileNotFoundException, IOException {
        for (InformationResourceFileVersion version : informationResourceFileVersions) {
            File file = version.getTransientFile();

            if (file == null) {
                throw new FileNotFoundException(version + " -- file does not exist");
            }
            if (!file.exists()) {
                throw new FileNotFoundException(file.getCanonicalPath() + " does not exist");
            }

        }

    }

    public boolean processFile(InformationResourceFile irFile) throws Exception {
        return processFile(irFile.getLatestUploadedVersion());
    }

    @Autowired
    public void setWorkflows(List<Workflow> workflows) {
        if (CollectionUtils.isEmpty(workflows)) {
            return;
        }
        for (Workflow workflow : workflows) {
            for (String validExtension : workflow.getValidExtensions()) {
                Workflow previousWorkflow = fileExtensionToWorkflowMap.put(validExtension, workflow);
                if (previousWorkflow != null) {
                    logger.warn("associated {} with {}, replacing old workflow {}", new Object[] { workflow, validExtension, previousWorkflow });
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

}
