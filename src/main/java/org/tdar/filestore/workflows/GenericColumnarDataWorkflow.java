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
 * 
 * FIXME: figure out object lifecycle for workflows + tasks
 * 
 * @author Adam Brin
 * 
 */
@Component
public class GenericColumnarDataWorkflow extends BaseWorkflow {

    @Override
    public FileType getInformationResourceFileType() {
        return FileType.COLUMNAR_DATA;
    }

    public GenericColumnarDataWorkflow() {
        registerFileExtension("csv", ResourceType.CODING_SHEET, ResourceType.DATASET);
        // registerExtension("tab",ResourceType.CODING_SHEET);
        registerFileExtension("merge", ResourceType.CODING_SHEET);
        registerFileExtension("xlsx", ResourceType.CODING_SHEET, ResourceType.DATASET);
        registerFileExtension("xls", ResourceType.CODING_SHEET, ResourceType.DATASET);
        registerFileExtension("mdb", ResourceType.DATASET);
        registerFileExtension("accdb", ResourceType.DATASET);
        registerFileExtension("mdbx", ResourceType.DATASET);
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
