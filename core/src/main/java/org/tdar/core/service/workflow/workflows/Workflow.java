package org.tdar.core.service.workflow.workflows;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.file.FileType;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.filestore.WorkflowContext;
import org.tdar.filestore.tasks.Task;

/**
 * $Id$
 * 
 * 
 * @author Adam Brin
 * @version $Rev$
 */
public interface Workflow {

    boolean run(WorkflowContext workflowContext) throws Exception;

    Set<String> getValidExtensions();

    void addTask(Class<? extends Task> task, WorkflowPhase phase);

    boolean canProcess(String ext);

    FileType getInformationResourceFileType();

    boolean isEnabled();

    void registerFileExtension(String ext, ResourceType... types);

    Set<String> getValidExtensionsForResourceType(ResourceType type);

    Map<String, List<String>> getRequiredExtensions();

    Map<String, List<String>> getSuggestedExtensions();

    void initializeWorkflowContext(WorkflowContext ctx, InformationResourceFileVersion[] versions);

    boolean validateProxyCollection(FileProxy primary);
    
    void setExtension(String ext);

    String getExtension();

}
