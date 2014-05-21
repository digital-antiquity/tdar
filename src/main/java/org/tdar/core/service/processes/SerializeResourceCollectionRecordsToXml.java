package org.tdar.core.service.processes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.util.ScheduledBatchProcess;
import org.tdar.core.service.XmlService;

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
public class SerializeResourceCollectionRecordsToXml extends ScheduledBatchProcess<ResourceCollection> {

    private static final long serialVersionUID = -7464071895046875197L;

    @Autowired
    private transient XmlService xmlService;

    @Override
    public String getDisplayName() {
        return "Serialize all resource collection records to record.xml";
    }

    @Override
    public Class<ResourceCollection> getPersistentClass() {
        return ResourceCollection.class;
    }

    @Override
    public boolean isSingleRunProcess() {
        return true;
    }

    @Override
    public void process(ResourceCollection resource) {
        xmlService.logRecordXmlToFilestore(resource);
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
