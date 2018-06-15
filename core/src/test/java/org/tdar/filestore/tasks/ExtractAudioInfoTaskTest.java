package org.tdar.filestore.tasks;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.Audio;
import org.tdar.filestore.FileStoreFile;

/**
 * A bare bones test required for the processing of the extract audio info task.
 * A run through some of the test files at http://www-mmsp.ece.mcgill.ca/documents/AudioFormats/AIFF/Samples.html shows that there are a number of AIF files
 * not supported by the java sound api :(
 * 
 * @author Martin Paulo
 */
public class ExtractAudioInfoTaskTest {

    private static final String UNKNOWN_CODEC = " - ";

    private static final String AIFF_EXPECTED = "AIFF (.aif) file, byte length: 829726, data format: PCM_SIGNED 44100.0 Hz, 16 bit, stereo, 4 bytes/frame, big-endian, frame length: 207360";
    private static final String WAV_EXPECTED = "WAVE (.wav) file, byte length: 829786, data format: PCM_SIGNED 44100.0 Hz, 16 bit, stereo, 4 bytes/frame, little-endian, frame length: 207360";
    private Audio testSubject = new Audio();

    @Test
    public void readAiffFileMetadata() throws FileNotFoundException {
        testFileCodec("testing.aiff", AIFF_EXPECTED);
    }

    @Test
    public void readWavFileMetadata() throws FileNotFoundException {
        testFileCodec("testing.wav", WAV_EXPECTED);
    }

    @Test
    public void readFlacFileMetadata() throws FileNotFoundException {
        testFileCodec("testing.flac", UNKNOWN_CODEC);
    }

    @Test
    public void readMp3FileMetadata() throws FileNotFoundException {
        testFileCodec("testing.mp3", UNKNOWN_CODEC);
    }

    private void testFileCodec(String fileName, String expected) throws FileNotFoundException {
        ExtractAudioInfoTask task = new ExtractAudioInfoTask();
        File audioFile = TestConstants.getFile(TestConstants.TEST_AUDIO_DIR, fileName);
        FileStoreFile fsf = new FileStoreFile();
        task.writeFileMetadataToAudioFile(fsf, audioFile);
        final String audioCodecFound = fsf.getCodex();
        assertTrue("Expected: '" + expected + "' but found: '" + audioCodecFound + "'", expected.equals(audioCodecFound));
    }

}
