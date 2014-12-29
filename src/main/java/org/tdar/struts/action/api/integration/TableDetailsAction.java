package org.tdar.struts.action.api.integration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.dao.integration.TableDetailsProxy;
import org.tdar.core.service.DataIntegrationService;
import org.tdar.utils.json.JsonIntegrationDetailsFilter;

@Namespace("/api/integration")
@ParentPackage("secured")
@Component
@Scope("prototype")
public class TableDetailsAction extends AbstractIntegrationAction {

    private static final long serialVersionUID = 5956429144322203034L;
    private List<Long> dataTableIds = new ArrayList<>();

    @Autowired
    DataIntegrationService integrationService;

    @Action(value = "table-details")
    public String dataTableDetails() throws IOException {
        TableDetailsProxy proxy  = integrationService.getTableDetails(dataTableIds);
        setJsonObject(proxy, JsonIntegrationDetailsFilter.class);
        return SUCCESS;
    }

    public List<Long> getDataTableIds() {
        return dataTableIds;
    }

    public void setDataTableIds(List<Long> dataTableIds) {
        this.dataTableIds = dataTableIds;
    }

}
