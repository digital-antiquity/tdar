/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.filestore.workflows;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.InformationResourceFile.FileType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.filestore.tasks.PDFDerivativeTask;
import org.tdar.filestore.workflows.Workflow.BaseWorkflow;

/**
 * @author Adam Brin
 *
 */
@Component
public class PDFWorkflow extends BaseWorkflow {
	
	@Override
	public FileType getInformationResourceFileType() {
		return FileType.DOCUMENT;
	}

	public PDFWorkflow() {
		registerFileExtension("pdf",ResourceType.DOCUMENT);
		addTask(new PDFDerivativeTask(), WorkflowPhase.CREATE_DERIVATIVE);
	}

	public boolean isEnabled() {
		return true;
	}
}
