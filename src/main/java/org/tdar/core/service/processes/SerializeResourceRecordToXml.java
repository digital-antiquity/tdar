package org.tdar.core.service.processes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.util.ScheduledBatchProcess;
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
public class SerializeResourceRecordToXml extends ScheduledBatchProcess<Resource> {

    
    /**
     * 
     */
    private static final long serialVersionUID = 7024941986161148001L;
    @Autowired
    private ResourceService resourceService;

    @Override
    public String getDisplayName() {
        return "Serialize all resources to record.xml";
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
        resourceService.saveRecordToFilestore(resource);
    }

    @Override
    public int getBatchSize() {
        return 500;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }

}
