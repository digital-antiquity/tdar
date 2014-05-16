package org.tdar.core.bean.resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Martin Paulo
 */
public class ResourceTypeTestCase {

    /**
     * This test is only useful in that it guards against accidental changes to the expected elements of the Audio ResourceType.
     * Is this a useful thing to test? I guess it depends on the assumptions underlying the resource type, and how the values in the fields are changed.
     * The test could made more generic, so that a simple set of expected results is compared to the actual: in that way all resource types could be
     * tested like this.
     */
    @SuppressWarnings("static-method")
    @Test
    public void testAudioIsAsExpected() {
        final ResourceType audio = ResourceType.AUDIO;
        assertTrue(audio.isAudio());
        assertFalse(audio.isArchive());
        assertFalse(audio.isCodingSheet());
        assertFalse(audio.isCompositeFilesEnabled());
        assertFalse(audio.isDataset());
        assertFalse(audio.isDataTableSupported());
        assertFalse(audio.isDocument());
        assertFalse(audio.isGeospatial());
        assertFalse(audio.isImage());
        assertFalse(audio.isOntology());
        assertFalse(audio.isProject());
        assertFalse(audio.isSensoryData());
        assertFalse(audio.isSupporting());
        assertFalse(audio.isVideo());
        assertFalse(audio.supportBulkUpload());
        assertTrue(audio.getLabel().equals(audio.getPlural()));
        assertTrue(audio.getResourceClass().equals(Audio.class));
        assertTrue("Found field name of: " + audio.getFieldName(), "audio".equals(audio.getFieldName()));
        assertTrue("Found label name of: " + audio.getLabel(), "Audio".equals(audio.getLabel()));
        assertTrue("Found dcmi string of: " + audio.toDcmiTypeString(), "Sound".equals(audio.toDcmiTypeString()));
        assertTrue("Found dcmi string of: " + audio.toDcmiTypeString(), "Sound".equals(audio.toDcmiTypeString()));
        assertTrue(audio.equals(ResourceType.fromString("AUDIO")));
        assertTrue(audio.equals(ResourceType.fromClass(Audio.class)));
        assertTrue("AudioObject".equals(audio.getSchema()));
        assertTrue(audio.getOrder() == 11);
        assertTrue("unknown".equals(audio.getOpenUrlGenre()));
    }

}
