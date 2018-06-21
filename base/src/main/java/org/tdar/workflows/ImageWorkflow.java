package org.tdar.workflows;

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.tdar.filestore.FileType;
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
        addRequired(ImageWorkflow.class, Arrays.asList("gif", "tif", "jpg", "tiff", "jpeg"));

//        registerFileExtensions(new String[] { "gif", "tif", "jpg", "tiff", "jpeg" },
//                ResourceType.IMAGE, ResourceType.SENSORY_DATA);
//        registerFileExtensions(new String[] { "bmp", "pict", "png" }, ResourceType.IMAGE);
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
