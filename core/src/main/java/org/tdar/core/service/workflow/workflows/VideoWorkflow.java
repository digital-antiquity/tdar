package org.tdar.core.service.workflow.workflows;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.file.FileType;

/**
 * <p>
 * The start of the workflow that video files will follow.
 * <p>
 * If we are going to support play back then we need to make sure that there is a mp3 copy. But perhaps we only need to do the conversion when they request the
 * play back to occur? Another approach would be to simply sample the first 10 seconds, say, and then save that sample for play back.
 * 
 * @author Martin Paulo
 */
@Component
public class VideoWorkflow extends BaseWorkflow {

    /**
     * <p>
     * From: http://guides.archaeologydataservice.ac.uk/g2gp/Video_2
     * <p>
     * We are only going to support those formats that are marked as being suitable for preservation. We have excluded Matroska because we won’t know the codec
     * in advance
     * <p>
     * At the moment we only want to upload an save the files, but at a later date we do want to extract extra information. One possible approach would be to
     * use xuggler (http://www.xuggle.com/xuggler/documentation), however we would have to build the jar ourselves, due to the licensing conditions. The formats
     * that are claimed to be supported by xuggler are here: http://stackoverflow.com/questions/9727590/what-codecs-does-xuggler-support.
     * <p>
     * Another approach might be to install MediaInfo (http://mediaarea.net/en/MediaInfo) and use JNI to grab the info. More on these two here:
     * http://stackoverflow.com/questions/2168472/media-information-extractor-for-java
     * <p>
     * We need to be able to extract a thumbnail
     */
//    static final Collection<String> VIDEO_EXTENSIONS_SUPPORTED = java.util.Arrays.asList(new String[] { "mpg", "mpeg", "mp4", "mj2", "mjp2" });

    public VideoWorkflow() {
//        for (String extension : VIDEO_EXTENSIONS_SUPPORTED) {
//            registerFileExtension(extension, ResourceType.VIDEO);
//        }
        // what tasks do we want to do with video?
        // at the very least we should extract some of the meta data and put it into the parent resource, I'm guessing...
        // addTask(ExtractVideoInfoTask.class, WorkflowPhase.PRE_PROCESS);
    }

    @Override
    public FileType getInformationResourceFileType() {
        return FileType.VIDEO;
    }

}
