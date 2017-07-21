package org.tdar.struts.action.resource;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.struts_base.action.TdarActionException;

public abstract class AbstractSupportingResourceViewAction<R extends InformationResource> extends AbstractResourceViewAction<R> {

    private static final long serialVersionUID = -1581233578894577541L;
    private ArrayList<Resource> relatedResources;

    @Autowired
    private transient DataTableService dataTableService;
    private List<DataTable> tablesUsingResource;

    @Override
    protected void loadCustomViewMetadata() throws TdarActionException {
        super.loadCustomViewMetadata();
        if (relatedResources == null) {
            relatedResources = new ArrayList<Resource>();
            for (DataTable table : getTablesUsingResource()) {
                if (!table.getDataset().isDeleted()) {
                    relatedResources.add(table.getDataset());
                }
            }
        }
    }

    public List<DataTable> getTablesUsingResource() {
        if (tablesUsingResource == null) {
            tablesUsingResource = dataTableService.findDataTablesUsingResource(getPersistable());
        }
        return tablesUsingResource;
    }

    public void setRelatedResources(ArrayList<Resource> relatedResources) {
        this.relatedResources = relatedResources;
    }

    public List<Resource> getRelatedResources() {
        return relatedResources;
    }

}
