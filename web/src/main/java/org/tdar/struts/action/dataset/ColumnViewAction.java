package org.tdar.struts.action.dataset;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Namespaces;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespaces(value = {
        @Namespace("/dataset/column"),
        @Namespace("/geospatial/column"),
        @Namespace("/sensory-data/column")
})
public class ColumnViewAction extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = -7340667869470197516L;
    @Autowired
    private transient DatasetService datasetService;

    @Autowired
    private transient AuthorizationService authorizationService;

    private Long id;
    private Long columnId;
    private DataTableColumn dataTableColumn;
    private Dataset persistable;

    @Action(value = "{id}/{columnId}", results = {
            @Result(name = SUCCESS, location = "../../dataset/view-column.ftl") })
    public String getDataResultsRow() {
        try {
        setTransientViewableStatus(getResource(), getAuthenticatedUser());
        if (PersistableUtils.isNullOrTransient(getColumnId())) {
            return ERROR;
        }
        if (getDataTableColumn() != null) {
            if (authorizationService.canViewConfidentialInformation(getAuthenticatedUser(), getResource())) {
                return SUCCESS;
            }
        }
        } catch (Throwable t) {
            getLogger().error("{}",t,t);
        }
        return ERROR;
    }

    @Override
    public void prepare() throws Exception {
        persistable = datasetService.find(getId());
        if (getColumnId() != null) {
            this.setDataTableColumn(getGenericService().find(DataTableColumn.class,getColumnId()));
        }
        
        if (!authorizationService.canViewConfidentialInformation(getAuthenticatedUser(), getResource())) {
            addActionError("error.permission_denied");
        }

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Dataset getPersistable() {
        return persistable;
    }

    public void setPersistable(Dataset persistable) {
        this.persistable = persistable;
    }

    public Dataset getDataset() {
        return persistable;
    }

    public Dataset getResource() {
        return persistable;
    }

    /*
     * Creating a simple transient boolean to handle visibility here instead of freemarker
     */
    public void setTransientViewableStatus(InformationResource ir, TdarUser p) {
        authorizationService.applyTransientViewableFlag(ir, p);
    }

    public DataTableColumn getDataTableColumn() {
        return dataTableColumn;
    }

    public void setDataTableColumn(DataTableColumn dataTableColumn) {
        this.dataTableColumn = dataTableColumn;
    }

    public Long getColumnId() {
        return columnId;
    }

    public void setColumnId(Long columnId) {
        this.columnId = columnId;
    }
}
