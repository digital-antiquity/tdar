package org.tdar.core.service.integration.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.Transient;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.dao.GenericDao;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

/**
 * Abstract class to handle shared metadata for all Integration Workflow objects
 */
public abstract class AbstractIntegrationWorkflowData {

    @Transient
    protected Logger logger = LoggerFactory.getLogger(getClass());

    public abstract String getTitle();

    public abstract String getDescription();

    /**
     * Copy the title, description and JSON Data ont the bean
     * 
     * @param workflow
     * @param json
     */
    public void copyValuesToBean(DataIntegrationWorkflow workflow, String json) {
        workflow.setTitle(getTitle());
        workflow.setDescription(getDescription());
        workflow.setJsonData(json);
    }

    /**
     * load the entry and then set the persistable on the original skeleton
     * 
     * @param dao
     * @param skeletons
     * @param cls
     * @return
     * @throws IntegrationDeserializationException
     */
    protected <P extends Persistable> List<P> hydrate(GenericDao dao, List<? extends IntegrationDTO<P>> skeletons, Class<P> cls)
            throws IntegrationDeserializationException {
        Map<Long, ? extends Persistable> ids = PersistableUtils.createIdMap(skeletons);
        List<P> objects = dao.findAll(cls, ids.keySet());
        logger.trace("{} - id: {} : obj: {}", cls.getSimpleName(), ids, objects);
        for (P object : objects) {
            ((IntegrationDTO) ids.get(object.getId())).setPersistable(object);
        }
        return objects;
    }

    /**
     * Helper to simplify managing map of lists
     * 
     * @param fieldErrors
     * @param string
     * @return
     */
    protected List<String> checkAddKey(Map<String, List<String>> fieldErrors, String string) {
        if (!fieldErrors.containsKey(string)) {
            fieldErrors.put(string, new ArrayList<String>());
        }
        return fieldErrors.get(string);
    }

    /**
     * for each entry, check that the mapper persistable is not null
     * 
     * @param service
     * @param entries
     * @param fieldErrors
     * @param key
     * @param provider
     */
    public void validateForNulls(GenericDao service, List<? extends IntegrationDTO> entries, Map<String, List<String>> fieldErrors, String key,
            TextProvider provider) {
        if (CollectionUtils.isEmpty(entries)) {
            return;
        }
        for (IntegrationDTO entry : entries) {
            if (entry == null) {
                continue;
            }
            if (entry.getPersistable() == null) {
                checkAddKey(fieldErrors, key).add(provider.getText("integrationWorkflowData.field_not_in_db", Arrays.asList(key, entry)));
            }
        }

    }
}
