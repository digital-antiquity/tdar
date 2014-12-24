package org.tdar.core.service.integration;

import java.io.IOException;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.SerializationService;

/**
 * Service class serving as a bridge between json data and IntegrationContext objects.
 * 
 * JSON data gets converted into an intermediate POJO that can validate itself and return an
 * IntegrationContext object with a list of any validation (referential) errors.
 * 
 */
@Service
public class IntegrationWorkflowService {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SerializationService serializationService;
    
    @Autowired
    private GenericService genericService;

    @Transactional
    public IntegrationContext toIntegrationContext(DataIntegrationWorkflow workflow) throws IOException {
        IntegrationWorkflowData workflowData = serializationService.readObjectFromJson(workflow.getJsonData(), IntegrationWorkflowData.class);
        IntegrationContext context = workflowData.toIntegrationContext(genericService);
        // perform validity checks?
        return context;
    }

}
