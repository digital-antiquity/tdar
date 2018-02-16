package org.tdar.core.service.integration;

import java.io.IOException;
import java.util.List;

import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.service.integration.dto.IntegrationDeserializationException;
import org.tdar.core.service.integration.dto.IntegrationWorkflowWrapper;
import org.tdar.core.service.integration.dto.v1.IntegrationWorkflowData;

import com.opensymphony.xwork2.TextProvider;

public interface IntegrationWorkflowService {

    IntegrationContext toIntegrationContext(DataIntegrationWorkflow workflow, TextProvider provider) throws IOException,
            IntegrationDeserializationException;

    IntegrationSaveResult saveForController(DataIntegrationWorkflow persistable, IntegrationWorkflowData data, String json, TdarUser authUser,
            TextProvider provider);

    void validateWorkflow(IntegrationWorkflowWrapper data, TextProvider provider) throws IntegrationDeserializationException;

    List<DataIntegrationWorkflow> getWorkflowsForUser(TdarUser authorizedUser);

    void deleteForController(TextProvider provider, DataIntegrationWorkflow persistable, TdarUser authenticatedUser);

    DataIntegrationWorkflow find(Long id);

    DataIntegrationWorkflow duplicateWorkflow(DataIntegrationWorkflow workflow, TdarUser user);

}