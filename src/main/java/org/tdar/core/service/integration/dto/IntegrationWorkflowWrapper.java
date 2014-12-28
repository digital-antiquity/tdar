package org.tdar.core.service.integration.dto;

import org.tdar.core.dao.GenericDao;
import org.tdar.core.service.integration.IntegrationContext;


public interface IntegrationWorkflowWrapper {

    int getVersion();

    String getTitle();
    
    String getDescription();

    boolean isValid();

    void validate(GenericDao service);
    
    IntegrationContext toIntegrationContext(GenericDao service);
}
