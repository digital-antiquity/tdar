package org.tdar.core.service.processes;

import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.util.ScheduledBatchProcess;
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
public class UpgradeResourceCollectionPermissions extends ScheduledBatchProcess<ResourceCollection> {

    /**
     * 
     */
    private static final long serialVersionUID = -2575013323075159910L;

    @Autowired
    private transient ResourceService resourceCollectionService;

    @Autowired
    private transient GenericService genericService;

    @Override
    public String getDisplayName() {
        return "Upgrade Resource CollectionID Tree";
    }

    @Override
    public List<Long> findAllIds() {
        return Persistable.Base.extractIds(resourceCollectionService.findAll(ResourceCollection.class));
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
        List<Long> extractIds = Persistable.Base.extractIds(resource.getHierarchicalResourceCollections());
        if (resource.getParentIds() == null) {
            resource.setParentIds(new HashSet<Long>());
        }
        if (resource.getType() == CollectionType.INTERNAL) {
            return;
        }
        getLogger().debug("processing: {} ({})", resource.getName(), resource.getId());
        extractIds.remove(resource.getId());
        resource.getParentIds().addAll(extractIds);
        genericService.saveOrUpdate(resource);
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
