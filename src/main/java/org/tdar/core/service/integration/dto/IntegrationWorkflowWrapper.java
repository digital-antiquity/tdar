package org.tdar.core.service.integration.dto;

import org.tdar.core.dao.GenericDao;
import org.tdar.core.service.integration.IntegrationContext;


public interface IntegrationWorkflowWrapper {

    int getVersion();

    String getTitle();
    
    String getDescription();

    boolean isValid();

    void validate(GenericDao service) throws IntegrationDeserializationException;
    
    IntegrationContext toIntegrationContext(GenericDao service) throws IntegrationDeserializationException;
}
