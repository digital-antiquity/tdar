package org.tdar.core.service.workflow.workflows;

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.file.FileType;

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
        addRequired(GenericOntologyWorkflow.class, Arrays.asList("owl", "rdf"));
//        registerFileExtension("owl", ResourceType.ONTOLOGY);
//        registerFileExtension("rdf", ResourceType.ONTOLOGY);
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
