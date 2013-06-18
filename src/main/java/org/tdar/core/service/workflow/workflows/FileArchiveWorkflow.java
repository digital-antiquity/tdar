package org.tdar.core.service.workflow.workflows;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.InformationResourceFile.FileType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.service.workflow.workflows.Workflow.BaseWorkflow;
import org.tdar.filestore.tasks.IndexableTextExtractionTask;
import org.tdar.filestore.tasks.ListArchiveTask;

/**
 * $Id$
 * 
 * @author Adam Brin
 * @version $Revision$
 */
@Component
public class FileArchiveWorkflow extends BaseWorkflow {

    public FileArchiveWorkflow() {
        registerFileExtension("tgz", ResourceType.SENSORY_DATA, ResourceType.ARCHIVE);
        registerFileExtension("tar", ResourceType.SENSORY_DATA, ResourceType.ARCHIVE);
        registerFileExtension("zip", ResourceType.SENSORY_DATA, ResourceType.ARCHIVE);

        addTask(ListArchiveTask.class, WorkflowPhase.PRE_PROCESS);
        addTask(IndexableTextExtractionTask.class, WorkflowPhase.CREATE_DERIVATIVE);
    }

    @Override
    public FileType getInformationResourceFileType() {
        return FileType.FILE_ARCHIVE;
    }

    public boolean isEnabled() {
        return true;
    }
}
