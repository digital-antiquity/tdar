package org.tdar.core.service.workflow.workflows;

import java.util.Collection;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.file.FileType;
import org.tdar.filestore.tasks.ExtractAudioInfoTask;

/**
 * <p>
 * The workflow that audio files will follow.
 * <p>
 * If we are going to support play back then we need to make sure that there is a mp3 copy. But perhaps we only need to do the conversion when they request the
 * play back to occur? Another approach would be to simply sample the first 10 seconds, say, and then save that sample for play back.
 * 
 * @author Martin Paulo
 */
@Component
public class AudioWorkflow extends BaseWorkflow {

    /**
     * <p>
     * From: http://guides.archaeologydataservice.ac.uk/g2gp/Audio_3
     * <p>
     * We should support wav, bwf, aif and flac. However, the AudioSystem in java only supports wav, au and aif. So we are probably going to need to add
     * decoders to the Audio SPI. However, at the moment the request for this work makes no mention of the file types to be supported, so for the time being, we
     * will only support the ones the AudioSystem supports out of the box. And solicit user feedback.
     * <p>
     * Armin has requested that we simply ignore the files that are not supported, and that we add mp3 to the list for the time being.
     * <p>
     * <code>AudioSystem.getAudioFileTypes()</code> lists the types of audio files the system supports.
     * <p>
     * jFLAC is at: http://jflac.sourceforge.net/
     */
    public static final Collection<String> AUDIO_EXTENSIONS_SUPPORTED = java.util.Arrays.asList(new String[] { "wav", "aif", "aiff", "flac", "bwf", "mp3" });

    public AudioWorkflow() {
        for (String extension : AUDIO_EXTENSIONS_SUPPORTED) {
            registerFileExtension(extension, ResourceType.AUDIO);
        }
        // what tasks do we want to do with audio?
        // at the very least we should extract some of the meta data and put it into the parent resource, I'm guessing...
        addTask(ExtractAudioInfoTask.class, WorkflowPhase.PRE_PROCESS);
    }

    @Override
    public FileType getInformationResourceFileType() {
        return FileType.AUDIO;
    }

    // following doesn't seem to be used?
    @Override
    public boolean isEnabled() {
        return false;
    }
}
