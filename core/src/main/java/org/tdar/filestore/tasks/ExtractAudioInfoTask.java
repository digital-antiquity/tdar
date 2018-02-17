package org.tdar.filestore.tasks;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.tdar.core.bean.resource.Audio;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.filestore.WorkflowContext;

public class ExtractAudioInfoTask extends AbstractTask {

    private static final long serialVersionUID = -6384923066139056050L;

    @Override
    public void run() {
        final WorkflowContext ctx = getWorkflowContext();

        // first off, a whole raft of preconditions that we need to pass before we write the control file:
        // reality check: do we have an archive?
        final Class<? extends Resource> resourceClass = ctx.getResourceType().getResourceClass();
        if (Audio.class != resourceClass) {
            recordErrorAndExit("The Extract Audio Info Task has been called for a non audio resource! Resource class was: " + resourceClass);
        }

        // if we can't get the archive, we don't have enough information to run...
        Audio audio = (Audio) ctx.getTransientResource();
        if (audio == null) {
            recordErrorAndExit("Transient copy of audio not available...");
        }

        // are there actual files to copy?
        final List<InformationResourceFileVersion> audioFiles = ctx.getOriginalFiles();
        if (audioFiles.size() <= 0) {
            recordErrorAndExit("Must have an audio file to work with");
        }

        // at the moment there should only be one file
        if (1 < audioFiles.size()) {
            recordErrorAndExit("There are too many audio files to work with (only one expected)");
        }

        // Preconditions have been checked, now to write the control file and extract the audio files to work with.
        // at the moment there should be only one of these files: however, that should only be an artifact of the user interface.
        for (InformationResourceFileVersion version : audioFiles) {
            File originalAudioFile = version.getTransientFile();
            writeFileMetadataToAudioFile(audio, originalAudioFile);
        }

    }

    protected void writeFileMetadataToAudioFile(Audio targetAudioResource, File originalAudioFile) {
        AudioFileFormat audioFileFormat;
        try {
            audioFileFormat = AudioSystem.getAudioFileFormat(originalAudioFile);
            targetAudioResource.setAudioCodec(audioFileFormat.toString());
        } catch (UnsupportedAudioFileException | IOException e) {
            targetAudioResource.setAudioCodec(" - ");
            getLogger().error("Swallowed error - file: " + originalAudioFile.getName(), e);
        }
    }

    @Override
    public String getName() {
        return "extract audio info task";
    }

}
