package org.tdar.core.service.processes.upgradeTasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.processes.AbstractScheduledBatchProcess;

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
public class SerializeResourceRecordToXml extends AbstractScheduledBatchProcess<Resource> {

    private static final long serialVersionUID = 7024941986161148001L;

    @Autowired
    SerializationService serializationService;

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
        serializationService.logRecordXmlToFilestore(resource);
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
