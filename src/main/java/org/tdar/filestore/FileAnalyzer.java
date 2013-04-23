package org.tdar.filestore;

import java.io.File;
import java.io.FileNotFoundException;
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
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.workflow.MessageService;
import org.tdar.core.service.workflow.workflows.Workflow;

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
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public MessageService messageService;

    public FileType analyzeFile(InformationResourceFileVersion version) {
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

    public Workflow getWorkflow(InformationResourceFileVersion irFileVersion) throws Exception {
        return fileExtensionToWorkflowMap.get(irFileVersion.getExtension());
    }

    public boolean processFile(InformationResourceFileVersion irFileVersion) throws Exception {
        Workflow workflow = getWorkflow(irFileVersion);
        if (workflow == null)
            return false; // could argue that this is true
        if (irFileVersion == null) {
            throw new TdarRecoverableRuntimeException("File version was null, this should not happen");
        }

        File file = irFileVersion.getFile();

        if (file == null) {
            throw new FileNotFoundException(irFileVersion + " -- file does not exist");
        }
        if (!file.exists()) {
            throw new FileNotFoundException(file.getCanonicalPath() + " does not exist");
        }
        logger.debug("using workflow: {}", workflow);
        return messageService.sendFileProcessingRequest(irFileVersion, workflow);
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
}
