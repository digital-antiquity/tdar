package org.tdar.core.service.processes.daily;

import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.processes.AbstractScheduledBatchProcess;

/**
 * $Id$
 * 
 * ScheduledProcess to remove authorized Users that have expired
 * 
 * 
 * @author <a href='mailto:adam.brin@asu.edu'>Adam Brin</a>
 * @version $Rev$
 */

@Component
@Scope("prototype")
public class DailyTimedAccessRevokingProcess extends AbstractScheduledBatchProcess<ResourceCollection> {

    private static final long serialVersionUID = 7534566757094920406L;
    public TdarConfiguration config = TdarConfiguration.getInstance();
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient ResourceCollectionService resourceCollectionService;


    @Override
    public String getDisplayName() {
        return "Timed Access Revoking Porcess";

    }
    @Override
    public boolean isSingleRunProcess() {
        return false;
    }

    @Override
    public boolean shouldRunAtStartup() {
        return true;
    }

    /**
     * This ScheduledProcess is finished to completion after execute().
     */
    @Override
    public boolean isCompleted() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    @Override
    public void process(ResourceCollection persistable) throws Exception {
        DateTime now = DateTime.now();
        boolean changed = false;
        for (AuthorizedUser au : persistable.getAuthorizedUsers()) {
            if (now.isAfter(au.getDateExpires().getTime())) {
                persistable.getAuthorizedUsers().remove(au);
                genericDao.delete(au);
                changed = true;
            }
        }
        if (changed) {
            resourceCollectionService.saveOrUpdate(persistable);
        }
    }
    
    @Override
    public List<Long> findAllIds() {
        return resourceCollectionService.findCollectionIdsWithTimeLimitedAccess();
    }
    
    @Override
    public Class<ResourceCollection> getPersistentClass() {
        return ResourceCollection.class;
    }

}
