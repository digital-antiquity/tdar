package org.tdar.core.service.integration.dto;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.dao.GenericDao;
import org.tdar.utils.PersistableUtils;

/**
 * Abstract class to handle shared metadata for all Integration Workflow objects
 */
public abstract class AbstractIntegrationWorkflowData {

    public abstract String getTitle();
    
    public abstract String getDescription();

    public void copyValuesToBean(DataIntegrationWorkflow workflow, String json) {
        workflow.setTitle(getTitle());
        workflow.setDescription(getDescription());
        workflow.setJsonData(json);
    }

    protected <P extends Persistable> List<P> validate(GenericDao dao, List<? extends Persistable> skeletons, Class<P> cls) throws IntegrationDeserializationException {
        Map<Long, ? extends Persistable> ids = PersistableUtils.createIdMap(skeletons);
        List<P> objects = dao.findAll(cls, ids.keySet());
        for (P object : objects) {
            ids.remove(object.getId());
        }
        if (CollectionUtils.isNotEmpty(ids.keySet())) {
            throw new IntegrationDeserializationException(ids.values());
        }
        return objects;
    }
}
