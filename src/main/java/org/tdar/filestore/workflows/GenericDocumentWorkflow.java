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
import org.tdar.filestore.workflows.Workflow.BaseWorkflow;

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
		registerFileExtension("doc", ResourceType.DOCUMENT);
		registerFileExtension("docx", ResourceType.DOCUMENT);
		registerFileExtension("txt", ResourceType.DOCUMENT);
		// don't register PDF, because it's duplicative of an actual workflow that handles tihs.
	}
	
	@Override
	public boolean isEnabled() {
		return false;
	}
}
