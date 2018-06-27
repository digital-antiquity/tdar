package org.tdar.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.fileprocessing.workflows.AudioWorkflow;
import org.tdar.fileprocessing.workflows.FileArchiveWorkflow;
import org.tdar.fileprocessing.workflows.GenericColumnarDataWorkflow;
import org.tdar.fileprocessing.workflows.GenericDocumentWorkflow;
import org.tdar.fileprocessing.workflows.ImageWorkflow;
import org.tdar.fileprocessing.workflows.PDFWorkflow;
import org.tdar.fileprocessing.workflows.VideoWorkflow;
import org.tdar.fileprocessing.workflows.Workflow;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.FileStoreFile;

public class FileAnalyzerTest {

    FileAnalyzer analyzer = new FileAnalyzer();

    @Before
    public void setup() {
        analyzer.setWorkflows(Arrays.asList(new GenericColumnarDataWorkflow(), new ImageWorkflow(), new GenericDocumentWorkflow(), new PDFWorkflow(),
                new FileArchiveWorkflow(), new AudioWorkflow(), new VideoWorkflow()));
    }
    

    @Test
    public void testAnalyzerSuggestions() {
        assertEquals(ResourceType.DOCUMENT, analyzer.suggestTypeForFileName("test.doc", ResourceType.DOCUMENT));
        assertEquals(ResourceType.SENSORY_DATA, analyzer.suggestTypeForFileName("test.gif", ResourceType.SENSORY_DATA, ResourceType.IMAGE));
        assertEquals(ResourceType.IMAGE, analyzer.suggestTypeForFileName("test.gif", ResourceType.IMAGE, ResourceType.SENSORY_DATA));
        assertNull(analyzer.suggestTypeForFileName("test.xls", ResourceType.ONTOLOGY));
        assertEquals(ResourceType.CODING_SHEET, analyzer.suggestTypeForFileName("test.xls", ResourceType.ONTOLOGY, ResourceType.CODING_SHEET));
        assertFalse(analyzer.getExtensionsForType(ResourceType.ARCHIVE).contains("xml"));
        assertEquals(ResourceType.AUDIO, analyzer.suggestTypeForFileName("test.aiff", ResourceType.values()));
    }

    @Test
    public void testImage() {
        FileStoreFile fsf = new FileStoreFile();
        fsf.setFilename("test.jpg");
        fsf.setExtension("jpg");
        Workflow workflow = analyzer.getWorkflowForResourceType(fsf, ResourceType.IMAGE);
        assertEquals(ImageWorkflow.class, workflow.getClass());
    }
    
    @Test
    public void testImageSuggestion() {
        FileStoreFile fsf = new FileStoreFile();
        fsf.setFilename("test.jpg");
        fsf.setExtension("jpg");
        ResourceType type = analyzer.suggestTypeForFileName(fsf.getFilename(), ResourceType.DATASET, ResourceType.DOCUMENT, ResourceType.IMAGE);
        assertEquals(ResourceType.IMAGE, type);
    }

}
