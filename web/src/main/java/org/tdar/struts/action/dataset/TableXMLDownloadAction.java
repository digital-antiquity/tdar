package org.tdar.struts.action.dataset;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/dataset")
public class TableXMLDownloadAction extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = -4731261436258813618L;
    @Autowired
    private transient DatasetService datasetService;
    @Autowired
    private transient DataTableService dataTableService;
    @Autowired
    private transient AuthorizationService authorizationService;

    private InputStream xmlStream;
    private Long id;
    private Long dataTableId;
    private DataTable dataTable;
    private Dataset persistable;

    /**
     * @return The output of the xml request to the database wrapped in a stream
     */
    public InputStream getXmlStream() {
        return xmlStream;
    }

    /**
     * Used to return the contents of a table in a {@link Dataset} wrapped in XML.
     * The expected URL is of the form /dataset/xml?dataTableId=5818 where dataTableId = data table id
     * 
     * @return com.opensymphony.xwork2.SUCCESS if able to find and display the table, com.opensymphony.xwork2.ERROR if not.
     */
    @Action(value = "xml",
            results = {
                    @Result(name = TdarActionSupport.SUCCESS,
                            type = "stream",
                            params = {
                                    "contentDisposition", "attachment; filename=\"${dataTable.name}.xml\"",
                                    "contentType", "text/xml",
                                    "inputName", "xmlStream",
                            }),
                    @Result(name = TdarActionSupport.ERROR, type = HTTPHEADER, params = { "error", "404" }),
            })
    @SkipValidation
    public String getTableAsXml() {
        xmlStream = null;
        if (!getTdarConfiguration().isXmlExportEnabled()) {
            return ERROR;
        }
        if (PersistableUtils.isNullOrTransient(dataTableId)) {
            return ERROR;
        }
        DataTable dataTable = dataTableService.find(dataTableId);
        if (dataTable != null) {
            if (authorizationService.canViewConfidentialInformation(getAuthenticatedUser(), persistable)) {
                String dataTableAsXml = datasetService.selectTableAsXml(dataTable);
                xmlStream = new ByteArrayInputStream(dataTableAsXml.getBytes(StandardCharsets.UTF_8));
                return SUCCESS;
            }
        }
        return ERROR;
    }

    @Override
    public void prepare() throws Exception {
        persistable = datasetService.find(getId());
        if (dataTableId != null) {
            this.dataTable = dataTableService.find(dataTableId);
            if (persistable == null) {
                persistable = dataTableService.findDatasetForTable(dataTable);
            }
        } else {
            Set<DataTable> dataTables = persistable.getDataTables();
            if (!CollectionUtils.isEmpty(dataTables)) {
                dataTable = dataTables.iterator().next();
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDataTableId() {
        return dataTableId;
    }

    public void setDataTableId(Long dataTableId) {
        this.dataTableId = dataTableId;
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public Dataset getPersistable() {
        return persistable;
    }

    public void setPersistable(Dataset persistable) {
        this.persistable = persistable;
    }

}
