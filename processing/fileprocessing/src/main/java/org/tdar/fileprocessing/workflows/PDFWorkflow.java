package org.tdar.fileprocessing.workflows;

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.tdar.fileprocessing.tasks.IndexableTextExtractionTask;
import org.tdar.fileprocessing.tasks.PDFDerivativeTask;
import org.tdar.filestore.FileType;

/**
 * $Id$
 * 
 * @author Adam Brin
 * @version $Revision$
 */
@Component
public class PDFWorkflow extends BaseWorkflow {

    @Override
    public FileType getInformationResourceFileType() {
        return FileType.DOCUMENT;
    }

    public PDFWorkflow() {
        addRequired(GenericDocumentWorkflow.class, Arrays.asList("pdf"));
        addTask(IndexableTextExtractionTask.class, WorkflowPhase.CREATE_DERIVATIVE);
        addTask(PDFDerivativeTask.class, WorkflowPhase.CREATE_DERIVATIVE);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
