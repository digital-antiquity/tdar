package org.tdar.core.service.processes;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.util.ScheduledBatchProcess;
import org.tdar.core.service.AccountService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.resource.ResourceService;

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
public class InitializeBillingValuesOnResource extends ScheduledBatchProcess<Resource> {

    private static final long serialVersionUID = -4223808692952181718L;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private GenericService genericService;

    @Override
    public String getDisplayName() {
        return "set values on resource for space and files used";
    }

    @Override
    public List<Long> findAllIds() {
        return resourceService.findAllResourceIdsWithFiles();
    }

    @Override
    public Class<Resource> getPersistentClass() {
        return Resource.class;
    }

    @Override
    public boolean isSingleRunProcess() {
        return true;
    }

    @Override
    public void process(Resource resource) {
        accountService.getResourceEvaluator(resource);
        genericService.saveOrUpdate(resource);
    }

    @Override
    public int getBatchSize() {
        return 5;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

}
