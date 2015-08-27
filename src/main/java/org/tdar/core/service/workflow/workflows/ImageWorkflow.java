package org.tdar.core.service.workflow.workflows;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.file.FileType;
import org.tdar.core.service.workflow.workflows.Workflow.BaseWorkflow;
import org.tdar.filestore.tasks.ImageThumbnailTask;
import org.tdar.filestore.tasks.IndexableTextExtractionTask;

/**
 * $Id$
 * 
 * @author Adam Brin
 * @version $Revision$
 */
@Component
public class ImageWorkflow extends BaseWorkflow {

    public ImageWorkflow() {
        registerFileExtensions(new String[] { "gif", "tif", "jpg", "tiff", "jpeg" },
                ResourceType.IMAGE, ResourceType.SENSORY_DATA);
        registerFileExtensions(new String[] { "bmp", "pict", "png" }, ResourceType.IMAGE);
        addTask(ImageThumbnailTask.class, WorkflowPhase.CREATE_DERIVATIVE);
        addTask(IndexableTextExtractionTask.class, WorkflowPhase.CREATE_DERIVATIVE);
    }

    @Override
    public FileType getInformationResourceFileType() {
        return FileType.IMAGE;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
