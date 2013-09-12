package org.tdar.core.service.workflow.workflows;

import java.util.Collection;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.InformationResourceFile.FileType;
import org.tdar.core.service.workflow.workflows.Workflow.BaseWorkflow;

@Component
public class AudioWorkflow extends BaseWorkflow {
    
    /**
     * From: http://guides.archaeologydataservice.ac.uk/g2gp/Audio_3
     * We are probably going to need to add FLAC decoders to the Audio SPI
     * AudioSystem.getAudioFileTypes() will list the types the system supports. Currently I am expecting wav and aif to be the only two on this list...
     * jFLAC is at: http://jflac.sourceforge.net/
     * If we are going to support play back then we need to make sure that there is a mp3 copy. But perhaps we only need to do the conversion when they
     * request the play back to occur? Another approach would be to simply sample the first 10 seconds, say, and then save that sample for play back.
     */
    public static final Collection<String> AUDIO_EXTENSIONS_SUPPORTED = java.util.Arrays.asList(new String[]{"wav", "bwf", "aif", "aiff", "flac"});
    
    public AudioWorkflow() {
        for (String extension: AUDIO_EXTENSIONS_SUPPORTED) {
            registerFileExtension(extension, ResourceType.AUDIO);
        }
        // what tasks do we want to do with audio?
        // at the very least we should extract some of the meta data and put it into the parent resource.
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
