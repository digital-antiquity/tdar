/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.filestore.workflows;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.InformationResourceFile.FileType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.filestore.tasks.ListArchiveTask;
import org.tdar.filestore.workflows.Workflow.BaseWorkflow;

/**
 * @author Adam Brin
 * 
 */
@Component
public class FileArchiveWorkflow extends BaseWorkflow {

    public FileArchiveWorkflow() {
        registerFileExtension("tgz", ResourceType.SENSORY_DATA);
        registerFileExtension("tar", ResourceType.SENSORY_DATA);
        registerFileExtension("zip", ResourceType.SENSORY_DATA);

        addTask(new ListArchiveTask(), WorkflowPhase.PRE_PROCESS);
    }

    @Override
    public FileType getInformationResourceFileType() {
        return FileType.FILE_ARCHIVE;
    }

    public boolean isEnabled() {
        return true;
    }
}
