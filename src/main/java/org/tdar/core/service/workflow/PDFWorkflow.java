package org.tdar.core.service.workflow;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.InformationResourceFile.FileType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.service.workflow.Workflow.BaseWorkflow;
import org.tdar.filestore.tasks.IndexableTextExtractionTask;
import org.tdar.filestore.tasks.PDFDerivativeTask;

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
		registerFileExtension("pdf",ResourceType.DOCUMENT);
		addTask(IndexableTextExtractionTask.class, WorkflowPhase.CREATE_DERIVATIVE);
        addTask(PDFDerivativeTask.class, WorkflowPhase.CREATE_DERIVATIVE);
	}

	public boolean isEnabled() {
		return true;
	}
}
