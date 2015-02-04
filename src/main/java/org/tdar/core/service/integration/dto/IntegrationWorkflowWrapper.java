package org.tdar.core.service.integration.dto;

import org.tdar.core.dao.Dao.HibernateBase;
import org.tdar.core.dao.resource.OntologyNodeDao;
import org.tdar.core.service.integration.IntegrationContext;


public interface IntegrationWorkflowWrapper {

    int getVersion();

    String getTitle();
    
    String getDescription();

    boolean isValid();

    void validate(OntologyNodeDao service) throws IntegrationDeserializationException;
    
    IntegrationContext toIntegrationContext(OntologyNodeDao service) throws IntegrationDeserializationException;

    void setId(Long id);
}
