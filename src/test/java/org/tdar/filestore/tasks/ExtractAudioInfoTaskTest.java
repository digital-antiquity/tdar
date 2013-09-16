package org.tdar.filestore.tasks;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.Audio;

/**
 * A barebones test required for the processing of the extract audio info task.
 * A run through some of the test files at http://www-mmsp.ece.mcgill.ca/documents/AudioFormats/AIFF/Samples.html shows that there are a number of AIF files
 * not supported by the java sound api :(
 * 
 * @author Martin Paulo
 */
public class ExtractAudioInfoTaskTest {

    private static final String AIFF_EXPECTED =
            "AIFF (.aif) file, byte length: 829726, data format: PCM_SIGNED 44100.0 Hz, 16 bit, stereo, 4 bytes/frame, big-endian, frame length: 207360";
    private static final String WAV_EXPECTED =
            "WAVE (.wav) file, byte length: 829786, data format: PCM_SIGNED 44100.0 Hz, 16 bit, stereo, 4 bytes/frame, little-endian, frame length: 207360";
    private Audio testSubject = new Audio();

    @Test
    public void readAiffFileMetadata() throws UnsupportedAudioFileException, IOException {
        testFileCodec("testing.aiff", AIFF_EXPECTED);
    }

    @Test
    public void readWavFileMetadata() throws UnsupportedAudioFileException, IOException {
        testFileCodec("testing.wav", WAV_EXPECTED);
    }
    
    private void testFileCodec(String fileName, String expected) throws UnsupportedAudioFileException, IOException {
        ExtractAudioInfoTask task = new ExtractAudioInfoTask();
        File audioFile = new File(TestConstants.TEST_AUDIO_DIR + fileName);
        task.writeFileMetadataToAudioFile(testSubject, audioFile);
        assertTrue(expected.equals(testSubject.getAudioCodec()));;
    }

}
