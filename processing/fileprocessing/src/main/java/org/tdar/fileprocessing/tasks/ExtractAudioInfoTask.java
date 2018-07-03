package org.tdar.fileprocessing.tasks;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.tdar.fileprocessing.workflows.WorkflowContext;
import org.tdar.filestore.FileStoreFile;

public class ExtractAudioInfoTask extends AbstractTask {

    private static final long serialVersionUID = -6384923066139056050L;

    @Override
    public void run() {
        final WorkflowContext ctx = getWorkflowContext();

        // are there actual files to copy?
        final List<FileStoreFile> audioFiles = ctx.getOriginalFiles();
        if (audioFiles.size() <= 0) {
            recordErrorAndExit("Must have an audio file to work with");
        }

        // at the moment there should only be one file
        if (1 < audioFiles.size()) {
            recordErrorAndExit("There are too many audio files to work with (only one expected)");
        }

        // Preconditions have been checked, now to write the control file and extract the audio files to work with.
        // at the moment there should be only one of these files: however, that should only be an artifact of the user interface.
        for (FileStoreFile version : audioFiles) {
            File originalAudioFile = version.getTransientFile();
            writeFileMetadataToAudioFile(version, originalAudioFile);
        }

    }

    protected void writeFileMetadataToAudioFile(FileStoreFile version, File originalAudioFile) {
        String audioFileFormat = " - ";
        try {
            audioFileFormat = AudioSystem.getAudioFileFormat(originalAudioFile).toString();
        } catch (UnsupportedAudioFileException | IOException e) {
            getLogger().error("Swallowed error - file: " + originalAudioFile.getName(), e);
        }
        version.setCodex(audioFileFormat);
    }

    @Override
    public String getName() {
        return "extract audio info task";
    }

}
