package org.tdar.struts.action.dataset;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.resource.AbstractResourceViewAction;
import org.tdar.utils.PersistableUtils;

public abstract class AbstractDatasetViewAction<D extends Dataset> extends AbstractResourceViewAction<D> {

    private static final long serialVersionUID = -7657008098263870208L;
    private Long dataTableId;
    private InputStream xmlStream;
    private DataTable dataTable;
    private String dataTableColumnJson;

    @Autowired
    private transient DataTableService dataTableService;

    @Autowired
    private transient SerializationService serializationService;

    /**
     * @return The output of the xml request to the database wrapped in a stream
     */
    public InputStream getXmlStream() {
        return xmlStream;
    }

    @Override
    protected void loadCustomViewMetadata() throws TdarActionException {
        super.loadCustomViewMetadata();
        List<Map<String, Object>> result = new ArrayList<>();
        if (PersistableUtils.isNotNullOrTransient(getDataTable())) {
            for (DataTableColumn dtc : getDataTable().getSortedDataTableColumnsByImportOrder()) {
                Map<String, Object> col = new HashMap<>();
                col.put("simpleName", dtc.getJsSimpleName());
                col.put("displayName", dtc.getDisplayName());
                result.add(col);
            }
        }
        try {
            setDataTableColumnJson(serializationService.convertToJson(result));
        } catch (Exception e) {
            getLogger().error("cannot convert to JSON: {}", e);
        }
    }

    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public DataTable getDataTable() {
        getLogger().trace(dataTable + " dtID:" + dataTableId);
        if (dataTable == null) {
            if (dataTableId != null) {
                this.dataTable = dataTableService.find(dataTableId);
            } else {
                Set<DataTable> dataTables = ((Dataset) getResource()).getDataTables();
                if (!CollectionUtils.isEmpty(dataTables)) {
                    dataTable = dataTables.iterator().next();
                }
            }
        }
        return dataTable;
    }

    public void setDataTableId(Long dataTableId) {
        if (PersistableUtils.isNullOrTransient(dataTableId)) {
            getLogger().error("Trying to set data table id to null or -1: " + dataTableId);
            return;
        }
        this.dataTableId = dataTableId;
        // this.dataTable = dataTableService.find(dataTableId);
    }

    public Long getDataTableId() {
        return dataTableId;
    }

    public void setXmlStream(InputStream xmlStream) {
        this.xmlStream = xmlStream;
    }

    public String getDataTableColumnJson() {
        return dataTableColumnJson;
    }

    public void setDataTableColumnJson(String dataTableColumnJson) {
        this.dataTableColumnJson = dataTableColumnJson;
    }

    
    public boolean isMappingFeatureEnabled() {
        if (PersistableUtils.isNullOrTransient(getPersistable())) {
            return false;
        }

        if (getPersistable().getProject() == Project.NULL) {
            return false;
        }
        
        for (DataTable dt : getPersistable().getDataTables()) {
            if (!CollectionUtils.isEmpty(dt.getFilenameColumns())) {
                return true;
            }
        }
        return false;
    }
}
