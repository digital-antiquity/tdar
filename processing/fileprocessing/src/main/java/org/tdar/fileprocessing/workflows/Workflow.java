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

    void addTask(Class<? extends Task> task, WorkflowPhase phase);

    FileType getInformationResourceFileType();

    boolean isEnabled();

    void setExtension(String ext);

    String getExtension();

    boolean canProcess(String extension);

    List<String> getAllValidExtensions();

    Set<RequiredOptionalPairs> getRequiredOptionalPairs();

}
