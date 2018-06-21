/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service.workflow.workflows;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.file.FileType;
import org.tdar.filestore.tasks.IndexableTextExtractionTask;

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
