/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.fileprocessing.workflows;

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.tdar.fileprocessing.tasks.IndexableTextExtractionTask;
import org.tdar.filestore.FileType;

/**
 * @author Adam Brin
 * 
 */
@Component
public class GenericDocumentWorkflow extends BaseWorkflow {

    @Override
    public FileType getInformationResourceFileType() {
        return FileType.DOCUMENT;
    }

    public GenericDocumentWorkflow() {
        addRequired(PDFWorkflow.class, Arrays.asList("rtf", "doc", "docx", "txt"));

//        registerFileExtension("rtf", ResourceType.DOCUMENT);
//        registerFileExtension("doc", ResourceType.DOCUMENT);
//        registerFileExtension("docx", ResourceType.DOCUMENT);
//        registerFileExtension("txt", ResourceType.DOCUMENT);
        addTask(IndexableTextExtractionTask.class, WorkflowPhase.CREATE_DERIVATIVE);
        // don't register PDF, because it's duplicative of an actual workflow that handles tihs.
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
