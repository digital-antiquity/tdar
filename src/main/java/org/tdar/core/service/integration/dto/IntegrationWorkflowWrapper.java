package org.tdar.core.service.integration.dto;

import org.tdar.core.dao.GenericDao;
import org.tdar.core.service.integration.IntegrationContext;

import com.opensymphony.xwork2.TextProvider;

public interface IntegrationWorkflowWrapper {

    int getVersion();

    String getTitle();

    String getDescription();

    boolean isValid();

    void validate(GenericDao service, TextProvider provider) throws IntegrationDeserializationException;

    IntegrationContext toIntegrationContext(GenericDao service, TextProvider provider) throws IntegrationDeserializationException;

    void setId(Long id);
}
