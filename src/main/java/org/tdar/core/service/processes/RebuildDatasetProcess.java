package org.tdar.core.service.processes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.util.ScheduledBatchProcess;
import org.tdar.core.service.workflow.WorkflowContextService;

/**
 * $Id$
 * 
 * ScheduledProcess to reprocess all datasets.
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */

@Component
public class RebuildDatasetProcess extends ScheduledBatchProcess<Dataset> {

    private static final long serialVersionUID = -7637484880673479889L;

    @Autowired
    private transient WorkflowContextService workflowContextService;

    @Override
    public String getDisplayName() {
        return "Reprocess all datasets";
    }

    @Override
    public Class<Dataset> getPersistentClass() {
        return Dataset.class;
    }

    @Override
    public boolean isSingleRunProcess() {
        return true;
    }

    @Override
    public void process(Dataset dataset) {
        workflowContextService.getDatasetService().reprocess(dataset);
    }

    @Override
    public int getBatchSize() {
        return 5;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    /**
     * @param messageService
     *            the messageService to set
     */

}
