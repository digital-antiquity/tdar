package org.tdar.odata.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.db.model.abstracts.AbstractDataRecord;

// TODO RR: This Stub is a mess at the moment. Rationalise it.

public class StubRepositoryService implements InitialisableRepositoryService {

    private Map<String, Map<String, Object>> dataTables = new HashMap<String, Map<String, Object>>();
    private Map<String, Dataset> dataSets = new HashMap<String, Dataset>();
    private List<Dataset> ownedDataSets = new ArrayList<Dataset>();
    @SuppressWarnings("unused")
    private List<String> entityNames;
    private Collection<DataTable> ownedDataTables;

    private Map<String, Map<String, Map<String, Object>>> dataModel = new HashMap<String, Map<String, Map<String, Object>>>();

    public StubRepositoryService() {
        this(new ArrayList<String>());
    }

    public StubRepositoryService(List<String> entityNames) {
        super();
        this.entityNames = entityNames;
    }

    public List<String> findOwnedRootEntityTypes(Person person) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save(DataTable dataTable) {

        Dataset dataset = dataTable.getDataset();
        String dataSetKey = dataset.getName();
        Map<String, Map<String, Object>> knownDataSetMap = dataModel.get(dataSetKey);
        if (knownDataSetMap == null) {
            knownDataSetMap = new HashMap<String, Map<String, Object>>();
            dataModel.put(dataSetKey, knownDataSetMap);
        }
        String dataTableName = dataTable.getName();
        Map<String, Object> knownDataTableMap = knownDataSetMap.get(dataTableName);
        if (knownDataTableMap == null) {
            knownDataTableMap = new HashMap<String, Object>();
            knownDataSetMap.put(dataTableName, knownDataTableMap);
        }
        knownDataTableMap.put(dataTableName, dataTable);
    }

    @Override
    public void saveOwnedDatasetByName(String dataSetName, Dataset dataset) {
        dataSets.put(dataSetName, dataset);
    }

    @Override
    public void saveValueByTableNameAndPropertyName(String dataTableName, String propertyName, Object propertyValue) {
        dataTables.get(dataTableName).put(propertyName, propertyValue);
    }

    @Override
    public List<Dataset> findAllOwnedDatasets() {
        return ownedDataSets;
    }

    @Override
    public Collection<DataTable> findAllOwnedDataTables() {
        return ownedDataTables;
    }

    @Override
    public List<AbstractDataRecord> findAllOwnedDataRecords() {
        return new ArrayList<AbstractDataRecord>();
    }

    @Override
    public List<AbstractDataRecord> findAllDataRecordsForDataTable(DataTable dataTable) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateRecord(AbstractDataRecord dataRecord) {
        // TODO Auto-generated method stub
    }

    @Override
    public DataTable findOwnedDataTableByName(String entitySetName) {
        // TODO Auto-generated method stub
        return null;
    }

    // Initialisation

    @Override
    public void saveOwnedDatasets(List<Dataset> ownedDataSets) {
        this.ownedDataSets = ownedDataSets;
    }

    @Override
    public void saveOwnedDataTables(Collection<DataTable> ownedDataTables) {
        this.ownedDataTables = ownedDataTables;
    }

}
