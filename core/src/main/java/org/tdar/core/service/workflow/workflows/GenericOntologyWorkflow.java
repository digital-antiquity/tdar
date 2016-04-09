package org.tdar.core.service.workflow.workflows;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.file.FileType;
import org.tdar.core.service.workflow.workflows.Workflow.BaseWorkflow;

/**
 * $Id$
 * 
 * @author Adam Brin
 * @version $Revision$
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
