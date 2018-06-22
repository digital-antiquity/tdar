package org.tdar.fileprocessing.workflows;

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.tdar.fileprocessing.tasks.ImageThumbnailTask;
import org.tdar.fileprocessing.tasks.IndexableTextExtractionTask;
import org.tdar.filestore.FileType;

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
