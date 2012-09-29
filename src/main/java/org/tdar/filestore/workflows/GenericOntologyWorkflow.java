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
public class GenericOntologyWorkflow extends BaseWorkflow {
	
	@Override
	public FileType getInformationResourceFileType() {
		return FileType.OTHER;
	}

	public GenericOntologyWorkflow() {
		registerFileExtension("owl", ResourceType.ONTOLOGY);
		registerFileExtension("rdf", ResourceType.ONTOLOGY);
	}
	
	@Override
	public boolean isEnabled() {
		return false;
	}
}
