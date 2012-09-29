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
import org.tdar.filestore.tasks.ImageThumbnailTask;
import org.tdar.filestore.workflows.Workflow.BaseWorkflow;

/**
 * @author Adam Brin
 * 
 */
@Component
public class ImageWorkflow extends BaseWorkflow {

    public ImageWorkflow() {
        registerFileExtensions(new String[] { "gif", "tif", "jpg", "tiff", "jpeg" },
                ResourceType.IMAGE, ResourceType.SENSORY_DATA);
        registerFileExtensions(new String[] { "bmp", "pict", "png" }, ResourceType.IMAGE);
        addTask(ImageThumbnailTask.class, WorkflowPhase.CREATE_DERIVATIVE);
    }

    @Override
    public FileType getInformationResourceFileType() {
        return FileType.IMAGE;
    }

    public boolean isEnabled() {
        return true;
    }

}
