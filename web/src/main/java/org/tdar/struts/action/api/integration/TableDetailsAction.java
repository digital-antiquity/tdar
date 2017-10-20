package org.tdar.struts.action.api.integration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.dao.integration.TableDetailsProxy;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.integration.DataIntegrationService;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.utils.json.JsonIntegrationDetailsFilter;

import com.opensymphony.xwork2.Validateable;

@Namespace("/api/integration")
@ParentPackage("secured")
@Component
@Scope("prototype")
public class TableDetailsAction extends AbstractJsonApiAction implements Validateable {

    private static final long serialVersionUID = 5956429144322203034L;
    private List<Long> dataTableIds = new ArrayList<>();

    @Autowired
    DataIntegrationService integrationService;

    @Autowired
    AuthorizationService authorizationService;

    @Action(value = "table-details")
    public String dataTableDetails() throws IOException {
        TableDetailsProxy proxy = integrationService.getTableDetails(dataTableIds);
        setJsonObject(proxy, getJsonView());
        return SUCCESS;
    }

    public Class<?> getJsonView() {
        return JsonIntegrationDetailsFilter.class;
    }

    public List<Long> getDataTableIds() {
        return dataTableIds;
    }

    public void setDataTableIds(List<Long> dataTableIds) {
        this.dataTableIds = dataTableIds;
    }

    @Override
    public void validate() {
        super.validate();
        List<DataTable> tables = getGenericService().findAll(DataTable.class, dataTableIds);
        for (DataTable table : tables) {
            authorizationService.canView(getAuthenticatedUser(), table.getDataset());
            getActionErrors().add(getText("tableDetailsAction.cannot_integrate", Arrays.asList(table.getDataset().getTitle())));
        }
    }
}
