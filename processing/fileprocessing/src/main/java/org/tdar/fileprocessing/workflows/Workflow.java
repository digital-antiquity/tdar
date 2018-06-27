package org.tdar.fileprocessing.workflows;

import java.util.List;
import java.util.Set;

import org.tdar.fileprocessing.tasks.Task;
import org.tdar.filestore.FileStoreFileProxy;
import org.tdar.filestore.FileType;

/**
 * $Id$
 * 
 * 
 * @author Adam Brin
 * @version $Rev$
 */
public interface Workflow {

    boolean run(WorkflowContext workflowContext) throws Exception;

//    Set<String> getValidExtensions();

    void addTask(Class<? extends Task> task, WorkflowPhase phase);

//    boolean canProcess(String ext);

    FileType getInformationResourceFileType();

    boolean isEnabled();

//    void registerFileExtension(String ext, ResourceType... types);

//    Set<String> getValidExtensionsForResourceType(ResourceType type);

//    Map<String, List<String>> getRequiredExtensions();

    //    Map<String, List<String>> getSuggestedExtensions();

    void initializeWorkflowContext(WorkflowContext ctx, FileStoreFileProxy[] versions);

    void setExtension(String ext);

    String getExtension();

    boolean canProcess(String extension);

    List<String> getAllValidExtensions();

    Set<RequiredOptionalPairs> getRequiredOptionalPairs();

}
