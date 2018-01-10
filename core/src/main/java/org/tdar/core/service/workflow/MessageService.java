package org.tdar.core.service.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.service.workflow.workflows.Workflow;

public interface MessageService {

    /**
     * FIXME: should this be transactional? If so, convert GenericService references to GenericDao.
     * 
     * This is the beginning of the message process, it takes files that have just been uploaded and creates a workflow context for them, and sends them
     * on the MessageQueue
     * 
     * @param informationResourceFileVersions
     * @param workflow2
     * @return Martin: given that at some future date this might be pushing stuff onto a queue, it shouldn't return anything?
     */
    <W extends Workflow> boolean sendFileProcessingRequest(Workflow workflow, InformationResourceFileVersion... informationResourceFileVersions);

    /**
     * @param workflowContextService
     *            the workflowContextService to set
     */
    void setWorkflowContextService(WorkflowContextService workflowContextService);

}