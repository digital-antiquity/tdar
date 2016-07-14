package org.tdar.struts.action.api.integration;

import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.service.integration.DataIntegrationService;
import org.tdar.core.service.integration.ModernIntegrationDataResult;
import org.tdar.core.service.integration.dto.IntegrationDeserializationException;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.utils.json.JsonIntegrationFilter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Class to manage the Integraiton process via the JSON model
 * (unfinished)
 * 
 * @author abrin
 *
 */
@Namespace("/api/integration")
@ParentPackage("secured")
@Component
@Scope("prototype")
public class IntegrationAction extends AbstractJsonApiAction {

    private static final long serialVersionUID = -7897024503696246112L;

    @Autowired
    private transient DataIntegrationService dataIntegrationService;

    private Long ticketId;
    private String integration;

    private ModernIntegrationDataResult result;

    @Action("integrate")
    @PostOnly
    public String integrate() throws JsonParseException, JsonMappingException, IOException, IntegrationDeserializationException {
        try {
            setResult(dataIntegrationService.generateModernIntegrationResult(integration, this, getAuthenticatedUser()));
            getLogger().debug("result:{}", getResult());
            setResult(getResult());
            setTicketId(result.getTicket().getId());
            setJsonObject(getResult(), JsonIntegrationFilter.class);
        } catch (Throwable e) {
            addActionErrorWithException(e.getMessage(), e);
            return INPUT;
        }
        return SUCCESS;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public ModernIntegrationDataResult getResult() {
        return result;
    }

    public void setResult(ModernIntegrationDataResult result) {
        this.result = result;
    }

    public String getIntegration() {
        return integration;
    }

    public void setIntegration(String integration) {
        this.integration = integration;
    }
}
