package org.tdar.core.dao.integration;

import java.util.ArrayList;
import java.util.List;

import org.tdar.utils.json.JsonIntegrationFilter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

@JsonAutoDetect
public class IntegrationDataTableSearchResult extends AbstractIntegrationSearchResult {

    private static final long serialVersionUID = 6591331482228872881L;
    private List<DataTableProxy> dataTables = new ArrayList<>();

    @JsonView(JsonIntegrationFilter.class)
    public List<DataTableProxy> getDataTables() {
        return dataTables;
    }

    public void setDataTables(List<DataTableProxy> dataTables) {
        this.dataTables = dataTables;
    }

}
