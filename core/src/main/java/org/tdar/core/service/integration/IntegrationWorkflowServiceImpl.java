package org.tdar.core.service.integration;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.dao.integration.IntegrationWorkflowDao;
import org.tdar.core.dao.resource.OntologyNodeDao;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.ServiceInterface;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.integration.dto.IntegrationDeserializationException;
import org.tdar.core.service.integration.dto.IntegrationWorkflowWrapper;
import org.tdar.core.service.integration.dto.v1.IntegrationWorkflowData;

import com.opensymphony.xwork2.TextProvider;

/**
 * Service class serving as a bridge between json data and IntegrationContext objects.
 * 
 * JSON data gets converted into an intermediate POJO that can validate itself and return an
 * IntegrationContext object with a list of any validation (referential) errors.
 * 
 */
@Service
public class IntegrationWorkflowServiceImpl  extends ServiceInterface.TypedDaoBase<DataIntegrationWorkflow, IntegrationWorkflowDao> implements IntegrationWorkflowService {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient SerializationService serializationService;

    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient OntologyNodeDao ontologyNodeDao;

    /* (non-Javadoc)
     * @see org.tdar.core.service.integration.IntegrationWorkflowService#toIntegrationContext(org.tdar.core.bean.integration.DataIntegrationWorkflow, com.opensymphony.xwork2.TextProvider)
     */
    @Override
    @Transactional
    public IntegrationContext toIntegrationContext(DataIntegrationWorkflow workflow, TextProvider provider) throws IOException,
            IntegrationDeserializationException {
        IntegrationWorkflowData workflowData = serializationService.readObjectFromJson(workflow.getJsonData(), IntegrationWorkflowData.class);
        IntegrationContext context = workflowData.toIntegrationContext(ontologyNodeDao, provider);
        // perform validity checks?
        return context;
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.integration.IntegrationWorkflowService#saveForController(org.tdar.core.bean.integration.DataIntegrationWorkflow, org.tdar.core.service.integration.dto.v1.IntegrationWorkflowData, java.lang.String, org.tdar.core.bean.entity.TdarUser, com.opensymphony.xwork2.TextProvider)
     */
    @Override
    @Transactional(readOnly = false)
    public IntegrationSaveResult saveForController(DataIntegrationWorkflow persistable, IntegrationWorkflowData data, String json, TdarUser authUser,
            TextProvider provider) {
        IntegrationSaveResult result = new IntegrationSaveResult();
        result.setStatus(IntegrationSaveResult.ERROR);
        if (data == null) {
            result.getErrors().add(provider.getText("integrationWorkflowService.data_missing"));
            return result;
        }
        try {
            logger.debug("{}",data);
            validateWorkflow(data, provider);
            persistable.markUpdated(authUser);
            data.copyValuesToBean(persistable, json);
            ontologyNodeDao.saveOrUpdate(persistable);
            result.setStatus(IntegrationSaveResult.SUCCESS);
            result.setId(persistable.getId());
        } catch (IntegrationDeserializationException e) {
            result.getErrors().add(e.getMessage());
        } catch (Exception e) {
            logger.error("error", e);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.integration.IntegrationWorkflowService#validateWorkflow(org.tdar.core.service.integration.dto.IntegrationWorkflowWrapper, com.opensymphony.xwork2.TextProvider)
     */
    @Override
    @Transactional(readOnly = true)
    public void validateWorkflow(IntegrationWorkflowWrapper data, TextProvider provider) throws IntegrationDeserializationException {
        if (data == null) {
            return;
        }
        data.validate(ontologyNodeDao, provider);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.integration.IntegrationWorkflowService#getWorkflowsForUser(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public List<DataIntegrationWorkflow> getWorkflowsForUser(TdarUser authorizedUser) {
        return getDao().getWorkflowsForUser(authorizedUser, authorizationService.isEditor(authorizedUser));
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.integration.IntegrationWorkflowService#deleteForController(com.opensymphony.xwork2.TextProvider, org.tdar.core.bean.integration.DataIntegrationWorkflow, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = false)
    public void deleteForController(TextProvider provider, DataIntegrationWorkflow persistable, TdarUser authenticatedUser) {
        getDao().delete(persistable);
    }
}
