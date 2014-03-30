package org.tdar.core.service.processes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Creator;
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
public class SerializeCreatorRecordsToXml extends ScheduledBatchProcess<Creator> {

    private static final long serialVersionUID = -3071550641041719444L;
    
    @Autowired
    private transient XmlService xmlService;

    @Override
    public String getDisplayName() {
        return "Serialize all creator to record.xml";
    }

    @Override
    public Class<Creator> getPersistentClass() {
        return Creator.class;
    }

    @Override
    public boolean isSingleRunProcess() {
        return true;
    }

    @Override
    public void process(Creator resource) {
        xmlService.logRecordXmlToFilestore(resource);
    }

    @Override
    public int getBatchSize() {
        return 500;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

}
